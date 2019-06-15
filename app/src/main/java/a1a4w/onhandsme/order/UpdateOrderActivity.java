package a1a4w.onhandsme.order;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.Employee;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.model.Promotion;
import a1a4w.onhandsme.model.VatModel;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.Utils;

import static a1a4w.onhandsme.utils.Constants.refDatabase;
import static a1a4w.onhandsme.utils.Constants.refOrderList;

public class UpdateOrderActivity extends AppCompatActivity {

    private Spinner spinSales, spinPayment,spinProduct, spinUnitName,spinAddPromotion,spinVAT;
    private ImageButton addPromotion;
    private Bundle b = new Bundle();
    private EditText edtproductPrice, edtproductQuantity, edtdeliveryDate,edtSpecialDiscount, edtDialogProductQuantity, edtOrderDiscount, edtOrderNote;
    private TextView currentStorage,tvClientName, tvClientSale, clientDebt,tvEmployeeName,tvProductName,tvEmployeeMonthSale,tvPromotionName;
    private String orderDiscount, orderPushKeyString, clientCode,paymentType,promotionName,productStock,
            clientType, employeeName,productName,choosenVAT,emailLogin,clientName,saleManEmail;
    private Switch switchPayment;
    private Button btnChooseEmployee,btnChooseProduct,btnChoosePromotion,btnPreview;
    private DatabaseReference orderPushKey;
    private FirebaseRecyclerAdapter<Employee,EmployeeViewHolder> adapterFirebase;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct;
    private FirebaseRecyclerAdapter<Promotion,PromotionViewHolder> adapterFirebasePromotion;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView employeeList,programList;
    private AlertDialog.Builder dialogBuilder;
    private View dialogView;
    private LayoutInflater inflater;
    private ProgressDialog mProgressDialog;
    private Menu myMenu;
    private boolean dialogPromotion=false,isDebt,saleMan,outRoute;
    private Dialog dialogProductList,dialogProgramList,dialogEmployeeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_order);
        setSupportActionBar(toolbar);

        orderPushKeyString = refOrderList.push().getKey();
       // b.putString("OrderPushKey",orderPushKeyString);
        saleManEmail =FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

        Intent intent = this.getIntent();
        clientCode = intent.getStringExtra("ClientCode");
        emailLogin = intent.getStringExtra("EmailLogin");
        saleMan = intent.getBooleanExtra("SaleMan",false);
        outRoute = intent.getBooleanExtra("OutRoute",false);
       // employeeName = intent.getStringExtra("EmployeeName");
        getClientDebt();
        initilizeScreen();
        chooseAction();

    }

    private void chooseAction() {

        btnChooseEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                employeeListDialog();

            }
        });

        btnChooseProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                productListDialog();

            }
        });

        btnChoosePromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                programListDialog();
            }
        });

    }

    private void getClientDebt() {
        //Get Client debt
        refDatabase.child(emailLogin+"/Client").child(clientCode).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Client client = dataSnapshot.getValue(Client.class);
                String clientDebtData = client.getClientDebt();
                String clientSale = client.getClientSale();
                clientName = client.getClientName();
                tvClientName.setText(clientName);
                tvClientSale.setText(Utils.convertNumber(clientSale));
                clientDebt.setText(Utils.convertNumber(clientDebtData));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("CutPasteId")
    private void initilizeScreen() {

        DateTime dt = new DateTime();
        String month = dt.getMonthOfYear()+"";
        String year = dt.getYear()+"";

        //Toast.makeText(getApplicationContext(), year+"-"+month, Toast.LENGTH_LONG).show();

        spinVAT = (Spinner)findViewById(R.id.spin_order_vat);
        edtproductPrice = (EditText) findViewById(R.id.edt_order_product_price);
        edtproductQuantity = (EditText) findViewById(R.id.edt_order_product_quantity);
        edtdeliveryDate = (EditText) findViewById(R.id.edt_order_date_delivery);
        edtSpecialDiscount = (EditText) findViewById(R.id.edt_order_special_discount);
        edtOrderNote = findViewById(R.id.edt_order_note);

        tvEmployeeName = (TextView)findViewById(R.id.tv_order_employee_name);
        tvEmployeeMonthSale = findViewById(R.id.tv_order_employee_month_sale);
        tvProductName = (TextView)findViewById(R.id.tv_order_product_name);
        tvPromotionName = findViewById(R.id.tv_order_promotion_name);
        tvClientName = findViewById(R.id.tv_order_client_name);
        tvClientSale = findViewById(R.id.tv_order_client_sale);
        currentStorage = (TextView) findViewById(R.id.tv_order_product_stock);
        clientDebt = (TextView)findViewById(R.id.tv_order_client_debt);
        //switchPromotion = (Switch) findViewById(R.id.switch_update_order);
        switchPayment = (Switch) findViewById(R.id.sw_order_payment);

        btnChooseEmployee = findViewById(R.id.btn_order_choose_employee);
        btnChooseProduct = findViewById(R.id.btn_order_choose_product);
        btnChoosePromotion = findViewById(R.id.btn_order_choose_promotion);

        if(saleMan){
            btnChooseEmployee.setVisibility(View.GONE);
            refDatabase.child(emailLogin).child("Employee").child(saleManEmail).child("employeeName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    employeeName  = dataSnapshot.getValue().toString();
                    //Toast.makeText(getApplicationContext(),name,Toast.LENGTH_LONG).show();
                    tvEmployeeName.setText(employeeName);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            refDatabase.child(emailLogin).child("RevenueBySale").child(saleManEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tvEmployeeMonthSale.setText(Utils.convertNumber(dataSnapshot.getValue().toString()));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else{
            btnChooseEmployee.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    employeeListDialog();

                }
            });
        }

        tvProductName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogPromotion = false;
                v.startAnimation(Constants.buttonClick);
                productListDialog();

            }
        });

        switchPayment.setChecked(false);


        switchPayment.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    switchPayment.setText("Công nợ");
                    isDebt = true;


                }else {
                    switchPayment.setText("Tiền mặt");
                    isDebt = false;

                }
            }
        });


        spinVAT.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosenVAT = (String) parent.getItemAtPosition(position);
                //b.putString("ChoosenVAT",choosenVAT);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    private void productListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_product_list,null);
        builder.setView(dialogView);
        builder.setMessage("Chọn sản phẩm (trượt dọc để xem tiếp)");

        dialogProductList = builder.create();


        final RecyclerView productList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_product_list);
        productList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productList.setLayoutManager(linearLayoutManager);

        adapterFirebaseProduct = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product_pos,
                ProductViewHolder.class,
                refDatabase.child(emailLogin+"/Product")
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_pos,parent,false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.name.setText(model.getProductName());

            }
        };

        productList.setAdapter(adapterFirebaseProduct);
        adapterFirebaseProduct.notifyDataSetChanged();

        dialogProductList.show();
    }


    private void employeeListDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_employee_list,null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setMessage("Chọn nhân viên (trượt dọc để xem tiếp)");

        dialogEmployeeList = dialogBuilder.create();
        dialogEmployeeList.show();


        employeeList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_employee_list);
        employeeList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        employeeList.setLayoutManager(linearLayoutManager);

        adapterFirebase = new FirebaseRecyclerAdapter<Employee, EmployeeViewHolder>(
                Employee.class,
                R.id.item_client,
                EmployeeViewHolder.class,
                refDatabase.child(emailLogin).child("Employee")
        ) {
            @Override
            public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client,parent,false);
                return new EmployeeViewHolder(v);
            }


            @Override
            protected void populateViewHolder(EmployeeViewHolder viewHolder, Employee model, int position) {
                viewHolder.mEmployeeName.setText(model.getEmployeeName());

            }
        };

        employeeList.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();

    }

    private void programListDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_program_list,null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setMessage("Chọn chương  (trượt dọc để xem tiếp)");

        dialogProgramList = dialogBuilder.create();
        dialogProgramList.show();

        programList = (RecyclerView)dialogView.findViewById(R.id.rv_program_list);
        programList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        programList.setLayoutManager(linearLayoutManager);

        adapterFirebasePromotion = new FirebaseRecyclerAdapter<Promotion, PromotionViewHolder>(
                Promotion.class,
                R.layout.item_promotion,
                PromotionViewHolder.class,
                refDatabase.child(emailLogin).child("PromotionMan")
        ) {
            @Override
            public PromotionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion,parent,false);
                return new PromotionViewHolder(v);
            }


            @Override
            protected void populateViewHolder(PromotionViewHolder viewHolder, Promotion model, int position) {
                viewHolder.promotionName.setText(model.getPromotionName());

            }
        };

        programList.setAdapter(adapterFirebasePromotion);
        adapterFirebasePromotion.notifyDataSetChanged();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        myMenu = menu;
        getMenuInflater().inflate(R.menu.menu_update_order,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_preview){

            sendForPreview();

        }
        return super.onOptionsItemSelected(item);
    }

    private void testPreview(){

        final String productQuantity = edtproductQuantity.getText().toString();
        final String productPrice = edtproductPrice.getText().toString();
        final String deliveryDate = edtdeliveryDate.getText().toString();
        final String specialDiscount = edtSpecialDiscount.getText().toString();
        final String orderNote = edtOrderNote.getText().toString();

        if(TextUtils.isEmpty(productPrice)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập giá sản phẩm",Toast.LENGTH_LONG).show();

        }else if(TextUtils.isEmpty(productQuantity)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập số lượng sản phẩm",Toast.LENGTH_LONG).show();

        }else if(TextUtils.isEmpty(deliveryDate)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập ngày giao hàng",Toast.LENGTH_LONG).show();

        }
        else if(employeeName == null){
            Toast.makeText(getApplicationContext(),"Vui lòng chọn nhân viên",Toast.LENGTH_LONG).show();

        }else if(productName == null){
            Toast.makeText(getApplicationContext(),"Vui lòng chọn sản phẩm",Toast.LENGTH_LONG).show();

        }
        else if(Float.parseFloat(productQuantity)>Float.parseFloat(productStock)){
            Toast.makeText(getApplicationContext(),"Không đủ hàng tồn kho", Toast.LENGTH_LONG).show();
            edtproductQuantity.setText("");

        }
        else {


            float notVAT = Float.parseFloat(productPrice)*Float.parseFloat(productQuantity);
            float choosenVATLong = Float.parseFloat(choosenVAT)/100;
            final float discount = (orderDiscount != null)? Float.parseFloat(orderDiscount)/100 : 0;
            final float special = (!TextUtils.isEmpty(specialDiscount))? Float.parseFloat(specialDiscount)/100 : 0;
            final float totalDis = discount + special;

            Toast.makeText(getApplicationContext(), totalDis+"", Toast.LENGTH_LONG).show();


        }
    }

    private void sendForPreview() {

        //myMenu.findItem(R.id.action_preview).setVisible(false);

        final String productQuantity = edtproductQuantity.getText().toString();
        final String productPrice = edtproductPrice.getText().toString();
        final String deliveryDate = edtdeliveryDate.getText().toString();
        final String specialDiscount = edtSpecialDiscount.getText().toString();
        final String orderNote = edtOrderNote.getText().toString();

        if(TextUtils.isEmpty(productPrice)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập giá sản phẩm",Toast.LENGTH_LONG).show();

        }else if(TextUtils.isEmpty(productQuantity)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập số lượng sản phẩm",Toast.LENGTH_LONG).show();

        }else if(TextUtils.isEmpty(deliveryDate)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập ngày giao hàng",Toast.LENGTH_LONG).show();

        }
        else if(employeeName == null){
            Toast.makeText(getApplicationContext(),"Vui lòng chọn nhân viên",Toast.LENGTH_LONG).show();

        }else if(productName == null){
            Toast.makeText(getApplicationContext(),"Vui lòng chọn sản phẩm",Toast.LENGTH_LONG).show();

        }
        else if(Float.parseFloat(productQuantity)>Float.parseFloat(productStock)){
            Toast.makeText(getApplicationContext(),"Không đủ hàng tồn kho", Toast.LENGTH_LONG).show();
            edtproductQuantity.setText("");

        }
        else {

            showProgressDialog();

            float notVAT = Float.parseFloat(productPrice)*Float.parseFloat(productQuantity);
            float choosenVATLong = Float.parseFloat(choosenVAT)/100;

            final float discount = (orderDiscount != null)? Float.parseFloat(orderDiscount)/100 : 0;
            final float special = (!TextUtils.isEmpty(specialDiscount))? Float.parseFloat(specialDiscount)/100 : 0;
            final float totalDis = discount + special;

            //Toast.makeText(getApplicationContext(), totalDis+"", Toast.LENGTH_LONG).show();

            //float VAT =  notVAT*(1+choosenVATLong);
            float VAT = (float) (Math.round(notVAT*(1+choosenVATLong)*10d)/10d);

            OrderDetail orderDetail = new OrderDetail(clientCode,clientName,employeeName,switchPayment.getText().toString(),deliveryDate,saleManEmail,orderNote);

            refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).child("OtherInformation").setValue(orderDetail);
            final Product currentProduct = new Product(productName,productPrice,productQuantity,choosenVAT,totalDis+"");

            refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).child("ProductList").push().setValue(currentProduct);

            float finalPayment = VAT * (1-totalDis);

            VatModel vat = new VatModel(notVAT,VAT,finalPayment);

            refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).child("VAT").setValue(vat).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    hideProgressDialog();
                    edtproductQuantity.setText("");
                    edtproductPrice.setText("");
                    edtdeliveryDate.setText("");

                    Intent intent = new Intent(UpdateOrderActivity.this, PreviewOrderActivivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    intent.putExtra("EmailLogin", emailLogin);
                    intent.putExtra("OrderPushKey", orderPushKeyString);
                    intent.putExtra("OrderName", clientName);
                    intent.putExtra("OrderDiscount",totalDis+"");
                    intent.putExtra("VAT",choosenVAT);
                    intent.putExtra("ClientCode",clientCode);
                    intent.putExtra("OutRoute",outRoute);
                    intent.putExtra("SaleMan", saleMan);
                    startActivity(intent);

                }

            });

            /*

            if(discountVAT){

                final Product currentProduct = new Product(productName,productPrice,productQuantity,choosenVAT,VATDiscount+"");

                refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).child("ProductList").push().setValue(currentProduct);

                VatModel vat = new VatModel(notVAT+"",VAT+"",notVATDiscount+"",VATDiscount+"",VATDiscount+"");

                refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).child("VAT").setValue(vat).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        edtproductQuantity.setText("");
                        edtproductPrice.setText("");
                        edtdeliveryDate.setText("");

                        Intent intent = new Intent(UpdateOrderActivity.this, PreviewOrderActivivity.class);

                        intent.putExtra("EmailLogin", emailLogin);
                        intent.putExtra("OrderPushKey", orderPushKeyString);
                        intent.putExtra("OrderName", orderName);
                        intent.putExtra("OrderDiscount",orderDiscount);
                        intent.putExtra("DiscountTax",discountVAT);
                        intent.putExtra("ClientCode",clientCode);

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });

            }else{
                VatModel vat = new VatModel(notVAT+"",VAT+"",notVATDiscount+"",VATDiscount+"",notVATDiscount+"");
                final Product currentProduct = new Product(productName,productPrice,productQuantity,choosenVAT,notVATDiscount+"");

                refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).child("ProductList").push().setValue(currentProduct);

                refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).child("VAT").setValue(vat)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        edtproductQuantity.setText("");
                        edtproductPrice.setText("");
                        edtdeliveryDate.setText("");

                        Intent intent = new Intent(UpdateOrderActivity.this, PreviewOrderActivivity.class);

                        intent.putExtra("EmailLogin", emailLogin);
                        intent.putExtra("OrderPushKey", orderPushKeyString);
                        intent.putExtra("OrderDiscount",orderDiscount);
                        intent.putExtra("OrderName", orderName);
                        intent.putExtra("DiscountTax",discountVAT);
                        intent.putExtra("ClientCode",clientCode);

                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).setValue(null);
                        Toast.makeText(getApplicationContext(),"Xin lỗi, đã xảy ra lỗi ghi dữ liệu. Vui lòng thực hiện lại",Toast.LENGTH_LONG).show();
                    }
                });
            }

            */

        }

    }

    public class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView mEmployeeName;

        public EmployeeViewHolder(View itemView) {
            super(itemView);
            mEmployeeName = (TextView) itemView.findViewById(R.id.tv_item_client_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    if(dialogEmployeeList!=null) dialogEmployeeList.dismiss();

                    int position = getLayoutPosition();
                    DatabaseReference keyRef = adapterFirebase.getRef(position);
                    Employee employee = adapterFirebase.getItem(position);
                    employeeName = keyRef.getKey();

                    tvEmployeeName.setText(employeeName);

                }
            });

        }
    }
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name;

    public ProductViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_item_product_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    btnChooseProduct.setVisibility(View.GONE);
                    if(dialogProductList!=null) dialogProductList.dismiss();
                    int position = getLayoutPosition();
                    DatabaseReference keyRef = adapterFirebaseProduct.getRef(position);
                    String keyString = keyRef.getKey();
                    b.putString("ProductCode",keyString);

                    final Product p = adapterFirebaseProduct.getItem(position);

                    productName = p.getProductName();
                    edtproductPrice.setText(p.getUnitPrice());

                    tvProductName.setText(productName);

                    refDatabase.child(emailLogin+"/WarehouseMan/StorageMan").child(p.getProductName()).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            productStock = dataSnapshot.getValue().toString();
                            currentStorage.setText(productStock);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });

        }
    }
    public class PromotionViewHolder extends RecyclerView.ViewHolder {
        TextView promotionName;

        public PromotionViewHolder(View itemView) {
            super(itemView);
            promotionName = (TextView) itemView.findViewById(R.id.tv_item_promotion_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    btnChoosePromotion.setVisibility(View.GONE);
                    if(dialogProgramList!=null) dialogProgramList.dismiss();
                    int position = getLayoutPosition();
                    DatabaseReference keyRef = adapterFirebasePromotion.getRef(position);

                    Promotion p = adapterFirebasePromotion.getItem(position);

                    tvPromotionName.setText(p.getPromotionName());

                    orderDiscount = p.getPromotionDiscount();

                    refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).child("Promotion").push().child("promotionName").setValue(p.getPromotionName());


                }
            });

        }
    }


    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).setValue(null);
    }
}

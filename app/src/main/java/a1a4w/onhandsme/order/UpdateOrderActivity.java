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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Employee;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.model.VatModel;
import a1a4w.onhandsme.utils.Constants;

import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class UpdateOrderActivity extends AppCompatActivity {

    private Spinner spinSales, spinPayment,spinProduct, spinUnitName,spinAddPromotion,spinVAT;
    private ImageButton addPromotion;
    private Bundle b = new Bundle();
    private EditText edtproductPrice, edtproductQuantity, edtdeliveryDate,edtDialogProductPrice, edtDialogProductQuantity, edtOrderDiscount, edtOrderNote;
    private TextView currentStorage,dialogCurrentStorage, orderTitle, clientDebt,tvEmployeeName,tvProductName,tvChoosenPromotionProduct,tvChoosenPromotionProductStorage;
    private String orderName, orderPushKeyString, clientCode,paymentType,promotionName,promotionQuantity,
            clientType, employeeName,choosenPayment,choosenVAT,emailLogin,choosenPromotionProduct,saleManEmail;
    private Switch switchPromotion,switchDiscount;
    private Button btnDialogAddPromotion,btnDialogeCancel;
    private DatabaseReference orderPushKey;
    private FirebaseRecyclerAdapter<Employee,EmployeeViewHolder> adapterFirebase;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView employeeList;
    private AlertDialog.Builder dialogBuilder;
    private View dialogView;
    private LayoutInflater inflater;
    private ProgressDialog mProgressDialog;
    private Menu myMenu;
    private boolean dialogPromotion=false,discountVAT,saleMan;
    private Dialog dialogProductList;
    private Dialog dialogEmployeeList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_order);

        orderPushKeyString = Constants.refOrderList.push().getKey();
       // b.putString("OrderPushKey",orderPushKeyString);
        saleManEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail().replace(".",",");



        Intent intent = this.getIntent();
        orderName = intent.getStringExtra("ClientName");
        clientCode = intent.getStringExtra("ClientCode");
        clientType = intent.getStringExtra("ClientType");
        emailLogin = intent.getStringExtra("EmailLogin");
        saleMan = intent.getBooleanExtra("SaleMan",false);
       // employeeName = intent.getStringExtra("EmployeeName");
        getClientDebt();
        initilizeScreen();

    }

    private void getClientDebt() {
        //Get Client debt
        refDatabase.child(emailLogin+"/Client").child(clientCode).child("clientDebt").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String clientDebtData = dataSnapshot.getValue().toString();
                NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
                float clientDebtFloat = Float.parseFloat(clientDebtData);
                String clientDebtFloatConverted = numberFormat.format(clientDebtFloat);
                clientDebt.setText(clientDebtFloatConverted);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void addPromotionDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_promotion, null);

        //spinAddPromotion = (Spinner)dialogView.findViewById(R.id.spinner_dialog_warehouse_in_product);
        //dialogCurrentStorage = (TextView)dialogView.findViewById(R.id.tv_add_promotion_storage);
        edtDialogProductPrice = (EditText)dialogView.findViewById(R.id.edt_add_product_price);
        edtDialogProductQuantity = (EditText)dialogView.findViewById(R.id.edt_add_product_quantity);
        btnDialogAddPromotion = (Button)dialogView.findViewById(R.id.btn_add_product);
        btnDialogeCancel = (Button)dialogView.findViewById(R.id.btn_dialog_cancel);
        tvChoosenPromotionProduct = (TextView)dialogView.findViewById(R.id.tv_dialog_add_promotion_choosen_product);
        tvChoosenPromotionProductStorage = (TextView)dialogView.findViewById(R.id.tv_add_promotion_storage);

        tvChoosenPromotionProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                dialogPromotion = true;
                productListDialog();

                //choosenPromotionProduct = tvChoosenPromotionProduct.getText().toString();
            }
        });


        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Thêm hàng khuyến mãi");

        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();
        btnDialogeCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                dialog.dismiss();
            }
        });

        btnDialogAddPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                final String promotionName = b.getString("PromotionName");
                String promotionStorage = b.getString("PromotionStorage");
                final float promotionStorageFloat = Float.parseFloat(promotionStorage);
                //String orderPushKey = b.getString("OrderPushKey");
                String promotionPrice = edtDialogProductPrice.getText().toString();
                promotionQuantity = edtDialogProductQuantity.getText().toString();
                float promotionQuantityFloat = Float.parseFloat(promotionQuantity);
                String unitName = b.getString("ChoosenUnit");
                Product promotion = new Product(promotionName,promotionQuantity);

                if(TextUtils.isEmpty(promotionPrice)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập đơn giá", Toast.LENGTH_LONG).show();

                }else if (TextUtils.isEmpty(promotionQuantity)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập số lượng", Toast.LENGTH_LONG).show();

                }else if (promotionQuantityFloat>promotionStorageFloat){
                    Toast.makeText(getApplicationContext(),"Không đủ hàng tồn kho.", Toast.LENGTH_LONG).show();

                }
                else {
                    btnDialogAddPromotion.setEnabled(false);

                    refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).child("Promotion").push().setValue(promotion).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();
                        }
                    });

                }
            }
        });
    }

    @SuppressLint("CutPasteId")
    private void initilizeScreen() {

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_update_order);
        setSupportActionBar(toolbar);

        spinPayment = (Spinner)findViewById(R.id.spinner_update_order_payment);
        //spinProduct = (Spinner)findViewById(R.id.spinner_product_name);
        spinVAT = (Spinner)findViewById(R.id.spinner_VAT);
        edtproductPrice = (EditText) findViewById(R.id.edt_update_order_product_price);
        edtproductQuantity = (EditText) findViewById(R.id.edt_product_quantity);
        edtdeliveryDate = (EditText) findViewById(R.id.edt_update_order_date);
        edtOrderDiscount = (EditText) findViewById(R.id.edt_update_order_discount);
        edtOrderNote = (EditText) findViewById(R.id.edt_update_order_note);

        tvEmployeeName = (TextView)findViewById(R.id.tv_update_order_employee);
        tvProductName = (TextView)findViewById(R.id.tv_update_order_productName);
        currentStorage = (TextView) findViewById(R.id.tv_update_order_current_storage);
        orderTitle = (TextView)findViewById(R.id.tv_update_order_name);
        clientDebt = (TextView)findViewById(R.id.tv_update_order_debt);
        //switchPromotion = (Switch) findViewById(R.id.switch_update_order);
        switchDiscount = (Switch) findViewById(R.id.sw_discount_type);

        orderTitle.setText(orderName);

        if(saleMan){
            refDatabase.child(emailLogin).child("Employee").child(saleManEmail).child("employeeName").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.getValue().toString();
                    Toast.makeText(getApplicationContext(),name,Toast.LENGTH_LONG).show();
                    tvEmployeeName.setText(name);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }else{
            tvEmployeeName.setOnClickListener(new View.OnClickListener() {
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

        switchDiscount.setChecked(true);
        discountVAT = true;

        switchDiscount.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    switchDiscount.setText("Chiết khấu sau VAT");
                    discountVAT = true;
                }else {
                    switchDiscount.setText("Chiết khấu trước VAT");
                    discountVAT = false;

                }
            }
        });


        Spinner spinnerVAT = (Spinner) findViewById(R.id.spinner_VAT);

        spinnerVAT.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosenVAT = (String) parent.getItemAtPosition(position);
                //b.putString("ChoosenVAT",choosenVAT);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                paymentType = (String) parent.getItemAtPosition(position);
                //b.putString("PaymentType",choosenPayment);
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

        Button btnNew = dialogView.findViewById(R.id.btn_product_list_new);
        btnNew.setVisibility(View.GONE);

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                addNewProductDialog();
            }
        });

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
                viewHolder.productName.setText(model.getProductName());

            }
        };

        productList.setAdapter(adapterFirebaseProduct);
        adapterFirebaseProduct.notifyDataSetChanged();

        dialogProductList.show();
    }

    private void addNewProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_product,null);
        builder.setView(dialogView);
        builder.setMessage("Thêm sản phẩm mới");

        final Dialog dialogProduct = builder.create();
        dialogProduct.show();

        final EditText edtAddProduct = (EditText)dialogView.findViewById(R.id.edt_dialog_add_product_name);
        final EditText edtStorage = (EditText)dialogView.findViewById(R.id.edt_new_product_storage);

        Button btnAdd = (Button) dialogView.findViewById(R.id.btn_dialog_add_product_ok);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                final String productName = edtAddProduct.getText().toString().replace("."," ");
                final String productStorage = edtStorage.getText().toString();

                if(TextUtils.isEmpty(productName)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập tên Sản phẩm", Toast.LENGTH_LONG).show();

                }else if(TextUtils.isEmpty(productName)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập lượng tồn kho ban đầu", Toast.LENGTH_LONG).show();

                }else{
                    Product product = new Product(productName,productStorage);
                    Product newProduct = new Product(productName);
                    refDatabase.child(emailLogin+"/Product").child(productName).setValue(newProduct);
                    refDatabase.child(emailLogin).child("WarehouseMan/StorageMan").child(productName).setValue(product);
                    dialogProduct.dismiss();
                }


            }
        });

    }

    private void employeeListDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_employee_list,null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setMessage("Chọn nhân viên (trượt dọc để xem tiếp)");

        dialogEmployeeList = dialogBuilder.create();
        dialogEmployeeList.show();

        Button btnCreate = dialogView.findViewById(R.id.btn_employee_list_new);
        btnCreate.setVisibility(View.GONE);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                addEmployeeDialog();

            }
        });

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
                viewHolder.employeeName.setText(model.getEmployeeName());

            }
        };

        employeeList.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();

    }
    private void addEmployeeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_employee,null);
        builder.setView(dialogView);
        builder.setMessage("Thêm nhân viên mới");

        final Dialog dialog = builder.create();
        dialog.show();

        final EditText edtAddEmployee = (EditText)dialogView.findViewById(R.id.edt_dialog_add_employee_name);
        Button btnAdd = (Button) dialogView.findViewById(R.id.btn_dialog_add_employee_ok);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                final String employeeName = edtAddEmployee.getText().toString();

                if(TextUtils.isEmpty(employeeName)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập tên Nhân viên", Toast.LENGTH_LONG).show();

                }else{
                    Employee employee = new Employee(employeeName.replace("."," "));
                    refDatabase.child(emailLogin+"/Employee").push().setValue(employee);

                    dialog.dismiss();
                }


            }
        });

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

    private void sendForPreview() {
        showProgressDialog();

        //myMenu.findItem(R.id.action_preview).setVisible(false);

        final String productQuantity = edtproductQuantity.getText().toString();
        final String productPrice = edtproductPrice.getText().toString();
        final String deliveryDate = edtdeliveryDate.getText().toString();
        final String orderDiscount = edtOrderDiscount.getText().toString();
        final String employeeName = tvEmployeeName.getText().toString();
        //paymentType = b.getString("PaymentType");
        final String unitName = b.getString("UnitName");
        final String productName = tvProductName.getText().toString();;
        //final String choosenVAT = b.getString("ChoosenVAT");
        final String productStorage = b.getString("ProductStorage");
        String employeeCode = b.getString("EmployeeCode");
        final String orderNote = edtOrderNote.getText().toString();

        if(TextUtils.isEmpty(employeeName)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập tên nhân viên",Toast.LENGTH_LONG).show();

        } else if(TextUtils.isEmpty(productPrice)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập giá sản phẩm",Toast.LENGTH_LONG).show();

        }else if(TextUtils.isEmpty(productQuantity)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập số lượng sản phẩm",Toast.LENGTH_LONG).show();

        }else if(TextUtils.isEmpty(deliveryDate)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập ngày giao hàng",Toast.LENGTH_LONG).show();

        }else if(TextUtils.isEmpty(orderDiscount)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập chiết khấu cho đơn hàng",Toast.LENGTH_LONG).show();

        }else if(clientType == null || paymentType==null||productName==null || choosenVAT==null ){
            Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();

        }else if(productStorage == null){
            Toast.makeText(getApplicationContext(),"Chưa có dữ liệu tồn kho sản phẩm",Toast.LENGTH_LONG).show();

        }
        else if(employeeCode == null){
            Toast.makeText(getApplicationContext(),"Vui lòng chọn nhân viên",Toast.LENGTH_LONG).show();

        }
        else if(Float.parseFloat(productQuantity)>Float.parseFloat(productStorage)){
            Toast.makeText(getApplicationContext(),"Không đủ hàng tồn kho", Toast.LENGTH_LONG).show();
            edtproductQuantity.setText("");
            hideProgressDialog();
        }
        else {

            float notVAT = Float.parseFloat(productPrice)*Float.parseFloat(productQuantity);
            float choosenVATLong = Float.parseFloat(choosenVAT)/100;
            float discount = Float.parseFloat(orderDiscount)/100;

            float VAT =  notVAT*(1+choosenVATLong);
            float notVATDiscount = notVAT - notVAT*discount;
            float VATDiscount = VAT*(1-discount);

            OrderDetail orderDetail = new OrderDetail(clientCode,orderName,employeeName,clientType,paymentType,deliveryDate,orderNote,employeeCode);

            refDatabase.child(emailLogin+"/OrderList").child(orderPushKeyString).child("OtherInformation").setValue(orderDetail);

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

        }

    }

    public class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView employeeName;

        public EmployeeViewHolder(View itemView) {
            super(itemView);
            employeeName = (TextView) itemView.findViewById(R.id.tv_item_client_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    if(dialogEmployeeList!=null) dialogEmployeeList.dismiss();

                    int position = getLayoutPosition();
                    DatabaseReference keyRef = adapterFirebase.getRef(position);
                    Employee employee = adapterFirebase.getItem(position);
                    String keyString = keyRef.getKey();
                    b.putString("EmployeeCode",keyString);
                    tvEmployeeName.setText(employee.getEmployeeName());

                }
            });

        }
    }
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.tv_item_product_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    if(dialogProductList!=null) dialogProductList.dismiss();
                    int position = getLayoutPosition();
                    DatabaseReference keyRef = adapterFirebaseProduct.getRef(position);
                    String keyString = keyRef.getKey();
                    b.putString("ProductCode",keyString);

                    final Product p = adapterFirebaseProduct.getItem(position);

                    tvProductName.setText(p.getProductName());

                    refDatabase.child(emailLogin+"/WarehouseMan/StorageMan").child(p.getProductName()).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String storage = dataSnapshot.getValue().toString();
                            currentStorage.setText(storage);
                            b.putString("ProductName",p.getProductName());
                            b.putString("ProductStorage",storage);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
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


}

package vn.techlifegroup.wesell.order;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Client;
import vn.techlifegroup.wesell.model.Employee;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.model.Product;
import vn.techlifegroup.wesell.model.Promotion;
import vn.techlifegroup.wesell.model.VatModel;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;
import static vn.techlifegroup.wesell.utils.Constants.refEmployee;
import static vn.techlifegroup.wesell.utils.Constants.refOrder;
import static vn.techlifegroup.wesell.utils.Constants.refOrderList;
import static vn.techlifegroup.wesell.utils.Constants.refProductListByGroup;
import static vn.techlifegroup.wesell.utils.Constants.refUsers;

public class UpdateOrderActivity extends AppCompatActivity {

    private Spinner spinSales, spinPayment,spinUnitName,spinGroup, spinProduct,spinVAT;
    private ImageButton addPromotion;
    private Bundle b = new Bundle();
    private EditText edtproductQuantity, edtdeliveryDate,edtSpecialDiscount, edtDialogProductQuantity, edtOrderDiscount, edtOrderNote;
    private TextView currentStorage,tvClientName, tvClientSale, clientDebt,tvEmployeeName,tvProductName,tvEmployeeMonthSale,tvPromotionName;
    private String orderDiscount, orderPushKeyString, clientCode,paymentType,promotionName,productStock,
            clientType, employeeName,productName,choosenVAT,clientName,saleManEmail,productCode;
    private TextView edtproductPrice;
    private Switch switchPayment;
    private Button btnChooseEmployee,btnChooseProduct,btnChoosePromotion,btnPreview;
    private DatabaseReference orderPushKey;
    private FirebaseRecyclerAdapter<Employee,EmployeeViewHolder> adapterFirebase;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct;
    private FirebaseRecyclerAdapter<Promotion,PromotionViewHolder> adapterFirebasePromotion;
    private FirebaseRecyclerAdapter<Product,ProductOrderViewHolder> adapterProductOrder;

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView employeeList,programList,programListOrder;
    private AlertDialog.Builder dialogBuilder;
    private View dialogView;
    private LayoutInflater inflater;
    private ProgressDialog mProgressDialog;
    private Menu myMenu;
    private boolean dialogPromotion=false,isDebt,saleMan,outRoute;
    private Dialog dialogProductList,dialogProgramList,dialogEmployeeList;
    private String year,month,day;
    private RecyclerView rvProductList;

    private ArrayAdapter<String> adpGroup;
    private ArrayAdapter<String> adpProduct;

    private String groupChoose, productChoose,chosenProductPrice,finalPayment,discount;

    private String managedBy;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_order);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


       // b.putString("OrderPushKey",orderPushKeyString);
        saleManEmail =FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

        Intent intent = this.getIntent();
        clientCode = intent.getStringExtra("ClientCode");
        //emailLogin = intent.getStringExtra("EmailLogin");
        saleMan = intent.getBooleanExtra("SaleMan",false);
        outRoute = intent.getBooleanExtra("OutRoute",false);
       // employeeName = intent.getStringExtra("EmployeeName");

        refEmployee.child(saleManEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Employee employee = dataSnapshot.getValue(Employee.class);
                managedBy = employee.getManagedBy();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //keyOrderTemp = refUsers.child(clientCode).child("OrderTemp").push().getKey();

        orderPushKeyString = refOrderList.push().getKey();

        DateTime dt = new DateTime();
        month = dt.getMonthOfYear()+"";
        year = dt.getYear()+"";
        day = dt.getDayOfMonth()+"";

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
                //productListDialog();
                addProductToList();

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

    private void addProductToList() {
        final String productQuantity = edtproductQuantity.getText().toString();
        //final String productPrice = edtproductPrice.getText().toString().replace(",","");


        if(TextUtils.isEmpty(productQuantity)){
            Toast.makeText(getApplicationContext(),"Vui lòng nhập số lượng sản phẩm",Toast.LENGTH_LONG).show();

        }
        else if(productChoose == null){
            Toast.makeText(getApplicationContext(),"Vui lòng chọn sản phẩm",Toast.LENGTH_LONG).show();

        }
        else if(productStock == null || Float.parseFloat(productQuantity)>Float.parseFloat(productStock)){
            Toast.makeText(getApplicationContext(),"Không đủ hàng tồn kho", Toast.LENGTH_LONG).show();
            edtproductQuantity.setText("");

        }
        else {

            //rvProductList.setVisibility(View.VISIBLE);

            spinProduct.setSelection(0);
            edtproductPrice.setText(null);
            edtproductQuantity.setText(null);
            currentStorage.setText(null);

            float finalProductPayment = Float.parseFloat(chosenProductPrice)*Float.parseFloat(productQuantity);

            final Product currentProduct = new Product(productChoose,chosenProductPrice,productQuantity,productCode,finalProductPayment+"");

            refDatabase.child("OrderList").child(orderPushKeyString).child("ProductList").push().setValue(currentProduct);

        }
    }

    private void getClientDebt() {
        //Get Client debt
        refDatabase.child("Client").child(clientCode).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Client client = dataSnapshot.getValue(Client.class);
                String clientDebtData = client.getClientDebt();
                String clientSale = client.getClientSale();
                clientName = client.getClientName();
                tvClientName.setText(clientName);
                //tvClientSale.setText(Utils.convertNumber(clientSale));
                clientDebt.setText(Utils.convertNumber(clientDebtData));

                refDatabase.child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(year+"-"+month)){
                            refDatabase.child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(year)){
                                        refDatabase.child("TotalByClient").child(clientCode).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                tvClientSale.setText(Utils.convertNumber(dataSnapshot.getValue().toString()));

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @SuppressLint("CutPasteId")
    private void initilizeScreen() {

        DateTime dt = new DateTime();
        final String month = dt.getMonthOfYear()+"";
        final String year = dt.getYear()+"";

        //Toast.makeText(getApplicationContext(), year+"-"+month, Toast.LENGTH_LONG).show();

        spinVAT = (Spinner)findViewById(R.id.spin_order_vat);
        edtproductPrice = (TextView) findViewById(R.id.tv_order_product_price);
        edtproductQuantity = (EditText) findViewById(R.id.edt_order_product_quantity);
        edtdeliveryDate = (EditText) findViewById(R.id.edt_order_date_delivery);
        //edtSpecialDiscount = (EditText) findViewById(R.id.edt_order_special_discount);
        edtOrderNote = findViewById(R.id.edt_order_note);

        tvEmployeeName = (TextView)findViewById(R.id.tv_order_employee_name);
        tvEmployeeMonthSale = findViewById(R.id.tv_order_employee_month_sale);
        //tvPromotionName = findViewById(R.id.tv_order_promotion_name);
        tvClientName = findViewById(R.id.tv_order_client_name);
        tvClientSale = findViewById(R.id.tv_order_client_sale);
        currentStorage = (TextView) findViewById(R.id.tv_order_product_stock);
        clientDebt = (TextView)findViewById(R.id.tv_order_client_debt);
        //switchPromotion = (Switch) findViewById(R.id.switch_update_order);
        switchPayment = (Switch) findViewById(R.id.sw_order_payment);

        btnChooseEmployee = findViewById(R.id.btn_order_choose_employee);
        btnChooseProduct = findViewById(R.id.btn_order_choose_product);
        btnChoosePromotion = findViewById(R.id.btn_order_choose_promotion);

        spinGroup   = findViewById(R.id.spin_order_group);
        spinProduct = findViewById(R.id.spin_order_product);

        final List<String> listGroup = new ArrayList<String>();
        listGroup.add("Chọn nhóm");

        refProductListByGroup.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){
                    Iterable<DataSnapshot> catSnap = dataSnapshot.getChildren();
                    for(DataSnapshot itemCat:catSnap){
                        String storeGroupName = itemCat.getKey();
                        listGroup.add(storeGroupName);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //add listGroup

        adpGroup = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, listGroup);
        adpGroup.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinGroup.setAdapter(adpGroup);

        spinGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                groupChoose = (String) parent.getItemAtPosition(position);

                final List<String> listProduct = new ArrayList<String>();

                final List<String> listProductContains = new ArrayList<String>();

                listProduct.clear();
                listProduct.add("Chọn sản phẩm");

                refProductListByGroup.child(groupChoose).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChildren()){
                            Iterable<DataSnapshot> catSnap = dataSnapshot.getChildren();
                            for(DataSnapshot itemCat:catSnap){
                                String storeName = itemCat.getKey();
                                listProduct.add(storeName);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                //add listProduct

                adpProduct = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_list_item_1, listProduct);
                adpProduct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinProduct.setAdapter(adpProduct);

                spinProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        ((TextView) spinProduct.getSelectedView()).setTextColor(getResources().getColor(android.R.color.black));

                        edtproductQuantity.setText(null);
                        currentStorage.setText(null);

                        productChoose = (String) parent.getItemAtPosition(position);

                        refDatabase.child("OrderList").child(orderPushKeyString).child("ProductList")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChildren()) {
                                            Iterable<DataSnapshot> orderSnap = dataSnapshot.getChildren();
                                            for (DataSnapshot itemOrder : orderSnap) {

                                                Product productContains = itemOrder.getValue(Product.class);

                                                final String productName = productContains.getProductName();
                                                listProductContains.add(productName);

                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                        //add listProductContains

                        if(position != 0){

                            if (listProductContains.contains(productChoose)) {

                                Toast.makeText(getApplicationContext(), "Sản phẩm đã có", Toast.LENGTH_SHORT).show();

                                spinProduct.setSelection(0);
                                edtproductPrice.setText(null);
                                edtproductQuantity.setText(null);
                                currentStorage.setText(null);


                            }
                            else{

                                refDatabase.child("ProductList").child(productChoose).child("productPrice").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        chosenProductPrice = dataSnapshot.getValue().toString();
                                        edtproductPrice.setText(Utils.convertNumber(chosenProductPrice));

                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    }
                                });

                                refDatabase.child("WarehouseMan/StorageMan").child(productChoose).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
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
                        }else{
                            edtproductPrice.setText(null);
                            edtproductQuantity.setText(null);
                            currentStorage.setText(null);
                        }
/*
                        if (position != 0){
                            productName = (String) parent.getItemAtPosition(position);
                            refDatabase.child("Product").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                                    for(DataSnapshot itemProduct:snapProduct){
                                        Product p = itemProduct.getValue(Product.class);
                                        final String itemCode = p.getProductCode();
                                        String itemName = p.getProductName();
                                        final String itemPrice = p.getUnitPrice();
                                        if(itemName.equals(productName)){
                                            refDatabase.child("WarehouseMan/StorageMan").child(itemCode).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    productStock = dataSnapshot.getValue().toString();
                                                    currentStorage.setText(productStock);
                                                    edtproductPrice.setText(Utils.convertNumber(itemPrice));
                                                    edtproductPrice.setEnabled(false);
                                                    productCode = itemCode;
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

 */
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

/*
        final List<String> products = new ArrayList<>();

        products.add("Chọn sản phẩm");

        refDatabase.child("Product").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                for(DataSnapshot itemProduct:snapProduct){
                    Product p = itemProduct.getValue(Product.class);
                    String productName = p.getProductName();
                    products.add(productName);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

 */
/*
        adpProduct = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, products);
        adpProduct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinProduct.setAdapter(adpProduct);

        spinProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) spinProduct.getSelectedView()).setTextColor(getResources().getColor(android.R.color.black));

                if (position != 0){
                    productName = (String) parent.getItemAtPosition(position);
                    refDatabase.child("Product").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                            for(DataSnapshot itemProduct:snapProduct){
                                Product p = itemProduct.getValue(Product.class);
                                final String itemCode = p.getProductCode();
                                String itemName = p.getProductName();
                                final String itemPrice = p.getUnitPrice();
                                if(itemName.equals(productName)){
                                    refDatabase.child("WarehouseMan/StorageMan").child(itemCode).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            productStock = dataSnapshot.getValue().toString();
                                            currentStorage.setText(productStock);
                                            edtproductPrice.setText(Utils.convertNumber(itemPrice));
                                            edtproductPrice.setEnabled(false);
                                            productCode = itemCode;
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

 */

        rvProductList = findViewById(R.id.rv_order_list_product);

        rvProductList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvProductList.setLayoutManager(linearLayoutManager);

        DatabaseReference refProduct = refDatabase.child("OrderList").child(orderPushKeyString).child("ProductList");

        adapterProductOrder = new FirebaseRecyclerAdapter<Product, ProductOrderViewHolder>(
                Product.class,
                R.id.item_product,
                ProductOrderViewHolder.class,
                refProduct
        ) {
            @Override
            public ProductOrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product,parent,false);
                return new ProductOrderViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductOrderViewHolder viewHolder, Product model, int position) {
                viewHolder.name.setText(model.getProductName());
                viewHolder.price.setText(model.getUnitPrice());
                viewHolder.quantity.setText(model.getUnitQuantity());
            }
        };

        rvProductList.setAdapter(adapterProductOrder);
        adapterProductOrder.notifyDataSetChanged();

        programListOrder = findViewById(R.id.rv_program_list);
        // listProgram = (RecyclerView)dialogView.findViewById(R.id.rv_program_list);
        programListOrder.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        programListOrder.setLayoutManager(linearLayoutManager);

        adapterFirebasePromotion = new FirebaseRecyclerAdapter<Promotion, PromotionViewHolder>(
                Promotion.class,
                R.layout.item_promotion,
                PromotionViewHolder.class,
                refDatabase.child("OrderList").child(orderPushKeyString).child("Promotion")
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

        programListOrder.setAdapter(adapterFirebasePromotion);
        adapterFirebasePromotion.notifyDataSetChanged();

        edtproductPrice.setEnabled(false);

    if(saleMan){
            btnChooseEmployee.setVisibility(View.GONE);
            refDatabase.child("Employee").child(saleManEmail).child("employeeName").addListenerForSingleValueEvent(new ValueEventListener() {
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

            refDatabase.child("TotalBySale").child(saleManEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(year+"-"+month)){
                        refDatabase.child("TotalBySale").child(saleManEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                tvEmployeeMonthSale.setText(Utils.convertNumber(Math.round(Float.parseFloat(dataSnapshot.getValue().toString())*10d)/10d+""));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }else{
                        tvEmployeeMonthSale.setText("0");
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            refDatabase.child("TotalBySale").child(saleManEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild(year+"-"+month)){
                        refDatabase.child("TotalBySale").child(saleManEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                tvEmployeeMonthSale.setText(Utils.convertNumber(dataSnapshot.getValue().toString()));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }else{
                        tvEmployeeMonthSale.setText("0");
                    }
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
                refDatabase.child("Product")
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
                refDatabase.child("Employee")
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
                refDatabase.child("PromotionMan")
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

    private void sendForPreview() {

        //myMenu.findItem(R.id.action_preview).setVisible(false);


        final String deliveryDate = edtdeliveryDate.getText().toString();
        final String orderNote    = edtOrderNote.getText().toString();

       if(employeeName == null){
            Toast.makeText(getApplicationContext(),"Vui lòng chọn nhân viên",Toast.LENGTH_LONG).show();

        }else if(TextUtils.isEmpty(deliveryDate)){
           Toast.makeText(getApplicationContext(), "Vui lòng nhập ngày giao hàng", Toast.LENGTH_LONG).show();
       }else if(productChoose== null){
           Toast.makeText(getApplicationContext(), "Vui lòng chọn sản phẩm!", Toast.LENGTH_LONG).show();
       }
       else {

           // showProgressDialog();

           OrderDetail orderDetail = new OrderDetail(clientCode,clientName,employeeName,switchPayment.getText().toString(),deliveryDate,saleManEmail,orderNote);
           refDatabase.child("OrderList").child(orderPushKeyString).child("OtherInformation").setValue(orderDetail);

           refDatabase.child("OrderList").child(orderPushKeyString).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
               @Override
               public void onDataChange(DataSnapshot dataSnapshot) {
                   final Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                   final long productCount = dataSnapshot.getChildrenCount();

                   int i = 0;
                   final float[] VAT = {0};
                   final float[] notVAT = {0};
                   final float[] finalPayment = {0};
                   for (DataSnapshot itemProduct:snapProduct){
                       i++;
                       Product p = itemProduct.getValue(Product.class);
                       final String productPrice = p.getUnitPrice();
                       final String productQuantity = p.getUnitQuantity();
                       final String productName = p.getProductName();

                       final float choosenVATLong = Float.parseFloat(choosenVAT)/100;
                       notVAT[0] += Float.parseFloat(productPrice)*Float.parseFloat(productQuantity);
                       VAT[0] += (float) (Math.round((Float.parseFloat(productPrice)*Float.parseFloat(productQuantity)*(1+choosenVATLong)*10d)/10d));

                       if(i == productCount){

                           finalPayment[0] += (float) (Math.round((Float.parseFloat(productPrice)*Float.parseFloat(productQuantity)*(1+choosenVATLong)*10d)/10d));

                           refDatabase.child("OrderList").child(orderPushKeyString).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(DataSnapshot dataSnapshot) {
                                   if(dataSnapshot.hasChild("Promotion")){
                                       refDatabase.child("OrderList").child(orderPushKeyString).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(DataSnapshot dataSnapshot) {
                                               Iterable<DataSnapshot> snapPromotion = dataSnapshot.getChildren();
                                               final float choosenVATLong = Float.parseFloat(choosenVAT)/100;

                                               for(DataSnapshot itemPromotion:snapPromotion){

                                                   Promotion promotion = itemPromotion.getValue(Promotion.class);
                                                   final String promotionKey = promotion.getPromotionCode();
                                                   refDatabase.child("PromotionMan").child(promotionKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                                       @Override
                                                       public void onDataChange(final DataSnapshot dataSnapshotProman) {

                                                           if(dataSnapshotProman.hasChild("ProductDiscount")){

                                                               refDatabase.child("PromotionMan").child(promotionKey).child("ProductDiscount").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                   @Override
                                                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                                                       Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();

                                                                       for(DataSnapshot itemProduct:productSnap){

                                                                           Product p = itemProduct.getValue(Product.class);
                                                                           final String productDiscount = p.getProductDiscount();
                                                                           final String promotionName = p.getProductName();



                                                                           if(promotionName.equals(productName)){

                                                                               finalPayment[0] = (float)Math.round(Math.round((finalPayment[0]
                                                                                       - Float.parseFloat(productPrice)*Float.parseFloat(productQuantity) * Float.parseFloat(productDiscount)/100)));
                                                                               //Toast.makeText(getApplicationContext(), finalPayment[0]+"",Toast.LENGTH_LONG).show();

                                                                               if(dataSnapshotProman.hasChild("orderDiscount")){
                                                                                   refDatabase.child("PromotionMan").child(promotionKey).child("orderDiscount").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                       @Override
                                                                                       public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                           finalPayment[0] = Math.round(finalPayment[0]*(1-Float.parseFloat(dataSnapshot.getValue().toString())/100));
                                                                                           VatModel vatModel = new VatModel(notVAT[0],VAT[0],finalPayment[0]);

                                                                                           refDatabase.child("OrderList").child(orderPushKeyString).child("VAT").setValue(vatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                               @Override
                                                                                               public void onSuccess(Void aVoid) {
                                                                                                   hideProgressDialog();
                                                                                                   edtproductQuantity.setText("");
                                                                                                   edtproductPrice.setText("");
                                                                                                   edtdeliveryDate.setText("");

                                                                                                   Intent intent = new Intent(UpdateOrderActivity.this, PreviewOrderActivivity.class);

                                                                                                   //intent.putExtra("EmailLogin", emailLogin);
                                                                                                   intent.putExtra("OrderPushKey", orderPushKeyString);
                                                                                                   intent.putExtra("OrderName", clientName);
                                                                                                   //intent.putExtra("OrderDiscount",orderDiscount);
                                                                                                   //intent.putExtra("DiscountTax",discountVAT);
                                                                                                   intent.putExtra("ClientCode",clientCode);
                                                                                                   intent.putExtra("OutRoute",outRoute);

                                                                                                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                   startActivity(intent);
                                                                                               }
                                                                                           });
                                                                                       }

                                                                                       @Override
                                                                                       public void onCancelled(DatabaseError databaseError) {

                                                                                       }
                                                                                   });

                                                                               }else{
                                                                                   VatModel vatModel = new VatModel(notVAT[0],VAT[0],finalPayment[0]);

                                                                                   refDatabase.child("OrderList").child(orderPushKeyString).child("VAT").setValue(vatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                       @Override
                                                                                       public void onSuccess(Void aVoid) {
                                                                                           hideProgressDialog();
                                                                                           edtproductQuantity.setText("");
                                                                                           edtproductPrice.setText("");
                                                                                           edtdeliveryDate.setText("");

                                                                                           Intent intent = new Intent(UpdateOrderActivity.this, PreviewOrderActivivity.class);

                                                                                           //intent.putExtra("EmailLogin", emailLogin);
                                                                                           intent.putExtra("OrderPushKey", orderPushKeyString);
                                                                                           intent.putExtra("OrderName", clientName);
                                                                                           //intent.putExtra("OrderDiscount",orderDiscount);
                                                                                           //intent.putExtra("DiscountTax",discountVAT);
                                                                                           intent.putExtra("ClientCode",clientCode);
                                                                                           intent.putExtra("OutRoute",outRoute);

                                                                                           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                           startActivity(intent);
                                                                                       }
                                                                                   });
                                                                               }


                                                                           }else{
                                                                               if(dataSnapshotProman.hasChild("orderDiscount")){

                                                                                   refDatabase.child("PromotionMan").child(promotionKey).child("orderDiscount").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                       @Override
                                                                                       public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                           finalPayment[0] = Math.round(finalPayment[0]*(1-Float.parseFloat(dataSnapshot.getValue().toString())/100));
                                                                                           VatModel vatModel = new VatModel(notVAT[0],VAT[0],finalPayment[0]);

                                                                                           refDatabase.child("OrderList").child(orderPushKeyString).child("VAT").setValue(vatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                               @Override
                                                                                               public void onSuccess(Void aVoid) {
                                                                                                   hideProgressDialog();
                                                                                                   edtproductQuantity.setText("");
                                                                                                   edtproductPrice.setText("");
                                                                                                   edtdeliveryDate.setText("");

                                                                                                   Intent intent = new Intent(UpdateOrderActivity.this, PreviewOrderActivivity.class);

                                                                                                   //intent.putExtra("EmailLogin", emailLogin);
                                                                                                   intent.putExtra("OrderPushKey", orderPushKeyString);
                                                                                                   intent.putExtra("OrderName", clientName);
                                                                                                   //intent.putExtra("OrderDiscount",orderDiscount);
                                                                                                   //intent.putExtra("DiscountTax",discountVAT);
                                                                                                   intent.putExtra("ClientCode",clientCode);
                                                                                                   intent.putExtra("OutRoute",outRoute);

                                                                                                   intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                                   startActivity(intent);
                                                                                               }
                                                                                           });
                                                                                       }

                                                                                       @Override
                                                                                       public void onCancelled(DatabaseError databaseError) {

                                                                                       }
                                                                                   });

                                                                               }else{
                                                                                   VatModel vatModel = new VatModel(notVAT[0],VAT[0],finalPayment[0]);

                                                                                   refDatabase.child("OrderList").child(orderPushKeyString).child("VAT").setValue(vatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                       @Override
                                                                                       public void onSuccess(Void aVoid) {
                                                                                           hideProgressDialog();
                                                                                           edtproductQuantity.setText("");
                                                                                           edtproductPrice.setText("");
                                                                                           edtdeliveryDate.setText("");

                                                                                           Intent intent = new Intent(UpdateOrderActivity.this, PreviewOrderActivivity.class);

                                                                                           //intent.putExtra("EmailLogin", emailLogin);
                                                                                           intent.putExtra("OrderPushKey", orderPushKeyString);
                                                                                           intent.putExtra("OrderName", clientName);
                                                                                           //intent.putExtra("OrderDiscount",orderDiscount);
                                                                                           //intent.putExtra("DiscountTax",discountVAT);
                                                                                           intent.putExtra("ClientCode",clientCode);
                                                                                           intent.putExtra("OutRoute",outRoute);

                                                                                           intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                                           startActivity(intent);
                                                                                       }
                                                                                   });
                                                                               }
                                                                           }



                                                                          // Toast.makeText(getApplicationContext(), finalPayment[0]+"",Toast.LENGTH_LONG).show();

                                                                       }
                                                                   }

                                                                   @Override
                                                                   public void onCancelled(DatabaseError databaseError) {

                                                                   }
                                                               });
                                                           }
                                                            else if(dataSnapshotProman.hasChild("orderDiscount")){
                                                               refDatabase.child("PromotionMan").child(promotionKey).child("orderDiscount").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                   @Override
                                                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                                                       finalPayment[0] = Math.round(finalPayment[0]*(1-Float.parseFloat(dataSnapshot.getValue().toString())/100));
                                                                       VatModel vatModel = new VatModel(notVAT[0],VAT[0],finalPayment[0]);

                                                                       refDatabase.child("OrderList").child(orderPushKeyString).child("VAT").setValue(vatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                           @Override
                                                                           public void onSuccess(Void aVoid) {
                                                                               hideProgressDialog();
                                                                               edtproductQuantity.setText("");
                                                                               edtproductPrice.setText("");
                                                                               edtdeliveryDate.setText("");

                                                                               Intent intent = new Intent(UpdateOrderActivity.this, PreviewOrderActivivity.class);

                                                                               //intent.putExtra("EmailLogin", emailLogin);
                                                                               intent.putExtra("OrderPushKey", orderPushKeyString);
                                                                               intent.putExtra("OrderName", clientName);
                                                                               //intent.putExtra("OrderDiscount",orderDiscount);
                                                                               //intent.putExtra("DiscountTax",discountVAT);
                                                                               intent.putExtra("ClientCode",clientCode);
                                                                               intent.putExtra("OutRoute",outRoute);

                                                                               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                               startActivity(intent);
                                                                           }
                                                                       });
                                                                   }

                                                                   @Override
                                                                   public void onCancelled(DatabaseError databaseError) {

                                                                   }
                                                               });

                                                           }else{
                                                               VatModel vatModel = new VatModel(notVAT[0],VAT[0],finalPayment[0]);

                                                               refDatabase.child("OrderList").child(orderPushKeyString).child("VAT").setValue(vatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                   @Override
                                                                   public void onSuccess(Void aVoid) {
                                                                       hideProgressDialog();
                                                                       edtproductQuantity.setText("");
                                                                       edtproductPrice.setText("");
                                                                       edtdeliveryDate.setText("");

                                                                       Intent intent = new Intent(UpdateOrderActivity.this, PreviewOrderActivivity.class);

                                                                       //intent.putExtra("EmailLogin", emailLogin);
                                                                       intent.putExtra("OrderPushKey", orderPushKeyString);
                                                                       intent.putExtra("OrderName", clientName);
                                                                       //intent.putExtra("OrderDiscount",orderDiscount);
                                                                       //intent.putExtra("DiscountTax",discountVAT);
                                                                       intent.putExtra("ClientCode",clientCode);
                                                                       intent.putExtra("OutRoute",outRoute);

                                                                       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                       startActivity(intent);
                                                                   }
                                                               });
                                                           }

                                                           //Toast.makeText(getApplicationContext(), finalPayment[0]+"",Toast.LENGTH_LONG).show();


                                                       }

                                                       @Override
                                                       public void onCancelled(DatabaseError databaseError) {

                                                       }
                                                   });

                                               }
                                           }

                                           @Override
                                           public void onCancelled(DatabaseError databaseError) {

                                           }
                                       });

                                   }else{
                                       VatModel vatModel = new VatModel(notVAT[0],VAT[0],finalPayment[0]);

                                       refDatabase.child("OrderList").child(orderPushKeyString).child("VAT").setValue(vatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void aVoid) {
                                               hideProgressDialog();
                                               edtproductQuantity.setText("");
                                               edtproductPrice.setText("");
                                               edtdeliveryDate.setText("");

                                               Intent intent = new Intent(UpdateOrderActivity.this, PreviewOrderActivivity.class);

                                               //intent.putExtra("EmailLogin", emailLogin);
                                               intent.putExtra("OrderPushKey", orderPushKeyString);
                                               intent.putExtra("OrderName", clientName);
                                               //intent.putExtra("OrderDiscount",orderDiscount);
                                               //intent.putExtra("DiscountTax",discountVAT);
                                               intent.putExtra("ClientCode",clientCode);
                                               intent.putExtra("OutRoute",outRoute);

                                               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                               startActivity(intent);
                                           }
                                       });
                                   }



                                   //Toast.makeText(getApplicationContext(), finalPayment[0]+"",Toast.LENGTH_LONG).show();


                               }

                               @Override
                               public void onCancelled(DatabaseError databaseError) {

                               }
                           });

                       }else{
                           finalPayment[0] += (float) (Math.round((Float.parseFloat(productPrice)*Float.parseFloat(productQuantity)*(1+choosenVATLong)*10d)/10d));

                           refDatabase.child("OrderList").child(orderPushKeyString).addListenerForSingleValueEvent(new ValueEventListener() {
                               @Override
                               public void onDataChange(DataSnapshot dataSnapshot) {
                                   if(dataSnapshot.hasChild("Promotion")){
                                       refDatabase.child("OrderList").child(orderPushKeyString).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                                           @Override
                                           public void onDataChange(DataSnapshot dataSnapshot) {
                                               Iterable<DataSnapshot> snapPromotion = dataSnapshot.getChildren();
                                               final float choosenVATLong = Float.parseFloat(choosenVAT)/100;

                                               for(DataSnapshot itemPromotion:snapPromotion){

                                                   Promotion promotion = itemPromotion.getValue(Promotion.class);
                                                   final String promotionKey = promotion.getPromotionCode();
                                                   refDatabase.child("PromotionMan").child(promotionKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                                       @Override
                                                       public void onDataChange(final DataSnapshot dataSnapshotProman) {

                                                           if(dataSnapshotProman.hasChild("ProductDiscount")){

                                                               refDatabase.child("PromotionMan").child(promotionKey).child("ProductDiscount").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                   @Override
                                                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                                                       Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();

                                                                       for(DataSnapshot itemProduct:productSnap){

                                                                           Product p = itemProduct.getValue(Product.class);
                                                                           final String productDiscount = p.getProductDiscount();
                                                                           final String promotionName = p.getProductName();

                                                                           if(promotionName.equals(productName)){

                                                                               finalPayment[0] = (float)Math.round((finalPayment[0]
                                                                                       - Float.parseFloat(productPrice)*Float.parseFloat(productQuantity) * Float.parseFloat(productDiscount)/100));

                                                                           }

                                                                       }
                                                                   }

                                                                   @Override
                                                                   public void onCancelled(DatabaseError databaseError) {

                                                                   }
                                                               });
                                                           }


                                                       }

                                                       @Override
                                                       public void onCancelled(DatabaseError databaseError) {

                                                       }
                                                   });

                                               }
                                           }

                                           @Override
                                           public void onCancelled(DatabaseError databaseError) {

                                           }
                                       });

                                   }else{

                                       VatModel vatModel = new VatModel(notVAT[0],VAT[0],finalPayment[0]);

                                       refDatabase.child("OrderList").child(orderPushKeyString).child("VAT").setValue(vatModel).addOnSuccessListener(new OnSuccessListener<Void>() {
                                           @Override
                                           public void onSuccess(Void aVoid) {
                                               hideProgressDialog();
                                               edtproductQuantity.setText("");
                                               edtproductPrice.setText("");
                                               edtdeliveryDate.setText("");

                                               Intent intent = new Intent(UpdateOrderActivity.this, PreviewOrderActivivity.class);

                                               //intent.putExtra("EmailLogin", emailLogin);
                                               intent.putExtra("OrderPushKey", orderPushKeyString);
                                               intent.putExtra("OrderName", clientName);
                                               //intent.putExtra("OrderDiscount",orderDiscount);
                                               //intent.putExtra("DiscountTax",discountVAT);
                                               intent.putExtra("ClientCode",clientCode);
                                               intent.putExtra("OutRoute",outRoute);

                                               intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                               startActivity(intent);
                                           }
                                       });
                                   }
                               }

                               @Override
                               public void onCancelled(DatabaseError databaseError) {

                               }
                           });

                       }
                   }
               }

               @Override
               public void onCancelled(DatabaseError databaseError) {

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
    public class ProductOrderViewHolder extends RecyclerView.ViewHolder {
        TextView name,price,quantity;

        public ProductOrderViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_item_product_name);
            price = itemView.findViewById(R.id.tv_item_product_price);
            quantity = itemView.findViewById(R.id.tv_item_product_quantity);


        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView name,price,quantity;

    public ProductViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.tv_item_product_name);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);

                    rvProductList.setVisibility(View.VISIBLE);

                    btnChooseProduct.setVisibility(View.GONE);
                    if(dialogProductList!=null) dialogProductList.dismiss();
                    int position = getLayoutPosition();
                    DatabaseReference keyRef = adapterFirebaseProduct.getRef(position);
                    String keyString = keyRef.getKey();
                    b.putString("ProductCode",keyString);

                    final Product p = adapterFirebaseProduct.getItem(position);

                    productName = p.getProductName();
                    productCode = p.getProductCode();
                    edtproductPrice.setText(p.getUnitPrice());



                    refDatabase.child("WarehouseMan/StorageMan").child(p.getProductCode()).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            productStock = dataSnapshot.getValue().toString();
                            currentStorage.setText(productStock);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    //addProd();
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

                    int position = getAdapterPosition();

                    Promotion p = adapterFirebasePromotion.getItem(position);
                    String promotionKey = adapterFirebasePromotion.getRef(position).getKey();

                    DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MM-yyyy");

                    DateTime promotionEnd = fmt.parseDateTime(p.getPromotionEndDate());
                    DateTime dt = new DateTime();


                    if(dt.toDate().before( promotionEnd.toDate())){
                        programListOrder.setVisibility(View.VISIBLE);

                        if(dialogProgramList!=null) dialogProgramList.dismiss();

                        Promotion addPromotion = new Promotion(p.getPromotionName(),promotionKey);

                        refDatabase.child("OrderList").child(orderPushKeyString).child("Promotion").child(promotionKey).setValue(addPromotion);
                    }else{
                        Toast.makeText(getApplicationContext(), "Rất tiếc, đã qua thời gian áp dụng chương trình!", Toast.LENGTH_LONG).show();
                    }



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
        refDatabase.child("OrderList").child(orderPushKeyString).setValue(null);
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

        if(id == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

}

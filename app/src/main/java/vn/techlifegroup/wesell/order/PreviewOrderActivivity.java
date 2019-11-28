package vn.techlifegroup.wesell.order;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import vn.techlifegroup.wesell.MainActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.bytask.OrderManActivity;
import vn.techlifegroup.wesell.model.Client;
import vn.techlifegroup.wesell.model.Employee;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.model.Product;
import vn.techlifegroup.wesell.model.Promotion;
import vn.techlifegroup.wesell.model.VatModel;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.MySpinerAdapter;
import vn.techlifegroup.wesell.utils.Utils;
import fr.ganfra.materialspinner.MaterialSpinner;

import static vn.techlifegroup.wesell.utils.Constants.buttonClick;
import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class PreviewOrderActivivity extends AppCompatActivity {

    private RecyclerView recyclerViewProduct, recyclerViewPromotion;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct,adapterFirebaseAddProduct;
    private FirebaseRecyclerAdapter<Product,EditProductViewHolder> adapterFirebaseEditProduct;
    private FirebaseRecyclerAdapter<Promotion,PromotionViewHolder> adapterFirebasePromotion;

    private DatabaseReference refProduct, refPromotion,refOrderList,refStorage,refCompany;
    private ImageButton ibInfo, ibProduct, ibPromotion;
    private boolean discountVAT,outRoute,viewOnly,saleMan;

    private TextView tvClientName, tvClientType, tvPayment, tvDelivery, tvNotVAT,tvPromotionStorage,tvProductStorage,
            tvVAT,tvNotVATDiscount, tvFinalPayment,tvClientAddress, tvDeliveryName,tvNote,tvPromotionChoosen,tvProductChoosen,tvSaleName;
    private String orderPushKey,orderName,employeeName,clientType,clientCode,paymentType,deliveryDate,choosenVAT,emailLogin,discount;

    private float VAT,notVAT;

    private Bundle b = new Bundle();

    private ProgressDialog mProgressDialog;
    private boolean dialogPromotion = false;
    private Dialog dialogProductList;
    private String choosenVATDialog;
    private String productStorageDialog;
    private String productNameDialog;
    private String promotionStorageDialog;
    private Dialog dialogAddPromotion;
    private String promotionNameDialog;
    private Button btnSendApproved,btnCancel;
    private EditText edtDialogProductPrice;
    private String productCode;
    private String managerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Intent intent = this.getIntent();
        //discountVAT = intent.getBooleanExtra("DiscountTax",false);
        orderPushKey = intent.getStringExtra("OrderPushKey");
        orderName = intent.getStringExtra("OrderName");
        outRoute = intent.getBooleanExtra("OutRoute",false);
        saleMan = intent.getBooleanExtra("SaleMan",false);

        discount = intent.getStringExtra("OrderDiscount");
        choosenVAT = intent.getStringExtra("VAT");
        //clientCode = intent.getStringExtra("ClientCode");
        emailLogin = intent.getStringExtra("EmailLogin");
        viewOnly = intent.getBooleanExtra("ViewOnly",false);

        refOrderList = refDatabase.child(emailLogin).child("OrderList");
        refStorage = refDatabase.child(emailLogin).child("Storage");
        refCompany = refDatabase.child(emailLogin);

        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

        refCompany.child("Employee").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Employee employee = dataSnapshot.getValue(Employee.class);
                managerEmail = employee.getManagedBy();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        initializeScreen();

        //Toast.makeText(getApplicationContext(), discount, Toast.LENGTH_LONG).show();

    }

    private void initializeScreen() {

        recyclerViewProduct = (RecyclerView)findViewById(R.id.recycler_order_detail_product);
        recyclerViewPromotion = (RecyclerView)findViewById(R.id.order_recycler_promotion_preview);
        tvClientName = (TextView)findViewById(R.id.tv_approve_sale_client_name);
        tvClientType = (TextView)findViewById(R.id.tv_approve_sale_client_type);
        tvPayment = (TextView)findViewById(R.id.tv_approve_sale_payment_type);
        tvNotVAT = (TextView)findViewById(R.id.tv_detail_notVAT_preview);
        tvVAT = (TextView)findViewById(R.id.tv_detail_VAT_preview);
        tvFinalPayment = (TextView)findViewById(R.id.tv_preview_final_payment);
        tvClientAddress = (TextView)findViewById(R.id.tv_preview_order_address);
        tvDelivery = findViewById(R.id.tv_preview_delivery_date);
        tvNote = findViewById(R.id.tv_preview_order_note);
        tvSaleName = findViewById(R.id.tv_preview_sale_name);

        btnSendApproved = findViewById(R.id.btn_preview_send_approved);
        btnCancel = findViewById(R.id.btn_preview_cancel);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).setValue(null);
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
            }
        });

        btnSendApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                showProgressDialog();

                String thisYear = Calendar.getInstance().get(Calendar.YEAR)+"";
                String thisMonth = (Calendar.getInstance().get(Calendar.MONTH)+1)+"";
                String thisDate = Calendar.getInstance().get(Calendar.DATE)+"";

                if(outRoute){
                    if(managerEmail != null)
                        refDatabase.child(emailLogin+"/Order/OrderBySale").child(managerEmail).child("OutRoute").child(orderPushKey).child("orderName").setValue(orderName);
                    refDatabase.child(emailLogin+"/Order").child("OutRoute").child(orderPushKey).child("orderName").setValue(orderName).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            hideProgressDialog();

                        }
                    });
                }else{
                    if(managerEmail != null)
                        refDatabase.child(emailLogin+"/Order/OrderBySale").child(managerEmail).child("UnApproved").child(orderPushKey).child("orderName").setValue(orderName);

                    refDatabase.child(emailLogin).child("OrderListByTime").child(thisYear+"-"+thisMonth+"-"+thisDate).child(orderPushKey).setValue(orderName);

                    refDatabase.child(emailLogin+"/Order").child("UnApproved").child(orderPushKey).child("orderName").setValue(orderName).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(intent);
                            hideProgressDialog();

                        }
                    });

                }


            }
        });

        ibProduct = (ImageButton)findViewById(R.id.ib_preview_edit_product);


        ibProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                editProductDialog();

            }
        });

        if(viewOnly){
            btnSendApproved.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            ibProduct.setVisibility(View.GONE);
        }




        initializeRecyclerViewProduct();

        initializeRecyclerViewPromotion();

        viewVAT();
    }

    @Override
    protected void onResume() {
        viewOtherInformation();

        super.onResume();
    }

    private void viewVAT() {
        refOrderList.child(orderPushKey).child("VAT").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                VatModel currentVat = dataSnapshot.getValue(VatModel.class);
                if(currentVat!=null){
                    notVAT = currentVat.getNotVat();
                    VAT = currentVat.getIncludedVat();
                    //b.putString("NotVAT",currentVat.getNotVat());
                    //b.putString("IncludedVAT",currentVat.getIncludedVat());

                    //String notVATValue = currentVat.getNotVat();
                    tvNotVAT.setText(Utils.convertNumber(notVAT+""));

                    //String vatValue = currentVat.getIncludedVat();
                    tvVAT.setText(Utils.convertNumber(VAT+""));

                    float finalPayment = currentVat.getFinalPayment();
                    tvFinalPayment.setText(Utils.convertNumber(finalPayment+""));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void viewOtherInformation() {

        refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                if(orderDetail != null){
                    String clientName = orderDetail.getOrderName();
                    String payment = orderDetail.getPaymentType();
                    String deliveryDate = orderDetail.getDateDelivery();
                    String orderNote = orderDetail.getOrderNote();
                    String saleName = orderDetail.getSaleName();
                    clientCode = orderDetail.getClientCode();

                    tvClientName.setText(clientName);
                    //tvClientType.setText(clientType);
                    tvPayment.setText(payment);
                    tvDelivery.setText(deliveryDate);
                    tvNote.setText(orderNote);
                    tvSaleName.setText(saleName);

                    refDatabase.child(emailLogin+"/Client").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Client client = dataSnapshot.getValue(Client.class);
                            String clientAddress = client.getClientStreet();
                            String clientDistrict = client.getClientDistrict();
                            String clientCity = client.getClientCity();
                            //String clientDeliveryName = client.getClientDeliveryName();
                            clientType = client.getClientType();
                            tvClientAddress.setText(clientAddress+", "+clientDistrict+", "+clientCity);
                            tvClientType.setText(clientType);
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

    private void initializeRecyclerViewPromotion() {
        recyclerViewPromotion.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewPromotion.setLayoutManager(linearLayoutManager);

        refPromotion = refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion");

        adapterFirebasePromotion = new FirebaseRecyclerAdapter<Promotion, PromotionViewHolder>(
                Promotion.class,
                R.layout.item_promotion,
                PromotionViewHolder.class,
                refPromotion
        ) {
            @Override
            public PromotionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion,parent,false);
                return new PromotionViewHolder(v);
            }


            @Override
            protected void populateViewHolder(PromotionViewHolder viewHolder, Promotion model, int position) {
                viewHolder.mPromotionName.setText(model.getPromotionName());
            }
        };

        recyclerViewPromotion.setAdapter(adapterFirebasePromotion);
        adapterFirebasePromotion.notifyDataSetChanged();
    }
    private void initializeRecyclerViewProduct() {
        recyclerViewProduct.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewProduct.setLayoutManager(linearLayoutManager);


        refProduct = refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList");

        adapterFirebaseProduct = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product,
                ProductViewHolder.class,
                refProduct
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product,parent,false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productPrice.setText(Utils.convertNumber(model.getUnitPrice()));
                viewHolder.productQuantity.setText(model.getUnitQuantity());
            }
        };

        recyclerViewProduct.setAdapter(adapterFirebaseProduct);
        adapterFirebaseProduct.notifyDataSetChanged();
    }

    public class EditProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;
        ImageButton deleteProduct;


        public EditProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView)itemView.findViewById(R.id.tv_item_product_name);
            productPrice = (TextView)itemView.findViewById(R.id.tv_item_product_price);
            productQuantity = (TextView) itemView.findViewById(R.id.tv_item_product_quantity);

            productQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);

                    int pos = getAdapterPosition();
                    final Product choosenP = adapterFirebaseEditProduct.getItem(pos);
                    final DatabaseReference refProduct = adapterFirebaseEditProduct.getRef(pos);
                    final String productKey = adapterFirebaseEditProduct.getRef(pos).getKey();

                    AlertDialog.Builder builder = new AlertDialog.Builder(PreviewOrderActivivity.this);
                    builder.setMessage("Thay đổi số lượng sản phẩm?");

                    final EditText input = new EditText(PreviewOrderActivivity.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    input.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
                    input.setHint("Nhập số lượng");
                    builder.setView(input);

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String number = input.getText().toString();
                            if(TextUtils.isEmpty(number)){
                                Toast.makeText(getApplicationContext(), "Vui lòng nhập số lượng!", Toast.LENGTH_LONG).show();
                            }else{
                                Product updateP = new Product(choosenP.getProductName(),choosenP.getUnitPrice(),number, choosenP.getProductCode(), choosenP.getFinalPayment());
                                refProduct.setValue(updateP);
                                adapterFirebaseEditProduct.notifyDataSetChanged();

                                
                            }
                        }
                    }).setNegativeButton("Không", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                }
            });

        }
    }
    public class EditPromotionViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;
        ImageButton deletePromotion;


        public EditPromotionViewHolder(View itemView) {
            super(itemView);
            productName = (TextView)itemView.findViewById(R.id.tv_item_promotion_name);
            productPrice = (TextView)itemView.findViewById(R.id.tv_item_promotion_price);
            productQuantity = (TextView) itemView.findViewById(R.id.tv_item_promotion_quantity);


        }
    }
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;


        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView)itemView.findViewById(R.id.tv_item_product_name);
            productPrice = (TextView)itemView.findViewById(R.id.tv_item_product_price);
            productQuantity = (TextView) itemView.findViewById(R.id.tv_item_product_quantity);



        }
    }
    public class PromotionViewHolder extends RecyclerView.ViewHolder {
        TextView mPromotionName;


        public PromotionViewHolder(View itemView) {
            super(itemView);
            mPromotionName = (TextView) itemView.findViewById(R.id.tv_item_promotion_name);
        }
    }

    private void editInfoDialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edit_info, null);

        dialogBuilder.setView(dialogView);


        dialogBuilder.setTitle("Thay đổi thông tin khách hàng");

        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        Spinner spinPayment = (Spinner)dialogView.findViewById(R.id.spinner_update_order_payment);
        final EditText edtDate = (EditText)dialogView.findViewById(R.id.edt_dialog_edit_info_date);
        Button btnOk = (Button)dialogView.findViewById(R.id.btn_dialog_edit_info_ok);

        String[] arrayDistPayment = this.getResources().getStringArray(R.array.payment_type_array);
        List<String> arrayListPayment = Arrays.asList(arrayDistPayment);
        MySpinerAdapter spinerAdapterPayment = new MySpinerAdapter();
        spinerAdapterPayment.addItems(arrayListPayment);
        MaterialSpinner spinnerPayment = (MaterialSpinner)dialogView.findViewById(R.id.spinner_update_order_payment);
        spinnerPayment.setAdapter(spinerAdapterPayment);
        spinnerPayment.setSelection(0);

        spinPayment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choosenPayment = (String) parent.getItemAtPosition(position);
                b.putString("PaymentType",choosenPayment);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                refDatabase.child(emailLogin+"/Order").child("UnApproved").child(orderPushKey).child("paymentType").setValue(b.getString("PaymentType"));
                refDatabase.child(emailLogin+"/Order").child("UnApproved").child(orderPushKey).child("dateDelivery").setValue(edtDate.getText().toString());

                refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").child("paymentType").setValue(b.getString("PaymentType"));
                refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").child("dateDelivery").setValue(edtDate.getText().toString());

                dialog.dismiss();
            }
        });

    }
    private void editProductDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edit_product, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Thay đổi thông tin sản phẩm");
        dialogBuilder.setMessage("Nhập vào phần số lượng sản phẩm để thay đổi (nếu cần)");

        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        final RecyclerView editProductRecycler = (RecyclerView)dialogView.findViewById(R.id.edit_product_recycler);
        final Button addProduct = (Button)dialogView.findViewById(R.id.btn_dialog_edit_product_add);

        addProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);

                addProductDialog();
               // dialog.dismiss();

            }
        });


        editProductRecycler.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        editProductRecycler.setLayoutManager(linearLayoutManager);

        refProduct = refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList");

        adapterFirebaseEditProduct = new FirebaseRecyclerAdapter<Product, EditProductViewHolder>(
                Product.class,
                R.id.item_product_edit,
                EditProductViewHolder.class,
                refProduct
        ) {
            @Override
            public EditProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_edit,parent,false);
                return new EditProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(EditProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productPrice.setText(model.getUnitPrice());
                viewHolder.productQuantity.setText(model.getUnitQuantity());
            }
        };

        editProductRecycler.setAdapter(adapterFirebaseEditProduct);
        adapterFirebaseEditProduct.notifyDataSetChanged();
    }
    private void addProductDialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_product, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Thêm sản phẩm");
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();



        edtDialogProductPrice = (EditText)dialogView.findViewById(R.id.edt_add_product_price);
        edtDialogProductPrice.setEnabled(false);
        //edtDialogProductPrice.addTextChangedListener(new Utils.NumberTextWatcherForThousand(edtDialogProductPrice));

        final EditText edtDialogProductQuantity = (EditText)dialogView.findViewById(R.id.edt_add_product_quantity);
        Button btnDialogAddProduct = (Button)dialogView.findViewById(R.id.btn_add_product);
        Button btnDialogCancel = (Button)dialogView.findViewById(R.id.btn_dialog_cancel);
        tvProductChoosen = dialogView.findViewById(R.id.tv_add_product_choose);
        tvProductStorage = dialogView.findViewById(R.id.tv_add_product_storage);

        tvProductChoosen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                productListDialog();
            }
        });

        btnDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                dialog.dismiss();
            }
        });

        btnDialogAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                String productQuantity = edtDialogProductQuantity.getText().toString();
                float productQuantityInt = Float.parseFloat(productQuantity);

                String productPrice = edtDialogProductPrice.getText().toString();


                if(productQuantityInt > Float.parseFloat(productStorageDialog)){
                    Toast.makeText(getApplicationContext(),"Không đủ hàng tồn kho", Toast.LENGTH_LONG).show();
                    edtDialogProductQuantity.setText("");
                }
                else
                 if(TextUtils.isEmpty(productPrice)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập đơn giá", Toast.LENGTH_LONG).show();
                } else if (TextUtils.isEmpty(productQuantity)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập số lượng", Toast.LENGTH_LONG).show();

                }else {

                     float productPayment = Float.parseFloat(productPrice) * Float.parseFloat(productQuantity);
                     Product productAdded = new Product(productNameDialog,productPrice,productQuantity,productCode,productPayment+"");
                     refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList").push().setValue(productAdded);

                     /*

                    if(discountVAT){
                        VatModel vat = new VatModel(currentNotVAT+"",currentVAT+"",currentNotVATDiscount+"",currentVATDiscount+"",currentVATDiscount+"");
                        Product productAdded = new Product(productNameDialog,productPrice,productQuantity,choosenVATDialog,newVATDiscount+"");
                        refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList").push().setValue(productAdded).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                            }
                        });
                        refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("VAT").setValue(vat).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();
                            }
                        });
                    }else{

                        VatModel vat = new VatModel(currentNotVAT+"",currentVAT+"",currentNotVATDiscount+"",currentVATDiscount+"",currentNotVATDiscount+"");
                        Product productAdded = new Product(productNameDialog,productPrice,productQuantity,choosenVATDialog,newNotVATDiscount+"");
                        refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList").push().setValue(productAdded).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                            }
                        });
                        refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("VAT").setValue(vat).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();
                            }
                        });
                    }


                     */

                }
            }
        });

    }
    private void productListDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_list,null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setMessage("Chọn sản phẩm (trượt dọc để xem tiếp)");

        dialogProductList = dialogBuilder.create();
        dialogProductList.show();

        final RecyclerView productList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_product_list);
        productList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productList.setLayoutManager(linearLayoutManager);

        Button btnNew = dialogView.findViewById(R.id.btn_product_list_new);

        btnNew.setVisibility(View.GONE);

        adapterFirebaseAddProduct = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
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
            protected void populateViewHolder(final ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());

                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        view.startAnimation(buttonClick);

                        if(!dialogPromotion){
                            if(dialogProductList!=null) dialogProductList.dismiss();

                            int position = viewHolder.getLayoutPosition();
                            final Product p = adapterFirebaseAddProduct.getItem(position);
                            productNameDialog = p.getProductName();
                            productCode = p.getProductCode();
                            tvProductChoosen.setText(p.getProductName());

                            refDatabase.child(emailLogin).child("Product").child(p.getProductCode()).child("unitPrice").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    edtDialogProductPrice.setText(dataSnapshot.getValue().toString());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        refDatabase.child(emailLogin+"/WarehouseMan/StorageMan").child(p.getProductCode()).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    productStorageDialog = dataSnapshot.getValue().toString();
                                    tvProductStorage.setText(productStorageDialog);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }else{
                            if(dialogProductList!=null) dialogProductList.dismiss();

                            int position = viewHolder.getLayoutPosition();

                            final Product p = adapterFirebaseAddProduct.getItem(position);
                            promotionNameDialog = p.getProductName();

                            tvPromotionChoosen.setText(p.getProductName());

                            refDatabase.child(emailLogin+"/WarehouseMan/StorageMan").child(p.getProductCode()).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    promotionStorageDialog = dataSnapshot.getValue().toString();
                                    tvPromotionStorage.setText(promotionStorageDialog);

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }


                    }
                });

            }
        };

        productList.setAdapter(adapterFirebaseAddProduct);
        adapterFirebaseAddProduct.notifyDataSetChanged();


    }

    private void addNewProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_product,null);
        builder.setView(dialogView);
        builder.setMessage("Thêm sản phẩm mới");

        final Dialog dialog = builder.create();
        dialog.show();

        final EditText edtAddProduct = (EditText)dialogView.findViewById(R.id.edt_dialog_add_product_name);
        Button btnAdd = (Button) dialogView.findViewById(R.id.btn_dialog_add_product_ok);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                final String productName = edtAddProduct.getText().toString();

                if(TextUtils.isEmpty(productName)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập tên Nhân viên", Toast.LENGTH_LONG).show();

                }else{
                    Product product = new Product(productName);
                    refDatabase.child(emailLogin+"/Product").push().setValue(product);

                    dialog.dismiss();
                }


            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_preview,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_send_approvement){

            showProgressDialog();

            String thisYear = Calendar.getInstance().get(Calendar.YEAR)+"";
            String thisMonth = (Calendar.getInstance().get(Calendar.MONTH)+1)+"";
            String thisDate = Calendar.getInstance().get(Calendar.DATE)+"";

            refDatabase.child(emailLogin).child("OrderListByTime").child(thisYear+"-"+thisMonth+"-"+thisDate).child(orderPushKey).setValue(orderName);

            refDatabase.child(emailLogin+"/Order").child("UnApproved").child(orderPushKey).child("orderName").setValue(orderName).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                    Intent intent = new Intent(getApplicationContext(), OrderManActivity.class);
                    intent.putExtra("OrderName",orderName);
                    intent.putExtra("EmailLogin",emailLogin);
                    startActivity(intent);
                    hideProgressDialog();

                }
            });

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(!viewOnly){
            refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).setValue(null);
            startActivity(new Intent(this,MainActivity.class));
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

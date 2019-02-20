package a1a4w.onhandsme.order;

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
import android.support.v7.widget.Toolbar;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import a1a4w.onhandsme.MainActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.bytask.OrderManActivity;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.model.VatModel;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.MySpinerAdapter;
import a1a4w.onhandsme.utils.Utils;
import fr.ganfra.materialspinner.MaterialSpinner;

import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class PreviewOrderActivivity extends AppCompatActivity {

    private RecyclerView recyclerViewProduct, recyclerViewPromotion;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct,adapterFirebasePromotion,adapterFirebaseAddProduct;
    private FirebaseRecyclerAdapter<Product,EditProductViewHolder> adapterFirebaseEditProduct;
    private FirebaseRecyclerAdapter<Product,EditPromotionViewHolder> adapterFirebaseEditPromotion;

    private DatabaseReference refProduct, refPromotion,refOrderList,refStorage;
    private ImageButton ibInfo, ibProduct, ibPromotion;
    private boolean discountVAT;

    private TextView tvClientName, tvClientType, tvPayment, tvDelivery, tvNotVAT,tvPromotionStorage,tvProductStorage,
            tvVAT,tvNotVATDiscount, tvVATDiscount,tvClientAddress, tvDeliveryName,tvNote,tvPromotionChoosen,tvProductChoosen;
    private String orderPushKey,orderName,employeeName,clientType,clientCode,paymentType,deliveryDate,productName,emailLogin,productPrice,unitName,orderDiscount;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_order);

        Intent intent = this.getIntent();
        discountVAT = intent.getBooleanExtra("DiscountTax",false);
        orderPushKey = intent.getStringExtra("OrderPushKey");
        orderName = intent.getStringExtra("OrderName");

        orderDiscount = intent.getStringExtra("OrderDiscount");
       // choosenVAT = intent.getStringExtra("ChoosenVAT");
        clientCode = intent.getStringExtra("ClientCode");
        emailLogin = intent.getStringExtra("EmailLogin");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_preview_order);
        setSupportActionBar(toolbar);

        refOrderList = refDatabase.child(emailLogin).child("OrderList");
        refStorage = refDatabase.child(emailLogin).child("Storage");

        initializeScreen();

    }

    private void initializeScreen() {

        recyclerViewProduct = (RecyclerView)findViewById(R.id.recycler_order_detail_product);
        recyclerViewPromotion = (RecyclerView)findViewById(R.id.order_recycler_promotion_preview);
        tvClientName = (TextView)findViewById(R.id.tv_approve_sale_client_name);
        tvClientType = (TextView)findViewById(R.id.tv_approve_sale_client_type);
        tvPayment = (TextView)findViewById(R.id.tv_approve_sale_payment_type);
        tvDelivery = (TextView)findViewById(R.id.tv_approve_sale_delivery_date);
        tvNotVAT = (TextView)findViewById(R.id.tv_detail_notVAT_preview);
        tvVAT = (TextView)findViewById(R.id.tv_detail_VAT_preview);
        tvNotVATDiscount = (TextView)findViewById(R.id.tv_detail_notVAT_discount_preview);
        tvVATDiscount = (TextView)findViewById(R.id.tv_detail_VAT_discount_preview);
        tvClientAddress = (TextView)findViewById(R.id.tv_preview_order_address);
        tvDeliveryName = (TextView)findViewById(R.id.tv_preview_order_delivery_name);
        tvNote = (TextView)findViewById(R.id.tv_preview_order_note);

        ibInfo = (ImageButton)findViewById(R.id.ib_preview_edit_information);
        ibProduct = (ImageButton)findViewById(R.id.ib_preview_edit_product);
        ibPromotion = (ImageButton)findViewById(R.id.ib_preview_edit_promotion);

        if(discountVAT){
            tvNotVATDiscount.setVisibility(View.INVISIBLE);
            tvVATDiscount.setVisibility(View.VISIBLE);
        }else{
            tvNotVATDiscount.setVisibility(View.VISIBLE);
            tvVATDiscount.setVisibility(View.INVISIBLE);

        }

        ibInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                editInfoDialog();
            }
        });

        ibProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                editProductDialog();

            }
        });

        ibPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                editPromotionDialog();
            }
        });

        initializeRecyclerViewProduct();

        initializeRecyclerViewPromotion();

        viewVAT();
    }

    @Override
    protected void onResume() {
        viewOtherInformation();

        super.onResume();
    }

    private void getVAT() {
        refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("VAT").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                VatModel vatModel = dataSnapshot.getValue(VatModel.class);
                if(vatModel!=null){
                    b.putString("NotVAT",vatModel.getNotVat());
                    b.putString("IncludedVAT",vatModel.getIncludedVat());
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void viewVAT() {
        refOrderList.child(orderPushKey).child("VAT").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                VatModel currentVat = dataSnapshot.getValue(VatModel.class);
                if(currentVat!=null){
                    b.putString("NotVAT",currentVat.getNotVat());
                    b.putString("IncludedVAT",currentVat.getIncludedVat());

                    String notVATValue = currentVat.getNotVat();
                    tvNotVAT.setText(Utils.convertNumber(notVATValue));

                    String vatValue = currentVat.getIncludedVat();
                    tvVAT.setText(Utils.convertNumber(vatValue));

                    String notVatDis = currentVat.getNotVatDiscount();
                    tvNotVATDiscount.setText(Utils.convertNumber(notVatDis));

                    String vatDis = currentVat.getIncludedVatDiscount();
                    tvVATDiscount.setText(Utils.convertNumber(vatDis));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void viewOtherInformation() {
        refDatabase.child(emailLogin+"/OrderList").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(orderPushKey)){
                    refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("OtherInformation")){
                                refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                                        if(orderDetail != null){
                                            String clientName = orderDetail.getOrderName();
                                            String clientType = orderDetail.getClientType();
                                            String payment = orderDetail.getPaymentType();
                                            String deliveryDate = orderDetail.getDateDelivery();
                                            String orderNote = orderDetail.getOrderNote();

                                            tvClientName.setText(clientName);
                                            tvClientType.setText(clientType);
                                            tvPayment.setText(payment);
                                            tvDelivery.setText(deliveryDate);
                                            tvNote.setText(orderNote);

                                            refDatabase.child(emailLogin+"/Client").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Client client = dataSnapshot.getValue(Client.class);
                                                    String clientAddress = client.getClientStreet();
                                                    String clientDistrict = client.getClientDistrict();
                                                    String clientCity = client.getClientCity();
                                                    String clientDeliveryName = client.getClientDeliveryName();
                                                    tvClientAddress.setText(clientAddress+", "+clientDistrict+", "+clientCity);
                                                    tvDeliveryName.setText(clientDeliveryName);
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

        adapterFirebasePromotion = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product,
                ProductViewHolder.class,
                refPromotion
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product,parent,false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productQuantity.setText(model.getUnitQuantity());
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
    private void editPromotionDialog() {

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edit_promotion, null);

        final RecyclerView editPromotionRecycler = (RecyclerView)dialogView.findViewById(R.id.edit_promotion_recycler);
        final Button btnAddPromotion = (Button)dialogView.findViewById(R.id.btn_edit_promotion_add);

        btnAddPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPromotionDialog();
            }
        });

        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Thay đổi thông tin khuyến mãi");

        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        editPromotionRecycler.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        editPromotionRecycler.setLayoutManager(linearLayoutManager);


        refPromotion = refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion");

        adapterFirebaseEditPromotion = new FirebaseRecyclerAdapter<Product, EditPromotionViewHolder>(
                Product.class,
                R.id.item_promotion_edit,
                EditPromotionViewHolder.class,
                refPromotion
        ) {
            @Override
            public EditPromotionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion_edit,parent,false);
                return new EditPromotionViewHolder(v);
            }


            @Override
            protected void populateViewHolder(EditPromotionViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productPrice.setText(model.getUnitPrice());
                viewHolder.productQuantity.setText(model.getUnitQuantity());
            }
        };

        editPromotionRecycler.setAdapter(adapterFirebaseEditPromotion);
        adapterFirebaseEditPromotion.notifyDataSetChanged();


    }
    private void editProductDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edit_product, null);
        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Thay đổi thông tin sản phẩm");

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
        getVAT();
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_product, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setTitle("Thêm sản phẩm");
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        Spinner spinProductVAT = (Spinner)dialogView.findViewById(R.id.spinner_dialog_add_product_VAT);
        tvProductStorage = (TextView)dialogView.findViewById(R.id.tv_add_product_storage);
        tvProductChoosen = (TextView)dialogView.findViewById(R.id.tv_add_product_choose);

        final EditText edtDialogProductPrice = (EditText)dialogView.findViewById(R.id.edt_add_product_price);
        //edtDialogProductPrice.addTextChangedListener(new Utils.NumberTextWatcherForThousand(edtDialogProductPrice));

        final EditText edtDialogProductQuantity = (EditText)dialogView.findViewById(R.id.edt_add_product_quantity);
        Button btnDialogAddProduct = (Button)dialogView.findViewById(R.id.btn_add_product);
        Button btnDialogCancel = (Button)dialogView.findViewById(R.id.btn_dialog_cancel);

        String[] arrayProductVAT = this.getResources().getStringArray(R.array.VAT_array);
        List<String> arrayListProductVAT = Arrays.asList(arrayProductVAT);
        MySpinerAdapter spinerAdapterProductVAT = new MySpinerAdapter();
        spinerAdapterProductVAT.addItems(arrayListProductVAT);
        MaterialSpinner spinnerProductVAT = (MaterialSpinner)dialogView.findViewById(R.id.spinner_dialog_add_product_VAT);
        spinnerProductVAT.setAdapter(spinerAdapterProductVAT);
        spinnerProductVAT.setSelection(0);

        spinProductVAT.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosenVATDialog = (String) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

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
                String notVAT = b.getString("NotVAT");
                String VAT = b.getString("IncludedVAT");

                float choosenVATInt = Float.parseFloat(choosenVATDialog);
                float discount = Float.parseFloat(orderDiscount)/100;

                float newNotVAT = Float.parseFloat(productPrice)*Float.parseFloat(productQuantity);
                float newVAT = Float.parseFloat(productPrice)*Float.parseFloat(productQuantity)*(1+choosenVATInt);
                float newNotVATDiscount = newNotVAT*(1-discount);
                float newVATDiscount = newVAT*(1-discount);;

                float  currentNotVAT = newNotVAT + Float.parseFloat(notVAT);
                float  currentVAT = newVAT + Float.parseFloat(VAT);

                float currentNotVATDiscount = currentNotVAT*(1-discount);
                float currentVATDiscount = currentVAT*(1-discount);

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




                    /*
                     Constants.refOrderDetail.child(thisYear+"").child(thisMonth+"").child(thisDate+"").child(orderName)
                            .child("ProductList").push().setValue(productAdded).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            addMoreDialog();
                        }
                    });

                     */

                }
            }
        });

    }
    public void addPromotionDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_add_promotion, null);

        final EditText edtDialogProductPrice = (EditText)dialogView.findViewById(R.id.edt_add_product_price);
        final EditText edtDialogProductQuantity = (EditText)dialogView.findViewById(R.id.edt_add_product_quantity);
        final Button btnDialogAddPromotion = (Button)dialogView.findViewById(R.id.btn_add_product);
        Button btnDialogeCancel = (Button)dialogView.findViewById(R.id.btn_dialog_cancel);
        tvPromotionChoosen = (TextView)dialogView.findViewById(R.id.tv_dialog_add_promotion_choosen_product);
        tvPromotionStorage = (TextView)dialogView.findViewById(R.id.tv_add_promotion_storage);

        tvPromotionChoosen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                productListDialog();
                dialogPromotion = true;

            }
        });

        dialogBuilder.setView(dialogView);

        dialogBuilder.setTitle("Thêm hàng khuyến mãi");

        dialogAddPromotion = dialogBuilder.create();
        dialogAddPromotion.show();
        btnDialogeCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                dialogAddPromotion.dismiss();
            }
        });

        btnDialogAddPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnDialogAddPromotion.setEnabled(false);
                v.startAnimation(Constants.buttonClick);
                String promotionPrice = edtDialogProductPrice.getText().toString();
                final String promotionQuantity = edtDialogProductQuantity.getText().toString();
                Product promotion = new Product(promotionNameDialog,promotionQuantity);

                if (TextUtils.isEmpty(promotionQuantity)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập số lượng", Toast.LENGTH_LONG).show();

                }

                else if (Float.parseFloat(promotionQuantity)>Float.parseFloat(promotionStorageDialog)){
                    Toast.makeText(getApplicationContext(),"Không đủ hàng tồn kho.", Toast.LENGTH_LONG).show();

                }
                else {
                    refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion").push().setValue(promotion);
                    dialogAddPromotion.dismiss();
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

                            tvProductChoosen.setText(p.getProductName());

                            refDatabase.child(emailLogin+"/WarehouseMan/StorageMan").child(p.getProductName()).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
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

                            refDatabase.child(emailLogin+"/WarehouseMan/StorageMan").child(promotionNameDialog).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
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
        startActivity(new Intent(this,MainActivity.class));
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

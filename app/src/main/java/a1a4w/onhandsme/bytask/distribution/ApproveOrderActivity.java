package a1a4w.onhandsme.bytask.distribution;

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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.list.ClientListActivity;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.model.VatModel;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.Utils;

public class ApproveOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProduct, recyclerViewPromotion;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct,adapterFirebasePromotion;
    private DatabaseReference refProduct, refPromotion;

    private TextView tvClientName, tvClientType, tvPayment, tvDelivery, tvNotVAT, tvVAT,tvFinalPayment,tvClientDebt,tvNote;
    private String orderPushKey,emailLogin;
    private Bundle b = new Bundle();
    private ProgressDialog mProgressDialog;
    private String email;
    private boolean normal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_order);

        Intent intent = this.getIntent();
        orderPushKey = intent.getStringExtra("OrderPushKey");
        emailLogin = intent.getStringExtra("EmailLogin");
        normal = intent.getBooleanExtra("Normal",false);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_approve_warehouse_in);
        setSupportActionBar(toolbar);

        recyclerViewProduct = (RecyclerView)findViewById(R.id.recycler_order_detail_product);
        recyclerViewPromotion = (RecyclerView)findViewById(R.id.recyclerview_order_detail_promotion);
        tvClientName = (TextView)findViewById(R.id.tv_approve_sale_client_name);
        tvClientType = (TextView)findViewById(R.id.tv_approve_sale_client_type);
        tvPayment = (TextView)findViewById(R.id.tv_approve_sale_payment_type);
        tvDelivery = (TextView)findViewById(R.id.tv_approve_sale_delivery_date);
        tvNotVAT = (TextView)findViewById(R.id.tv_detail_notVAT_approve_order);
        tvVAT = (TextView)findViewById(R.id.tv_detail_VAT_aprove_order);
        tvFinalPayment = (TextView)findViewById(R.id.tv_detail_final_payment_approve_order);
        tvClientDebt = (TextView)findViewById(R.id.tv_approve_order_client_debt);
        tvNote = (TextView)findViewById(R.id.tv_approve_order_order_note);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        email = firebaseAuth.getCurrentUser().getEmail();

        initializeRecyclerViewProduct();

        initializeRecyclerViewPromotion();

        viewVAT();

        viewOtherInformation();

        //setUpdateApprovedQuantity();

    }


    private void viewOtherInformation() {
        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                if(orderDetail!=null){
                    String clientName = orderDetail.getOrderName();
                    String clientType = orderDetail.getClientType();
                    String paymentType = orderDetail.getPaymentType();
                    String deliveryDate = orderDetail.getDateDelivery();
                    String clientCode = orderDetail.getClientCode();
                    String orderNote = orderDetail.getOrderNote();

                    b.putString("PaymentType",paymentType);

                    tvNote.setText(orderNote);
                    tvClientName.setText(clientName);
                    tvClientType.setText(clientType);
                    tvPayment.setText(paymentType);
                    tvDelivery.setText(deliveryDate);
                    Constants.refDatabase.child(emailLogin+"/Client").child(clientCode).child("clientDebt").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String clientDebt = dataSnapshot.getValue().toString();

                            tvClientDebt.setText(Utils.convertNumber(clientDebt));
                            hideProgressDialog();
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

    private void viewVAT() {
        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("VAT")){
                    Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("VAT").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            VatModel currentVat = dataSnapshot.getValue(VatModel.class);
                            if(currentVat!=null){
                                tvNotVAT.setText(Utils.convertNumber(currentVat.getNotVat()));
                                tvVAT.setText(Utils.convertNumber(currentVat.getIncludedVat()));
                                tvFinalPayment.setText(Utils.convertNumber(currentVat.getFinalPayment()));
                                hideProgressDialog();
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
        showProgressDialog();
        recyclerViewPromotion.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewPromotion.setLayoutManager(linearLayoutManager);

        refPromotion = Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion");

        adapterFirebasePromotion = new FirebaseRecyclerAdapter<Product,ProductViewHolder>(
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
                viewHolder.productPrice.setText(model.getUnitPrice());
                viewHolder.productQuantity.setText(model.getUnitQuantity());
            }
        };

        recyclerViewPromotion.setAdapter(adapterFirebasePromotion);
        adapterFirebasePromotion.notifyDataSetChanged();
        hideProgressDialog();
    }

    private void initializeRecyclerViewProduct() {
        showProgressDialog();
        recyclerViewProduct.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewProduct.setLayoutManager(linearLayoutManager);

        refProduct = Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList");

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
                viewHolder.productPrice.setText(model.getUnitPrice());
                viewHolder.productQuantity.setText(model.getUnitQuantity());
            }
        };

        recyclerViewProduct.setAdapter(adapterFirebaseProduct);
        adapterFirebaseProduct.notifyDataSetChanged();
        hideProgressDialog();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_approve_order,menu);
        MenuItem itemApproveImport = menu.findItem(R.id.action_approve_order_in);
        MenuItem itemApproveOut = menu.findItem(R.id.action_approve_order_out);

        if(normal){
            itemApproveImport.setVisible(true);
            itemApproveImport.setVisible(false);

        }else{
            itemApproveImport.setVisible(false);
            itemApproveImport.setVisible(true);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_approve_order_out){
            AlertDialog.Builder builder= new AlertDialog.Builder(ApproveOrderActivity.this);

            builder.setMessage("Duyệt đơn hàng xuất kho?");
            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showProgressDialog();

                    String paymentType = b.getString("PaymentType");

                    if (paymentType == null) {
                        Toast.makeText(getApplicationContext(), "Đang xử lý...", Toast.LENGTH_LONG).show();
                    } else {
                        Constants.refDatabase.child(emailLogin + "/OrderList").child(orderPushKey).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                                Constants.refDatabase.child(emailLogin + "/Order").child("UnApproved").child(orderPushKey).setValue(null);
                                Constants.refDatabase.child(emailLogin + "/Order").child("Approved").child(orderPushKey).setValue(orderDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        hideProgressDialog();
                                        Intent it = new Intent(getApplicationContext(),DistributionManActivity.class);
                                        it.putExtra("EmailLogin",emailLogin);
                                        startActivity(it);
                                    }
                                });

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }

                }
            });

            builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Constants.refDatabase.child(emailLogin+"/Order").child("UnApproved").child(orderPushKey).setValue(null);
                    dialog.dismiss();

                }
            });
            builder.show();

        }

        if(id==R.id.action_approve_order_in){
            AlertDialog.Builder builder= new AlertDialog.Builder(ApproveOrderActivity.this);
            builder.setMessage("Duyệt đơn hàng nhập kho?");
            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                            Constants.refDatabase.child(emailLogin+"/Order").child("DeniedWarehouse").child(orderPushKey).setValue(orderDetail);
                            Constants.refDatabase.child(emailLogin+"/Order").child("DeniedDistribution").child(orderPushKey).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    Intent it = new Intent(getApplicationContext(),DistributionManActivity.class);
                                    it.putExtra("EmailLogin",emailLogin);
                                    startActivity(it);
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

            builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();

                }
            });
            builder.show();
        }
        if(id ==R.id.action_approve_delete){
            AlertDialog.Builder builder= new AlertDialog.Builder(ApproveOrderActivity.this);
            builder.setMessage("Xóa đơn hàng?");

            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Constants.refDatabase.child(emailLogin+"/Order").child("Deleted").push().setValue(orderPushKey);
                    Constants.refDatabase.child(emailLogin+"/OrderList").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(orderPushKey)){
                                Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();
                                        for (DataSnapshot p:productSnap){
                                            Product product = p.getValue(Product.class);
                                            final String productName = product.getProductName();
                                            final float productOrderQuantity = Float.parseFloat(product.getUnitQuantity());

                                            Constants.refDatabase.child(emailLogin+"/Storage").child(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    String productStorage = dataSnapshot.getValue().toString();
                                                    float updateStorage = Float.parseFloat(productStorage) + productOrderQuantity;
                                                    Constants.refDatabase.child(emailLogin+"/Storage").child(productName).setValue(updateStorage+"");
                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(productName).child("unitQuantity").setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(!productSnap.iterator().hasNext()){
                                                                Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        if(dataSnapshot.hasChild("Promotion")){
                                                                            Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();
                                                                                    for (DataSnapshot p:productSnap){
                                                                                        Product product = p.getValue(Product.class);
                                                                                        String productName = product.getProductName();
                                                                                        final float productOrderQuantity = Float.parseFloat(product.getUnitQuantity());
                                                                                        Constants.refDatabase.child(emailLogin+"/Storage").child(productName).runTransaction(new Transaction.Handler() {
                                                                                            @Override
                                                                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                                                                String productStorage = mutableData.getValue().toString();
                                                                                                float updateStorage = Float.parseFloat(productStorage) + productOrderQuantity;
                                                                                                mutableData.setValue(updateStorage+"");
                                                                                                return Transaction.success(mutableData);
                                                                                            }

                                                                                            @Override
                                                                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                                                            }
                                                                                        });

                                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(productName).child("unitQuantity").runTransaction(new Transaction.Handler() {
                                                                                            @Override
                                                                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                                                                String productStorage = mutableData.getValue().toString();
                                                                                                float updateStorage = Float.parseFloat(productStorage) + productOrderQuantity;
                                                                                                mutableData.setValue(updateStorage+"");
                                                                                                return Transaction.success(mutableData);
                                                                                            }

                                                                                            @Override
                                                                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                                                            }
                                                                                        });

                                                                                    }



                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });

                                                                        }else{
                                                                            Constants.refDatabase.child(emailLogin+"/Order").child("UnApproved").child(orderPushKey).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    Intent it = new Intent(getApplicationContext(),DistributionManActivity.class);
                                                                                    it.putExtra("EmailLogin",emailLogin);
                                                                                    startActivity(it);
                                                                                    hideProgressDialog();
                                                                                }
                                                                            });;
                                                                        }
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });

                                                            }
                                                        }
                                                    });

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
                                Constants.refDatabase.child(emailLogin+"/Order").child("UnApproved").child(orderPushKey).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Intent it = new Intent(getApplicationContext(),DistributionManActivity.class);
                                        it.putExtra("EmailLogin",emailLogin);
                                        startActivity(it);
                                        hideProgressDialog();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            });
            builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
        return super.onOptionsItemSelected(item);
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

    private void setUpdateApprovedQuantity() {
        showProgressDialog();

        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> promotionSnap = dataSnapshot.getChildren();
                int i = 0;
                for(DataSnapshot product:promotionSnap){
                    final int y = i;
                    Product p = product.getValue(Product.class);
                    final String productName = p.getProductName();
                    final String productQuantity = p.getUnitQuantity();
                    b.putString("ProductName"+i,productName);

                    Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("Promotion")){
                                Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> promotionSnap = dataSnapshot.getChildren();
                                        int x = 0;
                                        for(DataSnapshot promotion:promotionSnap){
                                            final int z = x;
                                            Product p = promotion.getValue(Product.class);
                                            final String promotionName = p.getProductName();
                                            b.putString("ProductName"+y+z,productName);
                                            b.putString("PromotionName"+y+z,promotionName);

                                            final String promotionQuantity = p.getUnitQuantity();

                                            if(promotionName.equals(productName)){
                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.hasChild("ApprovedOrder")){
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ApprovedOrder").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if(dataSnapshot.hasChild(promotionName)){
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ApprovedOrder").child(promotionName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                Product product = dataSnapshot.getValue(Product.class);
                                                                                String approvedQuantity = product.getUnitQuantity();
                                                                                float updateApprovedQuantity = Float.parseFloat(approvedQuantity)+Float.parseFloat(productQuantity)+Float.parseFloat(promotionQuantity);
                                                                                b.putString("UpdateApprovedProduct"+y+z,updateApprovedQuantity+"");
                                                                                b.putString("UpdateApprovedPromotion"+y+z,"0");

                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                                    }else{
                                                                        float updateApprovedQuantity = Float.parseFloat(productQuantity)+Float.parseFloat(promotionQuantity);
                                                                        b.putString("UpdateApprovedProduct"+y+z,updateApprovedQuantity+"");
                                                                        b.putString("UpdateApprovedPromotion"+y+z,"0");
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });
                                                        }else{
                                                            float updateApprovedQuantity = Float.parseFloat(productQuantity)+Float.parseFloat(promotionQuantity);
                                                            b.putString("UpdateApprovedProduct"+y+z,updateApprovedQuantity+"");                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                            }else{
                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.hasChild("ApprovedOrder")){
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ApprovedOrder").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.hasChild(promotionName)) {
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ApprovedOrder").child(promotionName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                Product product = dataSnapshot.getValue(Product.class);
                                                                                String approvedQuantity = product.getUnitQuantity();
                                                                                // float updateProductQuantity = Float.parseFloat(approvedQuantity)+Float.parseFloat(productQuantity);
                                                                                float updatePromotionQuantity = Float.parseFloat(approvedQuantity) + Float.parseFloat(promotionQuantity);

                                                                                // b.putString("UpdateApprovedProduct"+y+z,updateProductQuantity+"");
                                                                                b.putString("UpdateApprovedPromotion" + y + z, updatePromotionQuantity + "");

                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                                    } else {
                                                                        float updateApprovedPromotion = Float.parseFloat(promotionQuantity);

                                                                        // b.putString("UpdateProductQuantity"+y+z,updateApprovedQuantity+"");
                                                                        b.putString("UpdateApprovedPromotion" + y + z, updateApprovedPromotion + "");
                                                                    }

                                                                    if(dataSnapshot.hasChild(productName)){
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ApprovedOrder").child(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                Product product = dataSnapshot.getValue(Product.class);
                                                                                String approvedQuantity = product.getUnitQuantity();
                                                                                // float updateProductQuantity = Float.parseFloat(approvedQuantity)+Float.parseFloat(productQuantity);
                                                                                float updateApprovedProduct = Float.parseFloat(approvedQuantity) + Float.parseFloat(productQuantity);

                                                                                // b.putString("UpdateApprovedProduct"+y+z,updateProductQuantity+"");
                                                                                b.putString("UpdateApprovedProduct" + y + z, updateApprovedProduct + "");

                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    }else{
                                                                        float updateApprovedProduct = Float.parseFloat(productQuantity);
                                                                        b.putString("UpdateApprovedProduct" + y + z, updateApprovedProduct + "");
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });
                                                        }else{
                                                            float updateApprovedProduct = Float.parseFloat(productQuantity);
                                                            float updateApprovedPromotion = Float.parseFloat(promotionQuantity);

                                                            b.putString("UpdateApprovedProduct"+y+z,updateApprovedProduct+"");
                                                            b.putString("UpdateApprovedPromotion"+y+z,updateApprovedPromotion+"");                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }

                                            x++;
                                            b.putInt("ForIntPromotionValue",x);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                b.putInt("ForIntPromotionValue",-1);
                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("ApprovedOrder")){
                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ApprovedOrder").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(productName)){
                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ApprovedOrder").child(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                Product product = dataSnapshot.getValue(Product.class);
                                                                String approvedQuantity = product.getUnitQuantity();
                                                                float updateApprovedQuantity = Float.parseFloat(approvedQuantity)+Float.parseFloat(productQuantity);
                                                                b.putString("UpdateApprovedProduct"+y,updateApprovedQuantity+"");

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }else{
                                                        b.putString("UpdateApprovedProduct"+y,productQuantity);
                                                       // b.putString("ProductName"+y,productName);
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }else{
                                            b.putString("UpdateApprovedProduct"+y,productQuantity);
                                          //  b.putString("ProductName"+y,productName);
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
                    i++;
                    b.putInt("ForIntProductValue",i);
                }
                hideProgressDialog();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }




}

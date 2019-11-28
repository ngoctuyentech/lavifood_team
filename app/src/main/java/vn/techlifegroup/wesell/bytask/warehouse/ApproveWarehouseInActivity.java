package vn.techlifegroup.wesell.bytask.warehouse;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.model.Product;
import vn.techlifegroup.wesell.model.VatModel;
import vn.techlifegroup.wesell.model.WarehouseIn;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

import static vn.techlifegroup.wesell.utils.Constants.refOrderList;

public class ApproveWarehouseInActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProduct, recyclerViewPromotion;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct,adapterFirebasePromotion;
    private DatabaseReference refProduct, refPromotion;

    private TextView tvClientName, tvClientType, tvPayment, tvDelivery, tvNotVAT, tvVAT,tvFinalPayment;
    private String orderPushKey,emailLogin;
    private Bundle b = new Bundle();
    private String thisYearString, thisMonthString, thisDateString;
    private ProgressDialog mProgressDialog;
    private float VAT,notVAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_warehouse_in);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_approve_warehouse_in);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        orderPushKey = intent.getStringExtra("OrderPushKey");
        emailLogin = intent.getStringExtra("EmailLogin");

        thisYearString = Calendar.getInstance().get(Calendar.YEAR)+"";
        thisMonthString = (Calendar.getInstance().get(Calendar.MONTH)+1)+"";
        thisDateString = Calendar.getInstance().get(Calendar.DATE)+"";

        recyclerViewProduct = (RecyclerView)findViewById(R.id.recyclerview_approve_warehouse_product);
        recyclerViewPromotion = (RecyclerView)findViewById(R.id.recyclerview_order_detail_promotion);
        tvClientName = (TextView)findViewById(R.id.tv_approve_sale_client_name);
        tvClientType = (TextView)findViewById(R.id.tv_approve_sale_client_type);
        tvPayment = (TextView)findViewById(R.id.tv_approve_sale_payment_type);
        tvDelivery = (TextView)findViewById(R.id.tv_preview_delivery_date);
        tvNotVAT = (TextView)findViewById(R.id.tv_detail_notVAT_warehouse_in);
        tvVAT = (TextView)findViewById(R.id.tv_detail_VAT_warehouse_in);
        tvFinalPayment = (TextView)findViewById(R.id.tv_detail_final_paymenttv_detail_VAT_warehouse_in);

        initializeRecyclerViewProduct();
        initializeRecyclerViewPromotion();
        viewVAT();
        viewOtherInformation();
        getStorage();
    }

    private void getStorage() {
        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();
                int i = 0;
                for(DataSnapshot p:productSnap){
                    final int y= i;
                    Product product = p.getValue(Product.class);
                    String productName = product.getProductName();
                    b.putString("ProductName"+i,productName);
                    final String productQuantity = product.getUnitQuantity();
                    b.putString("ProductQuantity"+i,productQuantity);
                    i++;
                    b.putInt("IntProductMax",i);

                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(productName).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String productStorage = dataSnapshot.getValue().toString();
                            b.putString("ProductStorage"+y,productStorage);

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

        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("Promotion")){
                    Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();
                            int i = 0;
                            for(DataSnapshot p:productSnap){
                                final int y = i;
                                Product product = p.getValue(Product.class);
                                String productName = product.getProductName();
                                b.putString("PromotionName"+i,productName);
                                final String promotionQuantity = product.getUnitQuantity();
                                b.putString("PromotionQuantity"+i,promotionQuantity);

                                i++;
                                b.putInt("IntPromotionMax",i);

                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(productName).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String productStorage = dataSnapshot.getValue().toString();
                                        b.putString("PromotionStorage"+y,productStorage);

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
                    b.putInt("IntPromotionMax",-1);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void viewOtherInformation() {
        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                if(orderDetail!=null){
                    String clientName = orderDetail.getOrderName();
                    String clientType = orderDetail.getClientType();
                    String payment = orderDetail.getPaymentType();
                    String deliveryDate = orderDetail.getDateDelivery();

                    b.putString("ClientName",clientName);

                    tvClientName.setText(clientName);
                    tvClientType.setText(clientType);
                    tvPayment.setText(payment);
                    tvDelivery.setText(deliveryDate);
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

    private void initializeRecyclerViewPromotion() {
        recyclerViewPromotion.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewPromotion.setLayoutManager(linearLayoutManager);

        recyclerViewPromotion.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager)recyclerViewPromotion.getLayoutManager()).findFirstVisibleItemPosition();
                recyclerViewPromotion.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });

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
    }

    private void initializeRecyclerViewProduct() {
        recyclerViewProduct.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewProduct.setLayoutManager(linearLayoutManager);

        recyclerViewProduct.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager)recyclerViewProduct.getLayoutManager()).findFirstVisibleItemPosition();
                recyclerViewProduct.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });

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
        getMenuInflater().inflate(R.menu.menu_approve_warehouse_in,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_approve_warehouse_in){
            //Order processing
            Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                    Constants.refDatabase.child(emailLogin+"/Order").child("DeniedWarehouse").child(orderPushKey).setValue(null);
                    Constants.refDatabase.child(emailLogin+"/Order").child("WarehouseOut").child(orderPushKey).setValue(null);
                    Constants.refDatabase.child(emailLogin).child("WarehouseMan/BackIn").child(orderPushKey).setValue(orderDetail);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            AlertDialog.Builder builder= new AlertDialog.Builder(ApproveWarehouseInActivity.this);
            builder.setMessage("Xác nhận đã nhập kho?");
            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showProgressDialog();
                    int iProductMax = b.getInt("IntProductMax");
                    int iPromotionMax = b.getInt("IntPromotionMax");
                    String clientName = b.getString("ClientName");
                    long timeStamp = Calendar.getInstance().getTime().getTime();
                    String dateIn = Utils.getDate(timeStamp+"");

                    if(iProductMax == 0 || iPromotionMax == 0){
                        Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();

                    }else{
                        for(int i = 0; i<iProductMax;i++){
                            final String productQuantity = b.getString("ProductQuantity"+i);
                            final String productStorage = b.getString("ProductStorage"+i);
                            final String productName = b.getString("ProductName"+i);
                            if(productQuantity == null||productName==null || productStorage == null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();

                            }else{
                                final float updateProductStorage = Float.parseFloat(productStorage)+Float.parseFloat(productQuantity);

                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(productName).child("unitQuantity").setValue(updateProductStorage+"");

                                WarehouseIn warehouseIn = new WarehouseIn(clientName,productName,productQuantity,dateIn,productStorage,updateProductStorage+"");

                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductIn").child(productName).child(thisYearString).push().setValue(warehouseIn);
                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductIn").child(productName).child(thisYearString +"-"+ thisMonthString).push().setValue(warehouseIn);
                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductIn").child(productName).child(thisYearString +"-"+ thisMonthString +"-"+ thisDateString).push().setValue(warehouseIn);

                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("In").child(thisYearString).push().setValue(warehouseIn);
                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("In").child(thisYearString+"-"+thisMonthString).push().setValue(warehouseIn);
                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("In").child(thisYearString+"-"+thisMonthString+"-"+thisDateString).push().setValue(warehouseIn).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        hideProgressDialog();
                                        Intent it = new Intent(getApplicationContext(),WarehouseManActivity.class);
                                        it.putExtra("EmailLogin",emailLogin);
                                        startActivity(it);
                                    }
                                });



                            }
                        }

                        for(int i = 0; i<iPromotionMax;i++){
                            final String promotionName = b.getString("PromotionName"+i);
                            final String promotionQuantity = b.getString("PromotionQuantity"+i);
                            String promotionStorage = b.getString("PromotionStorage"+i);
                            if(promotionQuantity == null||promotionName==null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();

                            }else{
                                final float updatePromotionStorage = Float.parseFloat(promotionStorage) + Float.parseFloat(promotionQuantity);
                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(promotionName).child("unitQuantity").setValue(updatePromotionStorage+"");

                                WarehouseIn warehouseIn = new WarehouseIn(clientName,promotionName,promotionQuantity,dateIn,promotionStorage,updatePromotionStorage+"");

                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("In").child(thisYearString).push().setValue(warehouseIn);
                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("In").child(thisYearString+"-"+thisMonthString).push().setValue(warehouseIn);
                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("In").child(thisYearString+"-"+thisMonthString+"-"+thisDateString).push().setValue(warehouseIn);
                            }
                        }
                    }



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

}

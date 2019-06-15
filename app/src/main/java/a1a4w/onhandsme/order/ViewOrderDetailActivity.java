package a1a4w.onhandsme.order;

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

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import a1a4w.onhandsme.MainActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.bytask.debt.ApproveSaleActivity;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.DebtHistory;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.model.VatModel;
import a1a4w.onhandsme.model.WarehouseIn;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.Utils;

import static a1a4w.onhandsme.utils.Constants.refDatabase;
import static a1a4w.onhandsme.utils.Constants.refOrderList;

public class ViewOrderDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerViewProduct, recyclerViewPromotion;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct,adapterFirebasePromotion;
    private DatabaseReference refProduct, refPromotion;
    private TextView tvClientName, tvClientType, tvPayment, tvDelivery, tvNotVAT,
            tvVAT,tvFinalPayment,tvClientDebt,tvNote,tvClientAddress,tvDeliveryName,tvEmployeeName;
    private String orderPushKey,userRole,emailLogin;
    private boolean warehouse,delivery,denied,cashReturn,updateSale;
    private ProgressDialog mProgressDialog;
    private OrderDetail orderDetail;
    private String clientName;
    private String clientCode;
    private HashMap<String,Float> productStock = new HashMap<>();
    private float VAT,notVAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_order_detail);

        Intent intent = this.getIntent();
        orderPushKey = intent.getStringExtra("OrderPushKey");
        userRole = intent.getStringExtra("UserRole");
        emailLogin = intent.getStringExtra("EmailLogin");
        warehouse = intent.getBooleanExtra("Warehouse",false);
        delivery = intent.getBooleanExtra("Delivery",false);
        denied = intent.getBooleanExtra("Denied",false);
        cashReturn = intent.getBooleanExtra("CashReturn",false);
        updateSale = intent.getBooleanExtra("UpdateSale",false);

        productStock.clear();

       // Toast.makeText(getApplicationContext(),delivery+"",Toast.LENGTH_LONG).show();

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_view_order_detail);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        recyclerViewProduct = (RecyclerView)findViewById(R.id.recycler_order_detail_product);
        recyclerViewPromotion = (RecyclerView)findViewById(R.id.recyclerview_order_detail_promotion);
        tvClientName = (TextView)findViewById(R.id.tv_approve_sale_client_name);
        tvClientType = (TextView)findViewById(R.id.tv_approve_sale_client_type);
        tvPayment = (TextView)findViewById(R.id.tv_approve_sale_payment_type);
        tvDelivery = (TextView)findViewById(R.id.tv_preview_delivery_date);
        tvNotVAT = (TextView)findViewById(R.id.tv_detail_notVAT__detail);
        tvVAT = (TextView)findViewById(R.id.tv_detail_VAT__detail);
        tvClientDebt = (TextView)findViewById(R.id.tv_detail_client_debt);
        tvFinalPayment = (TextView)findViewById(R.id.tv_detail_final_payment_detail);
        tvNote = (TextView)findViewById(R.id.tv_order_detail_note);
        tvClientAddress =  (TextView)findViewById(R.id.tv_order_detail_address);
        tvDeliveryName =  (TextView)findViewById(R.id.tv_order_detail_delivery_name);
        tvEmployeeName = findViewById(R.id.tv_order_detail_employee_name);

        initializeRecyclerViewProduct();
        initializeRecyclerViewPromotion();

        viewVAT();
        viewOtherInformation();

    }

    private void viewOtherInformation() {
        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orderDetail = dataSnapshot.getValue(OrderDetail.class);
                if(orderDetail!=null){
                    clientName = orderDetail.getOrderName();
                    String clientType = orderDetail.getClientType();
                    String payment = orderDetail.getPaymentType();
                    String deliveryDate = orderDetail.getDateDelivery();
                    clientCode = orderDetail.getClientCode();
                    String orderNote = orderDetail.getOrderNote();
                    String employeeName = orderDetail.getSaleName();

                    tvClientName.setText(clientName);
                    tvClientType.setText(clientType);
                    tvPayment.setText(payment);
                    tvDelivery.setText(deliveryDate);
                    tvNote.setText(orderNote);
                    tvEmployeeName.setText(employeeName);

                    Constants.refDatabase.child(emailLogin+"/Client").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Client client = dataSnapshot.getValue(Client.class);
                            String clientAddress = client.getClientStreet();
                            String clientDistrict = client.getClientDistrict();
                            String clientCity = client.getClientCity();
                            String clientDeliveryName = client.getClientDeliveryName();
                            String clientDebt = client.getClientDebt();
                            tvClientAddress.setText(clientAddress+", "+clientDistrict+", "+clientCity);
                            tvDeliveryName.setText(clientDeliveryName);
                            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

                            float clientDebtFloat = Float.parseFloat(clientDebt);
                            String clientDebtFloatConverted = numberFormat.format(clientDebtFloat);
                            tvClientDebt.setText(clientDebtFloatConverted);
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

        refPromotion = Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion");

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
                //viewHolder.productPrice.setText(Utils.convertNumber(model.getUnitPrice()));
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
                viewHolder.productPrice.setText(Utils.convertNumber(model.getUnitPrice()));
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
        getMenuInflater().inflate(R.menu.menu_view_order,menu);
        MenuItem approveWarehouseOut = menu.findItem(R.id.action_approve_export);
        MenuItem approveWarehouseIn = menu.findItem(R.id.action_approve_import);

        MenuItem approveDelivery = menu.findItem(R.id.action_approve_delivery);
        MenuItem approveDenied = menu.findItem(R.id.action_approve_denied);
        MenuItem cashReturnItem = menu.findItem(R.id.action_approve_cash_return);
        MenuItem updateSaleItem = menu.findItem(R.id.action_approve_update_sys);

        if(warehouse){
            approveWarehouseOut.setVisible(true);
        }
        if(delivery){
            approveDelivery.setVisible(true);
            approveDenied.setVisible(true);

        }
        if(denied)
            approveWarehouseIn.setVisible(true);

        if(cashReturn){
            cashReturnItem.setVisible(true);

        }

        if(updateSale)
            updateSaleItem.setVisible(true);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
        }

        if(id == R.id.action_approve_export){
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewOrderDetailActivity.this);
            builder.setMessage("Xác nhận đơn hàng đã được xuất kho?");
            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Constants.refDatabase.child(emailLogin+"/Order").child("Approved").child(orderPushKey).setValue(null);

                    Constants.refDatabase.child(emailLogin+"/Order").child("WarehouseOut").child(orderPushKey).setValue(orderDetail);

                    Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();
                            long itemCount = dataSnapshot.getChildrenCount();

                            int i = 0;
                            for (final DataSnapshot p:productSnap){
                                i++;
                                final Product product = p.getValue(Product.class);
                                assert product != null;
                                final String productName = product.getProductName();
                                final float productQuantity = Float.parseFloat(product.getUnitQuantity());

                                if(i == itemCount){
                                    if(productStock.containsKey(productName)){
                                        float currentValue = productStock.get(productName);
                                        float updateValue = currentValue + productQuantity;
                                        productStock.put(productName,updateValue);

                                        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("Promotion")){
                                                    Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            final Iterable<DataSnapshot> promotionSnap = dataSnapshot.getChildren();
                                                            long itemCount = dataSnapshot.getChildrenCount();

                                                            int i = 0;
                                                            for(DataSnapshot promotionItem:promotionSnap){
                                                                i++;
                                                                Product promotion = promotionItem.getValue(Product.class);
                                                                assert promotion != null;
                                                                final String promotionName = promotion.getProductName();
                                                                final float promotionQuantity = Float.parseFloat(promotion.getUnitQuantity());

                                                                if(i==itemCount){

                                                                    if(productStock.containsKey(promotionName)){
                                                                        float currentValue = productStock.get(promotionName);
                                                                        float updateValue = currentValue + promotionQuantity;
                                                                        productStock.put(promotionName,updateValue);

                                                                        int y = 0;
                                                                        for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                                            y++;
                                                                            int mapItemCount = productStock.size();

                                                                            if(y == mapItemCount){
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        final String productStorage = dataSnapshot.getValue().toString();
                                                                                        final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();
                                                                                        if(updateStorage<0){
                                                                                            Toast.makeText(getApplicationContext(),entry.getKey()+ " không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                                        }else{
                                                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                                    .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    hideProgressDialog();
                                                                                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                                                    Toast.makeText(getApplicationContext(),"Xuất kho thành công!",Toast.LENGTH_LONG).show();

                                                                                                }
                                                                                            });
                                                                                        }



                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });

                                                                            }else{
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        final String productStorage = dataSnapshot.getValue().toString();
                                                                                        final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                                        if(updateStorage<0){
                                                                                            Toast.makeText(getApplicationContext(),entry.getKey()+" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                                        }else{
                                                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                                    .setValue(updateStorage+"");

                                                                                        }

                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });

                                                                            }
                                                                        }

                                                                    }else{
                                                                        productStock.put(promotionName,promotionQuantity);

                                                                        for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    final String productStorage = dataSnapshot.getValue().toString();
                                                                                    final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                                    if(updateStorage<0){
                                                                                        Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                                    }else{
                                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                                .setValue(updateStorage+"");
                                                                                    }

                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });
                                                                        }

                                                                    }

                                                                }else{
                                                                    if(productStock.containsKey(promotionName)){
                                                                        float currentValue = productStock.get(promotionName);
                                                                        float updateValue = currentValue + promotionQuantity;
                                                                        productStock.put(promotionName,updateValue);
                                                                    }else
                                                                        productStock.put(promotionName,promotionQuantity);


                                                                }
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }else{

                                                    int y = 0;
                                                    for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                        y++;
                                                        int mapItemCount = productStock.size();

                                                        if(y == mapItemCount){
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    final String productStorage = dataSnapshot.getValue().toString();
                                                                    final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                    if(updateStorage<0){
                                                                        Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                    }else{
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                hideProgressDialog();
                                                                                Toast.makeText(getApplicationContext(),"Xuất kho thành công!",Toast.LENGTH_LONG).show();
                                                                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                            }
                                                                        });
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });

                                                        }else{
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    final String productStorage = dataSnapshot.getValue().toString();
                                                                    final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                    if(updateStorage<0){
                                                                        Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                    }else{
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                .setValue(updateStorage+"");
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });

                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }else{

                                        productStock.put(productName,productQuantity);
                                        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("Promotion")){
                                                    Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            final Iterable<DataSnapshot> promotionSnap = dataSnapshot.getChildren();
                                                            long itemCount = dataSnapshot.getChildrenCount();

                                                            int i = 0;
                                                            for(DataSnapshot promotionItem:promotionSnap){
                                                                i++;
                                                                Product promotion = promotionItem.getValue(Product.class);
                                                                assert promotion != null;
                                                                final String promotionName = promotion.getProductName();
                                                                final float promotionQuantity = Float.parseFloat(promotion.getUnitQuantity());

                                                                if(i==itemCount){

                                                                    if(productStock.containsKey(promotionName)){
                                                                        float currentValue = productStock.get(promotionName);
                                                                        float updateValue = currentValue + promotionQuantity;
                                                                        productStock.put(promotionName,updateValue);

                                                                        int y = 0;
                                                                        for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                                            y++;
                                                                            int mapItemCount = productStock.size();

                                                                            if(y == mapItemCount){
                                                                                final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                                                WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(), entry.getValue()+"", timeStamp);
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(warehouseout);

                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        final String productStorage = dataSnapshot.getValue().toString();
                                                                                        final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                                        if(updateStorage<0){
                                                                                            Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                                        }else{
                                                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                                    .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    hideProgressDialog();
                                                                                                    Toast.makeText(getApplicationContext(),"Xuất kho thành công!",Toast.LENGTH_LONG).show();
                                                                                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                                                }
                                                                                            });
                                                                                        }

                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });

                                                                            }else{
                                                                                final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                                                WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(), entry.getValue()+"", timeStamp);
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(warehouseout);

                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        final String productStorage = dataSnapshot.getValue().toString();
                                                                                        final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                                        if(updateStorage<0){
                                                                                            Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                                        }else{
                                                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                                    .setValue(updateStorage+"");

                                                                                        }

                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });

                                                                            }
                                                                        }

                                                                    }else{
                                                                        productStock.put(promotionName,promotionQuantity);

                                                                        int y = 0;
                                                                        for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                                            y++;
                                                                            int mapItemCount = productStock.size();

                                                                            if(y == mapItemCount){
                                                                                final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                                                WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(), entry.getValue()+"", timeStamp);
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(warehouseout);

                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        final String productStorage = dataSnapshot.getValue().toString();
                                                                                        final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                                        if(updateStorage<0){
                                                                                            Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                                        }else{
                                                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                                    .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                    hideProgressDialog();
                                                                                                    Toast.makeText(getApplicationContext(),"Xuất kho thành công!",Toast.LENGTH_LONG).show();
                                                                                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                                                }
                                                                                            });
                                                                                        }

                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });

                                                                            }else{
                                                                                final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                                                WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(), entry.getValue()+"", timeStamp);
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(warehouseout);

                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        final String productStorage = dataSnapshot.getValue().toString();
                                                                                        final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                                        if(updateStorage<0){
                                                                                            Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                                        }else{
                                                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                                    .setValue(updateStorage+"");

                                                                                        }

                                                                                    }

                                                                                    @Override
                                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                                    }
                                                                                });

                                                                            }
                                                                        }

                                                                    }

                                                                }else{
                                                                    if(productStock.containsKey(promotionName)){
                                                                        float currentValue = productStock.get(promotionName);
                                                                        float updateValue = currentValue + promotionQuantity;
                                                                        productStock.put(promotionName,updateValue);
                                                                    }else
                                                                        productStock.put(promotionName,promotionQuantity);


                                                                }
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }else{
                                                    int y = 0;
                                                    for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                        y++;
                                                        int mapItemCount = productStock.size();

                                                        if(y == mapItemCount){

                                                            final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                            final WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(),entry.getValue()+"",timeStamp);
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(product);

                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    final String productStorage = dataSnapshot.getValue().toString();
                                                                    final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();
                                                                    if(updateStorage<0){
                                                                        Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                    }else{
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                hideProgressDialog();
                                                                                Toast.makeText(getApplicationContext(),"Xuất kho thành công!",Toast.LENGTH_LONG).show();
                                                                                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                            }
                                                                        });


                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });

                                                        }else{
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    final String productStorage = dataSnapshot.getValue().toString();
                                                                    final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                    if(updateStorage<0){
                                                                        Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                    }else{
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                .setValue(updateStorage+"");


                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });

                                                            final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                            final WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(),entry.getValue()+"",timeStamp);
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(product);
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }
                                }else{

                                    if(productStock.containsKey(productName)){
                                        float currentValue = productStock.get(productName);
                                        float updateValue = currentValue + productQuantity;
                                        productStock.put(productName,updateValue);

                                    }else{
                                        productStock.put(productName,productQuantity);
                                    }

                                }
           }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

            builder.show();
        }

        if(id == R.id.action_approve_delivery){
            AlertDialog.Builder builder = new AlertDialog.Builder(ViewOrderDetailActivity.this);
            builder.setMessage("Xác nhận đơn hàng đã được giao?");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    refDatabase.child(emailLogin).child("OrderList").child(orderPushKey).child("OtherInformation").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                            assert orderDetail != null;
                            String paymentType = orderDetail.getPaymentType();
                            final String clientCode = orderDetail.getClientCode();

                            if(paymentType.equals("Công nợ")){

                                refDatabase.child(emailLogin).child("Order/Debt").child(orderPushKey).setValue(orderDetail);
                                refDatabase.child(emailLogin).child("Client").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Client client = dataSnapshot.getValue(Client.class);
                                        assert client != null;
                                        final String clientDebt = client.getClientDebt();
                                        final String clientName = client.getClientName();
                                        refDatabase.child(emailLogin).child("OrderList").child(orderPushKey).child("VAT").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                VatModel vat = dataSnapshot.getValue(VatModel.class);
                                                assert vat != null;
                                                float finalPayment = vat.getFinalPayment();

                                                float updateClientDebt = Float.parseFloat(clientDebt) + finalPayment;
                                                //float updateClientDebt = Float.parseFloat(clientDebt) + Float.parseFloat(finalPayment);
                                                String timeStamp = (Calendar.getInstance().getTime().getTime())+"";
                                                DebtHistory debtHistory = new DebtHistory(clientName,clientDebt,finalPayment+"","0",updateClientDebt+"");

                                                refDatabase.child(emailLogin).child("Accounting/DebtHistory").child(timeStamp).setValue(debtHistory);
                                                refDatabase.child(emailLogin).child("Accounting/DebtByClient").child(clientName).child(timeStamp).setValue(debtHistory);
                                                refDatabase.child(emailLogin).child("Client").child(clientCode).child("clientDebt").setValue(updateClientDebt+"");


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

                                refDatabase.child(emailLogin).child("Order/Deliveried").child(orderPushKey).setValue(orderDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        refDatabase.child(emailLogin).child("Order/WarehouseOut").child(orderPushKey).setValue(null);
                                        Toast.makeText(getApplicationContext(),"Xác nhận giao hàng thành công!",Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                    }
                                });
                            }else{
                                refDatabase.child(emailLogin).child("Order/Cash").child(orderPushKey).setValue(orderDetail);

                                refDatabase.child(emailLogin).child("Order/Deliveried").child(orderPushKey).setValue(orderDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        refDatabase.child(emailLogin).child("Order/WarehouseOut").child(orderPushKey).setValue(null);
                                        Toast.makeText(getApplicationContext(),"Xác nhận giao hàng thành công!",Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                    }
                                });
                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }).show();
        }

        if(id == R.id.action_approve_denied){
            Constants.refDatabase.child(emailLogin+"/Order").child("WarehouseOut").child(orderPushKey).setValue(null);
            Constants.refDatabase.child(emailLogin+"/Order").child("DeniedWarehouse").child(orderPushKey).setValue(orderDetail);


        }
        
        if(id == R.id.action_approve_import){
            Constants.refDatabase.child(emailLogin+"/Order").child("WarehouseOut").child(orderPushKey).setValue(null);
            Constants.refDatabase.child(emailLogin+"/Order").child("DeniedWarehouse").child(orderPushKey).setValue(orderDetail);

            Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();
                    long itemCount = dataSnapshot.getChildrenCount();

                    int i = 0;
                    for (final DataSnapshot p:productSnap){
                        i++;
                        final Product product = p.getValue(Product.class);
                        assert product != null;
                        final String productName = product.getProductName();
                        final float productQuantity = Float.parseFloat(product.getUnitQuantity());

                        if(i == itemCount){
                            if(productStock.containsKey(productName)){
                                float currentValue = productStock.get(productName);
                                float updateValue = currentValue + productQuantity;
                                productStock.put(productName,updateValue);

                                Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("Promotion")){
                                            Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    final Iterable<DataSnapshot> promotionSnap = dataSnapshot.getChildren();
                                                    long itemCount = dataSnapshot.getChildrenCount();

                                                    int i = 0;
                                                    for(DataSnapshot promotionItem:promotionSnap){
                                                        i++;
                                                        Product promotion = promotionItem.getValue(Product.class);
                                                        assert promotion != null;
                                                        final String promotionName = promotion.getProductName();
                                                        final float promotionQuantity = Float.parseFloat(promotion.getUnitQuantity());

                                                        if(i==itemCount){

                                                            if(productStock.containsKey(promotionName)){
                                                                float currentValue = productStock.get(promotionName);
                                                                float updateValue = currentValue + promotionQuantity;
                                                                productStock.put(promotionName,updateValue);

                                                                int y = 0;
                                                                for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                                    y++;
                                                                    int mapItemCount = productStock.size();

                                                                    if(y == mapItemCount){
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                final String productStorage = dataSnapshot.getValue().toString();
                                                                                final float updateStorage = Float.parseFloat(productStorage) + entry.getValue();
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                        .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        hideProgressDialog();
                                                                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                                        Toast.makeText(getApplicationContext(),"Nhập hàng thành công!",Toast.LENGTH_LONG).show();

                                                                                    }
                                                                                });


                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    }else{
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                final String productStorage = dataSnapshot.getValue().toString();
                                                                                final float updateStorage = Float.parseFloat(productStorage) + entry.getValue();
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                        .setValue(updateStorage+"");


                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    }
                                                                }

                                                            }else{
                                                                productStock.put(promotionName,promotionQuantity);

                                                                for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            final String productStorage = dataSnapshot.getValue().toString();
                                                                            final float updateStorage = Float.parseFloat(productStorage) + entry.getValue();
                                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                    .setValue(updateStorage+"");


                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                                }

                                                            }

                                                        }else{
                                                            if(productStock.containsKey(promotionName)){
                                                                float currentValue = productStock.get(promotionName);
                                                                float updateValue = currentValue + promotionQuantity;
                                                                productStock.put(promotionName,updateValue);
                                                            }else
                                                                productStock.put(promotionName,promotionQuantity);


                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }else{

                                            int y = 0;
                                            for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                y++;
                                                int mapItemCount = productStock.size();

                                                if(y == mapItemCount){
                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            final String productStorage = dataSnapshot.getValue().toString();
                                                            final float updateStorage = Float.parseFloat(productStorage) + entry.getValue();
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                    .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    hideProgressDialog();
                                                                    Toast.makeText(getApplicationContext(),"Nhập hàng thành công!",Toast.LENGTH_LONG).show();
                                                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                }
                                                            });


                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                }else{
                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            final String productStorage = dataSnapshot.getValue().toString();
                                                            final float updateStorage = Float.parseFloat(productStorage) + entry.getValue();
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                    .setValue(updateStorage+"");


                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }else{

                                productStock.put(productName,productQuantity);
                                Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("Promotion")){
                                            Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    final Iterable<DataSnapshot> promotionSnap = dataSnapshot.getChildren();
                                                    long itemCount = dataSnapshot.getChildrenCount();

                                                    int i = 0;
                                                    for(DataSnapshot promotionItem:promotionSnap){
                                                        i++;
                                                        Product promotion = promotionItem.getValue(Product.class);
                                                        assert promotion != null;
                                                        final String promotionName = promotion.getProductName();
                                                        final float promotionQuantity = Float.parseFloat(promotion.getUnitQuantity());

                                                        if(i==itemCount){

                                                            if(productStock.containsKey(promotionName)){
                                                                float currentValue = productStock.get(promotionName);
                                                                float updateValue = currentValue + promotionQuantity;
                                                                productStock.put(promotionName,updateValue);

                                                                int y = 0;
                                                                for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                                    y++;
                                                                    int mapItemCount = productStock.size();

                                                                    if(y == mapItemCount){
                                                                        final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                                        WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(), entry.getValue()+"", timeStamp);
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(warehouseout);

                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                final String productStorage = dataSnapshot.getValue().toString();
                                                                                final float updateStorage = Float.parseFloat(productStorage) + entry.getValue();
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                        .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        hideProgressDialog();
                                                                                        Toast.makeText(getApplicationContext(),"Nhập hàng thành công!",Toast.LENGTH_LONG).show();
                                                                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                                    }
                                                                                });


                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    }else{
                                                                        final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                                        WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(), entry.getValue()+"", timeStamp);
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(warehouseout);

                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                final String productStorage = dataSnapshot.getValue().toString();
                                                                                final float updateStorage = Float.parseFloat(productStorage) + entry.getValue();
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                        .setValue(updateStorage+"");


                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    }
                                                                }

                                                            }else{
                                                                productStock.put(promotionName,promotionQuantity);

                                                                int y = 0;
                                                                for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                                    y++;
                                                                    int mapItemCount = productStock.size();

                                                                    if(y == mapItemCount){
                                                                        final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                                        WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(), entry.getValue()+"", timeStamp);
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(warehouseout);

                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                final String productStorage = dataSnapshot.getValue().toString();
                                                                                final float updateStorage = Float.parseFloat(productStorage) + entry.getValue();
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                        .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        hideProgressDialog();
                                                                                        Toast.makeText(getApplicationContext(),"Nhập hàng thành công!",Toast.LENGTH_LONG).show();
                                                                                        startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                                    }
                                                                                });


                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    }else{
                                                                        final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                                        WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(), entry.getValue()+"", timeStamp);
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(warehouseout);

                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                final String productStorage = dataSnapshot.getValue().toString();
                                                                                final float updateStorage = Float.parseFloat(productStorage) + entry.getValue();
                                                                                Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                        .setValue(updateStorage+"");


                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    }
                                                                }

                                                            }

                                                        }else{
                                                            if(productStock.containsKey(promotionName)){
                                                                float currentValue = productStock.get(promotionName);
                                                                float updateValue = currentValue + promotionQuantity;
                                                                productStock.put(promotionName,updateValue);
                                                            }else
                                                                productStock.put(promotionName,promotionQuantity);


                                                        }
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }else{
                                            int y = 0;
                                            for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                y++;
                                                int mapItemCount = productStock.size();

                                                if(y == mapItemCount){

                                                    final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                    final WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(),entry.getValue()+"",timeStamp);
                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(product);

                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            final String productStorage = dataSnapshot.getValue().toString();
                                                            final float updateStorage = Float.parseFloat(productStorage) + entry.getValue();
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                    .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    hideProgressDialog();
                                                                    Toast.makeText(getApplicationContext(),"Nhập hàng thành công!",Toast.LENGTH_LONG).show();
                                                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                }
                                                            });


                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                }else{
                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            final String productStorage = dataSnapshot.getValue().toString();
                                                            final float updateStorage = Float.parseFloat(productStorage) + entry.getValue();
                                                            Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                    .setValue(updateStorage+"");


                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                    final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                    final WarehouseIn warehouseout = new WarehouseIn(clientName,entry.getKey(),entry.getValue()+"",timeStamp);
                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("Out").child(timeStamp).setValue(warehouseout);
                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductOut").child(entry.getKey()).child(timeStamp).setValue(product);
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }else{

                            if(productStock.containsKey(productName)){
                                float currentValue = productStock.get(productName);
                                float updateValue = currentValue + productQuantity;
                                productStock.put(productName,updateValue);

                            }else{
                                productStock.put(productName,productQuantity);
                            }

                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        if(id == R.id.action_approve_cash_return){
            AlertDialog.Builder builder= new AlertDialog.Builder(ViewOrderDetailActivity.this);
            builder.setMessage("Xác nhận đơn hàng đã thu tiền?");
            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                            Constants.refDatabase.child(emailLogin+"/Order").child("Cash").child(orderPushKey).setValue(null);
                            Constants.refDatabase.child(emailLogin+"/Order").child("Money").child(orderPushKey).setValue(orderDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(getApplicationContext(),"Xác nhận đã thu thành công",Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }).show();
        }
        if(id == R.id.action_approve_update_sys){

            AlertDialog.Builder builder= new AlertDialog.Builder(ViewOrderDetailActivity.this);
            builder.setMessage("Cập nhật doanh số hệ thống?");
            builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                            Constants.refDatabase.child(emailLogin+"/Order").child("Sale").child(orderPushKey).setValue(orderDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Intent it = new Intent(getApplicationContext(),ApproveSaleActivity.class);
                                    it.putExtra("OrderPushKey",orderPushKey);
                                    it.putExtra("OrderType","Tiền Mặt");
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
            }).show();
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

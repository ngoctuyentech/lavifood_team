package a1a4w.onhandsme.pos;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Bill;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.Utils;

public class ViewBillDetailActivity extends AppCompatActivity {
    private TextView tvBillValue,tvCustomerCash,tvCashBack,tvShopName,tvEmployeeName,tvBillTime,tvBillCode;
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebase;
    private LinearLayoutManager linearLayoutManager;
    private String billPushKey,emailLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_bill_detail);

        Intent intent = this.getIntent();
        billPushKey = intent.getStringExtra("BillPushKey");
        emailLogin = intent.getStringExtra("EmailLogin");

        tvBillValue = (TextView)findViewById(R.id.tv_bill_detail_value);
        tvCustomerCash = (TextView)findViewById(R.id.tv_bill_detail_customerCash);
        tvCashBack = (TextView)findViewById(R.id.tv__bill_detail_cashBack);
        tvShopName = (TextView)findViewById(R.id.tv__bill_detail_shopName);
        tvEmployeeName = (TextView)findViewById(R.id.tv__bill_detail_employeeName);
        tvBillTime = (TextView)findViewById(R.id.tv__bill_detail_time);
        tvBillCode = (TextView)findViewById(R.id.tv__bill_detail_billCode);


        getGeneralInfo();

        initializeProductList();


    }

    private void getGeneralInfo() {
        Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("BillInfo").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Bill bill  = dataSnapshot.getValue(Bill.class);
                tvBillCode.setText(bill.getBillCode());
                tvBillValue.setText(Utils.convertNumber(bill.getPayment()));
                tvBillTime.setText(Utils.getDateCurrentTimeZone(Long.parseLong(bill.getTimeStamp())));
                tvCustomerCash.setText(Utils.convertNumber(bill.getCustomerCash()));
                tvCashBack.setText(Utils.convertNumber(bill.getCashBack()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                Constants.refDatabase.child(emailLogin).child("Z_POS_Shop").child(orderDetail.getShopCode()).child("shopName").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tvShopName.setText(dataSnapshot.getValue().toString());
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Constants.refDatabase.child(emailLogin).child("Z_POS_Employee").child(orderDetail.getEmployeeCode()).child("employeeName").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        tvEmployeeName.setText(dataSnapshot.getValue().toString());
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

    private void initializeProductList() {
        recyclerView = (RecyclerView)findViewById(R.id.recycler_bill_detail_productList);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                recyclerView.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });

        DatabaseReference refProductList = Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList");

        adapterFirebase = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product_bill,
                ProductViewHolder.class,
                refProductList
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_bill,parent,false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productTotal.setText(Utils.convertNumber(model.getProductTotal()));
                viewHolder.productQuantity.setText(model.getUnitQuantity());
            }
        };

        recyclerView.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productTotal, productQuantity;


        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView)itemView.findViewById(R.id.tv_item_product_bill_name);
            productQuantity = (TextView)itemView.findViewById(R.id.tv_item_product_bill_quantity);
            productTotal = (TextView) itemView.findViewById(R.id.tv_item_product_bill_total);

        }
    }

}

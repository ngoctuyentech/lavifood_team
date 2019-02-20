package a1a4w.onhandsme.pos;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.model.Shop;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.MySpinerAdapter;
import a1a4w.onhandsme.utils.Utils;

import java.util.Arrays;
import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;

public class StoreSaleActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<OrderDetail,OrderDetailViewHolder> adapterFirebase;
    private LinearLayoutManager linearLayoutManager;
    private TextView tvBillQuantity,tvWhatYear,tvWhatMonth,tvWhatDate,tvSaleYear,tvSaleMonth,tvSaleDate,tvStoreName,tvProductName;
    private String emailLogin,choosenYear,choosenMonth,choosenDate,shopCode,productName;
    private boolean timeSale,shopSale,productSale;
    private Spinner spinTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_sale);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_store_sale);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        choosenYear = intent.getStringExtra("ChoosenYear");
        choosenMonth = intent.getStringExtra("ChoosenMonth");
        choosenDate = intent.getStringExtra("ChoosenDate");
        shopCode = intent.getStringExtra("ShopCode");
        productName = intent.getStringExtra("ProductName");
        timeSale = intent.getBooleanExtra("TimeSale",false);
        shopSale = intent.getBooleanExtra("ShopSale",false);
        productSale = intent.getBooleanExtra("ProductSale",false);
        emailLogin = intent.getStringExtra("EmailLogin");

        tvBillQuantity = (TextView)findViewById(R.id.tv_store_sale_billTotal);
        tvWhatYear = (TextView)findViewById(R.id.tv_store_sale_whatYear);
        tvWhatMonth = (TextView)findViewById(R.id.tv_store_sale_whatMonth);
        tvWhatDate = (TextView)findViewById(R.id.tv_store_sale_whatDate);
        tvSaleYear = (TextView)findViewById(R.id.tv_store_sale_yearSale);
        tvSaleMonth = (TextView)findViewById(R.id.tv_store_sale_saleMonth);
        tvSaleDate = (TextView)findViewById(R.id.tv_store_sale_saleDate);
        tvStoreName = (TextView)findViewById(R.id.tv_store_sale_productName);
        tvProductName = (TextView)findViewById(R.id.tv_store_sale_shopName);
        spinTime = (Spinner)findViewById(R.id.spin_store_sale_time);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_store_sale_billList);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        String[] arrayTime = this.getResources().getStringArray(R.array.time_array);
        List<String> arrayListTime = Arrays.asList(arrayTime);
        MySpinerAdapter spinerAdapterTime = new MySpinerAdapter();
        spinerAdapterTime.addItems(arrayListTime);
        MaterialSpinner spinnerTime = (MaterialSpinner)findViewById(R.id.spin_store_sale_time);
        spinnerTime.setAdapter(spinerAdapterTime);
        spinnerTime.setSelection(2);

        spinTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String choosenTime = (String) parent.getItemAtPosition(position);
                switch (choosenTime) {
                    case "Năm":
                        if (timeSale) {
                            billList(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_BillByTime").child(choosenYear));
                        } else if (shopSale) {
                            billList(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreOrder").child(shopCode).child(choosenYear));
                        } else {
                            billList(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductOrder").child(productName).child(choosenYear));
                        }
                        break;
                    case "Tháng":
                        if (timeSale) {
                            billList(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_BillByTime").child(choosenYear + choosenMonth));
                        } else if (shopSale) {
                            billList(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreOrder").child(shopCode).child(choosenYear + choosenMonth));
                        } else {
                            billList(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductOrder").child(productName).child(choosenYear + choosenMonth));
                        }
                        break;
                    default:
                        if (timeSale) {
                            billList(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_BillByTime").child(choosenYear + choosenMonth + choosenDate));
                        } else if (shopSale) {
                            billList(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreOrder").child(shopCode).child(choosenYear + choosenMonth + choosenDate));
                        } else {
                            billList(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductOrder").child(productName).child(choosenYear + choosenMonth + choosenDate));
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(timeSale){
            //billList(Constants.refBillByTime.child(choosenYear+choosenMonth+choosenDate));
            setSale(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_BillTotalByTime"));
        }else if(shopSale){
            //Toast.makeText(getApplicationContext(),"ShopSale",Toast.LENGTH_LONG).show();
            Constants.refDatabase.child(emailLogin).child("Z_POS_Shop").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Shop shop = dataSnapshot.getValue(Shop.class);
                    String shopName = shop.getShopName();
                    tvStoreName.setText(shopName);
                    tvStoreName.setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
           // billList(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreOrder").child(shopCode).child(choosenYear+choosenMonth+choosenDate));
            setSale(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreSale").child(shopCode));
        }else {
            tvProductName.setText(productName);
            tvProductName.setVisibility(View.VISIBLE);
          //  billList(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductOrder").child(productName).child(choosenYear+choosenMonth+choosenDate));
            setSale(Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName));
        }

    }

    private void setSale(final DatabaseReference ref) {
        tvWhatYear.setVisibility(View.VISIBLE);
        tvWhatMonth.setVisibility(View.VISIBLE);
        tvWhatDate.setVisibility(View.VISIBLE);

        tvWhatYear.setText(choosenYear);
        tvWhatMonth.setText(choosenMonth);
        tvWhatDate.setText(choosenDate);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(choosenYear)){
                    ref.child(choosenYear).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            tvSaleYear.setText(Utils.convertNumber(dataSnapshot.getValue().toString()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                if(dataSnapshot.hasChild(choosenYear+choosenMonth)){
                    ref.child(choosenYear+choosenMonth).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            tvSaleMonth.setText(Utils.convertNumber(dataSnapshot.getValue().toString()));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }

                if(dataSnapshot.hasChild(choosenYear+choosenMonth+choosenDate)){
                    ref.child(choosenYear+choosenMonth+choosenDate).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            tvSaleDate.setText(Utils.convertNumber(dataSnapshot.getValue().toString()));
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

    private void billList(DatabaseReference ref) {

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

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long itemCount = dataSnapshot.getChildrenCount();
                tvBillQuantity.setText(itemCount+"");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        adapterFirebase = new FirebaseRecyclerAdapter<OrderDetail,OrderDetailViewHolder>(
                OrderDetail.class,
                R.id.item_bill_info,
                OrderDetailViewHolder.class,
                ref
        ) {
            @Override
            public OrderDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill_info,parent,false);
                return new OrderDetailViewHolder(v);
            }


            @Override
            protected void populateViewHolder(OrderDetailViewHolder viewHolder, OrderDetail model, int position) {
                viewHolder.billCode.setText(model.getBillCode());
                viewHolder.billPayment.setText(Utils.convertNumber(model.getClientPayment()));
            }
        };

        recyclerView.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();



    }


    private class OrderDetailViewHolder extends RecyclerView.ViewHolder {
        TextView billCode, billPayment;

        OrderDetailViewHolder(View itemView) {
            super(itemView);
            billCode = (TextView)itemView.findViewById(R.id.tv_item_bill_info_billCode);
            billPayment = (TextView) itemView.findViewById(R.id.tv_item_bill_info_payment);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    String itemKey = adapterFirebase.getRef(position).getKey();
                    Intent it = new Intent(getApplicationContext(),ViewBillDetailActivity.class);
                    it.putExtra("BillPushKey",itemKey);
                    startActivity(it);
                }
            });

        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,ShopChainActivity.class));
        super.onBackPressed();
    }
}

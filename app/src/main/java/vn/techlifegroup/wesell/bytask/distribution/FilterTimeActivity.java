package vn.techlifegroup.wesell.bytask.distribution;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.order.ViewOrderDetailActivity;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

public class FilterTimeActivity extends AppCompatActivity {
    private String choosenYear, choosenMonth,choosenQuarter,launchFrom,emailLogin;
    private int choosenYearInt, choosenMonthInt, choosenQuarterInt;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolder> adapterFirebase;
    private DatabaseReference refOrder;
    private TextView thisMonthRev, thisYearRev, thisQuarterRev,orderCount,whatMonth,whatQuarter,whatYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_order);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_filter_order);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();
        launchFrom = intent.getStringExtra("LaunchFrom");
        emailLogin = intent.getStringExtra("EmailLogin");

        //Toast.makeText(this,launchFrom,Toast.LENGTH_SHORT).show();
        choosenYear = intent.getStringExtra("ChoosenYear");
        choosenMonth = intent.getStringExtra("ChoosenMonth");
        choosenMonthInt = Integer.parseInt(choosenMonth);
        choosenYearInt = Integer.parseInt(choosenYear);
        if(choosenMonthInt == 1 || choosenMonthInt == 2 || choosenMonthInt == 3 ) {
            choosenQuarterInt = 1;
            choosenQuarter = choosenQuarterInt+"";
        }else if(choosenMonthInt == 4 || choosenMonthInt == 5 || choosenMonthInt == 6) {
            choosenQuarterInt = 2;
            choosenQuarter = choosenQuarterInt+"";

        }else if(choosenMonthInt == 7 || choosenMonthInt == 8 || choosenMonthInt == 9){
            choosenQuarterInt = 3;
            choosenQuarter = choosenQuarterInt+"";

        }else{
            choosenQuarterInt = 4;
            choosenQuarter = choosenQuarterInt+"";

        }

        thisMonthRev = (TextView)findViewById(R.id.tv_filter_this_month_client);
        thisYearRev = (TextView)findViewById(R.id.tv_filter_this_quarter_client);
        thisQuarterRev = (TextView)findViewById(R.id.tv_filter_this_quarter);
        orderCount = (TextView)findViewById(R.id.tv_filter_order_count);
        whatMonth = (TextView)findViewById(R.id.tv_filter_order_what_month);
        whatQuarter = (TextView)findViewById(R.id.tv_filter_order_what_quarter);
        whatYear = (TextView)findViewById(R.id.tv_filter_order_what_year);

        whatMonth.setText(choosenMonth);
        whatQuarter.setText(choosenQuarter);
        whatYear.setText(choosenYear);

        Constants.refDatabase.child(emailLogin).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("TotalByTime")){
                    Constants.refDatabase.child(emailLogin).child("TotalByTime").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(choosenYear)){
                                Toast.makeText(getApplicationContext(),choosenYear,Toast.LENGTH_LONG).show();

                                Constants.refDatabase.child(emailLogin).child("TotalByTime").child(choosenYear).child(choosenYear).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String thisYearRevenue = dataSnapshot.getValue().toString();
                                        thisYearRev.setText(Utils.convertNumber(thisYearRevenue));
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                Constants.refDatabase.child(emailLogin).child("TotalByTime").child(choosenYear).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(choosenYear+choosenQuarter)){
                                            Constants.refDatabase.child(emailLogin + "/TotalByTime").child(choosenYear).child(choosenYear+choosenQuarter).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    String thisQuarterRevenue = dataSnapshot.getValue().toString();
                                                    thisQuarterRev.setText(Utils.convertNumber(thisQuarterRevenue));
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

                                Constants.refDatabase.child(emailLogin).child("TotalByTime").child(choosenYear).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(choosenYear+choosenQuarter+choosenMonth)){
                                            Constants.refDatabase.child(emailLogin).child("TotalByTime").child(choosenYear).child(choosenYear+choosenQuarter+choosenMonth).addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    String thisMonthRevenue = dataSnapshot.getValue().toString();
                                                    thisMonthRev.setText(Utils.convertNumber(thisMonthRevenue));
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

        recyclerView = (RecyclerView)findViewById(R.id.filter_recycler_order_list);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        refOrder = Constants.refDatabase.child(emailLogin + "/TimeOrder").child(choosenYear+choosenQuarter+choosenMonth);

        refOrder.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long snapOrderCount = dataSnapshot.getChildrenCount();
                orderCount.setText(snapOrderCount+"");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapterFirebase = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolder>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolder.class,
                refOrder
        ) {
            @Override
            public OrderViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order,parent,false);
                return new OrderViewHolder(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolder viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerView.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();


    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderName;
        ImageButton orderStatus;

        public OrderViewHolder(View itemView) {
            super(itemView);
            orderName = (TextView)itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebase.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    Intent intent1 = new Intent(getApplicationContext(),ViewOrderDetailActivity.class);
                    intent1.putExtra("EmailLogin",emailLogin);
                    intent1.putExtra("OrderPushKey",itemKeyString);
                    startActivity(intent1);
                }
            });

        }
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter_time,menu);
        return super.onCreateOptionsMenu(menu);
    }


@Override
    public void onBackPressed() {
        Toast.makeText(this,"Back",Toast.LENGTH_LONG).show();
        startActivity(new Intent(this,launchFrom.getClass()));
        super.onBackPressed();
    }
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            this.onBackPressed();        }
        return super.onOptionsItemSelected(item);
    }
}

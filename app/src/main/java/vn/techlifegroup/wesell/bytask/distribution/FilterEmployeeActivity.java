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

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Objects;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.order.ViewOrderDetailActivity;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class FilterEmployeeActivity extends AppCompatActivity {

    private String choosenYear, choosenMonth,choosenQuarter,emailLogin,saleEmail;
    private int choosenYearInt, choosenMonthInt, choosenQuarterInt;
    private String employeeName;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolder> adapterFirebase;
    private DatabaseReference refEmployeeOrder;
    private TextView thisMonthRev, thisYearRev, thisQuarterRev,orderCount,whatMonth,whatQuarter,whatYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_employee);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_filter_employee);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();
        employeeName = intent.getStringExtra("EmployeeName");
        choosenYear = intent.getStringExtra("ChoosenYear");
        choosenMonth = intent.getStringExtra("ChoosenMonth");
        emailLogin = intent.getStringExtra("EmailLogin");
        saleEmail = intent.getStringExtra("SaleEmail");

        if(choosenMonth.equals("1") || choosenMonth.equals("2") || choosenMonth.equals("3")){
            choosenQuarter = "1";
        }else if (choosenMonth.equals("4") || choosenMonth.equals("5") || choosenMonth.equals("6")){
            choosenQuarter = "2";
        }else if (choosenMonth.equals("7") || choosenMonth.equals("8") || choosenMonth.equals("9")){
            choosenQuarter = "3";
        }else{
            choosenQuarter = "4";
        }

        initializeScreen();

        thisMonthRev = (TextView)findViewById(R.id.tv_filter_this_month_employee);
        thisYearRev = (TextView)findViewById(R.id.tv_filter_this_year_employee);
        thisQuarterRev = (TextView)findViewById(R.id.tv_filter_this_quarter_employee);
        orderCount =  (TextView)findViewById(R.id.tv_filter_order_count_employee);
        whatMonth = (TextView)findViewById(R.id.tv_filter_employee_what_month);
        whatQuarter = (TextView)findViewById(R.id.tv_filter_employee_what_quarter);
        whatYear = (TextView)findViewById(R.id.tv_filter_employee_what_year);

        whatMonth.setText(choosenMonth);
        whatQuarter.setText(choosenQuarter);
        whatYear.setText(choosenYear);

        refDatabase.child(emailLogin+"/TotalByEmployee").child(saleEmail).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(choosenYear)){
                    refDatabase.child(emailLogin).child("TotalByEmployee").child(saleEmail).child(choosenYear).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String thisYearRevenue = dataSnapshot.getValue().toString();
                            thisYearRev.setText(Utils.convertNumber(thisYearRevenue));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

                if(dataSnapshot.hasChild(choosenYear+choosenQuarter)){
                    refDatabase.child(emailLogin).child("TotalByEmployee").child(saleEmail).child(choosenYear+choosenQuarter).addListenerForSingleValueEvent(new ValueEventListener() {
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
                if(dataSnapshot.hasChild(choosenYear+choosenQuarter+choosenMonth)){
                    refDatabase.child(emailLogin).child("TotalByEmployee").child(saleEmail).child(choosenYear+choosenQuarter+choosenMonth).addListenerForSingleValueEvent(new ValueEventListener() {
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

    private void initializeScreen() {
        recyclerView = (RecyclerView)findViewById(R.id.filter_recycler_order_list_employee);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        refEmployeeOrder = refDatabase.child(emailLogin).child("EmployeeOrder").child(saleEmail).child(choosenYear+choosenQuarter+choosenMonth);

        refEmployeeOrder.addListenerForSingleValueEvent(new ValueEventListener() {
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
                refEmployeeOrder
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
                    intent1.putExtra("OrderPushKey",itemKeyString);
                    intent1.putExtra("EmailLogin",emailLogin);
                    startActivity(intent1);
                }
            });

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}


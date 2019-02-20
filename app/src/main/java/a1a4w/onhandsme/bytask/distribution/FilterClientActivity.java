package a1a4w.onhandsme.bytask.distribution;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
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
import java.util.Objects;

import a1a4w.onhandsme.MainActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.bytask.OrderManActivity;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.order.ViewOrderDetailActivity;
import a1a4w.onhandsme.utils.Constants;

import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class FilterClientActivity extends AppCompatActivity {
    private String choosenYear, choosenMonth,choosenQuarter,emailLogin;
    private int choosenYearInt, choosenMonthInt, choosenQuarterInt;
    private String clientCode;
    private RecyclerView recyclerView,clientList;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolder> adapterFirebase;
    private DatabaseReference refClientOrder;
    private TextView thisMonthRev, thisYearRev, thisQuarterRev,orderCount,whatMonth,whatQuarter,whatYear;
    private FirebaseRecyclerAdapter<Client, ClientViewHolder> adapterFirebaseClientList;
    private Bundle b = new Bundle();
    private boolean saleMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filter_client);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_filter_client);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();
        clientCode = intent.getStringExtra("ClientCode");
        choosenYear = intent.getStringExtra("ChoosenYear");
        choosenMonth = intent.getStringExtra("ChoosenMonth");
        emailLogin = intent.getStringExtra("EmailLogin");
        saleMan = intent.getBooleanExtra("SaleMan",false);


        thisMonthRev = (TextView)findViewById(R.id.tv_filter_this_month_client);
        thisYearRev = (TextView)findViewById(R.id.tv_filter_this_year_client);
        thisQuarterRev = (TextView)findViewById(R.id.tv_filter_this_quarter_client);
        orderCount =  (TextView)findViewById(R.id.tv_filter_order_count_client);
        whatMonth = (TextView)findViewById(R.id.tv_filter_client_what_month);
        whatQuarter = (TextView)findViewById(R.id.tv_filter_client_what_quarter);
        whatYear = (TextView)findViewById(R.id.tv_filter_client_what_year);



        if(choosenMonth.equals("1") || choosenMonth.equals("2") || choosenMonth.equals("3")){
            choosenQuarter = "1";
        }else if (choosenMonth.equals("4") || choosenMonth.equals("5") || choosenMonth.equals("6")){
            choosenQuarter = "2";
        }else if (choosenMonth.equals("7") || choosenMonth.equals("8") || choosenMonth.equals("9")){
            choosenQuarter = "3";
        }else{
            choosenQuarter = "4";
        }

        whatMonth.setText(choosenMonth);
        whatQuarter.setText(choosenQuarter);
        whatYear.setText(choosenYear);

        refDatabase.child(emailLogin+"/TotalByClient").child(clientCode).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(choosenYear)){
                    refDatabase.child(emailLogin+"/TotalByClient").child(clientCode).child(choosenYear).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String thisYearRevenue = dataSnapshot.getValue().toString();
                            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
                            float thisYearRevenueFloat = Float.parseFloat(thisYearRevenue);
                            String thisYearRevenueFloatConverted = numberFormat.format(thisYearRevenueFloat);
                            thisYearRev.setText(thisYearRevenueFloatConverted);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }

                if(dataSnapshot.hasChild(choosenYear+choosenQuarter)){
                    refDatabase.child(emailLogin+"/TotalByClient").child(clientCode).child(choosenYear+choosenQuarter).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String thisQuarterRevenue = dataSnapshot.getValue().toString();

                            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
                            float thisQuarterRevenueFloat = Float.parseFloat(thisQuarterRevenue);
                            String thisQuarterRevenueFloatConverted = numberFormat.format(thisQuarterRevenueFloat);
                            thisQuarterRev.setText(thisQuarterRevenueFloatConverted);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                if(dataSnapshot.hasChild(choosenYear+choosenQuarter+choosenMonth)){
                    refDatabase.child(emailLogin+"/TotalByClient").child(clientCode).child(choosenYear+choosenQuarter+choosenMonth).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String thisMonthRevenue = dataSnapshot.getValue().toString();

                            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
                            float thisMonthRevenueFloat = Float.parseFloat(thisMonthRevenue);
                            String thisMonthRevenueFloatConverted = numberFormat.format(thisMonthRevenueFloat);
                            thisMonthRev.setText(thisMonthRevenueFloatConverted);
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

        initializeScreen();

    }

    private void initializeScreen() {
        recyclerView = (RecyclerView)findViewById(R.id.filter_recycler_order_list_client);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);


        refClientOrder = refDatabase.child(emailLogin + "/ClientOrder").child(clientCode).child(choosenYear+choosenQuarter+choosenMonth);

        refClientOrder.addListenerForSingleValueEvent(new ValueEventListener() {
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
                refClientOrder
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

    private class OrderViewHolder extends RecyclerView.ViewHolder {
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

    private class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView clientName;

        public ClientViewHolder(View itemView) {
            super(itemView);
            clientName = (TextView) itemView.findViewById(R.id.tv_item_client_sale_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    String keyRef = adapterFirebaseClientList.getRef(position).getKey();

                    Intent it = new Intent(getApplicationContext(), FilterClientActivity.class);
                    it.putExtra("ClientCode", keyRef);
                    it.putExtra("ChoosenYear", choosenYear);
                    it.putExtra("ChoosenMonth", choosenMonth);
                    startActivity(it);


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

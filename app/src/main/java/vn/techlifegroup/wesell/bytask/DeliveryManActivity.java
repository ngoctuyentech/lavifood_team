package vn.techlifegroup.wesell.bytask;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
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

import java.util.Calendar;

import vn.techlifegroup.wesell.LoginActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Client;
import vn.techlifegroup.wesell.model.DebtHistory;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.model.VatModel;
import vn.techlifegroup.wesell.order.ViewOrderDetailActivity;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class DeliveryManActivity extends AppCompatActivity {

    private RecyclerView recyclerViewApproved,recyclerViewPaid,recyclerViewWarehouseout;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolderApproved> adapterFirebaseApproved;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolderPaid> adapterFirebasePaid;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolderWarehouseout> adapterFirebaseWarehouseout;

    private LinearLayoutManager linearLayoutManager;
    private LinearLayout layoutApproved, layoutPaid, layoutWarehouseout;
    private DatabaseReference refApproved,refPaid,refWarehouseout;
    private TextView tvApproved, tvPaid,tvWarehouseout;
    private Bundle b = new Bundle();
    private String emailLogin;
    private String thisYear, thisMonth, thisDate;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_man);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_delivery);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");


        tvApproved = (TextView) findViewById(R.id.tv_delivery_approved);


        tvPaid = (TextView) findViewById(R.id.tv_delivery_paid);
        tvWarehouseout = (TextView) findViewById(R.id.tv_delivery_warehouseout);
        layoutApproved = (LinearLayout)findViewById(R.id.layout_delivery_approved);
        layoutPaid = (LinearLayout)findViewById(R.id.layout_delivery_paid);
        layoutWarehouseout = (LinearLayout)findViewById(R.id.layout_delivery_warehouseout);

        thisYear = (Calendar.getInstance().get(Calendar.YEAR)) + "";
        thisMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1) + "";
        thisDate = (Calendar.getInstance().get(Calendar.DATE)) + "";

        tvWarehouseout.setBackgroundColor(Color.WHITE);
        tvWarehouseout.setTextColor(getResources().getColor(R.color.colorAccent));

        initializeApproved();
        initializePaid();
        initializeWarehouseout();

        tvApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                tvApproved.setBackgroundColor(Color.WHITE);
                tvPaid.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                tvWarehouseout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                layoutApproved.setVisibility(View.VISIBLE);
                layoutPaid.setVisibility(View.INVISIBLE);
                layoutWarehouseout.setVisibility(View.INVISIBLE);

            }
        });

        tvPaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                tvPaid.setBackgroundColor(Color.WHITE);
                tvApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                tvWarehouseout.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                layoutPaid.setVisibility(View.VISIBLE);
                layoutApproved.setVisibility(View.INVISIBLE);
                layoutWarehouseout.setVisibility(View.INVISIBLE);

                tvPaid.setTextColor(getResources().getColor(R.color.colorAccent));
                tvWarehouseout.setTextColor(getResources().getColor(android.R.color.white));

            }
        });

        tvWarehouseout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                tvWarehouseout.setBackgroundColor(Color.WHITE);
                tvApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                tvPaid.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                layoutWarehouseout.setVisibility(View.VISIBLE);
                layoutPaid.setVisibility(View.INVISIBLE);
                layoutApproved.setVisibility(View.INVISIBLE);

                tvWarehouseout.setTextColor(getResources().getColor(R.color.colorAccent));
                tvPaid.setTextColor(getResources().getColor(android.R.color.white));

            }
        });

    }

    private void initializeWarehouseout() {
        recyclerViewWarehouseout = (RecyclerView)findViewById(R.id.recyclerview_delivery_warehouseout);
        recyclerViewWarehouseout.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewWarehouseout.setLayoutManager(linearLayoutManager);

        refWarehouseout = refDatabase.child(emailLogin).child("Order").child("WarehouseOut");

        adapterFirebaseWarehouseout = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderWarehouseout>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderWarehouseout.class,
                refWarehouseout
        ) {
            @Override
            public OrderViewHolderWarehouseout onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order,parent,false);
                return new OrderViewHolderWarehouseout(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolderWarehouseout viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerViewWarehouseout.setAdapter(adapterFirebaseWarehouseout);
        adapterFirebaseWarehouseout.notifyDataSetChanged();
    }

    private void initializePaid() {
        recyclerViewPaid = (RecyclerView)findViewById(R.id.recyclerview_delivery_paid);
        recyclerViewPaid.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewPaid.setLayoutManager(linearLayoutManager);


        refPaid = refDatabase.child(emailLogin).child("Order").child("Money");

        adapterFirebasePaid = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderPaid>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderPaid.class,
                refPaid
        ) {
            @Override
            public OrderViewHolderPaid onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order,parent,false);
                return new OrderViewHolderPaid(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolderPaid viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerViewPaid.setAdapter(adapterFirebasePaid);
        adapterFirebasePaid.notifyDataSetChanged();
    }

    private void initializeApproved() {

        recyclerViewApproved = (RecyclerView)findViewById(R.id.recyclerview_delivery_approved);
        recyclerViewApproved.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewApproved.setLayoutManager(linearLayoutManager);

        refApproved = refDatabase.child(emailLogin).child("Order").child("Approved");

        adapterFirebaseApproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderApproved>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderApproved.class,
                refApproved
        ) {
            @Override
            public OrderViewHolderApproved onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order,parent,false);
                return new OrderViewHolderApproved(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolderApproved viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerViewApproved.setAdapter(adapterFirebaseApproved);
        adapterFirebaseApproved.notifyDataSetChanged();
    }

    public class OrderViewHolderApproved extends RecyclerView.ViewHolder {
        TextView orderName;
        ImageButton orderStatus;

        public OrderViewHolderApproved(View itemView) {
            super(itemView);
            orderName = (TextView)itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseApproved.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    Intent intent1 = new Intent(getApplicationContext(),ViewOrderDetailActivity.class);
                    intent1.putExtra("OrderPushKey",itemKeyString);
                    intent1.putExtra("EmailLogin",emailLogin);
                    startActivity(intent1);
                }
            });
        }
    }

    public class OrderViewHolderPaid extends RecyclerView.ViewHolder {
        TextView orderName;

        public OrderViewHolderPaid(View itemView) {
            super(itemView);
            orderName = (TextView)itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebasePaid.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    Intent intent1 = new Intent(getApplicationContext(),ViewOrderDetailActivity.class);
                    intent1.putExtra("OrderPushKey",itemKeyString);
                    startActivity(intent1);
                }
            });
        }
    }

    public class OrderViewHolderWarehouseout extends RecyclerView.ViewHolder {
        TextView orderName;

        public OrderViewHolderWarehouseout(View itemView) {
            super(itemView);
            orderName = (TextView)itemView.findViewById(R.id.tv_order_name);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseWarehouseout.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    Intent intent1 = new Intent(getApplicationContext(),ViewOrderDetailActivity.class);
                    intent1.putExtra("OrderPushKey",itemKeyString);
                    intent1.putExtra("Delivery",true);
                    intent1.putExtra("EmailLogin",emailLogin);
                    startActivity(intent1);
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_delivery,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_logout_delivery){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}

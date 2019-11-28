package vn.techlifegroup.wesell.bytask.debt;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.order.ViewOrderDetailActivity;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class ViewDebtOrderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<OrderDetail, OrderViewHolder> adapterFirebase;
    private String emailLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_debt_order);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_view_debt_order);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

        getDebtOrderList();
    }

    private void getDebtOrderList() {
        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_debt_order);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);


        DatabaseReference refDebtOrder = refDatabase.child(emailLogin+"/Order/Debt");

        adapterFirebase = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolder>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolder.class,
                refDebtOrder
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

        public OrderViewHolder(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebase.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    Intent intent1 = new Intent(getApplicationContext(), ViewOrderDetailActivity.class);
                    intent1.putExtra("OrderPushKey", itemKeyString);
                    intent1.putExtra("EmailLogin", emailLogin);
                    startActivity(intent1);
                }
            });

        }
    }

}

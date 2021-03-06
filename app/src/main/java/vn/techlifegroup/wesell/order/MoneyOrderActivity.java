package vn.techlifegroup.wesell.order;

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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.bytask.debt.DebtManActivity;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

public class MoneyOrderActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolder> adapterFirebase;
    private DatabaseReference refMoney;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_money_order);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_money_order);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_money_order);
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

        refMoney = Constants.refOrder.child("Money");

        adapterFirebase = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolder>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolder.class,
                refMoney
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
            orderName = (TextView)itemView.findViewById(R.id.tv_order_name);

            /*
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    AlertDialog.Builder builder= new AlertDialog.Builder(DebtOrderActivity.this);
                    builder.setMessage("Xác nhập đơn hàng đã nộp tiền đủ?");
                    builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = getLayoutPosition();
                            final DatabaseReference itemKey = adapterFirebase.getRef(position);
                            final String itemKeyString = itemKey.getKey();
                            Constants.refOrderList.child(itemKeyString).child("OtherInformation").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                                    Constants.refOrder.child("GotMoney").child(itemKeyString).setValue(orderDetail);
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
                    return false;
                }
            });
             */



            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebase.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    Intent intent1 = new Intent(getApplicationContext(),ViewOrderDetailActivity.class);
                    intent1.putExtra("OrderPushKey",itemKeyString);
                    startActivity(intent1);
                }
            });

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_money_order,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_money_order_home){
            startActivity(new Intent(getApplicationContext(), DebtManActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}


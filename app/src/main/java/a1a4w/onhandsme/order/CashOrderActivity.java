package a1a4w.onhandsme.order;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.bytask.debt.DebtManActivity;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.Utils;

public class CashOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolder> adapterFirebase;
    private DatabaseReference refCash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_admin);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_admin_payment);
        setSupportActionBar(toolbar);

        recyclerView = (RecyclerView)findViewById(R.id.recycler_payment_admin);
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

        refCash = Constants.refOrder.child("Cash");

        adapterFirebase = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolder>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolder.class,
                refCash
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

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    AlertDialog.Builder builder= new AlertDialog.Builder(CashOrderActivity.this);
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
        getMenuInflater().inflate(R.menu.menu_payment,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_payment_home){
            startActivity(new Intent(getApplicationContext(), DebtManActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}

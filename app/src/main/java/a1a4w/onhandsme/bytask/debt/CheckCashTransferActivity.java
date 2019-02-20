package a1a4w.onhandsme.bytask.debt;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.list.AddClientActivity;
import a1a4w.onhandsme.list.UpdateClientActivity;
import a1a4w.onhandsme.model.CashTransfer;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.order.UpdateOrderActivity;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.Utils;

public class CheckCashTransferActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<CashTransfer,CashTransferViewHolder> adapterFirebase;
    private Bundle b = new Bundle();
    private DatabaseReference refCashTransfer;
    private LinearLayoutManager linearLayoutManager;
    private String emailLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_cash_transfer);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

        recyclerView = (RecyclerView)findViewById(R.id.recyclerview_check_cash);
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

        refCashTransfer = Constants.refDatabase.child(emailLogin+"/Acccounting").child("CashTranfer");

        adapterFirebase = new FirebaseRecyclerAdapter<CashTransfer, CashTransferViewHolder>(
                CashTransfer.class,
                R.id.item_cash_transfer,
                CashTransferViewHolder.class,
                refCashTransfer
        ) {
            @Override
            public CashTransferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cash_transfer,parent,false);
                return new CashTransferViewHolder(v);
            }


            @Override
            protected void populateViewHolder(CashTransferViewHolder viewHolder, CashTransfer model, int position) {
                viewHolder.clientName.setText(model.getClientName());
                viewHolder.accountName.setText(model.getAccount());
                viewHolder.cashValue.setText(model.getCashValue());
                viewHolder.dateOfTransfer.setText(model.getDateOfTransfer());

            }
        };

        recyclerView.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();

    }



    public class CashTransferViewHolder extends RecyclerView.ViewHolder {
        TextView clientName,accountName, cashValue, dateOfTransfer;

        public CashTransferViewHolder(View itemView) {
            super(itemView);
            clientName = (TextView) itemView.findViewById(R.id.tv_item_cash_clientName);
            accountName = (TextView) itemView.findViewById(R.id.tv_item_cash_accountName);
            cashValue = (TextView) itemView.findViewById(R.id.tv_item_cash_value);
            dateOfTransfer = (TextView) itemView.findViewById(R.id.tv_item_cash_transferdate);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    String keyRef = adapterFirebase.getRef(position).getKey();

                    Constants.refDatabase.child(emailLogin+"/Client").child(keyRef).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Client client = dataSnapshot.getValue(Client.class);
                            Intent intent = new Intent(getApplicationContext(),UpdateOrderActivity.class);
                            intent.putExtra("ClientName", client.getClientName());
                            intent.putExtra("ClientCode", client.getClientCode());
                            intent.putExtra("EmailLogin", emailLogin);

                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    String keyRef = adapterFirebase.getRef(position).getKey();

                    Constants.refDatabase.child(emailLogin+"/Client").child(keyRef).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Client client = dataSnapshot.getValue(Client.class);
                            Intent intent = new Intent(getApplicationContext(),UpdateClientActivity.class);
                            intent.putExtra("ClientCode", client.getClientCode());
                            intent.putExtra("EmailLogin", emailLogin);
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    return false;
                }
            });

        }
    }


    private void createNewClientDialog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage("Tạo khách hàng mới?");

        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(getApplicationContext(), AddClientActivity.class));
            }
        });
        builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       // getMenuInflater().inflate(R.menu.menu_new_order,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /*
        int id = item.getItemId();

        if(id == R.id.action_new_order_home){
            startActivity(new Intent(getApplicationContext(), OrderManActivity.class));
        }
         */

        return super.onOptionsItemSelected(item);
    }
}

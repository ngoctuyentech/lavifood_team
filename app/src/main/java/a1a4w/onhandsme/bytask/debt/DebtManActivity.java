package a1a4w.onhandsme.bytask.debt;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

import java.util.Arrays;
import java.util.List;

import a1a4w.onhandsme.LoginActivity;
import a1a4w.onhandsme.MainActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.list.ClientListActivity;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.order.ViewOrderDetailActivity;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.MySpinerAdapter;
import a1a4w.onhandsme.utils.Utils;
import fr.ganfra.materialspinner.MaterialSpinner;

public class DebtManActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPayCheck, recyclerViewCash, recyclerLaterPay;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolderUnapproved> adapterFirebaseCash;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolderApproved> adapterFirebasePayCheck;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolderDenied> adapterFirebaseLaterPay;
    private LinearLayoutManager linearLayoutManager;
    private LinearLayout layoutPayCheck, layoutCash, layoutLaterPay;
    private LinearLayout boxApproved, boxUnApproved,boxCancelled;
    private DatabaseReference refPayCheck, refCash, refLaterPay;
    private TextView tvPayCheck, tvCash, tvLaterPay;
    private String userRole,emailLogin;
    private Bundle b= new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debt_man);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_debt);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        //userRole = intent.getStringExtra("UserRole");
        emailLogin = intent.getStringExtra("EmailLogin");

        tvCash = (TextView) findViewById(R.id.tv_debt_cash);
        tvPayCheck = (TextView) findViewById(R.id.tv_debt_paycheck);
        tvLaterPay = (TextView) findViewById(R.id.tv_debt_later);
        layoutPayCheck = (LinearLayout)findViewById(R.id.layout_approved_debt);
        layoutCash = (LinearLayout)findViewById(R.id.layout_unapproved_debt);
        layoutLaterPay = (LinearLayout)findViewById(R.id.layout_denied_debt);

        boxApproved = (LinearLayout)findViewById(R.id.debt_man_boxApproved);
        boxUnApproved = (LinearLayout)findViewById(R.id.debt_man_boxUnApproved);
        boxCancelled = (LinearLayout)findViewById(R.id.debt_man_boxCanceled);

        boxUnApproved.setBackgroundColor(Color.WHITE);
        layoutCash.setVisibility(View.VISIBLE);
        tvCash.setTextColor(getResources().getColor(R.color.colorAccent));


        initializeUnapproved();
        initializeApproved();
        initializeDenied();

        boxApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                boxApproved.setBackgroundColor(Color.WHITE);
                boxUnApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxCancelled.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                layoutPayCheck.setVisibility(View.VISIBLE);
                layoutCash.setVisibility(View.INVISIBLE);
                layoutLaterPay.setVisibility(View.INVISIBLE);

                tvPayCheck.setTextColor(getResources().getColor(R.color.colorAccent));
                tvCash.setTextColor(getResources().getColor(android.R.color.white));
                tvLaterPay.setTextColor(getResources().getColor(android.R.color.white));
            }
        });

        boxUnApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                boxUnApproved.setBackgroundColor(Color.WHITE);
                boxApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxCancelled.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                layoutPayCheck.setVisibility(View.INVISIBLE);
                layoutCash.setVisibility(View.VISIBLE);
                layoutLaterPay.setVisibility(View.INVISIBLE);

                tvCash.setTextColor(getResources().getColor(R.color.colorAccent));
                tvPayCheck.setTextColor(getResources().getColor(android.R.color.white));
                tvLaterPay.setTextColor(getResources().getColor(android.R.color.white));
            }
        });

        boxCancelled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                boxCancelled.setBackgroundColor(Color.WHITE);
                boxApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxUnApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                layoutPayCheck.setVisibility(View.INVISIBLE);
                layoutCash.setVisibility(View.INVISIBLE);
                layoutLaterPay.setVisibility(View.VISIBLE);

                tvLaterPay.setTextColor(getResources().getColor(R.color.colorAccent));
                tvCash.setTextColor(getResources().getColor(android.R.color.white));
                tvPayCheck.setTextColor(getResources().getColor(android.R.color.white));
            }
        });


    }

    private void initializeDenied() {
        recyclerLaterPay = (RecyclerView)findViewById(R.id.order_denied_recyclerview_debt);
        recyclerLaterPay.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerLaterPay.setLayoutManager(linearLayoutManager);

        getDeniedOrder();

    }

    private void initializeApproved() {
        recyclerViewPayCheck = (RecyclerView)findViewById(R.id.order_approved_recyclerview_debt);
        recyclerViewPayCheck.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewPayCheck.setLayoutManager(linearLayoutManager);

        getApprovedOrder();

    }

    private void initializeUnapproved() {
        recyclerViewCash = (RecyclerView)findViewById(R.id.order_unapproved_recyclerview_debt);
        recyclerViewCash.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewCash.setLayoutManager(linearLayoutManager);

        getUnApprovedOrder();

    }

    private void getApprovedOrder() {
        refPayCheck = Constants.refDatabase.child(emailLogin+"/Order").child("Money");

        adapterFirebasePayCheck = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderApproved>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderApproved.class,
                refPayCheck
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

        recyclerViewPayCheck.setAdapter(adapterFirebasePayCheck);
        adapterFirebasePayCheck.notifyDataSetChanged();
    }

    private void getUnApprovedOrder() {
        refCash = Constants.refDatabase.child(emailLogin+"/Order").child("Cash");

        adapterFirebaseCash = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderUnapproved>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderUnapproved.class,
                refCash
        ) {
            @Override
            public OrderViewHolderUnapproved onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order,parent,false);
                return new OrderViewHolderUnapproved(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolderUnapproved viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerViewCash.setAdapter(adapterFirebaseCash);
        adapterFirebaseCash.notifyDataSetChanged();
    }

    private void getDeniedOrder() {
        refLaterPay = Constants.refDatabase.child(emailLogin+"/Order").child("Debt");

        adapterFirebaseLaterPay = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderDenied>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderDenied.class,
                refLaterPay
        ) {
            @Override
            public OrderViewHolderDenied onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order,parent,false);
                return new OrderViewHolderDenied(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolderDenied viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerLaterPay.setAdapter(adapterFirebaseLaterPay);
        adapterFirebaseLaterPay.notifyDataSetChanged();
    }

    public class OrderViewHolderUnapproved extends RecyclerView.ViewHolder {
        TextView orderName;

        public OrderViewHolderUnapproved(View itemView) {
            super(itemView);
            orderName = (TextView)itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseCash.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    Intent intent1 = new Intent(getApplicationContext(),ViewOrderDetailActivity.class);
                    intent1.putExtra("OrderPushKey",itemKeyString);
                    intent1.putExtra("EmailLogin",emailLogin);
                    intent1.putExtra("CashReturn",true);
                    startActivity(intent1);
                }
            });

        }
    }

    public class OrderViewHolderApproved extends RecyclerView.ViewHolder {
        TextView orderName;

        public OrderViewHolderApproved(View itemView) {
            super(itemView);
            orderName = (TextView)itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebasePayCheck.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    Intent intent1 = new Intent(getApplicationContext(),ViewOrderDetailActivity.class);
                    intent1.putExtra("OrderPushKey",itemKeyString);
                    intent1.putExtra("EmailLogin",emailLogin);
                    intent1.putExtra("UpdateSale",true);
                    startActivity(intent1);
                }
            });

        }
    }

    public class OrderViewHolderDenied extends RecyclerView.ViewHolder {
        TextView orderName;

        public OrderViewHolderDenied(View itemView) {
            super(itemView);
            orderName = (TextView)itemView.findViewById(R.id.tv_order_name);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    AlertDialog.Builder builder= new AlertDialog.Builder(DebtManActivity.this);
                    builder.setMessage("Cập nhật doanh số hệ thống?");
                    builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            int position = getLayoutPosition();
                            final DatabaseReference itemKey = adapterFirebaseLaterPay.getRef(position);
                            final String itemKeyString = itemKey.getKey();
                            Constants.refDatabase.child(emailLogin+"/OrderList").child(itemKeyString).child("OtherInformation").addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                                    Constants.refDatabase.child(emailLogin+"/Order").child("Sale").child(itemKeyString).setValue(orderDetail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Intent it = new Intent(getApplicationContext(),ApproveSaleActivity.class);
                                            it.putExtra("OrderPushKey",itemKeyString);
                                            it.putExtra("OrderType","Công Nợ");
                                            it.putExtra("EmailLogin",emailLogin);
                                            startActivity(it);
                                        }
                                    });

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
                    DatabaseReference itemKey = adapterFirebaseLaterPay.getRef(position);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_debt,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_update_debt){
            Intent intent = new Intent(getApplicationContext(),ClientListActivity.class);
            intent.putExtra("UpdateDebt",true);
            intent.putExtra("EmailLogin",emailLogin);

            startActivity(intent);
        }
        if(id == R.id.action_debt_logout){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        if(id == R.id.action_view_debt_order){
            Intent intent = new Intent(getApplicationContext(),ViewDebtOrderActivity.class);
            intent.putExtra("UpdateDebt",true);
            intent.putExtra("EmailLogin",emailLogin);
            startActivity(intent);
        }

        if(id == R.id.action_view_debt_history){
            debtHistoryDialog();
        }


        return super.onOptionsItemSelected(item);
    }

    private void debtHistoryDialog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_filter_client, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Spinner spinYearFilter = (Spinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        Spinner spinMonthFilter = (Spinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Button btnOk = (Button)dialogView.findViewById(R.id.btn_dialog_filter_client_ok);

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        spinnerYear.setAdapter(spinerAdapterYear);
        spinnerYear.setSelection(0);

        spinYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String choosenYear = (String) parent.getItemAtPosition(position);
                b.putString("ChoosenYear",choosenYear);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayMonth = this.getResources().getStringArray(R.array.month_filter_array);
        List<String> arrayListMonth = Arrays.asList(arrayMonth);
        MySpinerAdapter spinerAdapterMonth = new MySpinerAdapter();
        spinerAdapterMonth.addItems(arrayListMonth);
        MaterialSpinner spinnerMonth = (MaterialSpinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        spinnerMonth.setAdapter(spinerAdapterMonth);
        spinnerMonth.setSelection(0);

        spinMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                final String choosenMonth = (String) parent.getItemAtPosition(position);
                b.putString("ChoosenMonth",choosenMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                String choosenYear = b.getString("ChoosenYear");
                String choosenMonth = b.getString("ChoosenMonth");

                if(choosenYear == null || choosenMonth==null){
                    Toast.makeText(getApplicationContext(),"Vui lòng đợi...",Toast.LENGTH_SHORT).show();
                }else{
                    v.startAnimation(Constants.buttonClick);
                    Intent it = new Intent(getApplicationContext(),ClientListActivity.class);
                    it.putExtra("ChoosenYear",choosenYear);
                    it.putExtra("ChoosenMonth",choosenMonth);
                    it.putExtra("DebtHistory",true);

                    startActivity(it);
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,MainActivity.class));
        super.onBackPressed();
    }
}

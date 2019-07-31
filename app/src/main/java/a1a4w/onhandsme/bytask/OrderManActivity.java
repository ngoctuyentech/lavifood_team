package a1a4w.onhandsme.bytask;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import a1a4w.onhandsme.MainActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.bytask.distribution.FilterClientActivity;
import a1a4w.onhandsme.bytask.distribution.FilterEmployeeActivity;
import a1a4w.onhandsme.bytask.distribution.FilterProductActivity;
import a1a4w.onhandsme.bytask.distribution.FilterTimeActivity;
import a1a4w.onhandsme.list.ClientListActivity;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.Employee;
import a1a4w.onhandsme.model.Group;
import a1a4w.onhandsme.model.MapModel;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.model.Promotion;
import a1a4w.onhandsme.model.VatModel;
import a1a4w.onhandsme.order.PreviewOrderActivivity;
import a1a4w.onhandsme.utils.AdapterOrder;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.MySpinerAdapter;
import fr.ganfra.materialspinner.MaterialSpinner;
import im.delight.android.location.SimpleLocation;

import static a1a4w.onhandsme.list.AddClientActivity.MY_REQUEST_LOCATION;
import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class OrderManActivity extends AppCompatActivity {
    private RecyclerView recyclerViewUnApproved, recyclerViewApproved, recyclerViewDenied, clientList;
    private FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderUnapproved> adapterFirebaseUnapproved;
    private FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderApproved> adapterFirebaseApproved;
    private FirebaseRecyclerAdapter<OrderDetail, OutRouteViewHolder> adapterFirebaseDenied;
    private FirebaseRecyclerAdapter<Product, ProductViewHolder> adapterFirebaseProduct;
    private FirebaseRecyclerAdapter<Employee, EmployeeViewHolder> adapterFirebaseEmployee;
    private FirebaseRecyclerAdapter<Client, ClientViewHolder> adapterFirebaseClient;
    FirebaseRecyclerAdapter<Group, GroupViewHolder> adapterFirebaseClientGroup;
    private FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderByTime> adapterFirebaseByTime;

    private LinearLayoutManager linearLayoutManager;
    private LinearLayout layoutApproved, layoutUnapproved, layoutDenied, boxApproved, boxUnApproved, boxCancelled;
    private DatabaseReference refApproved, refUnapproved, refDenied,refCompany;
    private Bundle b = new Bundle();
    private String emailLogin, choosenMonth, choosenYear, choosenProduct, choosenEmployee, employeeEmail;

    private Dialog dialogProductList, dialogEmployeeList;
    private TextView tvChooseProduct, tvEmployeeName, tvUnapproved, tvApproved, tvDenied;
    private boolean saleMan,supervisor,asm,admin;
    private double latitude, longitude;
    private RecyclerView rvClientList;
    int MY_REQUEST_CALL = 2;
    private String clientPhone;
    private ProgressDialog mProgressDialog;
    private HashMap<String,Float> productStock = new HashMap<>();
    private String userEmail,managerEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_man);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_order);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");
        saleMan = intent.getBooleanExtra("SaleMan", false);
        supervisor = intent.getBooleanExtra("Supervisor", false);
        admin = intent.getBooleanExtra("Admin",false);

        asm = intent.getBooleanExtra("ASM", false);
        //refCurrentClient = Constants.refClientMan.child("Đại lý").child("Nha Trang");
        userEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail().replace(".", ",");

        refCompany = refDatabase.child(emailLogin);

        tvUnapproved = (TextView) findViewById(R.id.tv_order_unapproved);
        tvApproved = (TextView) findViewById(R.id.tv_order_approved);
        tvDenied = (TextView) findViewById(R.id.tv_order_denied);


        boxApproved = (LinearLayout) findViewById(R.id.order_man_boxApproved);
        boxUnApproved = (LinearLayout) findViewById(R.id.order_man_boxUnApproved);
        boxCancelled = (LinearLayout) findViewById(R.id.order_man_boxCanceled);

        recyclerViewApproved = (RecyclerView) findViewById(R.id.order_approved_recyclerview);
        recyclerViewDenied = (RecyclerView) findViewById(R.id.order_denied_recyclerview);
        recyclerViewUnApproved = (RecyclerView) findViewById(R.id.order_unapproved_recyclerview);

        ConstraintLayout csSuccess = findViewById(R.id.cs_orderman_success);

        if(admin)
            csSuccess.setVisibility(View.GONE);

        boxUnApproved.setBackgroundColor(Color.WHITE);
        recyclerViewUnApproved.setVisibility(View.VISIBLE);
        tvUnapproved.setTextColor(getResources().getColor(R.color.colorAccent));

        ImageButton ibNewOrder = (ImageButton) findViewById(R.id.ib_order_man_newOrder);

        ibNewOrder.setVisibility(View.GONE);

        ibNewOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent it = new Intent(OrderManActivity.this, ClientListActivity.class);
                it.putExtra("EmailLogin", emailLogin);
                it.putExtra("SaleMan", saleMan);
                startActivity(it);
            }
        });


        initializeUnapproved();
        initializeApproved();
        initializeDenied();

        boxApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                boxApproved.setBackgroundColor(Color.WHITE);
                boxUnApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxCancelled.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                recyclerViewApproved.setVisibility(View.VISIBLE);
                recyclerViewUnApproved.setVisibility(View.INVISIBLE);
                recyclerViewDenied.setVisibility(View.INVISIBLE);

                tvApproved.setTextColor(getResources().getColor(R.color.colorAccent));
                tvUnapproved.setTextColor(getResources().getColor(android.R.color.white));
                tvDenied.setTextColor(getResources().getColor(android.R.color.white));


            }
        });

        boxUnApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                boxUnApproved.setBackgroundColor(Color.WHITE);
                boxApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxCancelled.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                recyclerViewApproved.setVisibility(View.INVISIBLE);
                recyclerViewUnApproved.setVisibility(View.VISIBLE);
                recyclerViewDenied.setVisibility(View.INVISIBLE);

                tvUnapproved.setTextColor(getResources().getColor(R.color.colorAccent));
                tvApproved.setTextColor(getResources().getColor(android.R.color.white));
                tvDenied.setTextColor(getResources().getColor(android.R.color.white));

            }
        });

        boxCancelled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                boxCancelled.setBackgroundColor(Color.WHITE);
                boxApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxUnApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                recyclerViewApproved.setVisibility(View.INVISIBLE);
                recyclerViewUnApproved.setVisibility(View.INVISIBLE);
                recyclerViewDenied.setVisibility(View.VISIBLE);

                tvDenied.setTextColor(getResources().getColor(R.color.colorAccent));
                tvApproved.setTextColor(getResources().getColor(android.R.color.white));
                tvUnapproved.setTextColor(getResources().getColor(android.R.color.white));
            }
        });

        locationPreparation();
        orderByTime();

        //getTotalByTime();

    }

    private void getTotalByTime() {
        final Map<String,Float> totalSales = new HashMap<>();
        refCompany.child("TotalByClient").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapClient = dataSnapshot.getChildren();
                for(final DataSnapshot itemClient:snapClient){
                    Iterable<DataSnapshot> snapSale = itemClient.getChildren();
                    for(final DataSnapshot itemSale:snapSale){
                        final String timeKey = itemSale.getKey();
                        final float timeSale = Float.parseFloat(itemSale.getValue().toString());

                        if(totalSales.containsKey(timeKey)){
                            float currentSale = totalSales.get(timeKey);
                            float updateSale = timeSale + currentSale;
                            totalSales.put(timeKey,updateSale);
                        }else{
                            totalSales.put(timeKey,timeSale);
                        }

                        for(Map.Entry<String,Float> entry:totalSales.entrySet()){
                            refCompany.child("TotalByTime").child(entry.getKey()).setValue(entry.getValue()+"");
                        }
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void orderByTime() {

        final RecyclerView listByTime = findViewById(R.id.rv_order_list_by_time);
        listByTime.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        listByTime.setLayoutManager(linearLayoutManager);

        final Button btnListMonth = findViewById(R.id.btn_order_month);
        final Button btnYear = findViewById(R.id.btn_order_year);

        btnListMonth.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
        btnYear.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));


        btnListMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                final List<OrderDetail> orders = new ArrayList<>();

                btnListMonth.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                btnYear.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));

                refDatabase.child(emailLogin).child("SaleOrder").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapOrder = dataSnapshot.getChildren();
                        for(DataSnapshot itemOrder:snapOrder){
                            final String orderKey = itemOrder.getKey();
                            String timeStamp = itemOrder.getValue().toString();

                            Date orderDate = (new Date(Long.parseLong(timeStamp)));

                            DateTime dt = new DateTime();

                            String year = dt.getYear()+"";
                            String month = dt.getMonthOfYear() +"";

                            String maxDay = dt.dayOfMonth().withMaximumValue().getDayOfMonth()+"";
                            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                            try {
                                Date startMonthDate = sdf.parse("01/"+month+"/"+year);
                                Date endMonthDate = sdf.parse(maxDay + "/"+month+"/"+year);

                                if(orderDate.after(startMonthDate) && orderDate.before(endMonthDate)){
                                    refDatabase.child(emailLogin).child("OrderList").child(orderKey).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                                            OrderDetail  addOrder = new OrderDetail(orderDetail.getOrderName(),orderKey);
                                            orders.add(addOrder);

                                            listByTime.setVisibility(View.VISIBLE);

                                            AdapterOrder adapterOrder = new AdapterOrder(getApplicationContext(),orders,emailLogin,OrderManActivity.this);
                                            listByTime.setAdapter(adapterOrder);
                                            adapterOrder.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        btnYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                final List<OrderDetail> orders = new ArrayList<>();

                btnYear.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                btnListMonth.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));

                refDatabase.child(emailLogin).child("SaleOrder").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapOrder = dataSnapshot.getChildren();
                        for(DataSnapshot itemOrder:snapOrder){
                            final String orderKey = itemOrder.getKey();
                            String timeStamp = itemOrder.getValue().toString();

                            Date orderDate = (new Date(Long.parseLong(timeStamp)));

                            DateTime dt = new DateTime();

                            String year = dt.getYear()+"";
                            String month = dt.getMonthOfYear() +"";

                            String maxDay = dt.dayOfMonth().withMaximumValue().getDayOfMonth()+"";
                            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                            try {
                                Date startMonthDate = sdf.parse("1/1/"+year);
                                Date endMonthDate = sdf.parse("31/12/"+year);

                                if(orderDate.after(startMonthDate) && orderDate.before(endMonthDate)){

                                    refDatabase.child(emailLogin).child("OrderList").child(orderKey).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                                            OrderDetail  addOrder = new OrderDetail(orderDetail.getOrderName(),orderKey);
                                            orders.add(addOrder);

                                            listByTime.setVisibility(View.VISIBLE);

                                            AdapterOrder adapterOrder = new AdapterOrder(getApplicationContext(),orders,emailLogin,OrderManActivity.this);
                                            listByTime.setAdapter(adapterOrder);
                                            adapterOrder.notifyDataSetChanged();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        refDatabase.child(emailLogin).child("SaleOrder").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapOrder = dataSnapshot.getChildren();

                final List<OrderDetail> orders = new ArrayList<>();

                for(DataSnapshot itemOrder:snapOrder){
                    final String orderKey = itemOrder.getKey();
                    String timeStamp = itemOrder.getValue().toString();

                    Date orderDate = (new Date(Long.parseLong(timeStamp)));

                    DateTime dt = new DateTime();

                    String year = dt.getYear()+"";
                    String month = dt.getMonthOfYear() +"";

                    String maxDay = dt.dayOfMonth().withMaximumValue().getDayOfMonth()+"";
                    DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                    try {
                        Date startMonthDate = sdf.parse("01/"+month+"/"+year);
                        Date endMonthDate = sdf.parse(maxDay + "/"+month+"/"+year);

                        if(orderDate.after(startMonthDate) && orderDate.before(endMonthDate)){
                            refDatabase.child(emailLogin).child("OrderList").child(orderKey).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                                    OrderDetail  addOrder = new OrderDetail(orderDetail.getOrderName(),orderKey);
                                    orders.add(addOrder);

                                    listByTime.setVisibility(View.VISIBLE);

                                    AdapterOrder adapterOrder = new AdapterOrder(getApplicationContext(),orders,emailLogin,OrderManActivity.this);
                                    listByTime.setAdapter(adapterOrder);
                                    adapterOrder.notifyDataSetChanged();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }


                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void locationPreparation() {

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(getApplicationContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_REQUEST_LOCATION);
            }

        }


        mFusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(final Location location) {
                if (location != null) {

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                } else {
                    Toast.makeText(getApplicationContext(), "Không lấy được vị trí hiện tại, vui lòng thử lại!", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void initializeDenied() {
        recyclerViewDenied.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewDenied.setLayoutManager(linearLayoutManager);

        getOutRouteOrder();


    }

    private void initializeApproved() {
        recyclerViewApproved.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewApproved.setLayoutManager(linearLayoutManager);


        getApprovedOrder();

    }

    private void initializeUnapproved() {
        recyclerViewUnApproved.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewUnApproved.setLayoutManager(linearLayoutManager);

        getUnApprovedOrder();

    }

    private void getApprovedOrder() {
        if(admin){
            refApproved = Constants.refDatabase.child(emailLogin + "/Order").child("Approved");
            adapterFirebaseApproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderApproved>(
                    OrderDetail.class,
                    R.id.order_cardview,
                    OrderViewHolderApproved.class,
                    refApproved
            ) {
                @Override
                public OrderViewHolderApproved onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                    return new OrderViewHolderApproved(v);
                }


                @Override
                protected void populateViewHolder(OrderViewHolderApproved viewHolder, OrderDetail model, int position) {
                    viewHolder.orderName.setText(model.getOrderName());
                    if(admin){
                        viewHolder.ivApprove.setVisibility(View.VISIBLE);
                    }

                }
            };

            recyclerViewApproved.setAdapter(adapterFirebaseApproved);
            adapterFirebaseApproved.notifyDataSetChanged();
        }

        if(saleMan){
            refCompany.child("Employee").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Employee employee = dataSnapshot.getValue(Employee.class);
                    String managerEmail = employee.getManagedBy();

                    refApproved = refCompany.child("Order/OrderBySale").child(managerEmail).child("Approved");

                    adapterFirebaseApproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderApproved>(
                            OrderDetail.class,
                            R.id.order_cardview,
                            OrderViewHolderApproved.class,
                            refApproved
                    ) {
                        @Override
                        public OrderViewHolderApproved onCreateViewHolder(ViewGroup parent, int viewType) {
                            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                            return new OrderViewHolderApproved(v);
                        }


                        @Override
                        protected void populateViewHolder(OrderViewHolderApproved viewHolder, OrderDetail model, int position) {
                            viewHolder.orderName.setText(model.getOrderName());
                            if(admin){
                                viewHolder.ivApprove.setVisibility(View.VISIBLE);
                            }

                        }
                    };

                    recyclerViewApproved.setAdapter(adapterFirebaseApproved);
                    adapterFirebaseApproved.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if(supervisor){
            refApproved = refCompany.child("Order/OrderBySale").child(userEmail).child("Approved");

            adapterFirebaseApproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderApproved>(
                    OrderDetail.class,
                    R.id.order_cardview,
                    OrderViewHolderApproved.class,
                    refApproved
            ) {
                @Override
                public OrderViewHolderApproved onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                    return new OrderViewHolderApproved(v);
                }


                @Override
                protected void populateViewHolder(OrderViewHolderApproved viewHolder, OrderDetail model, int position) {
                    viewHolder.orderName.setText(model.getOrderName());
                    if(admin){
                        viewHolder.ivApprove.setVisibility(View.VISIBLE);
                    }

                }
            };

            recyclerViewApproved.setAdapter(adapterFirebaseApproved);
            adapterFirebaseApproved.notifyDataSetChanged();
        }



    }

    private void getUnApprovedOrder() {

        if(admin){
            refUnapproved = Constants.refDatabase.child(emailLogin + "/Order").child("UnApproved");

            adapterFirebaseUnapproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderUnapproved>(
                    OrderDetail.class,
                    R.id.order_cardview,
                    OrderViewHolderUnapproved.class,
                    refUnapproved
            ) {
                @Override
                public OrderViewHolderUnapproved onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                    return new OrderViewHolderUnapproved(v);
                }


                @Override
                protected void populateViewHolder(OrderViewHolderUnapproved viewHolder, OrderDetail model, int position) {
                    viewHolder.orderName.setText(model.getOrderName());
                    if(supervisor || asm){
                        viewHolder.ivApprove.setVisibility(View.VISIBLE);
                    }
                }
            };

            recyclerViewUnApproved.setAdapter(adapterFirebaseUnapproved);
            adapterFirebaseUnapproved.notifyDataSetChanged();
        }

        if(saleMan){
            refCompany.child("Employee").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Employee employee = dataSnapshot.getValue(Employee.class);
                    String managerEmail = employee.getManagedBy();

                    refUnapproved = refCompany.child("Order/OrderBySale").child(managerEmail).child("UnApproved");

                    adapterFirebaseUnapproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderUnapproved>(
                            OrderDetail.class,
                            R.id.order_cardview,
                            OrderViewHolderUnapproved.class,
                            refUnapproved
                    ) {
                        @Override
                        public OrderViewHolderUnapproved onCreateViewHolder(ViewGroup parent, int viewType) {
                            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                            return new OrderViewHolderUnapproved(v);
                        }


                        @Override
                        protected void populateViewHolder(OrderViewHolderUnapproved viewHolder, OrderDetail model, int position) {
                            viewHolder.orderName.setText(model.getOrderName());
                            if(supervisor || asm){
                                viewHolder.ivApprove.setVisibility(View.VISIBLE);
                            }
                        }
                    };

                    recyclerViewUnApproved.setAdapter(adapterFirebaseUnapproved);
                    adapterFirebaseUnapproved.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if(supervisor){
            refUnapproved = refCompany.child("Order/OrderBySale").child(userEmail).child("UnApproved");

            adapterFirebaseUnapproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderUnapproved>(
                    OrderDetail.class,
                    R.id.order_cardview,
                    OrderViewHolderUnapproved.class,
                    refUnapproved
            ) {
                @Override
                public OrderViewHolderUnapproved onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                    return new OrderViewHolderUnapproved(v);
                }


                @Override
                protected void populateViewHolder(OrderViewHolderUnapproved viewHolder, OrderDetail model, int position) {
                    viewHolder.orderName.setText(model.getOrderName());
                    if(supervisor || asm){
                        viewHolder.ivApprove.setVisibility(View.VISIBLE);
                    }
                }
            };

            recyclerViewUnApproved.setAdapter(adapterFirebaseUnapproved);
            adapterFirebaseUnapproved.notifyDataSetChanged();
        }



    }

    private void getOutRouteOrder() {

        if(admin){
            refDenied = Constants.refDatabase.child(emailLogin + "/Order").child("OutRoute");

            adapterFirebaseDenied = new FirebaseRecyclerAdapter<OrderDetail, OutRouteViewHolder>(
                    OrderDetail.class,
                    R.id.order_cardview,
                    OutRouteViewHolder.class,
                    refDenied
            ) {
                @Override
                public OutRouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                    return new OutRouteViewHolder(v);
                }


                @Override
                protected void populateViewHolder(OutRouteViewHolder viewHolder, OrderDetail model, int position) {
                    viewHolder.orderName.setText(model.getOrderName());
                    if(supervisor || asm){
                        viewHolder.ivApprove.setVisibility(View.VISIBLE);
                    }
                }
            };

            recyclerViewDenied.setAdapter(adapterFirebaseDenied);
            adapterFirebaseDenied.notifyDataSetChanged();
        }

        if(saleMan){
            refCompany.child("Employee").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Employee employee = dataSnapshot.getValue(Employee.class);
                    String managerEmail = employee.getManagedBy();

                    refDenied = refCompany.child("Order/OrderBySale").child(managerEmail).child("OutRoute");

                    adapterFirebaseDenied = new FirebaseRecyclerAdapter<OrderDetail, OutRouteViewHolder>(
                            OrderDetail.class,
                            R.id.order_cardview,
                            OutRouteViewHolder.class,
                            refDenied
                    ) {
                        @Override
                        public OutRouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                            return new OutRouteViewHolder(v);
                        }


                        @Override
                        protected void populateViewHolder(OutRouteViewHolder viewHolder, OrderDetail model, int position) {
                            viewHolder.orderName.setText(model.getOrderName());
                            if(supervisor || asm){
                                viewHolder.ivApprove.setVisibility(View.VISIBLE);
                            }
                        }
                    };

                    recyclerViewDenied.setAdapter(adapterFirebaseDenied);
                    adapterFirebaseDenied.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        if(supervisor){
            refDenied = refCompany.child("Order/OrderBySale").child(userEmail).child("OutRoute");

            adapterFirebaseDenied = new FirebaseRecyclerAdapter<OrderDetail, OutRouteViewHolder>(
                    OrderDetail.class,
                    R.id.order_cardview,
                    OutRouteViewHolder.class,
                    refDenied
            ) {
                @Override
                public OutRouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                    return new OutRouteViewHolder(v);
                }


                @Override
                protected void populateViewHolder(OutRouteViewHolder viewHolder, OrderDetail model, int position) {
                    viewHolder.orderName.setText(model.getOrderName());
                    if(supervisor || asm){
                        viewHolder.ivApprove.setVisibility(View.VISIBLE);
                    }
                }
            };

            recyclerViewDenied.setAdapter(adapterFirebaseDenied);
            adapterFirebaseDenied.notifyDataSetChanged();
        }

    }

    private void orderEmployeeFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_employee_filter, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Spinner spinYearFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        Spinner spinMonthFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Button btnOk = (Button) dialogView.findViewById(R.id.btn_dialog_filter_employee_ok);
        tvEmployeeName = dialogView.findViewById(R.id.tv_filter_employee_choose);

        tvEmployeeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                employeeListDialog();

            }
        });

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        spinnerYear.setAdapter(spinerAdapterYear);
        spinnerYear.setSelection(0);

        spinYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosenYear = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayMonth = this.getResources().getStringArray(R.array.month_filter_array);
        List<String> arrayListMonth = Arrays.asList(arrayMonth);
        MySpinerAdapter spinerAdapterMonth = new MySpinerAdapter();
        spinerAdapterMonth.addItems(arrayListMonth);
        MaterialSpinner spinnerMonth = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        spinnerMonth.setAdapter(spinerAdapterMonth);
        spinnerMonth.setSelection(0);

        spinMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosenMonth = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                choosenEmployee = tvEmployeeName.getText().toString();

                if (choosenYear != null && choosenMonth != null && !choosenEmployee.equals("Chọn nhân viên")) {
                    Intent it = new Intent(getApplicationContext(), FilterEmployeeActivity.class);
                    it.putExtra("ChoosenYear", choosenYear);
                    it.putExtra("ChoosenMonth", choosenMonth);
                    it.putExtra("EmailLogin", emailLogin);
                    it.putExtra("EmployeeName", choosenEmployee);
                    startActivity(it);
                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn đủ các yếu tố", Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    private void orderFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_filter_order, null);
        builder.setView(dialogView);

        Spinner spinYearFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Spinner spinMonthFilter = (Spinner) dialogView.findViewById(R.id.spinner_filter_month);
        Button btnOkFilter = (Button) dialogView.findViewById(R.id.btn_filter_order_ok);

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        spinnerYear.setAdapter(spinerAdapterYear);
        spinnerYear.setSelection(0);

        spinYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenYear = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayMonth = this.getResources().getStringArray(R.array.month_filter_array);
        List<String> arrayListMonth = Arrays.asList(arrayMonth);
        MySpinerAdapter spinerAdapterMonth = new MySpinerAdapter();
        spinerAdapterMonth.addItems(arrayListMonth);
        MaterialSpinner spinnerMonth = (MaterialSpinner) dialogView.findViewById(R.id.spinner_filter_month);
        spinnerMonth.setAdapter(spinerAdapterMonth);
        spinnerMonth.setSelection(0);

        spinMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenMonth = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOkFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                if (choosenYear != null && choosenMonth != null) {
                    Intent intent = new Intent(getApplicationContext(), FilterTimeActivity.class);
                    intent.putExtra("ChoosenYear", choosenYear);
                    intent.putExtra("ChoosenMonth", choosenMonth);
                    intent.putExtra("EmailLogin", emailLogin);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn đủ các yếu tố", Toast.LENGTH_LONG).show();
                }

            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void orderProductFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_filter_product, null);
        builder.setView(dialogView);

        Spinner spinYearFilter = (Spinner) dialogView.findViewById(R.id.spinner_filter_product_year);
        Spinner spinMonthFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Button btnOkFilter = (Button) dialogView.findViewById(R.id.btn_filter_product_ok);
        tvChooseProduct = dialogView.findViewById(R.id.tv_filter_product_choose);

        tvChooseProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                productListDialog();
            }
        });

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner) dialogView.findViewById(R.id.spinner_filter_product_year);
        spinnerYear.setAdapter(spinerAdapterYear);
        spinnerYear.setSelection(0);

        spinYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenYear = (String) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayMonth = this.getResources().getStringArray(R.array.month_filter_array);
        List<String> arrayListMonth = Arrays.asList(arrayMonth);
        MySpinerAdapter spinerAdapterMonth = new MySpinerAdapter();
        spinerAdapterMonth.addItems(arrayListMonth);
        MaterialSpinner spinnerMonth = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        spinnerMonth.setAdapter(spinerAdapterMonth);
        spinnerMonth.setSelection(0);

        spinMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenMonth = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOkFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                choosenProduct = tvChooseProduct.getText().toString();
                if (choosenYear != null && choosenMonth != null && !choosenProduct.equals("Chọn sản phẩm")) {
                    Intent intent = new Intent(getApplicationContext(), FilterProductActivity.class);
                    intent.putExtra("ChoosenProduct", choosenProduct);
                    intent.putExtra("ChoosenYear", choosenYear);
                    intent.putExtra("ChoosenMonth", choosenMonth);
                    intent.putExtra("EmailLogin", emailLogin);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn đủ các yếu tố", Toast.LENGTH_LONG).show();
                }

            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void orderClientFilterDialog(final String clientCode) {

        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_filter_client, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Spinner spinYearFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        Spinner spinMonthFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Button btnOk = (Button) dialogView.findViewById(R.id.btn_dialog_filter_client_ok);

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        spinnerYear.setAdapter(spinerAdapterYear);
        spinnerYear.setSelection(0);

        spinYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenYear = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayMonth = this.getResources().getStringArray(R.array.month_filter_array);
        List<String> arrayListMonth = Arrays.asList(arrayMonth);
        MySpinerAdapter spinerAdapterMonth = new MySpinerAdapter();
        spinerAdapterMonth.addItems(arrayListMonth);
        MaterialSpinner spinnerMonth = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        spinnerMonth.setAdapter(spinerAdapterMonth);
        spinnerMonth.setSelection(0);

        spinMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenMonth = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                if (choosenYear != null && choosenMonth != null) {
                    Intent it = new Intent(getApplicationContext(), FilterClientActivity.class);
                    it.putExtra("ChoosenYear", choosenYear);
                    it.putExtra("ChoosenMonth", choosenMonth);
                    it.putExtra("EmailLogin", emailLogin);
                    it.putExtra("ClientCode", clientCode);
                    it.putExtra("SaleMan", true);
                    startActivity(it);

                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn đủ các yếu tố", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void productListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_list, null);
        builder.setView(dialogView);
        builder.setMessage("Chọn sản phẩm (trượt dọc để xem tiếp)");

        dialogProductList = builder.create();

        Button btnNew = dialogView.findViewById(R.id.btn_product_list_new);

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
            }
        });

        final RecyclerView productList = (RecyclerView) dialogView.findViewById(R.id.recycler_dialog_product_list);
        productList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productList.setLayoutManager(linearLayoutManager);

        adapterFirebaseProduct = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product_pos,
                ProductViewHolder.class,
                refDatabase.child(emailLogin + "/Product")
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_pos, parent, false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());

            }
        };

        productList.setAdapter(adapterFirebaseProduct);
        adapterFirebaseProduct.notifyDataSetChanged();

        dialogProductList.show();
    }


    private void employeeListDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_employee_list, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setMessage("Chọn nhân viên (trượt dọc để xem tiếp)");

        dialogEmployeeList = dialogBuilder.create();
        dialogEmployeeList.show();

        Button btnCreate = dialogView.findViewById(R.id.btn_employee_list_new);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                addEmployeeDialog();

            }
        });

        RecyclerView employeeList = (RecyclerView) dialogView.findViewById(R.id.recycler_dialog_employee_list);
        employeeList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        employeeList.setLayoutManager(linearLayoutManager);

        adapterFirebaseEmployee = new FirebaseRecyclerAdapter<Employee, EmployeeViewHolder>(
                Employee.class,
                R.id.item_client,
                EmployeeViewHolder.class,
                refDatabase.child(emailLogin).child("Employee")
        ) {
            @Override
            public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);
                return new EmployeeViewHolder(v);
            }


            @Override
            protected void populateViewHolder(EmployeeViewHolder viewHolder, Employee model, int position) {
                viewHolder.employeeName.setText(model.getEmployeeName());

            }
        };

        employeeList.setAdapter(adapterFirebaseEmployee);
        adapterFirebaseEmployee.notifyDataSetChanged();

    }

    private void addEmployeeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_employee, null);
        builder.setView(dialogView);
        builder.setMessage("Thêm nhân viên mới");

        final Dialog dialog = builder.create();
        dialog.show();

        final EditText edtAddEmployee = (EditText) dialogView.findViewById(R.id.edt_dialog_add_employee_name);
        Button btnAdd = (Button) dialogView.findViewById(R.id.btn_dialog_add_employee_ok);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                final String employeeName = edtAddEmployee.getText().toString();

                if (TextUtils.isEmpty(employeeName)) {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập tên Nhân viên", Toast.LENGTH_LONG).show();

                } else {
                    Employee employee = new Employee(employeeName);
                    refDatabase.child(emailLogin + "/Employee").push().setValue(employee);

                    dialog.dismiss();
                }


            }
        });

    }


    public class OrderViewHolderUnapproved extends RecyclerView.ViewHolder {
        TextView orderName;
        ImageView ivApprove;

        public OrderViewHolderUnapproved(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);
            ivApprove = itemView.findViewById(R.id.iv_item_order_approve);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    final int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseUnapproved.getRef(position);
                    final String itemKeyString = itemKey.getKey();

                    refDatabase.child(emailLogin).child("OrderList").child(itemKeyString).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                            assert orderDetail != null;
                            String clientCode = orderDetail.getClientCode();
                            Intent intent1 = new Intent(getApplicationContext(), PreviewOrderActivivity.class);
                            intent1.putExtra("OrderPushKey", itemKeyString);
                            intent1.putExtra("EmailLogin", emailLogin);
                            intent1.putExtra("ClientCode",clientCode);
                            intent1.putExtra("ViewOnly",true);
                            startActivity(intent1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            });

            ivApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    int position = getAdapterPosition();
                    final OrderDetail orderDetail = adapterFirebaseUnapproved.getItem(position);
                    final String orderPushKey = adapterFirebaseUnapproved.getRef(position).getKey();

                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                    builder.setMessage("Duyệt đơn hàng này?");

                    builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            refCompany.child("Order/OrderBySale").child(userEmail).child("UnApproved").child(orderPushKey).setValue(null);

                            Constants.refDatabase.child(emailLogin+"/Order").child("UnApproved").child(orderPushKey).setValue(null);
                            dialog.dismiss();

                        }
                    });

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            refCompany.child("Order/OrderBySale").child(userEmail).child("Approved").child(orderPushKey).child("orderName").setValue(orderDetail.getOrderName());
                            Constants.refDatabase.child(emailLogin+"/Order").child("Approved").child(orderPushKey).child("orderName").setValue(orderDetail.getOrderName()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Constants.refDatabase.child(emailLogin+"/Order").child("UnApproved").child(orderPushKey).setValue(null);
                                    refCompany.child("Order/OrderBySale").child(userEmail).child("UnApproved").child(orderPushKey).setValue(null);

                                }
                            });

                        }
                    }).show();
                }
            });

        }
    }

    public class OrderViewHolderApproved extends RecyclerView.ViewHolder {
        TextView orderName;
        ImageButton ibPrint;
        ImageView ivApprove,ivDelivery;

        public OrderViewHolderApproved(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);
            ivApprove = itemView.findViewById(R.id.iv_item_order_approve);
            ivDelivery = itemView.findViewById(R.id.iv_item_order_delivery);

            ivDelivery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);

                }
            });

            ivApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);

                    showProgressDialog();

                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                    builder.setMessage("Cập nhật doanh số đơn hàng này?");

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DateTime dt = new DateTime();
                            final String year = dt.getYear()+"";
                            final String month = dt.getMonthOfYear()+"";
                            final String date = dt.getDayOfMonth()+"";

                            int position = getLayoutPosition();
                            final DatabaseReference itemKey = adapterFirebaseApproved.getRef(position);
                            final String orderKey = itemKey.getKey();

                            refCompany.child("Order/OrderBySale").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapSup = dataSnapshot.getChildren();
                                    for(DataSnapshot itemSup:snapSup){
                                        final DatabaseReference refSup = itemSup.getRef();
                                        refSup.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("Approved")){
                                                    refSup.child("Approved").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.hasChild(orderKey)){
                                                                refSup.child("Approved").child(orderKey).setValue(null);
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

                            refDatabase.child(emailLogin).child("OrderList").child(orderKey).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    final OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                                    final String clientCode = orderDetail.getClientCode();
                                    final String saleEmail = orderDetail.getEmployeeCode();

                                    final String timeStamp = Calendar.getInstance().getTime().getTime()+"";

                                    refDatabase.child(emailLogin).child("OrderList").child(orderKey).child("VAT").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            VatModel vatModel = dataSnapshot.getValue(VatModel.class);
                                            final float finalPayment = vatModel.getFinalPayment();

                                            Constants.refDatabase.child(emailLogin).child("ClientOrder").child(clientCode).child(orderKey).setValue(timeStamp);
                                            refCompany.child("SaleOrder").child(saleEmail).child(orderKey).setValue(timeStamp);
                                            refCompany.child("Order/Sale").child(orderKey).setValue(orderDetail);
                                            Constants.refDatabase.child(emailLogin).child("TimeOrder").child(orderKey).setValue(timeStamp);

                                            refCompany.child("Order/Approved").child(orderKey).setValue(null);
                                            refCompany.child("TotalByT").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(year+"-"+month+"-"+date)){
                                                        refCompany.child("TotalByTime").child(clientCode).child(year+"-"+month+"-"+date).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                float updateSale = currentSale + finalPayment;

                                                                refCompany.child("TotalByTime").child(clientCode).child(year+"-"+month+"-"+date).setValue(updateSale+"");
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        refCompany.child("TotalByTime").child(clientCode).child(year+"-"+month+"-"+date).setValue(finalPayment+"");
                                                    }

                                                    if(dataSnapshot.hasChild(year+"-"+month)){
                                                        refCompany.child("TotalByTime").child(clientCode).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                float updateSale = currentSale + finalPayment;

                                                                refCompany.child("TotalByTime").child(clientCode).child(year+"-"+month).setValue(updateSale+"");
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        refCompany.child("TotalByTime").child(clientCode).child(year+"-"+month).setValue(finalPayment+"");
                                                    }

                                                    if(dataSnapshot.hasChild(year)){
                                                        refCompany.child("TotalByTime").child(clientCode).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                float updateSale = currentSale + finalPayment;

                                                                refCompany.child("TotalByTime").child(clientCode).child(year).setValue(updateSale+"");
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        refCompany.child("TotalByTime").child(clientCode).child(year).setValue(finalPayment+"");
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            refCompany.child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(year+"-"+month+"-"+date)){
                                                        refCompany.child("TotalByClient").child(clientCode).child(year+"-"+month+"-"+date).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                float updateSale = currentSale + finalPayment;

                                                                refCompany.child("TotalByClient").child(clientCode).child(year+"-"+month+"-"+date).setValue(updateSale+"");
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        refCompany.child("TotalByClient").child(clientCode).child(year+"-"+month+"-"+date).setValue(finalPayment+"");
                                                    }

                                                    if(dataSnapshot.hasChild(year+"-"+month)){
                                                        refCompany.child("TotalByClient").child(clientCode).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                float updateSale = currentSale + finalPayment;

                                                                refCompany.child("TotalByClient").child(clientCode).child(year+"-"+month).setValue(updateSale+"");
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        refCompany.child("TotalByClient").child(clientCode).child(year+"-"+month).setValue(finalPayment+"");
                                                    }

                                                    if(dataSnapshot.hasChild(year)){
                                                        refCompany.child("TotalByClient").child(clientCode).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                float updateSale = currentSale + finalPayment;

                                                                refCompany.child("TotalByClient").child(clientCode).child(year).setValue(updateSale+"");
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        refCompany.child("TotalByClient").child(clientCode).child(year).setValue(finalPayment+"");
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(year+"-"+month+"-"+date)){
                                                        refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month+"-"+date).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                float updateSale = currentSale + finalPayment;

                                                                refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month+"-"+date).setValue(updateSale+"");
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month+"-"+date).setValue(finalPayment+"");
                                                    }

                                                    if(dataSnapshot.hasChild(year+"-"+month)){
                                                        refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                float updateSale = currentSale + finalPayment;

                                                                refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).setValue(updateSale+"");
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).setValue(finalPayment+"");
                                                    }

                                                    if(dataSnapshot.hasChild(year)){
                                                        refCompany.child("TotalBySale").child(saleEmail).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                float updateSale = currentSale + finalPayment;

                                                                refCompany.child("TotalBySale").child(saleEmail).child(year).setValue(updateSale+"");
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        refCompany.child("TotalBySale").child(saleEmail).child(year).setValue(finalPayment+"");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                            refCompany.child("OrderList").child(orderKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();
                                                    long itemCount = dataSnapshot.getChildrenCount();

                                                    int i = 0;
                                                    for(DataSnapshot itemProduct:productSnap){
                                                        i++;
                                                        final Product product = itemProduct.getValue(Product.class);
                                                        final String finalPayment = product.getFinalPayment();
                                                        final String productName = product.getProductName();
                                                        final String productCode = product.getProductCode();
                                                        final float productQuantity = Float.parseFloat(product.getUnitQuantity());

                                                        refCompany.child("TotalProduct").child(productCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if(dataSnapshot.hasChild(year+"-"+month+"-"+date)){
                                                                    refCompany.child("TotalProduct").child(productCode).child(year+"-"+month+"-"+date).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                            float updateSale = currentSale + Float.parseFloat(finalPayment);

                                                                            refCompany.child("TotalProduct").child(productCode).child(year+"-"+month+"-"+date).setValue(updateSale+"");
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });

                                                                }else{
                                                                    refCompany.child("TotalProduct").child(productCode).child(year+"-"+month+"-"+date).setValue(finalPayment+"");
                                                                }

                                                                if(dataSnapshot.hasChild(year+"-"+month)){
                                                                    refCompany.child("TotalProduct").child(productCode).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                            float updateSale = currentSale + Float.parseFloat(finalPayment);

                                                                            refCompany.child("TotalProduct").child(productCode).child(year+"-"+month).setValue(updateSale+"");
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });

                                                                }else{
                                                                    refCompany.child("TotalProduct").child(productCode).child(year+"-"+month).setValue(finalPayment+"");
                                                                }

                                                                if(dataSnapshot.hasChild(year)){
                                                                    refCompany.child("TotalProduct").child(productCode).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                            float updateSale = currentSale + Float.parseFloat(finalPayment);

                                                                            refCompany.child("TotalProduct").child(productCode).child(year).setValue(updateSale+"");
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });

                                                                }else{
                                                                    refCompany.child("TotalProduct").child(productCode).child(year).setValue(finalPayment+"");
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                        refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if(dataSnapshot.hasChild(year+"-"+month+"-"+date)){
                                                                    refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year+"-"+month+"-"+date).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                            float updateSale = currentSale + Float.parseFloat(finalPayment);

                                                                            refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year+"-"+month+"-"+date).setValue(updateSale+"");
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });

                                                                }else{
                                                                    refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year+"-"+month+"-"+date).setValue(finalPayment+"");
                                                                }

                                                                if(dataSnapshot.hasChild(year+"-"+month)){
                                                                    refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                            float updateSale = currentSale + Float.parseFloat(finalPayment);

                                                                            refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year+"-"+month).setValue(updateSale+"");
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });

                                                                }else{
                                                                    refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year+"-"+month).setValue(finalPayment+"");
                                                                }

                                                                if(dataSnapshot.hasChild(year)){
                                                                    refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                            float updateSale = currentSale + Float.parseFloat(finalPayment);

                                                                            refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year).setValue(updateSale+"");
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });

                                                                }else{
                                                                    refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year).setValue(finalPayment+"");
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                        refCompany.child("OrderList").child(orderKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if(dataSnapshot.hasChild("Promotion")){
                                                                    refCompany.child("OrderList").child(orderKey).child(orderKey).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            Iterable<DataSnapshot> snapPromotion = dataSnapshot.getChildren();
                                                                            for (DataSnapshot itemPromotion:snapPromotion){
                                                                                DatabaseReference refPromotion = itemPromotion.getRef();
                                                                                refPromotion.addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        if(dataSnapshot.hasChild("BGM")){
                                                                                            Iterable<DataSnapshot> snapBGM = dataSnapshot.getChildren();
                                                                                            for (DataSnapshot itemBGM:snapBGM){
                                                                                                Promotion pBGM = itemBGM.getValue(Promotion.class);
                                                                                                if(pBGM.getPromotionBuyName().equals(productName)){
                                                                                                    if(Float.parseFloat(pBGM.getPromotionBuyQuantity()) <= productQuantity){

                                                                                                        float bgmGetQuantity = Float.parseFloat(pBGM.getPromotionGetQuantity());

                                                                                                        int numberOfPart = (int) (productQuantity/Float.parseFloat(pBGM.getPromotionBuyQuantity()));

                                                                                                        final float numberOfGet = numberOfPart * bgmGetQuantity;

                                                                                                        refCompany.child("WarehouseMan/StorageMan").child(pBGM.getPromotionGetCode()).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                            @Override
                                                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                                                final String productStorage = dataSnapshot.getValue().toString();
                                                                                                                final float updateStorage = Float.parseFloat(productStorage) - numberOfGet;
                                                                                                                refCompany.child("WarehouseMan/StorageMan").child(productCode).child("unitQuantity").setValue(updateStorage+"");

                                                                                                            }

                                                                                                            @Override
                                                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                                                            }
                                                                                                        });
                                                                                                    }
                                                                                                }

                                                                                            }
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

                                                        if(productStock.containsKey(productCode)){
                                                            float currentValue = productStock.get(productCode);
                                                            float updateValue = currentValue + productQuantity;
                                                            productStock.put(productCode,updateValue);

                                                        }else{
                                                            productStock.put(productCode,productQuantity);
                                                        }

                                                        if(i == itemCount){
                                                            if(productStock.containsKey(productName)){

                                                                float currentValue = productStock.get(productName);
                                                                float updateValue = currentValue + productQuantity;
                                                                productStock.put(productCode,updateValue);

                                                                int y = 0;
                                                                for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                                    y++;
                                                                    int mapItemCount = productStock.size();

                                                                    if(y == mapItemCount){
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                final String productStorage = dataSnapshot.getValue().toString();
                                                                                final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                                if(updateStorage<0){
                                                                                    Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                                }else{

                                                                                    Toast.makeText(getApplicationContext(),"Cập nhật thành công 2!",Toast.LENGTH_LONG).show();

                                                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                            .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            hideProgressDialog();
                                                                                            Toast.makeText(getApplicationContext(),"Cập nhật thành công!",Toast.LENGTH_LONG).show();
                                                                                            //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                                        }
                                                                                    });
                                                                                }

                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    }else{
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                final String productStorage = dataSnapshot.getValue().toString();
                                                                                final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                                if(updateStorage<0){
                                                                                    Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                                }else{
                                                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                            .setValue(updateStorage+"");
                                                                                }

                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    }
                                                                }

                                                            }else{

                                                                productStock.put(productCode,productQuantity);
                                                                int y = 0;
                                                                for(final Map.Entry<String,Float> entry:productStock.entrySet()){
                                                                    y++;
                                                                    int mapItemCount = productStock.size();

                                                                    if(y == mapItemCount){

                                                                        final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                final String productStorage = dataSnapshot.getValue().toString();
                                                                                final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();
                                                                                if(updateStorage<0){
                                                                                    Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                                }else{
                                                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                            .setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            hideProgressDialog();
                                                                                            Toast.makeText(getApplicationContext(),"Cập nhật thành công!",Toast.LENGTH_LONG).show();
                                                                                            //startActivity(new Intent(getApplicationContext(),MainActivity.class));
                                                                                        }
                                                                                    });


                                                                                }

                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    }else{
                                                                        Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                final String productStorage = dataSnapshot.getValue().toString();
                                                                                final float updateStorage = Float.parseFloat(productStorage) - entry.getValue();

                                                                                if(updateStorage<0){
                                                                                    Toast.makeText(getApplicationContext(),entry.getKey() +" không đủ hàng xuất kho",Toast.LENGTH_LONG).show();
                                                                                }else{
                                                                                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("StorageMan").child(entry.getKey()+"/unitQuantity")
                                                                                            .setValue(updateStorage+"");


                                                                                }

                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                                    }
                                                                }

                                                            }
                                                        }

                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    }).show();



                }
            });

            /*
            ibPrint = (ImageButton) itemView.findViewById(R.id.ib_item_order);

            ibPrint.setVisibility(View.VISIBLE);

            ibPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseApproved.getRef(position);
                    final String itemKeyString = itemKey.getKey();
                    Constants.refDatabase.child(emailLogin + "/OrderList").child(itemKeyString).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                            String orderName = orderDetail.getOrderName();
                            Intent it = new Intent(getApplicationContext(), PrintPreviewActivity.class);
                            it.putExtra("EmailLogin", emailLogin);
                            it.putExtra("OrderPushKey", itemKeyString);
                            it.putExtra("OrderName", orderName);
                            startActivity(it);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });
*/
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseApproved.getRef(position);
                    final String itemKeyString = itemKey.getKey();
                    refDatabase.child(emailLogin).child("OrderList").child(itemKeyString).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                            assert orderDetail != null;
                            String clientCode = orderDetail.getClientCode();
                            Intent intent1 = new Intent(getApplicationContext(), PreviewOrderActivivity.class);
                            intent1.putExtra("OrderPushKey", itemKeyString);
                            intent1.putExtra("EmailLogin", emailLogin);
                            intent1.putExtra("ClientCode",clientCode);
                            intent1.putExtra("ViewOnly",true);
                            startActivity(intent1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

        }
    }

    public class OutRouteViewHolder extends RecyclerView.ViewHolder {
        TextView orderName;
        ImageView ivApprove;

        public OutRouteViewHolder(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);
            ivApprove = itemView.findViewById(R.id.iv_item_order_approve);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseDenied.getRef(position);
                    final String itemKeyString = itemKey.getKey();

                    refDatabase.child(emailLogin).child("OrderList").child(itemKeyString).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                            assert orderDetail != null;
                            String clientCode = orderDetail.getClientCode();
                            Intent intent1 = new Intent(getApplicationContext(), PreviewOrderActivivity.class);
                            intent1.putExtra("OrderPushKey", itemKeyString);
                            intent1.putExtra("EmailLogin", emailLogin);
                            intent1.putExtra("ClientCode",clientCode);
                            intent1.putExtra("ViewOnly",true);
                            startActivity(intent1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

            ivApprove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    int position = getAdapterPosition();
                    final OrderDetail orderDetail = adapterFirebaseDenied.getItem(position);
                    final String orderPushKey = adapterFirebaseDenied.getRef(position).getKey();

                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                    builder.setMessage("Duyệt đơn hàng này?");

                    builder.setNeutralButton("Chưa", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    builder.setNegativeButton("Xoá", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Constants.refDatabase.child(emailLogin+"/Order").child("OutRoute").child(orderPushKey).setValue(null);
                            refCompany.child("Order/OrderBySale").child(userEmail).child("OutRoute").child(orderPushKey).setValue(null);
                            dialog.dismiss();

                        }
                    });

                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            refCompany.child("Order/OrderBySale").child(userEmail).child("Approved").child(orderPushKey).child("orderName").setValue(orderDetail.getOrderName());
                            Constants.refDatabase.child(emailLogin+"/Order").child("Approved").child(orderPushKey).child("orderName").setValue(orderDetail.getOrderName()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Constants.refDatabase.child(emailLogin+"/Order").child("OutRoute").child(orderPushKey).setValue(null);
                                    refCompany.child("Order/OrderBySale").child(userEmail).child("OutRoute").child(orderPushKey).setValue(null);

                                }
                            });

                        }
                    }).show();
                }
            });
        }
    }

    public class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView employeeName;

        public EmployeeViewHolder(View itemView) {
            super(itemView);
            employeeName = (TextView) itemView.findViewById(R.id.tv_item_client_sale_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    if (dialogEmployeeList != null) dialogEmployeeList.dismiss();
                    int position = getLayoutPosition();
                    Employee employee = adapterFirebaseEmployee.getItem(position);
                    tvEmployeeName.setText(employee.getEmployeeName());

                }
            });

        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.tv_item_product_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    if (dialogProductList != null) dialogProductList.dismiss();
                    int position = getLayoutPosition();
                    DatabaseReference keyRef = adapterFirebaseProduct.getRef(position);
                    String keyString = keyRef.getKey();

                    final Product p = adapterFirebaseProduct.getItem(position);

                    tvChooseProduct.setText(p.getProductName());
                }
            });

        }
    }
    public class OrderViewHolderByTime extends RecyclerView.ViewHolder {
        TextView orderName;


        public OrderViewHolderByTime(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);


        }
    }


    private void dialogGroupList(View dialogView) {
        final RecyclerView rvClientGroup = dialogView.findViewById(R.id.rv_client_sale_group);
        rvClientGroup.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvClientGroup.setLayoutManager(linearLayoutManager1);

        adapterFirebaseClientGroup
                = new FirebaseRecyclerAdapter<Group, GroupViewHolder>(
                Group.class,
                R.layout.item_group,
                GroupViewHolder.class,
                refDatabase.child(emailLogin).child("ClientManBySale").child(employeeEmail).child("Group")
        ) {
            @Override
            public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
                return new GroupViewHolder(v);
            }

            @Override
            protected void populateViewHolder(final GroupViewHolder viewHolder, Group model, int position) {
                viewHolder.groupName.setText(model.getGroupName());

            }
        };

        rvClientGroup.setAdapter(adapterFirebaseClientGroup);
        adapterFirebaseClientGroup.notifyDataSetChanged();

    }

    private void dialogClientList(DatabaseReference ref) {

        adapterFirebaseClient = new FirebaseRecyclerAdapter<Client, ClientViewHolder>(
                Client.class,
                R.layout.item_client_sale,
                ClientViewHolder.class,
                ref
        ) {
            @Override
            public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_sale, parent, false);
                return new ClientViewHolder(v);
            }

            @Override
            protected void populateViewHolder(ClientViewHolder viewHolder, Client model, int position) {
                viewHolder.clientName.setText(model.getClientName());
                viewHolder.clientName.setTextColor(getResources().getColor(R.color.colorAccent));
                viewHolder.clientAddress.setText(model.getClientAddress());

            }
        };

        rvClientList.setAdapter(adapterFirebaseClient);
        adapterFirebaseClient.notifyDataSetChanged();

    }

    public class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView clientName, clientAddress, clientMeet, clientSale, clientGroup, clientEdit;

        public ClientViewHolder(View itemView) {
            super(itemView);
            clientName = (TextView) itemView.findViewById(R.id.tv_item_client_sale_name);
            clientAddress = itemView.findViewById(R.id.tv_item_client_sale_address);
            clientMeet = itemView.findViewById(R.id.tv_client_sale_meet);
            clientSale = itemView.findViewById(R.id.tv_client_sale_view);
            clientGroup = itemView.findViewById(R.id.tv_client_sale_group);
            clientEdit = itemView.findViewById(R.id.tv_client_sale_edit);

            clientMeet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                    builder.setMessage("Bạn muốn xác nhận đang có mặt tại nơi của khách hàng?");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            final String timeStamp = Calendar.getInstance().getTime().getTime() + "";
                            int position = getLayoutPosition();
                            final Client client = adapterFirebaseClient.getItem(position);
                            final String clientCode = client.getClientCode();
                            final Client clientMeet = new Client(clientCode,client.getClientName(),client.getClientType(),client.getClientAddress(),timeStamp);

                            refDatabase.child(emailLogin).child("Client").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("map")) {
                                        refDatabase.child(emailLogin).child("Client").child(clientCode).child("map").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                MapModel map = dataSnapshot.getValue(MapModel.class);

                                                SimpleLocation.Point clientPoint = new SimpleLocation.Point(Double.parseDouble(map.getLatitude())
                                                        , Double.parseDouble(map.getLongitude()));

                                                SimpleLocation.Point salesPoint = new SimpleLocation.Point(latitude, longitude);
                                                double currentDistance = SimpleLocation.calculateDistance(clientPoint, salesPoint);

                                                if (currentDistance > 30) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                                                    builder.setMessage("Bạn đang ở quá xa vị trí của khách hàng!");
                                                } else {
                                                    refDatabase.child(emailLogin).child("SalesManagement/Meet").child(employeeEmail)
                                                            .child(timeStamp).setValue(clientMeet);
                                                    Toast.makeText(getApplicationContext(), "Xác nhận thăm viếng khách hàng thành công!",
                                                            Toast.LENGTH_LONG).show();
                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Không thể xác nhận do thiếu dữ liệu khách hàng!",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }).show();

                }
            });

            clientSale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    int position = getLayoutPosition();
                    final Client client = adapterFirebaseClient.getItem(position);
                    final String clientCode = client.getClientCode();

                    orderClientFilterDialog(clientCode);
                }
            });

            clientGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    int position = getLayoutPosition();
                    final Client client = adapterFirebaseClient.getItem(position);
                    final String clientCode = client.getClientCode();

                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_choose_group, null);
                    builder.setView(dialogView);
                    builder.setMessage("Chọn nhóm cho KH: " + "\n" + clientName);

                    final Dialog dialog = builder.create();
                    dialog.show();

                    final RecyclerView rvClientGroup = dialogView.findViewById(R.id.rv_choose_group);
                    rvClientGroup.setHasFixedSize(true);
                    StaggeredGridLayoutManager linearLayoutManager1 = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL);
                    rvClientGroup.setLayoutManager(linearLayoutManager1);

                    adapterFirebaseClientGroup
                            = new FirebaseRecyclerAdapter<Group, GroupViewHolder>(
                            Group.class,
                            R.layout.item_group,
                            GroupViewHolder.class,
                            refDatabase.child(emailLogin).child("ClientManBySale").child(employeeEmail).child("Group")
                    ) {
                        @Override
                        public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
                            return new GroupViewHolder(v);
                        }

                        @Override
                        protected void populateViewHolder(final GroupViewHolder viewHolder, Group model, int position) {
                            viewHolder.groupName.setText(model.getGroupName());
                            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                    view.startAnimation(buttonClick);

                                    int position = viewHolder.getAdapterPosition();
                                    final Group group = adapterFirebaseClientGroup.getItem(position);
                                    final String groupName = group.getGroupName();

                                    if (groupName.equals("Tất cả")) {
                                        Toast.makeText(getApplicationContext(), "Không thể xếp vào nhóm này!", Toast.LENGTH_LONG).show();
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                                        builder.setTitle("Nhóm của KH này là: " + groupName + "?");

                                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                refDatabase.child(emailLogin).child("ClientManBySale")
                                                        .child(employeeEmail).child(groupName).child(clientCode).setValue(client);
                                            }
                                        }).show();
                                    }


                                }
                            });

                        }
                    };

                    rvClientGroup.setAdapter(adapterFirebaseClientGroup);
                    adapterFirebaseClientGroup.notifyDataSetChanged();

                }
            });

            clientEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    int position = getLayoutPosition();
                    final Client client = adapterFirebaseClient.getItem(position);
                    String clientCode = client.getClientCode();

                    refDatabase.child(emailLogin).child("Client").child(clientCode).child("clientPhone").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String clientPhone = dataSnapshot.getValue().toString();
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clientPhone));
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

        }
    }

    private class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;

        GroupViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.tv_item_group_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    int position = getAdapterPosition();
                    String groupName = adapterFirebaseClientGroup.getItem(position).getGroupName();
                    if (groupName.equals("Tất cả")) {
                        DatabaseReference ref = refDatabase.child(emailLogin)
                                .child("ClientBySale").child(employeeEmail);
                        dialogClientList(ref);
                    } else {
                        DatabaseReference ref = refDatabase.child(emailLogin)
                                .child("ClientManBySale").child(employeeEmail).child(groupName);
                        dialogClientList(ref);
                    }

                }


            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    view.startAnimation(buttonClick);

                    int position = getAdapterPosition();
                    final String groupKey = adapterFirebaseClientGroup.getRef(position).getKey();
                    String groupName = adapterFirebaseClientGroup.getItem(position).getGroupName();

                    if (groupName.equals("Tất cả")) {
                        Toast.makeText(getApplicationContext(), "Bạn không thể tạo/xóa nhóm này!", Toast.LENGTH_LONG).show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                        builder.setTitle("Tạo/Đổi tên nhóm khách hàng");

                        final EditText edtName = new EditText(OrderManActivity.this);
                        edtName.setHeight(80);
                        edtName.setWidth(200);
                        edtName.setGravity(Gravity.CENTER);
                        edtName.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        edtName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                        edtName.setHint("Nhập tên nhóm");

                        builder.setView(edtName);

                        builder.setPositiveButton("Tạo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = edtName.getText().toString();
                                if (TextUtils.isEmpty(name)) {
                                    Toast.makeText(getApplicationContext(), "Vui lòng nhập tên nhóm", Toast.LENGTH_LONG).show();
                                } else {
                                    Group group = new Group(name);
                                    refDatabase.child(emailLogin).child("ClientManBySale").child(employeeEmail).child("Group").push().setValue(group);
                                }
                            }
                        }).setNegativeButton("Đổi tên", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = edtName.getText().toString();
                                if (TextUtils.isEmpty(name)) {
                                    Toast.makeText(getApplicationContext(), "Vui lòng nhập tên nhóm", Toast.LENGTH_LONG).show();
                                } else {

                                    Group group = new Group(name);
                                    refDatabase.child(emailLogin).child("ClientManBySale").child(employeeEmail).child("Group").child(groupKey).setValue(group);
                                }

                            }
                        }).show();
                    }

                    return false;
                }
            });


        }
    }

    @Override
    public void onBackPressed() {
        b.clear();
        startActivity(new Intent(this,MainActivity.class));
        super.onBackPressed();
    }
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }


}

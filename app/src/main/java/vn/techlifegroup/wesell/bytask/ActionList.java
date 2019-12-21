package vn.techlifegroup.wesell.bytask;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import de.hdodenhof.circleimageview.CircleImageView;
import vn.techlifegroup.wesell.LoginActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.list.AccountList;
import vn.techlifegroup.wesell.list.ClientListBySaleTeam;
import vn.techlifegroup.wesell.list.SaleList;
import vn.techlifegroup.wesell.model.Client;
import vn.techlifegroup.wesell.model.Employee;
import vn.techlifegroup.wesell.model.Functions;
import vn.techlifegroup.wesell.utils.AdapterFunctions;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class ActionList extends AppCompatActivity {
    private RecyclerView rvAction;
    private FirebaseRecyclerAdapter<Employee,EmployeeViewHolder> adapter ;
    private ImageView ivOrder, ivClient,ivPromotion,ivAnnouncement,ivTeam,
            ivOrderSup,ivPromotionSup,ivAnnoucementSup,ivSaleLogout,ivSupLogout,ivClientSup,
            ivSupASM,ivPromotionASM,ivReportASM,ivAnnouncementASM,ivLogoutASM,ivSaleASM,
            ivCreateAcc,ivLogoutAdmin,ivAdminProduct,ivAdminOrder,ivAdminPayroll,ivAdminPromotion;

    private String emailLogin,userEmail;
    private boolean saleMan,supervisor,asm,admin,rsm;
    private LinearLayout lnSup,lnSaleMan,lnASM,lnAdmin;

    private String currentDay,date,day,year,month;
    private TextView tvName,tvRole;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPref;
    private int pos;
    private BarChart barTime;

    DatabaseReference refCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);

        Intent it = this.getIntent();
        emailLogin = it.getStringExtra("EmailLogin");
        saleMan = it.getBooleanExtra("SaleMan", false);
        supervisor = it.getBooleanExtra("Supervisor",false);
        asm = it.getBooleanExtra("ASM",false);
        admin = it.getBooleanExtra("Admin",false);
        rsm = it.getBooleanExtra("RSM", false);

        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();


        DateTime dt = new DateTime();
        day = dt.dayOfWeek().getAsText();
        final String week = dt.weekOfWeekyear().getAsText();
        year = dt.getYear()+"";
        month = dt.getMonthOfYear()+"";

        barTime = findViewById(R.id.barchart_action_list);


         tvRole = findViewById(R.id.tv_activity_main_hello);

    }

    @Override
    protected void onResume() {
        super.onResume();

        refDatabase.child("Employee").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                TextView tvHello = findViewById(R.id.tv_activity_main_hello);

                Employee employee = dataSnapshot.getValue(Employee.class);

                String name = employee.getEmployeeName();

                tvHello.setText("Xin chào " + name);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(saleMan){

            getDay();

            getSaleFunc();

            getSaleRev();



        }

        if(supervisor){
            getDay();
            getSupFunc();
            //actionSupOnClick();
            getSaleRev();
        }


    }

    private void getSupFunc() {
        List<Functions> functions = Arrays.asList(
                new Functions(R.drawable.icon_func_team,"đội ngũ"),
                new Functions(R.drawable.icon_func_shop,"khách hàng"),
                new Functions(R.drawable.icon_cart,"đơn hàng"),
                new Functions(R.drawable.icon_promotion,"chương trình"),
                new Functions(R.drawable.icon_chat,"thông báo")

        );

        RecyclerView rvFunction = findViewById(R.id.rv_function);

        rvFunction.setHasFixedSize(true);

        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
        LinearLayoutManager linearLayoutManagerFunc = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFunction.setLayoutManager(linearLayoutManagerFunc);

        AdapterFunctions adapterFunctions = new AdapterFunctions(getApplicationContext(), functions, ActionList.this,"Supervisor");
        rvFunction.setAdapter(adapterFunctions);
        adapterFunctions.notifyDataSetChanged();

        FirebaseRecyclerAdapter<Employee, EmployeeViewHolder> adapterDetail;
        RecyclerView rvClientList;

        rvClientList = findViewById(R.id.rv_client_list);
        rvClientList.setHasFixedSize(true);


        rvClientList = findViewById(R.id.rv_client_list);
        rvClientList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManagerClient = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvClientList.setLayoutManager(linearLayoutManagerClient);


        adapterDetail = new FirebaseRecyclerAdapter<Employee, EmployeeViewHolder>(
                Employee.class,
                R.layout.item_saleman,
                EmployeeViewHolder.class,
                refDatabase.child("SaleManBySup").child(userEmail).child("Tất cả")
        ) {
            @Override
            public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saleman, parent, false);
                return new EmployeeViewHolder(v);
            }


            @Override
            protected void populateViewHolder(EmployeeViewHolder viewHolder, Employee model, int position) {
                viewHolder.name.setText(model.getEmployeeName());

            }
        };

        rvClientList.setAdapter(adapterDetail);
        adapterDetail.notifyDataSetChanged();


    }

    private void getSaleRev() {

        final List<BarEntry> monthEntries = new ArrayList<>();

        refDatabase.child("TotalBySale").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                for(DataSnapshot itemTime:snapTimeSale){

                    String timeKey = itemTime.getKey();

                    String value = itemTime.getValue().toString();

                    if(timeKey.length()>7 ){

                        if(timeKey.contains(year+"-"+month)){

                            monthEntries.add(new BarEntry(Integer.parseInt(timeKey.substring(timeKey.lastIndexOf("-")+1)), Float.parseFloat(value)));

                            BarDataSet set = new BarDataSet(monthEntries,"Doanh số theo ngày");

                            BarData data = new BarData(set);

                            Description description = new Description();
                            description.setText("");

                            barTime.getAxisRight().setDrawGridLines(false);
                            barTime.getAxisLeft().setDrawGridLines(false);
                            barTime.getXAxis().setDrawGridLines(false);
                            barTime.getXAxis().setGranularityEnabled(true);
                            //barTime.getXAxis().setDrawLabels(false);
                            barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                            //barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barEntryLabels));
                            barTime.setDescription(description);
                            barTime.getAxisRight().setEnabled(false);
                            barTime.setTouchEnabled(true);
                            //barTime.setMarker(mv);
                            barTime.setData(data);
                            barTime.animateXY(1000,2000);
                            barTime.setFitBars(true); // make the x-axis fit exactly all bars
                            barTime.invalidate(); // refresh

                        }
                        //barEntryLabels.add(timeKey.substring(5));

                    }


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getSaleFunc() {

        List<Functions> functions = Arrays.asList(
                new Functions(R.drawable.icon_cart,"đơn hàng"),
                new Functions(R.drawable.icon_func_shop,"khách hàng"),
                new Functions(R.drawable.icon_promotion,"chương trình"),
                new Functions(R.drawable.icon_chat,"thông báo")
        );

        RecyclerView rvFunction = findViewById(R.id.rv_function);

        rvFunction.setHasFixedSize(true);

        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL);
        LinearLayoutManager linearLayoutManagerFunc = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvFunction.setLayoutManager(linearLayoutManagerFunc);

        AdapterFunctions adapterFunctions = new AdapterFunctions(getApplicationContext(), functions, ActionList.this,"SaleMan");
        rvFunction.setAdapter(adapterFunctions);
        adapterFunctions.notifyDataSetChanged();

        FirebaseRecyclerAdapter<Client, ClientViewHolder> adapterDetail;
        RecyclerView rvClientList;

        rvClientList = findViewById(R.id.rv_client_list);
        rvClientList.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManagerClient = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvClientList.setLayoutManager(linearLayoutManagerClient);


        adapterDetail = new FirebaseRecyclerAdapter<Client, ClientViewHolder>(
                Client.class,
                R.layout.item_client_route,
                ClientViewHolder.class,
                refDatabase.child("SaleRoute").child(userEmail).child(currentDay)
        ) {
            @Override
            public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_route, parent, false);
                return new ClientViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ClientViewHolder viewHolder, Client model, int position) {
                viewHolder.clientName.setText(model.getClientName());
                viewHolder.circleClient.setImageDrawable(getResources().getDrawable(R.drawable.icon_client2));

            }
        };

        rvClientList.setAdapter(adapterDetail);
        adapterDetail.notifyDataSetChanged();
    }

    private void getSupSale() {
        final HashMap<String,Float> salesMonth = new HashMap<>();
        final HashMap<String,Float> salesYear = new HashMap<>();

        refCompany.child("SaleManBySup").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                final long supCount = dataSnapshot.getChildrenCount();

                final float[] totalDaySale = {0};
                final float[] totalMonthSale = {0};
                final float[] totalYearSale = {0};

                int i = 0;
                for(DataSnapshot itemSale:snapSale){
                    i ++;
                    final String saleEmail = itemSale.getKey();
                    final String saleName = itemSale.getValue(Employee.class).getEmployeeName();
                    final String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);

                    refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(year+"-"+month+"-"+day)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month+"-"+day).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalDaySale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year+"-"+month+"-"+day).setValue(totalDaySale[0] +"");

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            if(dataSnapshot.hasChild(year+"-"+month)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalMonthSale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year+"-"+month).setValue(totalMonthSale[0] +"");

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            if(dataSnapshot.hasChild(year)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalYearSale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year).setValue(totalYearSale[0] +"");

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


                    final int finalI = i;
                    refCompany.child("TotalProductBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChildren()){

                                Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                                for(DataSnapshot itemProductSale:snapProduct){
                                    final String productCode = itemProductSale.getKey();
                                    DatabaseReference refProduct = itemProductSale.getRef();
                                    refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if(dataSnapshot.hasChild(year+"-"+month)){

                                                refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(salesMonth.containsKey(productCode)){
                                                            float updateSale = currentSale + salesMonth.get(productCode);
                                                            salesMonth.put(productCode,updateSale);

                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year+"-"+month).setValue(updateSale+"");


                                                        }else{
                                                            salesMonth.put(productCode,currentSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year+"-"+month).setValue(salesMonth.get(productCode)+"");

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }

                                            if(dataSnapshot.hasChild(year)){

                                                refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(salesYear.containsKey(productCode)){
                                                            float updateSale = currentSale + salesYear.get(productCode);
                                                            salesYear.put(productCode,updateSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year).setValue(salesYear.get(productCode)+"");


                                                        }else{
                                                            salesYear.put(productCode,currentSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year).setValue(salesYear.get(productCode)+"");

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

    private void getASMSale() {
        final HashMap<String,Float> salesMonth = new HashMap<>();
        final HashMap<String,Float> salesYear = new HashMap<>();

        refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                final long supCount = dataSnapshot.getChildrenCount();

                final float[] totalDaySale = {0};
                final float[] totalMonthSale = {0};
                final float[] totalYearSale = {0};

                int i = 0;
                for(DataSnapshot itemSale:snapSale){
                    i ++;
                    final String saleEmail = itemSale.getKey();
                    final String saleName = itemSale.getValue(Employee.class).getEmployeeName();
                    final String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);

                    refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(year+"-"+month+"-"+day)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month+"-"+day).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalDaySale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year+"-"+month+"-"+day).setValue(totalDaySale[0] +"");

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            if(dataSnapshot.hasChild(year+"-"+month)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalMonthSale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year+"-"+month).setValue(totalMonthSale[0] +"");

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            if(dataSnapshot.hasChild(year)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalYearSale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year).setValue(totalYearSale[0] +"");

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


                    final int finalI = i;
                    refCompany.child("TotalProductBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChildren()){

                                Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                                for(DataSnapshot itemProductSale:snapProduct){
                                    final String productCode = itemProductSale.getKey();
                                    DatabaseReference refProduct = itemProductSale.getRef();
                                    refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if(dataSnapshot.hasChild(year+"-"+month)){

                                                refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(salesMonth.containsKey(productCode)){
                                                            float updateSale = currentSale + salesMonth.get(productCode);
                                                            salesMonth.put(productCode,updateSale);

                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year+"-"+month).setValue(updateSale+"");


                                                        }else{
                                                            salesMonth.put(productCode,currentSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year+"-"+month).setValue(salesMonth.get(productCode)+"");

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }

                                            if(dataSnapshot.hasChild(year)){

                                                refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(salesYear.containsKey(productCode)){
                                                            float updateSale = currentSale + salesYear.get(productCode);
                                                            salesYear.put(productCode,updateSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year).setValue(salesYear.get(productCode)+"");


                                                        }else{
                                                            salesYear.put(productCode,currentSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year).setValue(salesYear.get(productCode)+"");

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


    public class EmployeeViewHolder extends RecyclerView.ViewHolder {
         TextView name;

        public EmployeeViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.tv_item_sale_circle_name);

        }
    }

    public class ClientViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleClient;
        TextView clientName;

        public ClientViewHolder(View itemView) {
            super(itemView);
            circleClient = (CircleImageView) itemView.findViewById(R.id.profile_image);
            clientName = itemView.findViewById(R.id.tv_item_client_circle_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), "Chon khach hang", Toast.LENGTH_SHORT).show();
                }
            });


        }
    }

    private void getDay() {

        DateTime dt = new DateTime();
        day = dt.dayOfWeek().getAsText();

        date = dt.toString().substring(0, 10);
        switch (day) {
            case "Thứ Hai":
                currentDay = "a_Thứ hai";
                break;
            case "Thứ Ba":
                currentDay = "b_Thứ ba";
                break;
            case "Thứ Tư":
                currentDay = "c_Thứ tư";
                break;
            case "Thứ Năm":
                currentDay = "d_Thứ năm";
                break;
            case "Thứ Sáu":
                currentDay = "e_Thứ sáu";
                break;
            case "Thứ Bảy":
                currentDay = "f_Thứ bảy";
                break;
            case "Monday":
                currentDay = "a_Thứ hai";
                break;
            case "Tuesday":
                currentDay = "b_Thứ ba";
                break;
            case "Wednesday":
                currentDay = "c_Thứ tư";
                break;
            case "Thursday":
                currentDay = "d_Thứ năm";
                break;
            case "Friday":
                currentDay = "e_Thứ sáu";
                break;
            case "Saturday":
                currentDay = "f_Thứ bảy";
                break;
            default:
                currentDay = "a_Thứ hai";
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_log_out){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(),LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}

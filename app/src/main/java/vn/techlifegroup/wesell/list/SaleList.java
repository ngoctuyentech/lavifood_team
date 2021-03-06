package vn.techlifegroup.wesell.list;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import vn.techlifegroup.wesell.MainActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.bytask.SaleRoute;
import vn.techlifegroup.wesell.bytask.TeamMan;
import vn.techlifegroup.wesell.model.Client;
import vn.techlifegroup.wesell.model.Employee;
import vn.techlifegroup.wesell.model.Group;
import vn.techlifegroup.wesell.model.KPI;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;

import static vn.techlifegroup.wesell.utils.Constants.buttonClick;
import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class SaleList extends AppCompatActivity {

    private RecyclerView rvGroup,rvDetail;
    private FirebaseRecyclerAdapter<Group, GroupViewHolder> adapterGroup ;
    private FirebaseRecyclerAdapter<Employee, SaleViewHolder> adapterDetail,adaperSaleMan ;
    private FirebaseRecyclerAdapter<Client, ClientViewHolder> adapterClient ;

    private String emailLogin,userEmail,choosenMonth,exportClickName;
    private ImageView ivAddGroup;
    private int itemCat;
    private FloatingActionButton fabAddClient;
    private Button btnRoute;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean asm,supervisor;
    private ProgressBar barKPISale, barKPINewClient;
    private String currentDay,date,day,year,month;
    DatabaseReference refCompany;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_list_by_sup);

        Intent it = this.getIntent();
        emailLogin = it.getStringExtra("EmailLogin");
        asm = it.getBooleanExtra("ASM", false);
        supervisor = it.getBooleanExtra("Supervisor", false);

        refCompany = refDatabase;

        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        btnRoute = findViewById(R.id.btn_sale_client_route);

        DateTime dt = new DateTime();
        day = dt.dayOfWeek().getAsText();
        final String week = dt.weekOfWeekyear().getAsText();
        year = dt.getYear()+"";
        month = dt.getMonthOfYear()+"";

        if(supervisor){
            DatabaseReference refSale = refDatabase.child("SaleManBySup").child(userEmail).child("Tất cả");
            DatabaseReference refGroup = refDatabase.child("SaleManBySup").child(userEmail).child("Group");
            groupList(refGroup);
            detailList(refSale);
            gotoRoute();

        }

        addGroup();


        getKPI();
    }


    private void getKPI() {
        barKPISale = findViewById(R.id.bar_sale_list_kpi_sale);
        barKPINewClient = findViewById(R.id.bar_sale_list_kpi_new);
        final TextView tvKPISale = findViewById(R.id.tv_sale_list_kpi_sale);
        final TextView tvKPINew = findViewById(R.id.tv_sale_list_kpi_new);

        refDatabase.child("KPI").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapKPI = dataSnapshot.getChildren();
                for (DataSnapshot itemKPI : snapKPI) {
                    KPI kpi = itemKPI.getValue(KPI.class);
                    String kpiTime = kpi.getKpiTime();
                    String kpiType = kpi.getKpiType();

                    final NumberFormat numberFormat = NumberFormat.getPercentInstance();
                    numberFormat.setMaximumFractionDigits(0);

                    if(kpiTime.equals(year+"-"+month) && kpiType.equals("TotalSale")){

                        final int kpiTarget = Integer.parseInt(kpi.getKpiTarget());

                        refDatabase.child("TotalBySale").child(userEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                float kpiReach = Float.parseFloat(dataSnapshot.getValue().toString());
                                float percentReach = kpiReach/kpiTarget;

                                //Toast.makeText(getApplicationContext(), percentReach+"", Toast.LENGTH_LONG).show();

                                String formattedString = numberFormat.format(percentReach);
                                tvKPISale.setText(formattedString);

                                barKPISale.setMax(kpiTarget);
                                //barKPISale.setMin(0);
                                barKPISale.setProgress((int) kpiReach);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                    if(kpiTime.equals(year+"-"+month) && kpiType.equals("New Client")){
                        float kpiReach = Float.parseFloat(kpi.getKpiReach());
                        float kpiTarget = Float.parseFloat(kpi.getKpiTarget());
                        float percentReach = kpiReach/kpiTarget;

                        String formattedString = numberFormat.format(percentReach);

                        tvKPINew.setText(formattedString);
                        barKPINewClient.setMax((int) kpiTarget);
                        barKPINewClient.setProgress((int) kpiReach);

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void gotoRoute() {

        btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent it = new Intent(getApplicationContext(), TeamMan.class);
                it.putExtra("EmailLogin",emailLogin);

                startActivity(it);
            }
        });
    }

    private void addSale() {
        fabAddClient = findViewById(R.id.fab_sale_client);
        fabAddClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                createNewSaleDialog();
            }
        });
    }

    private void createNewSaleDialog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage("Tạo Nhân viên bán hàng mới");

        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "Vui lòng liên hệ với admin để tạo tài khoản người dùng mới!", Toast.LENGTH_LONG).show();
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


    private void addGroup() {
        ivAddGroup = findViewById(R.id.iv_sale_list_add_group);
        ivAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                AlertDialog.Builder builder = new AlertDialog.Builder(SaleList.this);
                builder.setMessage("Thêm nhóm nhân viên?");

                final EditText input = new EditText(SaleList.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                builder.setView(input);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String newGroupName = input.getText().toString();
                        if(TextUtils.isEmpty(newGroupName)){
                            Toast.makeText(getApplicationContext(), "Vui lòng nhập tên nhóm!", Toast.LENGTH_LONG).show();

                        }else if (newGroupName.equals("Tất cả")){
                            Toast.makeText(getApplicationContext(), "Không thể đặt tên này!", Toast.LENGTH_LONG).show();

                        }
                       else{
                            if(supervisor)
                                refDatabase.child("SaleManBySup").child(userEmail).child("Group").push().child("groupName").setValue(newGroupName);
                            else
                                refDatabase.child("SupByASM").child(userEmail).child("Group").push().child("groupName").setValue(newGroupName);
                        }

                    }
                }).show();
            }
        });
    }

    private void groupList(DatabaseReference refGroup) {
        rvGroup = (RecyclerView) findViewById(R.id.rv_sale_list_group);
        rvGroup.setHasFixedSize(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        rvGroup.setLayoutManager(linearLayoutManager);


        adapterGroup = new FirebaseRecyclerAdapter<Group, GroupViewHolder>(
                Group.class,
                R.layout.item_group,
                GroupViewHolder.class,
                refGroup
        ) {
            @Override
            public void onBindViewHolder(@NonNull GroupViewHolder viewHolder, int position) {

                super.onBindViewHolder(viewHolder, position);
                viewHolder.groupName.setBackground((position==itemCat)? getResources().getDrawable(R.drawable.border_drug_cat_accent):getResources().getDrawable(R.drawable.border_drug_cat));

            }

            @NonNull
            @Override
            public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
                return new GroupViewHolder(v);
            }


            @Override
            protected void populateViewHolder(GroupViewHolder viewHolder, Group model, int position) {
                viewHolder.groupName.setText(model.getGroupName());
            }
        };


        rvGroup.setAdapter(adapterGroup);
        adapterGroup.notifyDataSetChanged();
    }
    private void detailList(DatabaseReference refClient) {

        rvDetail = (RecyclerView) findViewById(R.id.rv_sale_list_detail);
        rvDetail.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        rvDetail.setLayoutManager(staggeredGridLayoutManager);


        adapterDetail = new FirebaseRecyclerAdapter<Employee, SaleViewHolder>(
                Employee.class,
                R.layout.item_saleman,
                SaleViewHolder.class,
                refClient
        ) {
            @Override
            public SaleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saleman, parent, false);
                return new SaleViewHolder(v);
            }


            @Override
            protected void populateViewHolder(SaleViewHolder viewHolder, Employee model, int position) {
                viewHolder.saleName.setText(model.getEmployeeName());

                viewHolder.circleSale.setImageDrawable(getResources().getDrawable(R.drawable.icon_saleman));
            }
        };

        rvDetail.setAdapter(adapterDetail);
        adapterDetail.notifyDataSetChanged();
    }


    public class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;

        public GroupViewHolder(View itemView) {
            super(itemView);
            groupName = (TextView) itemView.findViewById(R.id.tv_item_group_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);

                    itemCat = getAdapterPosition();
                    adapterGroup.notifyDataSetChanged();
                    Group group = adapterGroup.getItem(itemCat);
                    String groupName = group.getGroupName();
                    if(supervisor)
                    detailList(refDatabase.child("SaleManBySup").child(userEmail).child(groupName));
                    else
                        detailList(refDatabase.child("SupByASM").child(userEmail).child(groupName));

                }
            });



        }
    }
    public class SaleViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleSale;
        TextView saleName;

        public SaleViewHolder(View itemView) {
            super(itemView);
            circleSale = (CircleImageView) itemView.findViewById(R.id.profile_image);
            saleName = itemView.findViewById(R.id.tv_item_sale_circle_name);

            DateTime dt = new DateTime();
            final String month = dt.getMonthOfYear()+"";
            final String year = dt.getYear()+"";

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);

                    if(supervisor)
                        supClick();
                    else
                        asmClick();


                }

                private void asmClick() {
                    int position = getAdapterPosition();
                    final Employee employee = adapterDetail.getItem(position);
                    final String saleEmail = employee.getEmployeeEmail();

                    exportClickName = "ThisMonth";

                    AlertDialog.Builder builder = new AlertDialog.Builder(SaleList.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_saleman_detail,null);
                    builder.setView(dialogView);

                    final Dialog dialog = builder.create();
                    dialog.show();
                    final TextView tvName = dialogView.findViewById(R.id.tv_sale_detail_name);

                    refCompany.child("Employee").child(saleEmail).child("employeeName").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            tvName.setText(dataSnapshot.getValue().toString());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    final BarChart barTime = (BarChart)dialogView.findViewById(R.id.bar_sale_detail);

                    final Button yearSale = dialogView.findViewById(R.id.btn_sale_detail_yearsale);
                    final Button monthSale = dialogView.findViewById(R.id.btn_sale_month_sale);
                    final Button thisMonthSale = dialogView.findViewById(R.id.btn_sale_thismonth);


                    yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                    monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                    thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

                    yearSale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            exportClickName = "YearSale";

                            yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                            monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                            thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));

                            final List<BarEntry> yearEntries = new ArrayList<>();
                            final ArrayList<String> barEntryLabels = new ArrayList<>();;

                            refDatabase.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                                    for(DataSnapshot itemTime:snapTimeSale){

                                        String timeKey = itemTime.getKey();

                                        String value = itemTime.getValue().toString();

                                        if(timeKey.length()<5){

                                            barEntryLabels.add(timeKey);
                                            yearEntries.add(new BarEntry(Integer.parseInt(timeKey), Float.parseFloat(value)));

                                            BarDataSet set = new BarDataSet(yearEntries,"Doanh số theo năm");

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


                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                    monthSale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);
                            exportClickName = "MonthSale";
                            yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                            monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                            thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));

                            final List<BarEntry> monthEntries = new ArrayList<>();

                            refDatabase.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                                    for(DataSnapshot itemTime:snapTimeSale){

                                        String timeKey = itemTime.getKey();

                                        String value = itemTime.getValue().toString();

                                        if(timeKey.length()>5 && timeKey.length()<8){


                                            //barEntryLabels.add(timeKey.substring(5));

                                            monthEntries.add(new BarEntry(Integer.parseInt(timeKey.substring(5)), Float.parseFloat(value)));

                                            BarDataSet set = new BarDataSet(monthEntries,"Doanh số theo tháng");

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


                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                    thisMonthSale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);
                            exportClickName = "DaySale";
                            yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                            monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                            thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                            DateTime dt = new DateTime();
                            final String month = dt.getMonthOfYear()+"";
                            final String year = dt.getYear()+"";

                            final List<BarEntry> monthEntries = new ArrayList<>();

                            refDatabase.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    });

                    final List<BarEntry> monthEntries = new ArrayList<>();


                    refDatabase.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                            for(DataSnapshot itemTime:snapTimeSale){

                                String timeKey = itemTime.getKey();

                                String value = itemTime.getValue().toString();

                                if(timeKey.length()>7 ){

                                    if(timeKey.contains(year+"-"+month)){

                                        monthEntries.add(new BarEntry(Integer.parseInt(timeKey.substring(timeKey.lastIndexOf("-")+1)), Float.parseFloat(value)));

                                        BarDataSet set = new BarDataSet(monthEntries,"Doanh số theo tháng");

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

                    final ProgressBar barKPISale = dialogView.findViewById(R.id.bar_dialog_saleman_kpi_sale);
                    final ProgressBar barKPINewClient = dialogView.findViewById(R.id.bar_dialog_saleman_kpi_new);

                    final TextView tvKPISale = dialogView.findViewById(R.id.tv_dialog_saleman_kpi_sale);
                    final TextView tvKPINew = dialogView.findViewById(R.id.tv_dialog_saleman_kpi_new);

                    refDatabase.child("KPI").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapKPI = dataSnapshot.getChildren();
                            for (DataSnapshot itemKPI : snapKPI) {
                                KPI kpi = itemKPI.getValue(KPI.class);
                                String kpiTime = kpi.getKpiTime();
                                String kpiType = kpi.getKpiType();

                                final NumberFormat numberFormat = NumberFormat.getPercentInstance();
                                numberFormat.setMaximumFractionDigits(0);

                                if(kpiTime.equals(year+"-"+month) && kpiType.equals("TotalSale")){

                                    final int kpiTarget = Integer.parseInt(kpi.getKpiTarget());

                                    refDatabase.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            float kpiReach = Float.parseFloat(dataSnapshot.getValue().toString());
                                            float percentReach = kpiReach/kpiTarget;

                                            //Toast.makeText(getApplicationContext(), percentReach+"", Toast.LENGTH_LONG).show();

                                            String formattedString = numberFormat.format(percentReach);
                                            tvKPISale.setText(formattedString);

                                            barKPISale.setMax(kpiTarget);
                                            //barKPISale.setMin(0);
                                            barKPISale.setProgress((int) kpiReach);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }

                                if(kpiTime.equals(year+"-"+month) && kpiType.equals("New Client")){
                                    float kpiReach = Float.parseFloat(kpi.getKpiReach());
                                    float kpiTarget = Float.parseFloat(kpi.getKpiTarget());
                                    float percentReach = kpiReach/kpiTarget;

                                    String formattedString = numberFormat.format(percentReach);

                                    tvKPINew.setText(formattedString);
                                    barKPINewClient.setMax((int) kpiTarget);
                                    barKPINewClient.setProgress((int) kpiReach);

                                }
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    ImageView phone = dialogView.findViewById(R.id.iv_sale_detail_phone);
                    ImageView grouping = dialogView.findViewById(R.id.iv_sale_detail_grouping);
                    ImageView ivRoute = dialogView.findViewById(R.id.iv_sale_detail_route);
                    ImageView ivKPI = dialogView.findViewById(R.id.iv_sale_detail_kpi);
                    //ImageView ivTeam = dialogView.findViewById(R.id.iv_sale_detail_team);
                    //ivTeam.setVisibility(View.VISIBLE);


                    ivRoute.setVisibility(View.GONE);

                    ImageView ivClient = dialogView.findViewById(R.id.iv_saleman_detail_client);
                    ivClient.setVisibility(View.GONE);

                    ImageView ivSupList = dialogView.findViewById(R.id.iv_saleman_detail_employee_list);

                    ivSupList.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(SaleList.this);
                            View dialogView = getLayoutInflater().inflate(R.layout.dialog_employee_list,null);
                            builder.setView(dialogView);
                            builder.setTitle("Danh sách bán hàng");

                            final Dialog dialog = builder.create();
                            dialog.show();

                            RecyclerView rvDetail = (RecyclerView) dialogView.findViewById(R.id.recycler_dialog_employee_list);
                            rvDetail.setHasFixedSize(true);
                            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
                            rvDetail.setLayoutManager(staggeredGridLayoutManager);


                            adaperSaleMan = new FirebaseRecyclerAdapter<Employee, SaleViewHolder>(
                                    Employee.class,
                                    R.layout.item_saleman,
                                    SaleViewHolder.class,
                                    refCompany.child("SaleManBySup").child(saleEmail).child("Tất cả")
                            ) {
                                @Override
                                public SaleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saleman, parent, false);
                                    return new SaleViewHolder(v);
                                }


                                @Override
                                protected void populateViewHolder(SaleViewHolder viewHolder, Employee model, int position) {
                                    viewHolder.saleName.setText(model.getEmployeeName());

                                    viewHolder.circleSale.setImageDrawable(getResources().getDrawable(R.drawable.icon_saleman));

                                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            v.startAnimation(buttonClick);

                                        }
                                    });
                                }
                            };

                            rvDetail.setAdapter(adaperSaleMan);
                            adaperSaleMan.notifyDataSetChanged();
                        }
                    });

                    phone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            refDatabase.child(emailLogin).child("Employee").child(saleEmail).child("employeePhone").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String phone = dataSnapshot.getValue().toString();
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + phone));
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                    grouping.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(SaleList.this);
                            builder.setMessage("Đổi nhóm nhân viên?");

                            final EditText input = new EditText(SaleList.this);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT);
                            input.setLayoutParams(lp);
                            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                            input.setHint("Nhập tên nhóm");
                            builder.setView(input);

                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final String newGroupName = input.getText().toString();
                                    if(TextUtils.isEmpty(newGroupName)){
                                        Toast.makeText(getApplicationContext(), "Vui lòng nhập tên nhóm!", Toast.LENGTH_LONG).show();

                                    }else if (newGroupName.equals("Tất cả")){
                                        Toast.makeText(getApplicationContext(), "Không thể đặt tên này!", Toast.LENGTH_LONG).show();

                                    }
                                    else{
                                        refDatabase.child("SupByASM").child(userEmail).child("Group").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Iterable<DataSnapshot> snapGroup = dataSnapshot.getChildren();
                                                long itemCount = dataSnapshot.getChildrenCount();
                                                ArrayList<String> names = new ArrayList<>();

                                                int i = 0;
                                                for(DataSnapshot itemGroup:snapGroup){
                                                    i++;
                                                    Group group = itemGroup.getValue(Group.class);
                                                    String groupName = group.getGroupName();
                                                    names.add(groupName);

                                                    if(i == itemCount){
                                                        if(names.contains(newGroupName)){
                                                            refDatabase.child("SaleManBySup").child(userEmail).child(newGroupName).child(saleEmail).setValue(employee);

                                                        }else{
                                                            refDatabase.child("SupByASM").child(userEmail).child("Group").push().child("groupName").setValue(newGroupName);
                                                            refDatabase.child("SupByASM").child(userEmail).child(newGroupName).child(saleEmail).setValue(employee);
                                                        }

                                                        /*
                                                        if(groupName.equals(newGroupName)){
                                                            String keyGroup = itemGroup.getKey();
                                                            refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child(newGroupName).child(clientCode).setValue(client);
                                                        }else{
                                                            refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child("Group").push().child("groupName").setValue(newGroupName);
                                                            refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child(newGroupName).child(clientCode).setValue(client);
                                                        }
                                                        */
                                                    }



                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }

                                }
                            }).show();

                        }
                    });


                    ivRoute.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {

                                Intent it = new Intent(getApplicationContext(), SaleRoute.class);
                                it.putExtra("Supervisor", true);
                                it.putExtra("SaleEmail", saleEmail);
                                it.putExtra("EmailLogin",emailLogin);
                                startActivity(it);

                            } else {
                                ActivityCompat.requestPermissions(SaleList.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }



                        }
                    });

                    ivKPI.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(SaleList.this);
                            View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_kpi,null);
                            builder.setView(dialogView);

                            final Dialog dialog = builder.create();
                            dialog.show();



                            final EditText edtSale = dialogView.findViewById(R.id.edt_set_kpi_sale);
                            final EditText edtNew = dialogView.findViewById(R.id.edt_set_kpi_new);
                            final TextView tvThisMonthSaleKPI = dialogView.findViewById(R.id.tv_set_kpi_thismonth);

                            edtSale.addTextChangedListener(new Utils.NumberTextWatcherForThousand(edtSale));

                            refCompany.child("KPI").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapKPI = dataSnapshot.getChildren();
                                    for(DataSnapshot itemKPI:snapKPI){
                                        KPI kpi = itemKPI.getValue(KPI.class);
                                        String kpiTime = kpi.getKpiTime();
                                        String kpiType = kpi.getKpiType();

                                        if(kpiTime.equals(year+"-"+month) && kpiType.equals("TotalSale")){
                                            tvThisMonthSaleKPI.setText(Utils.convertNumber(kpi.getKpiTarget()));
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            Spinner spinMonth = dialogView.findViewById(R.id.spin_set_kpi_month);

                            spinMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if(position != 0){
                                        choosenMonth = (String) parent.getItemAtPosition(position);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });

                            Button btnDone = dialogView.findViewById(R.id.btn_set_kpi_done);

                            btnDone.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    v.startAnimation(buttonClick);

                                    final String sale = edtSale.getText().toString().replace(",","");
                                    final String newClient = edtNew.getText().toString();

                                    if(TextUtils.isEmpty(sale) || TextUtils.isEmpty(newClient)){
                                        Toast.makeText(getApplicationContext(),"Vui lòng nhập đầy đủ các chỉ tiêu", Toast.LENGTH_LONG).show();

                                    }else if(choosenMonth == null){
                                        Toast.makeText(getApplicationContext(), "Vui lòng chọn tháng!", Toast.LENGTH_LONG).show();
                                    }
                                    else{

                                        final KPI updateClientKPI = new KPI(year+"-"+choosenMonth,"New ",newClient);
                                        final KPI updateSaleKPI = new KPI(year+"-"+choosenMonth,"TotalSale",sale);

                                        refDatabase.child("KPI").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild(saleEmail)){
                                                    refDatabase.child("KPI").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Iterable<DataSnapshot> snapKPI = dataSnapshot.getChildren();
                                                            long itemCount = dataSnapshot.getChildrenCount();


                                                            int i = 0;
                                                            boolean updateSale = false;
                                                            for (DataSnapshot itemKPI:snapKPI){
                                                                i++;
                                                                KPI kpi = itemKPI.getValue(KPI.class);
                                                                String kpiType = kpi.getKpiType();
                                                                String kpiTime = kpi.getKpiTime();
                                                                DatabaseReference refKPI = itemKPI.getRef();

                                                                //Toast.makeText(getApplicationContext(), refKPI+"", Toast.LENGTH_LONG).show();

                                                                if(kpiTime.equals(year+"-"+choosenMonth) && kpiType.equals("TotalSale")){
                                                                    refKPI.setValue(updateSaleKPI);
                                                                    updateSale = true;

                                                                }

                                                                if(i == itemCount){
                                                                    if(!updateSale){

                                                                        refDatabase.child(emailLogin).child("KPI").child(saleEmail).push().setValue(updateSaleKPI);

                                                                    }

                                                                }

                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                }else{
                                                    refDatabase.child("KPI").child(saleEmail).push().setValue(updateSaleKPI);
                                                    refDatabase.child("KPI").child(saleEmail).push().setValue(updateClientKPI);

                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                                        dialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Phân bổ chỉ tiêu thành công!", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });

                        }
                    });

                }

                private void supClick(){
                    int position = getAdapterPosition();
                    final Employee employee = adapterDetail.getItem(position);
                    final String saleEmail = employee.getEmployeeEmail();

                    exportClickName = "ThisMonth";

                    AlertDialog.Builder builder = new AlertDialog.Builder(SaleList.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_saleman_detail,null);
                    builder.setView(dialogView);

                    Dialog dialog = builder.create();
                    dialog.show();
                    final TextView tvName = dialogView.findViewById(R.id.tv_sale_detail_name);

                    refCompany.child("Employee").child(saleEmail).child("employeeName").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            tvName.setText(dataSnapshot.getValue().toString());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    final BarChart barTime = (BarChart)dialogView.findViewById(R.id.bar_sale_detail);

                    final Button yearSale = dialogView.findViewById(R.id.btn_sale_detail_yearsale);
                    final Button monthSale = dialogView.findViewById(R.id.btn_sale_month_sale);
                    final Button thisMonthSale = dialogView.findViewById(R.id.btn_sale_thismonth);

                    yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                    monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                    thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

                    yearSale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            exportClickName = "YearSale";

                            yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                            monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                            thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));

                            final List<BarEntry> yearEntries = new ArrayList<>();
                            final ArrayList<String> barEntryLabels = new ArrayList<>();;

                            refDatabase.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                                    for(DataSnapshot itemTime:snapTimeSale){

                                        String timeKey = itemTime.getKey();

                                        String value = itemTime.getValue().toString();

                                        if(timeKey.length()<5){

                                            barEntryLabels.add(timeKey);
                                            yearEntries.add(new BarEntry(Integer.parseInt(timeKey), Float.parseFloat(value)));

                                            BarDataSet set = new BarDataSet(yearEntries,"Doanh số theo năm");

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


                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                    monthSale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);
                            exportClickName = "MonthSale";
                            yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                            monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                            thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));

                            final List<BarEntry> monthEntries = new ArrayList<>();

                            refDatabase.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                                    for(DataSnapshot itemTime:snapTimeSale){

                                        String timeKey = itemTime.getKey();

                                        String value = itemTime.getValue().toString();

                                        if(timeKey.length()>5 && timeKey.length()<8){


                                            //barEntryLabels.add(timeKey.substring(5));

                                            monthEntries.add(new BarEntry(Integer.parseInt(timeKey.substring(5)), Float.parseFloat(value)));

                                            BarDataSet set = new BarDataSet(monthEntries,"Doanh số theo tháng");

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


                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    });

                    thisMonthSale.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);
                            exportClickName = "DaySale";
                            yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                            monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                            thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                            DateTime dt = new DateTime();
                            final String month = dt.getMonthOfYear()+"";
                            final String year = dt.getYear()+"";

                            final List<BarEntry> monthEntries = new ArrayList<>();

                            refDatabase.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    });

                    final List<BarEntry> monthEntries = new ArrayList<>();

                    refDatabase.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                            for(DataSnapshot itemTime:snapTimeSale){

                                String timeKey = itemTime.getKey();

                                String value = itemTime.getValue().toString();

                                if(timeKey.length()>7 ){

                                    if(timeKey.contains(year+"-"+month)){

                                        monthEntries.add(new BarEntry(Integer.parseInt(timeKey.substring(timeKey.lastIndexOf("-")+1)), Float.parseFloat(value)));

                                        BarDataSet set = new BarDataSet(monthEntries,"Doanh số theo tháng");

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

                    final ProgressBar barKPISale = dialogView.findViewById(R.id.bar_dialog_saleman_kpi_sale);
                    final ProgressBar barKPINewClient = dialogView.findViewById(R.id.bar_dialog_saleman_kpi_new);

                    final TextView tvKPISale = dialogView.findViewById(R.id.tv_dialog_saleman_kpi_sale);
                    final TextView tvKPINew = dialogView.findViewById(R.id.tv_dialog_saleman_kpi_new);

                    refDatabase.child("KPI").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapKPI = dataSnapshot.getChildren();
                            for (DataSnapshot itemKPI : snapKPI) {
                                KPI kpi = itemKPI.getValue(KPI.class);
                                String kpiTime = kpi.getKpiTime();
                                String kpiType = kpi.getKpiType();

                                final NumberFormat numberFormat = NumberFormat.getPercentInstance();
                                numberFormat.setMaximumFractionDigits(0);

                                if(kpiTime.equals(year+"-"+month) && kpiType.equals("TotalSale")){

                                    final int kpiTarget = Integer.parseInt(kpi.getKpiTarget());

                                    refDatabase.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            float kpiReach = Float.parseFloat(dataSnapshot.getValue().toString());
                                            float percentReach = kpiReach/kpiTarget;

                                            String formattedString = numberFormat.format(percentReach);
                                            tvKPISale.setText(formattedString);

                                            barKPISale.setMax(kpiTarget);
                                            //barKPISale.setMin(0);
                                            barKPISale.setProgress((int) kpiReach);
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }
/*
                                if(kpiTime.equals(year+"-"+month) && kpiType.equals("New Client")){
                                    float kpiReach = Float.parseFloat(kpi.getKpiReach());
                                    float kpiTarget = Float.parseFloat(kpi.getKpiTarget());
                                    float percentReach = kpiReach/kpiTarget;

                                    String formattedString = numberFormat.format(percentReach);

                                    tvKPINew.setText(formattedString);
                                    barKPINewClient.setMax((int) kpiTarget);
                                    barKPINewClient.setProgress((int) kpiReach);

                                }

                               */
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    ImageView phone = dialogView.findViewById(R.id.iv_sale_detail_phone);
                    ImageView grouping = dialogView.findViewById(R.id.iv_sale_detail_grouping);
                    ImageView ivRoute = dialogView.findViewById(R.id.iv_sale_detail_route);
                    ImageView ivKPI = dialogView.findViewById(R.id.iv_sale_detail_kpi);
                    ImageView ivClient = dialogView.findViewById(R.id.iv_saleman_detail_client);
                    ImageView ivSupList = dialogView.findViewById(R.id.iv_saleman_detail_employee_list);

                    ivSupList.setVisibility(View.GONE);
                    ivClient.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(SaleList.this);
                            View dialogView = getLayoutInflater().inflate(R.layout.dialog_client_list,null);
                            builder.setView(dialogView);
                            builder.setTitle("Danh sách khách hàng");

                            final Dialog dialog = builder.create();
                            dialog.show();

                            RecyclerView rvList = dialogView.findViewById(R.id.rv_client_list_of_sale);
                            rvList.setHasFixedSize(true);
                            StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
                            rvList.setLayoutManager(staggeredGridLayoutManager);

                            adapterClient = new FirebaseRecyclerAdapter<Client, ClientViewHolder>(
                                    Client.class,
                                    R.layout.item_client_circle,
                                    ClientViewHolder.class,
                                    refCompany.child("ClientManBySale").child(saleEmail).child("Tất cả")
                            ) {
                                @Override
                                public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_circle, parent, false);
                                    return new ClientViewHolder(v);
                                }


                                @Override
                                protected void populateViewHolder(ClientViewHolder viewHolder, Client model, int position) {
                                    viewHolder.clientName.setText(model.getClientName());
                                    viewHolder.circleClient.setImageDrawable(getResources().getDrawable(R.drawable.icon_client2));

                                    viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            v.startAnimation(buttonClick);

                                        }
                                    });
                                    //Glide.with(getApplicationContext()).load(model.getClientUrl()).into(viewHolder.circleClient);
                                }
                            };

                            rvList.setAdapter(adapterClient);
                            adapterClient.notifyDataSetChanged();
                        }
                    });
                    phone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            refDatabase.child("Employee").child(saleEmail).child("employeePhone").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String phone = dataSnapshot.getValue().toString();
                                    Intent intent = new Intent(Intent.ACTION_DIAL);
                                    intent.setData(Uri.parse("tel:" + phone));
                                    startActivity(intent);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                    });

                    grouping.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(SaleList.this);
                            builder.setMessage("Đổi nhóm nhân viên?");

                            final EditText input = new EditText(SaleList.this);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT);
                            input.setLayoutParams(lp);
                            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                            input.setHint("Nhập tên nhóm");
                            builder.setView(input);

                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    final String newGroupName = input.getText().toString();
                                    if(TextUtils.isEmpty(newGroupName)){
                                        Toast.makeText(getApplicationContext(), "Vui lòng nhập tên nhóm!", Toast.LENGTH_LONG).show();

                                    }else if (newGroupName.equals("Tất cả")){
                                        Toast.makeText(getApplicationContext(), "Không thể đặt tên này!", Toast.LENGTH_LONG).show();

                                    }
                                    else{
                                        refDatabase.child(emailLogin).child("SaleManBySup").child(userEmail).child("Group").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Iterable<DataSnapshot> snapGroup = dataSnapshot.getChildren();
                                                long itemCount = dataSnapshot.getChildrenCount();
                                                ArrayList<String> names = new ArrayList<>();

                                                int i = 0;
                                                for(DataSnapshot itemGroup:snapGroup){
                                                    i++;
                                                    Group group = itemGroup.getValue(Group.class);
                                                    String groupName = group.getGroupName();
                                                    names.add(groupName);

                                                    if(i == itemCount){
                                                        if(names.contains(newGroupName)){
                                                            refDatabase.child(emailLogin).child("SaleManBySup").child(userEmail).child(newGroupName).child(saleEmail).setValue(employee);

                                                        }else{
                                                            refDatabase.child(emailLogin).child("SaleManBySup").child(userEmail).child("Group").push().child("groupName").setValue(newGroupName);
                                                            refDatabase.child(emailLogin).child("SaleManBySup").child(userEmail).child(newGroupName).child(saleEmail).setValue(employee);
                                                        }

                                                        /*
                                                        if(groupName.equals(newGroupName)){
                                                            String keyGroup = itemGroup.getKey();
                                                            refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child(newGroupName).child(clientCode).setValue(client);
                                                        }else{
                                                            refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child("Group").push().child("groupName").setValue(newGroupName);
                                                            refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child(newGroupName).child(clientCode).setValue(client);
                                                        }
                                                        */
                                                    }



                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }

                                }
                            }).show();

                        }
                    });

                    ivRoute.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            if (ContextCompat.checkSelfPermission(getApplicationContext(),
                                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {

                                Intent it = new Intent(getApplicationContext(), SaleRoute.class);
                                it.putExtra("Supervisor", true);
                                it.putExtra("SaleEmail", saleEmail);
                                it.putExtra("EmailLogin",emailLogin);
                                startActivity(it);

                            } else {
                                ActivityCompat.requestPermissions(SaleList.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            }

                        }
                    });

                    ivKPI.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(SaleList.this);
                            View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_kpi,null);
                            builder.setView(dialogView);

                            final Dialog dialog = builder.create();
                            dialog.show();

                            final EditText edtSale = dialogView.findViewById(R.id.edt_set_kpi_sale);
                            final EditText edtNew = dialogView.findViewById(R.id.edt_set_kpi_new);
                            final TextView tvThisMonthSaleKPI = dialogView.findViewById(R.id.tv_set_kpi_thismonth);

                            edtSale.addTextChangedListener(new Utils.NumberTextWatcherForThousand(edtSale));

                            refCompany.child("KPI").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapKPI = dataSnapshot.getChildren();
                                    for(DataSnapshot itemKPI:snapKPI){
                                        KPI kpi = itemKPI.getValue(KPI.class);
                                        String kpiTime = kpi.getKpiTime();
                                        String kpiType = kpi.getKpiType();

                                        if(kpiTime.equals(year+"-"+month) && kpiType.equals("TotalSale")){
                                            tvThisMonthSaleKPI.setText(Utils.convertNumber(kpi.getKpiTarget()));
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            Spinner spinMonth = dialogView.findViewById(R.id.spin_set_kpi_month);

                            spinMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if(position != 0){
                                        choosenMonth = (String) parent.getItemAtPosition(position);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });

                            Button btnDone = dialogView.findViewById(R.id.btn_set_kpi_done);

                            btnDone.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    v.startAnimation(buttonClick);

                                    final String sale = edtSale.getText().toString().replace(",","");
                                    final String newClient = edtNew.getText().toString();

                                    if(TextUtils.isEmpty(sale) || TextUtils.isEmpty(newClient)){
                                        Toast.makeText(getApplicationContext(),"Vui lòng nhập đầy đủ các chỉ tiêu", Toast.LENGTH_LONG).show();

                                    }else if(choosenMonth == null){
                                        Toast.makeText(getApplicationContext(), "Vui lòng chọn tháng!", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                         final KPI updateClientKPI = new KPI(year+"-"+choosenMonth,"New Client",newClient);
                                         final KPI updateSaleKPI = new KPI(year+"-"+choosenMonth,"TotalSale",sale);

                                        refDatabase.child("KPI").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild(saleEmail)){
                                                    refDatabase.child("KPI").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Iterable<DataSnapshot> snapKPI = dataSnapshot.getChildren();
                                                            long itemCount = dataSnapshot.getChildrenCount();


                                                            int i = 0;
                                                            boolean updateSale = false;
                                                            for (DataSnapshot itemKPI:snapKPI){
                                                                i++;
                                                                KPI kpi = itemKPI.getValue(KPI.class);
                                                                String kpiType = kpi.getKpiType();
                                                                String kpiTime = kpi.getKpiTime();
                                                                DatabaseReference refKPI = itemKPI.getRef();

                                                                //Toast.makeText(getApplicationContext(), refKPI+"", Toast.LENGTH_LONG).show();

                                                                if(kpiTime.equals(year+"-"+choosenMonth) && kpiType.equals("TotalSale")){
                                                                    refKPI.setValue(updateSaleKPI);
                                                                    updateSale = true;

                                                                }

                                                                if(i == itemCount){
                                                                    if(!updateSale){

                                                                        refDatabase.child(emailLogin).child("KPI").child(saleEmail).push().setValue(updateSaleKPI);

                                                                    }

                                                                }

                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                }else{
                                                    refDatabase.child("KPI").child(saleEmail).push().setValue(updateSaleKPI);
                                                    refDatabase.child("KPI").child(saleEmail).push().setValue(updateClientKPI);

                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                        dialog.dismiss();
                                        Toast.makeText(getApplicationContext(), "Phân bổ chỉ tiêu thành công!", Toast.LENGTH_LONG).show();
                                    }

                                }
                            });

                        }
                    });

                }
            });


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {


                }else{
                    Toast.makeText(getApplicationContext(), "Không thể sử dụng tính năng này nếu bạn chưa kích hoạt định vị!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                }
            }
        }

    }

    public class ClientViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleClient;
        TextView clientName;

        public ClientViewHolder(View itemView) {
            super(itemView);
            circleClient = (CircleImageView) itemView.findViewById(R.id.profile_image);
            clientName = itemView.findViewById(R.id.tv_item_client_circle_name);


        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }
}

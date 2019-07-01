package a1a4w.onhandsme.list;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Employee;
import a1a4w.onhandsme.model.Group;
import a1a4w.onhandsme.model.KPI;
import a1a4w.onhandsme.utils.Constants;
import de.hdodenhof.circleimageview.CircleImageView;

import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class SaleListByASM extends AppCompatActivity {
    private String emailLogin,userEmail,choosenMonth,exportClickName;
    private String currentDay,date,day,year,month;
    private ImageView ivAddGroup;
    private ProgressBar barKPISale, barKPINewClient;
    private RecyclerView rvGroup,rvDetail;
    private FirebaseRecyclerAdapter<Group, GroupViewHolder> adapterGroup ;
    private FirebaseRecyclerAdapter<Employee, SaleViewHolder> adapterDetail ;
    private int itemCat;
    private FirebaseRecyclerAdapter<Employee, SaleViewHolder> adapterEmployee;
    private int choosenEmployee;
    private boolean saleMan,supervisor,isBeingManaged = false;
    private String choosenEmployeeEmail,managedByEmail,choosenEmployeeName,choosenClientCode;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_list_by_asm);

        Intent it = this.getIntent();
        emailLogin = it.getStringExtra("EmailLogin");


        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        //btnRoute = findViewById(R.id.btn_sale_client_route);

        DateTime dt = new DateTime();
        day = dt.dayOfWeek().getAsText();
        final String week = dt.weekOfWeekyear().getAsText();
        year = dt.getYear()+"";
        month = dt.getMonthOfYear()+"";

        DatabaseReference refSale = refDatabase.child(emailLogin).child("SaleManByASM").child(userEmail).child("Tất cả");
        DatabaseReference refGroup = refDatabase.child(emailLogin).child("SaleManByASM").child(userEmail).child("Group");

        groupList(refGroup);
        detailList(refSale);
        addGroup();
        getKPI();
    }

    private void groupList(DatabaseReference refGroup) {
        rvGroup = (RecyclerView) findViewById(R.id.rv_sale_list_group_asm);
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

        rvDetail = (RecyclerView) findViewById(R.id.rv_sale_list_detail_asm);
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

    private void getKPI() {
        barKPISale = findViewById(R.id.bar_sale_list_kpi_sale_asm);
        barKPINewClient = findViewById(R.id.bar_sale_list_kpi_new_asm);
        final TextView tvKPISale = findViewById(R.id.tv_sale_list_kpi_sale_asm);
        final TextView tvKPINew = findViewById(R.id.tv_sale_list_kpi_new_asm);

        refDatabase.child(emailLogin).child("KPI").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
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

                        refDatabase.child(emailLogin).child("TotalBySale").child(userEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
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


    private void addGroup() {
        ivAddGroup = findViewById(R.id.iv_sale_list_add_group_asm);
        ivAddGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                AlertDialog.Builder builder = new AlertDialog.Builder(SaleListByASM.this);
                builder.setMessage("Thêm nhóm nhân viên?");

                final EditText input = new EditText(SaleListByASM.this);
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
                            refDatabase.child(emailLogin).child("leManByASM").child(userEmail).child("Group").push().child("groupName").setValue(newGroupName);

                        }

                    }
                }).show();
            }
        });
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

                    detailList(refDatabase.child(emailLogin).child("SaleManByASM").child(userEmail).child(groupName));
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

                    int currentChoosenEmployee = getAdapterPosition();
                    adapterEmployee.notifyDataSetChanged();
                    Employee employee = adapterEmployee.getItem(currentChoosenEmployee);
                    choosenEmployeeEmail = employee.getEmployeeEmail();
                    choosenEmployeeName = employee.getEmployeeName();

                    asmClick();

                    if(isBeingManaged && currentChoosenEmployee != choosenEmployee ){
                        AlertDialog.Builder builder = new AlertDialog.Builder(SaleListByASM.this);
                        builder.setMessage("Nhân viên đã được phân bổ cho giám sát khác, bạn muốn thay đổi?");

                        builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                refDatabase.child(emailLogin).child("SaleRoute").child(managedByEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> snapDay = dataSnapshot.getChildren();
                                        for(DataSnapshot itemDay:snapDay){
                                            String dayName = itemDay.getKey();
                                            Iterable<DataSnapshot> snapClient = itemDay.getChildren();

                                            for(DataSnapshot itemClient:snapClient){
                                                String itemClientCode = itemClient.getKey();
                                                DatabaseReference refClient = itemClient.getRef();

                                                if(itemClientCode.equals(choosenClientCode)){
                                                    refClient.setValue(null);
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                        }).show();
                    }

                }

                private void asmClick() {
                    int position = getAdapterPosition();
                    final Employee employee = adapterDetail.getItem(position);
                    final String saleEmail = employee.getEmployeeEmail();
                    //Toast.makeText(getApplicationContext(), saleEmail, Toast.LENGTH_LONG).show();
                    exportClickName = "ThisMonth";

                    AlertDialog.Builder builder = new AlertDialog.Builder(SaleListByASM.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_saleman_detail,null);
                    builder.setView(dialogView);

                    final Dialog dialog = builder.create();
                    dialog.show();

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

                            refDatabase.child(emailLogin).child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
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

                            refDatabase.child(emailLogin).child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
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

                            refDatabase.child(emailLogin).child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
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


                    refDatabase.child(emailLogin).child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
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

                    refDatabase.child(emailLogin).child("KPI").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
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

                                    refDatabase.child(emailLogin).child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
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
                    ImageView grouping = dialogView.findViewById(R.id.iv_sale_detail_grouping);             ImageView ivRoute = dialogView.findViewById(R.id.iv_sale_detail_route);
                    ImageView ivKPI = dialogView.findViewById(R.id.iv_sale_detail_kpi);
                    ImageView ivTeam = dialogView.findViewById(R.id.iv_sale_detail_team);
                    ivTeam.setVisibility(View.VISIBLE);

                    ivRoute.setVisibility(View.GONE);
                    ivKPI.setVisibility(View.GONE);

                    ivTeam.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(SaleListByASM.this);
                            View dialogView = getLayoutInflater().inflate(R.layout.dialog_sale_to_sup,null);
                            builder.setView(dialogView);

                            final Dialog dialog = builder.create();
                            dialog.show();

                            final TextView tvManageBy = dialogView.findViewById(R.id.tv_dialog_sale_to_sup_manageBy);
                            refDatabase.child(emailLogin).child("SaleManByASM").child(userEmail).child("Tất cả").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild("managedBy")){
                                        isBeingManaged = true;
                                        refDatabase.child(emailLogin).child("SaleManByASM").child(userEmail).child("Tất cả").child(saleEmail).child("managedBy").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Employee employee = dataSnapshot.getValue(Employee.class);

                                                tvManageBy.setText(employee.getEmployeeName());
                                                managedByEmail = employee.getEmployeeEmail();

                                                refDatabase.child(emailLogin).child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Iterable<DataSnapshot> snapSaleMan = dataSnapshot.getChildren();
                                                        int i = 0;
                                                        for(DataSnapshot itemSale:snapSaleMan){
                                                            String salemanEmail = itemSale.getKey();
                                                            if(salemanEmail.equals(managedByEmail)){
                                                                choosenEmployee = i;
                                                            }
                                                            i++;

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
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            RecyclerView rvListSale = dialogView.findViewById(R.id.rv_dialog_sale_to_sup_list);
                            rvListSale.setHasFixedSize(true);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(SaleListByASM.this,LinearLayoutManager.HORIZONTAL,false);
                            rvListSale.setLayoutManager(linearLayoutManager);

                            adapterEmployee = new FirebaseRecyclerAdapter<Employee, SaleViewHolder>(
                                    Employee.class,
                                    R.layout.item_saleman,
                                 SaleViewHolder.class,
                                    refDatabase.child(emailLogin).child("SaleManByASM").child(userEmail).child("Tất cả")
                            ) {
                                @Override
                                public SaleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saleman, parent, false);
                                    return new SaleViewHolder(v);
                                }


                                @Override
                                protected void populateViewHolder(SaleViewHolder viewHolder, Employee model, int position) {
                                    viewHolder.saleName.setText(model.getEmployeeName());
                                    viewHolder.circleSale.setBorderColor((position==choosenEmployee)? getResources().getColor(android.R.color.holo_green_light):getResources().getColor(android.R.color.black));
                                    viewHolder.circleSale.setBorderWidth((position==choosenEmployee)? 12:6);

                                    //holder.circleClient.setBackground((position==choosenClient)? context.getResources().getDrawable(android.R.color.white):context.getResources().getDrawable(R.drawable.border_drug_cat_accent));
                                    viewHolder.circleSale.setCircleBackgroundColor((position==choosenEmployee)? getResources().getColor(android.R.color.white):getResources().getColor(android.R.color.transparent));
                                    Glide.with(getApplicationContext()).load(model.getEmployeeUrl()).into(viewHolder.circleSale);
                                }
                            };

                            rvListSale.setAdapter(adapterEmployee);
                            adapterEmployee.notifyDataSetChanged();

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

                            AlertDialog.Builder builder = new AlertDialog.Builder(SaleListByASM.this);
                            builder.setMessage("Đổi nhóm nhân viên?");

                            final EditText input = new EditText(SaleListByASM.this);
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
                                        refDatabase.child(emailLogin).child("SupByASM").child(userEmail).child("Group").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                                            refDatabase.child(emailLogin).child("SupByASM").child(userEmail).child("Group").push().child("groupName").setValue(newGroupName);
                                                            refDatabase.child(emailLogin).child("SupByASM").child(userEmail).child(newGroupName).child(saleEmail).setValue(employee);
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



                }


            });


        }
    }

}

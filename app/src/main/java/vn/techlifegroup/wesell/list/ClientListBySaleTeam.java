package vn.techlifegroup.wesell.list;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import vn.techlifegroup.wesell.MainActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.bytask.SaleRoute;
import vn.techlifegroup.wesell.model.Client;
import vn.techlifegroup.wesell.model.Employee;
import vn.techlifegroup.wesell.model.Group;
import vn.techlifegroup.wesell.model.KPI;
import vn.techlifegroup.wesell.model.MapModel;
import vn.techlifegroup.wesell.order.UpdateOrderActivity;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import de.siegmar.fastcsv.reader.CsvContainer;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.CsvRow;
import de.siegmar.fastcsv.writer.CsvWriter;

import static vn.techlifegroup.wesell.list.AddClientActivity.MY_REQUEST_LOCATION;
import static vn.techlifegroup.wesell.order.PrintPreviewActivity.MY_REQUEST_CODE;
import static vn.techlifegroup.wesell.utils.Constants.buttonClick;
import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class ClientListBySaleTeam extends AppCompatActivity {
    private RecyclerView rvGroup,rvDetail;
    private FirebaseRecyclerAdapter<Group, GroupViewHolder> adapterGroup ;
    private FirebaseRecyclerAdapter<Client, ClientViewHolder> adapterDetail ;
    private FirebaseRecyclerAdapter<Employee,SaleViewHolder> adapterEmployee;
    private String emailLogin,userEmail;
    private ImageView ivAddGroup;
    private int itemCat;
    private FloatingActionButton fabAddClient;
    private Button btnRoute;
    private String exportClickName;
    private String currentDay,date,day,year,month;
    private double latitude,longitude;
    private boolean saleMan,supervisor;
    private int choosenEmployee;
    private String choosenEmployeeEmail,managedByEmail,choosenEmployeeName,choosenClientCode,choosenGroup;
    private  int MY_REQUEST_READ = 1;
    private ProgressBar barKPISale, barKPINewClient;
    private ArrayAdapter<String> adpProduct;
    private RecyclerView rvListSale;
    private Dialog dialogSaleRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_list_by_sale);

        Intent it = this.getIntent();
        emailLogin = it.getStringExtra("EmailLogin");
        saleMan = it.getBooleanExtra("SaleMan",false);
        supervisor = it.getBooleanExtra("Supervisor",false);
        btnRoute = findViewById(R.id.btn_sale_client_route);

        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        getDay();
        if(saleMan){
            DatabaseReference refClient = refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child("Tất cả");
            DatabaseReference refGroup = refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child("Group");

            groupList(refGroup);
            detailList(refClient);
            addGroup();
            addClient();
            gotoRoute();

        }

        if(supervisor){

            btnRoute.setVisibility(View.GONE);
            DatabaseReference refClient = refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child("Tất cả");
            DatabaseReference refGroup = refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child("Group");

            refDatabase.child(emailLogin).child("SaleManBySup").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterator<DataSnapshot> ite = dataSnapshot.getChildren().iterator();
                    Employee defaultEmployee = ite.next().getValue(Employee.class);
                    choosenEmployeeEmail = defaultEmployee.getEmployeeEmail();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            groupList(refGroup);
            detailList(refClient);
            addGroup();
            addClient();
            gotoRoute();
            //importclient();

        }

        getKPI();

    }

    private void getKPI() {
        barKPISale = findViewById(R.id.bar_client_list_kpi_sale);
        barKPINewClient = findViewById(R.id.bar_client_list_kpi_new);
        final TextView tvKPISale = findViewById(R.id.tv_client_list_kpi_sale);
        final TextView tvKPINew = findViewById(R.id.tv_client_list_kpi_new);


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

                        refDatabase.child(emailLogin).child("TotalBySale").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.hasChild(year+"-"+month)){
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
    }


    private void importclient() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        MY_REQUEST_READ);
            }

        }else{

            File appDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Cloudbiz DMS");
            final File csvFile = new File(appDir, "list.csv");
            final String saleEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

            CsvReader csvReader = new CsvReader();
            csvReader.setContainsHeader(true);

            CsvContainer csv = null;
            try {
                csv = csvReader.read(csvFile, StandardCharsets.UTF_8);

                for (int i = 0;i<csv.getRowCount();i++){
                    String clientName = csv.getRow(i).getField("Tên Shop");
                    String clientStreet = csv.getRow(i).getField("Địa chỉ");
                    String clientDistrict = csv.getRow(i).getField("Quận");
                    String clientCity = "Hồ Chí Minh";
                    String clientProvinceString = "Hồ Chí Minh";
                    String clientType = "Tạp hoá";
                    String clientZone = "Hồ Chí Minh";

                    String pushKey = refDatabase.child(emailLogin).child("Client").push().getKey();


                    LatLng latLng = Utils.getLocationFromAddress(getApplicationContext(), clientStreet+", "+clientCity);
                    MapModel map = new MapModel(latLng.latitude+"",latLng.longitude+"");

                    final DatabaseReference clientPush = refDatabase.child(emailLogin+"/Client").push();

                    Client client = new Client(pushKey,clientName,clientType, clientStreet,
                            clientDistrict,clientZone,clientProvinceString,
                            "0", "","0","0",map,saleEmail,"");


                    Client clientMan = new Client(pushKey,clientName,map,clientZone);

                    refDatabase.child(emailLogin).child("Client").child(pushKey).setValue(client);
                    refDatabase.child(emailLogin+"/ClientMan").child(clientType).child(clientZone).child(pushKey).setValue(clientMan);

                    if(csv.getRow(i).getField("Khu vực").equals("HCM1")){
                        refDatabase.child(emailLogin).child("ClientManBySup").child("test_sup@gmail,com").child("Tất cả").child(pushKey).setValue(clientMan);

                 }
                }

                csv.getRow(2).getField(0);

                Toast.makeText(getApplicationContext(), csv.getRowCount()+"",Toast.LENGTH_LONG).show();

                for (CsvRow row : csv.getRows()) {

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void getCurrentLatLong() {
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

    private void getDay() {
        DateTime dt = new DateTime();
        day = dt.dayOfWeek().getAsText();
        final String week = dt.weekOfWeekyear().getAsText();
        year = dt.getYear()+"";
        month = dt.getMonthOfYear()+"";


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

            default:
                currentDay = "a_Thứ hai";

        }
        //Toast.makeText(getApplicationContext(), week, Toast.LENGTH_LONG).show();

        refDatabase.child("1-System/currentWeek").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String currentWeek = dataSnapshot.getValue().toString();
                if(!currentWeek.equals(week)){

                    refDatabase.child(emailLogin).child("SaleRoute").child(userEmail).child(currentDay).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapClient = dataSnapshot.getChildren();
                            for(DataSnapshot itemClient:snapClient){
                                DatabaseReference refClient = itemClient.getRef();
                                refClient.child("isMet").setValue(false);
                                refDatabase.child("1-System/currentWeek").setValue(week);

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

    private void gotoRoute() {
        btnRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent it = new Intent(getApplicationContext(), SaleRoute.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("SaleMan",true);
                startActivity(it);
            }
        });
    }

    private void addClient() {
        fabAddClient = findViewById(R.id.fab_sale_client);
        fabAddClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                createNewClientDialog();
            }
        });
    }

    private void createNewClientDialog() {
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage("Tạo khách hàng mới?");

        builder.setPositiveButton("Có", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent it = new Intent(getApplicationContext(), AddClientActivity.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("SaleMan",saleMan);
                it.putExtra("Supervisor", supervisor);
                startActivity(it);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(ClientListBySaleTeam.this);
                builder.setMessage("Thêm nhóm khách hàng?");

                final EditText input = new EditText(ClientListBySaleTeam.this);
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
                            if(saleMan){
                                refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child("Group").push().child("groupName").setValue(newGroupName);

                            }else{
                                refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child("Group").push().child("groupName").setValue(newGroupName);

                            }
                        }

                    }
                }).show();
            }
        });
    }

    private void detailList(DatabaseReference refClient) {

        rvDetail = (RecyclerView) findViewById(R.id.rv_sale_list_detail);
        rvDetail.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        rvDetail.setLayoutManager(staggeredGridLayoutManager);


        adapterDetail = new FirebaseRecyclerAdapter<Client, ClientViewHolder>(
                Client.class,
                R.layout.item_client_circle,
                ClientViewHolder.class,
                refClient
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

                //Glide.with(getApplicationContext()).load(model.getClientUrl()).into(viewHolder.circleClient);
            }
        };


        rvDetail.setAdapter(adapterDetail);
        adapterDetail.notifyDataSetChanged();
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
                //refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child("Group")
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
                    v.startAnimation(Constants.buttonClick);

                    if(saleMan){
                        clientSaleClick();
                    }
                    
                    if(supervisor){
                        clientSupClick();
                    }

                }

                private void clientSupClick() {
                    int position = getAdapterPosition();
                    final Client client = adapterDetail.getItem(position);
                    final DatabaseReference refClient = adapterDetail.getRef(position);
                    final String parentKey = adapterDetail.getRef(position).getParent().getKey();
                    final String clientCode = client.getClientCode();
                    exportClickName = "ThisMonth";
                    choosenClientCode = clientCode;

                    final AlertDialog.Builder builder = new AlertDialog.Builder(ClientListBySaleTeam.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_client_detail,null);
                    builder.setView(dialogView);

                    Dialog dialog = builder.create();
                    dialog.show();

                    final BarChart barTime = (BarChart)dialogView.findViewById(R.id.bar_client_detail_sale);

                    final TextView tvClientName = dialogView.findViewById(R.id.tv_client_detail_name);
                    final TextView tvClientAddress = dialogView.findViewById(R.id.tv_client_detail_address);

                    final Button yearSale = dialogView.findViewById(R.id.btn_client_detail_yearsale);
                    final Button monthSale = dialogView.findViewById(R.id.btn_client_detail_month_sale);
                    final Button thisMonthSale = dialogView.findViewById(R.id.btn_client_detail_thismonth);
                    Button btnExportExcel = dialogView.findViewById(R.id.btn_client_detail_exportExcel);

                    ImageView phone = dialogView.findViewById(R.id.btn_client_detail_phone);
                    ImageView grouping = dialogView.findViewById(R.id.btn_client_detail_grouping);
                    ImageView ivOrder = dialogView.findViewById(R.id.iv_client_detail_order);
                    ImageView ivFixLoc = dialogView.findViewById(R.id.iv_client_fix_location);
                    ImageView ivSaleRoute = dialogView.findViewById(R.id.iv_client_detail_saleroute);
                    ImageView ivApprove = dialogView.findViewById(R.id.iv_client_detail_approve);


                    if(parentKey.equals("Mới")) {
                        ivApprove.setVisibility(View.VISIBLE);
                    }

                    ivApprove.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(ClientListBySaleTeam.this);
                            builder.setMessage("Duyệt khách hàng này?");
                            builder.setPositiveButton("Duyệt", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {

                                    refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child("Tất cả").child(clientCode).setValue(client);

                                    refDatabase.child(emailLogin).child("Client").child(clientCode).setValue(client).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            refClient.setValue(null);
                                            refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child("Mới").child(clientCode).setValue(null);
                                            dialog.dismiss();
                                        }
                                    });
                                }
                            }).show();


                        }
                    });

                    //ivSaleRoute.setVisibility(View.GONE);

                    yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                    monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                    thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

                    tvClientName.setText(client.getClientName());
                    tvClientAddress.setText(client.getClientStreet());

                    DateTime dt = new DateTime();
                    final String month = dt.getMonthOfYear()+"";
                    final String year = dt.getYear()+"";

                    final List<BarEntry> monthEntries = new ArrayList<>();

                    refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
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

                    ivSaleRoute.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {


                            v.startAnimation(buttonClick);
                            AlertDialog.Builder builder = new AlertDialog.Builder(ClientListBySaleTeam.this);
                            View dialogView = getLayoutInflater().inflate(R.layout.dialog_sale_route_man,null);
                            builder.setView(dialogView);


                            dialogSaleRoute = builder.create();
                            dialogSaleRoute.show();

                            final TextView tvManageBy = dialogView.findViewById(R.id.tv_dialog_route_man_manageBy);
                            final TextView tvClientName = dialogView.findViewById(R.id.tv_sale_route_clientname);

                            refDatabase.child(emailLogin).child("Client").child(choosenClientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Client client = dataSnapshot.getValue(Client.class);
                                    tvClientName.setText(client.getClientName());

                                    if(dataSnapshot.hasChild("managedBy")){

                                        refDatabase.child(emailLogin).child("Client").child(choosenClientCode).child("managedBy").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Employee employee = dataSnapshot.getValue(Employee.class);
                                                //Toast.makeText(getApplicationContext(), employee.getEmployeeName(), Toast.LENGTH_LONG).show();
                                                final String employeeEmail = employee.getEmployeeEmail();
                                                tvManageBy.setText(employee.getEmployeeName());

                                                refDatabase.child(emailLogin).child("SaleManBySup").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();

                                                        int i =0;
                                                        for(DataSnapshot itemSale:snapSale){
                                                            String itemEmail = itemSale.getKey();
                                                            if(employeeEmail.equals(itemEmail)){
                                                                choosenEmployee = i;
                                                                adapterEmployee.notifyDataSetChanged();

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

                            rvListSale = dialogView.findViewById(R.id.rv_dialog_route_sale_list);
                            rvListSale.setHasFixedSize(true);
                            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ClientListBySaleTeam.this,LinearLayoutManager.HORIZONTAL,false);
                            rvListSale.setLayoutManager(linearLayoutManager);

                            adapterEmployee = new FirebaseRecyclerAdapter<Employee, SaleViewHolder>(
                                    Employee.class,
                                    R.layout.item_saleman,
                                    SaleViewHolder.class,
                                    refDatabase.child(emailLogin).child("SaleManBySup").child(userEmail).child("Tất cả")
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
                                    viewHolder.circleSale.setImageDrawable(getResources().getDrawable(R.drawable.icon_saleman));

                                }
                            };

                            rvListSale.setAdapter(adapterEmployee);
                            adapterEmployee.notifyDataSetChanged();



                            final CheckBox chMon = dialogView.findViewById(R.id.check_dialog_route_monday);
                            final CheckBox chTue = dialogView.findViewById(R.id.check_dialog_route_tuesday);
                            final CheckBox chWed = dialogView.findViewById(R.id.check_dialog_route_wednesday);
                            final CheckBox chThu = dialogView.findViewById(R.id.check_dialog_route_thu);
                            final CheckBox chFri = dialogView.findViewById(R.id.check_dialog_route_fri);
                            final CheckBox chSar = dialogView.findViewById(R.id.check_dialog_route_sar);

                            refDatabase.child(emailLogin).child("Client").child(choosenClientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild("managedBy")){

                                        refDatabase.child(emailLogin).child("Client").child(choosenClientCode).child("managedBy").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Employee employee = dataSnapshot.getValue(Employee.class);
                                                //Toast.makeText(getApplicationContext(), employee.getEmployeeName(), Toast.LENGTH_LONG).show();

                                                tvManageBy.setText(employee.getEmployeeName());
                                                managedByEmail = employee.getEmployeeEmail();

                                                refDatabase.child(emailLogin).child("SaleRoute").child(managedByEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Iterable<DataSnapshot> snapDay = dataSnapshot.getChildren();
                                                        for(DataSnapshot itemDay:snapDay){
                                                            String dayName = itemDay.getKey();
                                                            Iterable<DataSnapshot> snapClient = itemDay.getChildren();

                                                            for(DataSnapshot itemClient:snapClient){
                                                                String itemClientCode = itemClient.getKey();
                                                                if(itemClientCode.equals(clientCode)){

                                                                    switch (dayName){
                                                                        case "a_Thứ hai":
                                                                            chMon.setChecked(true);
                                                                            break;

                                                                        case "b_Thứ ba":
                                                                            chTue.setChecked(true);
                                                                            break;

                                                                        case "c_Thứ tư":
                                                                            chWed.setChecked(true);
                                                                            break;

                                                                        case "d_Thứ năm":
                                                                            chThu.setChecked(true);
                                                                            break;

                                                                        case "e_Thứ sáu":
                                                                            chFri.setChecked(true);
                                                                            break;

                                                                        case "f_Thứ bảy":
                                                                            chSar.setChecked(true);
                                                                            break;

                                                                    }
                                                                }
                                                            }
                                                 }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                                refDatabase.child(emailLogin).child("SaleManBySup").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        Iterable<DataSnapshot> snapSaleMan = dataSnapshot.getChildren();
                                                        int i = 0;
                                                        for(DataSnapshot itemSale:snapSaleMan){
                                                            String salemanEmail = itemSale.getKey();
                                                            if(salemanEmail.equals(managedByEmail)){
                                                                choosenEmployee = i;
                                                            }else{
                                                                choosenEmployee = 0;
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


                                    }else{
                                        tvManageBy.setText("Chưa");
                                        //Toast.makeText(getApplicationContext(), "here", Toast.LENGTH_LONG).show();

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            Button btnDone = dialogView.findViewById(R.id.btn_dialog_route_done);
                            btnDone.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    v.startAnimation(buttonClick);

                                    Employee manageByEmployee = new Employee(choosenEmployeeName,choosenEmployeeEmail);

                                    refDatabase.child(emailLogin).child("ClientManBySale").child(choosenEmployeeEmail).child("Tất cả").child(clientCode).setValue(client);

                                    if(chMon.isChecked()){
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("a_Thứ hai").child(clientCode).setValue(client);
                                        refDatabase.child(emailLogin).child("Client").child(choosenClientCode).child("managedBy").setValue(manageByEmployee);
                                    }else{
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("a_Thứ hai").child(clientCode).setValue(null);
                                    }

                                    if(chTue.isChecked()){
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("b_Thứ ba").child(clientCode).setValue(client);
                                        refDatabase.child(emailLogin).child("Client").child(choosenClientCode).child("managedBy").setValue(manageByEmployee);

                                    }else{
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("b_Thứ ba").child(clientCode).setValue(null);
                                    }
                                    if(chWed.isChecked()){
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("c_Thứ tư").child(clientCode).setValue(client);
                                        refDatabase.child(emailLogin).child("Client").child(choosenClientCode).child("managedBy").setValue(manageByEmployee);

                                    }else{
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("c_Thứ tư").child(clientCode).setValue(null);
                                    }

                                    if(chThu.isChecked()){
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("d_Thứ năm").child(clientCode).setValue(client);
                                        refDatabase.child(emailLogin).child("Client").child(choosenClientCode).child("managedBy").setValue(manageByEmployee);

                                    }else{
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("d_Thứ năm").child(clientCode).setValue(null);
                                    }

                                    if(chFri.isChecked()){
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("e_Thứ sáu").child(clientCode).setValue(client);
                                        refDatabase.child(emailLogin).child("Client").child(choosenClientCode).child("managedBy").setValue(manageByEmployee);

                                    }else{
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("e_Thứ sáu").child(clientCode).setValue(null);
                                    }

                                    if(chSar.isChecked()){
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("f_Thứ bảy").child(clientCode).setValue(client);
                                        refDatabase.child(emailLogin).child("Client").child(choosenClientCode).child("managedBy").setValue(manageByEmployee);

                                    }else{
                                        refDatabase.child(emailLogin).child("SaleRoute").child(choosenEmployeeEmail).child("f_Thứ bảy").child(clientCode).setValue(null);
                                    }

                                    dialogSaleRoute.dismiss();

                                }
                            });


                        }
                    });

                    ivOrder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);
                            Intent it = new Intent(getApplicationContext(), UpdateOrderActivity.class);
                            it.putExtra("EmailLogin", emailLogin);
                            it.putExtra("ClientCode", clientCode);
                            it.putExtra("SaleMan",true);
                            it.putExtra("OutRoute",true);
                            startActivity(it);
                        }
                    });

                    btnExportExcel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);
                            exportData(clientCode);
                        }
                    });

                    phone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            refDatabase.child(emailLogin).child("Client").child(clientCode).child("clientPhone").addListenerForSingleValueEvent(new ValueEventListener() {
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

                    ivFixLoc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(ClientListBySaleTeam.this);
                            builder.setMessage("Cập nhật toạ khách hàng?");

                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MapModel updateMap = new MapModel(latitude+"",longitude+"");
                                    refDatabase.child(emailLogin).child("Client").child(clientCode).child("map").setValue(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getApplicationContext(), "Cập nhật hoàn tất!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }).show();

                        }
                    });

                    grouping.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(ClientListBySaleTeam.this);
                            View dialogView = getLayoutInflater().inflate(R.layout.dialog_grouping,null);
                            builder.setView(dialogView);

                            final Dialog dialog = builder.create();
                            dialog.show();
                            /*
                            builder.setMessage("Đổi nhóm khách hàng?");

                            final EditText input = new EditText(ClientListBySaleTeam.this);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT);
                            input.setLayoutParams(lp);
                            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                            input.setHint("Nhập tên nhóm");
                            builder.setView(input);
*/
                             final Spinner spinGroup = dialogView.findViewById(R.id.spin_grouping);
                             final EditText edtGroupName = dialogView.findViewById(R.id.edt_grouping_input);
                             Button btnDone = dialogView.findViewById(R.id.btn_grouping_done);
                             final CheckBox chDelete = dialogView.findViewById(R.id.ch_grouping_delete);

                             final List<String> listGroup = new ArrayList<>();

                            listGroup.add("Chọn nhóm");

                            refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child("Group").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapGroup = dataSnapshot.getChildren();
                                    for(DataSnapshot itemGroup:snapGroup){
                                        Group group = itemGroup.getValue(Group.class);

                                        String groupName = group.getGroupName();
                                        if(!groupName.equals("Tất cả")) {
                                            listGroup.add(groupName);

                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            adpProduct = new ArrayAdapter<String>(getApplicationContext(),
                                    android.R.layout.simple_list_item_1, listGroup);
                            adpProduct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                            spinGroup.setAdapter(adpProduct);

                            spinGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    ((TextView) spinGroup.getSelectedView()).setTextColor(getResources().getColor(android.R.color.black));

                                    if(position!=0){
                                        choosenGroup = (String) parent.getItemAtPosition(position);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                            btnDone.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    v.startAnimation(buttonClick);
                                    final String newGroupName = edtGroupName.getText().toString();

                                    if (newGroupName.equals("Tất cả")){
                                        Toast.makeText(getApplicationContext(), "Không thể đặt tên này!", Toast.LENGTH_LONG).show();

                                    }else if(choosenGroup == null){
                                        Toast.makeText(getApplicationContext(), "Vui lòng chọn tên nhóm!", Toast.LENGTH_LONG).show();

                                    }
                                    else{
                                        dialog.dismiss();

                                        refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child(choosenGroup).child(clientCode).setValue(client);
                                        if(chDelete.isChecked()){
                                            if(!parentKey.equals("Tất cả")){
                                                refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child(choosenGroup).child(clientCode).setValue(null);
                                                //Toast.makeText(getApplicationContext(),refClient+"", Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    }
                                }
                            });


                        }
                    });

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

                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
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

                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
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

                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
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
                }


                private void clientSaleClick() {
                    int position = getAdapterPosition();
                    final Client client = adapterDetail.getItem(position);
                    final DatabaseReference refClient = adapterDetail.getRef(position);
                    final String parentKey = adapterDetail.getRef(position).getParent().getKey();
                    final String clientCode = client.getClientCode();
                    exportClickName = "ThisMonth";

                    AlertDialog.Builder builder = new AlertDialog.Builder(ClientListBySaleTeam.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_client_detail,null);
                    builder.setView(dialogView);

                    Dialog dialog = builder.create();
                    dialog.show();

                    final BarChart barTime = (BarChart)dialogView.findViewById(R.id.bar_client_detail_sale);

                    final TextView tvClientName = dialogView.findViewById(R.id.tv_client_detail_name);
                    final TextView tvClientAddress = dialogView.findViewById(R.id.tv_client_detail_address);

                    final Button yearSale = dialogView.findViewById(R.id.btn_client_detail_yearsale);
                    final Button monthSale = dialogView.findViewById(R.id.btn_client_detail_month_sale);
                    final Button thisMonthSale = dialogView.findViewById(R.id.btn_client_detail_thismonth);
                    Button btnExportExcel = dialogView.findViewById(R.id.btn_client_detail_exportExcel);

                    ImageView phone = dialogView.findViewById(R.id.btn_client_detail_phone);
                    ImageView grouping = dialogView.findViewById(R.id.btn_client_detail_grouping);
                    ImageView ivOrder = dialogView.findViewById(R.id.iv_client_detail_order);
                    ImageView ivFixLoc = dialogView.findViewById(R.id.iv_client_fix_location);
                    ImageView ivSaleRoute = dialogView.findViewById(R.id.iv_client_detail_saleroute);

                    ivSaleRoute.setVisibility(View.GONE);

                    yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                    monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                    thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

                    refDatabase.child(emailLogin).child("Client").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Client clientInfo = dataSnapshot.getValue(Client.class);
                            assert clientInfo != null;
                            String clientStreet = clientInfo.getClientStreet();
                            String clientProvince = clientInfo.getClientProvince();
                            tvClientName.setText(clientInfo.getClientName());
                            tvClientAddress.setText(clientStreet + ", " + clientProvince);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    DateTime dt = new DateTime();
                    final String month = dt.getMonthOfYear()+"";
                    final String year = dt.getYear()+"";

                    final List<BarEntry> monthEntries = new ArrayList<>();

                    refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
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

                    ivOrder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);
                            Intent it = new Intent(getApplicationContext(), UpdateOrderActivity.class);
                            it.putExtra("EmailLogin", emailLogin);
                            it.putExtra("ClientCode", clientCode);
                            it.putExtra("SaleMan",true);
                            it.putExtra("OutRoute",true);
                            startActivity(it);
                        }
                    });

                    btnExportExcel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);
                            exportData(clientCode);
                        }
                    });

                    phone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            refDatabase.child(emailLogin).child("Client").child(clientCode).child("clientPhone").addListenerForSingleValueEvent(new ValueEventListener() {
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

                    ivFixLoc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(ClientListBySaleTeam.this);
                            builder.setMessage("Cập nhật toạ khách hàng?");

                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    MapModel updateMap = new MapModel(latitude+"",longitude+"");
                                    refDatabase.child(emailLogin).child("Client").child(clientCode).child("map").setValue(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getApplicationContext(), "Cập nhật hoàn tất!", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                }
                            }).show();

                        }
                    });

                    grouping.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(buttonClick);

                            AlertDialog.Builder builder = new AlertDialog.Builder(ClientListBySaleTeam.this);
                            View dialogView = getLayoutInflater().inflate(R.layout.dialog_grouping,null);
                            builder.setView(dialogView);

                            final Dialog dialog = builder.create();
                            dialog.show();
                            /*
                            builder.setMessage("Đổi nhóm khách hàng?");

                            final EditText input = new EditText(ClientListBySaleTeam.this);
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT);
                            input.setLayoutParams(lp);
                            input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                            input.setHint("Nhập tên nhóm");
                            builder.setView(input);
*/
                            Spinner spinGroup = dialogView.findViewById(R.id.spin_grouping);
                            final EditText edtGroupName = dialogView.findViewById(R.id.edt_grouping_input);
                            Button btnDone = dialogView.findViewById(R.id.btn_grouping_done);
                            final CheckBox chDelete = dialogView.findViewById(R.id.ch_grouping_delete);

                            final List<String> listGroup = new ArrayList<>();

                            listGroup.add("Chọn nhóm");
                            Toast.makeText(getApplicationContext(), userEmail, Toast.LENGTH_LONG).show();

                            refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child("Group").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapGroup = dataSnapshot.getChildren();
                                    for(DataSnapshot itemGroup:snapGroup){
                                        Group group = itemGroup.getValue(Group.class);

                                        String groupName = group.getGroupName();
                                        if(!groupName.equals("Tất cả")) {
                                            listGroup.add(groupName);

                                        }
                                    }


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            adpProduct = new ArrayAdapter<String>(getApplicationContext(),
                                    android.R.layout.simple_list_item_1, listGroup);
                            adpProduct.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                            spinGroup.setAdapter(adpProduct);

                            spinGroup.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if(position!=0){
                                        choosenGroup = (String) parent.getItemAtPosition(position);
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                            btnDone.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    v.startAnimation(buttonClick);
                                    final String newGroupName = edtGroupName.getText().toString();

                                    if (newGroupName.equals("Tất cả")){
                                        Toast.makeText(getApplicationContext(), "Không thể đặt tên này!", Toast.LENGTH_LONG).show();

                                    }else if(choosenGroup == null){
                                        Toast.makeText(getApplicationContext(), "Vui lòng chọn tên nhóm!", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child(choosenGroup).child(clientCode).setValue(client);
                                        dialog.dismiss();

                                        if(chDelete.isChecked()){
                                            if(!parentKey.equals("Tất cả")){
                                                refClient.setValue(null);
                                                //Toast.makeText(getApplicationContext(),refClient+"", Toast.LENGTH_LONG).show();
                                            }
                                        }

                                    }
                                }
                            });
                        }
                    });

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

                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
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

                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
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

                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
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
                }

                private void exportData(String clientCode) {

                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    MY_REQUEST_CODE);
                        }

                    }else {

                        String timeStamp = Calendar.getInstance().getTime().getTime()+"";

                        File appDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Cloudbiz DMS");
                        final File csvFile = new File(appDir, userEmail+"_"+timeStamp+".csv");

                        if (!appDir.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            appDir.mkdir();
                        }
                        // fix
                        //noinspection ResultOfMethodCallIgnored
                        appDir.setExecutable(true);
                        //noinspection ResultOfMethodCallIgnored
                        appDir.setReadable(true);
                        //noinspection ResultOfMethodCallIgnored
                        appDir.setWritable(true);
                        MediaScannerConnection.scanFile(ClientListBySaleTeam.this, new String[]{appDir.toString()}, null, null);

                        final CsvWriter csvWriter = new CsvWriter();
                        final Collection<String[]> data = new ArrayList<>();

                        if(exportClickName.equals("YearSale")){
                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    data.add(new String[] { "Nam","Doanh so" });

                                    Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();
                                    long itemCount = dataSnapshot.getChildrenCount();
                                    int i = 0;

                                    for(DataSnapshot itemTime:snapTimeSale){
                                        i++;
                                        String timeKey = itemTime.getKey();
                                        String value = itemTime.getValue().toString();

                                        final List<String> valuesToFile = new ArrayList<>();

                                        if(timeKey.length()<5){

                                            data.add(new String[] {timeKey,value});
                                            try {
                                                csvWriter.write(csvFile, StandardCharsets.UTF_8, data);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }else if(exportClickName.equals("MonthSale")){
                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();
                                    data.add(new String[] { "","Doanh so" });

                                    for(DataSnapshot itemTime:snapTimeSale){

                                        String timeKey = itemTime.getKey();

                                        String value = itemTime.getValue().toString();

                                        if(timeKey.length()>5 && timeKey.length()<8){

                                            data.add(new String[] {timeKey.substring(5),value});
                                            try {
                                                csvWriter.write(csvFile, StandardCharsets.UTF_8, data);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }

                                        }

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }else{

                            DateTime dt = new DateTime();
                            final String month = dt.getMonthOfYear()+"";
                            final String year = dt.getYear()+"";

                            refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();
                                    data.add(new String[] { "","Doanh so" });

                                    for(DataSnapshot itemTime:snapTimeSale){

                                        String timeKey = itemTime.getKey();

                                        String value = itemTime.getValue().toString();


                                        if(timeKey.length()>7 ){

                                            if(timeKey.contains(year+"-"+month)){

                                                data.add(new String[] {timeKey.substring(timeKey.lastIndexOf("-")+1),value});
                                                try {
                                                    csvWriter.write(csvFile, StandardCharsets.UTF_8, data);
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
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

                    }

                }


            });


        }
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
                    if(saleMan){
                        detailList(refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child(groupName));

                    }

                    if(supervisor){
                        detailList(refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child(groupName));

                    }
                }
            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.startAnimation(Constants.buttonClick);

                    Group group = adapterGroup.getItem(itemCat);
                    final String oldName = group.getGroupName();


                    AlertDialog.Builder builder = new AlertDialog.Builder(ClientListBySaleTeam.this);
                    builder.setMessage("Đổi tên nhóm khách hàng?");

                    final EditText input = new EditText(ClientListBySaleTeam.this);
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

                            }else if (oldName.equals("Tất cả")){
                                Toast.makeText(getApplicationContext(), "Không thể đổi tên nhóm này!", Toast.LENGTH_LONG).show();

                            }
                            else{
                                int pos = getAdapterPosition();
                                String key = adapterGroup.getRef(pos).getKey();

                                if(saleMan){
                                    refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child("Group").child(key).child("groupName").setValue(newGroupName);
                                    //Đổi tên và copy các thành phần trong group
                                    refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child(oldName).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> snapClient = dataSnapshot.getChildren();
                                            for(DataSnapshot itemClient: snapClient){
                                                Client clientCopied = itemClient.getValue(Client.class);

                                                refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child(newGroupName).push().setValue(clientCopied).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        refDatabase.child(emailLogin).child("ClientManBySale").child(userEmail).child(oldName).setValue(null);
                                                    }
                                                });

                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });
                                }

                                if(supervisor){
                                    refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child("Group").child(key).child("groupName").setValue(newGroupName);
                                    //Đổi tên và copy các thành phần trong group
                                    refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child(oldName).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> snapClient = dataSnapshot.getChildren();
                                            for(DataSnapshot itemClient: snapClient){
                                                Client clientCopied = itemClient.getValue(Client.class);

                                                refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child(newGroupName).push().setValue(clientCopied).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        refDatabase.child(emailLogin).child("ClientManBySup").child(userEmail).child(oldName).setValue(null);
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
                    }).show();
                    return false;
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    choosenEmployee = getAdapterPosition();
                    final Employee employee = adapterEmployee.getItem(choosenEmployee);
                    choosenEmployeeEmail = employee.getEmployeeEmail();
                    choosenEmployeeName = employee.getEmployeeName();

                    adapterEmployee.notifyDataSetChanged();

                    refDatabase.child(emailLogin).child("Client").child(choosenClientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("managedBy")){

                                refDatabase.child(emailLogin).child("Client").child(choosenClientCode).child("managedBy").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final Employee  currentEmployee = dataSnapshot.getValue(Employee.class);
                                        //Toast.makeText(getApplicationContext(), employee.getEmployeeName(), Toast.LENGTH_LONG).show();

                                        if(!choosenEmployeeEmail.equals(currentEmployee.getEmployeeEmail())){

                                            AlertDialog.Builder builder = new AlertDialog.Builder(ClientListBySaleTeam.this);
                                            builder.setMessage("Khách hàng đang được quản lý bởi nhân viên " + currentEmployee.getEmployeeName()+", bạn muốn chuyển sang quản lý bởi " +
                                                            choosenEmployeeName +" ?");

                                            builder.setNegativeButton("Không", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            }).setPositiveButton("Có", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialogSaleRoute.dismiss();

                                                    //adapterEmployee.notifyDataSetChanged();
                                                    refDatabase.child(emailLogin).child("Client").child(choosenClientCode).child("managedBy").setValue(employee);
                                                    refDatabase.child(emailLogin).child("ClientManBySale").child(currentEmployee.getEmployeeEmail()).child("Tất cả").child(choosenClientCode).setValue(null);

                                                    refDatabase.child(emailLogin).child("SaleRoute").child(currentEmployee.getEmployeeEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
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
            });


    }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }
}

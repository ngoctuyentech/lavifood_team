package a1a4w.onhandsme.bytask;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import a1a4w.onhandsme.MainActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.Employee;
import a1a4w.onhandsme.model.MapModel;
import a1a4w.onhandsme.order.UpdateOrderActivity;
import a1a4w.onhandsme.utils.AdapterMeetClient;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.EmployeeTracker;
import a1a4w.onhandsme.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import im.delight.android.location.SimpleLocation;

import static a1a4w.onhandsme.list.AddClientActivity.MY_REQUEST_LOCATION;
import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class SaleRoute extends AppCompatActivity implements OnMapReadyCallback {

    public static GoogleMap mMap;
    private LatLng[] listLoc;
    private static final String TAG = SaleRoute.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted = false;
    public static String emailLogin,userEmail;
    public static double latitude,longitude,clientOnMapLat,clientOnMapLong,currentDistance;
    private HashMap<String, Double> distanceMap = new HashMap<>();
    private Map sortTopProduct;
    private List<Client> listVisits;
    @SuppressLint("StaticFeak")
    private TextView tvRouteMon, tvRouteTue,tvRouteWed,tvRouteThu,tvRouteFri,tvRouteSat;
    private RecyclerView rvClientList;
    public static Button btnIn,btnOut,btnNewOrder;
    public static String choosenClientName,choosenClientCode;
    private String exportClickName;
    private String currentDay,date;
    private SupportMapFragment mapFragment;
    private String saleEmail;
    private boolean supervisor,saleMan;
    private FirebaseRecyclerAdapter<Client, ClientViewHolder> adapterDetail ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_route);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        Intent it = this.getIntent();
        emailLogin = it.getStringExtra("EmailLogin");
        supervisor = it.getBooleanExtra("Supervisor",false);
        saleEmail = it.getStringExtra("SaleEmail");
        saleMan = it.getBooleanExtra("SaleMan", false);

        tvRouteMon = findViewById(R.id.tv_route_mon);
        tvRouteTue = findViewById(R.id.tv_route_tue);
        tvRouteWed = findViewById(R.id.tv_route_wed);
        tvRouteThu = findViewById(R.id.tv_route_thu);
        tvRouteFri = findViewById(R.id.tv_route_fri);
        tvRouteSat = findViewById(R.id.tv_route_sat);

        rvClientList = findViewById(R.id.rv_sale_route_client_list);
        rvClientList.setHasFixedSize(true);
        StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rvClientList.setLayoutManager(staggeredGridLayoutManager);


        getDay();
        onDayRouteClick();
        saleAction();

        if(supervisor){
            userEmail = saleEmail;

            tvRouteMon.setBackground(getResources().getDrawable(R.drawable.border_accent));
            tvRouteTue.setBackground(getResources().getDrawable(R.drawable.border_white));
            tvRouteWed.setBackground(getResources().getDrawable(R.drawable.border_white));
            tvRouteThu.setBackground(getResources().getDrawable(R.drawable.border_white));
            tvRouteFri.setBackground(getResources().getDrawable(R.drawable.border_white));
            tvRouteSat.setBackground(getResources().getDrawable(R.drawable.border_white));
            listOfVisit("a_Thứ hai");
            saleMan = true;
        }

        else{


            userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

            getCurrentLatLong();

            Intent itService = new Intent(getApplicationContext(), EmployeeTracker.class);
            itService.putExtra("EmailLogin",emailLogin);
            itService.putExtra("SaleEmail",userEmail);
            startService(itService);
        }

    }

    private void onDayRouteClick() {
        tvRouteMon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                tvRouteMon.setBackground(getResources().getDrawable(R.drawable.border_accent));
                tvRouteTue.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteWed.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteThu.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteFri.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteSat.setBackground(getResources().getDrawable(R.drawable.border_white));

                listOfVisit("a_Thứ hai");
            }
        });


        tvRouteTue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                tvRouteMon.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteTue.setBackground(getResources().getDrawable(R.drawable.border_accent));
                tvRouteWed.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteThu.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteFri.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteSat.setBackground(getResources().getDrawable(R.drawable.border_white));

                listOfVisit("b_Thứ ba");
            }
        });

        tvRouteWed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                tvRouteMon.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteTue.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteWed.setBackground(getResources().getDrawable(R.drawable.border_accent));
                tvRouteThu.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteFri.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteSat.setBackground(getResources().getDrawable(R.drawable.border_white));

                listOfVisit("c_Thứ tư");
            }
        });

        tvRouteThu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                tvRouteMon.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteTue.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteWed.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteThu.setBackground(getResources().getDrawable(R.drawable.border_accent));
                tvRouteFri.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteSat.setBackground(getResources().getDrawable(R.drawable.border_white));

                listOfVisit("d_Thứ năm");
            }
        });

        tvRouteFri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                tvRouteMon.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteTue.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteWed.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteThu.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteFri.setBackground(getResources().getDrawable(R.drawable.border_accent));
                tvRouteSat.setBackground(getResources().getDrawable(R.drawable.border_white));

                listOfVisit("e_Thứ sáu");
            }
        });

        tvRouteSat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                tvRouteMon.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteTue.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteWed.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteThu.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteFri.setBackground(getResources().getDrawable(R.drawable.border_white));
                tvRouteSat.setBackground(getResources().getDrawable(R.drawable.border_accent));

                listOfVisit("f_Thứ bảy");
            }
        });
    }

    private void getClientList(){

        adapterDetail = new FirebaseRecyclerAdapter<Client, ClientViewHolder>(
                Client.class,
                R.layout.item_client_circle,
                ClientViewHolder.class,
                refDatabase.child(emailLogin).child("SaleRoute").child(userEmail).child(currentDay)
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


        rvClientList.setAdapter(adapterDetail);
        adapterDetail.notifyDataSetChanged();
    }

    private void getDay() {
        DateTime dt = new DateTime();
        String day = dt.dayOfWeek().getAsText();
        final String week = dt.weekOfWeekyear().getAsText();

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

            default:
                currentDay = "a_Thứ hai";

        }
        //Toast.makeText(getApplicationContext(), week, Toast.LENGTH_LONG).show();

    }

    @Override
    protected void onResume() {
        super.onResume();



        //getClientList();

        //pushKeyVisit = refDatabase.child(emailLogin).child("SaleVisit").child(userEmail).push().getKey();
    }

    private void saleAction() {
        btnIn = findViewById(R.id.btn_sale_route_in);
        btnOut = findViewById(R.id.btn_sale_route_out);
        btnNewOrder = findViewById(R.id.btn_sale_route_order);

        if(supervisor){
            btnIn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            btnIn.setEnabled(false);
            btnOut.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            btnOut.setEnabled(false);
            btnNewOrder.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            btnNewOrder.setEnabled(false);
        }

        btnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);

                Toast.makeText(getApplicationContext(),"Đã check in!", Toast.LENGTH_LONG).show();

                String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                Employee employeeVisit = new Employee(timeStamp,choosenClientCode,choosenClientName);
                refDatabase.child(emailLogin).child("SaleVisit").child(userEmail).child(date).child(choosenClientCode).setValue(employeeVisit);

            }


        });

        btnOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                v.startAnimation(Constants.buttonClick);

                Toast.makeText(getApplicationContext(),"Đã check !", Toast.LENGTH_LONG).show();

                final String timeStamp = Calendar.getInstance().getTime().getTime()+"";
                refDatabase.child(emailLogin).child("SaleVisit").child(userEmail).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(choosenClientCode)){
                            refDatabase.child(emailLogin).child("SaleVisit").child(userEmail).child(date).child(choosenClientCode).child("outTime").setValue(timeStamp);
                        }else{
                            Toast.makeText(getApplicationContext(), "Bạn chưa check in!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                refDatabase.child(emailLogin).child("SaleRoute").child(userEmail).child(currentDay).child(choosenClientCode).child("isMet").setValue(true).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listOfVisit(currentDay);

                    }
                });
            }
        });

        btnNewOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);

                refDatabase.child(emailLogin).child("SaleVisit").child(userEmail).child(date).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(choosenClientCode)){

                            Intent it = new Intent(getApplicationContext(), UpdateOrderActivity.class);
                            it.putExtra("EmailLogin", emailLogin);
                            it.putExtra("ClientCode", choosenClientCode);
                            it.putExtra("SaleMan",true);
                            startActivity(it);

                        }else{
                            Toast.makeText(getApplicationContext(), "Bạn chưa check in!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



            }
        });

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
                    //Toast.makeText(getApplicationContext(), latitude+"", Toast.LENGTH_LONG).show();
                    listOfVisit(currentDay);

                } else {
                    Toast.makeText(getApplicationContext(), "Không lấy được vị trí hiện tại, vui lòng thử lại!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void listOfVisit(final String currentDay) {

        listVisits = new ArrayList<>();
        if(distanceMap!=null) distanceMap.clear();
        if(sortTopProduct!=null) sortTopProduct.clear();

        refDatabase.child(emailLogin).child("SaleRoute").child(userEmail).child(currentDay).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapClient = dataSnapshot.getChildren();
                long itemCount = dataSnapshot.getChildrenCount();

                int i = 0;
                for (DataSnapshot itemClient:snapClient) {
                    i++;
                    Client client = itemClient.getValue(Client.class);
                    assert client != null;

                    boolean isMet = client.isMet();

                    if(!isMet){

                        MapModel mapModel = client.getMap();
                        String clientCode = client.getClientCode();

                        SimpleLocation.Point agentPoint = new SimpleLocation.Point(Double.parseDouble(mapModel.getLatitude()), Double.parseDouble(mapModel.getLongitude()));
                        SimpleLocation.Point salePoint = new SimpleLocation.Point(latitude,longitude);
                        double currentDistance = SimpleLocation.calculateDistance(agentPoint, salePoint);

                        distanceMap.put(clientCode, currentDistance);
                        sortTopProduct = Utils.sortIncreaseByValues(distanceMap);

                        if(i == itemCount){
                            Set set = sortTopProduct.entrySet();

                            Iterator iterator = set.iterator();
                            Map.Entry me = (Map.Entry) iterator.next();

                            final double minDis = Double.parseDouble(me.getValue().toString());

                            //Toast.makeText(getApplicationContext(),minDis+"", Toast.LENGTH_LONG).show();

                            for(Map.Entry<String,Double> entry: distanceMap.entrySet()){
                                String key = entry.getKey();
                                double value = entry.getValue();

                                if(value == minDis){

                                    refDatabase.child(emailLogin).child("SaleRoute").child(userEmail).child(currentDay).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Client addListClient = dataSnapshot.getValue(Client.class);
                                            assert addListClient != null;
                                            MapModel clientMap = addListClient.getMap();
                                            String nearestBuyDay = addListClient.getNearestBuyDay();
                                            String monthSale = addListClient.getMonthSale();
                                            choosenClientName = addListClient.getClientName();
                                            choosenClientCode = addListClient.getClientCode();
                                            //tvMonthSale.setText(Utils.convertNumber(monthSale));
                                            //tvNearestBuy.setText(nearestBuyDay);
/*
                                        if(minDis>100){
                                            btnIn.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                            btnIn.setEnabled(false);
                                            btnOut.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                            btnOut.setEnabled(false);
                                            btnNewOrder.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                                            btnNewOrder.setEnabled(false);
                                        }

*/
                                            clientOnMapLat = Double.parseDouble(clientMap.getLatitude());
                                            clientOnMapLong = Double.parseDouble(clientMap.getLongitude());

                                            LatLng onMapClient = new LatLng(clientOnMapLat, clientOnMapLong);

                                            mMap.addMarker(new MarkerOptions().position(onMapClient)
                                                    .title("Khách hàng gần nhất"));

                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(onMapClient,16.0f));
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }

                                refDatabase.child(emailLogin).child("SaleRoute").child(userEmail).child(currentDay).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Client addListClient = dataSnapshot.getValue(Client.class);
                                        assert addListClient != null;

                                        listVisits.add(addListClient);

                                        AdapterMeetClient adapterMeetClient = new AdapterMeetClient(getApplicationContext(),listVisits,SaleRoute.this,emailLogin);
                                        rvClientList.setAdapter(adapterMeetClient);
                                        adapterMeetClient.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }

                        }



                    }



                    //Toast.makeText(getApplicationContext(), currentDistance+"", Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);

            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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

                    getCurrentLatLong();

                    Intent itService = new Intent(getApplicationContext(), EmployeeTracker.class);
                    itService.putExtra("EmailLogin",emailLogin);
                    itService.putExtra("SaleEmail",userEmail);
                    startService(itService);

                }else{
                    Toast.makeText(getApplicationContext(), "Không thể sử dụng tính năng này nếu bạn chưa kích hoạt định vị!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));

                }
            }
        }

    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.string.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }


        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
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

}

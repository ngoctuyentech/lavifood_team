package vn.techlifegroup.wesell.bytask;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import vn.techlifegroup.wesell.MainActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Client;
import vn.techlifegroup.wesell.model.Employee;
import vn.techlifegroup.wesell.model.MapModel;
import vn.techlifegroup.wesell.utils.Utils;
import de.hdodenhof.circleimageview.CircleImageView;
import im.delight.android.location.SimpleLocation;

import static vn.techlifegroup.wesell.list.AddClientActivity.MY_REQUEST_LOCATION;
import static vn.techlifegroup.wesell.utils.Constants.buttonClick;
import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class TeamMan extends AppCompatActivity implements OnMapReadyCallback {
    public static double latitude,longitude,currentDistance,employeeLat,employeeLng;
    private String emailLogin,userEmail;
    private List<Client> listSaleMan;
    private HashMap<String, Double> distanceMap = new HashMap<>();
    private Map sortTopProduct;
    private SupportMapFragment mapFragment;
    private FirebaseRecyclerAdapter<Employee, SaleViewHolder> adapterDetail ;
    private RecyclerView rvList;
    private int choosenEmployee;
    private GoogleMap mMap;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    private DatabaseReference refSale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_man);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");


        refSale = refDatabase.child("SaleManBySup").child(userEmail).child("Tất cả");

        getCurrentLatLong();

        detailList(refSale);
    }

    private void detailList(DatabaseReference refClient) {

        rvList = (RecyclerView) findViewById(R.id.rv_team_man_list);
        rvList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false);
        rvList.setLayoutManager(linearLayoutManager);


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

                viewHolder.circleSale.setBorderColor((position==choosenEmployee)?getResources().getColor(android.R.color.holo_green_light):getResources().getColor(android.R.color.black));
                viewHolder.circleSale.setBorderWidth((position==choosenEmployee)? 12:6);

                //holder.circleClient.setBackground((position==choosenClient)? context.getResources().getDrawable(android.R.color.white):context.getResources().getDrawable(R.drawable.border_drug_cat_accent));
                viewHolder.circleSale.setCircleBackgroundColor((position==choosenEmployee)? getResources().getColor(android.R.color.white):getResources().getColor(android.R.color.transparent));
                viewHolder.circleSale.setImageDrawable(getResources().getDrawable(R.drawable.icon_saleman));            }
        };

        rvList.setAdapter(adapterDetail);
        adapterDetail.notifyDataSetChanged();
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
                 //listOfVisit();

                } else {
                    Toast.makeText(getApplicationContext(), "Không lấy được vị trí hiện tại, vui lòng thử lại!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private void listOfVisit() {

        listSaleMan = new ArrayList<>();
        //if(distanceMap!=null) distanceMap.clear();
        // if(sortTopProduct!=null) sortTopProduct.clear();

        refDatabase.child("GeoFire").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> saleLocSnap = dataSnapshot.getChildren();
                long itemCount = dataSnapshot.getChildrenCount();

                int i = 0;
                for(DataSnapshot itemLoc:saleLocSnap){
                    i++;
                    MapModel mapModel = itemLoc.getValue(MapModel.class);
                    String saleEmail = itemLoc.getKey();

                    SimpleLocation.Point salePoint = new SimpleLocation.Point(Double.parseDouble(mapModel.getLatitude()), Double.parseDouble(mapModel.getLongitude()));
                    SimpleLocation.Point supPoint = new SimpleLocation.Point(latitude,longitude);
                    double currentDistance = SimpleLocation.calculateDistance(supPoint, salePoint);

                    distanceMap.put(saleEmail, currentDistance);
                    sortTopProduct = Utils.sortIncreaseByValues(distanceMap);

                    if(i == itemCount){

                        Set set = sortTopProduct.entrySet();
                        Iterator iterator = set.iterator();
                        Map.Entry me = (Map.Entry) iterator.next();

                        final double minDis = Double.parseDouble(me.getValue().toString());

                        //Toast.makeText(getApplicationContext(),minDis+"", Toast.LENGTH_LONG).show();

                        for(Map.Entry<String,Double> entry: distanceMap.entrySet()){
                            final String key = entry.getKey();
                            double value = entry.getValue();

                            if(value == minDis){
                                refDatabase.child("GeoFire").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        MapModel saleLoc = dataSnapshot.getValue(MapModel.class);
                                        employeeLat = Double.parseDouble(saleLoc.getLatitude());
                                        employeeLng = Double.parseDouble(saleLoc.getLongitude());

                                        LatLng onMapClient = new LatLng(employeeLat, employeeLng);

                                        mMap.addMarker(new MarkerOptions().position(onMapClient));

                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(onMapClient,16.0f));
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.string.style_json));

        } catch (Resources.NotFoundException e) {

        }


        refSale.limitToFirst(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                for(DataSnapshot itemSale:snapSale){
                    String saleEmail = itemSale.getValue(Employee.class).getEmployeeEmail();

                    refDatabase.child("GeoFire").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            MapModel map = dataSnapshot.getValue(MapModel.class);

                            LatLng onMapClient = new LatLng(Double.parseDouble(map.getLatitude()),Double.parseDouble(map.getLongitude()));

                            mMap.addMarker(new MarkerOptions().position(onMapClient));

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(onMapClient,16.0f));

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




        getLocationPermission();
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

            if (mMap != null) {
                mMap.setMyLocationEnabled(true);

            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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
                    adapterDetail.notifyDataSetChanged();

                    Employee employee = adapterDetail.getItem(choosenEmployee);
                    String employeeEmail = employee.getEmployeeEmail();
                    refDatabase.child("GeoFire").child(employeeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            MapModel map = dataSnapshot.getValue(MapModel.class);

                            LatLng onMapClient = new LatLng(Double.parseDouble(map.getLatitude()),Double.parseDouble(map.getLongitude()));

                            mMap.addMarker(new MarkerOptions().position(onMapClient));

                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(onMapClient,16.0f));
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

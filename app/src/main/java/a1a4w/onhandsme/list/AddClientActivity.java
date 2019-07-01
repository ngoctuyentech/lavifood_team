package a1a4w.onhandsme.list;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.bytask.ActionList;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.Group;
import a1a4w.onhandsme.model.MapModel;
import a1a4w.onhandsme.utils.MySpinerAdapter;
import fr.ganfra.materialspinner.MaterialSpinner;

import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class AddClientActivity extends AppCompatActivity {

    EditText clientName,clientStreet, clientDist, clientProvince, clientPhone, clientDeliveryService,clientInform, clientDebt,contactName;
    Button addClient;
    private Bundle b = new Bundle();
    private ProgressDialog mProgressDialog;
    private String emailLogin,clientType,clientZone,clientAddress, userEmail;
    public static int MY_REQUEST_LOCATION = 999;
    private double latitude,longitude;
    private MapModel map;
    private Switch swLocation;
    private boolean chooseLocation,saleMan,supervisor;
    int PLACE_PICKER_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_client);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_add_client);
        setSupportActionBar(toolbar);

        Intent it = this.getIntent();
        emailLogin = it.getStringExtra("EmailLogin");
        saleMan = it.getBooleanExtra("SaleMan",false);
        supervisor = it.getBooleanExtra("Supervisor",false);

        clientName = (EditText)findViewById(R.id.edt_update_client_name);
        clientStreet = (EditText)findViewById(R.id.edt_update_client_street);
        clientDist = (EditText)findViewById(R.id.edt_update_client_dist);
        clientProvince = (EditText)findViewById(R.id.edt_update_client_province);
        clientPhone = (EditText)findViewById(R.id.edt_update_client_phone);
        clientInform = (EditText)findViewById(R.id.edt_update_client_inform);
        contactName = findViewById(R.id.edt_add_client_contact_name);
        //clientDeliveryService = (EditText)findViewById(R.id.edt_update_client_delivery_service);
        //clientDebt = (EditText)findViewById(R.id.edt_client_debt);
        //addClient = (Button)findViewById(R.id.btn_add_client);
        swLocation = findViewById(R.id.sw_add_client_location);

        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

        locationPreparation();

        swLocation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    chooseLocation = false;
                    locationPreparation();
                }else{
                    chooseLocation = true;
                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                    try {
                        startActivityForResult(builder.build(AddClientActivity.this), PLACE_PICKER_REQUEST);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        Spinner spinClientType = (Spinner) findViewById(R.id.spinner_client_type);
        Spinner spinClientZone = (Spinner) findViewById(R.id.spinner_add_client_client_zone);


        String[] arrayClientType = this.getResources().getStringArray(R.array.client_type_array);
        List<String> arrayListClient = Arrays.asList(arrayClientType);
        MySpinerAdapter spinerAdapterClient = new MySpinerAdapter();
        spinerAdapterClient.addItems(arrayListClient);
        MaterialSpinner spinnerClient = (MaterialSpinner)findViewById(R.id.spinner_client_type);
        spinnerClient.setAdapter(spinerAdapterClient);
        spinnerClient.setSelection(0);

        spinClientType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clientType = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayClientZone = this.getResources().getStringArray(R.array.client_province_array);
        List<String> arrayListClientZone = Arrays.asList(arrayClientZone);
        MySpinerAdapter spinerAdapterClientZone = new MySpinerAdapter();
        spinerAdapterClientZone.addItems(arrayListClientZone);
        MaterialSpinner spinnerClientZone = (MaterialSpinner)findViewById(R.id.spinner_add_client_client_zone);
        spinnerClientZone.setAdapter(spinerAdapterClientZone);
        spinnerClientZone.setSelection(0);

        spinClientZone.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                clientZone = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                if(location!=null){

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                    map = new MapModel(latitude+"",longitude+"");

                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                    try {
                        clientAddress = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1)
                                .get(0).getAddressLine(0);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }else{
                    Toast.makeText(getApplicationContext(),"Không lấy được vị trí hiện tại, vui lòng thử lại!",Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (MY_REQUEST_LOCATION == requestCode) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling

                    return;
                }

            }
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);


                map = new MapModel(place.getLatLng().latitude+"",place.getLatLng().longitude+"");

                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    clientAddress = geocoder.getFromLocation(place.getLatLng().latitude, place.getLatLng().longitude, 1)
                            .get(0).getAddressLine(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_client,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add_client){

            final String clientNameString = clientName.getText().toString();
            final String clientStreetString = clientStreet.getText().toString();
            final String clientDistString = clientDist.getText().toString();
            final String clientProvinceString = clientProvince.getText().toString();
            final String clientPhoneString = clientPhone.getText().toString();
           // final String clientInformString = clientInform.getText().toString();
           // final String clientDeliveryServiceString = clientDeliveryService.getText().toString();
            final String contact = contactName.getText().toString();
            //final String clientDebtString = clientDebt.getText().toString();
            final String clientOrder = b.getString("ClientOrder");

            if (TextUtils.isEmpty(clientNameString)){
                Toast.makeText(getApplicationContext(),"Vui lòng nhập tên khách hàng", Toast.LENGTH_LONG).show();

            } else if (TextUtils.isEmpty(clientStreetString)){
                Toast.makeText(getApplicationContext(),"Vui lòng nhập tên đường", Toast.LENGTH_LONG).show();

            } else if (TextUtils.isEmpty(clientDistString)) {
                Toast.makeText(getApplicationContext(),"Vui lòng nhập tên quận", Toast.LENGTH_LONG).show();

            }  else if (TextUtils.isEmpty(clientProvinceString)) {
                Toast.makeText(getApplicationContext(),"Vui lòng nhập tên tỉnh", Toast.LENGTH_LONG).show();

            } else if (TextUtils.isEmpty(clientPhoneString)) {
                Toast.makeText(getApplicationContext(),"Vui lòng nhập số điện thoại", Toast.LENGTH_LONG).show();

            } else if(clientZone == null || clientType == null ){
                Toast.makeText(getApplicationContext(),"Đang xử lý...", Toast.LENGTH_LONG).show();
            }

            else {
                AlertDialog.Builder builder = new AlertDialog.Builder(AddClientActivity.this);
                builder.setMessage("Xác nhận địa chỉ khách hàng");

                final EditText edtAddress = new EditText(AddClientActivity.this);
                edtAddress.setHeight(80);
                edtAddress.setWidth(200);
                edtAddress.setGravity(Gravity.CENTER);
                edtAddress.setImeOptions(EditorInfo.IME_ACTION_DONE);
                edtAddress.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                edtAddress.setText(clientAddress);

                builder.setView(edtAddress);

                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                        String address = edtAddress.getText().toString();
                        if(TextUtils.isEmpty(address)){

                            Toast.makeText(getApplicationContext(),"Vui lòng nhập địa chỉ",Toast.LENGTH_LONG).show();
                        }else{
                            showProgressDialog();

                            final String saleEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

                            final DatabaseReference clientPush = refDatabase.child(emailLogin+"/Client").push();
                            final String pushKey = clientPush.getKey();
                            final Client client = new Client(pushKey,clientNameString,clientType, clientStreetString,
                                    clientDistString,clientZone,clientProvinceString,
                                    clientPhoneString, "","0","0",map,saleEmail,contact);

                            clientAddress = address;

                            final Client clientMan = new Client(pushKey,clientNameString,map,clientStreetString);

                            if(supervisor){

                                refDatabase.child(emailLogin).child("ClientManBySup").child(saleEmail).child("Tất cả").child(pushKey).setValue(clientMan);

                                refDatabase.child(emailLogin+"/ClientMan").child(clientType).child(clientZone).child(pushKey).setValue(clientMan);

                                refDatabase.child(emailLogin+"/Client").child(pushKey).setValue(client).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(getApplicationContext(),"Đã thêm khách hàng mới", Toast.LENGTH_LONG).show();
                                        Intent it = new Intent(getApplicationContext(), ActionList.class);
                                        it.putExtra("EmailLogin",emailLogin);
                                        it.putExtra("Supervisor",true);
                                        startActivity(it);
                                    }
                                });
                            }

                            if(saleMan){

                                refDatabase.child(emailLogin).child("SaleManBySup").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> snapSup = dataSnapshot.getChildren();
                                        for(DataSnapshot itemSup:snapSup){
                                            DatabaseReference refSup = itemSup.getRef();
                                            final String supEmail = itemSup.getKey();
                                            //Toast.makeText(getApplicationContext(),supEmail, Toast.LENGTH_LONG).show();

                                            final List<String> groups = new ArrayList<>();
                                            refSup.child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    if(dataSnapshot.hasChild(saleEmail)){
                                                        refDatabase.child(emailLogin).child("ClientManBySup").child(supEmail).child("Group").addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                Iterable<DataSnapshot> snapGroup = dataSnapshot.getChildren();
                                                                long itemCount = dataSnapshot.getChildrenCount();

                                                                int i = 0;
                                                                for(DataSnapshot itemGroup:snapGroup){
                                                                    i++;
                                                                    Group group = itemGroup.getValue(Group.class);
                                                                    String groupName = group.getGroupName();
                                                                    groups.add(groupName);

                                                                    if(i==itemCount){
                                                                        if(!groups.contains("Mới")){
                                                                            refDatabase.child(emailLogin).child("ClientManBySup").child(supEmail).child("Group").push().child("groupName").setValue("Mới");
                                                                        }
                                                                    }
                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                        refDatabase.child(emailLogin).child("ClientManBySup").child(supEmail).child("Mới").child(pushKey).setValue(client).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                Toast.makeText(getApplicationContext(),"Đã thêm khách hàng mới và chờ duyệt!", Toast.LENGTH_LONG).show();
                                                                Intent it = new Intent(getApplicationContext(), ActionList.class);
                                                                it.putExtra("EmailLogin",emailLogin);
                                                                it.putExtra("SaleMan",true);
                                                                startActivity(it);
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

                            }


                        }
                    }
                }).show();


            }


        }
        return super.onOptionsItemSelected(item);
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

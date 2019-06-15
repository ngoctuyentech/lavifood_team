package a1a4w.onhandsme.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Objects;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.MapModel;

import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class UpdateClientActivity extends AppCompatActivity {
    String clientCode;
    EditText clientName,clientStreet, clientDist,clientCity, clientProvince, clientPhone, clientDeliveryService,clientInform, clientDebt;
    Button addClient;
    private Bundle b = new Bundle();
    private String thisYear,thisMonth,emailLogin,employeeEmail,createBy;
    private String clientInformString;
    private String clientDeliveryServiceString;
    private String clientType;
    private String clientProvinceString;
    private MapModel map;
    private boolean saleMan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_client);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_update_client);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        clientCode = intent.getStringExtra("ClientCode");
        emailLogin = intent.getStringExtra("EmailLogin");
        saleMan = intent.getBooleanExtra("SaleMan",false);
        employeeEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail().replace(".",",");

        thisYear = (Calendar.getInstance().get(Calendar.YEAR))+"";
        thisMonth = (Calendar.getInstance().get(Calendar.MONTH)+1)+"";
        clientDebt = findViewById(R.id.edt_update_client_debt);
        clientName = (EditText)findViewById(R.id.edt_update_client_name);
        clientStreet = (EditText)findViewById(R.id.edt_update_client_street);
        clientDist = (EditText)findViewById(R.id.edt_update_client_dist);
        clientCity = (EditText)findViewById(R.id.edt_update_client_city);
        clientPhone = (EditText)findViewById(R.id.edt_update_client_phone);

        if(saleMan){
            clientDebt.setEnabled(false);
            clientName.setEnabled(false);
        }

        refDatabase.child(emailLogin+"/Client").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Client client = dataSnapshot.getValue(Client.class);
                assert client != null;
                clientName.setText(client.getClientName());
                clientStreet.setText(client.getClientStreet());
                clientDist.setText(client.getClientDistrict());
                clientCity.setText(client.getClientCity());
                clientPhone.setText(client.getClientPhone());
                clientDebt.setText(client.getClientDebt());
                clientInformString = client.getClientOrderInform();
                clientDeliveryServiceString = client.getClientDeliveryName();
                clientType = client.getClientType();
                clientProvinceString = client.getClientProvince();
                createBy = client.getCreateBy();
                map = client.getMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update_client,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_update_client){
            String clientNameString = clientName.getText().toString();
            String clientStreetString = clientStreet.getText().toString();
            String clientDistString = clientDist.getText().toString();
            String clientCityString = clientCity.getText().toString();
            String clientPhoneString = clientPhone.getText().toString();
            String clientDebtString = clientDebt.getText().toString();

            if(TextUtils.isEmpty(clientNameString)){
                Toast.makeText(getApplicationContext(),"Vui lòng nhập mã khách hàng", Toast.LENGTH_LONG).show();
            }else if(TextUtils.isEmpty(clientDebtString)){
                Toast.makeText(getApplicationContext(),"Vui lòng nhập công nợ khách hàng", Toast.LENGTH_LONG).show();

            }
            else if (TextUtils.isEmpty(clientStreetString)){
                Toast.makeText(getApplicationContext(),"Vui lòng nhập tên đường", Toast.LENGTH_LONG).show();

            } else if (TextUtils.isEmpty(clientDistString)) {
                Toast.makeText(getApplicationContext(),"Vui lòng nhập tên quận", Toast.LENGTH_LONG).show();

            }  else if (TextUtils.isEmpty(clientCityString)) {
                Toast.makeText(getApplicationContext(), "Vui lòng nhập tên thành phố", Toast.LENGTH_LONG).show();

            }  else if (TextUtils.isEmpty(clientPhoneString)) {
                Toast.makeText(getApplicationContext(),"Vui lòng nhập số điện thoại", Toast.LENGTH_LONG).show();

            }

            else {

/*
                Client client = new Client(clientCode,clientNameString,clientType, clientStreetString,
                        clientDistString,clientCityString,clientProvinceString,
                        clientPhoneString, clientInformString,clientDebtString,"0",map,createBy);

                DatabaseReference clientPush = refDatabase.child(emailLogin+"/Client").child(clientCode);

                refDatabase.child(emailLogin).child("ClientBySale").child(employeeEmail).child(clientCode).child("clientName").setValue(clientNameString);
                refDatabase.child(emailLogin).child("ClientMan").child(clientType).child(clientCityString).child(clientCode)
                        .child("clientName").setValue(clientNameString);

                clientPush.setValue(client).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(getApplicationContext(),"Đã cập nhật thông tin khách hàng", Toast.LENGTH_LONG).show();
                        Intent it = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(it);
                    }
                });

                */
            }

        }
        return super.onOptionsItemSelected(item);
    }
}

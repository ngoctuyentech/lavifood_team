package vn.techlifegroup.wesell.bytask.distribution;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Client;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class MeetClientReportActivity extends AppCompatActivity {
    private String emailLogin,saleName,saleEmail;
    private String choosenYear;
    private String choosenMonth;
    private RecyclerView recyclerView;
    private List<Client> clientList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meet_client_report);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_meet_client);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");
        saleName = intent.getStringExtra("EmployeeName");
        saleEmail = intent.getStringExtra("SaleEmail");
        choosenYear = intent.getStringExtra("ChoosenYear");
        choosenMonth = intent.getStringExtra("ChoosenMonth");

        clientList.clear();

        TextView tvEmployeeName = findViewById(R.id.tv_meet_client_saleName);
        tvEmployeeName.setText(saleName);

        recyclerView = (RecyclerView)findViewById(R.id.rv_meet_client_report);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        DateTime dt = new DateTime();
        DateTime choosenTime = dt.withYear(Integer.parseInt(choosenYear)).withMonthOfYear(Integer.parseInt(choosenMonth));

        final String minDate = choosenTime.dayOfMonth().withMinimumValue().toString("dd/MM/yyyy");
        final String maxDate = choosenTime.dayOfMonth().withMaximumValue().toString("dd/MM/yyyy");
        @SuppressLint("SimpleDateFormat") final DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        refDatabase.child(emailLogin).child("SalesManagement/Meet").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> clientSnap = dataSnapshot.getChildren();
                for(DataSnapshot itemClient : clientSnap){
                    String timeStampIn = itemClient.getKey();
                    final Client client = itemClient.getValue(Client.class);
                    Date date = (new Date(Long.parseLong(timeStampIn)));

                    try {
                        Date startDate = sdf.parse(minDate);
                        Date endDate = sdf.parse(maxDate);
                        if(startDate.before(date)||startDate.equals(date) && endDate.after(date)||endDate.equals(date)){
                            clientList.add(client);

                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
/*
                AdapterMeetClient adapterMeet = new AdapterMeetClient(getApplicationContext(),clientList);
                recyclerView.setAdapter(adapterMeet);
                adapterMeet.notifyDataSetChanged();
                */
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            this.onBackPressed();        }
        return super.onOptionsItemSelected(item);
    }


}

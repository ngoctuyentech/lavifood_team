package a1a4w.onhandsme.bytask.warehouse;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.model.WarehouseIn;
import a1a4w.onhandsme.utils.AdapterStorage;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.MySpinerAdapter;
import a1a4w.onhandsme.utils.Utils;
import fr.ganfra.materialspinner.MaterialSpinner;

import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class ViewInOutHistory extends AppCompatActivity{

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<WarehouseIn, WarehouseInViewHolder> adapterFirebase;
    private LinearLayoutManager linearLayoutManager;
    private Bundle b = new Bundle();
    private Calendar mCalendar;
    private String emailLogin;
    private String choosenYear;
    private String choosenMonth;
    private List<WarehouseIn> listStorage = new ArrayList<>();
    private boolean out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_in_history);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_view_in_history);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");
        out = intent.getBooleanExtra("Out",false);
        listStorage.clear();

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_in_history);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        filterTimeDialog();

    }

    private void filterTimeDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filter_view_month_date, null);

        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        Spinner spinYearFilter = (Spinner)dialogView.findViewById(R.id.spinner_dialog_filter_time_md_year);
        final Spinner spinMonthFilter = (Spinner)dialogView.findViewById(R.id.spinner_dialog_filter_time_md_month);
        Button btnOk = dialogView.findViewById(R.id.btn_view_in_history);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);

                if(choosenYear != null && choosenMonth != null){
                    StorageTransactionList(choosenYear,choosenMonth);
                    dialog.dismiss();

                }else {
                    Toast.makeText(getApplicationContext(),"Vui lòng chọn đầy đủ ngày tháng!",Toast.LENGTH_LONG).show();
                }

            }
        });

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner)dialogView.findViewById(R.id.spinner_dialog_filter_time_md_year);
        spinnerYear.setAdapter(spinerAdapterYear);
        spinnerYear.setSelection(0);

        spinYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0) choosenYear = (String) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayMonth = this.getResources().getStringArray(R.array.month_filter_array);
        List<String> arrayListMonth = Arrays.asList(arrayMonth);
        MySpinerAdapter spinerAdapterMonth = new MySpinerAdapter();
        spinerAdapterMonth.addItems(arrayListMonth);
        MaterialSpinner spinnerMonth = (MaterialSpinner)dialogView.findViewById(R.id.spinner_dialog_filter_time_md_month);
        spinnerMonth.setAdapter(spinerAdapterMonth);
        spinnerMonth.setSelection(0);

        spinMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0) choosenMonth = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void StorageTransactionList(String choosenYear,String choosenMonth) {

        String inOut = out? "Out":"In";

        DateTime dt = new DateTime();
        DateTime choosenTime = dt.withYear(Integer.parseInt(choosenYear)).withMonthOfYear(Integer.parseInt(choosenMonth));

        final String minDate = choosenTime.dayOfMonth().withMinimumValue().toString("dd/MM/yyyy");
        final String maxDate = choosenTime.dayOfMonth().withMaximumValue().toString("dd/MM/yyyy");
        @SuppressLint("SimpleDateFormat") final DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        refDatabase.child(emailLogin).child("WarehouseMan").child(inOut).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> stockSnap = dataSnapshot.getChildren();
                for(DataSnapshot itemStock:stockSnap){
                    String timeStampIn = itemStock.getKey();
                    final WarehouseIn warehouseIn = itemStock.getValue(WarehouseIn.class);
                    Date date = (new Date(Long.parseLong(timeStampIn)));

                    try {
                        Date startDate = sdf.parse(minDate);
                        Date endDate = sdf.parse(maxDate);
                        if(startDate.before(date)||startDate.equals(date) && endDate.after(date)||endDate.equals(date)){
                            listStorage.add(warehouseIn);

                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }

                AdapterStorage adapterStorage = new AdapterStorage(getApplicationContext(),listStorage);
                recyclerView.setAdapter(adapterStorage);
                adapterStorage.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public class WarehouseInViewHolder extends RecyclerView.ViewHolder {
        TextView dateIn, productName, productQuantity, supplier;

        public WarehouseInViewHolder(View itemView) {
            super(itemView);
            dateIn  = (TextView) itemView.findViewById(R.id.tv_view_in_history_date);
            productName  = (TextView) itemView.findViewById(R.id.tv_view_in_history_product);
            productQuantity  = (TextView) itemView.findViewById(R.id.tv_view_in_history_product_quantity);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
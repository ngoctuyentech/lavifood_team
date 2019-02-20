package a1a4w.onhandsme.pos;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import a1a4w.onhandsme.LoginActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.bytask.debt.DebtHistoryActivity;
import a1a4w.onhandsme.bytask.distribution.FilterProductActivity;
import a1a4w.onhandsme.bytask.distribution.FilterTimeActivity;
import a1a4w.onhandsme.bytask.warehouse.ViewStorageTransaction;
import a1a4w.onhandsme.list.ClientListActivity;
import a1a4w.onhandsme.list.EmployeeListActivity;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.Utils;

public class GeneralManagerActivity extends AppCompatActivity implements View.OnClickListener {
    ImageView ivSale, ivDebt, ivWarehouse,ivEmployeeSale;
    String choosenProduct,choosenChannel,choosenYear,choosenMonth,emailLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_general_manager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_general_manager);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

        ivDebt = (ImageView)findViewById(R.id.iv_general_manger_debt);
        ivSale = (ImageView)findViewById(R.id.iv_general_manger_sale);
        ivWarehouse = (ImageView)findViewById(R.id.iv__general_manger_warehouse);
        ivEmployeeSale = (ImageView)findViewById(R.id.iv_general_manager_employeeSale);

        ivDebt.setOnClickListener(this);
        ivSale.setOnClickListener(this);
        ivWarehouse.setOnClickListener(this);
        ivEmployeeSale.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        v.startAnimation(Constants.buttonClick);
        switch (v.getId()){
            case R.id.iv_general_manger_debt:{
                viewClientDebt();
                break;
            }
            case R.id.iv_general_manger_sale:{
                chooseSaleType();
                break;
            }
            case R.id.iv__general_manger_warehouse:{
                viewStorage();
                break;
            }
            case R.id.iv_general_manager_employeeSale: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_filter_time_general,null);
                builder.setView(dialogView);
                //final Dialog dialog = builder.create();
                //dialog.show();
                builder.show();

                final Spinner spinYear = (Spinner)dialogView.findViewById(R.id.spin_filter_time_general_year);
                final Spinner spinMonth = (Spinner)dialogView.findViewById(R.id.spin_filter_time_general_month);
                final Button btnOk = (Button)dialogView.findViewById(R.id.btn_filter_time_general_ok);

                spinYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(position!=0){
                            choosenYear = (String)parent.getItemAtPosition(position);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
                spinMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if(position!=0){
                            choosenMonth = (String)parent.getItemAtPosition(position);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        v.startAnimation(Constants.buttonClick);
                        if(choosenMonth==null || choosenYear==null){
                            Toast.makeText(getApplicationContext(),"Vui lòng chọn năm và tháng",Toast.LENGTH_LONG).show();
                        }else{
                            Intent it = new Intent(getApplicationContext(), EmployeeListActivity.class);
                            it.putExtra("ChoosenYear",choosenYear);
                            it.putExtra("ChoosenMonth",choosenMonth);
                            it.putExtra("EmployeeFilter",true);
                            it.putExtra("EmailLogin",emailLogin);

                            startActivity(it);

                        }
                    }
                });

                break;
            }
            
        }
        
    }

    private void viewStorage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_general_storage,null);
        builder.setView(dialogView);
        //final Dialog dialog = builder.create();
        //dialog.show();
        builder.show();

        final Button btnStorageTime = (Button)dialogView.findViewById(R.id.btn_general_storage_time);
        final Button btnStorageProduct = (Button)dialogView.findViewById(R.id.btn_general_storage_product);

        final Spinner spinProduct = (Spinner)dialogView.findViewById(R.id.spin_general_storage_product);

        btnStorageTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                Intent it = new Intent(GeneralManagerActivity.this, ViewStorageTransaction.class);
                startActivity(it);

            }
        });

        btnStorageProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                spinProduct.setVisibility(View.VISIBLE);
                spinProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String choosenProduct = (String)parent.getItemAtPosition(position);
                        if(position!=0){
                            Intent it = new Intent(GeneralManagerActivity.this, ViewStorageTransaction.class);
                            it.putExtra("ProductStorageTransaction",true);
                            it.putExtra("ProductName",choosenProduct);
                            it.putExtra("EmailLogin",emailLogin);
                            startActivity(it);
                        }

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

            }
        });


    }

    private void chooseSaleType() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_general_sale,null);
        builder.setView(dialogView);
        //final Dialog dialog = builder.create();
        //dialog.show();
        builder.show();

        final Button btnSaleTotal = (Button)dialogView.findViewById(R.id.btn_dialog_general_saleTotal);
        final Button btnSaleProduct = (Button)dialogView.findViewById(R.id.btn_dialog_general_saleProduct);
        final Button btnSaleChannel = (Button)dialogView.findViewById(R.id.btn_dialog_general_saleChannel);
        final Spinner spinProduct = (Spinner)dialogView.findViewById(R.id.spin_general_product);
        final Spinner spinChannel = (Spinner)dialogView.findViewById(R.id.spin_general_channel);
        final Spinner spinYear = (Spinner)dialogView.findViewById(R.id.spin_general_year);
        final Spinner spinMonth = (Spinner)dialogView.findViewById(R.id.spin_general_month);
        final TextView tvWhatMonth = (TextView)dialogView.findViewById(R.id.tv_general_whatMonth);
        final TextView tvWhatYear = (TextView)dialogView.findViewById(R.id.tv_general_whatYear);
        final TextView tvMonthSale = (TextView)dialogView.findViewById(R.id.tv_general_monthSale);
        final TextView tvYearSale = (TextView)dialogView.findViewById(R.id.tv_general_yearSale);

        spinYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    choosenYear = (String)parent.getItemAtPosition(position);
                    tvWhatYear.setText(choosenYear+":");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    choosenMonth = (String)parent.getItemAtPosition(position);
                    tvWhatMonth.setText(choosenMonth+":");
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        btnSaleTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                if(choosenYear == null|| choosenMonth == null){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập đầy đủ năm và tháng",Toast.LENGTH_LONG).show();
                }else{
                    Intent it = new Intent(GeneralManagerActivity.this, FilterTimeActivity.class);
                    String launchFrom = this.getClass().getSimpleName();
                    it.putExtra("ChoosenYear",choosenYear);
                    it.putExtra("ChoosenMonth",choosenMonth);
                    it.putExtra("LaunchFrom",launchFrom);
                    it.putExtra("EmailLogin",emailLogin);

                    startActivity(it);
                }


            }
        });

        btnSaleProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                if(choosenYear == null|| choosenMonth == null){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập đầy đủ năm và tháng",Toast.LENGTH_LONG).show();
                }else{
                    btnSaleChannel.setVisibility(View.INVISIBLE);
                    btnSaleTotal.setVisibility(View.INVISIBLE);
                    tvMonthSale.setVisibility(View.INVISIBLE);
                    tvWhatMonth.setVisibility(View.INVISIBLE);
                    tvWhatYear.setVisibility(View.INVISIBLE);
                    tvYearSale.setVisibility(View.INVISIBLE);

                    spinProduct.setVisibility(View.VISIBLE);
                    spinProduct.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            choosenProduct = (String) parent.getItemAtPosition(position);
                            if(position!=0){
                                Intent it = new Intent(GeneralManagerActivity.this, FilterProductActivity.class);
                                //String launchFrom = this.getClass().getSimpleName();
                                it.putExtra("ChoosenYear",choosenYear);
                                it.putExtra("ChoosenMonth",choosenMonth);
                                it.putExtra("ChoosenProduct",choosenProduct);
                                it.putExtra("EmailLogin",emailLogin);

                                startActivity(it);
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

            }
        });

        btnSaleChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                if(choosenYear == null|| choosenMonth == null){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập đầy đủ năm và tháng",Toast.LENGTH_LONG).show();
                }else{
                    btnSaleProduct.setVisibility(View.INVISIBLE);
                    btnSaleTotal.setVisibility(View.INVISIBLE);
                    spinChannel.setVisibility(View.VISIBLE);
                    spinChannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            choosenChannel = (String) parent.getItemAtPosition(position);
                            if(position!=0){
                                Constants.refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("SaleByChannel")){
                                            Constants.refDatabase.child(emailLogin).child("SaleByChannel").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(choosenChannel)){
                                                        Constants.refDatabase.child(emailLogin).child("SaleByChannel").child(choosenChannel).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if(dataSnapshot.hasChild(choosenYear)){
                                                                    Constants.refDatabase.child(emailLogin).child("SaleByChannel").child(choosenChannel).child(choosenYear).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            tvYearSale.setText(Utils.convertNumber(dataSnapshot.getValue().toString()));
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                                }else{
                                                                    tvYearSale.setText("Chưa phát sinh");
                                                                }

                                                                if(dataSnapshot.hasChild(choosenYear+choosenMonth)){
                                                                    Constants.refDatabase.child(emailLogin).child("SaleByChannel").child(choosenChannel).child(choosenYear+choosenMonth).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            tvMonthSale.setText(Utils.convertNumber(dataSnapshot.getValue().toString()));
                                                                        }

                                                                        @Override
                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                        }
                                                                    });
                                                                }else{
                                                                    tvMonthSale.setText("Chưa phát sinh");
                                                                }

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }else{
                                                        tvYearSale.setText("Chưa phát sinh");
                                                        tvMonthSale.setText("Chưa phát sinh");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }else{
                                            tvYearSale.setText("Chưa phát sinh");
                                            tvMonthSale.setText("Chưa phát sinh");

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });
                }

            }
        });

    }

    private void viewClientDebt() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_general_debt,null);
        builder.setView(dialogView);
        //final Dialog dialog = builder.create();
        //dialog.show();
        builder.show();

        final Button btnDebtByClient = (Button)dialogView.findViewById(R.id.btn_general_debt_client);
        final Button btnDebtByTime = (Button)dialogView.findViewById(R.id.btn_general_debt_time);
        final Spinner spinYear = (Spinner)dialogView.findViewById(R.id.spin_general_debt_year);
        final Spinner spinMonth = (Spinner)dialogView.findViewById(R.id.spin_general_debt_month);

        spinYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    choosenYear = (String)parent.getItemAtPosition(position);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position!=0){
                    choosenMonth = (String)parent.getItemAtPosition(position);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnDebtByClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                if(choosenYear == null|| choosenMonth == null){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập đầy đủ năm và tháng",Toast.LENGTH_LONG).show();
                }else{
                    Intent it = new Intent(GeneralManagerActivity.this, ClientListActivity.class);
                    it.putExtra("ChoosenYear",choosenYear);
                    it.putExtra("ChoosenMonth",choosenMonth);
                    it.putExtra("DebtHistory",true);
                    it.putExtra("EmailLogin",emailLogin);

                    startActivity(it);
                }

            }
        });

        btnDebtByTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                if(choosenYear == null|| choosenMonth == null){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập đầy đủ năm và tháng",Toast.LENGTH_LONG).show();
                }else{
                    Intent it = new Intent(GeneralManagerActivity.this, DebtHistoryActivity.class);
                    it.putExtra("ChoosenYear",choosenYear);
                    it.putExtra("ChoosenMonth",choosenMonth);
                    it.putExtra("DebtByTime",true);
                    it.putExtra("EmailLogin",emailLogin);

                    startActivity(it);
                }

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_general,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_general_logout){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}

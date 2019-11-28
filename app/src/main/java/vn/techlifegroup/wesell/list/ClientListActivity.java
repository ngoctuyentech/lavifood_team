package vn.techlifegroup.wesell.list;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
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

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import vn.techlifegroup.wesell.MainActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.bytask.debt.DebtHistoryActivity;
import vn.techlifegroup.wesell.bytask.distribution.FilterClientActivity;
import vn.techlifegroup.wesell.model.CashTransfer;
import vn.techlifegroup.wesell.model.Client;
import vn.techlifegroup.wesell.model.DebtHistory;
import vn.techlifegroup.wesell.order.UpdateOrderActivity;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.MySpinerAdapter;
import vn.techlifegroup.wesell.utils.Utils;
import fr.ganfra.materialspinner.MaterialSpinner;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class ClientListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Client,ClientViewHolder> adapterFirebase;
    private DatabaseReference refCurrentClient;
    private String choosenYear, choosenMonth;
    private Bundle b = new Bundle();
    private boolean clientFilter,updateDebt,updateClient,debtHistory,saleMan;
    private int thisMonth, thisYear, thisDate,thisQuater;
    private String thisMonthString, thisYearString, thisDateString, thisQuarterString,timeStamp,emailLogin,clientType,clientZone;
    private LinearLayoutManager linearLayoutManager;
    private String clientDebt;
    private Switch switchCash;
    private boolean cash;
    private String clientCode,clientName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_order);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_new_order);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        Intent it = this.getIntent();
        choosenYear = it.getStringExtra("ChoosenYear");
        choosenMonth = it.getStringExtra("ChoosenMonth");
        clientFilter = it.getBooleanExtra("ClientFilter",false);
        updateDebt = it.getBooleanExtra("UpdateDebt",false);
        updateClient = it.getBooleanExtra("UpdateClient",false);
        debtHistory = it.getBooleanExtra("DebtHistory",false);
        emailLogin = it.getStringExtra("EmailLogin");
        saleMan = it.getBooleanExtra("SaleMan",false);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_new_order);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(Constants.buttonClick);
                createNewClientDialog();

            }
        });
        timeStamp = (Calendar.getInstance().getTime().getTime())+"";

        thisMonth = Calendar.getInstance().get(Calendar.MONTH) +1;
        thisMonthString = thisMonth+"";
        thisYear = Calendar.getInstance().get(Calendar.YEAR);
        thisYearString = thisYear+"";
        thisDate = Calendar.getInstance().get(Calendar.DATE);
        thisDateString = thisDate+"";

        recyclerView = (RecyclerView)findViewById(R.id.new_order_recyclerview);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        filterClientDialog();

    }

    private void filterClientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_order_filter_client,null);
        builder.setView(dialogView);
        builder.setTitle("Phân loại khách hàng");

        final Dialog dialog = builder.create();
        dialog.show();

        Spinner spinClientType = (Spinner)dialogView.findViewById(R.id.spiner_new_order_client_type);
        Spinner spinClientZone = (Spinner)dialogView.findViewById(R.id.spiner_new_order_client_zone);
        Button btnOK = (Button)dialogView.findViewById(R.id.btn_dialog_new_order_ok);
        Button btnNew = dialogView.findViewById(R.id.btn_filter_client_new);

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewClientDialog();

            }
        });

        String[] arrayClientType = this.getResources().getStringArray(R.array.client_type_array);
        List<String> arrayListClient = Arrays.asList(arrayClientType);
        MySpinerAdapter spinerAdapterClient = new MySpinerAdapter();
        spinerAdapterClient.addItems(arrayListClient);
        MaterialSpinner spinnerClient = (MaterialSpinner)dialogView.findViewById(R.id.spiner_new_order_client_type);
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
        MaterialSpinner spinnerClientZone = (MaterialSpinner)dialogView.findViewById(R.id.spiner_new_order_client_zone);
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

        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);

                dialog.dismiss();

                refCurrentClient = refDatabase.child(emailLogin).child("ClientMan").child(clientType).child(clientZone);

                adapterFirebase = new FirebaseRecyclerAdapter<Client, ClientViewHolder>(
                        Client.class,
                        R.id.item_client,
                        ClientViewHolder.class,
                        refCurrentClient
                ) {
                    @Override
                    public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client,parent,false);
                        return new ClientViewHolder(v);
                    }

                    @Override
                    protected void populateViewHolder(ClientViewHolder viewHolder, Client model, int position) {
                        viewHolder.clientNameView.setText(model.getClientName());

                    }
                };

                recyclerView.setAdapter(adapterFirebase);
                adapterFirebase.notifyDataSetChanged();

            }
        });

    }

    public class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView clientNameView;

        public ClientViewHolder(View itemView) {
            super(itemView);
            clientNameView = (TextView) itemView.findViewById(R.id.tv_item_client_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    final Client client = adapterFirebase.getItem(position);

                    clientCode = adapterFirebase.getRef(position).getKey();
                    clientName = client.getClientName();

                    if(clientFilter) {
                        Intent intent = new Intent(getApplicationContext(), FilterClientActivity.class);
                        intent.putExtra("ChoosenMonth", choosenMonth);
                        intent.putExtra("ChoosenYear", choosenYear);
                        intent.putExtra("ClientCode", clientCode);
                        intent.putExtra("EmailLogin",emailLogin);
                        startActivity(intent);
                    }else if(debtHistory){
                        Intent intent = new Intent(getApplicationContext(), DebtHistoryActivity.class);
                        intent.putExtra("ChoosenMonth", choosenMonth);
                        intent.putExtra("ChoosenYear", choosenYear);
                        intent.putExtra("ClientName", clientName);
                        startActivity(intent);

                    }else if(updateDebt){

                        LayoutInflater inflater = getLayoutInflater();
                        View dialogView = inflater.inflate(R.layout.dialog_update_debt, null);

                        AlertDialog.Builder builder= new AlertDialog.Builder(dialogView.getContext());
                        builder.setView(dialogView);

                        final AlertDialog dialog = builder.create();
                        dialog.show();

                        builder.setTitle("Cập nhật công nợ");

                        final EditText updateDebt = (EditText)dialogView.findViewById(R.id.edt_update_debt_dialog);
                        //updateDebt.addTextChangedListener(new Utils.NumberTextWatcherForThousand(updateDebt));

                        final Button updateCancel = (Button)dialogView.findViewById(R.id.btn_update_debt_cancel_dialog);
                        Button updateOk = (Button) dialogView.findViewById(R.id.btn_update_debt_ok_dialog);
                        final TextView tvClientName = (TextView)dialogView.findViewById(R.id.tv_dialog_update_debt_client_name);
                        final EditText edtAccountName = (EditText) dialogView.findViewById(R.id.edt_dialog_update_debt_account);
                        final EditText edtDateOfTransfer = (EditText)dialogView.findViewById(R.id.edt_dialog_update_debt_dateTransfer);
                        final EditText edtBankNumber = (EditText)dialogView.findViewById(R.id.edt_dialog_update_debt_bank_number);
                        final EditText edtBankName = (EditText)dialogView.findViewById(R.id.edt_dialog_update_debt_bank_name);

                        final TextView tvClientDebt = (TextView) dialogView.findViewById(R.id.tv_dialog_update_debt_currentdebt);
                        switchCash = (Switch)dialogView.findViewById(R.id.switch_dialog_update_debt_cash);

                        tvClientName.setText(clientName);
                        refDatabase.child(emailLogin).child("Client").child(clientCode).child("clientDebt").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                clientDebt = dataSnapshot.getValue().toString();
                                tvClientDebt.setText(Utils.convertNumber(clientDebt));

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        switchCash.setChecked(false);

                        switchCash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    switchCash.setText("Có");
                                    edtAccountName.setVisibility(View.INVISIBLE);
                                    edtDateOfTransfer.setVisibility(View.INVISIBLE);
                                    edtBankNumber.setVisibility(View.INVISIBLE);
                                    edtBankName.setVisibility(View.INVISIBLE);

                                    cash = true;
                                }else {
                                    switchCash.setText("Không");
                                    edtAccountName.setVisibility(View.VISIBLE);
                                    edtDateOfTransfer.setVisibility(View.VISIBLE);
                                    edtBankNumber.setVisibility(View.VISIBLE);
                                    edtBankName.setVisibility(View.VISIBLE);
                                    cash = false;

                                }
                            }
                        });

                        updateCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                v.startAnimation(Constants.buttonClick);
                                dialog.dismiss();
                            }
                        });

                        updateOk.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                v.startAnimation(Constants.buttonClick);
                                String debtUpdated = updateDebt.getText().toString();
                                String accountName = edtAccountName.getText().toString();
                                String dateOfTransfer = edtDateOfTransfer.getText().toString();
                                String bankNumber = edtBankNumber.getText().toString();
                                String bankName = edtBankName.getText().toString();

                                if(!cash){
                                    b.putString("PaymentCash","Chuyển khoản");
                                    if(TextUtils.isEmpty(debtUpdated)){
                                        Toast.makeText(getApplicationContext(),"Vui lòng nhập công nợ", Toast.LENGTH_LONG).show();
                                    }else if(TextUtils.isEmpty(accountName)){
                                        Toast.makeText(getApplicationContext(),"Vui lòng nhập số tài khoản", Toast.LENGTH_LONG).show();

                                    }else if(TextUtils.isEmpty(dateOfTransfer)){
                                        Toast.makeText(getApplicationContext(),"Vui lòng nhập ngày chuyển khoản", Toast.LENGTH_LONG).show();
                                    }else if(TextUtils.isEmpty(bankName)){
                                        Toast.makeText(getApplicationContext(),"Vui lòng nhập số lệnh ngân hàng", Toast.LENGTH_LONG).show();
                                    }else if(TextUtils.isEmpty(bankNumber)){
                                        Toast.makeText(getApplicationContext(),"Vui lòng nhập tên ngân hàng", Toast.LENGTH_LONG).show();

                                    }else if(clientDebt==null) {
                                        Toast.makeText(getApplicationContext(), "Đang xử lý...", Toast.LENGTH_LONG).show();
                                    }else if(clientName==null){

                                        Toast.makeText(getApplicationContext(), "Đang xử lý...", Toast.LENGTH_LONG).show();

                                    }else{
                                        CashTransfer cashTransfer = new CashTransfer(clientName,debtUpdated,accountName,dateOfTransfer);
                                        refDatabase.child("Accounting").child("CashTransfer").child(clientName).child(thisYearString+thisMonthString).setValue(cashTransfer);
                                        b.putString("ClientDebtDialog",clientDebt);
                                        float updateDebt = Float.parseFloat(clientDebt)- Float.parseFloat(debtUpdated);
                                        DebtHistory debtHistory = new DebtHistory(clientName,clientDebt,"0",debtUpdated,updateDebt+"");
                                        refDatabase.child("DebtHistory").child(timeStamp).setValue(debtHistory);
                                        refDatabase.child("DebtByClient").child(clientName).child(timeStamp).setValue(debtHistory);


                                        b.putString("UpdateDebt",updateDebt+"");
                                        Constants.refClient.child(clientCode).child("clientDebt").setValue(updateDebt+"");
                                        Constants.refClientMan.child(clientType).child(clientZone).child(clientCode).child("clientDebt").setValue(updateDebt+"");
                                        dialog.dismiss();
                                        updateDebtResultDialog();

                                    }
                                }else{
                                    b.putString("PaymentCash","Tiền mặt");

                                    b.putString("ClientDebtDialog",clientDebt);
                                    float updateDebt = Float.parseFloat(clientDebt)- Float.parseFloat(debtUpdated);
                                    DebtHistory debtHistory = new DebtHistory(clientName,clientDebt,"0",debtUpdated,updateDebt+"");
                                    refDatabase.child("DebtHistory").child(clientCode).child(thisYearString+thisMonthString).push().setValue(debtHistory);
                                    b.putString("UpdateDebt",updateDebt+"");
                                    Constants.refClient.child(clientCode).child("clientDebt").setValue(updateDebt+"");
                                    dialog.dismiss();
                                    refDatabase.child("Accounting").child("MoneyBack").child(accountName).child(String.valueOf(thisYear+thisMonth+thisDate)).setValue(debtUpdated);
                                    updateDebtResultDialog();
                                }

                            }
                        });

                    } else if(updateClient){
                        Intent it = new Intent(getApplicationContext(),UpdateClientActivity.class);
                        it.putExtra("ClientCode",clientCode);
                        startActivity(it);
                    //New order
                    }else{
                        refDatabase.child(emailLogin+"/Client").child(clientCode).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Client client = dataSnapshot.getValue(Client.class);
                                Intent intent = new Intent(getApplicationContext(),UpdateOrderActivity.class);
                                assert client != null;
                                intent.putExtra("ClientName", client.getClientName());
                                intent.putExtra("ClientType", client.getClientType());
                                intent.putExtra("ClientCode",client.getClientCode());
                                intent.putExtra("EmailLogin",emailLogin);
                                intent.putExtra("SaleMan",saleMan);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }


                }
            });


        }
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

    private void updateDebtResultDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_update_debt_result,null);
        dialogBuilder.setView(dialogView);

        TextView tvClientName = (TextView)dialogView.findViewById(R.id.tv_update_debt_result_clientname);
        TextView tvOldDebt = (TextView)dialogView.findViewById(R.id.tv_update_debt_result_old_debt);
        TextView tvNewDebt = (TextView)dialogView.findViewById(R.id.tv_update_debt_result_new_debt);
        TextView tvpaymentType = (TextView)dialogView.findViewById(R.id.tv_update_debt_result_payment_type);

        String clientName = b.getString("ClientName");
        String oldDebt = b.getString("ClientDebtDialog");
        String newDebt = b.getString("UpdateDebt");
        String paymentType = b.getString("PaymentCash");
        if(clientName == null || oldDebt == null ||newDebt == null || paymentType == null){
            Toast.makeText(getApplicationContext(),"Đang xử lý",Toast.LENGTH_LONG).show();
        }else{
            tvpaymentType.setText(paymentType);
            tvClientName.setText(clientName);
            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

            float oldDebtFloat = Float.parseFloat(oldDebt);
            float newDebtFloat = Float.parseFloat(newDebt);

            String oldDebtFloatConverted = numberFormat.format(oldDebtFloat);
            String newDebtFloatConverted = numberFormat.format(newDebtFloat);

            tvOldDebt.setText(oldDebtFloatConverted);
            tvNewDebt.setText(newDebtFloatConverted);

        }
        dialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = dialogBuilder.create();
        dialog.show();
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

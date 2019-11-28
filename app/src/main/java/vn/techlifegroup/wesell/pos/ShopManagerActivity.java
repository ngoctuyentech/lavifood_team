package vn.techlifegroup.wesell.pos;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import vn.techlifegroup.wesell.LoginActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Bill;
import vn.techlifegroup.wesell.model.Cash;
import vn.techlifegroup.wesell.model.Employee;
import vn.techlifegroup.wesell.model.Product;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

import java.util.Calendar;

public class ShopManagerActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView ivCash,ivDoc,ivSale,ivWarehouse;
    private Bundle b = new Bundle();
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Cash,CashViewHolder> adapterFirebase;
    private FirebaseRecyclerAdapter<Product,ProductStorageViewHolder> adapterFirebaseShopStorage;
    private FirebaseRecyclerAdapter<Product,ProductStorageViewHolder2> adapterFirebaseShopStorageIn;
    private FirebaseRecyclerAdapter<Product,ProductStorageViewHolder>adapterFirebaseStorageCheck;
    private FirebaseRecyclerAdapter<Bill,BillViewHolder>adapterFirebaseBillCash;
    private ConstraintLayout constraintView;

    private String emailLogin,choosenYear,choosenMonth,shopCode, employeeEmail,choosenMonthSale,choosenMonthTransfer,choosenMonthProductTransfer,thisYear,thisMonth,thisDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_manager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_shop_manager);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");


        constraintView = (ConstraintLayout)findViewById(R.id.activity_shop_manager);


        thisYear = (Calendar.getInstance().get(Calendar.YEAR))+"";
        thisMonth = (Calendar.getInstance().get(Calendar.MONTH)+1)+"";
        thisDate = (Calendar.getInstance().get(Calendar.DATE))+"";


        ivCash = (ImageView)findViewById(R.id.iv_shopman_cashout);
        ivDoc = (ImageView)findViewById(R.id.iv_shopman_doc);
        ivSale = (ImageView)findViewById(R.id.iv_shopman_sale);
        ivWarehouse = (ImageView)findViewById(R.id.iv_shopman_warehouse);

        ivCash.setOnClickListener(this);
        ivDoc.setOnClickListener(this);
        ivSale.setOnClickListener(this);
        ivWarehouse.setOnClickListener(this);

        employeeEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
        Constants.refDatabase.child(emailLogin).child("Z_POS_Employee").child(employeeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Employee employee = dataSnapshot.getValue(Employee.class);
                shopCode = employee.getShopCode();
                b.putString("ShopCode",shopCode);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Constants.refRole.child(employeeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String userRole = dataSnapshot.getValue().toString();
                if(userRole.equals("ShopMan")){
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("RequestPending")){
                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").child("RequestPending").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(shopCode)){
                                            Snackbar.make(constraintView,"Vui lòng xác nhận số tiền xuất quỹ",Snackbar.LENGTH_INDEFINITE)
                                                    .setAction("Xem", new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").child("RequestPending").child(shopCode).child("DateKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    final String dateKey = dataSnapshot.getValue().toString();
                                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").child("RequestPending").child(shopCode).child("Email").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            String email = dataSnapshot.getValue().toString();
                                                                            cashOutConfirmDialog(dateKey,email);

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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }



    @Override
    public void onClick(View v) {
        v.startAnimation(Constants.buttonClick);
        switch (v.getId()){
            case R.id.iv_shopman_cashout:{
                viewCashOutHistory();
                break;
            }
            case R.id.iv_shopman_doc:{
                viewDocTransfer();
                break;
            }
            case R.id.iv_shopman_sale:{
                viewShopSale();
                break;
            }
            case R.id.iv_shopman_warehouse:{
                chooseStoreActionDialog();
                break;
            }
        }
    }

    private void viewDocTransfer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_doc_transfer,null);
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        Button btnDocCash = (Button)dialogView.findViewById(R.id.btn_doc_transfer_cash);
        Button btnDocProduct = (Button)dialogView.findViewById(R.id.btn_doc_transfer_product);
        btnDocCash.setVisibility(View.VISIBLE);

        btnDocCash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                cashTransferDialog();
                dialog.dismiss();

            }
        });

        btnDocProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                productTransferDialog();
                dialog.dismiss();
            }
        });
    }

    private void cashTransferDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cash_transfer,null);
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        final TextView tvTransferTo = (TextView)dialogView.findViewById(R.id.tv_cash_transfer_name_to);
        final TextView tvTransferReceived = (TextView)dialogView.findViewById(R.id.tv_cash_transfer_name_receive);
        final TextView tvCashFirst = (TextView)dialogView.findViewById(R.id.tv_cash_transfer_cashFirst);
        final TextView tvCashBill= (TextView)dialogView.findViewById(R.id.tv_cash_transfer_cash_cashBill);
        final TextView tvCashOut = (TextView)dialogView.findViewById(R.id.tv_cash_transfer_cash_cashOut);
        final TextView tvCashTotal = (TextView)dialogView.findViewById(R.id.tv_cash_transfer_cash_cashTotal);
        Spinner spinMonth = (Spinner)dialogView.findViewById(R.id.spin_cash_transfer_month);
        final EditText edtDateTransfer = (EditText)dialogView.findViewById(R.id.edt_cash_transfer_date);
        final RecyclerView billList = (RecyclerView)dialogView.findViewById(R.id.recycler_cash_transfer);

        spinMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosenMonthTransfer =(String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        edtDateTransfer.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    //Toast.makeText(getApplicationContext(),"Done",Toast.LENGTH_LONG).show();
                    final String dateTransfer = edtDateTransfer.getText().toString();
                    Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(thisYear+choosenMonthTransfer+dateTransfer)){
                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear+choosenMonthTransfer+dateTransfer).child("Request").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String email = dataSnapshot.getValue().toString();
                                        Constants.refDatabase.child(emailLogin).child("Z_POS_Employee").child(email).child("employeeName").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                tvTransferTo.setText(dataSnapshot.getValue().toString());
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
                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear+choosenMonthTransfer+dateTransfer).child("Confirm").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String emailConfirm = dataSnapshot.getValue().toString();
                                        if(emailConfirm.equals("False")){
                                            Toast.makeText(getApplicationContext(),"Bàn giao tiền mặt chưa hoàn thành",Toast.LENGTH_LONG).show();
                                        }else{
                                            Constants.refDatabase.child(emailLogin).child("Z_POS_Employee").child(emailConfirm).child("employeeName").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    tvTransferReceived.setText(dataSnapshot.getValue().toString());
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
                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear+choosenMonthTransfer+dateTransfer).child("cash").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Cash cash = dataSnapshot.getValue(Cash.class);
                                        tvCashFirst.setText(Utils.convertNumber(cash.getCashFirst()));
                                        tvCashBill.setText(Utils.convertNumber(cash.getCashBill()));
                                        tvCashOut.setText(Utils.convertNumber(cash.getCashOut()));
                                        tvCashTotal.setText(Utils.convertNumber(cash.getCashTotal()));



                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                Toast.makeText(getApplicationContext(),"Không có dữ liệu bàn giao vào thời điểm được chọn.",Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    billList.setHasFixedSize(true);
                    linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    billList.setLayoutManager(linearLayoutManager);

                    DatabaseReference refBillList = Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear+choosenMonthTransfer+dateTransfer).child("BillList");

                    adapterFirebaseBillCash = new FirebaseRecyclerAdapter<Bill,BillViewHolder>(
                            Bill.class,
                            R.id.item_bill_info,
                            BillViewHolder.class,
                            refBillList
                    ) {
                        @Override
                        public BillViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill_info,parent,false);
                            return new BillViewHolder(v);
                        }


                        @Override
                        protected void populateViewHolder(BillViewHolder viewHolder, Bill model, int position) {
                            viewHolder.billCode.setText(model.getBillCode());
                            viewHolder.billPayment.setText(Utils.convertNumber(model.getPayment()));
                        }
                    };

                    billList.setAdapter(adapterFirebaseBillCash);
                    adapterFirebaseBillCash.notifyDataSetChanged();

                    return true;
                }
                return false;
            }
        });



    }

    private void productTransferDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_product_transfer,null);
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        Spinner spinMonth = (Spinner)dialogView.findViewById(R.id.spin_product_transfer_month);
        final EditText edtDate = (EditText)dialogView.findViewById(R.id.edt_product_transfer_date);
        final RecyclerView productStorageList = (RecyclerView)dialogView.findViewById(R.id.recycler_product_transfer);
        final TextView tvNameTo = (TextView)dialogView.findViewById(R.id.tv_product_transfer_name_to);
        final TextView tvNameReceive = (TextView)dialogView.findViewById(R.id.tv_product_transfer_name_receive);
        Button btnOk = (Button)dialogView.findViewById(R.id.btn_product_transfer_ok);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                dialog.dismiss();
            }
        });

        spinMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosenMonthProductTransfer = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        edtDate.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_NEXT){
                    final String dateTransfer = edtDate.getText().toString();

                    InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("Request").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(thisYear+choosenMonthProductTransfer+dateTransfer)){
                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("Request").child(shopCode).child(thisYear+choosenMonthProductTransfer+dateTransfer).child("Request").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String email = dataSnapshot.getValue().toString();
                                        Constants.refDatabase.child(emailLogin).child("Z_POS_Employee").child(email).child("employeeName").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                tvNameTo.setText(dataSnapshot.getValue().toString());
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
                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("Request").child(shopCode).child(thisYear+choosenMonthProductTransfer+dateTransfer).child("Confirm").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String emailConfirm = dataSnapshot.getValue().toString();
                                        if(emailConfirm.equals("False")){
                                            Toast.makeText(getApplicationContext(),"Bàn giao hàng hóa chưa hoàn thành",Toast.LENGTH_LONG).show();
                                        }else{
                                            Constants.refDatabase.child(emailLogin).child("Z_POS_Employee").child(emailConfirm).child("employeeName").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    tvNameReceive.setText(dataSnapshot.getValue().toString());
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

                            }else{
                                Toast.makeText(getApplicationContext(),"Không có dữ liệu bàn giao vào thời điểm được chọn.",Toast.LENGTH_LONG).show();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });

                    productStorageList.setHasFixedSize(true);
                    linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    productStorageList.setLayoutManager(linearLayoutManager);

                    adapterFirebaseStorageCheck = new FirebaseRecyclerAdapter<Product,ProductStorageViewHolder>(
                            Product.class,
                            R.id.item_product_view_storage,
                            ProductStorageViewHolder.class,
                            Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("Request").child(shopCode)
                                    .child(thisYear+choosenMonthProductTransfer+dateTransfer).child("StorageList")
                    ) {
                        @Override
                        public ProductStorageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_view_storage,parent,false);
                            return new ProductStorageViewHolder(v);
                        }


                        @Override
                        protected void populateViewHolder(ProductStorageViewHolder viewHolder, Product model, int position) {
                            viewHolder.productName.setText(model.getProductName());
                            viewHolder.productQuantity.setText(model.getUnitQuantity());

                        }
                    };

                    productStorageList.setAdapter(adapterFirebaseStorageCheck);
                    adapterFirebaseStorageCheck.notifyDataSetChanged();

                    return true;
                }
                return false;
            }
        });


    }

    private void viewShopSale() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_view_shop_sale, null);
        builder.setTitle("Xem doanh số của cửa hàng");
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        Spinner spinMonth = (Spinner)dialogView.findViewById(R.id.spin_dialog_view_shop);
        final EditText edtDate = (EditText)dialogView.findViewById(R.id.edt_dialog_view_shop);
        final TextView tvMonthSale = (TextView)dialogView.findViewById(R.id.tv_dialog_view_shop_month);
        final TextView tvDateSale = (TextView)dialogView.findViewById(R.id.tv_dialog_view_shop_date);

        spinMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosenMonthSale = (String) parent.getItemAtPosition(position);
                //b.putString("ChoosenYear",choosenYear);
                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreSale").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(thisYear+choosenMonthSale)){
                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreSale").child(shopCode).child(thisYear+choosenMonthSale).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    tvMonthSale.setText(Utils.convertNumber(dataSnapshot.getValue().toString()));

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }else{
                            tvMonthSale.setText("0");
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        edtDate.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    final String choosenDate = edtDate.getText().toString();
                    Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreSale").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(thisYear+choosenMonthSale+choosenDate)){
                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreSale").child(shopCode).child(thisYear+choosenMonthSale+choosenDate).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        tvDateSale.setText(Utils.convertNumber(dataSnapshot.getValue().toString()));
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                tvDateSale.setText(Utils.convertNumber("0"));

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    return true;
                }
                return false;
            }
        });
    }

    private void viewCashOutHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cash_history, null);
        builder.setTitle("Lịch sử rút tiền mặt");
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        Spinner spinYear = (Spinner) dialogView.findViewById(R.id.spin_dialog_cash_history_year);
        Spinner spinMonth = (Spinner) dialogView.findViewById(R.id.spin_dialog_cash_history_month);
        RecyclerView cashHistoryList = (RecyclerView) dialogView.findViewById(R.id.recycler_dialog_cash_history);

        spinYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 choosenYear = (String) parent.getItemAtPosition(position);
                //b.putString("ChoosenYear",choosenYear);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinMonth.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosenMonth = (String) parent.getItemAtPosition(position);
               // b.putString("ChoosenMonth",choosenMonth);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        cashHistoryList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        cashHistoryList.setLayoutManager(linearLayoutManager);

        if(shopCode ==null){
            Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_SHORT).show();
        }else{
            String choosenTime = choosenYear+choosenMonth;
            DatabaseReference refCashHistory = Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutHistory").child(shopCode).child(choosenTime);

            adapterFirebase = new FirebaseRecyclerAdapter<Cash,CashViewHolder>(
                    Cash.class,
                    R.id.item_cash_out_history,
                    CashViewHolder.class,
                    refCashHistory
            ) {
                @Override
                public CashViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cash_out_history,parent,false);
                    return new CashViewHolder(v);
                }


                @Override
                protected void populateViewHolder(CashViewHolder viewHolder, Cash model, int position) {
                    viewHolder.cashOut.setText(model.getCashOut());
                    viewHolder.timeStamp.setText(Utils.getDateCurrentTimeZone(Long.parseLong(model.getTimeStamp())));
                }
            };

            cashHistoryList.setAdapter(adapterFirebase);
            adapterFirebase.notifyDataSetChanged();
        }

    }

    private void chooseStoreActionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_storeman_store,null);
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        Button btnViewStore = (Button)dialogView.findViewById(R.id.btn_dialog_storeman_store_view);
        Button btnStoreIn = (Button)dialogView.findViewById(R.id.btn_dialog_storeman_store_in);
        btnStoreIn.setVisibility(View.VISIBLE);

        btnViewStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                shopStorageDialog();
                dialog.dismiss();

            }
        });

        btnStoreIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                shopStorageDialogIn();
                dialog.dismiss();
            }
        });

    }

    private void shopStorageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_check_storage,null);
        builder.setView(dialogView);

        final Dialog dialog = builder.create();
        dialog.show();


        final RecyclerView productStorageList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_check_storage);
        Button btnOk = (Button)dialogView.findViewById(R.id.btn_dialog_check_storage);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                dialog.dismiss();
            }
        });


        productStorageList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productStorageList.setLayoutManager(linearLayoutManager);

        productStorageList.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager)productStorageList.getLayoutManager()).findFirstVisibleItemPosition();
                productStorageList.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });

        adapterFirebaseShopStorage = new FirebaseRecyclerAdapter<Product,ProductStorageViewHolder>(
                Product.class,
                R.id.item_product_view_storage,
                ProductStorageViewHolder.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode)
        ) {
            @Override
            public ProductStorageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_view_storage,parent,false);
                return new ProductStorageViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductStorageViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productQuantity.setText(model.getUnitQuantity());

            }
        };

        productStorageList.setAdapter(adapterFirebaseShopStorage);
        adapterFirebaseShopStorage.notifyDataSetChanged();
    }
    private void shopStorageDialogIn() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_check_storage,null);
        builder.setView(dialogView);

        final Dialog dialog = builder.create();
        dialog.show();


        final RecyclerView productStorageList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_check_storage);
        Button btnOk = (Button)dialogView.findViewById(R.id.btn_dialog_check_storage);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);

                dialog.dismiss();
            }
        });


        productStorageList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productStorageList.setLayoutManager(linearLayoutManager);

        productStorageList.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager)productStorageList.getLayoutManager()).findFirstVisibleItemPosition();
                productStorageList.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });

        adapterFirebaseShopStorageIn = new FirebaseRecyclerAdapter<Product,ProductStorageViewHolder2>(
                Product.class,
                R.id.item_product_view_storage,
                ProductStorageViewHolder2.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode)
        ) {
            @Override
            public ProductStorageViewHolder2 onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_view_storage,parent,false);
                return new ProductStorageViewHolder2(v);
            }


            @Override
            protected void populateViewHolder(ProductStorageViewHolder2 viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productQuantity.setText(model.getUnitQuantity());

            }
        };

        productStorageList.setAdapter(adapterFirebaseShopStorageIn);
        adapterFirebaseShopStorageIn.notifyDataSetChanged();
    }

    private void cashOutConfirmDialog(final String dateKey, final String email){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cash_out,null);
        builder.setMessage("Xác nhận số tiền mặt đã nhận bàn giao từ nhân viên");
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        final EditText edtCashNumber = (EditText)dialogView.findViewById(R.id.edt_dialog_cash_out_number);
        edtCashNumber.setVisibility(View.INVISIBLE);
        final TextView tvCashNumber = (TextView)dialogView.findViewById(R.id.tv_dialog_cash_out_number);
        Button btnOk = (Button)dialogView.findViewById(R.id.btn_dialog_cash_out_ok);

        final String shopCode = b.getString("ShopCode");

        if(shopCode== null){
            Toast.makeText(getApplicationContext(),"Đang trích xuất dữ liệu...",Toast.LENGTH_LONG).show();
        }else{
            Constants.refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.hasChild("Z_POS_ShopCashOutCheck")){
                        Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").child("Request/"+shopCode+"/"+dateKey).child("CashOut").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                tvCashNumber.setText(Utils.convertNumber(dataSnapshot.getValue().toString()));
                                tvCashNumber.setVisibility(View.VISIBLE);
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

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                String shopCode = b.getString("ShopCode");
                String cashOutNumber = tvCashNumber.getText().toString().replace(",","");

                if(shopCode == null){
                    Toast.makeText(getApplicationContext(),"Lỗi dữ liệu, vui lòng thử lại",Toast.LENGTH_LONG).show();
                }
                else{
                    String timeStamp = (Calendar.getInstance().getTime().getTime())+"";
                    Cash casOutModel = new Cash(cashOutNumber,timeStamp);

                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutHistory").child(shopCode).child(thisYear).push().setValue(casOutModel);
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutHistory").child(shopCode).child(thisYear+thisMonth).push().setValue(casOutModel);
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutHistory").child(shopCode).child(thisYear+thisMonth+thisDate).push().setValue(casOutModel);

                    Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(dateKey).child("CashOut").child(email).setValue(cashOutNumber);
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").child("RequestPending").child(shopCode).setValue(null);
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").child("Request").child(shopCode).child(dateKey).child("Confirm").setValue(employeeEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();
                        }
                    });
                }

            }
        });


    }



    private class CashViewHolder extends RecyclerView.ViewHolder {
        TextView cashOut,timeStamp;

        CashViewHolder(View itemView) {
            super(itemView);
            cashOut = (TextView) itemView.findViewById(R.id.tv_item_cash_out_his_cash);
            timeStamp = (TextView) itemView.findViewById(R.id.tv_item_cash_out_his_time);
        }
    }
    private class ProductStorageViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productQuantity;

        ProductStorageViewHolder(View itemView) {
            super(itemView);
            productName = (TextView)itemView.findViewById(R.id.tv_item_product_view_storage_name);
            productQuantity = (TextView) itemView.findViewById(R.id.tv_item_product_view_storage_quantity);

        }
    }
    private class ProductStorageViewHolder2 extends RecyclerView.ViewHolder {
        TextView productName, productQuantity;
        float storageFirst=0,storageLast=0,storageIn = 0;

        ProductStorageViewHolder2(View itemView) {
            super(itemView);
            productName = (TextView)itemView.findViewById(R.id.tv_item_product_view_storage_name);
            productQuantity = (TextView) itemView.findViewById(R.id.tv_item_product_view_storage_quantity);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    final String shopCode = adapterFirebaseShopStorageIn.getRef(position).getParent().getKey();
                    Product product = adapterFirebaseShopStorageIn.getItem(position);
                    final String productName = product.getProductName();


                    AlertDialog.Builder builder = new AlertDialog.Builder(ShopManagerActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_storeman_storage_in,null);
                    builder.setView(dialogView);
                   // builder.setTitle("Nhập kho hàng hóa");
                    final Dialog d = builder.create();
                    d.show();
                    //builder.show();

                    final TextView tvStorageFirst = (TextView)dialogView.findViewById(R.id.tv_dialog_storeman_storageIn_first);
                    final TextView tvStorageLast = (TextView)dialogView.findViewById(R.id.tv_dialog_storeman_storageIn_last);
                    TextView tvStorageProductName = (TextView)dialogView.findViewById(R.id.tv_dialog_storeman_storageIn_productName);
                    final TextView tvStorageIn = (TextView)dialogView.findViewById(R.id.tv_dialog_storeman_storageIn_in);

                    final EditText edtQuantity = (EditText)dialogView.findViewById(R.id.edt__dialog_storeman_storageIn_quantitiy);
                    Button btnOk = (Button)dialogView.findViewById(R.id.btn__dialog_storeman_storageIn_ok);

                    tvStorageProductName.setText(productName);

                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode).child(productName).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            storageFirst = Float.parseFloat(dataSnapshot.getValue().toString());
                            tvStorageFirst.setText(Utils.convertNumber(storageFirst+""));

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                    edtQuantity.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            if (actionId == EditorInfo.IME_ACTION_DONE) {
                                storageIn = Float.parseFloat(edtQuantity.getText().toString());
                                tvStorageIn.setText(storageIn+"");
                                edtQuantity.setVisibility(View.INVISIBLE);
                                tvStorageIn.setVisibility(View.VISIBLE);
                                if(storageIn !=0){
                                    storageLast = storageFirst + storageIn;
                                    tvStorageLast.setText(storageLast+"");
                                }

                                return true;
                            }
                            return false;
                        }
                    });

                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(Constants.buttonClick);

                            final long timeStamp = Calendar.getInstance().getTime().getTime();

                            if(storageLast==0 || storageIn == 0){
                                Toast.makeText(getApplicationContext(),"Vui lòng nhập số lượng nhập kho.",Toast.LENGTH_LONG).show();
                            }else{

                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode).child(productName).child("unitQuantity").setValue(storageLast+"");
                                Constants.refDatabase.child(emailLogin).child("StorageHistory").child("Z_POS_ShopStorageInHistory").child(shopCode).child(productName).child(timeStamp+"").child("First").setValue(storageFirst+"");
                                Constants.refDatabase.child(emailLogin).child("StorageHistory").child("Z_POS_ShopStorageInHistory").child(shopCode).child(productName).child(timeStamp+"").child("Last").setValue(storageLast+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        storageFirst = 0;
                                        storageLast = 0;
                                        storageIn = 0;
                                    }
                                });
                                d.dismiss();



                            }
                        }
                    });

                }
            });

        }
    }
    private class BillViewHolder extends RecyclerView.ViewHolder {
        TextView billCode, billPayment;

        BillViewHolder(View itemView) {
            super(itemView);
            billCode = (TextView)itemView.findViewById(R.id.tv_item_bill_info_billCode);
            billPayment = (TextView) itemView.findViewById(R.id.tv_item_bill_info_payment);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_shop_man,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_logout_shopman){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this,LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }
}

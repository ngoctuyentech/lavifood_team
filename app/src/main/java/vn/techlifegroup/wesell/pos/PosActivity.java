package vn.techlifegroup.wesell.pos;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import vn.techlifegroup.wesell.LoginActivity;
import vn.techlifegroup.wesell.MainActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Bill;
import vn.techlifegroup.wesell.model.Cash;
import vn.techlifegroup.wesell.model.Employee;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.model.Product;
import vn.techlifegroup.wesell.model.Shop;
import vn.techlifegroup.wesell.utils.BluetoothService;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.DeviceListActivity;
import vn.techlifegroup.wesell.utils.Utils;
import vn.techlifegroup.wesell.utils.printsdk.Command;
import vn.techlifegroup.wesell.utils.printsdk.PrintPicture;
import vn.techlifegroup.wesell.utils.printsdk.PrinterCommand;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

public class PosActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Product, ProductViewHolder> adapterFirebase;
    private FirebaseRecyclerAdapter<Product, ProductBillViewHolder> adapterFirebaseBill;
    private FirebaseRecyclerAdapter<Product, ProductBillPromotionViewHolder> adapterFirebaseBillPromotion;

    private FirebaseRecyclerAdapter<Product, ProductStorageViewHolder> adapterFirebaseStorage, adapterFirebaseStorageConfirm;
    private FirebaseRecyclerAdapter<Product, ProductStorageViewHolder2> adapterFirebasePromotion;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Bill, BillViewHolder> adapterFirebaseBillCash;
    private String TAG = "PosActivity";
    private ConstraintLayout constraintView;


    //bill
    private String billPushKey,promotionRate,promotionType,promotionValue;
    private TextView paymentValue;
    private float billPayment = 0, promotionQuantityFloat = 0;
    private int productOrder = 0;
    private boolean promotionStatus = false,promotionProduct = false;

    //personal info
    private String employeeEmail,employeeName, thisYear, thisMonth, thisDate,emailLogin;

    private Bundle b = new Bundle();
    private FloatingActionButton fab;
    private HashMap<Integer, Boolean> statesMap = new HashMap<>();

    //bluetooth print
    private BluetoothAdapter mBluetoothAdapter = null;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final String DEVICE_NAME = "device_name";
    public static final int MESSAGE_TOAST = 5;
    public static final String TOAST = "toast";
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_READ = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private static final boolean DEBUG = true;
    public static final int MESSAGE_CONNECTION_LOST = 6;
    public static final int MESSAGE_UNABLE_CONNECT = 7;

    private BluetoothService mService = null;
    private String mConnectedDeviceName = null;
    private ImageView ivPromotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pos);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_pos);
        setSupportActionBar(toolbar);

        employeeEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");


        ivPromotion = (ImageView)findViewById(R.id.iv_pos_activity_promotion);
        constraintView = (ConstraintLayout) findViewById(R.id.content_pos);
        paymentValue = (TextView) findViewById(R.id.tv_pos_payment);

        ivPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                addPromotionDialog();

            }
        });

        billPushKey = Constants.refBillPOS.push().getKey();


        thisYear = (Calendar.getInstance().get(Calendar.YEAR)) + "";
        thisMonth = (Calendar.getInstance().get(Calendar.MONTH) + 1) + "";
        thisDate = (Calendar.getInstance().get(Calendar.DATE)) + "";


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                previewBillDialog();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth chưa được kích hoạt",
                    Toast.LENGTH_LONG).show();
            finish();
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        // If Bluetooth is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the session
        } else {
            if (mService == null)
                mService = new BluetoothService(this, mHandler);
        }
    }

    @Override
    public synchronized void onResume() {
        super.onResume();

        getBillData();
        getProductPosList();
        getBillTotal();

        if (mService != null) {

            if (mService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth services
                mService.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if (DEBUG)
            Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop() {
        super.onStop();
        if (DEBUG)
            Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop the Bluetooth services
        if (mService != null)
            mService.stop();
        if (DEBUG)
            Log.e(TAG, "--- ON DESTROY ---");
    }

    private void getBillTotal() {
        Constants.refDatabase.child(emailLogin).child("SaleMan/Z_POS_BillByTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(thisYear)) {
                    Constants.refDatabase.child(emailLogin).child("SaleMan/Z_POS_BillByTime").child(thisYear).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String billTotalYear = dataSnapshot.getValue().toString();
                            b.putString("BillTotalYear", billTotalYear);
                            Constants.refDatabase.child(emailLogin).child("SaleMan/Z_POS_BillTotalHistory").child(thisYear + thisMonth + thisDate).child("Year").child(billPushKey).setValue(billTotalYear);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    b.putString("BillTotalYear", "0");
                    Constants.refDatabase.child(emailLogin).child("SaleMan/Z_POS_BillTotalHistory").child(thisYear + thisMonth + thisDate).child("Year").child(billPushKey).setValue("0");

                }

                if (dataSnapshot.hasChild(thisYear + thisMonth)) {
                    Constants.refDatabase.child(emailLogin).child("SaleMan/Z_POS_BillByTime").child(thisYear + thisMonth).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String billTotalMonth = dataSnapshot.getValue().toString();
                            b.putString("BillTotalMonth", billTotalMonth);
                            Constants.refDatabase.child(emailLogin).child("SaleMan/Z_POS_BillTotalHistory").child(thisYear + thisMonth + thisDate).child("Month").child(billPushKey).setValue(billTotalMonth);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    b.putString("BillTotalMonth", "0");
                    Constants.refDatabase.child(emailLogin).child("SaleMan/Z_POS_BillTotalHistory").child(thisYear + thisMonth + thisDate).child("Month").child(billPushKey).setValue("0");

                }

                if (dataSnapshot.hasChild(thisYear + thisMonth + thisDate)) {
                    Constants.refDatabase.child(emailLogin).child("SaleMan/Z_POS_BillByTime").child(thisYear + thisMonth + thisDate).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String billTotalDate = dataSnapshot.getValue().toString();
                            b.putString("BillTotalDate", billTotalDate);
                            Constants.refBillTotalHistory.child(thisYear + thisMonth + thisDate).child("Date").child(billPushKey).setValue(billTotalDate);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                } else {
                    b.putString("BillTotalDate", "0");
                    Constants.refDatabase.child(emailLogin).child("SaleMan/Z_POS_BillTotalHistory").child(thisYear + thisMonth + thisDate).child("Date").child(billPushKey).setValue("0");

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getBillData() {
        Constants.refDatabase.child(emailLogin).child("Z_POS_Employee").child(employeeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Employee employee = dataSnapshot.getValue(Employee.class);
                employeeName = employee.getEmployeeName();
                String employeeCode = employee.getEmployeeCode();
                final String shopCode = employee.getShopCode();
                b.putString("EmployeeName", employeeName);
                b.putString("ShopCode", shopCode);
                b.putString("EmployeeCode", employeeCode);

                Constants.refDatabase.child(emailLogin).child("Z_POS_Shop").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Shop shop = dataSnapshot.getValue(Shop.class);
                        String shopName = shop.getShopName();
                        String shopNameNotViet = shop.getShopNameNotViet();
                        String shopAddress = shop.getShopAddress();
                        b.putString("ShopName", shopName);
                        b.putString("ShopNameNotViet", shopNameNotViet);
                        b.putString("ShopAddress", shopAddress);

                        Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("RequestPending")) {
                                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(shopCode)){
                                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").child(shopCode).child("Email").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        String email = dataSnapshot.getValue().toString();
                                                        if (!employeeEmail.equals(email)) {
                                                            Snackbar.make(constraintView, "Vui lòng xác nhận tồn kho của cửa hàng", Snackbar.LENGTH_INDEFINITE)
                                                                    .setAction("Xem", new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").child(shopCode).child("DateKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    String dateKey = dataSnapshot.getValue().toString();
                                                                                    storageCheckConfirmDialog(dateKey);

                                                                                    // Toast.makeText(getApplicationContext(),dateKey,Toast.LENGTH_LONG).show();
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
                                } else {
                                    Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild("RequestPending")) {
                                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.hasChild(shopCode)){
                                                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("Email").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    String email = dataSnapshot.getValue().toString();
                                                                    if (!employeeEmail.equals(email)) {
                                                                        Snackbar.make(constraintView, "Vui lòng xác nhận tồn quỹ của cửa hàng", Snackbar.LENGTH_INDEFINITE)
                                                                                .setAction("Xem", new View.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(View v) {
                                                                                        Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("DateKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                            @Override
                                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                                String dateKey = dataSnapshot.getValue().toString();
                                                                                                cashCheckConfirmDialog(dateKey);

                                                                                                // Toast.makeText(getApplicationContext(),dateKey,Toast.LENGTH_LONG).show();
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
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                        Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("RequestPending")) {
                                    Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(shopCode)){
                                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("Email").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        String email = dataSnapshot.getValue().toString();
                                                        if (!employeeEmail.equals(email)) {
                                                            Snackbar.make(constraintView, "Vui lòng xác nhận tồn quỹ của cửa hàng", Snackbar.LENGTH_INDEFINITE)
                                                                    .setAction("Xem", new View.OnClickListener() {
                                                                        @Override
                                                                        public void onClick(View v) {
                                                                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("DateKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    String dateKey = dataSnapshot.getValue().toString();
                                                                                    cashCheckConfirmDialog(dateKey);

                                                                                    // Toast.makeText(getApplicationContext(),dateKey,Toast.LENGTH_LONG).show();
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

                                } else {
                                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild("RequestPending")) {
                                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.hasChild(shopCode)){
                                                            Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").child(shopCode).child("Email").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    String email = dataSnapshot.getValue().toString();
                                                                    if (!employeeEmail.equals(email)) {
                                                                        Snackbar.make(constraintView, "Vui lòng xác nhận tồn kho của cửa hàng", Snackbar.LENGTH_INDEFINITE)
                                                                                .setAction("Xem", new View.OnClickListener() {
                                                                                    @Override
                                                                                    public void onClick(View v) {
                                                                                        Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").child(shopCode).child("DateKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                            @Override
                                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                                String dateKey = dataSnapshot.getValue().toString();
                                                                                                storageCheckConfirmDialog(dateKey);

                                                                                                // Toast.makeText(getApplicationContext(),dateKey,Toast.LENGTH_LONG).show();
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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void addPromotionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_pos_add_promotion, null);
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        final Spinner spinPromotionRate = (Spinner)dialogView.findViewById(R.id.spin_pos_promotion_rate);
        final Spinner spinPromotionType = (Spinner)dialogView.findViewById(R.id.spin_pos_promotion_type);
        final TextView tvPromotionValue = (TextView)dialogView.findViewById(R.id.tv_pos_promotion_value);
        final RecyclerView productList = (RecyclerView)dialogView.findViewById(R.id.recycler_pos_promotion);
        Button btnOk = (Button)dialogView.findViewById(R.id.btn_pos_add_promotion);

        spinPromotionType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                promotionType = (String) parent.getItemAtPosition(position);
                b.putString("PromotionType",promotionType);

                if(promotionType.equals("Hàng")){
                    productList.setVisibility(View.VISIBLE);
                    promotionValue = "0";
                    tvPromotionValue.setText("0");
                    spinPromotionRate.setEnabled(false);
                }else{
                    Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("Promotion")){
                                Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("Promotion").setValue(null);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    productList.setVisibility(View.INVISIBLE);
                    spinPromotionRate.setEnabled(true);
                    spinPromotionRate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            promotionRate = (String) parent.getItemAtPosition(position);
                            float promotionValueFloat = billPayment*Float.parseFloat(promotionRate)/100;
                            promotionValue = promotionValueFloat+"";
                            tvPromotionValue.setText(Utils.convertNumber(promotionValue+""));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        productList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productList.setLayoutManager(linearLayoutManager);


        adapterFirebasePromotion = new FirebaseRecyclerAdapter<Product, ProductStorageViewHolder2>(
                Product.class,
                R.id.item_product_view_storage,
                ProductStorageViewHolder2.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_Product")
        ) {
            @Override
            public ProductStorageViewHolder2 onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_view_storage, parent, false);
                return new ProductStorageViewHolder2(v);
            }


            @Override
            protected void populateViewHolder(ProductStorageViewHolder2 viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());

            }
        };

        productList.setAdapter(adapterFirebasePromotion);
        adapterFirebasePromotion.notifyDataSetChanged();

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                if(promotionType.equals("Hàng") && !promotionProduct){
                    Toast.makeText(getApplicationContext(),"Vui lòng chọn SP khuyến mãi",Toast.LENGTH_LONG).show();
                }else{
                    dialog.dismiss();
                    promotionStatus = true;
                }

            }
        });

    }

    private void previewBillDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog_bill_preview, null);
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        TextView tvEmployeeName = (TextView) dialogView.findViewById(R.id.tv_bill_preview_employeename);
        TextView tvShopName = (TextView) dialogView.findViewById(R.id.tv_bill_preview_shopname);
        TextView tvShopAddress = (TextView) dialogView.findViewById(R.id.tv_bill_preview_shopaddress);
        final TextView tvBillCode = (TextView) dialogView.findViewById(R.id.tv_bill_preview_billcode);
        TextView tvDateTime = (TextView) dialogView.findViewById(R.id.tv_bill_preview_date);
        TextView tvTotalPayment = (TextView) dialogView.findViewById(R.id.tv_bill_preview_totalpayment);
        TextView tvPromotionCash = (TextView) dialogView.findViewById(R.id.tv_bill_preview_promotion_cash);
        TextView tvKMBH = (TextView) dialogView.findViewById(R.id.tv_bill_preview_kmbh);
        RecyclerView promotionBill = (RecyclerView) dialogView.findViewById(R.id.recycler_bill_preview_promtion);

        final TextView tvCashBack = (TextView) dialogView.findViewById(R.id.tv_bill_preview_cashback);

        final TextView tvCustomerCash = (TextView) dialogView.findViewById(R.id.tv_bill_preview_cashcustomer);
        final EditText edtCustomerCash = (EditText) dialogView.findViewById(R.id.edt_bill_preview_customercash);
        //final String customerCash = edtCustomerCash.getText().toString();
        ImageView ivPrint = (ImageView) dialogView.findViewById(R.id.iv_bill_preview_print);

        tvTotalPayment.setText(Utils.convertNumber(billPayment + ""));

        if(promotionProduct){
            promotionBill.setVisibility(View.VISIBLE);
            tvKMBH.setVisibility(View.VISIBLE);

        }else if(promotionStatus){
            tvPromotionCash.setText(Utils.convertNumber(promotionValue));
        }else{
            tvPromotionCash.setText("Không");
        }


        edtCustomerCash.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {

                    tvCustomerCash.setText(Utils.convertNumber(edtCustomerCash.getText().toString()));

                    if(promotionValue!=null){
                        float cashBackFloat = Float.parseFloat(edtCustomerCash.getText().toString()) - billPayment + Float.parseFloat(promotionValue);
                        b.putString("CashBack", cashBackFloat + "");
                        tvCashBack.setText(Utils.convertNumber(cashBackFloat + ""));
                        edtCustomerCash.setVisibility(View.INVISIBLE);
                        tvCustomerCash.setVisibility(View.VISIBLE);

                    }else{
                        float cashBackFloat = Float.parseFloat(edtCustomerCash.getText().toString()) - billPayment;
                        //b.putString("CustomerCash",edtCustomerCash.getText().toString());
                        b.putString("CashBack", cashBackFloat + "");
                        tvCashBack.setText(Utils.convertNumber(cashBackFloat + ""));
                        edtCustomerCash.setVisibility(View.INVISIBLE);
                        tvCustomerCash.setVisibility(View.VISIBLE);
                    }

                    return true;
                }
                return false;
            }
        });

        final String employeeName = b.getString("EmployeeName");
        //final String employeeCode = b.getString("EmployeeCode");
        final String shopName = b.getString("ShopName");
       // final String shopNameNotViet = b.getString("ShopNameNotViet");
        final String shopAddress = b.getString("ShopAddress");
        final long timeStamp = Calendar.getInstance().getTime().getTime();
        final String shopCode = b.getString("ShopCode");
        final String billTotalYear = b.getString("BillTotalYear");
        final String billTotalMonth = b.getString("BillTotalMonth");
        final String billTotalDate = b.getString("BillTotalDate");

        if (employeeName == null || shopName == null || shopAddress == null || shopCode == null) {
            Toast.makeText(getApplicationContext(), "Đang xử lý...Vui lòng thực hiện lại thao tác.", Toast.LENGTH_LONG).show();
        } else {
            tvEmployeeName.setText(employeeName);
            tvShopName.setText(shopName);
            tvShopAddress.setText(shopAddress);
            tvDateTime.setText(Utils.getDateCurrentTimeZone(timeStamp));
            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_BillByTime").child(thisYear + thisMonth + thisDate).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String billCountCode = (dataSnapshot.getChildrenCount() + 1) + "";
                    String billCode = shopCode + thisYear + thisMonth + thisDate + billCountCode;
                    tvBillCode.setText(billCode);
                    b.putString("BillCode", billCode);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }


        //Get productBill info
        Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();
                int i = 0;
                for (DataSnapshot p : productSnap) {
                    Product product = p.getValue(Product.class);
                    String productOrder = product.getProductOrder();
                    String productName = product.getProductName();
                    String productNameNoViet = product.getNameNotViet();
                    String productQuantity = product.getUnitQuantity();
                    String productPrice = product.getUnitPrice();
                    String productTotal = product.getProductTotal();
                    b.putString("ProductOrder" + i, productOrder);
                    b.putString("ProductName" + i, productName);
                    b.putString("ProductNameNotViet" + i, productNameNoViet);
                    b.putString("ProductQuantity" + i, productQuantity);
                    b.putString("ProductPrice" + i, productPrice);
                    b.putString("ProductTotal" + i, productTotal);
                    i++;
                    b.putInt("ForIntValue", i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("Promotion")){
                    Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();
                            int i = 0;
                            for (DataSnapshot p : productSnap) {
                                Product promotion = p.getValue(Product.class);
                                String promotionName = promotion.getProductName();
                                String promotionNameNoViet = promotion.getNameNotViet();
                                String promotionQuantity = promotion.getUnitQuantity();
                                b.putString("PromotionName" + i, promotionName);
                                b.putString("PromotionNameNotViet" + i, promotionNameNoViet);
                                b.putString("PromotionQuantity" + i, promotionQuantity);
                                i++;
                                b.putInt("PromotionForIntValue", i);
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


        ivPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String customerCash = edtCustomerCash.getText().toString();
                final String cashBack = b.getString("CashBack");

                b.putString("CustomerCash",customerCash);
                v.startAnimation(Constants.buttonClick);
                if (TextUtils.isEmpty(customerCash)) {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập tiền khách đưa", Toast.LENGTH_LONG).show();
                } else if (cashBack == null) {
                    Toast.makeText(getApplicationContext(), "Đang xử lý...Vui lòng thực hiện lại thao tác.", Toast.LENGTH_LONG).show();

                }else if (mService.getState() != BluetoothService.STATE_CONNECTED) {
                    Intent serverIntent = new Intent(PosActivity.this, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(PosActivity.this);
                    builder.setMessage("Hóa đơn đã hoàn thành và sẵn sàng được in?");

                    final String billCode = b.getString("BillCode");
                    final int forIntValue = b.getInt("ForIntValue");
                    final int promotionForIntValue = b.getInt("PromotionForIntValue");
                    //final String customerCash = edtCustomerCash.getText().toString();

                    //final float updateBillTotalYear = Float.parseFloat(billTotalYear) + billPayment;
                   // final float updateBillTotalMonth = Float.parseFloat(billTotalMonth) + billPayment;
                   // final float updateBillTotalDate = Float.parseFloat(billTotalDate) + billPayment;

                    builder.setPositiveButton("In", new DialogInterface.OnClickListener() {
                        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //TODO: new COde for BillTotal - need check
                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_BillTotalByTime").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> timeSnap = dataSnapshot.getChildren();

                                    for (DataSnapshot itemTime : timeSnap) {
                                        String timeKey = itemTime.getKey();
                                        String timeValue = itemTime.getValue().toString();
                                        DatabaseReference itemTimeRef = itemTime.getRef();

                                        if (timeKey.equals(thisYear)) {
                                            float updateTotalYear = Float.parseFloat(timeValue) + billPayment;
                                            itemTimeRef.setValue(updateTotalYear + "");
                                        }

                                        if (timeKey.equals(thisYear + thisMonth) ) {
                                            float updateTotalYear = Float.parseFloat(timeValue) + billPayment;
                                            itemTimeRef.setValue(updateTotalYear + "");
                                        }

                                        if (timeKey.equals(thisYear + thisMonth + thisDate) ) {
                                            float updateTotalYear = Float.parseFloat(timeValue) + billPayment;
                                            itemTimeRef.setValue(updateTotalYear + "");
                                        }

                                    }

                                    DatabaseReference snapShotRef = dataSnapshot.getRef();

                                    if(!dataSnapshot.hasChild(thisYear)){
                                        snapShotRef.child(thisYear).setValue(billPayment+"");
                                    }
                                    if(!dataSnapshot.hasChild(thisYear+thisMonth)){
                                        snapShotRef.child(thisYear+thisMonth).setValue(billPayment+"");
                                    }
                                    if(!dataSnapshot.hasChild(thisYear+thisMonth+thisDate)){
                                        snapShotRef.child(thisYear+thisMonth+thisDate).setValue(billPayment+"");

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreSale").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> timeSnap = dataSnapshot.getChildren();

                                    for (DataSnapshot itemTime : timeSnap) {
                                        String timeKey = itemTime.getKey();
                                        String timeValue = itemTime.getValue().toString();
                                        DatabaseReference itemTimeRef = itemTime.getRef();

                                        if (timeKey.equals(thisYear)) {
                                            float updateTotalYear = Float.parseFloat(timeValue) + billPayment;
                                            itemTimeRef.setValue(updateTotalYear + "");
                                        }

                                        if (timeKey.equals(thisYear + thisMonth) ) {
                                            float updateTotalYear = Float.parseFloat(timeValue) + billPayment;
                                            itemTimeRef.setValue(updateTotalYear + "");
                                        }

                                        if (timeKey.equals(thisYear + thisMonth + thisDate) ) {
                                            float updateTotalYear = Float.parseFloat(timeValue) + billPayment;
                                            itemTimeRef.setValue(updateTotalYear + "");
                                        }

                                    }

                                    DatabaseReference snapShotRef = dataSnapshot.getRef();

                                    if(!dataSnapshot.hasChild(thisYear)){
                                        snapShotRef.child(thisYear).setValue(billPayment+"");
                                    }
                                    if(!dataSnapshot.hasChild(thisYear+thisMonth)){
                                        snapShotRef.child(thisYear+thisMonth).setValue(billPayment+"");
                                    }
                                    if(!dataSnapshot.hasChild(thisYear+thisMonth+thisDate)){
                                        snapShotRef.child(thisYear+thisMonth+thisDate).setValue(billPayment+"");

                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            final Bill bill = new Bill(customerCash, cashBack, billCode, billPayment + "", timeStamp + "");
                            final OrderDetail orderDetail = new OrderDetail(employeeEmail.replace(".", ","), shopCode, billPayment + "", billCode, timeStamp + "");

                            Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("OtherInformation").setValue(orderDetail);

                            Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeBill").child(employeeEmail).child(thisYear + thisMonth + thisDate).child(billPushKey).setValue(bill);
                            //Employee cash
                            Constants.refDatabase.child(emailLogin).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("Z_POS_EmployeeCash")) {
                                        Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeCash").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild(employeeEmail)) {
                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeCash").child(employeeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.hasChild(thisYear + thisMonth + thisDate)) {
                                                                Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeCash").child(employeeEmail).child(thisYear + thisMonth + thisDate).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        String totalCash = dataSnapshot.getValue().toString();
                                                                        float updateCash = Float.parseFloat(totalCash) + billPayment;
                                                                        //Toast.makeText(getApplicationContext(),"BillPayment: "+billPayment+"",Toast.LENGTH_LONG).show();
                                                                        Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeCash").child(employeeEmail).child(thisYear + thisMonth + thisDate).setValue(updateCash + "");
                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });

                                                            } else {
                                                                Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeCash").child(employeeEmail).child(thisYear + thisMonth + thisDate).setValue(billPayment + "");
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });

                                                } else {
                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeCash").child(employeeEmail).child(thisYear + thisMonth + thisDate).setValue(billPayment + "");
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeCash").child(employeeEmail).child(thisYear + thisMonth + thisDate).setValue(billPayment + "");
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                            if(!shopCode.equals("CH6")){
                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_BillByTime").child(thisYear).child(billPushKey).setValue(orderDetail);
                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_BillByTime").child(thisYear + thisMonth).child(billPushKey).setValue(orderDetail);
                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_BillByTime").child(thisYear + thisMonth + thisDate).child(billPushKey).setValue(orderDetail);

                            }
                            Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("BillInfo").setValue(bill);

                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreOrder").child(shopCode).child(thisYear).child(billPushKey).setValue(orderDetail);
                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreOrder").child(shopCode).child(thisYear + thisMonth).child(billPushKey).setValue(orderDetail);
                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_StoreOrder").child(shopCode).child(thisYear + thisMonth + thisDate).child(billPushKey).setValue(orderDetail);

                            if(promotionProduct){
                                //Remove Storage
                                Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();
                                        //float productTotalFloat = 0;
                                        for (DataSnapshot p : productSnap) {
                                            Product product = p.getValue(Product.class);
                                            final String productName = product.getProductName();
                                            final String productTotal = product.getProductTotal();
                                            final String productQuantity = product.getUnitQuantity();
                                            //productTotalFloat = productTotalFloat + Float.parseFloat(productTotal);
                                            Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode).child(productName).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    String currentStorage = dataSnapshot.getValue().toString();
                                                    float updateStorage = Float.parseFloat(currentStorage)-Float.parseFloat(productQuantity);

                                                    Constants.refDatabase.child(emailLogin).child("StorageHistory").child("Z_POS_ShopStorageInHistory").child(shopCode).child(productName).child(billPushKey).setValue(currentStorage);
                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode).child(productName).child("unitQuantity").setValue(updateStorage + "").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(!productSnap.iterator().hasNext())
                                                                Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        Iterable<DataSnapshot> promotionSnap = dataSnapshot.getChildren();

                                                                        for (DataSnapshot p : promotionSnap) {
                                                                            Product product = p.getValue(Product.class);
                                                                            final String promotionName = product.getProductName();
                                                                            final String promotionQuantity = product.getUnitQuantity();
                                                                            Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode).child(promotionName).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    String currentStorage = dataSnapshot.getValue().toString();
                                                                                    float updateStorage = Float.parseFloat(currentStorage) - Float.parseFloat(promotionQuantity);
                                                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode).child(promotionName).child("unitQuantity").setValue(updateStorage + "");
                                                                                }

                                                                                @Override
                                                                                public void onCancelled(DatabaseError databaseError) {

                                                                                }
                                                                            });

                                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_PromotionQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    if (dataSnapshot.hasChild(promotionName)) {
                                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_PromotionQuantity").child(promotionName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                            @Override
                                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                                if (dataSnapshot.hasChild(thisYear)) {
                                                                                                    Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_PromotionQuantity").child(promotionName).child(thisYear).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                        @Override
                                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                                            String promotionYear = dataSnapshot.getValue().toString();
                                                                                                            float updatePromotionYear = Float.parseFloat(promotionYear) + Float.parseFloat(promotionQuantity);
                                                                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_PromotionQuantity").child(promotionName).child(thisYear).setValue(updatePromotionYear + "");
                                                                                                        }

                                                                                                        @Override
                                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                                        }
                                                                                                    });
                                                                                                } else {
                                                                                                    Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_PromotionQuantity").child(promotionName).child(thisYear).setValue(promotionQuantity);
                                                                                                }

                                                                                                if (dataSnapshot.hasChild(thisYear + thisMonth)) {
                                                                                                    Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_PromotionQuantity").child(promotionName).child(thisYear + thisMonth).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                                        @Override
                                                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                                            String productMonth = dataSnapshot.getValue().toString();
                                                                                                            float updatePromotionMonth = Float.parseFloat(productMonth) + Float.parseFloat(promotionQuantity);
                                                                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_PromotionQuantity").child(promotionQuantity).child(thisYear + thisMonth).setValue(updatePromotionMonth + "");
                                                                                                        }

                                                                                                        @Override
                                                                                                        public void onCancelled(DatabaseError databaseError) {

                                                                                                        }
                                                                                                    });

                                                                                                } else {
                                                                                                    Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_PromotionQuantity").child(promotionName).child(thisYear + thisMonth).setValue(promotionQuantity);


                                                                                                }


                                                                                            }

                                                                                            @Override
                                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                                            }
                                                                                        });
                                                                                    } else {


                                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_PromotionQuantity").child(promotionName).child(thisYear).setValue(promotionQuantity);
                                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_PromotionQuantity").child(promotionName).child(thisYear + thisMonth).setValue(promotionQuantity);
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

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            if (!shopCode.equals("CH6"))
                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild(productName)) {
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.hasChild(thisYear)) {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                String productYearSale = dataSnapshot.getValue().toString();
                                                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear).child(billPushKey).setValue(productYearSale);
                                                                                float updateProductYearSale = Float.parseFloat(productYearSale) + Float.parseFloat(productTotal);
                                                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear).setValue(updateProductYearSale + "");
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                                    } else {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear).setValue(productTotal);
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear).child(billPushKey).setValue(productTotal);
                                                                    }

                                                                    if (dataSnapshot.hasChild(thisYear + thisMonth)) {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                String productMonthSale = dataSnapshot.getValue().toString();
                                                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear + thisMonth).child(billPushKey).setValue(productMonthSale);
                                                                                float updateProductYearSale = Float.parseFloat(productMonthSale) + Float.parseFloat(productTotal);
                                                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth).setValue(updateProductYearSale + "");
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    } else {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth).setValue(productTotal);
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear + thisMonth).child(billPushKey).setValue(productTotal);


                                                                    }

                                                                    if (dataSnapshot.hasChild(thisYear + thisMonth + thisDate)) {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth + thisDate).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                String productDateSale = dataSnapshot.getValue().toString();
                                                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear + thisMonth + thisDate).child(billPushKey).setValue(productDateSale);
                                                                                float updateProductYearSale = Float.parseFloat(productDateSale) + Float.parseFloat(productTotal);
                                                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth + thisDate).setValue(updateProductYearSale + "");
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    } else {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth + thisDate).setValue(productTotal);
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear + thisMonth + thisDate).child(billPushKey).setValue(productTotal);
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });
                                                        } else {
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear).setValue(productTotal);
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth).setValue(productTotal);
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth + thisDate).setValue(productTotal);

                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear).child(billPushKey).setValue(productTotal);
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear + thisMonth).child(billPushKey).setValue(productTotal);
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear + thisMonth + thisDate).child(billPushKey).setValue(productTotal);
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
                            }else{
                                Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();

                                        float productTotalFloat = 0;

                                        for (DataSnapshot p : productSnap) {
                                            Product product = p.getValue(Product.class);
                                            final String productName = product.getProductName();
                                            final String productTotal = product.getProductTotal();
                                            final String productQuantity = product.getUnitQuantity();
                                            productTotalFloat = productTotalFloat + Float.parseFloat(productTotal);

                                            Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode).child(productName).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    String currentStorage = dataSnapshot.getValue().toString();
                                                    Constants.refDatabase.child(emailLogin).child("StorageHistory").child("Z_POS_ShopStorageHistory").child(shopCode).child(productName).child(billPushKey).setValue(currentStorage);
                                                    float updateStorage = Float.parseFloat(currentStorage) - Float.parseFloat(productQuantity);
                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode).child(productName).child("unitQuantity").setValue(updateStorage + "");
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });

                                            if(!shopCode.equals("CH6")) {
                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild(productName)) {
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    if (dataSnapshot.hasChild(thisYear)) {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                String productYearSale = dataSnapshot.getValue().toString();
                                                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear).child(billPushKey).setValue(productYearSale);
                                                                                float updateProductYearSale = Float.parseFloat(productYearSale) + Float.parseFloat(productTotal);
                                                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear).setValue(updateProductYearSale + "");
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });
                                                                    } else {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear).setValue(productTotal);
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear).child(billPushKey).setValue(productTotal);
                                                                    }

                                                                    if (dataSnapshot.hasChild(thisYear + thisMonth)) {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                String productMonthSale = dataSnapshot.getValue().toString();
                                                                                Constants.refPOSProductSaleHistory.child(productName).child(thisYear + thisMonth).child(billPushKey).setValue(productMonthSale);
                                                                                float updateProductYearSale = Float.parseFloat(productMonthSale) + Float.parseFloat(productTotal);
                                                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth).setValue(updateProductYearSale + "");
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    } else {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth).setValue(productTotal);
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear + thisMonth).child(billPushKey).setValue(productTotal);


                                                                    }

                                                                    if (dataSnapshot.hasChild(thisYear + thisMonth + thisDate)) {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth + thisDate).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                String productDateSale = dataSnapshot.getValue().toString();
                                                                                Constants.refPOSProductSaleHistory.child(productName).child(thisYear + thisMonth + thisDate).child(billPushKey).setValue(productDateSale);
                                                                                float updateProductYearSale = Float.parseFloat(productDateSale) + Float.parseFloat(productTotal);
                                                                                Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth + thisDate).setValue(updateProductYearSale + "");
                                                                            }

                                                                            @Override
                                                                            public void onCancelled(DatabaseError databaseError) {

                                                                            }
                                                                        });

                                                                    } else {
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth + thisDate).setValue(productTotal);
                                                                        Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear + thisMonth + thisDate).child(billPushKey).setValue(productTotal);
                                                                    }

                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });
                                                        } else {
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(thisYear).setValue(productTotal);
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth).setValue(productTotal);
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSale").child(productName).child(thisYear + thisMonth + thisDate).setValue(productTotal);

                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear).child(billPushKey).setValue(productTotal);
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear + thisMonth).child(billPushKey).setValue(productTotal);
                                                            Constants.refDatabase.child(emailLogin).child("SaleMan").child("Z_POS_ProductSaleHistory").child(productName).child(thisYear + thisMonth + thisDate).child(billPushKey).setValue(productTotal);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {
                                                    }
                                                });
                                            }

                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            //Print_Test();
                            //String msg = shopName+"\n\n";

                            printBill();
                            printBill();

                            /*
                            File fontFile = new File(Environment.getExternalStorageDirectory(),"resources/fonts/vuArial.ttf");
                            File fontFileBold = new File(Environment.getExternalStorageDirectory(),"resources/fonts/vuArialBold.ttf");

                            //File pdfDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "NestArt");
                            File pdfDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"NestArt");

                            //File pdfDir = new File(PosActivity.this.getExternalCacheDir(),"NestArt");
                            if (!pdfDir.exists()){
                                //noinspection ResultOfMethodCallIgnored
                                pdfDir.mkdir();
                            }
                            // fix
                            //noinspection ResultOfMethodCallIgnored
                            pdfDir.setExecutable(true);
                            //noinspection ResultOfMethodCallIgnored
                            pdfDir.setReadable(true);
                            //noinspection ResultOfMethodCallIgnored
                            pdfDir.setWritable(true);

                            String timeStampString = (Calendar.getInstance().getTime().getTime())+"";

                            MediaScannerConnection.scanFile(PosActivity.this, new String[] {pdfDir.toString()}, null, null);

                            //Now create the name of your PDF file that you will generate
                            pdfFile = new File(pdfDir, "billTest.pdf");

                            try {
                                final Document document = new Document();
                                baos = new ByteArrayOutputStream();

                                //PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                                PdfWriter.getInstance(document, baos);
                                document.open();

                                BaseFont bfBold = BaseFont.createFont(fontFileBold.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                                BaseFont bfNormal = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                                Font fontTitle = new Font(bfBold,25);
                                Font fontSubTitle = new Font(bfBold,15);
                                Font fontSmall = new Font(bfNormal,10);
                                final Font fontNormal = new Font(bfNormal,12);

                                Paragraph pTitle = new Paragraph(shopName, fontTitle);
                                pTitle.setAlignment(Element.ALIGN_CENTER);
                                document.add(pTitle);


                                Paragraph pAddress = new Paragraph(shopAddress, fontSmall);
                                pAddress.setAlignment(Element.ALIGN_CENTER);
                                document.add(pAddress);

                                Paragraph pPaymentBill = new Paragraph("PHIẾU THANH TOÁN", fontSubTitle);
                                pPaymentBill.setAlignment(Element.ALIGN_CENTER);
                                document.add(pPaymentBill);

                                Paragraph pBillInfo = new Paragraph("Số HĐ: " + billCode+ "    "+ Utils.getDateCurrentTimeZone(timeStamp), fontNormal);
                                pBillInfo.setAlignment(Element.ALIGN_CENTER);
                                document.add(pBillInfo);

                                Paragraph pEmployee = new Paragraph("Tên NV: " + employeeName, fontNormal);
                                pEmployee.setAlignment(Element.ALIGN_CENTER);
                                pEmployee.setSpacingAfter(20f);
                                document.add(pEmployee);

                                float[] columnWidths = {1,8,2,3,3};
                                final PdfPTable table = new PdfPTable(columnWidths);

                                for (int i = 0; i < 1; i++) {
                                    PdfPCell cell;
                                    cell = new PdfPCell(new Phrase("TT",fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthRight(0);
                                    table.addCell(cell);

                                    cell = new PdfPCell(new Phrase("Sản phẩm",fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthRight(0);
                                    table.addCell(cell);

                                    cell = new PdfPCell(new Phrase("SL",fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthRight(0);
                                    table.addCell(cell);

                                    cell = new PdfPCell(new Phrase("Đ.Giá",fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                    cell.setBorderWidthRight(0);
                                    cell.setBorderWidthLeft(0);
                                    table.addCell(cell);

                                    cell = new PdfPCell(new Phrase("T.Tiền",fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthRight(0);
                                    table.addCell(cell);
                                }
                                document.add(table);

                                PdfPTable [] tables = new  PdfPTable[forIntValue];

                                for(int i =0; i<forIntValue;i++){
                                    String productName = b.getString("ProductName"+i);
                                    String productOrder = b.getString("ProductOrder"+i);
                                    String productQuantity = b.getString("ProductQuantity"+i);
                                    String productPrice = b.getString("ProductPrice"+i);
                                    String productTotal = b.getString("ProductTotal"+i);

                                    if(productName == null ||productOrder == null ||productQuantity == null ||productPrice == null ||productTotal == null){
                                        Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                                    }else{

                                        tables[i] = new PdfPTable(columnWidths);

                                        for (int x = 0; x < 1; x++) {
                                            PdfPCell cell;
                                            cell = new PdfPCell(new Phrase(productOrder,fontNormal));
                                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                            cell.setBorderWidthLeft(0);
                                            cell.setBorderWidthRight(0);
                                            tables[i].addCell(cell);

                                            cell = new PdfPCell(new Phrase(productName,fontNormal));
                                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                            cell.setBorderWidthLeft(0);
                                            cell.setBorderWidthRight(0);
                                            tables[i].addCell(cell);

                                            cell = new PdfPCell(new Phrase(productQuantity,fontNormal));
                                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                            cell.setBorderWidthLeft(0);
                                            cell.setBorderWidthRight(0);
                                            tables[i].addCell(cell);

                                            cell = new PdfPCell(new Phrase(Utils.convertNumber(productPrice),fontNormal));
                                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                            cell.setBorderWidthRight(0);
                                            cell.setBorderWidthLeft(0);
                                            tables[i].addCell(cell);

                                            cell = new PdfPCell(new Phrase(Utils.convertNumber(productTotal),fontNormal));
                                            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                            cell.setBorderWidthLeft(0);
                                            cell.setBorderWidthRight(0);
                                            tables[i].addCell(cell);
                                        }
                                        document.add(tables[i]);

                                    }

                                }

                                float[] columnWidth2s = {5,5};
                                final PdfPTable table2 = new PdfPTable(columnWidth2s);

                                for (int i = 0; i < 1; i++) {
                                    PdfPCell cell;
                                    cell = new PdfPCell(new Phrase("Thành tiền", fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthRight(0);
                                    table2.addCell(cell);

                                    cell = new PdfPCell(new Phrase(Utils.convertNumber(billPayment+""), fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthRight(0);
                                    table2.addCell(cell);
                                }
                                table2.setSpacingAfter(20f);
                                document.add(table2);

                                final PdfPTable table3 = new PdfPTable(columnWidth2s);

                                for (int i = 0; i < 1; i++) {
                                    PdfPCell cell;
                                    cell = new PdfPCell(new Phrase("Tiền khách đưa", fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthBottom(0);
                                    cell.setBorderWidthTop(0);
                                    cell.setBorderWidthRight(0);
                                    table3.addCell(cell);

                                    cell = new PdfPCell(new Phrase(Utils.convertNumber(customerCash), fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthBottom(0);
                                    cell.setBorderWidthTop(0);
                                    cell.setBorderWidthRight(0);
                                    table3.addCell(cell);
                                }

                                document.add(table3);

                                final PdfPTable table4 = new PdfPTable(columnWidth2s);

                                for (int i = 0; i < 1; i++) {
                                    PdfPCell cell;
                                    cell = new PdfPCell(new Phrase("Tiền thừa", fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthBottom(0);
                                    cell.setBorderWidthTop(0);
                                    cell.setBorderWidthRight(0);
                                    table4.addCell(cell);

                                    cell = new PdfPCell(new Phrase(Utils.convertNumber(cashBack), fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthBottom(0);
                                    cell.setBorderWidthTop(0);
                                    cell.setBorderWidthRight(0);
                                    table4.addCell(cell);
                                }

                                document.add(table4);

                                document.close();


                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }
                            PrintManager printManager = (PrintManager) PosActivity.this.getSystemService(Context.PRINT_SERVICE);
                            String jobName = PosActivity.this.getString(R.string.app_name) + " Document";

                            PrintDocumentAdapter pda = new PrintDocumentAdapter() {
                                private int pageCount;
                                @Override
                                public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                                    // Prepare the layout.
                                    int newPageCount;
                                    // Mils is 1/1000th of an inch. Obviously.
                                    if(newAttributes.getMediaSize().getHeightMils() < 1000) {
                                        newPageCount = 2;
                                    } else {
                                        newPageCount = 1;
                                    }
                                    // Has the layout actually changed?
                                    boolean layoutChanged = newPageCount != pageCount;
                                    pageCount = newPageCount;
                                    PrintDocumentInfo info = new PrintDocumentInfo
                                            .Builder("print_output.pdf")
                                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                            .setPageCount(pageCount)
                                            .build();

                                    // Not actually going to do anything for now
                                    callback.onLayoutFinished(info, layoutChanged);
                                }

                                @Override
                                public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                                    InputStream input = null;
                                    OutputStream output = null;
                                    try {
                                        input =  new FileInputStream(pdfFile);
                                        //input =  new ByteArrayInputStream(baos.toByteArray());
                                        output = new FileOutputStream(destination.getFileDescriptor());
                                        byte[] buf = new byte[1024];
                                        int bytesRead;

                                        while ((bytesRead = input.read(buf)) > 0) {
                                            output.write(buf, 0, bytesRead);
                                        }

                                        callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});



                                    } catch (Exception e) {
                                        //Catch exception
                                    } finally {
                                        try {
                                            if (input != null) {
                                                input.close();
                                            }
                                            if (output != null) {
                                                output.close();
                                            }
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                }
                            };

                            PrintAttributes printAttributes = new PrintAttributes.Builder()
                                    .setMediaSize(PrintAttributes.MediaSize.ISO_C8).build();

                            printManager.print(jobName, pda, printAttributes);
                             */


                        }

                    });
                    builder.show();
                    dialog.dismiss();

                }

            }
        });


        RecyclerView productBill = (RecyclerView) dialogView.findViewById(R.id.recyclerView_dialog_bill);
        productBill.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productBill.setLayoutManager(linearLayoutManager);

        adapterFirebaseBill = new FirebaseRecyclerAdapter<Product, ProductBillViewHolder>(
                Product.class,
                R.id.item_bill,
                ProductBillViewHolder.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList")
        ) {
            @Override
            public ProductBillViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false);
                return new ProductBillViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductBillViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productPrice.setText(Utils.convertNumber(model.getUnitPrice()));
                viewHolder.productOrder.setText(model.getProductOrder());
                viewHolder.productQuantity.setText(Utils.convertNumber(model.getUnitQuantity()));
                viewHolder.productTotal.setText(Utils.convertNumber(model.getProductTotal()));

            }
        };

        productBill.setAdapter(adapterFirebaseBill);
        adapterFirebaseBill.notifyDataSetChanged();

        promotionBill.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        promotionBill.setLayoutManager(linearLayoutManager);

        adapterFirebaseBillPromotion = new FirebaseRecyclerAdapter<Product, ProductBillPromotionViewHolder>(
                Product.class,
                R.id.item_bill,
                ProductBillPromotionViewHolder.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("Promotion")
        ) {
            @Override
            public ProductBillPromotionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false);
                return new ProductBillPromotionViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductBillPromotionViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productQuantity.setText(Utils.convertNumber(model.getUnitQuantity()));

            }
        };

        promotionBill.setAdapter(adapterFirebaseBillPromotion);
        adapterFirebaseBillPromotion.notifyDataSetChanged();


        // builder.show();
    }

    private void printBill() {

        final String billCode = b.getString("BillCode");
        final String shopName = b.getString("ShopName");
        final String shopAddress = b.getString("ShopAddress");
        final int forIntValue = b.getInt("ForIntValue");
        final int promotionForIntValue = b.getInt("PromotionForIntValue");
        final String customerCash = b.getString("CustomerCash");
        //final String customerCash = edtCustomerCash.getText().toString();
        final String cashBack = b.getString("CashBack");
        final long timeStamp = Calendar.getInstance().getTime().getTime();

        if(billCode==null || shopName==null || shopAddress==null || customerCash==null || cashBack==null){
            Toast.makeText(getApplicationContext(),"Vui lòng thực hiện lại lệnh in",Toast.LENGTH_LONG).show();
        }else{
            try {
                GraphicalPrint();

                String msShopName = shopName + "\n\n";
                Command.ESC_Align[2] = 0x01;
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x11;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(msShopName.getBytes("GBK"));

                String msDC = shopAddress + "\n\n";
                Command.ESC_Align[2] = 0x01;
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(Command.ESC_Align);
                SendDataByte(msDC.getBytes("GBK"));

                String msPTT = "PHIEU THANH TOAN\n\n";
                Command.ESC_Align[2] = 0x01;
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(Command.ESC_Align);
                SendDataByte(msPTT.getBytes("GBK"));

                String msTime = Utils.getDateCurrentTimeZone(timeStamp);
                String msTTHD = "So HD: " + billCode + "       " + msTime + "\n";
                Command.ESC_Align[2] = 0x00;
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(msTTHD.getBytes("GBK"));

                String msTNV = "Nhan vien: " + employeeName + "\n";
                Command.ESC_Align[2] = 0x00;
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(msTNV.getBytes("GBK"));

                String msD1 = "-----------------------------------------------\n";
                Command.ESC_Align[2] = 0x00;
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(msD1.getBytes("GBK"));

                String msTD = "TT  San pham                      SL    D.Gia\n";
                Command.ESC_Align[2] = 0x00;
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(msTD.getBytes("GBK"));

                String msD2 = "-----------------------------------------------\n";
                Command.ESC_Align[2] = 0x00;
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(msD2.getBytes("GBK"));

                for (int i = 0; i < forIntValue; i++) {
                    String productNameNotViet = b.getString("ProductNameNotViet" + i);
                    String productOrder = b.getString("ProductOrder" + i);
                    String productQuantity = b.getString("ProductQuantity" + i);
                    String productPrice = b.getString("ProductPrice" + i);
                    String productTotal = b.getString("ProductTotal" + i);

                    if (productNameNotViet == null || productOrder == null || productQuantity == null || productPrice == null || productTotal == null) {
                        Toast.makeText(getApplicationContext(), "Đang xử lý...", Toast.LENGTH_LONG).show();
                    } else {

                        //int space1 = 40 - productNameNotViet.length()-productPrice.length()-productQuantity.length();
                        int space1 = 33 - productNameNotViet.length()-productQuantity.length();
                        int space2 = 10 - Utils.convertNumber(productPrice).length();

                        char[] chars = new char[space1];
                        Arrays.fill(chars, ' ');
                        String text = new String(chars);

                        char[] chars2 = new char[space2];
                        Arrays.fill(chars2, ' ');
                        String text2 = new String(chars2);

                        String msTTSP = productOrder + "  " + productNameNotViet + text + productQuantity + text2 + Utils.convertNumber(productPrice) + "\n";
                        Command.ESC_Align[2] = 0x00;
                        SendDataByte(Command.ESC_Align);
                        Command.GS_ExclamationMark[2] = 0x00;
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte(msTTSP.getBytes("GBK"));

                    }
                }

                String KTr = "\n\n";
                Command.ESC_Align[2] = 0x00;
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(KTr.getBytes("GBK"));

                String thanhtien = "Thanh tien:";
                int space2 = 46 - thanhtien.length() - Utils.convertNumber(billPayment + "").length();
                char[] chars2 = new char[space2];
                Arrays.fill(chars2, ' ');
                String text2 = new String(chars2);

                String msTT = "Thanh tien:" + text2 + Utils.convertNumber(billPayment + "") + "\n";
                Command.ESC_Align[2] = 0x00;
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(msTT.getBytes("GBK"));

                if(promotionStatus && !promotionProduct){
                    String kmbt = "Khuyen mai bang tien ";
                    int spaceKMBT = 43 - kmbt.length() - Utils.convertNumber(promotionValue).length()-(promotionRate+"%").length();
                    char[] charsKMBT = new char[spaceKMBT];
                    Arrays.fill(charsKMBT, ' ');
                    String textKMBT = new String(charsKMBT);
                    String msKMBT = "Khuyen mai bang tien " + "("+promotionRate+"%"+"):"+ textKMBT + Utils.convertNumber(promotionValue) + "\n";

                    Command.ESC_Align[2] = 0x00;
                    SendDataByte(Command.ESC_Align);
                    Command.GS_ExclamationMark[2] = 0x00;
                    SendDataByte(Command.GS_ExclamationMark);
                    SendDataByte(msKMBT.getBytes("GBK"));

                    String tc = "Tong cong:";
                    String tcValue = (billPayment-Float.parseFloat(promotionValue))+"";
                    int spaceTc = 46 - tc.length() - Utils.convertNumber(tcValue).length();
                    char[] charsTc = new char[spaceTc];
                    Arrays.fill(charsTc, ' ');
                    String textTc = new String(charsTc);
                    String msTc = "Tong cong:" + textTc + Utils.convertNumber(tcValue) + "\n";

                    Command.ESC_Align[2] = 0x00;
                    SendDataByte(Command.ESC_Align);
                    Command.GS_ExclamationMark[2] = 0x00;
                    SendDataByte(Command.GS_ExclamationMark);
                    SendDataByte(msTc.getBytes("GBK"));
                }

                String tienkhachdua = "Tien khach dua:";
                int space3 = 46 - tienkhachdua.length() - Utils.convertNumber(customerCash + "").length();
                char[] chars3 = new char[space3];
                Arrays.fill(chars3, ' ');
                String text3 = new String(chars3);

                String msTKD = "Tien khach dua:" + text3 + Utils.convertNumber(customerCash) + "\n";
                Command.ESC_Align[2] = 0x00;
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(msTKD.getBytes("GBK"));

                String tienthua = "Tien thua:";
                int space4 = 46 - tienthua.length() - Utils.convertNumber(cashBack + "").length();
                char[] chars4 = new char[space4];
                Arrays.fill(chars4, ' ');
                String text4 = new String(chars4);

                String msTThua = "Tien thua:" + text4 + Utils.convertNumber(cashBack) + "\n";
                Command.ESC_Align[2] = 0x00;
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(msTThua.getBytes("GBK"));

                if(promotionProduct){
                    String msKMBH = "Khuyen mai bang hang:\n";
                    Command.ESC_Align[2] = 0x00;
                    SendDataByte(Command.ESC_Align);
                    Command.GS_ExclamationMark[2] = 0x00;
                    SendDataByte(Command.GS_ExclamationMark);
                    SendDataByte(msKMBH.getBytes("GBK"));
                    for (int i = 0; i < promotionForIntValue; i++) {
                        String promotionNameNotViet = b.getString("PromotionNameNotViet" + i);
                        String promotionQuantity = b.getString("PromotionQuantity" + i);


                        if (promotionNameNotViet == null ||  promotionQuantity == null ) {
                            Toast.makeText(getApplicationContext(), "Đang xử lý...", Toast.LENGTH_LONG).show();
                        } else {

                            //int space1 = 40 - productNameNotViet.length()-productPrice.length()-productQuantity.length();
                            int space1 = 46 - promotionNameNotViet.length()-promotionQuantity.length();

                            char[] chars = new char[space1];
                            Arrays.fill(chars, ' ');
                            String text = new String(chars);

                            String msTTKM = promotionNameNotViet + text + promotionQuantity + "\n";
                            Command.ESC_Align[2] = 0x00;
                            SendDataByte(Command.ESC_Align);
                            Command.GS_ExclamationMark[2] = 0x00;
                            SendDataByte(Command.GS_ExclamationMark);
                            SendDataByte(msTTKM.getBytes("GBK"));

                        }
                    }
                }

                String msXH = "\n\n\n";
                Command.ESC_Align[2] = 0x00;
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x00;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataByte(msXH.getBytes("GBK"));

                SendDataByte(PrinterCommand.POS_Set_Cut(1));
                SendDataByte(PrinterCommand.POS_Set_PrtInit());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }

    private void getProductPosList() {
        recyclerView = (RecyclerView) findViewById(R.id.recycler_pos);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                recyclerView.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fab.isShown())
                    fab.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        adapterFirebase = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_pos,
                ProductViewHolder.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_Product")
        ) {

            @Override
            public void onBindViewHolder(ProductViewHolder viewHolder, int position) {
                //viewHolder.productQuantityView.setVisibility(statesMap.get(position) != null && statesMap.get(position) ? View.VISIBLE : View.GONE);
                viewHolder.productQuantityView.setVisibility(b.getBoolean(position + "") ? View.VISIBLE : View.GONE);
                super.onBindViewHolder(viewHolder, position);
            }

            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pos, parent, false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productPrice.setText(Utils.convertNumber(model.getUnitPrice()));
                Glide.with(getApplicationContext()).load(model.getProductUrl()).error(R.drawable.product).fitCenter().override(300, 200).into(viewHolder.productImage);

            }
        };

        recyclerView.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();
    }

    private void storageCheckRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_check_storage, null);
        builder.setView(dialogView);
        builder.setMessage("Yêu cầu xác nhận tồn kho hiện tại");

        final Dialog dialog = builder.create();
        dialog.show();


        final RecyclerView productStorageList = (RecyclerView) dialogView.findViewById(R.id.recycler_dialog_check_storage);
        Button btnOk = (Button) dialogView.findViewById(R.id.btn_dialog_check_storage);
        final String shopCode = b.getString("ShopCode");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);

                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("StorageList").setValue(null);

                if (shopCode == null) {
                    Toast.makeText(getApplicationContext(), "Đang xử lý...", Toast.LENGTH_LONG).show();
                } else {
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> storageSnap = dataSnapshot.getChildren();
                            for (DataSnapshot p : storageSnap) {
                                final Product product = p.getValue(Product.class);

                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").child(shopCode).child("DateKey").setValue(thisYear + thisMonth + thisDate);
                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").child(shopCode).child("Email").setValue(employeeEmail);
                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("StorageList").push().setValue(product);
                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("Request").setValue(employeeEmail);
                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("Confirm").setValue("False");

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    dialog.dismiss();
                }

            }
        });


        productStorageList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productStorageList.setLayoutManager(linearLayoutManager);

        productStorageList.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager) productStorageList.getLayoutManager()).findFirstVisibleItemPosition();
                productStorageList.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });

        adapterFirebaseStorage = new FirebaseRecyclerAdapter<Product, ProductStorageViewHolder>(
                Product.class,
                R.id.item_product_view_storage,
                ProductStorageViewHolder.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode)
        ) {
            @Override
            public ProductStorageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_view_storage, parent, false);
                return new ProductStorageViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductStorageViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productQuantity.setText(model.getUnitQuantity());

            }
        };

        productStorageList.setAdapter(adapterFirebaseStorage);
        adapterFirebaseStorage.notifyDataSetChanged();

        //builder.show();

    }

    private void storageCheckConfirmDialog(final String dateKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_check_storage, null);
        builder.setView(dialogView);
        builder.setMessage("Xác nhận tồn kho hiện tại");
        final Dialog dialog = builder.create();
        dialog.show();

        RecyclerView productStorageList = (RecyclerView) dialogView.findViewById(R.id.recycler_dialog_check_storage);
        Button btnOk = (Button) dialogView.findViewById(R.id.btn_dialog_check_storage);
        final String shopCode = b.getString("ShopCode");

        productStorageList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productStorageList.setLayoutManager(linearLayoutManager);

        if (dateKey == null || shopCode == null) {
            Toast.makeText(getApplicationContext(), "Đang xử lý...", Toast.LENGTH_LONG).show();
        } else {
            adapterFirebaseStorageConfirm = new FirebaseRecyclerAdapter<Product, ProductStorageViewHolder>(
                    Product.class,
                    R.id.item_product_view_storage,
                    ProductStorageViewHolder.class,
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("Request").child(shopCode).child(dateKey).child("StorageList")
            ) {
                @Override
                public ProductStorageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_view_storage, parent, false);
                    return new ProductStorageViewHolder(v);
                }


                @Override
                protected void populateViewHolder(ProductStorageViewHolder viewHolder, Product model, int position) {
                    viewHolder.productName.setText(model.getProductName());
                    viewHolder.productQuantity.setText(model.getUnitQuantity());

                }
            };

            productStorageList.setAdapter(adapterFirebaseStorageConfirm);
            adapterFirebaseStorageConfirm.notifyDataSetChanged();
        }


        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);

                if (dateKey == null || shopCode == null) {
                    Toast.makeText(getApplicationContext(), "Đang xử lý...", Toast.LENGTH_LONG).show();
                } else {
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("Request").child(shopCode).child(dateKey).child("Confirm").setValue(employeeEmail);
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").child(shopCode).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();
                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("RequestPending")) {

                                        Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild(shopCode)){
                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("Email").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            String email = dataSnapshot.getValue().toString();
                                                            if (!employeeEmail.equals(email)) {
                                                                Snackbar.make(constraintView, "Vui lòng xác nhận tồn quỹ cửa hàng", Snackbar.LENGTH_INDEFINITE)
                                                                        .setAction("Xem", new View.OnClickListener() {
                                                                            @Override
                                                                            public void onClick(View v) {
                                                                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("DateKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        String dateKey = dataSnapshot.getValue().toString();
                                                                                        cashCheckConfirmDialog(dateKey);

                                                                                        // Toast.makeText(getApplicationContext(),dateKey,Toast.LENGTH_LONG).show();
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
                    });
                }

            }
        });

    }

    private void cashCheckRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_bill_check, null);
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        final RecyclerView billList = (RecyclerView) dialogView.findViewById(R.id.recycler_dialog_bill_check);
        final TextView tvCashBill = (TextView) dialogView.findViewById(R.id.tv_dialog_bill_check_billTotal);
        final TextView tvCashFirst = (TextView) dialogView.findViewById(R.id.tv_dialog_bill_check_cashFirst);
        final TextView tvCashOut = (TextView) dialogView.findViewById(R.id.tv_dialog_bill_check_cashout);
        final TextView tvCashTotal = (TextView) dialogView.findViewById(R.id.tv_dialog_bill_check_total);
        final TextView tvBillQuantity = (TextView) dialogView.findViewById(R.id.tv_dialog_bill_check_billQuantity);
        Button btnConfirm = (Button) dialogView.findViewById(R.id.btn_dialog_bill_check_ok);
        final String shopCode = b.getString("ShopCode");

        Constants.refDatabase.child(emailLogin).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Z_POS_EmployeeCash")) {
                    Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeCash").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(employeeEmail)) {
                                Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeCash").child(employeeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(thisYear+thisMonth+thisDate)){
                                            Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeCash").child(employeeEmail).child(thisYear + thisMonth + thisDate).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    final String totalCash = dataSnapshot.getValue().toString();
                                                    tvCashBill.setText(Utils.convertNumber(totalCash));
                                                    b.putString("CashBill", totalCash);

                                                    if (shopCode == null) {
                                                        Toast.makeText(getApplicationContext(), "Đang tải dữ liệu...", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.hasChild(thisYear + thisMonth + thisDate)) {
                                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            if (dataSnapshot.hasChild("CashOut/" + employeeEmail)) {
                                                                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("CashOut/" + employeeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        final String cashOut = dataSnapshot.getValue().toString();
                                                                                        tvCashOut.setText(Utils.convertNumber(cashOut));

                                                                                        Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashFirst").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                            @Override
                                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                                String cashFirst = dataSnapshot.getValue().toString();
                                                                                                float cashTotal = Float.parseFloat(cashFirst) + Float.parseFloat(totalCash) - Float.parseFloat(cashOut);
                                                                                                tvCashTotal.setText(Utils.convertNumber(cashTotal + ""));
                                                                                                b.putString("CashOut", cashOut);
                                                                                                b.putString("CashTotal", cashTotal + "");
                                                                                                b.putString("CashFirst", cashFirst);

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
                                                                            } else {
                                                                                tvCashOut.setText("0");
                                                                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashFirst").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                    @Override
                                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                        String cashFirst = dataSnapshot.getValue().toString();
                                                                                        float cashTotal = Float.parseFloat(cashFirst) + Float.parseFloat(totalCash);
                                                                                        tvCashTotal.setText(Utils.convertNumber(cashTotal + ""));
                                                                                        tvCashFirst.setText(Utils.convertNumber(cashFirst));

                                                                                        b.putString("CashOut", "0");
                                                                                        b.putString("CashTotal", cashTotal + "");
                                                                                        b.putString("CashFirst", cashFirst);
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

                                                                    tvCashOut.setText("0");
                                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashFirst").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            String cashFirst = dataSnapshot.getValue().toString();
                                                                            float cashTotal = Float.parseFloat(cashFirst) + Float.parseFloat(totalCash);
                                                                            tvCashTotal.setText(Utils.convertNumber(cashTotal + ""));
                                                                            tvCashFirst.setText(Utils.convertNumber(cashFirst));

                                                                            b.putString("CashOut", "0");
                                                                            b.putString("CashTotal", cashTotal + "");
                                                                            b.putString("CashFirst", cashFirst);
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

                                        }else{
                                            tvCashBill.setText(Utils.convertNumber("0"));
                                            b.putString("CashBill", "0");
                                            Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashFirst").child(shopCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    String cashFirst = dataSnapshot.getValue().toString();
                                                    float cashTotal = Float.parseFloat(cashFirst);
                                                    tvCashTotal.setText(Utils.convertNumber(cashTotal + ""));
                                                    tvCashFirst.setText(Utils.convertNumber(cashFirst));

                                                    b.putString("CashOut", "0");
                                                    b.putString("CashTotal", cashTotal + "");
                                                    b.putString("CashFirst", cashFirst);
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
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Constants.refDatabase.child(emailLogin).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild("Z_POS_EmployeeBill")) {
                    Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeBill").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(employeeEmail)) {
                                Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeBill").child(employeeEmail).child(thisYear + thisMonth + thisDate).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long itemCount = dataSnapshot.getChildrenCount();
                                        tvBillQuantity.setText(Utils.convertNumber(itemCount + ""));

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

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("BillList").setValue(null);

                final String cashOut = b.getString("CashOut");
                final String shopCode = b.getString("ShopCode");
                final String cashTotal = b.getString("CashTotal");
                final String cashBill = b.getString("CashBill");
                final String cashFirst = b.getString("CashFirst");

                if (cashBill == null || shopCode == null || cashOut == null ||cashTotal == null ||cashFirst == null) {
                    Toast.makeText(getApplicationContext(), "Lỗi dữ liệu...", Toast.LENGTH_LONG).show();
                } else {
                    Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeBill").child(employeeEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(thisYear+thisMonth+thisDate)){
                                Constants.refDatabase.child(emailLogin).child("Z_POS_EmployeeBill").child(employeeEmail).child(thisYear + thisMonth + thisDate).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> billSnap = dataSnapshot.getChildren();
                                        for (DataSnapshot b : billSnap) {
                                            Bill bill = b.getValue(Bill.class);

                                            Cash cash = new Cash(cashFirst, cashBill, cashOut, cashTotal + "");

                                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("DateKey").setValue(thisYear + thisMonth + thisDate);
                                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("Email").setValue(employeeEmail);

                                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("BillList").push().setValue(bill);
                                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("cash").setValue(cash);
                                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("Request").setValue(employeeEmail);
                                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("Confirm").setValue("False").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    dialog.dismiss();
                                                }
                                            });


                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }else{

                                Cash cash = new Cash(cashFirst, cashBill, cashOut, cashTotal + "");

                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("DateKey").setValue(thisYear + thisMonth + thisDate);
                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("Email").setValue(employeeEmail);

                                //Constants.refPOSBillCheck.child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("BillList").push().setValue(bill);
                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("cash").setValue(cash);
                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("Request").setValue(employeeEmail);
                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("Confirm").setValue("False").addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        dialog.dismiss();
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
        });

        billList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        billList.setLayoutManager(linearLayoutManager);

        billList.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager) billList.getLayoutManager()).findFirstVisibleItemPosition();
                billList.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });

        DatabaseReference refCurrentEmployeeBill = Constants.refEmployeeBill.child(employeeEmail).child(thisYear + thisMonth + thisDate);

        adapterFirebaseBillCash = new FirebaseRecyclerAdapter<Bill, BillViewHolder>(
                Bill.class,
                R.id.item_bill_info,
                BillViewHolder.class,
                refCurrentEmployeeBill
        ) {
            @Override
            public BillViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill_info, parent, false);
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

    }

    private void cashCheckConfirmDialog(final String dateKey) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_bill_check, null);
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        final RecyclerView billList = (RecyclerView) dialogView.findViewById(R.id.recycler_dialog_bill_check);
        final TextView tvCashBill = (TextView) dialogView.findViewById(R.id.tv_dialog_bill_check_billTotal);
        final TextView tvCashFirst = (TextView) dialogView.findViewById(R.id.tv_dialog_bill_check_cashFirst);
        final TextView tvCashOut = (TextView) dialogView.findViewById(R.id.tv_dialog_bill_check_cashout);
        final TextView tvCashTotal = (TextView) dialogView.findViewById(R.id.tv_dialog_bill_check_total);
        final TextView tvBillQuantity = (TextView) dialogView.findViewById(R.id.tv_dialog_bill_check_billQuantity);
        Button btnConfirm = (Button) dialogView.findViewById(R.id.btn_dialog_bill_check_ok);

        final String shopCode = b.getString("ShopCode");

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                if (dateKey == null || shopCode == null) {
                    Toast.makeText(getApplicationContext(), "Lỗi dữ liệu...", Toast.LENGTH_LONG).show();
                } else {

                    Constants.refDatabase.child(emailLogin).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild("Z_POS_BillCheck")) {
                                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.hasChild("Request/" + shopCode + "/" + dateKey)) {
                                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request/" + shopCode + "/" + dateKey).child("cash").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    Cash cash = dataSnapshot.getValue(Cash.class);
                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashFirst").child(shopCode).setValue(cash.getCashTotal());
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

                    Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request").child(shopCode).child(dateKey).child("Confirm").setValue(employeeEmail);
                    Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).setValue(null);
                    dialog.dismiss();
                }
            }
        });

        if (shopCode == null) {
            Toast.makeText(getApplicationContext(), "Lỗi dữ liệu...", Toast.LENGTH_LONG).show();
        } else {
            Constants.refDatabase.child(emailLogin).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Z_POS_BillCheck")) {
                        Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("Request")) {
                                    Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request/" + shopCode + "/" + dateKey).child("BillList").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            long billCount = dataSnapshot.getChildrenCount();
                                            tvBillQuantity.setText(billCount + "");
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
            Constants.refDatabase.child(emailLogin).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild("Z_POS_BillCheck")) {
                        Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChild("Request/" + shopCode + "/" + dateKey)) {
                                    Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("Request/" + shopCode + "/" + dateKey).child("cash").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Cash cash = dataSnapshot.getValue(Cash.class);
                                            tvCashBill.setText(Utils.convertNumber(cash.getCashBill()));
                                            tvCashTotal.setText(Utils.convertNumber(cash.getCashTotal()));
                                            tvCashOut.setText(Utils.convertNumber(cash.getCashOut()));
                                            tvCashFirst.setText(Utils.convertNumber(cash.getCashFirst()));
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

            billList.setHasFixedSize(true);
            linearLayoutManager = new LinearLayoutManager(getApplicationContext());
            billList.setLayoutManager(linearLayoutManager);

            billList.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
                @Override
                public void onLoadMore(int current_page) {
                    int lastFirstVisiblePosition = ((LinearLayoutManager) billList.getLayoutManager()).findFirstVisibleItemPosition();
                    billList.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
                }
            });

            DatabaseReference refCurrentEmployeeBill = Constants.refPOSBillCheck.child("Request").child(shopCode).child(dateKey).child("BillList");

            adapterFirebaseBillCash = new FirebaseRecyclerAdapter<Bill, BillViewHolder>(
                    Bill.class,
                    R.id.item_bill_info,
                    BillViewHolder.class,
                    refCurrentEmployeeBill
            ) {
                @Override
                public BillViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill_info, parent, false);
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
        }
    }

    private void cashOutRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_cash_out, null);
        builder.setMessage("Xác nhận số tiền mặt xuất quỹ trong ca làm việc");
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        final EditText edtCashNumber = (EditText) dialogView.findViewById(R.id.edt_dialog_cash_out_number);
        //final TextView tvCashNumber = (TextView)dialogView.findViewById(R.id.tv_dialog_cash_out_number);
        Button btnOk = (Button) dialogView.findViewById(R.id.btn_dialog_cash_out_ok);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                String cashOutNumber = edtCashNumber.getText().toString();
                final String shopCode = b.getString("ShopCode");
                if (TextUtils.isEmpty(cashOutNumber)) {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập số tiền xuất quỹ", Toast.LENGTH_LONG).show();
                } else if (shopCode == null) {
                    Toast.makeText(getApplicationContext(), "Lỗi dữ liệu, vui lòng thử lại", Toast.LENGTH_LONG).show();
                } else {
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").child("RequestPending").child(shopCode).child("DateKey").setValue(thisYear + thisMonth + thisDate);
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").child("RequestPending").child(shopCode).child("Email").setValue(employeeEmail);

                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("CashOut").setValue(cashOutNumber);
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("Request").setValue(employeeEmail);
                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopCashOutCheck").child("Request").child(shopCode).child(thisYear + thisMonth + thisDate).child("Confirm").setValue("False").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();
                        }
                    });
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            startActivity(new Intent(this, MainActivity.class));
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pos, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            //startActivity(new Intent(getApplicationContext(), PosActivity.class));
            billPushKey = Constants.refBillPOS.push().getKey();
            onResume();
            billPayment = 0;
            paymentValue.setText("0");
            b.clear();
            //Constants.refBillPOS.child(billPushKey).setValue(null);
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.storage_check_request) {
            storageCheckRequestDialog();
        } else if (id == R.id.storage_check_confirm) {
            final String shopCode = b.getString("ShopCode");
            if (shopCode == null) {
                Toast.makeText(getApplicationContext(), "Lỗi dữ liệu...", Toast.LENGTH_LONG).show();
            } else {
                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("RequestPending")) {
                            Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(shopCode)){
                                        Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").child(shopCode).child("Email").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String email = dataSnapshot.getValue().toString();
                                                if (!employeeEmail.equals(email)) {
                                                    Snackbar.make(constraintView, "Vui lòng xác nhận tồn kho của cửa hàng", Snackbar.LENGTH_INDEFINITE)
                                                            .setAction("Xem", new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending").child(shopCode).child("DateKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            String dateKey = dataSnapshot.getValue().toString();
                                                                            storageCheckConfirmDialog(dateKey);

                                                                            // Toast.makeText(getApplicationContext(),dateKey,Toast.LENGTH_LONG).show();
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

                        } else {
                            Toast.makeText(getApplicationContext(), "Không có yêu cầu xác nhận tồn kho nào", Toast.LENGTH_LONG).show();
                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("RequestPending")) {

                                        Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending/Email").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String email = dataSnapshot.getValue().toString();
                                                if (!employeeEmail.equals(email)) {
                                                    Snackbar.make(constraintView, "Vui lòng xác nhận tồn kho của cửa hàng", Snackbar.LENGTH_INDEFINITE)
                                                            .setAction("Xem", new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorageCheck").child("RequestPending/DateKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            String dateKey = dataSnapshot.getValue().toString();
                                                                            storageCheckConfirmDialog(dateKey);

                                                                            // Toast.makeText(getApplicationContext(),dateKey,Toast.LENGTH_LONG).show();
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

        } else if (id == R.id.cash_check_request) {
            cashCheckRequestDialog();

        } else if (id == R.id.cash_out_request) {
            cashOutRequestDialog();
        } else if (id == R.id.cash_check_confirm) {
            final String shopCode = b.getString("ShopCode");
            if (shopCode == null) {
                Toast.makeText(getApplicationContext(), "Lỗi dữ liệu...", Toast.LENGTH_LONG).show();
            } else {

                Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("RequestPending")) {
                            Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(shopCode)){
                                        Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("Email").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                String email = dataSnapshot.getValue().toString();
                                                if (!employeeEmail.equals(email)) {
                                                    Snackbar.make(constraintView, "Vui lòng xác nhận tồn quỹ của cửa hàng", Snackbar.LENGTH_INDEFINITE)
                                                            .setAction("Xem", new View.OnClickListener() {
                                                                @Override
                                                                public void onClick(View v) {
                                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_BillCheck").child("RequestPending").child(shopCode).child("DateKey").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                        @Override
                                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                                            String dateKey = dataSnapshot.getValue().toString();
                                                                            cashCheckConfirmDialog(dateKey);

                                                                            // Toast.makeText(getApplicationContext(),dateKey,Toast.LENGTH_LONG).show();
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

                        } else {
                            Toast.makeText(getApplicationContext(), "Không có yêu cầu xác nhận tồn quĩ", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        } else if (id == R.id.pos_logout) {
            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            // viewSavedBillListDialog();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantityView;
        ImageView productImage, productAdd, productRemove;

        ProductViewHolder(final View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.tv_item_pos_productname);
            productPrice = (TextView) itemView.findViewById(R.id.tv_item_pos_productprice);
            productImage = (ImageView) itemView.findViewById(R.id.iv_item_pos_product_image);
            productAdd = (ImageView) itemView.findViewById(R.id.iv_item_pos_add);
            productRemove = (ImageView) itemView.findViewById(R.id.iv_item_pos_remove);
            productQuantityView = (TextView) itemView.findViewById(R.id.tv_item_pos_product_quantity);

            productAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    final int position = getLayoutPosition();
                    String itemKey = adapterFirebase.getRef(position).getKey();
                    productAdd.setVisibility(View.INVISIBLE);
                   final String shopCode = b.getString("ShopCode");

                    if (shopCode == null) {
                        Toast.makeText(getApplicationContext(), "Đang xử lý...", Toast.LENGTH_LONG).show();
                    } else {
                        Constants.refDatabase.child(emailLogin).child("Z_POS_Product").child(itemKey).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Product product = dataSnapshot.getValue(Product.class);
                                final String productPrice = product.getUnitPrice();
                                final String productName = product.getProductName();
                                final String productNameNoViet = product.getNameNotViet();

                                //Get Storage
                                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(shopCode).child(productName).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String storage = dataSnapshot.getValue().toString();
                                        final float productStorage = Float.parseFloat(storage);

                                        Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (dataSnapshot.hasChild(billPushKey)) {
                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (dataSnapshot.hasChild(productName)) {
                                                                Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").child(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @SuppressLint("SetTextI18n")
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        Product p = dataSnapshot.getValue(Product.class);
                                                                        String productQuantity = p.getUnitQuantity();
                                                                        String productPrice = p.getUnitPrice();
                                                                        String productOrder = p.getProductOrder();
                                                                        String productName = p.getProductName();
                                                                        int updateProductQuantity = Integer.parseInt(productQuantity) + 1;

                                                                        if (productStorage < updateProductQuantity) {
                                                                            Toast.makeText(getApplicationContext(), "Không đủ hàng trong kho", Toast.LENGTH_LONG).show();
                                                                        } else {
                                                                            billPayment = billPayment + Float.parseFloat(productPrice);
                                                                            paymentValue.setText(Utils.convertNumber(billPayment + ""));

                                                                            productQuantityView.setText(updateProductQuantity + "");
                                                                            productQuantityView.setVisibility(View.VISIBLE);
                                                                            b.putBoolean(position + "", true);

                                                                            float updateProductTotal = Float.parseFloat(productPrice) * updateProductQuantity;
                                                                            Product billProduct = new Product(productName, productPrice, updateProductQuantity + "", productOrder, updateProductTotal + "", productNameNoViet);

                                                                            Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").child(productName).setValue(billProduct).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    productAdd.setVisibility(View.VISIBLE);
                                                                                }
                                                                            });
                                                                        }

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            } else {
                                                                if (productStorage >= 1) {
                                                                    billPayment = billPayment + Float.parseFloat(productPrice);
                                                                    paymentValue.setText(Utils.convertNumber(billPayment + ""));

                                                                    productOrder++;
                                                                    productQuantityView.setText(1 + "");
                                                                    productQuantityView.setVisibility(View.VISIBLE);
                                                                    Product pList = new Product(productName, productPrice, 1 + "", productOrder + "", productPrice, productNameNoViet);
                                                                    Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").child(productName).setValue(pList).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            productAdd.setVisibility(View.VISIBLE);
                                                                        }
                                                                    });
                                                                } else {
                                                                    Toast.makeText(getApplicationContext(), "Không đủ hàng trong kho", Toast.LENGTH_LONG).show();
                                                                }
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                } else {

                                                    if (productStorage >= 1) {
                                                        billPayment = billPayment + Float.parseFloat(productPrice);
                                                        paymentValue.setText(Utils.convertNumber(billPayment + ""));

                                                        productOrder++;
                                                        productQuantityView.setText(1 + "");
                                                        productQuantityView.setVisibility(View.VISIBLE);
                                                        b.putBoolean(position + "", true);
                                                        //productQuantityView.setVisibility(productQuantityView.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                                                        //statesMap.put(position,productQuantityView.getVisibility() == View.VISIBLE );
                                                        Product pList = new Product(productName, productPrice, 1 + "", productOrder + "", productPrice, productNameNoViet);
                                                        Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").child(productName).setValue(pList).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                productAdd.setVisibility(View.VISIBLE);
                                                            }
                                                        });
                                                    } else {
                                                        Toast.makeText(getApplicationContext(), "Không đủ hàng trong kho", Toast.LENGTH_LONG).show();

                                                    }

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

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }

                }
            });

            productRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    productRemove.setVisibility(View.INVISIBLE);
                    final int position = getLayoutPosition();
                    String itemKey = adapterFirebase.getRef(position).getKey();
                    Constants.refProductPOS.child(itemKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Product product = dataSnapshot.getValue(Product.class);
                            final String productName = product.getProductName();
                            final String productNameNoViet = product.getNameNotViet();

                            Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild(billPushKey)) {
                                        Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if (dataSnapshot.hasChild(productName)) {
                                                            Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").child(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    Product p = dataSnapshot.getValue(Product.class);
                                                                    String productPrice = p.getUnitPrice();
                                                                    String productQuantity = p.getUnitQuantity();
                                                                    String productOrder = p.getProductOrder();
                                                                    billPayment = billPayment - Float.parseFloat(productPrice);
                                                                    paymentValue.setText(Utils.convertNumber(billPayment + ""));
                                                                    int updateProductQuantity = Integer.parseInt(productQuantity) - 1;
                                                                    float updateProductTotal = Float.parseFloat(productPrice) * updateProductQuantity;

                                                                    if (updateProductQuantity == 0) {
                                                                        Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").child(productName).setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                productRemove.setVisibility(View.VISIBLE);
                                                                            }
                                                                        });
                                                                        productQuantityView.setVisibility(View.INVISIBLE);
                                                                        b.putBoolean(position + "", false);

                                                                    } else if (updateProductQuantity < 0) {
                                                                        Toast.makeText(getApplicationContext(), "Chưa chọn sản phẩm", Toast.LENGTH_LONG).show();
                                                                        productRemove.setVisibility(View.VISIBLE);

                                                                    } else {
                                                                        productQuantityView.setText(updateProductQuantity + "");
                                                                        productQuantityView.setVisibility(View.VISIBLE);
                                                                        b.putBoolean(position + "", true);

                                                                        Product billProduct = new Product(productName, productPrice, updateProductQuantity + "", productOrder, updateProductTotal + "", productNameNoViet);

                                                                        Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("ProductList").child(productName).setValue(billProduct).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                productRemove.setVisibility(View.VISIBLE);

                                                                            }
                                                                        });

                                                                    }
                                                                }

                                                                @Override
                                                                public void onCancelled(DatabaseError databaseError) {

                                                                }
                                                            });

                                                        } else {
                                                            Toast.makeText(getApplicationContext(), "Chưa chọn sản phẩm", Toast.LENGTH_LONG).show();

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

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Chưa chọn sản phẩm", Toast.LENGTH_LONG).show();
                                        productRemove.setVisibility(View.VISIBLE);

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
            });

        }
    }

    private class ProductBillViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productOrder, productTotal, productQuantity;

        ProductBillViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.tv_item_bill_productname);
            productPrice = (TextView) itemView.findViewById(R.id.tv_item_bill_unit_price);
            productOrder = (TextView) itemView.findViewById(R.id.tv_item_bill_order);
            productTotal = (TextView) itemView.findViewById(R.id.tv_item_bill_total);
            productQuantity = (TextView) itemView.findViewById(R.id.tv_item_bill_quantity);

        }
    }

    private class ProductBillPromotionViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productOrder, productTotal, productQuantity;

        ProductBillPromotionViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.tv_item_bill_productname);
            productPrice = (TextView) itemView.findViewById(R.id.tv_item_bill_unit_price);
            productOrder = (TextView) itemView.findViewById(R.id.tv_item_bill_order);
            productTotal = (TextView) itemView.findViewById(R.id.tv_item_bill_total);
            productQuantity = (TextView) itemView.findViewById(R.id.tv_item_bill_quantity);

            productPrice.setVisibility(View.INVISIBLE);
            productOrder.setVisibility(View.INVISIBLE);
            productTotal.setVisibility(View.INVISIBLE);

        }
    }

    private class ProductStorageViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productQuantity;

        ProductStorageViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.tv_item_product_view_storage_name);
            productQuantity = (TextView) itemView.findViewById(R.id.tv_item_product_view_storage_quantity);

        }
    }

    private class ProductStorageViewHolder2 extends RecyclerView.ViewHolder {
        TextView productName, productStorage;

        ProductStorageViewHolder2(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.tv_item_product_view_storage_name);
            productStorage = (TextView) itemView.findViewById(R.id.tv_item_product_view_storage_quantity);
            productStorage.setVisibility(View.INVISIBLE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);

                    int position = getLayoutPosition();
                    Product product = adapterFirebasePromotion.getItem(position);
                    final String promotionName = product.getProductName();
                    final String promotionNameNotViet = product.getNameNotViet();
                    AlertDialog.Builder builder = new AlertDialog.Builder(PosActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_pos_promotion_quantity, null);
                    builder.setView(dialogView);
                    final Dialog dialog = builder.create();
                    dialog.show();

                    TextView tvPromotionName = (TextView)dialogView.findViewById(R.id.tv_pos_promotionQuantity);
                    final EditText edtPromtionQuantity = (EditText)dialogView.findViewById(R.id.edt_pos_promotion_quantity);
                    Button btnOk = (Button)dialogView.findViewById(R.id.btn_pos_promotion_quantity);

                    tvPromotionName.setText(promotionName);

                    btnOk.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            v.startAnimation(Constants.buttonClick);
                            String promotionQuantity = edtPromtionQuantity.getText().toString();
                            if(TextUtils.isEmpty(promotionQuantity)){
                                Toast.makeText(getApplicationContext(),"Vui lòng nhập số lượng khuyến mãi",Toast.LENGTH_LONG).show();
                            }else if(promotionValue==null){
                                Toast.makeText(getApplicationContext(),"Vui lòng thêm sản phẩm vào hóa đơn bán hàng",Toast.LENGTH_LONG).show();
                            }else{
                                    promotionProduct = true;
                                    Product promotionProduct = new Product(promotionName,promotionQuantity,promotionNameNotViet);
                                    promotionQuantityFloat = Float.parseFloat(promotionQuantity);
                                    Constants.refDatabase.child(emailLogin).child("Z_POS_Bill").child(billPushKey).child("Promotion").push().setValue(promotionProduct);

                            }

                                dialog.dismiss();
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
            billCode = (TextView) itemView.findViewById(R.id.tv_item_bill_info_billCode);
            billPayment = (TextView) itemView.findViewById(R.id.tv_item_bill_info_payment);

        }
    }

    private void SendDataByte(byte[] data) {

        if (mService.getState() != BluetoothService.STATE_CONNECTED) {
            Toast.makeText(this, "Không có kết nối!", Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        mService.write(data);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG)
            Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE: {
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(
                            DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    if (BluetoothAdapter.checkBluetoothAddress(address)) {
                        BluetoothDevice device = mBluetoothAdapter
                                .getRemoteDevice(address);
                        // Attempt to connect to the device
                        mService.connect(device);
                    }
                }
                break;
            }
            case REQUEST_ENABLE_BT: {
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a session
                    mService = new BluetoothService(this, mHandler);
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            }


        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (DEBUG)
                        Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:

                            break;
                        case BluetoothService.STATE_CONNECTING:
                            // mTitle.setText(R.string.title_connecting);
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            //mTitle.setText(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:

                    break;
                case MESSAGE_READ:

                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Đã kết nối tới máy in " + mConnectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
                case MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), "Mất kết nối với máy in",
                            Toast.LENGTH_SHORT).show();

                    break;
                case MESSAGE_UNABLE_CONNECT:
                    Toast.makeText(getApplicationContext(), "Không thể kết nối tới máy in",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void GraphicalPrint() {

        Bitmap bm1 = Utils.getImageFromAssetsFile(this, "nestart_logo.png");

        int nMode = 0;
        int nPaperWidth = 150;

        if (bm1 != null) {
            byte[] data = PrintPicture.POS_PrintBMP(bm1, nPaperWidth, nMode);
            Command.ESC_Align[2] = 0x01;
            SendDataByte(Command.ESC_Align);
            //SendDataByte(Command.ESC_Init);
            //SendDataByte(Command.LF);
            SendDataByte(data);
            SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(30));
            //SendDataByte(PrinterCommand.POS_Set_Cut(1));
            SendDataByte(PrinterCommand.POS_Set_PrtInit());
        }

    }


}

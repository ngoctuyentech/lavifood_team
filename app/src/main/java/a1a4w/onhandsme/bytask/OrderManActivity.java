package a1a4w.onhandsme.bytask;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import a1a4w.onhandsme.MainActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.bytask.distribution.FilterClientActivity;
import a1a4w.onhandsme.bytask.distribution.FilterEmployeeActivity;
import a1a4w.onhandsme.bytask.distribution.FilterProductActivity;
import a1a4w.onhandsme.bytask.distribution.FilterTimeActivity;
import a1a4w.onhandsme.list.ClientListActivity;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.Employee;
import a1a4w.onhandsme.model.Group;
import a1a4w.onhandsme.model.MapModel;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.order.PreviewOrderActivivity;
import a1a4w.onhandsme.order.PrintPreviewActivity;
import a1a4w.onhandsme.order.ViewOrderDetailActivity;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.MySpinerAdapter;
import fr.ganfra.materialspinner.MaterialSpinner;
import im.delight.android.location.SimpleLocation;

import static a1a4w.onhandsme.list.AddClientActivity.MY_REQUEST_LOCATION;
import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class OrderManActivity extends AppCompatActivity {
    private RecyclerView recyclerViewUnApproved, recyclerViewApproved, recyclerViewDenied, clientList;
    private FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderUnapproved> adapterFirebaseUnapproved;
    private FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderApproved> adapterFirebaseApproved;
    private FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderDenied> adapterFirebaseDenied;
    private FirebaseRecyclerAdapter<Product, ProductViewHolder> adapterFirebaseProduct;
    private FirebaseRecyclerAdapter<Employee, EmployeeViewHolder> adapterFirebaseEmployee;
    private FirebaseRecyclerAdapter<Client, ClientViewHolder> adapterFirebaseClient;
    FirebaseRecyclerAdapter<Group, GroupViewHolder> adapterFirebaseClientGroup;

    private LinearLayoutManager linearLayoutManager;
    private LinearLayout layoutApproved, layoutUnapproved, layoutDenied, boxApproved, boxUnApproved, boxCancelled;
    private DatabaseReference refApproved, refUnapproved, refDenied;
    private Bundle b = new Bundle();
    private String emailLogin, choosenMonth, choosenYear, choosenProduct, choosenEmployee, employeeEmail;

    private Dialog dialogProductList, dialogEmployeeList;
    private TextView tvChooseProduct, tvEmployeeName, tvUnapproved, tvApproved, tvDenied;
    private boolean saleMan;
    private double latitude, longitude;
    private RecyclerView rvClientList;
    int MY_REQUEST_CALL = 2;
    private String clientPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_order_man);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_order);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");
        saleMan = intent.getBooleanExtra("SaleMan", false);
        //refCurrentClient = Constants.refClientMan.child("Đại lý").child("Nha Trang");
        employeeEmail = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail().replace(".", ",");

        tvUnapproved = (TextView) findViewById(R.id.tv_order_unapproved);
        tvApproved = (TextView) findViewById(R.id.tv_order_approved);
        tvDenied = (TextView) findViewById(R.id.tv_order_denied);

        layoutApproved = (LinearLayout) findViewById(R.id.layout_approved);
        layoutUnapproved = (LinearLayout) findViewById(R.id.layout_unapproved);
        layoutDenied = (LinearLayout) findViewById(R.id.layout_denied);
        boxApproved = (LinearLayout) findViewById(R.id.order_man_boxApproved);
        boxUnApproved = (LinearLayout) findViewById(R.id.order_man_boxUnApproved);
        boxCancelled = (LinearLayout) findViewById(R.id.order_man_boxCanceled);

        boxUnApproved.setBackgroundColor(Color.WHITE);
        layoutUnapproved.setVisibility(View.VISIBLE);
        tvUnapproved.setTextColor(getResources().getColor(R.color.colorAccent));

        ImageButton ibNewOrder = (ImageButton) findViewById(R.id.ib_order_man_newOrder);


        if(saleMan){
            ibNewOrder.setVisibility(View.GONE);
        }

        ibNewOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent it = new Intent(OrderManActivity.this, ClientListActivity.class);
                it.putExtra("EmailLogin", emailLogin);
                it.putExtra("SaleMan", saleMan);
                startActivity(it);
            }
        });


        initializeUnapproved();
        initializeApproved();
        initializeDenied();

        boxApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                boxApproved.setBackgroundColor(Color.WHITE);
                boxUnApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxCancelled.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                layoutApproved.setVisibility(View.VISIBLE);
                layoutUnapproved.setVisibility(View.INVISIBLE);
                layoutDenied.setVisibility(View.INVISIBLE);

                tvApproved.setTextColor(getResources().getColor(R.color.colorAccent));
                tvUnapproved.setTextColor(getResources().getColor(android.R.color.white));
                tvDenied.setTextColor(getResources().getColor(android.R.color.white));


            }
        });

        boxUnApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                boxUnApproved.setBackgroundColor(Color.WHITE);
                boxApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxCancelled.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                layoutApproved.setVisibility(View.INVISIBLE);
                layoutUnapproved.setVisibility(View.VISIBLE);
                layoutDenied.setVisibility(View.INVISIBLE);

                tvUnapproved.setTextColor(getResources().getColor(R.color.colorAccent));
                tvApproved.setTextColor(getResources().getColor(android.R.color.white));
                tvDenied.setTextColor(getResources().getColor(android.R.color.white));

            }
        });

        boxCancelled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                boxCancelled.setBackgroundColor(Color.WHITE);
                boxApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxUnApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                layoutApproved.setVisibility(View.INVISIBLE);
                layoutUnapproved.setVisibility(View.INVISIBLE);
                layoutDenied.setVisibility(View.VISIBLE);

                tvDenied.setTextColor(getResources().getColor(R.color.colorAccent));
                tvApproved.setTextColor(getResources().getColor(android.R.color.white));
                tvUnapproved.setTextColor(getResources().getColor(android.R.color.white));
            }
        });

        locationPreparation();
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
                if (location != null) {

                    latitude = location.getLatitude();
                    longitude = location.getLongitude();

                } else {
                    Toast.makeText(getApplicationContext(), "Không lấy được vị trí hiện tại, vui lòng thử lại!", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void initializeDenied() {
        recyclerViewDenied = (RecyclerView) findViewById(R.id.order_denied_recyclerview);
        recyclerViewDenied.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewDenied.setLayoutManager(linearLayoutManager);

        getDeniedOrder();


    }

    private void initializeApproved() {
        recyclerViewApproved = (RecyclerView) findViewById(R.id.order_approved_recyclerview);
        recyclerViewApproved.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewApproved.setLayoutManager(linearLayoutManager);


        getApprovedOrder();

    }

    private void initializeUnapproved() {
        recyclerViewUnApproved = (RecyclerView) findViewById(R.id.order_unapproved_recyclerview);
        recyclerViewUnApproved.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewUnApproved.setLayoutManager(linearLayoutManager);

        getUnApprovedOrder();

    }

    private void getApprovedOrder() {
        refApproved = Constants.refDatabase.child(emailLogin + "/Order").child("Approved");

        adapterFirebaseApproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderApproved>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderApproved.class,
                refApproved
        ) {
            @Override
            public OrderViewHolderApproved onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                return new OrderViewHolderApproved(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolderApproved viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerViewApproved.setAdapter(adapterFirebaseApproved);
        adapterFirebaseApproved.notifyDataSetChanged();
    }

    private void getUnApprovedOrder() {
        refUnapproved = Constants.refDatabase.child(emailLogin + "/Order").child("UnApproved");

        adapterFirebaseUnapproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderUnapproved>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderUnapproved.class,
                refUnapproved
        ) {
            @Override
            public OrderViewHolderUnapproved onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                return new OrderViewHolderUnapproved(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolderUnapproved viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerViewUnApproved.setAdapter(adapterFirebaseUnapproved);
        adapterFirebaseUnapproved.notifyDataSetChanged();
    }

    private void getDeniedOrder() {
        refDenied = Constants.refDatabase.child(emailLogin + "/Order").child("OutRoute");

        adapterFirebaseDenied = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderDenied>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderDenied.class,
                refDenied
        ) {
            @Override
            public OrderViewHolderDenied onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                return new OrderViewHolderDenied(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolderDenied viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerViewDenied.setAdapter(adapterFirebaseDenied);
        adapterFirebaseDenied.notifyDataSetChanged();
    }

    private void orderEmployeeFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_employee_filter, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Spinner spinYearFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        Spinner spinMonthFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Button btnOk = (Button) dialogView.findViewById(R.id.btn_dialog_filter_employee_ok);
        tvEmployeeName = dialogView.findViewById(R.id.tv_filter_employee_choose);

        tvEmployeeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                employeeListDialog();

            }
        });

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        spinnerYear.setAdapter(spinerAdapterYear);
        spinnerYear.setSelection(0);

        spinYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosenYear = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayMonth = this.getResources().getStringArray(R.array.month_filter_array);
        List<String> arrayListMonth = Arrays.asList(arrayMonth);
        MySpinerAdapter spinerAdapterMonth = new MySpinerAdapter();
        spinerAdapterMonth.addItems(arrayListMonth);
        MaterialSpinner spinnerMonth = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        spinnerMonth.setAdapter(spinerAdapterMonth);
        spinnerMonth.setSelection(0);

        spinMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                choosenMonth = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                choosenEmployee = tvEmployeeName.getText().toString();

                if (choosenYear != null && choosenMonth != null && !choosenEmployee.equals("Chọn nhân viên")) {
                    Intent it = new Intent(getApplicationContext(), FilterEmployeeActivity.class);
                    it.putExtra("ChoosenYear", choosenYear);
                    it.putExtra("ChoosenMonth", choosenMonth);
                    it.putExtra("EmailLogin", emailLogin);
                    it.putExtra("EmployeeName", choosenEmployee);
                    startActivity(it);
                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn đủ các yếu tố", Toast.LENGTH_LONG).show();

                }

            }
        });

    }

    private void orderFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_filter_order, null);
        builder.setView(dialogView);

        Spinner spinYearFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Spinner spinMonthFilter = (Spinner) dialogView.findViewById(R.id.spinner_filter_month);
        Button btnOkFilter = (Button) dialogView.findViewById(R.id.btn_filter_order_ok);

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        spinnerYear.setAdapter(spinerAdapterYear);
        spinnerYear.setSelection(0);

        spinYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenYear = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayMonth = this.getResources().getStringArray(R.array.month_filter_array);
        List<String> arrayListMonth = Arrays.asList(arrayMonth);
        MySpinerAdapter spinerAdapterMonth = new MySpinerAdapter();
        spinerAdapterMonth.addItems(arrayListMonth);
        MaterialSpinner spinnerMonth = (MaterialSpinner) dialogView.findViewById(R.id.spinner_filter_month);
        spinnerMonth.setAdapter(spinerAdapterMonth);
        spinnerMonth.setSelection(0);

        spinMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenMonth = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOkFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                if (choosenYear != null && choosenMonth != null) {
                    Intent intent = new Intent(getApplicationContext(), FilterTimeActivity.class);
                    intent.putExtra("ChoosenYear", choosenYear);
                    intent.putExtra("ChoosenMonth", choosenMonth);
                    intent.putExtra("EmailLogin", emailLogin);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn đủ các yếu tố", Toast.LENGTH_LONG).show();
                }

            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void orderProductFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_filter_product, null);
        builder.setView(dialogView);

        Spinner spinYearFilter = (Spinner) dialogView.findViewById(R.id.spinner_filter_product_year);
        Spinner spinMonthFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Button btnOkFilter = (Button) dialogView.findViewById(R.id.btn_filter_product_ok);
        tvChooseProduct = dialogView.findViewById(R.id.tv_filter_product_choose);

        tvChooseProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                productListDialog();
            }
        });

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner) dialogView.findViewById(R.id.spinner_filter_product_year);
        spinnerYear.setAdapter(spinerAdapterYear);
        spinnerYear.setSelection(0);

        spinYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenYear = (String) parent.getItemAtPosition(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayMonth = this.getResources().getStringArray(R.array.month_filter_array);
        List<String> arrayListMonth = Arrays.asList(arrayMonth);
        MySpinerAdapter spinerAdapterMonth = new MySpinerAdapter();
        spinerAdapterMonth.addItems(arrayListMonth);
        MaterialSpinner spinnerMonth = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        spinnerMonth.setAdapter(spinerAdapterMonth);
        spinnerMonth.setSelection(0);

        spinMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenMonth = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOkFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                choosenProduct = tvChooseProduct.getText().toString();
                if (choosenYear != null && choosenMonth != null && !choosenProduct.equals("Chọn sản phẩm")) {
                    Intent intent = new Intent(getApplicationContext(), FilterProductActivity.class);
                    intent.putExtra("ChoosenProduct", choosenProduct);
                    intent.putExtra("ChoosenYear", choosenYear);
                    intent.putExtra("ChoosenMonth", choosenMonth);
                    intent.putExtra("EmailLogin", emailLogin);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn đủ các yếu tố", Toast.LENGTH_LONG).show();
                }

            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void orderClientFilterDialog(final String clientCode) {

        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_filter_client, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Spinner spinYearFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        Spinner spinMonthFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Button btnOk = (Button) dialogView.findViewById(R.id.btn_dialog_filter_client_ok);

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        spinnerYear.setAdapter(spinerAdapterYear);
        spinnerYear.setSelection(0);

        spinYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenYear = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayMonth = this.getResources().getStringArray(R.array.month_filter_array);
        List<String> arrayListMonth = Arrays.asList(arrayMonth);
        MySpinerAdapter spinerAdapterMonth = new MySpinerAdapter();
        spinerAdapterMonth.addItems(arrayListMonth);
        MaterialSpinner spinnerMonth = (MaterialSpinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        spinnerMonth.setAdapter(spinerAdapterMonth);
        spinnerMonth.setSelection(0);

        spinMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) choosenMonth = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                if (choosenYear != null && choosenMonth != null) {
                    Intent it = new Intent(getApplicationContext(), FilterClientActivity.class);
                    it.putExtra("ChoosenYear", choosenYear);
                    it.putExtra("ChoosenMonth", choosenMonth);
                    it.putExtra("EmailLogin", emailLogin);
                    it.putExtra("ClientCode", clientCode);
                    it.putExtra("SaleMan", true);
                    startActivity(it);

                } else {
                    Toast.makeText(getApplicationContext(), "Vui lòng chọn đủ các yếu tố", Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void productListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_list, null);
        builder.setView(dialogView);
        builder.setMessage("Chọn sản phẩm (trượt dọc để xem tiếp)");

        dialogProductList = builder.create();

        Button btnNew = dialogView.findViewById(R.id.btn_product_list_new);

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                addNewProductDialog();
            }
        });

        final RecyclerView productList = (RecyclerView) dialogView.findViewById(R.id.recycler_dialog_product_list);
        productList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productList.setLayoutManager(linearLayoutManager);

        adapterFirebaseProduct = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product_pos,
                ProductViewHolder.class,
                refDatabase.child(emailLogin + "/Product")
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_pos, parent, false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());

            }
        };

        productList.setAdapter(adapterFirebaseProduct);
        adapterFirebaseProduct.notifyDataSetChanged();

        dialogProductList.show();
    }

    private void addNewProductDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_new_product, null);
        builder.setView(dialogView);
        builder.setMessage("Thêm sản phẩm mới");

        final Dialog dialogProduct = builder.create();
        dialogProduct.show();

        final EditText edtAddProduct = (EditText) dialogView.findViewById(R.id.edt_dialog_add_product_name);
        final EditText edtStorage = (EditText) dialogView.findViewById(R.id.edt_new_product_storage);

        Button btnAdd = (Button) dialogView.findViewById(R.id.btn_dialog_add_product_ok);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                final String productName = edtAddProduct.getText().toString();
                final String productStorage = edtStorage.getText().toString();

                if (TextUtils.isEmpty(productName)) {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập tên Sản phẩm", Toast.LENGTH_LONG).show();

                } else if (TextUtils.isEmpty(productName)) {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập lượng tồn kho ban đầu", Toast.LENGTH_LONG).show();

                } else {
                    Product product = new Product(productName, productStorage);
                    Product newProduct = new Product(productName);
                    refDatabase.child(emailLogin + "/Product").child(productName).setValue(newProduct);
                    refDatabase.child(emailLogin).child("WarehouseMan/StorageMan").child(productName).setValue(product);
                    dialogProduct.dismiss();
                }


            }
        });

    }

    private void employeeListDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_employee_list, null);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setMessage("Chọn nhân viên (trượt dọc để xem tiếp)");

        dialogEmployeeList = dialogBuilder.create();
        dialogEmployeeList.show();

        Button btnCreate = dialogView.findViewById(R.id.btn_employee_list_new);
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
                addEmployeeDialog();

            }
        });

        RecyclerView employeeList = (RecyclerView) dialogView.findViewById(R.id.recycler_dialog_employee_list);
        employeeList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        employeeList.setLayoutManager(linearLayoutManager);

        adapterFirebaseEmployee = new FirebaseRecyclerAdapter<Employee, EmployeeViewHolder>(
                Employee.class,
                R.id.item_client,
                EmployeeViewHolder.class,
                refDatabase.child(emailLogin).child("Employee")
        ) {
            @Override
            public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);
                return new EmployeeViewHolder(v);
            }


            @Override
            protected void populateViewHolder(EmployeeViewHolder viewHolder, Employee model, int position) {
                viewHolder.employeeName.setText(model.getEmployeeName());

            }
        };

        employeeList.setAdapter(adapterFirebaseEmployee);
        adapterFirebaseEmployee.notifyDataSetChanged();

    }

    private void addEmployeeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_employee, null);
        builder.setView(dialogView);
        builder.setMessage("Thêm nhân viên mới");

        final Dialog dialog = builder.create();
        dialog.show();

        final EditText edtAddEmployee = (EditText) dialogView.findViewById(R.id.edt_dialog_add_employee_name);
        Button btnAdd = (Button) dialogView.findViewById(R.id.btn_dialog_add_employee_ok);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                final String employeeName = edtAddEmployee.getText().toString();

                if (TextUtils.isEmpty(employeeName)) {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập tên Nhân viên", Toast.LENGTH_LONG).show();

                } else {
                    Employee employee = new Employee(employeeName);
                    refDatabase.child(emailLogin + "/Employee").push().setValue(employee);

                    dialog.dismiss();
                }


            }
        });

    }


    public class OrderViewHolderUnapproved extends RecyclerView.ViewHolder {
        TextView orderName;

        public OrderViewHolderUnapproved(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    final int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseUnapproved.getRef(position);
                    final String itemKeyString = itemKey.getKey();

                    refDatabase.child(emailLogin).child("OrderList").child(itemKeyString).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                            assert orderDetail != null;
                            String clientCode = orderDetail.getClientCode();
                            Intent intent1 = new Intent(getApplicationContext(), PreviewOrderActivivity.class);
                            intent1.putExtra("OrderPushKey", itemKeyString);
                            intent1.putExtra("EmailLogin", emailLogin);
                            intent1.putExtra("ClientCode",clientCode);
                            intent1.putExtra("ViewOnly",true);
                            startActivity(intent1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }
            });


        }
    }

    public class OrderViewHolderApproved extends RecyclerView.ViewHolder {
        TextView orderName;
        ImageButton ibPrint;

        public OrderViewHolderApproved(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);
            ibPrint = (ImageButton) itemView.findViewById(R.id.ib_item_order);

            ibPrint.setVisibility(View.VISIBLE);

            ibPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseApproved.getRef(position);
                    final String itemKeyString = itemKey.getKey();
                    Constants.refDatabase.child(emailLogin + "/OrderList").child(itemKeyString).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                            String orderName = orderDetail.getOrderName();
                            Intent it = new Intent(getApplicationContext(), PrintPreviewActivity.class);
                            it.putExtra("EmailLogin", emailLogin);
                            it.putExtra("OrderPushKey", itemKeyString);
                            it.putExtra("OrderName", orderName);
                            startActivity(it);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseApproved.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    Intent intent1 = new Intent(getApplicationContext(), ViewOrderDetailActivity.class);
                    intent1.putExtra("OrderPushKey", itemKeyString);
                    intent1.putExtra("EmailLogin", emailLogin);
                    startActivity(intent1);
                }
            });

        }
    }

    public class OrderViewHolderDenied extends RecyclerView.ViewHolder {
        TextView orderName;

        public OrderViewHolderDenied(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseDenied.getRef(position);
                    final String itemKeyString = itemKey.getKey();

                    refDatabase.child(emailLogin).child("OrderList").child(itemKeyString).child("OtherInformation").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                            assert orderDetail != null;
                            String clientCode = orderDetail.getClientCode();
                            Intent intent1 = new Intent(getApplicationContext(), PreviewOrderActivivity.class);
                            intent1.putExtra("OrderPushKey", itemKeyString);
                            intent1.putExtra("EmailLogin", emailLogin);
                            intent1.putExtra("ClientCode",clientCode);
                            intent1.putExtra("ViewOnly",true);
                            startActivity(intent1);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

        }
    }

    public class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView employeeName;

        public EmployeeViewHolder(View itemView) {
            super(itemView);
            employeeName = (TextView) itemView.findViewById(R.id.tv_item_client_sale_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    if (dialogEmployeeList != null) dialogEmployeeList.dismiss();
                    int position = getLayoutPosition();
                    Employee employee = adapterFirebaseEmployee.getItem(position);
                    tvEmployeeName.setText(employee.getEmployeeName());

                }
            });

        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.tv_item_product_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    if (dialogProductList != null) dialogProductList.dismiss();
                    int position = getLayoutPosition();
                    DatabaseReference keyRef = adapterFirebaseProduct.getRef(position);
                    String keyString = keyRef.getKey();

                    final Product p = adapterFirebaseProduct.getItem(position);

                    tvChooseProduct.setText(p.getProductName());
                }
            });

        }
    }


    private void dialogGroupList(View dialogView) {
        final RecyclerView rvClientGroup = dialogView.findViewById(R.id.rv_client_sale_group);
        rvClientGroup.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager1 = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvClientGroup.setLayoutManager(linearLayoutManager1);

        adapterFirebaseClientGroup
                = new FirebaseRecyclerAdapter<Group, GroupViewHolder>(
                Group.class,
                R.layout.item_group,
                GroupViewHolder.class,
                refDatabase.child(emailLogin).child("ClientManBySale").child(employeeEmail).child("Group")
        ) {
            @Override
            public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
                return new GroupViewHolder(v);
            }

            @Override
            protected void populateViewHolder(final GroupViewHolder viewHolder, Group model, int position) {
                viewHolder.groupName.setText(model.getGroupName());

            }
        };

        rvClientGroup.setAdapter(adapterFirebaseClientGroup);
        adapterFirebaseClientGroup.notifyDataSetChanged();

    }

    private void dialogClientList(DatabaseReference ref) {

        adapterFirebaseClient = new FirebaseRecyclerAdapter<Client, ClientViewHolder>(
                Client.class,
                R.layout.item_client_sale,
                ClientViewHolder.class,
                ref
        ) {
            @Override
            public ClientViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_sale, parent, false);
                return new ClientViewHolder(v);
            }

            @Override
            protected void populateViewHolder(ClientViewHolder viewHolder, Client model, int position) {
                viewHolder.clientName.setText(model.getClientName());
                viewHolder.clientName.setTextColor(getResources().getColor(R.color.colorAccent));
                viewHolder.clientAddress.setText(model.getClientAddress());

            }
        };

        rvClientList.setAdapter(adapterFirebaseClient);
        adapterFirebaseClient.notifyDataSetChanged();

    }

    public class ClientViewHolder extends RecyclerView.ViewHolder {
        TextView clientName, clientAddress, clientMeet, clientSale, clientGroup, clientEdit;

        public ClientViewHolder(View itemView) {
            super(itemView);
            clientName = (TextView) itemView.findViewById(R.id.tv_item_client_sale_name);
            clientAddress = itemView.findViewById(R.id.tv_item_client_sale_address);
            clientMeet = itemView.findViewById(R.id.tv_client_sale_meet);
            clientSale = itemView.findViewById(R.id.tv_client_sale_view);
            clientGroup = itemView.findViewById(R.id.tv_client_sale_group);
            clientEdit = itemView.findViewById(R.id.tv_client_sale_edit);

            clientMeet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                    builder.setMessage("Bạn muốn xác nhận đang có mặt tại nơi của khách hàng?");
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            final String timeStamp = Calendar.getInstance().getTime().getTime() + "";
                            int position = getLayoutPosition();
                            final Client client = adapterFirebaseClient.getItem(position);
                            final String clientCode = client.getClientCode();
                            final Client clientMeet = new Client(clientCode,client.getClientName(),client.getClientType(),client.getClientAddress(),timeStamp);

                            refDatabase.child(emailLogin).child("Client").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChild("map")) {
                                        refDatabase.child(emailLogin).child("Client").child(clientCode).child("map").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                MapModel map = dataSnapshot.getValue(MapModel.class);

                                                SimpleLocation.Point clientPoint = new SimpleLocation.Point(Double.parseDouble(map.getLatitude())
                                                        , Double.parseDouble(map.getLongitude()));

                                                SimpleLocation.Point salesPoint = new SimpleLocation.Point(latitude, longitude);
                                                double currentDistance = SimpleLocation.calculateDistance(clientPoint, salesPoint);

                                                if (currentDistance > 30) {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                                                    builder.setMessage("Bạn đang ở quá xa vị trí của khách hàng!");
                                                } else {
                                                    refDatabase.child(emailLogin).child("SalesManagement/Meet").child(employeeEmail)
                                                            .child(timeStamp).setValue(clientMeet);
                                                    Toast.makeText(getApplicationContext(), "Xác nhận thăm viếng khách hàng thành công!",
                                                            Toast.LENGTH_LONG).show();
                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Không thể xác nhận do thiếu dữ liệu khách hàng!",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }).show();

                }
            });

            clientSale.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    int position = getLayoutPosition();
                    final Client client = adapterFirebaseClient.getItem(position);
                    final String clientCode = client.getClientCode();

                    orderClientFilterDialog(clientCode);
                }
            });

            clientGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    int position = getLayoutPosition();
                    final Client client = adapterFirebaseClient.getItem(position);
                    final String clientCode = client.getClientCode();

                    AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_choose_group, null);
                    builder.setView(dialogView);
                    builder.setMessage("Chọn nhóm cho KH: " + "\n" + clientName);

                    final Dialog dialog = builder.create();
                    dialog.show();

                    final RecyclerView rvClientGroup = dialogView.findViewById(R.id.rv_choose_group);
                    rvClientGroup.setHasFixedSize(true);
                    StaggeredGridLayoutManager linearLayoutManager1 = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.HORIZONTAL);
                    rvClientGroup.setLayoutManager(linearLayoutManager1);

                    adapterFirebaseClientGroup
                            = new FirebaseRecyclerAdapter<Group, GroupViewHolder>(
                            Group.class,
                            R.layout.item_group,
                            GroupViewHolder.class,
                            refDatabase.child(emailLogin).child("ClientManBySale").child(employeeEmail).child("Group")
                    ) {
                        @Override
                        public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group, parent, false);
                            return new GroupViewHolder(v);
                        }

                        @Override
                        protected void populateViewHolder(final GroupViewHolder viewHolder, Group model, int position) {
                            viewHolder.groupName.setText(model.getGroupName());
                            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    dialog.dismiss();
                                    view.startAnimation(buttonClick);

                                    int position = viewHolder.getAdapterPosition();
                                    final Group group = adapterFirebaseClientGroup.getItem(position);
                                    final String groupName = group.getGroupName();

                                    if (groupName.equals("Tất cả")) {
                                        Toast.makeText(getApplicationContext(), "Không thể xếp vào nhóm này!", Toast.LENGTH_LONG).show();
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                                        builder.setTitle("Nhóm của KH này là: " + groupName + "?");

                                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                refDatabase.child(emailLogin).child("ClientManBySale")
                                                        .child(employeeEmail).child(groupName).child(clientCode).setValue(client);
                                            }
                                        }).show();
                                    }


                                }
                            });

                        }
                    };

                    rvClientGroup.setAdapter(adapterFirebaseClientGroup);
                    adapterFirebaseClientGroup.notifyDataSetChanged();

                }
            });

            clientEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    int position = getLayoutPosition();
                    final Client client = adapterFirebaseClient.getItem(position);
                    String clientCode = client.getClientCode();

                    refDatabase.child(emailLogin).child("Client").child(clientCode).child("clientPhone").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String clientPhone = dataSnapshot.getValue().toString();
                            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + clientPhone));
                            startActivity(intent);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });

        }
    }

    private class GroupViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;

        GroupViewHolder(View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.tv_item_group_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(buttonClick);

                    int position = getAdapterPosition();
                    String groupName = adapterFirebaseClientGroup.getItem(position).getGroupName();
                    if (groupName.equals("Tất cả")) {
                        DatabaseReference ref = refDatabase.child(emailLogin)
                                .child("ClientBySale").child(employeeEmail);
                        dialogClientList(ref);
                    } else {
                        DatabaseReference ref = refDatabase.child(emailLogin)
                                .child("ClientManBySale").child(employeeEmail).child(groupName);
                        dialogClientList(ref);
                    }

                }


            });

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    view.startAnimation(buttonClick);

                    int position = getAdapterPosition();
                    final String groupKey = adapterFirebaseClientGroup.getRef(position).getKey();
                    String groupName = adapterFirebaseClientGroup.getItem(position).getGroupName();

                    if (groupName.equals("Tất cả")) {
                        Toast.makeText(getApplicationContext(), "Bạn không thể tạo/xóa nhóm này!", Toast.LENGTH_LONG).show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(OrderManActivity.this);
                        builder.setTitle("Tạo/Đổi tên nhóm khách hàng");

                        final EditText edtName = new EditText(OrderManActivity.this);
                        edtName.setHeight(80);
                        edtName.setWidth(200);
                        edtName.setGravity(Gravity.CENTER);
                        edtName.setImeOptions(EditorInfo.IME_ACTION_DONE);
                        edtName.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                        edtName.setHint("Nhập tên nhóm");

                        builder.setView(edtName);

                        builder.setPositiveButton("Tạo", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = edtName.getText().toString();
                                if (TextUtils.isEmpty(name)) {
                                    Toast.makeText(getApplicationContext(), "Vui lòng nhập tên nhóm", Toast.LENGTH_LONG).show();
                                } else {
                                    Group group = new Group(name);
                                    refDatabase.child(emailLogin).child("ClientManBySale").child(employeeEmail).child("Group").push().setValue(group);
                                }
                            }
                        }).setNegativeButton("Đổi tên", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = edtName.getText().toString();
                                if (TextUtils.isEmpty(name)) {
                                    Toast.makeText(getApplicationContext(), "Vui lòng nhập tên nhóm", Toast.LENGTH_LONG).show();
                                } else {

                                    Group group = new Group(name);
                                    refDatabase.child(emailLogin).child("ClientManBySale").child(employeeEmail).child("Group").child(groupKey).setValue(group);
                                }

                            }
                        }).show();
                    }

                    return false;
                }
            });


        }
    }

    @Override
    public void onBackPressed() {
        b.clear();
        startActivity(new Intent(this,MainActivity.class));
        super.onBackPressed();
    }


}

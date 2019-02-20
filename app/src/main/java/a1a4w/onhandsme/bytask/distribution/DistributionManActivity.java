package a1a4w.onhandsme.bytask.distribution;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import a1a4w.onhandsme.LoginActivity;
import a1a4w.onhandsme.MainActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.list.ClientListActivity;
import a1a4w.onhandsme.model.Employee;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.order.ViewOrderDetailActivity;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.MySpinerAdapter;

import java.util.Arrays;
import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;

import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;
import static a1a4w.onhandsme.utils.Constants.refLogin;
import static a1a4w.onhandsme.utils.Constants.refRole;

public class DistributionManActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUnApproved, recyclerViewApproved, recyclerViewDenied;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolderUnapproved> adapterFirebaseUnapproved;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolderApproved> adapterFirebaseApproved;
    private FirebaseRecyclerAdapter<OrderDetail,OrderViewHolderDenied> adapterFirebaseDenied;

    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct;
    private FirebaseRecyclerAdapter<Employee,EmployeeViewHolder> adapterFirebaseEmployee;
    private LinearLayoutManager linearLayoutManager;
    private LinearLayout layoutApproved, layoutUnapproved, layoutDenied,boxApproved, boxUnApproved,boxCancelled;
    private DatabaseReference refApproved, refUnapproved, refDenied;
    private TextView tvUnapproved, tvApproved, tvDenied;
    private String userRole, emailLogin,choosenYear,choosenMonth,choosenEmployee,choosenProduct;
    private Bundle b = new Bundle();
    private TextView tvEmployeeName,tvChooseProduct;
    private Dialog dialogProductList,dialogEmployeeList;
    private String saleEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distribution_man);

        Intent intent = this.getIntent();
        userRole = intent.getStringExtra("UserRole");
        emailLogin = intent.getStringExtra("EmailLogin");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_distribution);
        setSupportActionBar(toolbar);

        tvUnapproved = (TextView) findViewById(R.id.tv_distribution_unapproved);

        tvApproved = (TextView) findViewById(R.id.tv_distribution_approved);
        tvDenied = (TextView) findViewById(R.id.tv_distribution_denied);
        layoutApproved = (LinearLayout)findViewById(R.id.layout_approved_distribution);
        layoutUnapproved = (LinearLayout)findViewById(R.id.layout_unapproved_distribution);
        layoutDenied = (LinearLayout)findViewById(R.id.layout_denied_distribution);
        boxApproved = (LinearLayout)findViewById(R.id.distribution_man_boxApproved);
        boxUnApproved = (LinearLayout)findViewById(R.id.distribution_man_boxUnApproved);
        boxCancelled = (LinearLayout)findViewById(R.id.distribution_man_boxCanceled);

        boxUnApproved.setBackgroundColor(Color.WHITE);
        tvUnapproved.setTextColor(getResources().getColor(R.color.colorAccent));

        initializeUnapproved();
        initializeApproved();
        initializeDenied();

        boxApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
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
                v.startAnimation(Constants.buttonClick);
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
                v.startAnimation(Constants.buttonClick);
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
    }

    private void initializeDenied() {
        recyclerViewDenied = (RecyclerView)findViewById(R.id.order_denied_recyclerview_distribution);
        recyclerViewDenied.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewDenied.setLayoutManager(linearLayoutManager);

        getDeniedOrder();


    }
    private void initializeApproved() {
        recyclerViewApproved = (RecyclerView)findViewById(R.id.order_approved_recyclerview_distribution);
        recyclerViewApproved.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewApproved.setLayoutManager(linearLayoutManager);

        getApprovedOrder();

    }
    private void initializeUnapproved() {
        recyclerViewUnApproved = (RecyclerView)findViewById(R.id.order_unapproved_recyclerview_distribution);
        recyclerViewUnApproved.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewUnApproved.setLayoutManager(linearLayoutManager);

        getUnApprovedOrder();

    }

    private void getApprovedOrder() {
        refApproved = Constants.refDatabase.child(emailLogin+"/Order").child("Approved");

        adapterFirebaseApproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderApproved>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderApproved.class,
                refApproved
        ) {
            @Override
            public OrderViewHolderApproved onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order,parent,false);
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
        refUnapproved = Constants.refDatabase.child(emailLogin+"/Order").child("UnApproved");

        adapterFirebaseUnapproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderUnapproved>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderUnapproved.class,
                refUnapproved
        ) {
            @Override
            public OrderViewHolderUnapproved onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order,parent,false);
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
        refDenied = Constants.refDatabase.child(emailLogin+"/Order").child("DeniedWarehouse");

        adapterFirebaseDenied = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderDenied>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderDenied.class,
                refDenied
        ) {
            @Override
            public OrderViewHolderDenied onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order,parent,false);
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

    private class OrderViewHolderUnapproved extends RecyclerView.ViewHolder {
        TextView orderName;

        OrderViewHolderUnapproved(View itemView) {
            super(itemView);
            orderName = (TextView)itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseUnapproved.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    //Intent intent1 = new Intent(getApplicationContext(),ApproveOrderActivity.class);
                    Intent intent1 = new Intent(getApplicationContext(),ApproveOrderActivity.class);
                    intent1.putExtra("OrderPushKey",itemKeyString);
                    intent1.putExtra("EmailLogin",emailLogin);
                    intent1.putExtra("Normal",true);

                    startActivity(intent1);
                }
            });


        }
    }
    private class OrderViewHolderApproved extends RecyclerView.ViewHolder {
        TextView orderName;

        public OrderViewHolderApproved(View itemView) {
            super(itemView);
            orderName = (TextView)itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseApproved.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    Intent intent1 = new Intent(getApplicationContext(),ViewOrderDetailActivity.class);
                    intent1.putExtra("OrderPushKey",itemKeyString);
                    intent1.putExtra("EmailLogin",emailLogin);
                    startActivity(intent1);
                }
            });


        }
    }
    private class OrderViewHolderDenied extends RecyclerView.ViewHolder {
        TextView orderName;

        OrderViewHolderDenied(View itemView) {
            super(itemView);
            orderName = (TextView)itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemKey = adapterFirebaseDenied.getRef(position);
                    String itemKeyString = itemKey.getKey();
                    Intent intent1 = new Intent(getApplicationContext(),ApproveOrderActivity.class);
                    intent1.putExtra("OrderPushKey",itemKeyString);
                    intent1.putExtra("EmailLogin",emailLogin);

                    startActivity(intent1);
                }
            });


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_distribution,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout_distribution) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        if (id == R.id.action_view_order_time) {
            orderFilterDialog();
        }

        if (id == R.id.action_view_order_client) {
            orderClientFilterDialog();
        }

        if (id == R.id.action_view_order_product) {
            orderProductFilterDialog();
        }

        if(id == R.id.action_view_order_employee){
            orderEmployeeFilterDialog(true);
        }

        if(id == R.id.action_create_employee){
            addEmployeeDialog();
        }

        if(id == R.id.action_create_product){
            addNewProductDialog();
        }

        if(id == R.id.action_view_meet_client){
            orderEmployeeFilterDialog(false);
        }
        return super.onOptionsItemSelected(item);
    }

    private void orderEmployeeFilterDialog(final boolean saleReport) {
        AlertDialog.Builder builder= new AlertDialog.Builder(DistributionManActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_employee_filter, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Spinner spinYearFilter = (Spinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        Spinner spinMonthFilter = (Spinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Button btnOk = (Button)dialogView.findViewById(R.id.btn_dialog_filter_employee_ok);
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
        MaterialSpinner spinnerYear = (MaterialSpinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
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
        MaterialSpinner spinnerMonth = (MaterialSpinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
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

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                choosenEmployee = tvEmployeeName.getText().toString();

                if(choosenYear !=null && choosenMonth!=null && !choosenEmployee.equals("Chọn nhân viên")){
                    if(saleReport){
                        Intent it = new Intent(getApplicationContext(),FilterEmployeeActivity.class);
                        it.putExtra("ChoosenYear",choosenYear);
                        it.putExtra("ChoosenMonth",choosenMonth);
                        it.putExtra("EmailLogin",emailLogin);
                        it.putExtra("EmployeeName",choosenEmployee);
                        it.putExtra("SaleEmail",saleEmail);

                        startActivity(it);
                    }else{
                        Intent it = new Intent(getApplicationContext(),MeetClientReportActivity.class);
                        it.putExtra("ChoosenYear",choosenYear);
                        it.putExtra("ChoosenMonth",choosenMonth);
                        it.putExtra("EmailLogin",emailLogin);
                        it.putExtra("EmployeeName",choosenEmployee);
                        it.putExtra("SaleEmail",saleEmail);
                        startActivity(it);
                    }

                }else{
                    Toast.makeText(getApplicationContext(),"Vui lòng chọn đủ các yếu tố",Toast.LENGTH_LONG).show();

                }

            }
        });

    }
    private void orderFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DistributionManActivity.this);

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
                if(position !=0) choosenYear = (String) parent.getItemAtPosition(position);
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
                if(position !=0) choosenMonth = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOkFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                if(choosenYear !=null && choosenMonth!=null){
                    Intent intent = new Intent(getApplicationContext(), FilterTimeActivity.class);
                    intent.putExtra("ChoosenYear", choosenYear);
                    intent.putExtra("ChoosenMonth", choosenMonth);
                    intent.putExtra("EmailLogin",emailLogin);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Vui lòng chọn đủ các yếu tố",Toast.LENGTH_LONG).show();
                }

            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

    }
    private void orderProductFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DistributionManActivity.this);

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
                if(position !=0) choosenYear = (String) parent.getItemAtPosition(position);

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
                if(position !=0) choosenMonth = (String) parent.getItemAtPosition(position);
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
                if(choosenYear !=null && choosenMonth!=null && !choosenProduct.equals("Chọn sản phẩm")){
                    Intent intent = new Intent(getApplicationContext(), FilterProductActivity.class);
                    intent.putExtra("ChoosenProduct", choosenProduct);
                    intent.putExtra("ChoosenYear", choosenYear);
                    intent.putExtra("ChoosenMonth", choosenMonth);
                    intent.putExtra("EmailLogin",emailLogin);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Vui lòng chọn đủ các yếu tố",Toast.LENGTH_LONG).show();
                }

            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

    }
    private void orderClientFilterDialog() {

        AlertDialog.Builder builder= new AlertDialog.Builder(DistributionManActivity.this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_filter_client, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();
        dialog.show();

        Spinner spinYearFilter = (Spinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        Spinner spinMonthFilter = (Spinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Button btnOk = (Button)dialogView.findViewById(R.id.btn_dialog_filter_client_ok);

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_year);
        spinnerYear.setAdapter(spinerAdapterYear);
        spinnerYear.setSelection(0);

        spinYearFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position !=0) choosenYear = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String[] arrayMonth = this.getResources().getStringArray(R.array.month_filter_array);
        List<String> arrayListMonth = Arrays.asList(arrayMonth);
        MySpinerAdapter spinerAdapterMonth = new MySpinerAdapter();
        spinerAdapterMonth.addItems(arrayListMonth);
        MaterialSpinner spinnerMonth = (MaterialSpinner)dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        spinnerMonth.setAdapter(spinerAdapterMonth);
        spinnerMonth.setSelection(0);

        spinMonthFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position !=0) choosenMonth = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                if(choosenYear !=null && choosenMonth!=null){
                    Intent it = new Intent(getApplicationContext(),ClientListActivity.class);
                    it.putExtra("ChoosenYear",choosenYear);
                    it.putExtra("ChoosenMonth",choosenMonth);
                    it.putExtra("ClientFilter",true);
                    it.putExtra("EmailLogin",emailLogin);

                    startActivity(it);

                }else{
                    Toast.makeText(getApplicationContext(),"Vui lòng chọn đủ các yếu tố",Toast.LENGTH_LONG).show();
                }

            }
        });

    }

    private void productListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_list,null);
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

        final RecyclerView productList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_product_list);
        productList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productList.setLayoutManager(linearLayoutManager);

        adapterFirebaseProduct = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product_pos,
                ProductViewHolder.class,
                refDatabase.child(emailLogin+"/Product")
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_pos,parent,false);
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
        View dialogView = inflater.inflate(R.layout.dialog_new_product,null);
        builder.setView(dialogView);
        builder.setMessage("Thêm sản phẩm mới");

        final Dialog dialogProduct = builder.create();
        dialogProduct.show();

        final EditText edtAddProduct = (EditText)dialogView.findViewById(R.id.edt_dialog_add_product_name);
        final EditText edtStorage = (EditText)dialogView.findViewById(R.id.edt_new_product_storage);

        Button btnAdd = (Button) dialogView.findViewById(R.id.btn_dialog_add_product_ok);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                final String productName = edtAddProduct.getText().toString();
                final String productStorage = edtStorage.getText().toString();

                if(TextUtils.isEmpty(productName)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập tên Sản phẩm", Toast.LENGTH_LONG).show();

                }else if(TextUtils.isEmpty(productName)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập lượng tồn kho ban đầu", Toast.LENGTH_LONG).show();

                }else{
                    Product product = new Product(productName,productStorage);
                    Product newProduct = new Product(productName);
                    refDatabase.child(emailLogin+"/Product").child(productName).setValue(newProduct);
                    refDatabase.child(emailLogin).child("WarehouseMan/StorageMan").child(productName).setValue(product);
                    dialogProduct.dismiss();
                }


            }
        });

    }
    private void employeeListDialog() {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_employee_list,null);
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

        RecyclerView employeeList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_employee_list);
        employeeList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        employeeList.setLayoutManager(linearLayoutManager);

        adapterFirebaseEmployee = new FirebaseRecyclerAdapter<Employee, EmployeeViewHolder>(
                Employee.class,
                R.layout.item_client,
                EmployeeViewHolder.class,
                refDatabase.child(emailLogin).child("Employee")
        ) {
            @Override
            public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client,parent,false);
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
        View dialogView = inflater.inflate(R.layout.dialog_add_employee,null);
        builder.setView(dialogView);
        builder.setMessage("Thêm nhân viên mới");

        final Dialog dialog = builder.create();
        dialog.show();

        final EditText edtAddEmployee = (EditText)dialogView.findViewById(R.id.edt_dialog_add_employee_name);
        final EditText edtAddEmployeeEmail = (EditText)dialogView.findViewById(R.id.edt_add_employee_email);
        final EditText edtAddEmployeePass = (EditText)dialogView.findViewById(R.id.edt_add_employee_pass);

        Button btnAdd = (Button) dialogView.findViewById(R.id.btn_dialog_add_employee_ok);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                final String employeeName = edtAddEmployee.getText().toString();
                final String email = edtAddEmployeeEmail.getText().toString();
                final String pass = edtAddEmployeePass.getText().toString();

                if(TextUtils.isEmpty(employeeName)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập tên Nhân viên", Toast.LENGTH_LONG).show();

                }else{
                    refLogin.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(email.replace(".",","))){
                                Toast.makeText(getApplicationContext(),"Email này đã được sử dụng",Toast.LENGTH_LONG).show();
                            }else{
                                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,pass);

                                Employee employee = new Employee(employeeName);
                                refDatabase.child(emailLogin+"/Employee").child(email.replace("."," ")).setValue(employee);
                                refLogin.child(email.replace(".",",")).setValue(emailLogin);
                                refRole.child(emailLogin).child(email.replace(".",",")).setValue("SaleMan");
                                Toast.makeText(getApplicationContext(),"Thêm nhân viên mới hoàn tất!",Toast.LENGTH_LONG).show();

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

    }

    public class EmployeeViewHolder extends RecyclerView.ViewHolder {
        TextView employeeName;

        public EmployeeViewHolder(View itemView) {
            super(itemView);
            employeeName = (TextView) itemView.findViewById(R.id.tv_item_client_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    if(dialogEmployeeList!=null) dialogEmployeeList.dismiss();
                    int position = getLayoutPosition();
                    Employee employee = adapterFirebaseEmployee.getItem(position);
                    saleEmail = adapterFirebaseEmployee.getRef(position).getKey();
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
                    if(dialogProductList!=null) dialogProductList.dismiss();
                    int position = getLayoutPosition();

                    final Product p = adapterFirebaseProduct.getItem(position);

                    tvChooseProduct.setText(p.getProductName());
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

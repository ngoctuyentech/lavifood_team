package a1a4w.onhandsme.pos;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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
import com.google.firebase.database.ValueEventListener;
import a1a4w.onhandsme.LoginActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.model.Shop;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.MySpinerAdapter;
import a1a4w.onhandsme.utils.Utils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import fr.ganfra.materialspinner.MaterialSpinner;

public class ShopChainActivity extends AppCompatActivity {
    ImageView ivShop,ivEmployee,ivSale,ivWarehouse;
    private Bundle b = new Bundle();
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebase;
    private FirebaseRecyclerAdapter<Shop,ShopViewHolder> adapterFirebaseShop;
    private FirebaseRecyclerAdapter<Shop,ShopViewHolder2> adapterFirebaseShopStorage;
    private FirebaseRecyclerAdapter<Shop,ShopViewHolder3> adapterFirebaseShopStorageIn;
    private FirebaseRecyclerAdapter<Product,ProductStorageViewHolder> adapterFirebaseShopProductStorage;
    private FirebaseRecyclerAdapter<Product,ProductStorageViewHolder2> adapterFirebaseShopProductStorageIn;
    private long timeStamp;
    private LinearLayoutManager linearLayoutManager;
    private String emailLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_chain);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_storeman);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

        ivShop = (ImageView)findViewById(R.id.iv_storeman_shop);
        ivEmployee = (ImageView)findViewById(R.id.iv_storeman_employee);
        ivSale = (ImageView)findViewById(R.id.iv_storeman_sale);
        ivWarehouse = (ImageView)findViewById(R.id.iv_storeman_warehouse);


        ivShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                Intent it = new Intent(getApplicationContext(),StoreShopActivity.class);
                it.putExtra("EmailLogin",emailLogin);
                startActivity(it);
            }
        });

        ivEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                Intent it = new Intent(getApplicationContext(),StoreEmployeeActivity.class);
                it.putExtra("EmailLogin",emailLogin);
                startActivity(it);
            }
        });

        ivSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                //startActivity(new Intent(getApplicationContext(),StoreSaleActivity.class));
                chooseSaleTypeDialog();
            }
        });

        ivWarehouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                chooseStoreActionDialog();
                //startActivity(new Intent(getApplicationContext(),StoreWarehouseActivity.class));
            }
        });

    }

    private void chooseStoreActionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_storeman_store,null);
        builder.setView(dialogView);
        builder.show();

        Button btnViewStore = (Button)dialogView.findViewById(R.id.btn_dialog_storeman_store_view);
        Button btnStoreIn = (Button)dialogView.findViewById(R.id.btn_dialog_storeman_store_in);

        btnViewStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                storeListStorageDialog();

            }
        });

        btnStoreIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                storeListStorageInDialog();

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chain_man,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_logout_storeman){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this,LoginActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    private void chooseSaleTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_store_sale,null);
        builder.setView(dialogView);
        //final Dialog dialog = builder.create();
        //dialog.show();
        builder.show();

        Button btnSaleTotal = (Button)dialogView.findViewById(R.id.btn_dialog_chain_saleTotal);
        Button btnSaleStore = (Button)dialogView.findViewById(R.id.btn_dialog_chain_saleStore);
        Button btnSaleProduct = (Button)dialogView.findViewById(R.id.btn__dialog_chain_saleProduct);

        btnSaleTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                timeFilterDialog();
            }
        });

        btnSaleProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                productListDialog();

            }
        });

        btnSaleStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                storeListDialog();
            }
        });


    }

    private void storeListStorageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_shop_list,null);
        builder.setView(dialogView);

        final RecyclerView shopList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_shop_list);
        //progressBar = (ProgressBar)dialogView.findViewById(R.id.pB_dialog_shop_list);
        //   progressBar.setVisibility(View.VISIBLE);
        shopList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        shopList.setLayoutManager(linearLayoutManager);

        adapterFirebaseShopStorage = new FirebaseRecyclerAdapter<Shop, ShopViewHolder2>(
                Shop.class,
                R.id.item_shop,
                ShopViewHolder2.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_Shop")
        ) {
            @Override
            public ShopViewHolder2 onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop,parent,false);
                return new ShopViewHolder2(v);
            }


            @Override
            protected void populateViewHolder(ShopViewHolder2 viewHolder, Shop model, int position) {
                viewHolder.shopName.setText(model.getShopName());
                viewHolder.shopAddress.setText(model.getShopAddress());
                Glide.with(getApplicationContext()).load(model.getShopUrl()).error(R.drawable.storefront).fitCenter().override(300,200).into(viewHolder.shopPic);


            }
        };

        shopList.setAdapter(adapterFirebaseShopStorage);
        adapterFirebaseShopStorage.notifyDataSetChanged();

        builder.show();
    }
    private void storeListStorageInDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_shop_list,null);
        builder.setView(dialogView);

        final RecyclerView shopList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_shop_list);
        //progressBar = (ProgressBar)dialogView.findViewById(R.id.pB_dialog_shop_list);
        //   progressBar.setVisibility(View.VISIBLE);
        shopList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        shopList.setLayoutManager(linearLayoutManager);

        adapterFirebaseShopStorageIn = new FirebaseRecyclerAdapter<Shop, ShopViewHolder3>(
                Shop.class,
                R.id.item_shop,
                ShopViewHolder3.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_Shop")
        ) {
            @Override
            public ShopViewHolder3 onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop,parent,false);
                return new ShopViewHolder3(v);
            }


            @Override
            protected void populateViewHolder(ShopViewHolder3 viewHolder, Shop model, int position) {
                viewHolder.shopName.setText(model.getShopName());
                viewHolder.shopAddress.setText(model.getShopAddress());
                Glide.with(getApplicationContext()).load(model.getShopUrl()).error(R.drawable.storefront).fitCenter().override(300,200).into(viewHolder.shopPic);


            }
        };

        shopList.setAdapter(adapterFirebaseShopStorageIn);
        adapterFirebaseShopStorageIn.notifyDataSetChanged();

        builder.show();
    }

    private void storeListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_shop_list,null);
        builder.setView(dialogView);

        final RecyclerView shopList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_shop_list);
        //progressBar = (ProgressBar)dialogView.findViewById(R.id.pB_dialog_shop_list);
        //   progressBar.setVisibility(View.VISIBLE);
        shopList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        shopList.setLayoutManager(linearLayoutManager);

        adapterFirebaseShop = new FirebaseRecyclerAdapter<Shop, ShopViewHolder>(
                Shop.class,
                R.id.item_shop,
                ShopViewHolder.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_Shop")
        ) {
            @Override
            public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop,parent,false);
                return new ShopViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ShopViewHolder viewHolder, Shop model, int position) {
                viewHolder.shopName.setText(model.getShopName());
                viewHolder.shopAddress.setText(model.getShopAddress());
                Glide.with(getApplicationContext()).load(model.getShopUrl()).error(R.drawable.storefront).fitCenter().override(300,200).into(viewHolder.shopPic);


            }
        };

        shopList.setAdapter(adapterFirebaseShop);
        adapterFirebaseShop.notifyDataSetChanged();

        builder.show();
    }
    private void productListDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_product_list_pos,null);
        builder.setView(dialogView);


        final RecyclerView productList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_product_list_pos);
        //progressBar = (ProgressBar)dialogView.findViewById(R.id.pB_dialog_shop_list);
        //   progressBar.setVisibility(View.VISIBLE);
        productList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productList.setLayoutManager(linearLayoutManager);

        adapterFirebase = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product_pos,
                ProductViewHolder.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_Product")
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

        productList.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();

        builder.show();
    }

    private void productFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_filter_time_pos, null);
        builder.setView(dialogView);

        Spinner spinYearFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Spinner spinMonthFilter = (Spinner) dialogView.findViewById(R.id.spinner_filter_month);
        final EditText edtDate = (EditText)dialogView.findViewById(R.id.edt_dialog_filter_time_pos_date);
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
                final String choosenYear = (String) parent.getItemAtPosition(position);
                b.putString("ChoosenYear", choosenYear);

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
                final String choosenMonth = (String) parent.getItemAtPosition(position);
                b.putString("ChoosenMonth", choosenMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOkFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                String date = edtDate.getText().toString();
                String choosenYear = b.getString("ChoosenYear");
                String choosenMonth = b.getString("ChoosenMonth");
                String productName = b.getString("ProductName");
                if(TextUtils.isEmpty(date)){
                    Toast.makeText(getApplicationContext(),"Vui lòng chọn ngày",Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(getApplicationContext(), StoreSaleActivity.class);
                    intent.putExtra("ChoosenYear", choosenYear);
                    intent.putExtra("ChoosenMonth", choosenMonth);
                    intent.putExtra("ChoosenDate",date);
                    intent.putExtra("ProductName",productName);
                    intent.putExtra("ProductSale",true);
                    intent.putExtra("EmailLogin",emailLogin);

                    startActivity(intent);
                }


            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void timeFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_filter_time_pos, null);
        builder.setView(dialogView);

        Spinner spinYearFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Spinner spinMonthFilter = (Spinner) dialogView.findViewById(R.id.spinner_filter_month);
        final EditText edtDate = (EditText)dialogView.findViewById(R.id.edt_dialog_filter_time_pos_date);
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
                final String choosenYear = (String) parent.getItemAtPosition(position);
                b.putString("ChoosenYear", choosenYear);

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
                final String choosenMonth = (String) parent.getItemAtPosition(position);
                b.putString("ChoosenMonth", choosenMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOkFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                String date = edtDate.getText().toString();
                String choosenYear = b.getString("ChoosenYear");
                String choosenMonth = b.getString("ChoosenMonth");
                if(TextUtils.isEmpty(date)){
                    Toast.makeText(getApplicationContext(),"Vui lòng chọn ngày",Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(getApplicationContext(), StoreSaleActivity.class);
                    intent.putExtra("ChoosenYear", choosenYear);
                    intent.putExtra("ChoosenMonth", choosenMonth);
                    intent.putExtra("ChoosenDate",date);
                    intent.putExtra("TimeSale",true);
                    startActivity(intent);
                }


            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();

    }
    private void shopFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_filter_time_pos, null);
        builder.setView(dialogView);

        Spinner spinYearFilter = (Spinner) dialogView.findViewById(R.id.spinner_dialog_filter_employee_month);
        Spinner spinMonthFilter = (Spinner) dialogView.findViewById(R.id.spinner_filter_month);
        final EditText edtDate = (EditText)dialogView.findViewById(R.id.edt_dialog_filter_time_pos_date);
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
                final String choosenYear = (String) parent.getItemAtPosition(position);
                b.putString("ChoosenYear", choosenYear);

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
                final String choosenMonth = (String) parent.getItemAtPosition(position);
                b.putString("ChoosenMonth", choosenMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnOkFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                String date = edtDate.getText().toString();
                String choosenYear = b.getString("ChoosenYear");
                String choosenMonth = b.getString("ChoosenMonth");
                String shopCode = b.getString("ShopCode");
                if(TextUtils.isEmpty(date)){
                    Toast.makeText(getApplicationContext(),"Vui lòng chọn ngày",Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(getApplicationContext(), StoreSaleActivity.class);
                    intent.putExtra("ChoosenYear", choosenYear);
                    intent.putExtra("ChoosenMonth", choosenMonth);
                    intent.putExtra("ChoosenDate",date);
                    intent.putExtra("ShopCode",shopCode);
                    intent.putExtra("ShopSale",true);
                    startActivity(intent);
                }


            }
        });

        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void shopStorageDialog(String itemKey) {
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

        adapterFirebaseShopProductStorage = new FirebaseRecyclerAdapter<Product,ProductStorageViewHolder>(
                Product.class,
                R.id.item_product_view_storage,
                ProductStorageViewHolder.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(itemKey)
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

        productStorageList.setAdapter(adapterFirebaseShopProductStorage);
        adapterFirebaseShopProductStorage.notifyDataSetChanged();
    }
    private void shopStorageDialogIn(String itemKey) {
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

        adapterFirebaseShopProductStorageIn = new FirebaseRecyclerAdapter<Product,ProductStorageViewHolder2>(
                Product.class,
                R.id.item_product_view_storage,
                ProductStorageViewHolder2.class,
                Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child(itemKey)
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

        productStorageList.setAdapter(adapterFirebaseShopProductStorageIn);
        adapterFirebaseShopProductStorageIn.notifyDataSetChanged();
    }



    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView)itemView.findViewById(R.id.tv_item_product_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    Product product = adapterFirebase.getItem(position);
                    String productName = product.getProductName();
                    b.putString("ProductName",productName);
                    productFilterDialog();
                }
            });

        }
    }
    private class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView shopName,shopAddress;
        ImageView shopPic;

        ShopViewHolder(View itemView) {
            super(itemView);
            shopName = (TextView) itemView.findViewById(R.id.tv_item_client_sale_name);
            shopAddress = (TextView) itemView.findViewById(R.id.tv_item_shop_address);
            shopPic = (ImageView)itemView.findViewById(R.id.iv_item_shop_pic);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    String itemKey = adapterFirebaseShop.getRef(position).getKey();
                    b.putString("ShopCode",itemKey);
                    shopFilterDialog();

                }
            });



        }

    }
    private class ShopViewHolder2 extends RecyclerView.ViewHolder {
        TextView shopName,shopAddress;
        ImageView shopPic;

        ShopViewHolder2(View itemView) {
            super(itemView);
            shopName = (TextView) itemView.findViewById(R.id.tv_item_client_sale_name);
            shopAddress = (TextView) itemView.findViewById(R.id.tv_item_shop_address);
            shopPic = (ImageView)itemView.findViewById(R.id.iv_item_shop_pic);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    String itemKey = adapterFirebaseShopStorage.getRef(position).getKey();
                    shopStorageDialog(itemKey);

                }
            });



        }

    }
    private class ShopViewHolder3 extends RecyclerView.ViewHolder {
        TextView shopName,shopAddress;
        ImageView shopPic;

        ShopViewHolder3(View itemView) {
            super(itemView);
            shopName = (TextView) itemView.findViewById(R.id.tv_item_client_sale_name);
            shopAddress = (TextView) itemView.findViewById(R.id.tv_item_shop_address);
            shopPic = (ImageView)itemView.findViewById(R.id.iv_item_shop_pic);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    String itemKey = adapterFirebaseShopStorageIn.getRef(position).getKey();
                    shopStorageDialogIn(itemKey);

                }
            });

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
                    final String shopCode = adapterFirebaseShopProductStorageIn.getRef(position).getParent().getKey();
                    Product product = adapterFirebaseShopProductStorageIn.getItem(position);
                    final String productName = product.getProductName();


                    AlertDialog.Builder builder = new AlertDialog.Builder(ShopChainActivity.this);
                    LayoutInflater inflater = getLayoutInflater();
                    View dialogView = inflater.inflate(R.layout.dialog_storeman_storage_in,null);
                    builder.setView(dialogView);
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
                                if(storageFirst !=0){
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

                            timeStamp = Calendar.getInstance().getTime().getTime();

                            if(storageLast==0 || storageIn == 0 || storageFirst == 0){
                                Toast.makeText(getApplicationContext(),"Vui lòng nhập số lượng nhập kho.",Toast.LENGTH_LONG).show();
                            }else{
                                if(shopCode.equals("CH1")){
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


                                }else{
                                    Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child("CH1").child(productName).child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String currentStorage = dataSnapshot.getValue().toString();
                                            float updateStorage = Float.parseFloat(currentStorage+"")-storageIn;
                                            Constants.refDatabase.child(emailLogin).child("StorageHistory").child("Z_POS_ShopStorageOutHistory").child("CH1").child(productName).child(timeStamp+"").child("First").setValue(currentStorage);
                                            Constants.refDatabase.child(emailLogin).child("StorageHistory").child("Z_POS_ShopStorageOutHistory").child("CH1").child(productName).child(timeStamp+"").child("Last").setValue(updateStorage+"");
                                            Constants.refDatabase.child(emailLogin).child("Z_POS_ShopStorage").child("CH1").child(productName).child("unitQuantity").setValue(updateStorage+"").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
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
                                                }
                                            });

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                    d.dismiss();

                                }
                            }
                        }
                    });

                }
            });

        }
    }




}

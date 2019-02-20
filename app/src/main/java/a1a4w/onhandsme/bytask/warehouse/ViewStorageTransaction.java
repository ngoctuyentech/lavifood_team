package a1a4w.onhandsme.bytask.warehouse;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.util.Arrays;
import java.util.List;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.StorageTransaction;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.MySpinerAdapter;
import a1a4w.onhandsme.utils.Utils;
import fr.ganfra.materialspinner.MaterialSpinner;

public class ViewStorageTransaction extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<StorageTransaction,StorageTransactionViewHolder> adapterFirebase;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference refStorageTransaction;
    private Bundle b = new Bundle();
    private boolean productStorageTransaction;
    private String productName,emailLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_storage_transaction);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_view_storage_transaction);
        setSupportActionBar(toolbar);

        Intent it = this.getIntent();
        productStorageTransaction = it.getBooleanExtra("ProductStorageTransaction",false);
        productName = it.getStringExtra("ProductName");
        emailLogin = it.getStringExtra("EmailLogin");

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_transaction_storage);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);

        recyclerView.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                recyclerView.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });
        filterTimeDialog();
    }

    private void filterTimeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filter_view_month_date,null);
        builder.setView(dialogView);


        Spinner spinYearFilter = (Spinner)dialogView.findViewById(R.id.spinner_dialog_filter_time_md_year);
        final Spinner spinMonthFilter = (Spinner)dialogView.findViewById(R.id.spinner_dialog_filter_time_md_month);

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
                final String choosenYear = (String) parent.getItemAtPosition(position);
                b.putString("ChoosenYear",choosenYear);

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
                final String choosenMonth = (String) parent.getItemAtPosition(position);
                b.putString("ChoosenMonth",choosenMonth);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        builder.show();

    }

    private void StorageTransactionList(DatabaseReference refStorageTransaction){

        adapterFirebase = new FirebaseRecyclerAdapter<StorageTransaction,StorageTransactionViewHolder>(
                StorageTransaction.class,
                R.id.item_storage_transaction,
                StorageTransactionViewHolder.class,
                refStorageTransaction
        ) {
            @Override
            public StorageTransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_storage_transaction,parent,false);
                return new StorageTransactionViewHolder(v);
            }

            @Override
            protected void populateViewHolder(StorageTransactionViewHolder viewHolder, StorageTransaction model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productIn.setText(model.getProductIn());
                viewHolder.productOut.setText(model.getProductOut());
                viewHolder.productStorage.setText(model.getProductStorage());
            }
        };

        recyclerView.setAdapter(adapterFirebase);
        adapterFirebase.notifyDataSetChanged();

    }

    public class StorageTransactionViewHolder extends RecyclerView.ViewHolder {
        TextView productName,productIn,productOut, productStorage;

        public StorageTransactionViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.tv_storage_transaction_product_name);
            productIn = (TextView) itemView.findViewById(R.id.tv_storage_transaction_in);
            productOut = (TextView) itemView.findViewById(R.id.tv_storage_transaction_out);
            productStorage = (TextView) itemView.findViewById(R.id.tv_storage_transaction_storage);

        }
    }

}

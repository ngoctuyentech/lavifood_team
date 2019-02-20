package a1a4w.onhandsme.bytask.warehouse;

import android.content.DialogInterface;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.ChangeSkuTransaction;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.MySpinerAdapter;
import a1a4w.onhandsme.utils.Utils;
import fr.ganfra.materialspinner.MaterialSpinner;

public class ViewChangeSkuActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<ChangeSkuTransaction,ChangeSkuViewHolder> adapterFirebase;
    private LinearLayoutManager linearLayoutManager;
    private DatabaseReference refChangeSku;
    private Bundle b = new Bundle();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_change_sku);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_view_change_sku);
        setSupportActionBar(toolbar);


        recyclerView = (RecyclerView)findViewById(R.id.recycler_view_change_sku);
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

        filterTimedialog();




    }

    private void filterTimedialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView= inflater.inflate(R.layout.dialog_filter_time,null);
        builder.setView(dialogView);

        final EditText edtChoosenDate = (EditText)dialogView.findViewById(R.id.edt_filter_time_date);
        Spinner spinYearFilter = (Spinner)dialogView.findViewById(R.id.spinner_filter_time_year);
        final Spinner spinMonthFilter = (Spinner)dialogView.findViewById(R.id.spinner_filter_time_month);

        String[] arrayYear = this.getResources().getStringArray(R.array.year_filter_array);
        List<String> arrayListYear = Arrays.asList(arrayYear);
        MySpinerAdapter spinerAdapterYear = new MySpinerAdapter();
        spinerAdapterYear.addItems(arrayListYear);
        MaterialSpinner spinnerYear = (MaterialSpinner)dialogView.findViewById(R.id.spinner_filter_time_year);
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
        MaterialSpinner spinnerMonth = (MaterialSpinner)dialogView.findViewById(R.id.spinner_filter_time_month);
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
        builder.setPositiveButton("Chọn ngày", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String choosenYear = b.getString("ChoosenYear");
                String choosenMonth = b.getString("ChoosenMonth");
                String choosenDate = edtChoosenDate.getText().toString();

                if(choosenYear == null || choosenMonth == null || choosenDate == null){
                    Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_SHORT);
                }else if(TextUtils.isEmpty(choosenDate)){
                    Toast.makeText(getApplicationContext(),"Vui lòng nhập ngày",Toast.LENGTH_SHORT);
                }else{
                    refChangeSku = Constants.refDatabase.child("WarehouseMan").child("ChangeSku").child(choosenYear+choosenMonth+choosenDate);

                    adapterFirebase = new FirebaseRecyclerAdapter<ChangeSkuTransaction, ChangeSkuViewHolder>(
                            ChangeSkuTransaction.class,
                            R.id.item_change_sku,
                            ChangeSkuViewHolder.class,
                            refChangeSku
                    ) {
                        @Override
                        public ChangeSkuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_change_sku,parent,false);
                            return new ChangeSkuViewHolder(v);
                        }

                        @Override
                        protected void populateViewHolder(ChangeSkuViewHolder viewHolder, ChangeSkuTransaction model, int position) {
                            viewHolder.date.setText(getDate(model.getTimeStamp()));
                            viewHolder.source.setText(model.getSourceProduct());
                            viewHolder.des.setText(model.getDesProduct());
                            viewHolder.rate.setText(model.getChangeRate());
                            viewHolder.sourcenum.setText(model.getSourceQuantity());
                            viewHolder.sourceQuanIn.setText(model.getSourceStorage());
                            viewHolder.sourceQuanResult.setText(model.getResultSourceProduct());
                            viewHolder.desQuanIn.setText(model.getDesStorage());
                            viewHolder.desQuanResult.setText(model.getResultDesProduct());

                        }
                    };
                    recyclerView.setAdapter(adapterFirebase);
                    adapterFirebase.notifyDataSetChanged();
                }


                dialog.dismiss();
            }
        });

        builder.show();

    }

    public class ChangeSkuViewHolder extends RecyclerView.ViewHolder {
        TextView date,source,des,rate,sourcenum,sourceQuanIn,sourceQuanResult, desQuanIn, desQuanResult;

        public ChangeSkuViewHolder(View itemView) {
            super(itemView);
            date = (TextView) itemView.findViewById(R.id.tv_item_change_sku_date);
            source = (TextView) itemView.findViewById(R.id.tv_item_change_sku_soure_name);
            des = (TextView) itemView.findViewById(R.id.tv_item_change_sku_des_name);
            rate = (TextView) itemView.findViewById(R.id.tv_item_change_sku_change_rate);
            sourcenum = (TextView)itemView.findViewById(R.id.tv_item_change_sku_source_quantity);
            sourceQuanIn = (TextView)itemView.findViewById(R.id.tv_item_change_sku_source_storage_in);
            sourceQuanResult = (TextView)itemView.findViewById(R.id.tv_item_change_sku_source_storage_result);
            desQuanIn = (TextView)itemView.findViewById(R.id.tv_item_change_sku_des_storage_in);
            desQuanResult = (TextView)itemView.findViewById(R.id.tv_item_change_sku_des_storage_result);

        }
    }

    private String getDate(String timeStampStr){

        try{
            DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Date netDate = (new Date(Long.parseLong(timeStampStr)));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "00/00/00";
        }
    }

}

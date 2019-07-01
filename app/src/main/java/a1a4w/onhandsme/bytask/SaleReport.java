package a1a4w.onhandsme.bytask;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Client;
import a1a4w.onhandsme.model.Employee;
import a1a4w.onhandsme.utils.Utils;

import static a1a4w.onhandsme.utils.Constants.buttonClick;
import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class SaleReport extends AppCompatActivity {

    Button btnByEmployee,btnByProduct,btnByZone,btnMonth,btnYear,btnThisMonth,btnQuarter,btnTopSale,btnTopClient;
    String emailLogin,userEmail;
    DatabaseReference refCompany;
    String timeClicked, filterClicked;
    boolean updat= false,monthCountDone;
    Map<String,Float> saleValueMap = new HashMap<>();
    private Map sortedSaleValue;
    private Map sortClientValue;
    private ImageView ivAI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_report);

        btnByEmployee = findViewById(R.id.btn_sale_report_employee);
        btnByProduct = findViewById(R.id.btn_sale_report_product);
        btnYear = findViewById(R.id.btn_sale_report_year);
        btnMonth = findViewById(R.id.btn_sale_report_month);
        //btnQuarter = findViewById(R.id.btn_sale_report_quarter);
        final BarChart barTime = findViewById(R.id.bar_sale_report_filterByProduct);
        final BarChart barEmployee = findViewById(R.id.bar_sale_report_filterByEmployee);
        final BarChart barTop = findViewById(R.id.bar_sale_report_top10);
        btnTopSale = findViewById(R.id.btn_sale_report_topSale);
        btnTopClient =  findViewById(R.id.btn_sale_report_topClient);
        ivAI = findViewById(R.id.iv_sale_report_AI);

        ivAI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SaleReport.this);
                builder.setMessage("Vui lòng nâng cấp lên phiên bản Cloudbiz DMS Pro để sử dụng tính năng này!");
                builder.show();
            }
        });

        timeClicked = "Month";
        filterClicked = "Employee";

        Intent it = this.getIntent();
        emailLogin = it.getStringExtra("EmailLogin");

        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");

        btnMonth.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
        btnByEmployee.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
        btnTopSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

        refCompany = refDatabase.child(emailLogin);

        DateTime dt = new DateTime();
        final String month = dt.getMonthOfYear()+"";
        final String year = dt.getYear()+"";

        refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                //final String[] names = new String[(int) dataSnapshot.getChildrenCount()];
                final ArrayList<String> xLabels = new ArrayList<>();

                final List<BarEntry> monthEntries = new ArrayList<>();
                int i = 0;
                for (DataSnapshot itemSale:snapSale){
                    final String saleEmail = itemSale.getKey();
                    Employee employee = itemSale.getValue(Employee.class);
                    String saleName = employee.getEmployeeName();
                    String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);
                    //names[i] = saleNameShort;
                    xLabels.add(saleNameShort);

                    final int finalI = i;
                    refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(year+"-"+month)){

                                refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float monthSaleBySup = Float.parseFloat(dataSnapshot.getValue().toString());

                                        monthEntries.add(new BarEntry(finalI, monthSaleBySup));

                                        BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo nhân viên");

                                        BarData data = new BarData(set);

                                        Description description = new Description();
                                        description.setText("");

                                        barEmployee.getAxisRight().setDrawGridLines(false);
                                        barEmployee.getAxisLeft().setDrawGridLines(false);
                                        barEmployee.getXAxis().setDrawGridLines(false);
                                        barEmployee.getXAxis().setGranularityEnabled(true);
                                        //barTime.getXAxis().setDrawLabels(false);
                                        barEmployee.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                        barEmployee.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                            @Override
                                            public String getFormattedValue(float value) {
                                                return xLabels.get((int) value);                                            }
                                        });

                                        barEmployee.setDescription(description);
                                        barEmployee.getAxisRight().setEnabled(false);
                                        barEmployee.setTouchEnabled(true);
                                        //barTime.setMarker(mv);
                                        barEmployee.setData(data);
                                        barEmployee.animateXY(1000,2000);
                                        barEmployee.setFitBars(true); // make the x-axis fit exactly all bars
                                        barEmployee.invalidate(); // refresh

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
                    i++;

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapSup = dataSnapshot.getChildren();
                long supCount = dataSnapshot.getChildrenCount();

                final HashMap<String,Float> saleValues = new HashMap<>();
                final List<BarEntry> monthEntries = new ArrayList<>();
                final ArrayList<String> xLabels = new ArrayList<>();

                int i = 0;
                for(DataSnapshot itemSup:snapSup){
                    i++;
                    String supEmail = itemSup.getKey();

                    refCompany.child("SaleManBySup").child(supEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                            long saleCount = dataSnapshot.getChildrenCount();

                            int i = 0;
                            for(final DataSnapshot itemSale:snapSale){
                                i++;
                                final String saleEmail = itemSale.getKey();
                                final String saleName = itemSale.getValue(Employee.class).getEmployeeName();
                                final String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);


                                refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(year+"-"+month)){
                                            refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    saleValues.put(saleName,Float.parseFloat(dataSnapshot.getValue().toString()));
                                                    sortedSaleValue = Utils.sortDecreaseByValues(saleValues);

                                                    Set setSort = sortedSaleValue.entrySet();

                                                    int i = 0;
                                                    for (Object o : setSort) {
                                                        i++;

                                                        if (i<=10){
                                                            Map.Entry me = (Map.Entry) o;
                                                            String saleName = (String) me.getKey();
                                                            xLabels.add(saleName);
                                                            monthEntries.add(new BarEntry(i, (Float) me.getValue(),me.getKey()));

                                                        }

                                                    }

                                                    BarDataSet set = new BarDataSet(monthEntries,"Doanh số TOP tháng này theo nhân viên");

                                                    BarData data = new BarData(set);

                                                    Description description = new Description();
                                                    description.setText("");

                                                    barTop.getAxisRight().setDrawGridLines(false);
                                                    barTop.getAxisLeft().setDrawGridLines(false);
                                                    barTop.getXAxis().setDrawGridLines(false);
                                                    barTop.getXAxis().setGranularityEnabled(true);
                                                    //barTime.getXAxis().setDrawLabels(false);
                                                    barTop.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
/*
                                                            barTop.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                @Override
                                                                public String getFormattedValue(float value) {
                                                                    return xLabels.get((int) value);                                            }
                                                            });
*/
                                                    barTop.setDescription(description);
                                                    barTop.getAxisRight().setEnabled(false);
                                                    barTop.setTouchEnabled(true);
                                                    //barTime.setMarker(mv);
                                                    barTop.setData(data);
                                                    barTop.animateXY(1000,2000);
                                                    barTop.setFitBars(true); // make the x-axis fit exactly all bars
                                                    barTop.invalidate(); // refresh

                                                    barTop.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                                        @Override
                                                        public void onValueSelected(Entry e, Highlight h) {
                                                            Toast.makeText(getApplicationContext(),e.getData().toString(),Toast.LENGTH_LONG).show();
                                                        }

                                                        @Override
                                                        public void onNothingSelected() {

                                                        }
                                                    });




                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }else{
                                            sortedSaleValue = Utils.sortDecreaseByValues(saleValues);;
                                            Set setSort = sortedSaleValue.entrySet();

                                            int i = 0;
                                            for (Object o : setSort) {
                                                i++;

                                                if (i<=10){
                                                    Map.Entry me = (Map.Entry) o;
                                                    String saleName = (String) me.getKey();
                                                    xLabels.add(saleName);
                                                    monthEntries.add(new BarEntry(i, (Float) me.getValue(),me.getKey()));

                                                }

                                            }



                                            BarDataSet set = new BarDataSet(monthEntries,"Doanh số TOP tháng này theo nhân viên");

                                            BarData data = new BarData(set);

                                            Description description = new Description();
                                            description.setText("");

                                            barTop.getAxisRight().setDrawGridLines(false);
                                            barTop.getAxisLeft().setDrawGridLines(false);
                                            barTop.getXAxis().setDrawGridLines(false);
                                            barTop.getXAxis().setGranularityEnabled(true);
                                            //barTime.getXAxis().setDrawLabels(false);
                                            barTop.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
/*
                                                            barTop.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                @Override
                                                                public String getFormattedValue(float value) {
                                                                    return xLabels.get((int) value);                                            }
                                                            });
*/
                                            barTop.setDescription(description);
                                            barTop.getAxisRight().setEnabled(false);
                                            barTop.setTouchEnabled(true);
                                            //barTime.setMarker(mv);
                                            barTop.setData(data);
                                            barTop.animateXY(1000,2000);
                                            barTop.setFitBars(true); // make the x-axis fit exactly all bars
                                            barTop.invalidate(); // refresh

                                            barTop.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                                @Override
                                                public void onValueSelected(Entry e, Highlight h) {
                                                    Toast.makeText(getApplicationContext(),e.getData().toString(),Toast.LENGTH_LONG).show();
                                                }

                                                @Override
                                                public void onNothingSelected() {

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

        btnByEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                barTime.setVisibility(View.GONE);
                barEmployee.setVisibility(View.VISIBLE);

                filterClicked = "Employee";

                btnByProduct.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                btnByEmployee.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

                if(timeClicked.equals("Month")){

                    refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                            //final String[] names = new String[(int) dataSnapshot.getChildrenCount()];
                            final ArrayList<String> xLabels = new ArrayList<>();

                            final List<BarEntry> monthEntries = new ArrayList<>();
                            int i = 0;
                            for (DataSnapshot itemSale:snapSale){
                                final String saleEmail = itemSale.getKey();
                                Employee employee = itemSale.getValue(Employee.class);
                                String saleName = employee.getEmployeeName();
                                String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);
                                //names[i] = saleNameShort;
                                xLabels.add(saleNameShort);

                                final int finalI = i;
                                refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(year+"-"+month)){

                                            refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    float monthSaleBySup = Float.parseFloat(dataSnapshot.getValue().toString());

                                                    monthEntries.add(new BarEntry(finalI, monthSaleBySup));

                                                    BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo nhân viên");

                                                    BarData data = new BarData(set);

                                                    Description description = new Description();
                                                    description.setText("");

                                                    barEmployee.getAxisRight().setDrawGridLines(false);
                                                    barEmployee.getAxisLeft().setDrawGridLines(false);
                                                    barEmployee.getXAxis().setDrawGridLines(false);
                                                    barEmployee.getXAxis().setGranularityEnabled(true);
                                                    //barTime.getXAxis().setDrawLabels(false);
                                                    barEmployee.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                    barEmployee.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                        @Override
                                                        public String getFormattedValue(float value) {
                                                            return xLabels.get((int) value);                                            }
                                                    });

                                                    barEmployee.setDescription(description);
                                                    barEmployee.getAxisRight().setEnabled(false);
                                                    barEmployee.setTouchEnabled(true);
                                                    //barTime.setMarker(mv);
                                                    barEmployee.setData(data);
                                                    barEmployee.animateXY(1000,2000);
                                                    barEmployee.setFitBars(true); // make the x-axis fit exactly all bars
                                                    barEmployee.invalidate(); // refresh

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
                                i++;

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                if(timeClicked.equals("Year")){

                    refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                            //final String[] names = new String[(int) dataSnapshot.getChildrenCount()];
                            final ArrayList<String> xLabels = new ArrayList<>();

                            final List<BarEntry> monthEntries = new ArrayList<>();
                            int i = 0;
                            for (DataSnapshot itemSale:snapSale){
                                final String saleEmail = itemSale.getKey();
                                Employee employee = itemSale.getValue(Employee.class);
                                String saleName = employee.getEmployeeName();
                                String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);
                                //names[i] = saleNameShort;
                                xLabels.add(saleNameShort);

                                final int finalI = i;
                                refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(year)){

                                            refCompany.child("TotalBySale").child(saleEmail).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    float monthSaleBySup = Float.parseFloat(dataSnapshot.getValue().toString());

                                                    monthEntries.add(new BarEntry(finalI, monthSaleBySup));

                                                    BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo nhân viên");

                                                    BarData data = new BarData(set);

                                                    Description description = new Description();
                                                    description.setText("");

                                                    barEmployee.getAxisRight().setDrawGridLines(false);
                                                    barEmployee.getAxisLeft().setDrawGridLines(false);
                                                    barEmployee.getXAxis().setDrawGridLines(false);
                                                    barEmployee.getXAxis().setGranularityEnabled(true);
                                                    //barTime.getXAxis().setDrawLabels(false);
                                                    barEmployee.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                    barEmployee.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                        @Override
                                                        public String getFormattedValue(float value) {
                                                            return xLabels.get((int) value);                                            }
                                                    });

                                                    barEmployee.setDescription(description);
                                                    barEmployee.getAxisRight().setEnabled(false);
                                                    barEmployee.setTouchEnabled(true);
                                                    //barTime.setMarker(mv);
                                                    barEmployee.setData(data);
                                                    barEmployee.animateXY(1000,2000);
                                                    barEmployee.setFitBars(true); // make the x-axis fit exactly all bars
                                                    barEmployee.invalidate(); // refresh

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
                                i++;

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }
        });

        btnByProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                barEmployee.setVisibility(View.GONE);
                barTime.setVisibility(View.VISIBLE);

                filterClicked = "Product";

                btnByEmployee.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                btnByProduct.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

                if(timeClicked.equals("Month")){
                    btnByEmployee.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                    btnByProduct.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                    final ArrayList<String> xLabels = new ArrayList<>();
                    final Map<String,Float> saleByProducts = new HashMap<>();
                    final List<BarEntry> monthEntries = new ArrayList<>();
                    refCompany.child("TotalProductBySale").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                            final long itemCount = dataSnapshot.getChildrenCount();

                            int i = 0;
                            for(final DataSnapshot itemProduct:snapProduct){
                                i++;
                                final String productCode = itemProduct.getKey();
                                //xLabels.add(productCode);
                                final DatabaseReference refProduct = itemProduct.getRef();
                                refCompany.child("Product").child(productCode).child("productName").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String productName = dataSnapshot.getValue().toString();
                                        if(!xLabels.contains(productName)){
                                            xLabels.add(dataSnapshot.getValue().toString());

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                if(i ==itemCount){
                                    refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(year+"-"+month)){
                                                refProduct.child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float saleValue = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(saleByProducts.containsKey(productCode)){
                                                            float currentSale = saleByProducts.get(productCode);
                                                            float updateSale = currentSale + saleValue;
                                                            //Toast.makeText(getApplicationContext(), updateSale+"", Toast.LENGTH_LONG).show();
                                                            saleByProducts.put(productCode,updateSale);

                                                            int x = 0;
                                                            for(Map.Entry<String,Float> entry:saleByProducts.entrySet()){
                                                                monthEntries.add(new BarEntry(x, entry.getValue()));

                                                                BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo sản ");

                                                                BarData data = new BarData(set);

                                                                Description description = new Description();
                                                                description.setText("");

                                                                barTime.getAxisRight().setDrawGridLines(false);
                                                                barTime.getAxisLeft().setDrawGridLines(false);
                                                                barTime.getXAxis().setDrawGridLines(false);
                                                                barTime.getXAxis().setGranularityEnabled(true);
                                                                //barTime.getXAxis().setDrawLabels(false);
                                                                barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                                barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                    @Override
                                                                    public String getFormattedValue(float value) {
                                                                        return xLabels.get((int) value);                                            }
                                                                });

                                                                barTime.setDescription(description);
                                                                barTime.getAxisRight().setEnabled(false);
                                                                barTime.setTouchEnabled(true);
                                                                //barTime.setMarker(mv);
                                                                barTime.setData(data);
                                                                barTime.animateXY(1000,2000);
                                                                barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                                                barTime.invalidate(); // refreshx

                                                                x++;
                                                            }


                                                        }else{
                                                            saleByProducts.put(productCode, saleValue);

                                                            int x = 0;
                                                            for(Map.Entry<String,Float> entry:saleByProducts.entrySet()){
                                                                monthEntries.add(new BarEntry(x, entry.getValue()));

                                                                BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo nhân viên");

                                                                BarData data = new BarData(set);

                                                                Description description = new Description();
                                                                description.setText("");

                                                                barTime.getAxisRight().setDrawGridLines(false);
                                                                barTime.getAxisLeft().setDrawGridLines(false);
                                                                barTime.getXAxis().setDrawGridLines(false);
                                                                barTime.getXAxis().setGranularityEnabled(true);
                                                                //barTime.getXAxis().setDrawLabels(false);
                                                                barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                                barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                    @Override
                                                                    public String getFormattedValue(float value) {
                                                                        return xLabels.get((int) value);                                            }
                                                                });

                                                                barTime.setDescription(description);
                                                                barTime.getAxisRight().setEnabled(false);
                                                                barTime.setTouchEnabled(true);
                                                                //barTime.setMarker(mv);
                                                                barTime.setData(data);
                                                                barTime.animateXY(1000,2000);
                                                                barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                                                barTime.invalidate(); // refreshx

                                                                x++;
                                                            }

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
                                    refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(year+"-"+month)){
                                                refProduct.child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float saleValue = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(saleByProducts.containsKey(productCode)){
                                                            float currentSale = saleByProducts.get(productCode);
                                                            float updateSale = currentSale + saleValue;
                                                            //Toast.makeText(getApplicationContext(), updateSale+"", Toast.LENGTH_LONG).show();
                                                            saleByProducts.put(productCode,updateSale);

                                                        }else{
                                                            saleByProducts.put(productCode, saleValue);

                                                            refCompany.child("Product").child(productCode).child("productName").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    xLabels.add(dataSnapshot.getValue().toString());

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

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

                if(timeClicked.equals("Year")){
                    btnByEmployee.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                    btnByProduct.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                    final ArrayList<String> xLabels = new ArrayList<>();
                    final Map<String,Float> saleByProducts = new HashMap<>();
                    final List<BarEntry> monthEntries = new ArrayList<>();
                    refCompany.child("TotalProductBySale").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                            final long itemCount = dataSnapshot.getChildrenCount();

                            int i = 0;
                            for(final DataSnapshot itemProduct:snapProduct){
                                i++;
                                final String productCode = itemProduct.getKey();
                                //xLabels.add(productCode);
                                final DatabaseReference refProduct = itemProduct.getRef();
                                refCompany.child("Product").child(productCode).child("productName").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String productName = dataSnapshot.getValue().toString();
                                        if(!xLabels.contains(productName)){
                                            xLabels.add(dataSnapshot.getValue().toString());

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                if(i ==itemCount){
                                    refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(year)){
                                                refProduct.child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float saleValue = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(saleByProducts.containsKey(productCode)){
                                                            float currentSale = saleByProducts.get(productCode);
                                                            float updateSale = currentSale + saleValue;
                                                            //Toast.makeText(getApplicationContext(), updateSale+"", Toast.LENGTH_LONG).show();
                                                            saleByProducts.put(productCode,updateSale);

                                                            int x = 0;
                                                            for(Map.Entry<String,Float> entry:saleByProducts.entrySet()){
                                                                monthEntries.add(new BarEntry(x, entry.getValue()));

                                                                BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo sản ");

                                                                BarData data = new BarData(set);

                                                                Description description = new Description();
                                                                description.setText("");

                                                                barTime.getAxisRight().setDrawGridLines(false);
                                                                barTime.getAxisLeft().setDrawGridLines(false);
                                                                barTime.getXAxis().setDrawGridLines(false);
                                                                barTime.getXAxis().setGranularityEnabled(true);
                                                                //barTime.getXAxis().setDrawLabels(false);
                                                                barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                                barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                    @Override
                                                                    public String getFormattedValue(float value) {
                                                                        return xLabels.get((int) value);                                            }
                                                                });

                                                                barTime.setDescription(description);
                                                                barTime.getAxisRight().setEnabled(false);
                                                                barTime.setTouchEnabled(true);
                                                                //barTime.setMarker(mv);
                                                                barTime.setData(data);
                                                                barTime.animateXY(1000,2000);
                                                                barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                                                barTime.invalidate(); // refreshx

                                                                x++;
                                                            }


                                                        }else{
                                                            saleByProducts.put(productCode, saleValue);

                                                            int x = 0;
                                                            for(Map.Entry<String,Float> entry:saleByProducts.entrySet()){
                                                                monthEntries.add(new BarEntry(x, entry.getValue()));

                                                                BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo nhân viên");

                                                                BarData data = new BarData(set);

                                                                Description description = new Description();
                                                                description.setText("");

                                                                barTime.getAxisRight().setDrawGridLines(false);
                                                                barTime.getAxisLeft().setDrawGridLines(false);
                                                                barTime.getXAxis().setDrawGridLines(false);
                                                                barTime.getXAxis().setGranularityEnabled(true);
                                                                //barTime.getXAxis().setDrawLabels(false);
                                                                barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                                barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                    @Override
                                                                    public String getFormattedValue(float value) {
                                                                        return xLabels.get((int) value);                                            }
                                                                });

                                                                barTime.setDescription(description);
                                                                barTime.getAxisRight().setEnabled(false);
                                                                barTime.setTouchEnabled(true);
                                                                //barTime.setMarker(mv);
                                                                barTime.setData(data);
                                                                barTime.animateXY(1000,2000);
                                                                barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                                                barTime.invalidate(); // refreshx

                                                                x++;
                                                            }

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
                                    refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            if(dataSnapshot.hasChild(year)){
                                                refProduct.child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float saleValue = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(saleByProducts.containsKey(productCode)){
                                                            float currentSale = saleByProducts.get(productCode);
                                                            float updateSale = currentSale + saleValue;
                                                            //Toast.makeText(getApplicationContext(), updateSale+"", Toast.LENGTH_LONG).show();
                                                            saleByProducts.put(productCode,updateSale);

                                                        }else{
                                                            saleByProducts.put(productCode, saleValue);

                                                            refCompany.child("Product").child(productCode).child("productName").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    xLabels.add(dataSnapshot.getValue().toString());

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

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }


            }
        });

        btnMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                btnYear.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                btnMonth.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

                timeClicked = "Month";

               if(filterClicked.equals("Employee")){
                   refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(final DataSnapshot dataSnapshot) {
                           Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                           //final String[] names = new String[(int) dataSnapshot.getChildrenCount()];
                           final ArrayList<String> xLabels = new ArrayList<>();

                           final List<BarEntry> monthEntries = new ArrayList<>();
                           int i = 0;
                           for (DataSnapshot itemSale:snapSale){
                               final String saleEmail = itemSale.getKey();
                               Employee employee = itemSale.getValue(Employee.class);
                               String saleName = employee.getEmployeeName();
                               String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);
                               //names[i] = saleNameShort;
                               xLabels.add(saleNameShort);

                               final int finalI = i;
                               refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                   @Override
                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                       if(dataSnapshot.hasChild(year+"-"+month)){

                                           refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                               @Override
                                               public void onDataChange(DataSnapshot dataSnapshot) {
                                                   float monthSaleBySup = Float.parseFloat(dataSnapshot.getValue().toString());

                                                   monthEntries.add(new BarEntry(finalI, monthSaleBySup));

                                                   BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo nhân viên");

                                                   BarData data = new BarData(set);

                                                   Description description = new Description();
                                                   description.setText("");

                                                   barEmployee.getAxisRight().setDrawGridLines(false);
                                                   barEmployee.getAxisLeft().setDrawGridLines(false);
                                                   barEmployee.getXAxis().setDrawGridLines(false);
                                                   barEmployee.getXAxis().setGranularityEnabled(true);
                                                   //barTime.getXAxis().setDrawLabels(false);
                                                   barEmployee.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                   barEmployee.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                       @Override
                                                       public String getFormattedValue(float value) {
                                                           return xLabels.get((int) value);                                            }
                                                   });

                                                   barEmployee.setDescription(description);
                                                   barEmployee.getAxisRight().setEnabled(false);
                                                   barEmployee.setTouchEnabled(true);
                                                   //barTime.setMarker(mv);
                                                   barEmployee.setData(data);
                                                   barEmployee.animateXY(1000,2000);
                                                   barEmployee.setFitBars(true); // make the x-axis fit exactly all bars
                                                   barEmployee.invalidate(); // refresh

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
                               i++;

                           }
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });

               }

               if(filterClicked.equals("Product")){
                   refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                       @Override
                       public void onDataChange(final DataSnapshot dataSnapshot) {
                           Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                           long itemCount = dataSnapshot.getChildrenCount();

                           final List<BarEntry> monthEntries = new ArrayList<>();
                           final ArrayList<String> xLabels = new ArrayList<>();
                           final Map<String,Float> saleByProducts = new HashMap<>();

                           int i = 0;
                           for(DataSnapshot itemSale:snapSale){
                               String saleEmail = itemSale.getKey();
                               i++;

                               if(i == itemCount){
                                   refCompany.child("TotalProductBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                       @Override
                                       public void onDataChange(DataSnapshot dataSnapshot) {
                                           Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                                           final long itemCount = dataSnapshot.getChildrenCount();

                                           int i = 0;
                                           for(final DataSnapshot itemProduct:snapProduct){
                                               i++;
                                               final String productCode = itemProduct.getKey();
                                               //xLabels.add(productCode);
                                               final DatabaseReference refProduct = itemProduct.getRef();
                                               refCompany.child("Product").child(productCode).child("productName").addListenerForSingleValueEvent(new ValueEventListener() {
                                                   @Override
                                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                                       String productName = dataSnapshot.getValue().toString();
                                                       if(!xLabels.contains(productName)){
                                                           xLabels.add(dataSnapshot.getValue().toString());

                                                       }

                                                   }

                                                   @Override
                                                   public void onCancelled(DatabaseError databaseError) {

                                                   }
                                               });

                                               if(i ==itemCount){
                                                   refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                                       @Override
                                                       public void onDataChange(DataSnapshot dataSnapshot) {
                                                           if(dataSnapshot.hasChild(year+"-"+month)){
                                                               refProduct.child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                   @Override
                                                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                                                       float saleValue = Float.parseFloat(dataSnapshot.getValue().toString());

                                                                       if(saleByProducts.containsKey(productCode)){
                                                                           float currentSale = saleByProducts.get(productCode);
                                                                           float updateSale = currentSale + saleValue;
                                                                           //Toast.makeText(getApplicationContext(), updateSale+"", Toast.LENGTH_LONG).show();
                                                                           saleByProducts.put(productCode,updateSale);

                                                                           int x = 0;
                                                                           for(Map.Entry<String,Float> entry:saleByProducts.entrySet()){
                                                                               monthEntries.add(new BarEntry(x, entry.getValue()));

                                                                               BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo sản ");

                                                                               BarData data = new BarData(set);

                                                                               Description description = new Description();
                                                                               description.setText("");

                                                                               barTime.getAxisRight().setDrawGridLines(false);
                                                                               barTime.getAxisLeft().setDrawGridLines(false);
                                                                               barTime.getXAxis().setDrawGridLines(false);
                                                                               barTime.getXAxis().setGranularityEnabled(true);
                                                                               //barTime.getXAxis().setDrawLabels(false);
                                                                               barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                                               barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                                   @Override
                                                                                   public String getFormattedValue(float value) {
                                                                                       return xLabels.get((int) value);                                            }
                                                                               });

                                                                               barTime.setDescription(description);
                                                                               barTime.getAxisRight().setEnabled(false);
                                                                               barTime.setTouchEnabled(true);
                                                                               //barTime.setMarker(mv);
                                                                               barTime.setData(data);
                                                                               barTime.animateXY(1000,2000);
                                                                               barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                                                               barTime.invalidate(); // refreshx

                                                                               x++;
                                                                           }


                                                                       }else{
                                                                           saleByProducts.put(productCode, saleValue);

                                                                           int x = 0;
                                                                           for(Map.Entry<String,Float> entry:saleByProducts.entrySet()){
                                                                               monthEntries.add(new BarEntry(x, entry.getValue()));

                                                                               BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo nhân viên");

                                                                               BarData data = new BarData(set);

                                                                               Description description = new Description();
                                                                               description.setText("");

                                                                               barTime.getAxisRight().setDrawGridLines(false);
                                                                               barTime.getAxisLeft().setDrawGridLines(false);
                                                                               barTime.getXAxis().setDrawGridLines(false);
                                                                               barTime.getXAxis().setGranularityEnabled(true);
                                                                               //barTime.getXAxis().setDrawLabels(false);
                                                                               barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                                               barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                                   @Override
                                                                                   public String getFormattedValue(float value) {
                                                                                       return xLabels.get((int) value);                                            }
                                                                               });

                                                                               barTime.setDescription(description);
                                                                               barTime.getAxisRight().setEnabled(false);
                                                                               barTime.setTouchEnabled(true);
                                                                               //barTime.setMarker(mv);
                                                                               barTime.setData(data);
                                                                               barTime.animateXY(1000,2000);
                                                                               barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                                                               barTime.invalidate(); // refreshx

                                                                               x++;
                                                                           }

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
                                                   refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                                       @Override
                                                       public void onDataChange(DataSnapshot dataSnapshot) {
                                                           if(dataSnapshot.hasChild(year+"-"+month)){
                                                               refProduct.child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                   @Override
                                                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                                                       float saleValue = Float.parseFloat(dataSnapshot.getValue().toString());

                                                                       if(saleByProducts.containsKey(productCode)){
                                                                           float currentSale = saleByProducts.get(productCode);
                                                                           float updateSale = currentSale + saleValue;
                                                                           //Toast.makeText(getApplicationContext(), updateSale+"", Toast.LENGTH_LONG).show();
                                                                           saleByProducts.put(productCode,updateSale);

                                                                       }else{
                                                                           saleByProducts.put(productCode, saleValue);

                                                                           refCompany.child("Product").child(productCode).child("productName").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                               @Override
                                                                               public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                   xLabels.add(dataSnapshot.getValue().toString());

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

                                       }

                                       @Override
                                       public void onCancelled(DatabaseError databaseError) {

                                       }
                                   });

                               }else{
                                   refCompany.child("TotalProductBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                       @Override
                                       public void onDataChange(DataSnapshot dataSnapshot) {
                                           Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();

                                           for(DataSnapshot itemProduct:snapProduct){

                                               final String productCode = itemProduct.getKey();
                                               //xLabels.add(productCode);
                                               refCompany.child("Product").child(productCode).child("productName").addListenerForSingleValueEvent(new ValueEventListener() {
                                                   @Override
                                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                                       String productName = dataSnapshot.getValue().toString();
                                                       if(!xLabels.contains(productName)){
                                                           xLabels.add(dataSnapshot.getValue().toString());

                                                       }

                                                   }

                                                   @Override
                                                   public void onCancelled(DatabaseError databaseError) {

                                                   }
                                               });

                                               final DatabaseReference refProduct = itemProduct.getRef();

                                               refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                                   @Override
                                                   public void onDataChange(DataSnapshot dataSnapshot) {
                                                       if(dataSnapshot.hasChild(year+"-"+month)){
                                                           refProduct.child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                               @Override
                                                               public void onDataChange(DataSnapshot dataSnapshot) {
                                                                   float saleValue = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                   if(saleByProducts.containsKey(productCode)){
                                                                       float currentSale = saleByProducts.get(productCode);
                                                                       float updateSale = currentSale + saleValue;
                                                                       //Toast.makeText(getApplicationContext(), updateSale+"", Toast.LENGTH_LONG).show();
                                                                       saleByProducts.put(productCode,updateSale);


                                                                   }else{
                                                                       saleByProducts.put(productCode, saleValue);


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
                       }

                       @Override
                       public void onCancelled(DatabaseError databaseError) {

                       }
                   });

               }
            }
        });

        btnYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                btnMonth.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                btnYear.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

                timeClicked = "Year";

                if(filterClicked.equals("Employee")){
                    refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                            //final String[] names = new String[(int) dataSnapshot.getChildrenCount()];
                            final ArrayList<String> xLabels = new ArrayList<>();

                            final List<BarEntry> monthEntries = new ArrayList<>();
                            int i = 0;
                            for (DataSnapshot itemSale:snapSale){
                                final String saleEmail = itemSale.getKey();
                                Employee employee = itemSale.getValue(Employee.class);
                                String saleName = employee.getEmployeeName();
                                String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);
                                //names[i] = saleNameShort;
                                xLabels.add(saleNameShort);

                                final int finalI = i;
                                refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(year)){

                                            refCompany.child("TotalBySale").child(saleEmail).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    float monthSaleBySup = Float.parseFloat(dataSnapshot.getValue().toString());

                                                    monthEntries.add(new BarEntry(finalI, monthSaleBySup));

                                                    BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo nhân viên");

                                                    BarData data = new BarData(set);

                                                    Description description = new Description();
                                                    description.setText("");

                                                    barEmployee.getAxisRight().setDrawGridLines(false);
                                                    barEmployee.getAxisLeft().setDrawGridLines(false);
                                                    barEmployee.getXAxis().setDrawGridLines(false);
                                                    barEmployee.getXAxis().setGranularityEnabled(true);
                                                    //barTime.getXAxis().setDrawLabels(false);
                                                    barEmployee.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                    barEmployee.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                        @Override
                                                        public String getFormattedValue(float value) {
                                                            return xLabels.get((int) value);                                            }
                                                    });

                                                    barEmployee.setDescription(description);
                                                    barEmployee.getAxisRight().setEnabled(false);
                                                    barEmployee.setTouchEnabled(true);
                                                    //barTime.setMarker(mv);
                                                    barEmployee.setData(data);
                                                    barEmployee.animateXY(1000,2000);
                                                    barEmployee.setFitBars(true); // make the x-axis fit exactly all bars
                                                    barEmployee.invalidate(); // refresh

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
                                i++;

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

                if(filterClicked.equals("Product")){
                    refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                            long itemCount = dataSnapshot.getChildrenCount();

                            final List<BarEntry> monthEntries = new ArrayList<>();
                            final ArrayList<String> xLabels = new ArrayList<>();
                            final Map<String,Float> saleByProducts = new HashMap<>();

                            int i = 0;
                            for(DataSnapshot itemSale:snapSale){
                                String saleEmail = itemSale.getKey();
                                i++;

                                if(i == itemCount){
                                    refCompany.child("TotalProductBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                                            final long itemCount = dataSnapshot.getChildrenCount();

                                            int i = 0;
                                            for(final DataSnapshot itemProduct:snapProduct){
                                                i++;
                                                final String productCode = itemProduct.getKey();
                                                //xLabels.add(productCode);
                                                final DatabaseReference refProduct = itemProduct.getRef();
                                                refCompany.child("Product").child(productCode).child("productName").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        String productName = dataSnapshot.getValue().toString();
                                                        if(!xLabels.contains(productName)){
                                                            xLabels.add(dataSnapshot.getValue().toString());

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                                if(i ==itemCount){
                                                    refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.hasChild(year)){
                                                                refProduct.child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        float saleValue = Float.parseFloat(dataSnapshot.getValue().toString());

                                                                        if(saleByProducts.containsKey(productCode)){
                                                                            float currentSale = saleByProducts.get(productCode);
                                                                            float updateSale = currentSale + saleValue;
                                                                            //Toast.makeText(getApplicationContext(), updateSale+"", Toast.LENGTH_LONG).show();
                                                                            saleByProducts.put(productCode,updateSale);

                                                                            int x = 0;
                                                                            for(Map.Entry<String,Float> entry:saleByProducts.entrySet()){
                                                                                monthEntries.add(new BarEntry(x, entry.getValue()));

                                                                                BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo sản ");

                                                                                BarData data = new BarData(set);

                                                                                Description description = new Description();
                                                                                description.setText("");

                                                                                barTime.getAxisRight().setDrawGridLines(false);
                                                                                barTime.getAxisLeft().setDrawGridLines(false);
                                                                                barTime.getXAxis().setDrawGridLines(false);
                                                                                barTime.getXAxis().setGranularityEnabled(true);
                                                                                //barTime.getXAxis().setDrawLabels(false);
                                                                                barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                                                barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                                    @Override
                                                                                    public String getFormattedValue(float value) {
                                                                                        return xLabels.get((int) value);                                            }
                                                                                });

                                                                                barTime.setDescription(description);
                                                                                barTime.getAxisRight().setEnabled(false);
                                                                                barTime.setTouchEnabled(true);
                                                                                //barTime.setMarker(mv);
                                                                                barTime.setData(data);
                                                                                barTime.animateXY(1000,2000);
                                                                                barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                                                                barTime.invalidate(); // refreshx

                                                                                x++;
                                                                            }


                                                                        }else{
                                                                            saleByProducts.put(productCode, saleValue);

                                                                            int x = 0;
                                                                            for(Map.Entry<String,Float> entry:saleByProducts.entrySet()){
                                                                                monthEntries.add(new BarEntry(x, entry.getValue()));

                                                                                BarDataSet set = new BarDataSet(monthEntries,"Doanh số tháng này theo nhân viên");

                                                                                BarData data = new BarData(set);

                                                                                Description description = new Description();
                                                                                description.setText("");

                                                                                barTime.getAxisRight().setDrawGridLines(false);
                                                                                barTime.getAxisLeft().setDrawGridLines(false);
                                                                                barTime.getXAxis().setDrawGridLines(false);
                                                                                barTime.getXAxis().setGranularityEnabled(true);
                                                                                //barTime.getXAxis().setDrawLabels(false);
                                                                                barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                                                barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                                    @Override
                                                                                    public String getFormattedValue(float value) {
                                                                                        return xLabels.get((int) value);                                            }
                                                                                });

                                                                                barTime.setDescription(description);
                                                                                barTime.getAxisRight().setEnabled(false);
                                                                                barTime.setTouchEnabled(true);
                                                                                //barTime.setMarker(mv);
                                                                                barTime.setData(data);
                                                                                barTime.animateXY(1000,2000);
                                                                                barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                                                                barTime.invalidate(); // refreshx

                                                                                x++;
                                                                            }

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
                                                    refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.hasChild(year)){
                                                                refProduct.child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        float saleValue = Float.parseFloat(dataSnapshot.getValue().toString());

                                                                        if(saleByProducts.containsKey(productCode)){
                                                                            float currentSale = saleByProducts.get(productCode);
                                                                            float updateSale = currentSale + saleValue;
                                                                            //Toast.makeText(getApplicationContext(), updateSale+"", Toast.LENGTH_LONG).show();
                                                                            saleByProducts.put(productCode,updateSale);

                                                                        }else{
                                                                            saleByProducts.put(productCode, saleValue);

                                                                            refCompany.child("Product").child(productCode).child("productName").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                                @Override
                                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                    xLabels.add(dataSnapshot.getValue().toString());

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
                                                final int finalI = i;



                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                                }else{
                                    refCompany.child("TotalProductBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();

                                            for(DataSnapshot itemProduct:snapProduct){

                                                final String productCode = itemProduct.getKey();
                                                //xLabels.add(productCode);
                                                refCompany.child("Product").child(productCode).child("productName").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        String productName = dataSnapshot.getValue().toString();
                                                        if(!xLabels.contains(productName)){
                                                            xLabels.add(dataSnapshot.getValue().toString());

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                                final DatabaseReference refProduct = itemProduct.getRef();

                                                refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        if(dataSnapshot.hasChild(year)){
                                                            refProduct.child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                @Override
                                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                                    float saleValue = Float.parseFloat(dataSnapshot.getValue().toString());
                                                                    if(saleByProducts.containsKey(productCode)){
                                                                        float currentSale = saleByProducts.get(productCode);
                                                                        float updateSale = currentSale + saleValue;
                                                                        //Toast.makeText(getApplicationContext(), updateSale+"", Toast.LENGTH_LONG).show();
                                                                        saleByProducts.put(productCode,updateSale);


                                                                    }else{
                                                                        saleByProducts.put(productCode, saleValue);


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
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }
        });

        btnTopClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                btnTopSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                btnTopClient.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

                refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapSup = dataSnapshot.getChildren();
                        long supCount = dataSnapshot.getChildrenCount();

                        final HashMap<String,Float> saleValues = new HashMap<>();
                        final List<BarEntry> monthEntries = new ArrayList<>();
                        final ArrayList<String> xLabels = new ArrayList<>();

                        int i = 0;
                        for(DataSnapshot itemSup:snapSup){
                            i++;
                            String supEmail = itemSup.getKey();

                            refCompany.child("SaleManBySup").child(supEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                                    long saleCount = dataSnapshot.getChildrenCount();

                                    int i = 0;
                                    for(final DataSnapshot itemSale:snapSale){
                                        i++;
                                        final String saleEmail = itemSale.getKey();
                                        final String saleName = itemSale.getValue(Employee.class).getEmployeeName();
                                        final String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);

                                        refCompany.child("ClientManBySale").child(saleEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                Iterable<DataSnapshot> snapClient = dataSnapshot.getChildren();
                                                for(DataSnapshot itemClient:snapClient){
                                                    final String clientCode = itemClient.getKey();
                                                    refCompany.child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if(dataSnapshot.hasChild(year+"-"+month)){
                                                                refCompany.child("TotalByClient").child(clientCode).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        saleValues.put(clientCode,Float.parseFloat(dataSnapshot.getValue().toString()));
                                                                        sortClientValue = Utils.sortDecreaseByValues(saleValues);

                                                                        Set setSort = sortClientValue.entrySet();

                                                                        int i = 0;
                                                                        for (Object o : setSort) {
                                                                            i++;

                                                                            if (i<=10){
                                                                                Map.Entry me = (Map.Entry) o;
                                                                                String saleName = (String) me.getKey();
                                                                                xLabels.add(saleName);
                                                                                monthEntries.add(new BarEntry(i, (Float) me.getValue(),me.getKey()));

                                                                            }

                                                                        }


                                                                        BarDataSet set = new BarDataSet(monthEntries,"Doanh số TOP tháng này theo khách hàng");

                                                                        BarData data = new BarData(set);

                                                                        Description description = new Description();
                                                                        description.setText("");

                                                                        barTop.getAxisRight().setDrawGridLines(false);
                                                                        barTop.getAxisLeft().setDrawGridLines(false);
                                                                        barTop.getXAxis().setDrawGridLines(false);
                                                                        barTop.getXAxis().setGranularityEnabled(true);
                                                                        //barTime.getXAxis().setDrawLabels(false);
                                                                        barTop.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                                        barTop.setDescription(description);
                                                                        barTop.getAxisRight().setEnabled(false);
                                                                        barTop.setTouchEnabled(true);
                                                                        //barTime.setMarker(mv);
                                                                        barTop.setData(data);
                                                                        barTop.animateXY(1000,2000);
                                                                        barTop.setFitBars(true); // make the x-axis fit exactly all bars
                                                                        barTop.invalidate(); // refresh

                                                                        barTop.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                                                            @Override
                                                                            public void onValueSelected(Entry e, Highlight h) {

                                                                                getClientDetail(e.getData().toString());
                                                                            }

                                                                            @Override
                                                                            public void onNothingSelected() {

                                                                            }
                                                                        });

                                                                    }

                                                                    @Override
                                                                    public void onCancelled(DatabaseError databaseError) {

                                                                    }
                                                                });
                                                            }else{
                                                                sortClientValue = Utils.sortDecreaseByValues(saleValues);
                                                                Toast.makeText(getApplicationContext(), clientCode, Toast.LENGTH_LONG).show();

                                                                Set setSort = sortClientValue.entrySet();

                                                                int i = 0;
                                                                for (Object o : setSort) {
                                                                    i++;

                                                                    if (i<=10){
                                                                        Map.Entry me = (Map.Entry) o;
                                                                        String saleName = (String) me.getKey();
                                                                        xLabels.add(saleName);
                                                                        monthEntries.add(new BarEntry(i, (Float) me.getValue(),me.getKey()));

                                                                    }

                                                                }


                                                                Toast.makeText(getApplicationContext(), sortClientValue.size()+"", Toast.LENGTH_LONG).show();

                                                                BarDataSet set = new BarDataSet(monthEntries,"Doanh số TOP tháng này theo khách hàng");

                                                                BarData data = new BarData(set);

                                                                Description description = new Description();
                                                                description.setText("");

                                                                barTop.getAxisRight().setDrawGridLines(false);
                                                                barTop.getAxisLeft().setDrawGridLines(false);
                                                                barTop.getXAxis().setDrawGridLines(false);
                                                                barTop.getXAxis().setGranularityEnabled(true);
                                                                //barTime.getXAxis().setDrawLabels(false);
                                                                barTop.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                                                barTop.setDescription(description);
                                                                barTop.getAxisRight().setEnabled(false);
                                                                barTop.setTouchEnabled(true);
                                                                //barTime.setMarker(mv);
                                                                barTop.setData(data);
                                                                barTop.animateXY(1000,2000);
                                                                barTop.setFitBars(true); // make the x-axis fit exactly all bars
                                                                barTop.invalidate(); // refresh

                                                                barTop.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                                                    @Override
                                                                    public void onValueSelected(Entry e, Highlight h) {

                                                                        getClientDetail(e.getData().toString());
                                                                    }

                                                                    @Override
                                                                    public void onNothingSelected() {

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
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        btnTopSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                btnTopClient.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                btnTopSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

                refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapSup = dataSnapshot.getChildren();
                        long supCount = dataSnapshot.getChildrenCount();

                        final HashMap<String,Float> saleValues = new HashMap<>();
                        final List<BarEntry> monthEntries = new ArrayList<>();
                        final ArrayList<String> xLabels = new ArrayList<>();

                        int i = 0;
                        for(DataSnapshot itemSup:snapSup){
                            i++;
                            String supEmail = itemSup.getKey();

                            refCompany.child("SaleManBySup").child(supEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                                    long saleCount = dataSnapshot.getChildrenCount();

                                    int i = 0;
                                    for(final DataSnapshot itemSale:snapSale){
                                        i++;
                                        final String saleEmail = itemSale.getKey();
                                        final String saleName = itemSale.getValue(Employee.class).getEmployeeName();
                                        final String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);


                                        refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild(year+"-"+month)){
                                                    refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            saleValues.put(saleName,Float.parseFloat(dataSnapshot.getValue().toString()));
                                                            sortedSaleValue = Utils.sortDecreaseByValues(saleValues);

                                                            Set setSort = sortedSaleValue.entrySet();

                                                            int i = 0;
                                                            for (Object o : setSort) {
                                                                i++;

                                                                if (i<=10){
                                                                    Map.Entry me = (Map.Entry) o;
                                                                    String saleName = (String) me.getKey();
                                                                    xLabels.add(saleName);
                                                                    monthEntries.add(new BarEntry(i, (Float) me.getValue(),me.getKey()));

                                                                }

                                                            }

                                                            BarDataSet set = new BarDataSet(monthEntries,"Doanh số TOP tháng này theo nhân viên");

                                                            BarData data = new BarData(set);

                                                            Description description = new Description();
                                                            description.setText("");

                                                            barTop.getAxisRight().setDrawGridLines(false);
                                                            barTop.getAxisLeft().setDrawGridLines(false);
                                                            barTop.getXAxis().setDrawGridLines(false);
                                                            barTop.getXAxis().setGranularityEnabled(true);
                                                            //barTime.getXAxis().setDrawLabels(false);
                                                            barTop.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
/*
                                                            barTop.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                @Override
                                                                public String getFormattedValue(float value) {
                                                                    return xLabels.get((int) value);                                            }
                                                            });
*/
                                                            barTop.setDescription(description);
                                                            barTop.getAxisRight().setEnabled(false);
                                                            barTop.setTouchEnabled(true);
                                                            //barTime.setMarker(mv);
                                                            barTop.setData(data);
                                                            barTop.animateXY(1000,2000);
                                                            barTop.setFitBars(true); // make the x-axis fit exactly all bars
                                                            barTop.invalidate(); // refresh

                                                            barTop.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                                                @Override
                                                                public void onValueSelected(Entry e, Highlight h) {
                                                                    Toast.makeText(getApplicationContext(),e.getData().toString(),Toast.LENGTH_LONG).show();
                                                                }

                                                                @Override
                                                                public void onNothingSelected() {

                                                                }
                                                            });




                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }else{
                                                    sortedSaleValue = Utils.sortDecreaseByValues(saleValues);;
                                                    Set setSort = sortedSaleValue.entrySet();

                                                    int i = 0;
                                                    for (Object o : setSort) {
                                                        i++;

                                                        if (i<=10){
                                                            Map.Entry me = (Map.Entry) o;
                                                            String saleName = (String) me.getKey();
                                                            xLabels.add(saleName);
                                                            monthEntries.add(new BarEntry(i, (Float) me.getValue(),me.getKey()));

                                                        }

                                                    }



                                                    BarDataSet set = new BarDataSet(monthEntries,"Doanh số TOP tháng này theo nhân viên");

                                                    BarData data = new BarData(set);

                                                    Description description = new Description();
                                                    description.setText("");

                                                    barTop.getAxisRight().setDrawGridLines(false);
                                                    barTop.getAxisLeft().setDrawGridLines(false);
                                                    barTop.getXAxis().setDrawGridLines(false);
                                                    barTop.getXAxis().setGranularityEnabled(true);
                                                    //barTime.getXAxis().setDrawLabels(false);
                                                    barTop.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
/*
                                                            barTop.getXAxis().setValueFormatter(new IndexAxisValueFormatter() {

                                                                @Override
                                                                public String getFormattedValue(float value) {
                                                                    return xLabels.get((int) value);                                            }
                                                            });
*/
                                                    barTop.setDescription(description);
                                                    barTop.getAxisRight().setEnabled(false);
                                                    barTop.setTouchEnabled(true);
                                                    //barTime.setMarker(mv);
                                                    barTop.setData(data);
                                                    barTop.animateXY(1000,2000);
                                                    barTop.setFitBars(true); // make the x-axis fit exactly all bars
                                                    barTop.invalidate(); // refresh

                                                    barTop.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
                                                        @Override
                                                        public void onValueSelected(Entry e, Highlight h) {
                                                            Toast.makeText(getApplicationContext(),e.getData().toString(),Toast.LENGTH_LONG).show();
                                                        }

                                                        @Override
                                                        public void onNothingSelected() {

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
        });

    }

    private void getClientDetail(final String clientCode) {

        AlertDialog.Builder builder = new AlertDialog.Builder(SaleReport.this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_client_detail,null);
        builder.setView(dialogView);

        Dialog dialog = builder.create();
        dialog.show();

        final BarChart barTime = (BarChart)dialogView.findViewById(R.id.bar_client_detail_sale);

        final TextView tvClientName = dialogView.findViewById(R.id.tv_client_detail_name);
        final TextView tvClientAddress = dialogView.findViewById(R.id.tv_client_detail_address);

        final Button yearSale = dialogView.findViewById(R.id.btn_client_detail_yearsale);
        final Button monthSale = dialogView.findViewById(R.id.btn_client_detail_month_sale);
        final Button thisMonthSale = dialogView.findViewById(R.id.btn_client_detail_thismonth);
        //Button btnExportExcel = dialogView.findViewById(R.id.btn_client_detail_exportExcel);

        ImageView phone = dialogView.findViewById(R.id.btn_client_detail_phone);
        ImageView grouping = dialogView.findViewById(R.id.btn_client_detail_grouping);
        ImageView ivOrder = dialogView.findViewById(R.id.iv_client_detail_order);
        ImageView ivFixLoc = dialogView.findViewById(R.id.iv_client_fix_location);
        ImageView ivSaleRoute = dialogView.findViewById(R.id.iv_client_detail_saleroute);

        ivSaleRoute.setVisibility(View.GONE);
        grouping.setVisibility(View.GONE);
        ivFixLoc.setVisibility(View.GONE);
        ivOrder.setVisibility(View.GONE);

        yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
        monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
        thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));

        refDatabase.child(emailLogin).child("Client").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Client clientInfo = dataSnapshot.getValue(Client.class);
                assert clientInfo != null;
                String clientStreet = clientInfo.getClientStreet();
                String clientProvince = clientInfo.getClientProvince();
                tvClientName.setText(clientInfo.getClientName());
                tvClientAddress.setText(clientStreet + ", " + clientProvince);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        DateTime dt = new DateTime();
        final String month = dt.getMonthOfYear()+"";
        final String year = dt.getYear()+"";

        final List<BarEntry> monthEntries = new ArrayList<>();

        refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                for(DataSnapshot itemTime:snapTimeSale){

                    String timeKey = itemTime.getKey();

                    String value = itemTime.getValue().toString();

                    if(timeKey.length()>7 ){

                        if(timeKey.contains(year+"-"+month)){

                            monthEntries.add(new BarEntry(Integer.parseInt(timeKey.substring(timeKey.lastIndexOf("-")+1)), Float.parseFloat(value)));

                            BarDataSet set = new BarDataSet(monthEntries,"Doanh số theo tháng");

                            BarData data = new BarData(set);

                            Description description = new Description();
                            description.setText("");

                            barTime.getAxisRight().setDrawGridLines(false);
                            barTime.getAxisLeft().setDrawGridLines(false);
                            barTime.getXAxis().setDrawGridLines(false);
                            barTime.getXAxis().setGranularityEnabled(true);
                            //barTime.getXAxis().setDrawLabels(false);
                            barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                            //barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barEntryLabels));
                            barTime.setDescription(description);
                            barTime.getAxisRight().setEnabled(false);
                            barTime.setTouchEnabled(true);
                            //barTime.setMarker(mv);
                            barTime.setData(data);
                            barTime.animateXY(1000,2000);
                            barTime.setFitBars(true); // make the x-axis fit exactly all bars
                            barTime.invalidate(); // refresh

                        }
                        //barEntryLabels.add(timeKey.substring(5));

                    }


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                refDatabase.child(emailLogin).child("Client").child(clientCode).child("clientPhone").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String phone = dataSnapshot.getValue().toString();
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + phone));
                        startActivity(intent);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });



        yearSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);



                yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));

                final List<BarEntry> yearEntries = new ArrayList<>();
                final ArrayList<String> barEntryLabels = new ArrayList<>();;

                refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                        for(DataSnapshot itemTime:snapTimeSale){

                            String timeKey = itemTime.getKey();

                            String value = itemTime.getValue().toString();

                            if(timeKey.length()<5){

                                barEntryLabels.add(timeKey);
                                yearEntries.add(new BarEntry(Integer.parseInt(timeKey), Float.parseFloat(value)));

                                BarDataSet set = new BarDataSet(yearEntries,"Doanh số theo năm");

                                BarData data = new BarData(set);

                                Description description = new Description();
                                description.setText("");

                                barTime.getAxisRight().setDrawGridLines(false);
                                barTime.getAxisLeft().setDrawGridLines(false);
                                barTime.getXAxis().setDrawGridLines(false);
                                barTime.getXAxis().setGranularityEnabled(true);
                                //barTime.getXAxis().setDrawLabels(false);
                                barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                //barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barEntryLabels));
                                barTime.setDescription(description);
                                barTime.getAxisRight().setEnabled(false);
                                barTime.setTouchEnabled(true);
                                //barTime.setMarker(mv);
                                barTime.setData(data);
                                barTime.animateXY(1000,2000);
                                barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                barTime.invalidate(); // refresh

                            }


                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        monthSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));

                final List<BarEntry> monthEntries = new ArrayList<>();

                refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                        for(DataSnapshot itemTime:snapTimeSale){

                            String timeKey = itemTime.getKey();

                            String value = itemTime.getValue().toString();

                            if(timeKey.length()>5 && timeKey.length()<8){


                                //barEntryLabels.add(timeKey.substring(5));

                                monthEntries.add(new BarEntry(Integer.parseInt(timeKey.substring(5)), Float.parseFloat(value)));

                                BarDataSet set = new BarDataSet(monthEntries,"Doanh số theo tháng");

                                BarData data = new BarData(set);

                                Description description = new Description();
                                description.setText("");

                                barTime.getAxisRight().setDrawGridLines(false);
                                barTime.getAxisLeft().setDrawGridLines(false);
                                barTime.getXAxis().setDrawGridLines(false);
                                barTime.getXAxis().setGranularityEnabled(true);
                                //barTime.getXAxis().setDrawLabels(false);
                                barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                //barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barEntryLabels));
                                barTime.setDescription(description);
                                barTime.getAxisRight().setEnabled(false);
                                barTime.setTouchEnabled(true);
                                //barTime.setMarker(mv);
                                barTime.setData(data);
                                barTime.animateXY(1000,2000);
                                barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                barTime.invalidate(); // refresh

                            }


                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        thisMonthSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);

                yearSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                monthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat));
                thisMonthSale.setBackground(getResources().getDrawable(R.drawable.border_drug_cat_accent));
                DateTime dt = new DateTime();
                final String month = dt.getMonthOfYear() + "";
                final String year = dt.getYear() + "";

                final List<BarEntry> monthEntries = new ArrayList<>();

                refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> snapTimeSale = dataSnapshot.getChildren();


                        for (DataSnapshot itemTime : snapTimeSale) {

                            String timeKey = itemTime.getKey();

                            String value = itemTime.getValue().toString();


                            if (timeKey.length() > 7) {

                                if (timeKey.contains(year + "-" + month)) {

                                    monthEntries.add(new BarEntry(Integer.parseInt(timeKey.substring(timeKey.lastIndexOf("-") + 1)), Float.parseFloat(value)));

                                    BarDataSet set = new BarDataSet(monthEntries, "Doanh số theo ngày");

                                    BarData data = new BarData(set);

                                    Description description = new Description();
                                    description.setText("");

                                    barTime.getAxisRight().setDrawGridLines(false);
                                    barTime.getAxisLeft().setDrawGridLines(false);
                                    barTime.getXAxis().setDrawGridLines(false);
                                    barTime.getXAxis().setGranularityEnabled(true);
                                    //barTime.getXAxis().setDrawLabels(false);
                                    barTime.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

                                    //barTime.getXAxis().setValueFormatter(new IndexAxisValueFormatter(barEntryLabels));
                                    barTime.setDescription(description);
                                    barTime.getAxisRight().setEnabled(false);
                                    barTime.setTouchEnabled(true);
                                    //barTime.setMarker(mv);
                                    barTime.setData(data);
                                    barTime.animateXY(1000, 2000);
                                    barTime.setFitBars(true); // make the x-axis fit exactly all bars
                                    barTime.invalidate(); // refresh

                                }
                                //barEntryLabels.add(timeKey.substring(5));

                            }


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


package vn.techlifegroup.wesell.bytask.distribution;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.utils.Constants;

public class FilterProductActivity extends AppCompatActivity {
    private String choosenYear, choosenMonth,choosenQuarter,choosenProduct,emailLogin;
    private int choosenYearInt, choosenMonthInt, choosenQuarterInt;
    private DatabaseReference refProductMonth;
    private TextView thisMonthRev, thisYearRev, thisQuarterRev, orderCount,thisMonthTotal,thisQuarterTotal,thisYearTotal,thisMonthTotalPromotion,thisQuarterTotalPromotion,thisYearTotalPromotion,whatMonth,whatQuarter,whatYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter_product);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_filter_order_product);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();
        choosenYear = intent.getStringExtra("ChoosenYear");
        choosenMonth = intent.getStringExtra("ChoosenMonth");
        choosenProduct = intent.getStringExtra("ChoosenProduct");

        emailLogin = intent.getStringExtra("EmailLogin");

        thisMonthRev = (TextView)findViewById(R.id.tv_filter_this_month_product_revenue);
        thisYearRev = (TextView)findViewById(R.id.tv_filter_this_year_product_revenue);
        thisQuarterRev = (TextView)findViewById(R.id.tv_filter_this_quarter_product_revenue);
        thisMonthTotal = (TextView)findViewById(R.id.tv_filter_this_month_product_total);
        thisQuarterTotal = (TextView)findViewById(R.id.tv_filter_this_quarter_product_total);
        thisYearTotal = (TextView)findViewById(R.id.tv_filter_this_year_product_total);
        thisMonthTotalPromotion = (TextView)findViewById(R.id.tv_filter_this_month_promotion_total);
        thisQuarterTotalPromotion = (TextView)findViewById(R.id.tv_filter_this_quarter_promotion_total);
        thisYearTotalPromotion = (TextView)findViewById(R.id.tv_filter_this_year_promotion_total);
        orderCount = (TextView)findViewById(R.id.tv_filter_order_count_product);

        if(choosenMonth.equals("1") || choosenMonth.equals("2") || choosenMonth.equals("3")){
            choosenQuarter = "1";
        }else if (choosenMonth.equals("4") || choosenMonth.equals("5") || choosenMonth.equals("6")){
            choosenQuarter = "2";
        }else if (choosenMonth.equals("7") || choosenMonth.equals("8") || choosenMonth.equals("9")){
            choosenQuarter = "3";
        }else{
            choosenQuarter = "4";
        }

        whatMonth = (TextView)findViewById(R.id.tv_filter_product_what_month);
        whatQuarter = (TextView)findViewById(R.id.tv_filter_product_what_quarter);
        whatYear = (TextView)findViewById(R.id.tv_filter_product_what_year);

        whatMonth.setText(choosenMonth);
        whatQuarter.setText(choosenQuarter);
        whatYear.setText(choosenYear);


        Constants.refDatabase.child(emailLogin+"/ProductQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(choosenProduct)){
                    Constants.refDatabase.child(emailLogin+"/ProductQuantity").child(choosenProduct).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(choosenYear)){
                                Constants.refDatabase.child(emailLogin+"/ProductQuantity").child(choosenProduct).child(choosenYear).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String productYearTotal = dataSnapshot.getValue().toString();
                                        thisYearTotal.setText(productYearTotal);

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            if(dataSnapshot.hasChild(choosenYear+choosenQuarter)){
                                Constants.refDatabase.child(emailLogin+"/ProductQuantity").child(choosenProduct).child(choosenYear+choosenQuarter).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String productQuarterTotal = dataSnapshot.getValue().toString();
                                        thisQuarterTotal.setText(productQuarterTotal);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            if(dataSnapshot.hasChild(choosenYear+choosenQuarter+choosenMonth)){
                                Constants.refDatabase.child(emailLogin+"/ProductQuantity").child(choosenProduct).child(choosenYear+choosenQuarter+choosenMonth).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String productMonthTotal = dataSnapshot.getValue().toString();
                                        thisMonthTotal.setText(productMonthTotal);
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

        Constants.refDatabase.child(emailLogin+"/PromotionQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(choosenProduct)){
                    Constants.refDatabase.child(emailLogin+"/PromotionQuantity").child(choosenProduct).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(choosenYear)){
                                Constants.refDatabase.child(emailLogin+"/PromotionQuantity").child(choosenProduct).child(choosenYear).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String productYearTotal = dataSnapshot.getValue().toString();
                                        thisYearTotalPromotion.setText(productYearTotal);

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            if(dataSnapshot.hasChild(choosenYear+choosenQuarter)){
                                Constants.refDatabase.child(emailLogin+"/PromotionQuantity").child(choosenProduct).child(choosenYear+choosenQuarter).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String productQuarterTotal = dataSnapshot.getValue().toString();
                                        thisQuarterTotalPromotion.setText(productQuarterTotal);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            if(dataSnapshot.hasChild(choosenYear+choosenQuarter+choosenMonth)){
                                Constants.refDatabase.child(emailLogin+"/PromotionQuantity").child(choosenProduct).child(choosenYear+choosenQuarter+choosenMonth).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String productMonthTotal = dataSnapshot.getValue().toString();
                                        thisMonthTotalPromotion.setText(productMonthTotal);
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

        Constants.refDatabase.child(emailLogin+"/TotalByProduct").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(choosenProduct)){
                    Constants.refDatabase.child(emailLogin+"/TotalByProduct").child(choosenProduct).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(choosenYear)){
                                Constants.refDatabase.child(emailLogin+"/TotalByProduct").child(choosenProduct).child(choosenYear).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String thisYearRevenue = dataSnapshot.getValue().toString();
                                        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
                                        float thisYearRevenueFloat = Float.parseFloat(thisYearRevenue);
                                        String thisYearRevenueFloatConverted = numberFormat.format(thisYearRevenueFloat);
                                        thisYearRev.setText(thisYearRevenueFloatConverted);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            if(dataSnapshot.hasChild(choosenYear+choosenQuarter)){
                                Constants.refDatabase.child(emailLogin+"/TotalByProduct").child(choosenProduct).child(choosenYear+choosenQuarter).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String thisQuarterRevenue = dataSnapshot.getValue().toString();
                                        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
                                        float thisQuarterRevenueFloat = Float.parseFloat(thisQuarterRevenue);
                                        String thisQuarterRevenueFloatConverted = numberFormat.format(thisQuarterRevenueFloat);
                                        thisQuarterRev.setText(thisQuarterRevenueFloatConverted);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
                            if(dataSnapshot.hasChild(choosenYear+choosenQuarter+choosenMonth)){
                                Constants.refDatabase.child(emailLogin+"/TotalByProduct").child(choosenProduct).child(choosenYear+choosenQuarter+choosenMonth).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String thisMonthRevenue = dataSnapshot.getValue().toString();
                                        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
                                        float thisMonthRevenueFloat = Float.parseFloat(thisMonthRevenue);
                                        String thisMonthRevenueFloatConverted = numberFormat.format(thisMonthRevenueFloat);
                                        thisMonthRev.setText(thisMonthRevenueFloatConverted);
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

        refProductMonth = Constants.refDatabase.child(emailLogin+"/ProductOrder").child(choosenProduct).child(choosenYear+choosenQuarter+choosenMonth);
        refProductMonth.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long snapOrderCount = dataSnapshot.getChildrenCount();
                orderCount.setText(snapOrderCount+"");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter_product,menu);
        return super.onCreateOptionsMenu(menu);
    }
     */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            this.onBackPressed();                }
        return super.onOptionsItemSelected(item);
    }
}

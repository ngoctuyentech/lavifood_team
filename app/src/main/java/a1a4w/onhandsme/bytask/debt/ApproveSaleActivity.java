package a1a4w.onhandsme.bytask.debt;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.OrderDetail;
import a1a4w.onhandsme.model.Product;
import a1a4w.onhandsme.model.VatModel;
import a1a4w.onhandsme.utils.Constants;
import a1a4w.onhandsme.utils.Utils;

import static a1a4w.onhandsme.utils.Constants.refOrderList;

public class ApproveSaleActivity extends AppCompatActivity {

    private RecyclerView recyclerViewProduct, recyclerViewPromotion;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct,adapterFirebasePromotion;
    private DatabaseReference refProduct, refPromotion;

    private TextView tvClientName, tvClientType, tvPayment, tvDelivery, tvNotVAT, tvVAT,tvFinalPayment,tvClientDebt,tvNote;
    private String orderPushKey,orderType;
    private Bundle b = new Bundle();
    private int thisMonth, thisYear, thisDate,thisQuater;
    private String thisMonthString, thisYearString, thisDateString, thisQuarterString,emailLogin;
    private ProgressDialog mProgressDialog;
    private OrderDetail orderDetail;
    private String clientCode,clientName,clientType,paymentType;
    private String employeeCode;
    private String clientDebt,clientPayment;
    private String totalByClientYear,totalByClientQuarter,totalByClientMonth;

    private String totalByEmployeeYear,totalByEmployeeMonth,totalByEmployeeQuarter;
    private String totalByTimeYear,totalByTimeQuarter,totalByTimeMonth,totalByTimeDate;
    private float VAT,notVAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null){
            b.clear();
        }
        setContentView(R.layout.activity_appprove_sale);

        Intent intent = this.getIntent();
        orderPushKey = intent.getStringExtra("OrderPushKey");
        orderType = intent.getStringExtra("OrderType");
        emailLogin = intent.getStringExtra("EmailLogin");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_approve_sale);
        setSupportActionBar(toolbar);

        initializeScreen();

        initializeRecyclerViewProduct();
        initializeRecyclerViewPromotion();

        viewVAT();
        viewOtherInformation();
        //getProductData();
       // getCurrentTotalByTime();
    }


    //Data for view
    private void getProductSale() {

        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("Promotion")){
                    Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> promotionSnap = dataSnapshot.getChildren();
                            int i = 0;
                            for(DataSnapshot promotion:promotionSnap){
                                final int y = i;
                                Product p = promotion.getValue(Product.class);
                                final String promotionName = p.getProductName();
                                String promotionQuantity = p.getUnitQuantity();
                                b.putString("PromotionName"+i,promotionName);
                                b.putString("PromotionQuantity"+i,promotionQuantity);

                                Constants.refDatabase.child(emailLogin).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild("PromotionQuantity")){
                                            Constants.refDatabase.child(emailLogin).child("PromotionQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(promotionName)){
                                                        Constants.refDatabase.child(emailLogin).child("PromotionQuantity").child(promotionName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                if(dataSnapshot.hasChild(thisYearString)){
                                                                    Constants.refDatabase.child(emailLogin).child("PromotionQuantity").child(promotionName).child(thisYearString).runTransaction(new Transaction.Handler() {
                                                                        @Override
                                                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                                                            String promotionQuantityYear = mutableData.getValue().toString();
                                                                            b.putString("PromotionQuantityYear"+y,promotionQuantityYear);

                                                                            return Transaction.success(mutableData);
                                                                        }

                                                                        @Override
                                                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                                        }
                                                                    });
                                                                }else{
                                                                    b.putString("PromotionQuantityYear"+y,"0");
                                                                }
                                                                if(dataSnapshot.hasChild(thisYearString+thisQuarterString)){
                                                                    Constants.refDatabase.child(emailLogin).child("PromotionQuantity").child(promotionName).child(thisYearString+thisQuarterString).runTransaction(new Transaction.Handler() {
                                                                        @Override
                                                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                                                            String promotionQuantityQuarter = mutableData.getValue().toString();
                                                                            b.putString("PromotionQuantityQuarter"+y,promotionQuantityQuarter);
                                                                            return Transaction.success(mutableData);
                                                                        }

                                                                        @Override
                                                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                                        }
                                                                    });
                                                                }else{
                                                                    b.putString("PromotionQuantityQuarter"+y,"0");
                                                                }
                                                                if(dataSnapshot.hasChild(thisYearString+thisQuarterString+thisMonthString)){
                                                                    Constants.refDatabase.child(emailLogin).child("PromotionQuantity").child(promotionName).child(thisYearString+thisQuarterString+thisMonthString).runTransaction(new Transaction.Handler() {
                                                                        @Override
                                                                        public Transaction.Result doTransaction(MutableData mutableData) {
                                                                            String promotionQuantityMonth = mutableData.getValue().toString();
                                                                            b.putString("PromotionQuantityMonth"+y,promotionQuantityMonth);
                                                                            return Transaction.success(mutableData);
                                                                        }

                                                                        @Override
                                                                        public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                                        }
                                                                    });
                                                                }else{
                                                                    b.putString("PromotionQuantityMonth"+y,"0");
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }else{
                                                        b.putString("PromotionQuantityYear"+y,"0");
                                                        b.putString("PromotionQuantityQuarter"+y,"0");
                                                        b.putString("PromotionQuantityMonth"+y,"0");
                                                    }

                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }else{
                                            b.putString("PromotionQuantityYear"+y,"0");
                                            b.putString("PromotionQuantityQuarter"+y,"0");
                                            b.putString("PromotionQuantityMonth"+y,"0");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                i++;
                                b.putInt("ForIntValuePromotion",i);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else{
                    b.putInt("ForIntValuePromotion",-1);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> productSnap = dataSnapshot.getChildren();
                int i=0;
                for(DataSnapshot product:productSnap){
                    final int y = i;
                    Product p = product.getValue(Product.class);
                    final String productName = p.getProductName();
                    b.putString("ProductName"+i,productName);
                    String productPayment = p.getFinalPayment();
                    b.putString("ProductPayment"+i,productPayment);
                    final String producQuantity = p.getUnitQuantity();
                    b.putString("ProductQuantity"+i,producQuantity);

                    //TotalByProduct
                    Constants.refDatabase.child(emailLogin).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("TotalByProduct")){
                                Constants.refDatabase.child(emailLogin).child("TotalByProduct").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(productName)){
                                            Constants.refDatabase.child(emailLogin).child("TotalByProduct").child(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(thisYearString)){
                                                        Constants.refDatabase.child(emailLogin).child("TotalByProduct").child(productName).child(thisYearString).addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                String currentProductSale = dataSnapshot.getValue().toString();
                                                                b.putString("TotalByProductYear"+y,currentProductSale);

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }else{
                                                        b.putString("TotalByProductYear"+y,"0");
                                                    }
                                                    if(dataSnapshot.hasChild(thisYearString+thisQuarterString)){
                                                        Constants.refDatabase.child(emailLogin).child("TotalByProduct").child(productName).child(thisYearString+thisQuarterString).addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                String currentProductSale = dataSnapshot.getValue().toString();
                                                                b.putString("TotalByProductQuarter"+y,currentProductSale);
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }else{
                                                        b.putString("TotalByProductQuarter"+y,"0");
                                                    }
                                                    if(dataSnapshot.hasChild(thisYearString+thisQuarterString+thisMonthString)){
                                                        Constants.refDatabase.child(emailLogin).child("TotalByProduct").child(productName).child(thisYearString+thisQuarterString+thisMonthString).addValueEventListener(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                String currentProductSale = dataSnapshot.getValue().toString();
                                                                b.putString("TotalByProductMonth"+y,currentProductSale);
                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });
                                                    }else{
                                                        b.putString("TotalByProductMonth"+y,"0");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {
                                                }
                                            });
                                        }else{
                                            b.putString("TotalByProductYear"+y,"0");
                                            b.putString("TotalByProductQuarter"+y,"0");
                                            b.putString("TotalByProductMonth"+y,"0");
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }else{
                                b.putString("TotalByProductYear"+y,"0");
                                b.putString("TotalByProductQuarter"+y,"0");
                                b.putString("TotalByProductMonth"+y,"0");

                            }


                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //ProductQuantity
                    Constants.refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("ProductQuantity")){
                                Constants.refDatabase.child(emailLogin).child("ProductQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(productName)){
                                            Constants.refDatabase.child(emailLogin).child("ProductQuantity").child(productName).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {

                                                    if(dataSnapshot.hasChild(thisYearString)){
                                                        Constants.refDatabase.child(emailLogin).child("ProductQuantity").child(productName).child(thisYearString).runTransaction(new Transaction.Handler() {
                                                            @Override
                                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                                String productQuantityYear = mutableData.getValue().toString();
                                                                b.putString("ProductQuantityYear"+y,productQuantityYear);
                                                                return Transaction.success(mutableData);
                                                            }

                                                            @Override
                                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                            }
                                                        });
                                                    }else{
                                                        b.putString("ProductQuantityYear"+y,"0");
                                                    }
                                                    if(dataSnapshot.hasChild(thisYearString+thisQuarterString)){
                                                        Constants.refDatabase.child(emailLogin).child("ProductQuantity").child(productName).child(thisYearString+thisQuarterString).runTransaction(new Transaction.Handler() {
                                                            @Override
                                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                                String productQuantityQuarter = mutableData.getValue().toString();
                                                                b.putString("ProductQuantityQuarter"+y,productQuantityQuarter);
                                                                return Transaction.success(mutableData);
                                                            }

                                                            @Override
                                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                            }
                                                        });
                                                    }else{
                                                        b.putString("ProductQuantityQuarter"+y,"0");
                                                    }
                                                    if(dataSnapshot.hasChild(thisYearString+thisQuarterString+thisMonthString)){
                                                        Constants.refDatabase.child(emailLogin).child("ProductQuantity").child(productName).child(thisYearString+thisQuarterString+thisMonthString).runTransaction(new Transaction.Handler() {
                                                            @Override
                                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                                String productQuantityMonth = mutableData.getValue().toString();
                                                                b.putString("ProductQuantityMonth"+y,productQuantityMonth);

                                                                return Transaction.success(mutableData);
                                                            }

                                                            @Override
                                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                                            }
                                                        });
                                                    }else{
                                                        b.putString("ProductQuantityMonth"+y,"0");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }else{
                                            b.putString("ProductQuantityYear"+y,"0");
                                            b.putString("ProductQuantityQuarter"+y,"0");
                                            b.putString("ProductQuantityMonth"+y,"0");
                                        }


                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                b.putString("ProductQuantityYear"+y,"0");
                                b.putString("ProductQuantityQuarter"+y,"0");
                                b.putString("ProductQuantityMonth"+y,"0");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                    i++;
                    b.putInt("ForIntValueProduct",i);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void getTotalByTime() {

        Constants.refDatabase.child(emailLogin).child("TotalByTime").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(thisYearString)){
                    Constants.refDatabase.child(emailLogin).child("TotalByTime").child(thisYearString).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.hasChild(thisYearString)) {

                                Constants.refDatabase.child(emailLogin).child("TotalByTime")
                                        .child(thisYearString).child(thisYearString).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        totalByTimeYear = dataSnapshot.getValue().toString();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            }else{
                                totalByTimeYear = "0";
                            }

                            if(dataSnapshot.hasChild(thisYearString+thisQuarterString)){
                                Constants.refDatabase.child(emailLogin).child("TotalByTime")
                                        .child(thisYearString).child(thisYearString+thisQuarterString).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        totalByTimeQuarter = dataSnapshot.getValue().toString();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            }else{
                                totalByTimeQuarter= "0";

                            }

                            if(dataSnapshot.hasChild(thisYearString+thisQuarterString+thisMonthString)){
                                Constants.refDatabase.child(emailLogin).child("TotalByTime")
                                        .child(thisYearString).child(thisYearString+thisQuarterString+thisMonthString).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        totalByTimeMonth = dataSnapshot.getValue().toString();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            }else{
                                totalByTimeMonth= "0";

                            }if(dataSnapshot.hasChild(thisYearString+thisQuarterString+thisMonthString+thisDateString)){

                                Constants.refDatabase.child(emailLogin).child("TotalByTime")
                                        .child(thisYearString).child(thisYearString+thisQuarterString+thisMonthString+thisDateString).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        totalByTimeDate = dataSnapshot.getValue().toString();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });


                            }else{
                                totalByTimeDate = "0";

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else{
                    totalByTimeYear = "0";
                    totalByTimeQuarter = "0";
                    totalByTimeMonth = "0";
                    totalByTimeDate = "0";
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private void viewOtherInformation() {
        Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                orderDetail = dataSnapshot.getValue(OrderDetail.class);
                if(orderDetail!=null){
                    clientName = orderDetail.getOrderName();
                    clientType = orderDetail.getClientType();
                    paymentType = orderDetail.getPaymentType();

                    clientCode = orderDetail.getClientCode();

                    String deliveryDate = orderDetail.getDateDelivery();
                    String orderNote = orderDetail.getOrderNote();
                    employeeCode = orderDetail.getEmployeeCode();

                    tvNote.setText(orderNote);
                    tvClientName.setText(clientName);
                    tvClientType.setText(clientType);
                    tvPayment.setText(paymentType);
                    tvDelivery.setText(deliveryDate);

                    //Get ClientDebt
                    Constants.refDatabase.child(emailLogin+"/Client").child(clientCode).child("clientDebt").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            clientDebt = dataSnapshot.getValue().toString();
                            tvClientDebt.setText(Utils.convertNumber(clientDebt));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
             //TotalByEmployee
                    Constants.refDatabase.child(emailLogin).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("TotalByEmployee")){
                                Constants.refDatabase.child(emailLogin).child("TotalByEmployee").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(employeeCode)){
                                            Constants.refDatabase.child(emailLogin).child("TotalByEmployee").child(employeeCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(thisYearString)){

                                                        Constants.refDatabase.child(emailLogin).child("TotalByEmployee")
                                                                .child(employeeCode).child(thisYearString).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                totalByEmployeeYear = dataSnapshot.getValue().toString();

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        totalByEmployeeYear = "0";
                                                    }
                                                    if(dataSnapshot.hasChild(thisYearString+thisQuarterString)){

                                                        Constants.refDatabase.child(emailLogin).child("TotalByEmployee")
                                                                .child(employeeCode).child(thisYearString+thisQuarterString).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                totalByEmployeeQuarter = dataSnapshot.getValue().toString();

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        totalByEmployeeQuarter ="0";
                                                    }
                                                    if(dataSnapshot.hasChild(thisYearString+thisQuarterString+thisMonthString)){

                                                        Constants.refDatabase.child(emailLogin).child("TotalByEmployee")
                                                                .child(employeeCode).child(thisYearString+thisQuarterString+thisMonthString).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                totalByEmployeeMonth = dataSnapshot.getValue().toString();

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        b.putString("TotalByEmployeeMonth","0");
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }else{
                                            totalByEmployeeYear = "0";
                                            totalByEmployeeQuarter ="0";
                                            totalByEmployeeMonth ="0";
                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                totalByEmployeeYear ="0";
                                totalByEmployeeQuarter ="0";
                                totalByEmployeeMonth ="0";
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //TotalByClient
                    Constants.refDatabase.child(emailLogin).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("TotalByClient")){
                                Constants.refDatabase.child(emailLogin).child("TotalByClient").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.hasChild(clientCode)){
                                            Constants.refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.hasChild(thisYearString)){
                                                        Constants.refDatabase.child(emailLogin).child("TotalByClient")
                                                                .child(clientCode).child(thisYearString).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                totalByClientYear = dataSnapshot.getValue().toString();

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        totalByClientYear="0";
                                                    }
                                                    if(dataSnapshot.hasChild(thisYearString+thisQuarterString)){

                                                        Constants.refDatabase.child(emailLogin).child("TotalByClient")
                                                                .child(clientCode).child(thisYearString+thisQuarterString).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                totalByClientQuarter = dataSnapshot.getValue().toString();

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });

                                                    }else{
                                                        totalByClientQuarter="0";
                                                    }
                                                    if(dataSnapshot.hasChild(thisYearString+thisQuarterString+thisMonthString)){

                                                        Constants.refDatabase.child(emailLogin).child("TotalByClient")
                                                                .child(clientCode).child(thisYearString+thisQuarterString+thisMonthString).addListenerForSingleValueEvent(new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                totalByClientMonth = dataSnapshot.getValue().toString();

                                                            }

                                                            @Override
                                                            public void onCancelled(DatabaseError databaseError) {

                                                            }
                                                        });


                                                    }else{
                                                        totalByClientMonth="0";
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }else{
                                            totalByClientYear="0";
                                            totalByClientQuarter="0";
                                            totalByClientMonth="0";
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                totalByClientYear="0";
                                totalByClientQuarter="0";
                                totalByClientMonth="0";
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
    private void viewVAT() {
        refOrderList.child(orderPushKey).child("VAT").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                VatModel currentVat = dataSnapshot.getValue(VatModel.class);
                if(currentVat!=null){
                    notVAT = currentVat.getNotVat();
                    VAT = currentVat.getIncludedVat();
                    //b.putString("NotVAT",currentVat.getNotVat());
                    //b.putString("IncludedVAT",currentVat.getIncludedVat());

                    //String notVATValue = currentVat.getNotVat();
                    tvNotVAT.setText(Utils.convertNumber(notVAT+""));

                    //String vatValue = currentVat.getIncludedVat();
                    tvVAT.setText(Utils.convertNumber(VAT+""));

                    float finalPayment = currentVat.getFinalPayment();
                    tvFinalPayment.setText(Utils.convertNumber(finalPayment+""));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    //Screen
    private void initializeScreen() {
        thisMonth = Calendar.getInstance().get(Calendar.MONTH) +1;
        thisMonthString = thisMonth+"";
        thisYear = Calendar.getInstance().get(Calendar.YEAR);
        thisYearString = thisYear+"";
        thisDate = Calendar.getInstance().get(Calendar.DATE);
        thisDateString = thisDate+"";

        if(thisMonth == 1 || thisMonth == 2 || thisMonth == 3){
            thisQuarterString = "1";
        }else if(thisMonth == 4 || thisMonth == 5 || thisMonth == 6){
            thisQuarterString = "2";
        }else if(thisMonth == 7 || thisMonth == 8 || thisMonth == 9){
            thisQuarterString = "3";

        }else {
            thisQuarterString ="4";
        }
        recyclerViewPromotion = (RecyclerView)findViewById(R.id.recyclerview_order_detail_promotion);
        recyclerViewProduct = (RecyclerView)findViewById(R.id.recycler_order_detail_product);
        tvClientName = (TextView)findViewById(R.id.tv_approve_sale_client_name);
        tvClientType = (TextView)findViewById(R.id.tv_approve_sale_client_type);
        tvPayment = (TextView)findViewById(R.id.tv_approve_sale_payment_type);
        tvDelivery = (TextView)findViewById(R.id.tv_preview_delivery_date);
        tvNotVAT = (TextView)findViewById(R.id.tv_approve_sale_notVAT);
        tvVAT = (TextView)findViewById(R.id.tv_approve_sale_VAT);
        tvFinalPayment = (TextView)findViewById(R.id.tv_approve_sale_final_payment);
        tvClientDebt = (TextView)findViewById(R.id.tv_approve_sale_client_debt);
        tvNote = (TextView)findViewById(R.id.tv_approve_sale_order_note);

        getProductSale();
        getTotalByTime();

    }
    private void initializeRecyclerViewPromotion() {
        recyclerViewPromotion.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewPromotion.setLayoutManager(linearLayoutManager);

        recyclerViewPromotion.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager)recyclerViewPromotion.getLayoutManager()).findFirstVisibleItemPosition();
                recyclerViewPromotion.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });

        refPromotion = Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion");

        adapterFirebasePromotion = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product,
                ProductViewHolder.class,
                refPromotion
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product,parent,false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productPrice.setText(model.getUnitPrice());
                viewHolder.productQuantity.setText(model.getUnitQuantity());
            }
        };

        recyclerViewPromotion.setAdapter(adapterFirebasePromotion);
        adapterFirebasePromotion.notifyDataSetChanged();
    }
    private void initializeRecyclerViewProduct() {
        recyclerViewProduct.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewProduct.setLayoutManager(linearLayoutManager);

        recyclerViewProduct.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager)recyclerViewProduct.getLayoutManager()).findFirstVisibleItemPosition();
                recyclerViewProduct.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });

        refProduct = Constants.refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList");

        adapterFirebaseProduct = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product,
                ProductViewHolder.class,
                refProduct
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product,parent,false);
                return new ProductViewHolder(v);
            }

            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productPrice.setText(model.getUnitPrice());
                viewHolder.productQuantity.setText(model.getUnitQuantity());
            }
        };

        recyclerViewProduct.setAdapter(adapterFirebaseProduct);
        adapterFirebaseProduct.notifyDataSetChanged();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView)itemView.findViewById(R.id.tv_item_product_name);
            productPrice = (TextView)itemView.findViewById(R.id.tv_item_product_price);
            productQuantity = (TextView) itemView.findViewById(R.id.tv_item_product_quantity);

        }
    }

    //Utils
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_approve,menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id==R.id.action_approve_sale) {
            showProgressDialog();

            int loopPromotion=0;
            int loopProduct=0;

            int forIntValuePromotion = b.getInt("ForIntValuePromotion",0);
            int forIntValueProduct = b.getInt("ForIntValueProduct",0);
            //Check if promotion
            if(forIntValuePromotion == 0 || forIntValuePromotion == -1){
                Toast.makeText(getApplicationContext(),"Đơn hàng không có khuyến mãi",Toast.LENGTH_LONG).show();

            }else{
                //Record promotion information
                for(int i = 0;i<forIntValuePromotion;i++){
                    String promotionQuantity = b.getString("PromotionQuantity"+i);
                    String promotionName = b.getString("PromotionName"+i);
                    String promotionQuantityYear = b.getString("PromotionQuantityYear"+i);
                    String promotionQuantityQuarter = b.getString("PromotionQuantityQuarter"+i);
                    String promotionQuantityMonth = b.getString("PromotionQuantityMonth"+i);

                    if(promotionQuantity == null){
                        Toast.makeText(getApplicationContext(),"promotionQuantity",Toast.LENGTH_LONG).show();

                    } else if(promotionName == null){
                        Toast.makeText(getApplicationContext(),"promotionName",Toast.LENGTH_LONG).show();

                    } else if(promotionQuantityYear == null){
                        Toast.makeText(getApplicationContext(),"promotionQuantityYear",Toast.LENGTH_LONG).show();

                    } else if(promotionQuantityQuarter == null){
                        Toast.makeText(getApplicationContext(),"promotionQuantityQuarter",Toast.LENGTH_LONG).show();

                    } else if(promotionQuantityMonth == null){
                        //showProgressDialog();
                        Toast.makeText(getApplicationContext(),"promotionQuantityMonth",Toast.LENGTH_LONG).show();

                    }else{
                        float updatePromotionQuantityYear = Float.parseFloat(promotionQuantity)+Float.parseFloat(promotionQuantityYear);
                        float updatePromotionQuantityQuarter = Float.parseFloat(promotionQuantity)+Float.parseFloat(promotionQuantityQuarter);
                        float updatePromotionQuantityMonth = Float.parseFloat(promotionQuantity)+Float.parseFloat(promotionQuantityMonth);

                        Constants.refDatabase.child(emailLogin).child("PromotionQuantity").child(promotionName).child(thisYearString).setValue(updatePromotionQuantityYear+"");
                        Constants.refDatabase.child(emailLogin).child("PromotionQuantity").child(promotionName).child(thisYearString+thisQuarterString).setValue(updatePromotionQuantityQuarter+"");
                        Constants.refDatabase.child(emailLogin).child("PromotionQuantity").child(promotionName).child(thisYearString+thisQuarterString+thisMonthString).setValue(updatePromotionQuantityMonth+"");

                    }

                    loopPromotion++;

                }
            }

            for(int i=0;i<forIntValueProduct;i++){
                String productQuantity = b.getString("ProductQuantity"+i);
                String productName = b.getString("ProductName"+i);
                String productPayment = b.getString("ProductPayment"+i);
                String productQuantityYear = b.getString("ProductQuantityYear"+i);
                String productQuantityQuarter = b.getString("ProductQuantityQuarter"+i);
                String productQuantityMonth = b.getString("ProductQuantityMonth"+i);
                String totalByProductYear = b.getString("TotalByProductYear"+i);
                String totalByProductQuarter = b.getString("TotalByProductQuarter"+i);
                String totalByProductMonth = b.getString("TotalByProductMonth"+i);

                // Toast.makeText(getApplicationContext(),productName,Toast.LENGTH_SHORT).show();
                //  Toast.makeText(getApplicationContext(),productName+"productQuantity"+productQuantity,Toast.LENGTH_SHORT).show();
                // Toast.makeText(getApplicationContext(),productName+"productQuantityYear"+productQuantityYear,Toast.LENGTH_SHORT).show();

                if(productName == null){
                    Toast.makeText(getApplicationContext(),"productName",Toast.LENGTH_LONG).show();

                } else if(productQuantity == null){
                    Toast.makeText(getApplicationContext(),"productQuantity",Toast.LENGTH_LONG).show();

                }else if(productPayment == null){
                    Toast.makeText(getApplicationContext(),"productPayment",Toast.LENGTH_LONG).show();

                } else if(productQuantityYear == null){
                    Toast.makeText(getApplicationContext(),"productQuantityYear",Toast.LENGTH_LONG).show();

                }else if(productQuantityQuarter == null){
                    Toast.makeText(getApplicationContext(),"productQuantityQuarter",Toast.LENGTH_LONG).show();

                }else if(productQuantityMonth == null){
                    Toast.makeText(getApplicationContext(),"productQuantityMonth",Toast.LENGTH_LONG).show();

                }else if(totalByProductYear == null){
                    Toast.makeText(getApplicationContext(),"totalByProductYear",Toast.LENGTH_LONG).show();

                }else if(totalByProductQuarter == null){
                    Toast.makeText(getApplicationContext(),"totalByProductQuarter",Toast.LENGTH_LONG).show();

                }else if(totalByProductMonth == null){
                    // showProgressDialog();
                    Toast.makeText(getApplicationContext(),"totalByProductMonth",Toast.LENGTH_LONG).show();

                }else{

                    float updateProductQuantityYear = Float.parseFloat(productQuantity) + Float.parseFloat(productQuantityYear);
                    float updateProductQuantityQuarter = Float.parseFloat(productQuantity) + Float.parseFloat(productQuantityQuarter);
                    float updateProductQuantityMonth = Float.parseFloat(productQuantity) + Float.parseFloat(productQuantityMonth);

                    Constants.refDatabase.child(emailLogin).child("ProductQuantity").child(productName).child(thisYearString).setValue(updateProductQuantityYear+"");
                    Constants.refDatabase.child(emailLogin).child("ProductQuantity").child(productName).child(thisYearString+thisQuarterString).setValue(updateProductQuantityQuarter+"");
                    Constants.refDatabase.child(emailLogin).child("ProductQuantity").child(productName).child(thisYearString+thisQuarterString+thisMonthString).setValue(updateProductQuantityMonth+"");

                    float updateTotalByProductYear = Float.parseFloat(productPayment) + Float.parseFloat(totalByProductYear);
                    float updateTotalByProductQuarter = Float.parseFloat(productPayment) + Float.parseFloat(totalByProductQuarter);
                    float updateTotalByProductMonth = Float.parseFloat(productPayment) + Float.parseFloat(totalByProductMonth);

                    Constants.refDatabase.child(emailLogin).child("TotalByProduct").child(productName).child(thisYearString).setValue(updateTotalByProductYear+"");
                    Constants.refDatabase.child(emailLogin).child("TotalByProduct").child(productName).child(thisYearString+thisQuarterString).setValue(updateTotalByProductQuarter+"");
                    Constants.refDatabase.child(emailLogin).child("TotalByProduct").child(productName).child(thisYearString+thisQuarterString+thisMonthString).setValue(updateTotalByProductMonth+"");

                    Constants.refDatabase.child(emailLogin).child("ProductOrder").child(productName).child(thisYearString).child(orderPushKey).setValue(orderDetail);
                    Constants.refDatabase.child(emailLogin).child("ProductOrder").child(productName).child(thisYearString+thisQuarterString).child(orderPushKey).setValue(orderDetail);
                    Constants.refDatabase.child(emailLogin).child("ProductOrder").child(productName).child(thisYearString+thisQuarterString+thisMonthString).child(orderPushKey).setValue(orderDetail);

                }
                loopProduct++;


            }

            if(loopPromotion < (forIntValuePromotion)){
                Toast.makeText(getApplicationContext(),"in forpromotion",Toast.LENGTH_LONG).show();
            }else if(loopProduct<(forIntValueProduct)){
                Toast.makeText(getApplicationContext(),"in forproduct",Toast.LENGTH_LONG).show();
            }else{
                float updateTotalByClientYear = Float.parseFloat(clientPayment) + Float.parseFloat(totalByClientYear);
                float updateTotalByClientQuarter = Float.parseFloat(clientPayment) + Float.parseFloat(totalByClientQuarter);
                float updateTotalByClientMonth = Float.parseFloat(clientPayment) + Float.parseFloat(totalByClientMonth);

                Constants.refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).child(thisYearString).setValue(updateTotalByClientYear+"");
                Constants.refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).child(thisYearString+thisQuarterString).setValue(updateTotalByClientQuarter+"");
                Constants.refDatabase.child(emailLogin).child("TotalByClient").child(clientCode).child(thisYearString+thisQuarterString+thisMonthString).setValue(updateTotalByClientMonth+"");

                Constants.refDatabase.child(emailLogin).child("ClientOrder").child(clientCode).child(thisYearString).child(orderPushKey).setValue(orderDetail);
                Constants.refDatabase.child(emailLogin).child("ClientOrder").child(clientCode).child(thisYearString+thisQuarterString).child(orderPushKey).setValue(orderDetail);
                Constants.refDatabase.child(emailLogin).child("ClientOrder").child(clientCode).child(thisYearString+thisQuarterString+thisMonthString).child(orderPushKey).setValue(orderDetail);

                float updateTotalByEmployeeYear = Float.parseFloat(clientPayment) + Float.parseFloat(totalByEmployeeYear);
                float updateTotalByEmployeeQuarter = Float.parseFloat(clientPayment) + Float.parseFloat(totalByEmployeeQuarter);
                float updateTotalByEmployeeMonth = Float.parseFloat(clientPayment) + Float.parseFloat(totalByEmployeeMonth);

                Constants.refDatabase.child(emailLogin).child("TotalByEmployee").child(employeeCode).child(thisYearString).setValue(updateTotalByEmployeeYear+"");
                Constants.refDatabase.child(emailLogin).child("TotalByEmployee").child(employeeCode).child(thisYearString+thisQuarterString).setValue(updateTotalByEmployeeQuarter+"");
                Constants.refDatabase.child(emailLogin).child("TotalByEmployee").child(employeeCode).child(thisYearString+thisQuarterString+thisMonthString).setValue(updateTotalByEmployeeMonth+"");

                Constants.refDatabase.child(emailLogin).child("EmployeeOrder").child(employeeCode).child(thisYearString).child(orderPushKey).setValue(orderDetail);
                Constants.refDatabase.child(emailLogin).child("EmployeeOrder").child(employeeCode).child(thisYearString+thisQuarterString).child(orderPushKey).setValue(orderDetail);
                Constants.refDatabase.child(emailLogin).child("EmployeeOrder").child(employeeCode).child(thisYearString+thisQuarterString+thisMonthString).child(orderPushKey).setValue(orderDetail);

                float updateTotalByTimeYear = Float.parseFloat(clientPayment) + Float.parseFloat(totalByTimeYear);
                float updateTotalByTimeQuarter = Float.parseFloat(clientPayment) + Float.parseFloat(totalByTimeQuarter);
                float updateTotalByTimeMonth = Float.parseFloat(clientPayment) + Float.parseFloat(totalByTimeMonth);
                float updateTotalByTimeDate = Float.parseFloat(clientPayment) + Float.parseFloat(totalByTimeDate);

                Constants.refDatabase.child(emailLogin).child("TotalByTime").child(thisYearString).child(thisYearString).setValue(updateTotalByTimeYear+"");
                Constants.refDatabase.child(emailLogin).child("TotalByTime").child(thisYearString).child(thisYearString+thisQuarterString).setValue(updateTotalByTimeQuarter+"");
                Constants.refDatabase.child(emailLogin).child("TotalByTime").child(thisYearString).child(thisYearString+thisQuarterString+thisMonthString).setValue(updateTotalByTimeMonth+"");
                Constants.refDatabase.child(emailLogin).child("TotalByTime").child(thisYearString).child(thisYearString+thisQuarterString+thisMonthString+thisDateString).setValue(updateTotalByTimeDate+"");

                Constants.refDatabase.child(emailLogin).child("TimeOrder").child(thisYearString).child(orderPushKey).setValue(orderDetail);
                Constants.refDatabase.child(emailLogin).child("TimeOrder").child(thisYearString+thisQuarterString).child(orderPushKey).setValue(orderDetail);
                Constants.refDatabase.child(emailLogin).child("TimeOrder").child(thisYearString+thisQuarterString+thisMonthString).child(orderPushKey).setValue(orderDetail);
                Constants.refDatabase.child(emailLogin).child("TimeOrder").child(thisYearString+thisQuarterString+thisMonthString+thisDateString).child(orderPushKey).setValue(orderDetail).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if(paymentType.equals("Công nợ")){

                            Constants.refDatabase.child(emailLogin+"/Order/Debt").child(orderPushKey).setValue(null);
                            Constants.refDatabase.child(emailLogin).child("DebtMan").child(orderPushKey).setValue(orderDetail);
                            Constants.refDatabase.child(emailLogin).child("SaleMan").child(orderPushKey).setValue(orderDetail);
                            Constants.refDatabase.child(emailLogin+"/Order/Sale").child(orderPushKey).setValue(orderDetail);

                        }else{
                            Constants.refDatabase.child(emailLogin+"/Order/Money").child(orderPushKey).setValue(null);
                            Constants.refDatabase.child(emailLogin).child("SaleMan").child(orderPushKey).setValue(orderDetail);
                            Constants.refDatabase.child(emailLogin+"/Order/Sale").child(orderPushKey).setValue(orderDetail);

                        }
                        hideProgressDialog();
                        startActivity(new Intent(getApplicationContext(),DebtManActivity.class));
                        Toast.makeText(getApplicationContext(),"Cập nhật thành công", Toast.LENGTH_LONG).show();
                    }
                });
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        b.clear();
        super.onBackPressed();
    }
}

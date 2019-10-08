package a1a4w.onhandsme.bytask;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import java.util.HashMap;

import a1a4w.onhandsme.LoginActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.list.AccountList;
import a1a4w.onhandsme.list.ClientListBySaleTeam;
import a1a4w.onhandsme.list.SaleList;
import a1a4w.onhandsme.model.Employee;

import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class ActionList extends AppCompatActivity {
    private RecyclerView rvAction;
    private FirebaseRecyclerAdapter<Employee,EmployeeViewHolder> adapter ;
    private ImageView ivOrder, ivClient,ivPromotion,ivAnnouncement,ivTeam,
            ivOrderSup,ivPromotionSup,ivAnnoucementSup,ivSaleLogout,ivSupLogout,ivClientSup,
            ivSupASM,ivPromotionASM,ivReportASM,ivAnnouncementASM,ivLogoutASM,ivSaleASM,
            ivCreateAcc,ivLogoutAdmin,ivAdminProduct,ivAdminOrder,ivAdminPayroll,ivAdminPromotion;
    private String emailLogin,userEmail;
    private boolean saleMan,supervisor,asm,admin,rsm;
    private LinearLayout lnSup,lnSaleMan,lnASM,lnAdmin;
    DatabaseReference refCompany;
    private String currentDay,date,day,year,month;
    private TextView tvName,tvRole;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPref;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_list);

        Intent it = this.getIntent();
        emailLogin = it.getStringExtra("EmailLogin");
        saleMan = it.getBooleanExtra("SaleMan", false);
        supervisor = it.getBooleanExtra("Supervisor",false);
        asm = it.getBooleanExtra("ASM",false);
        admin = it.getBooleanExtra("Admin",false);
        rsm = it.getBooleanExtra("RSM", false);

        userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".", ",");
        refCompany = refDatabase.child(emailLogin);

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        //Toast.makeText(getApplicationContext(), userEmail, Toast.LENGTH_LONG).show();
        //pos = sharedPref.getInt("LayoutPosition", 0);

        DateTime dt = new DateTime();
        day = dt.dayOfWeek().getAsText();
        final String week = dt.weekOfWeekyear().getAsText();
        year = dt.getYear()+"";
        month = dt.getMonthOfYear()+"";

         ivOrder = findViewById(R.id.iv_bar_order);
         ivClient = findViewById(R.id.iv_bar_client);
        ivClientSup = findViewById(R.id.iv_bar_client_sup);

        ivPromotion = findViewById(R.id.iv_bar_promotion);
         ivAnnouncement = findViewById(R.id.iv_bar_announcement);
         ivTeam = findViewById(R.id.iv_bar_sale_sup);
         ivOrderSup = findViewById(R.id.iv_bar_order_sup);
         ivPromotionSup = findViewById(R.id.iv_bar_promotion_sup);
         ivAnnoucementSup = findViewById(R.id.iv_bar_announcement_sup);
         ivSaleLogout = findViewById(R.id.iv_bar_sale_logout);
         ivSupLogout = findViewById(R.id.iv_bar_sup_logout);

         ivPromotionASM = findViewById(R.id.iv_bar_promotion_asm);
         ivReportASM = findViewById(R.id.iv_bar_report_asm);
         ivAnnouncementASM = findViewById(R.id.iv_bar_announcement_asm);
         ivLogoutASM = findViewById(R.id.iv_bar_asm_logout);
         ivSupASM = findViewById(R.id.iv_bar_sup_asm);


         ivCreateAcc = findViewById(R.id.iv_bar_account_admin);
         ivLogoutAdmin = findViewById(R.id.iv_bar_admin_logout);
         ivAdminProduct = findViewById(R.id.iv_bar_account_admin_product);
         ivAdminOrder = findViewById(R.id.iv_bar_account_admin_order);
         ivAdminPayroll = findViewById(R.id.iv_bar_account_admin_product_payroll);
         ivAdminPromotion = findViewById(R.id.iv_bar_account_admin_promotion);

         tvName = findViewById(R.id.tv_action_list_account_name);
         tvRole = findViewById(R.id.tv_action_list_account_role);


        lnSup = findViewById(R.id.ln_sup);
         lnSaleMan = findViewById(R.id.ln_sale_man);
        lnASM = findViewById(R.id.ln_asm);
        lnAdmin = findViewById(R.id.ln_admin);

    }

    @Override
    protected void onResume() {
        super.onResume();

        refDatabase.child(emailLogin).child("Employee").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Employee employee = dataSnapshot.getValue(Employee.class);
                tvName.setText(employee.getEmployeeName());
                if(saleMan) tvRole.setText("Bán hàng");
                if(supervisor) tvRole.setText("Giám sát");
                if(asm) tvRole.setText("ASM");
                if(admin) tvRole.setText("Admin");
                if(rsm) tvRole.setText("RSM");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        if(saleMan){
            lnSup.setVisibility(View.GONE);
            lnSaleMan.setVisibility(View.VISIBLE);
            lnASM.setVisibility(View.GONE);
            lnAdmin.setVisibility(View.GONE);

            ivOrder.setBackgroundColor(getResources().getColor(R.color.colorOrder));

            rvAction = (RecyclerView) findViewById(R.id.rv_sale_action);
            rvAction.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActionList.this,LinearLayoutManager.HORIZONTAL,false);
            rvAction.setLayoutManager(linearLayoutManager);
            final SnapHelper snapHelper = new PagerSnapHelper();
            if (rvAction.getOnFlingListener() == null)
                snapHelper.attachToRecyclerView(rvAction);

            rvAction.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                        View centerView = snapHelper.findSnapView(linearLayoutManager);
                        if(centerView != null){
                            pos = linearLayoutManager.getPosition(centerView);


                            if(pos == 0){
                                ivOrder.setBackgroundColor(getResources().getColor(R.color.colorOrder));
                                ivClient.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivPromotion.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAnnouncement.setBackgroundColor(getResources().getColor(R.color.transparent));

                            }

                            if(pos == 1){
                                ivClient.setBackgroundColor(getResources().getColor(R.color.colorClient));
                                ivOrder.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivPromotion.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAnnouncement.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }

                            if(pos == 2){
                                ivPromotion.setBackgroundColor(getResources().getColor(R.color.colorPromotion));
                                ivClient.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivOrder.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAnnouncement.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }

                            if(pos == 3){
                                ivAnnouncement.setBackgroundColor(getResources().getColor(R.color.colorAnnouncement));
                                ivPromotion.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivClient.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivOrder.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }
                        }

                    }
                }
            });

            adapter = new FirebaseRecyclerAdapter<Employee,EmployeeViewHolder>(
                    Employee.class,
                    R.layout.item_menu,
                    EmployeeViewHolder.class,
                    refDatabase.child("1-System/MainMenu/SaleMan")
            ) {
                @Override
                public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
                    return new EmployeeViewHolder(v);
                }


                @Override
                protected void populateViewHolder(EmployeeViewHolder viewHolder, Employee model, int position) {
                    Glide.with(getApplicationContext()).load(model.getMenuOrderUrl()).into(viewHolder.ivOrderPic);
                }
            };


            rvAction.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            actionOnClick();
        }

        if(supervisor){

            getSupSale();

            lnSup.setVisibility(View.VISIBLE);
            lnSaleMan.setVisibility(View.GONE);
            lnASM.setVisibility(View.GONE);
            lnAdmin.setVisibility(View.GONE);

            rvAction = (RecyclerView) findViewById(R.id.rv_sale_action);
            rvAction.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActionList.this,LinearLayoutManager.HORIZONTAL,false);
            rvAction.setLayoutManager(linearLayoutManager);
            //rvAction.smoothScrollToPosition();
            final SnapHelper snapHelper = new PagerSnapHelper();
            if (rvAction.getOnFlingListener() == null)
                snapHelper.attachToRecyclerView(rvAction);
            ivTeam.setBackgroundColor(getResources().getColor(R.color.colorGroup));

            rvAction.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                        View centerView = snapHelper.findSnapView(linearLayoutManager);
                        if(centerView != null){
                            pos = linearLayoutManager.getPosition(centerView);
                            //editor.putInt("LayoutPosition", pos).apply();

                            //Toast.makeText(getApplicationContext(), pos+"", Toast.LENGTH_LONG).show();
                            if(pos == 0){
                                ivTeam.setBackgroundColor(getResources().getColor(R.color.colorGroup));
                                ivOrderSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivClientSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivPromotionSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAnnoucementSup.setBackgroundColor(getResources().getColor(R.color.transparent));

                            }

                            if(pos == 1){
                                ivTeam.setBackgroundColor(getResources().getColor(R.color.transparent));

                                ivClientSup.setBackgroundColor(getResources().getColor(R.color.colorClient));
                                ivOrderSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivPromotionSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAnnoucementSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }

                            if(pos == 2){
                                ivTeam.setBackgroundColor(getResources().getColor(R.color.transparent));

                                ivPromotionSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivClientSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivOrderSup.setBackgroundColor(getResources().getColor(R.color.colorOrder));
                                ivAnnoucementSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }

                            if(pos == 3){
                                ivTeam.setBackgroundColor(getResources().getColor(R.color.transparent));

                                ivAnnoucementSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivPromotionSup.setBackgroundColor(getResources().getColor(R.color.colorPromotion));
                                ivClientSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivOrderSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }

                            if(pos == 4){
                                ivTeam.setBackgroundColor(getResources().getColor(R.color.transparent));

                                ivAnnoucementSup.setBackgroundColor(getResources().getColor(R.color.colorAnnouncement));
                                ivPromotionSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivClientSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivOrderSup.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }
                        }

                    }
                }
            });

            adapter = new FirebaseRecyclerAdapter<Employee,EmployeeViewHolder>(
                    Employee.class,
                    R.layout.item_menu,
                    EmployeeViewHolder.class,
                    refDatabase.child("1-System/MainMenu/Supervisor")
            ) {
                @Override
                public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
                    return new EmployeeViewHolder(v);
                }


                @Override
                protected void populateViewHolder(EmployeeViewHolder viewHolder, Employee model, int position) {
                    Glide.with(getApplicationContext()).load(model.getMenuOrderUrl()).into(viewHolder.ivOrderPic);
                }
            };


            rvAction.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            actionSupOnClick();
        }

        if(asm){

            getASMSale();

            lnSup.setVisibility(View.GONE);
            lnSaleMan.setVisibility(View.GONE);
            lnASM.setVisibility(View.VISIBLE);
            lnAdmin.setVisibility(View.GONE);

            rvAction = (RecyclerView) findViewById(R.id.rv_sale_action);
            rvAction.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActionList.this,LinearLayoutManager.HORIZONTAL,false);
            rvAction.setLayoutManager(linearLayoutManager);
            final SnapHelper snapHelper = new PagerSnapHelper();

            if (rvAction.getOnFlingListener() == null)
                snapHelper.attachToRecyclerView(rvAction);

            ivSupASM.setBackgroundColor(getResources().getColor(R.color.colorSupervisor));

            rvAction.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                        View centerView = snapHelper.findSnapView(linearLayoutManager);
                        if(centerView != null){
                            int pos = linearLayoutManager.getPosition(centerView);

                            //Toast.makeText(getApplicationContext(), pos+"", Toast.LENGTH_LONG).show();
                            if(pos == 0){
                                ivSupASM.setBackgroundColor(getResources().getColor(R.color.colorSupervisor));
                                ivReportASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivPromotionASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAnnouncementASM.setBackgroundColor(getResources().getColor(R.color.transparent));

                            }

                            if(pos == 1){
                                ivSupASM.setBackgroundColor(getResources().getColor(R.color.transparent));

                                ivReportASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivPromotionASM.setBackgroundColor(getResources().getColor(R.color.colorPromotion));
                                ivAnnouncementASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }

                            if(pos == 2){
                                ivSupASM.setBackgroundColor(getResources().getColor(R.color.transparent));

                                ivPromotionASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivReportASM.setBackgroundColor(getResources().getColor(R.color.colorReport));
                                ivAnnouncementASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }

                            if(pos == 3){
                                ivSupASM.setBackgroundColor(getResources().getColor(R.color.transparent));

                                ivAnnouncementASM.setBackgroundColor(getResources().getColor(R.color.colorAnnouncement));
                                ivPromotionASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivReportASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }

                        }

                    }
                }
            });

            adapter = new FirebaseRecyclerAdapter<Employee,EmployeeViewHolder>(
                    Employee.class,
                    R.layout.item_menu,
                    EmployeeViewHolder.class,
                    refDatabase.child("1-System/MainMenu/ASM")
            ) {
                @Override
                public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
                    return new EmployeeViewHolder(v);
                }


                @Override
                protected void populateViewHolder(EmployeeViewHolder viewHolder, Employee model, int position) {
                    Glide.with(getApplicationContext()).load(model.getMenuOrderUrl()).into(viewHolder.ivOrderPic);
                }
            };


            rvAction.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            actionASM();
        }

        if(rsm){

            getASMSale();
            
            lnSup.setVisibility(View.GONE);
            lnSaleMan.setVisibility(View.GONE);
            lnASM.setVisibility(View.VISIBLE);
            lnAdmin.setVisibility(View.GONE);

            rvAction = (RecyclerView) findViewById(R.id.rv_sale_action);
            rvAction.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActionList.this,LinearLayoutManager.HORIZONTAL,false);
            rvAction.setLayoutManager(linearLayoutManager);
            final SnapHelper snapHelper = new PagerSnapHelper();
            if (rvAction.getOnFlingListener() == null)
                snapHelper.attachToRecyclerView(rvAction);
            ivSupASM.setBackgroundColor(getResources().getColor(R.color.colorSupervisor));

            rvAction.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                        View centerView = snapHelper.findSnapView(linearLayoutManager);

                        if(centerView != null){
                            int pos = linearLayoutManager.getPosition(centerView);

                            if(pos == 0){
                                ivSupASM.setBackgroundColor(getResources().getColor(R.color.colorSupervisor));
                                ivReportASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivPromotionASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAnnouncementASM.setBackgroundColor(getResources().getColor(R.color.transparent));

                            }

                            if(pos == 1){
                                ivSupASM.setBackgroundColor(getResources().getColor(R.color.transparent));

                                ivReportASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivPromotionASM.setBackgroundColor(getResources().getColor(R.color.colorPromotion));
                                ivAnnouncementASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }

                            if(pos == 2){
                                ivSupASM.setBackgroundColor(getResources().getColor(R.color.transparent));

                                ivPromotionASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivReportASM.setBackgroundColor(getResources().getColor(R.color.colorReport));
                                ivAnnouncementASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }

                            if(pos == 3){
                                ivSupASM.setBackgroundColor(getResources().getColor(R.color.transparent));

                                ivAnnouncementASM.setBackgroundColor(getResources().getColor(R.color.colorAnnouncement));
                                ivPromotionASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivReportASM.setBackgroundColor(getResources().getColor(R.color.transparent));
                            }


                        }



                        //Toast.makeText(getApplicationContext(), pos+"", Toast.LENGTH_LONG).show();

                    }
                }
            });

            adapter = new FirebaseRecyclerAdapter<Employee,EmployeeViewHolder>(
                    Employee.class,
                    R.layout.item_menu,
                    EmployeeViewHolder.class,
                    refDatabase.child("1-System/MainMenu/ASM")
            ) {
                @Override
                public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
                    return new EmployeeViewHolder(v);
                }


                @Override
                protected void populateViewHolder(EmployeeViewHolder viewHolder, Employee model, int position) {
                    Glide.with(getApplicationContext()).load(model.getMenuOrderUrl()).into(viewHolder.ivOrderPic);
                }
            };


            rvAction.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            actionASM();
        }

        if(admin){
            lnSup.setVisibility(View.GONE);
            lnSaleMan.setVisibility(View.GONE);
            lnASM.setVisibility(View.GONE);
            lnAdmin.setVisibility(View.VISIBLE);

            rvAction = (RecyclerView) findViewById(R.id.rv_sale_action);
            rvAction.setHasFixedSize(true);
            final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ActionList.this,LinearLayoutManager.HORIZONTAL,false);
            rvAction.setLayoutManager(linearLayoutManager);
            final SnapHelper snapHelper = new PagerSnapHelper();
            if (rvAction.getOnFlingListener() == null)
                snapHelper.attachToRecyclerView(rvAction);
            ivAdminOrder.setBackgroundColor(getResources().getColor(R.color.colorOrderAdmin));

            rvAction.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                    if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                        View centerView = snapHelper.findSnapView(linearLayoutManager);

                        if( centerView!= null) {
                            int pos = linearLayoutManager.getPosition(centerView);

                            if(pos == 0){
                                ivAdminOrder.setBackgroundColor(getResources().getColor(R.color.colorOrderAdmin));
                                ivCreateAcc.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminProduct.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminPayroll.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminPromotion.setBackgroundColor(getResources().getColor(R.color.transparent));


                            }
                            if(pos == 1){
                                ivAdminOrder.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivCreateAcc.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminProduct.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminPayroll.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminPromotion.setBackgroundColor(getResources().getColor(R.color.colorPromotion));

                            }


                            if(pos == 2){
                                ivAdminOrder.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivCreateAcc.setBackgroundColor(getResources().getColor(R.color.colorAccount));
                                ivAdminProduct.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminPayroll.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminPromotion.setBackgroundColor(getResources().getColor(R.color.transparent));

                            }

                            if(pos == 3){
                                ivAdminOrder.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivCreateAcc.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminProduct.setBackgroundColor(getResources().getColor(R.color.colorProduct));
                                ivAdminPayroll.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminPromotion.setBackgroundColor(getResources().getColor(R.color.transparent));

                            }
                            if(pos == 4){
                                ivAdminOrder.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivCreateAcc.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminProduct.setBackgroundColor(getResources().getColor(R.color.transparent));
                                ivAdminPayroll.setBackgroundColor(getResources().getColor(R.color.colorPayroll));
                                ivAdminPromotion.setBackgroundColor(getResources().getColor(R.color.transparent));

                            }


                        }

                        //Toast.makeText(getApplicationContext(), pos+"", Toast.LENGTH_LONG).show();


                    }
                }
            });

            adapter = new FirebaseRecyclerAdapter<Employee,EmployeeViewHolder>(
                    Employee.class,
                    R.layout.item_menu,
                    EmployeeViewHolder.class,
                    refDatabase.child("1-System/MainMenu/Admin")
            ) {
                @Override
                public EmployeeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
                    return new EmployeeViewHolder(v);
                }


                @Override
                protected void populateViewHolder(EmployeeViewHolder viewHolder, Employee model, int position) {
                    Glide.with(getApplicationContext()).load(model.getMenuOrderUrl()).into(viewHolder.ivOrderPic);
                }
            };


            rvAction.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            actionAdmin();
        }
    }

    private void getSupSale() {
        final HashMap<String,Float> salesMonth = new HashMap<>();
        final HashMap<String,Float> salesYear = new HashMap<>();

        refCompany.child("SaleManBySup").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                final long supCount = dataSnapshot.getChildrenCount();

                final float[] totalDaySale = {0};
                final float[] totalMonthSale = {0};
                final float[] totalYearSale = {0};

                int i = 0;
                for(DataSnapshot itemSale:snapSale){
                    i ++;
                    final String saleEmail = itemSale.getKey();
                    final String saleName = itemSale.getValue(Employee.class).getEmployeeName();
                    final String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);

                    refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(year+"-"+month+"-"+day)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month+"-"+day).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalDaySale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year+"-"+month+"-"+day).setValue(totalDaySale[0] +"");

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            if(dataSnapshot.hasChild(year+"-"+month)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalMonthSale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year+"-"+month).setValue(totalMonthSale[0] +"");

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            if(dataSnapshot.hasChild(year)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalYearSale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year).setValue(totalYearSale[0] +"");

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


                    final int finalI = i;
                    refCompany.child("TotalProductBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChildren()){

                                Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                                for(DataSnapshot itemProductSale:snapProduct){
                                    final String productCode = itemProductSale.getKey();
                                    DatabaseReference refProduct = itemProductSale.getRef();
                                    refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if(dataSnapshot.hasChild(year+"-"+month)){

                                                refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(salesMonth.containsKey(productCode)){
                                                            float updateSale = currentSale + salesMonth.get(productCode);
                                                            salesMonth.put(productCode,updateSale);

                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year+"-"+month).setValue(updateSale+"");


                                                        }else{
                                                            salesMonth.put(productCode,currentSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year+"-"+month).setValue(salesMonth.get(productCode)+"");

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }

                                            if(dataSnapshot.hasChild(year)){

                                                refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(salesYear.containsKey(productCode)){
                                                            float updateSale = currentSale + salesYear.get(productCode);
                                                            salesYear.put(productCode,updateSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year).setValue(salesYear.get(productCode)+"");


                                                        }else{
                                                            salesYear.put(productCode,currentSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year).setValue(salesYear.get(productCode)+"");

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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void actionASM() {

        ivSupASM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), SaleList.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("ASM",true);
                startActivity(it);

            }
        });

        ivPromotionASM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), PromotionList.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("ASM",true);
                startActivity(it);
            }
        });

        ivReportASM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), SaleReport.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("ASM",true);
                startActivity(it);
            }
        });


        ivAnnouncementASM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), AnnounceList.class);
                it.putExtra("EmailLogin",emailLogin);
                startActivity(it);
            }
        });

        ivLogoutASM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                Intent it = new Intent(getApplicationContext(), LoginActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(it);
            }
        });
    }

    private void actionAdmin() {

        ivCreateAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), AccountList.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("Admin",true);
                startActivity(it);
            }
        });

        ivAdminProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), CreateProduct.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("Admin",true);
                startActivity(it);
            }
        });

        ivAdminOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(),OrderManActivity.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("Admin",true);
                startActivity(it);
            }
        });

        ivAdminPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(),PromotionList.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("Admin",true);
                startActivity(it);
            }
        });

        ivLogoutAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                Intent it = new Intent(getApplicationContext(), LoginActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(it);
            }
        });
    }

    private void actionSupOnClick() {
        ivOrderSup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(),OrderManActivity.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("Supervisor",supervisor);
                startActivity(it);
            }
        });

        ivClientSup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), ClientListBySaleTeam.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("Supervisor",supervisor);

                startActivity(it);

            }
        });

        ivTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), SaleList.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("Supervisor",supervisor);

                startActivity(it);

            }
        });

        ivPromotionSup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(),PromotionList.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("Supervisor",supervisor);
                startActivity(it);
            }
        });


        ivAnnoucementSup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), AnnounceList.class);
                it.putExtra("EmailLogin",emailLogin);
                startActivity(it);
            }
        });

        ivSupLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                Intent it = new Intent(getApplicationContext(), LoginActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(it);

            }
        });

    }

    private void actionOnClick() {

        ivOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(),OrderManActivity.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("SaleMan",saleMan);
                startActivity(it);
            }
        });

        ivClient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), ClientListBySaleTeam.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("SaleMan",saleMan);

                startActivity(it);

            }
        });

        ivPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), PromotionList.class);
                it.putExtra("EmailLogin",emailLogin);
                startActivity(it);
            }
        });


        ivAnnouncement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(getApplicationContext(), AnnounceList.class);
                it.putExtra("EmailLogin",emailLogin);
                startActivity(it);
            }
        });

        ivSaleLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                Intent it = new Intent(getApplicationContext(), LoginActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(it);

            }
        });

    }



    private void getASMSale() {
        final HashMap<String,Float> salesMonth = new HashMap<>();
        final HashMap<String,Float> salesYear = new HashMap<>();

        refCompany.child("SupByASM").child(userEmail).child("Tất cả").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> snapSale = dataSnapshot.getChildren();
                final long supCount = dataSnapshot.getChildrenCount();

                final float[] totalDaySale = {0};
                final float[] totalMonthSale = {0};
                final float[] totalYearSale = {0};

                int i = 0;
                for(DataSnapshot itemSale:snapSale){
                    i ++;
                    final String saleEmail = itemSale.getKey();
                    final String saleName = itemSale.getValue(Employee.class).getEmployeeName();
                    final String saleNameShort = saleName.substring(saleName.lastIndexOf(" ")+1);

                    refCompany.child("TotalBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(year+"-"+month+"-"+day)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month+"-"+day).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalDaySale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year+"-"+month+"-"+day).setValue(totalDaySale[0] +"");

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            if(dataSnapshot.hasChild(year+"-"+month)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalMonthSale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year+"-"+month).setValue(totalMonthSale[0] +"");

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }

                            if(dataSnapshot.hasChild(year)){
                                refCompany.child("TotalBySale").child(saleEmail).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());
                                        totalYearSale[0] += currentSale;
                                        refCompany.child("TotalBySale").child(userEmail).child(year).setValue(totalYearSale[0] +"");

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


                    final int finalI = i;
                    refCompany.child("TotalProductBySale").child(saleEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if(dataSnapshot.hasChildren()){

                                Iterable<DataSnapshot> snapProduct = dataSnapshot.getChildren();
                                for(DataSnapshot itemProductSale:snapProduct){
                                    final String productCode = itemProductSale.getKey();
                                    DatabaseReference refProduct = itemProductSale.getRef();
                                    refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {

                                            if(dataSnapshot.hasChild(year+"-"+month)){

                                                refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year+"-"+month).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(salesMonth.containsKey(productCode)){
                                                            float updateSale = currentSale + salesMonth.get(productCode);
                                                            salesMonth.put(productCode,updateSale);

                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year+"-"+month).setValue(updateSale+"");


                                                        }else{
                                                            salesMonth.put(productCode,currentSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year+"-"+month).setValue(salesMonth.get(productCode)+"");

                                                        }

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                            }

                                            if(dataSnapshot.hasChild(year)){

                                                refCompany.child("TotalProductBySale").child(saleEmail).child(productCode).child(year).addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        float currentSale = Float.parseFloat(dataSnapshot.getValue().toString());

                                                        if(salesYear.containsKey(productCode)){
                                                            float updateSale = currentSale + salesYear.get(productCode);
                                                            salesYear.put(productCode,updateSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year).setValue(salesYear.get(productCode)+"");


                                                        }else{
                                                            salesYear.put(productCode,currentSale);
                                                            refCompany.child("TotalProductBySale").child(userEmail).child(productCode).child(year).setValue(salesYear.get(productCode)+"");

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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public class EmployeeViewHolder extends RecyclerView.ViewHolder {
         ImageView ivOrderPic;

        public EmployeeViewHolder(View itemView) {
            super(itemView);
            ivOrderPic = itemView.findViewById(R.id.iv_item_menu);

        }
    }

}

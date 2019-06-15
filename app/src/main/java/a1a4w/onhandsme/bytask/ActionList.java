package a1a4w.onhandsme.bytask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;

import a1a4w.onhandsme.LoginActivity;
import a1a4w.onhandsme.R;
import a1a4w.onhandsme.list.ClientListBySaleTeam;
import a1a4w.onhandsme.list.SaleListBySup;
import a1a4w.onhandsme.model.Employee;

import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class ActionList extends AppCompatActivity {
    private RecyclerView rvAction;
    private FirebaseRecyclerAdapter<Employee,EmployeeViewHolder> adapter ;
    private ImageView ivOrder, ivClient,ivPromotion,ivAnnouncement,ivTeam,ivOrderSup,ivPromotionSup,ivAnnoucementSup,ivSaleLogout,ivSupLogout,ivClientSup;
    private String emailLogin;
    private boolean saleMan,supervisor;
    private LinearLayout lnSup,lnSaleMan;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_list);

        Intent it = this.getIntent();
        emailLogin = it.getStringExtra("EmailLogin");
        saleMan = it.getBooleanExtra("SaleMan", false);
        supervisor = it.getBooleanExtra("Supervisor",false);

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

        ivOrder.setBackgroundColor(getResources().getColor(R.color.colorOrder));

        lnSup = findViewById(R.id.ln_sup);
         lnSaleMan = findViewById(R.id.ln_sale_man);
         
         if(saleMan){
             lnSup.setVisibility(View.GONE);
             lnSaleMan.setVisibility(View.VISIBLE);
             
             rvAction = (RecyclerView) findViewById(R.id.rv_sale_action);
             rvAction.setHasFixedSize(true);
             final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
             rvAction.setLayoutManager(linearLayoutManager);
             final SnapHelper snapHelper = new LinearSnapHelper();
             snapHelper.attachToRecyclerView(rvAction);

             rvAction.addOnScrollListener(new RecyclerView.OnScrollListener() {

                 @Override
                 public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                     super.onScrollStateChanged(recyclerView, newState);
                     if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                         View centerView = snapHelper.findSnapView(linearLayoutManager);
                         int pos = linearLayoutManager.getPosition(centerView);
                         Log.e("Snapped Item Position:",""+pos);

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
             lnSup.setVisibility(View.VISIBLE);
             lnSaleMan.setVisibility(View.GONE);
             
             rvAction = (RecyclerView) findViewById(R.id.rv_sale_action);
             rvAction.setHasFixedSize(true);
             final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
             rvAction.setLayoutManager(linearLayoutManager);
             final SnapHelper snapHelper = new PagerSnapHelper();
             snapHelper.attachToRecyclerView(rvAction);
             ivTeam.setBackgroundColor(getResources().getColor(R.color.colorGroup));

             rvAction.addOnScrollListener(new RecyclerView.OnScrollListener() {

                 @Override
                 public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                     super.onScrollStateChanged(recyclerView, newState);
                     if(newState == RecyclerView.SCROLL_STATE_IDLE) {
                         View centerView = snapHelper.findSnapView(linearLayoutManager);
                         int pos = linearLayoutManager.getPosition(centerView);

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
                Intent it = new Intent(getApplicationContext(), SaleListBySup.class);
                it.putExtra("EmailLogin",emailLogin);
                startActivity(it);

            }
        });

        ivPromotionSup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        ivAnnoucementSup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        ivSupLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));

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

            }
        });

        ivSaleLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));

            }
        });

    }

    public class EmployeeViewHolder extends RecyclerView.ViewHolder {
         ImageView ivOrderPic;

        public EmployeeViewHolder(View itemView) {
            super(itemView);
            ivOrderPic = itemView.findViewById(R.id.iv_item_menu);

            ivOrderPic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Employee em = adapter.getItem(position);
                    String menuName = em.getMenuOrderName();

                    if (menuName.equals("Đơn hàng")){
                        Intent it = new Intent(getApplicationContext(),OrderManActivity.class);
                        it.putExtra("EmailLogin",emailLogin);
                        startActivity(it);

                    }

                    if (menuName.equals("Khách hàng")){
                        Intent it = new Intent(getApplicationContext(), ClientListBySaleTeam.class);
                        it.putExtra("EmailLogin",emailLogin);
                        startActivity(it);

                    }

                    if (menuName.equals("Chương trình")){

                    }

                    if (menuName.equals("Thông báo")){

                    }
                }
            });

        }
    }

}

package vn.techlifegroup.wesell.bytask;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import vn.techlifegroup.wesell.MainActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Product;
import vn.techlifegroup.wesell.model.Promotion;

import static vn.techlifegroup.wesell.utils.Constants.buttonClick;
import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class PromotionList extends AppCompatActivity {
    private String emailLogin;
    private FirebaseRecyclerAdapter<Promotion, PromotionViewHolder> adapterFirebasePromotion;
    private boolean admin;
    private FirebaseRecyclerAdapter<Product, CreateProgram.ProductViewHolder> adapterFirebaseProductDis;
    private FirebaseRecyclerAdapter<Promotion, CreateProgram.ProductBGMHolder> adapterProductBGM;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_promotion);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");
        admin = intent.getBooleanExtra("Admin", false);

        FloatingActionButton fabAddPromotion = findViewById(R.id.fab_add_promotion);
        if(admin) fabAddPromotion.setVisibility(View.VISIBLE);

        fabAddPromotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(buttonClick);
                Intent it = new Intent(getApplicationContext(),CreateProgram.class);
                it.putExtra("EmailLogin",emailLogin);
                it.putExtra("Admin",true);
                startActivity(it);
                //dialogAddPromotion();
            }
        });



        RecyclerView recyclerViewPromotion = findViewById(R.id.rv_promotion_list);

        recyclerViewPromotion.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewPromotion.setLayoutManager(linearLayoutManager);

        DatabaseReference refPromotion = refDatabase.child(emailLogin).child("PromotionMan");

        adapterFirebasePromotion = new FirebaseRecyclerAdapter<Promotion, PromotionViewHolder>(
                Promotion.class,
                R.layout.item_promotion_detail,
                PromotionViewHolder.class,
                refPromotion
        ) {
            @Override
            public PromotionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_promotion_detail,parent,false);
                return new PromotionViewHolder(v);
            }


            @Override
            protected void populateViewHolder(PromotionViewHolder viewHolder, Promotion model, int position) {
                viewHolder.mPromotionName.setText(model.getPromotionName());
                viewHolder.startDate.setText(model.getPromotionStartDate());
                viewHolder.endDate.setText(model.getPromotionEndDate());

            }
        };

        recyclerViewPromotion.setAdapter(adapterFirebasePromotion);
        adapterFirebasePromotion.notifyDataSetChanged();
    }

    public class PromotionViewHolder extends RecyclerView.ViewHolder {
        TextView mPromotionName,startDate,endDate;
        ImageView del,save;

        public PromotionViewHolder(View itemView) {
            super(itemView);
            mPromotionName = (TextView) itemView.findViewById(R.id.tv_item_promotion_detail_name);
            startDate = itemView.findViewById(R.id.tv_item_promotion_start);
            endDate = itemView.findViewById(R.id.tv_item_promotion_end);
            //del = itemView.findViewById(R.id.iv_item_promotion_del);
            save = itemView.findViewById(R.id.iv_item_promotion_save);

            save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);

                    int pos = getAdapterPosition();
                    final DatabaseReference refPromotionMan = adapterFirebasePromotion.getRef(pos);
                    final Promotion p = adapterFirebasePromotion.getItem(pos);
                    final String promotionKey = adapterFirebasePromotion.getRef(pos).getKey();

                    AlertDialog.Builder builder = new AlertDialog.Builder(PromotionList.this);
                    builder.setMessage("Lưu và ẩn chương trình này?");
                    builder.setPositiveButton("Ẩn", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            DateTimeFormatter fmt = DateTimeFormat.forPattern("dd-MM-yyyy");

                            DateTime promotionEnd = fmt.parseDateTime(p.getPromotionEndDate());
                            DateTime dt = new DateTime();


                            if(dt.toDate().before( promotionEnd.toDate())){
                                Toast.makeText(getApplicationContext(), "Chương trình vẫn còn hiệu lực, vui lòng giữ lại để theo dõi!", Toast.LENGTH_LONG).show();
                            }else{
                                refPromotionMan.setValue(null);
                                refDatabase.child(emailLogin).child("PromotionHistory").child(promotionKey).setValue(p);
                            }
                        }
                    }).setNegativeButton("Không", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(buttonClick);

                    int pos = getAdapterPosition();
                    DatabaseReference refPromotionMan = adapterFirebasePromotion.getRef(pos);

                    AlertDialog.Builder builder = new AlertDialog.Builder(PromotionList.this);
                    View dialogView = getLayoutInflater().inflate(R.layout.dialog_promotion_view_detail,null);
                    builder.setView(dialogView);

                    Dialog dialog = builder.create();
                    dialog.show();

                    final TextView tvName = dialogView.findViewById(R.id.tv_promotion_detail_name);
                    RecyclerView listProductDis = dialogView.findViewById(R.id.rv_promotion_detail_product_dis);
                    RecyclerView listBGM = dialogView.findViewById(R.id.rv_promotion_detail_bgm);
                    final TextView tvOrderDis = dialogView.findViewById(R.id.tv_promotion_detail_orderDis);

                    listProductDis.setHasFixedSize(true);
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    listProductDis.setLayoutManager(linearLayoutManager);

                    refPromotionMan.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Promotion promotion = dataSnapshot.getValue(Promotion.class);
                            tvName.setText(promotion.getPromotionName());

                            if(dataSnapshot.hasChild("orderDiscount")){
                                tvOrderDis.setText(promotion.getOrderDiscount()+"%");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });



                    adapterFirebaseProductDis = new FirebaseRecyclerAdapter<Product, CreateProgram.ProductViewHolder>(
                            Product.class,
                            R.layout.item_product_discount,
                            CreateProgram.ProductViewHolder.class,
                            refPromotionMan.child("ProductDiscount")
                    ) {
                        @Override
                        public CreateProgram.ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_discount,parent,false);
                            return new CreateProgram.ProductViewHolder(v);
                        }


                        @Override
                        protected void populateViewHolder(CreateProgram.ProductViewHolder viewHolder, Product model, int position) {
                            viewHolder.name.setText(model.getProductName());
                            viewHolder.discount.setText(model.getProductDiscount()+"%");
                        }
                    };

                    listProductDis.setAdapter(adapterFirebaseProductDis);
                    adapterFirebaseProductDis.notifyDataSetChanged();

                    listBGM.setHasFixedSize(true);
                    LinearLayoutManager linearLayoutManager2 = new LinearLayoutManager(getApplicationContext());
                    listBGM.setLayoutManager(linearLayoutManager2);

                    adapterProductBGM = new FirebaseRecyclerAdapter<Promotion, CreateProgram.ProductBGMHolder>(
                            Promotion.class,
                            R.layout.item_product_bgm,
                            CreateProgram.ProductBGMHolder.class,
                            refPromotionMan.child("BGM")
                    ) {
                        @Override
                        public CreateProgram.ProductBGMHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_bgm,parent,false);
                            return new CreateProgram.ProductBGMHolder(v);
                        }


                        @Override
                        protected void populateViewHolder(CreateProgram.ProductBGMHolder viewHolder, Promotion model, int position) {
                            viewHolder.buyName.setText(model.getPromotionBuyName());
                            viewHolder.getName.setText(model.getPromotionGetName());
                            viewHolder.getQuantity.setText(model.getPromotionGetQuantity());
                            viewHolder.buyQuantity.setText(model.getPromotionBuyQuantity());
                        }
                    };

                    listBGM.setAdapter(adapterProductBGM);
                    adapterProductBGM.notifyDataSetChanged();


                }
            });




        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
    }
}

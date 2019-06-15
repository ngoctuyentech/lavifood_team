package a1a4w.onhandsme.bytask;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import a1a4w.onhandsme.R;
import a1a4w.onhandsme.model.Promotion;

import static a1a4w.onhandsme.utils.Constants.refDatabase;

public class PromotionList extends AppCompatActivity {
    private String emailLogin;
    private FirebaseRecyclerAdapter<Promotion, PromotionViewHolder> adapterFirebasePromotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promotion_list);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

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


        public PromotionViewHolder(View itemView) {
            super(itemView);
            mPromotionName = (TextView) itemView.findViewById(R.id.tv_item_promotion_detail_name);
            startDate = itemView.findViewById(R.id.tv_item_promotion_start);
            endDate = itemView.findViewById(R.id.tv_item_promotion_end);

        }
    }

}

package vn.techlifegroup.wesell.bytask;

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

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Announce;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;

public class AnnounceList extends AppCompatActivity {
    private String emailLogin;
    private FirebaseRecyclerAdapter<Announce, AnnounceViewHolder> adapterFirebasePromotion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announce_list);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

        RecyclerView recyclerViewPromotion = findViewById(R.id.rv_announce_list);

        recyclerViewPromotion.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewPromotion.setLayoutManager(linearLayoutManager);

        DatabaseReference refAnnounce = refDatabase.child(emailLogin).child("Announcement");

        adapterFirebasePromotion = new FirebaseRecyclerAdapter<Announce, AnnounceViewHolder>(
                Announce.class,
                R.layout.item_announce,
                AnnounceViewHolder.class,
                refAnnounce
        ) {
            @Override
            public AnnounceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_announce,parent,false);
                return new AnnounceViewHolder(v);
            }

            @Override
            protected void populateViewHolder(AnnounceViewHolder viewHolder, Announce model, int position) {
                viewHolder.topic.setText(model.getAnnounceTopic());
                viewHolder.content.setText(model.getAnnounceContent());
            }


        };

        recyclerViewPromotion.setAdapter(adapterFirebasePromotion);
        adapterFirebasePromotion.notifyDataSetChanged();


    }

    public class AnnounceViewHolder extends RecyclerView.ViewHolder {
        TextView topic,content;

        public AnnounceViewHolder(View itemView) {
            super(itemView);

            topic = itemView.findViewById(R.id.tv_announce_topic);
            content = itemView.findViewById(R.id.tv_announce_content);
        }
    }
}

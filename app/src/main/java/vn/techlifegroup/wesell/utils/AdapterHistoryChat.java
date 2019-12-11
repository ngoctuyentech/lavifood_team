package vn.techlifegroup.wesell.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.bytask.MainChatActivity;
import vn.techlifegroup.wesell.model.ChatHistoryModel;

import static com.facebook.FacebookSdk.getApplicationContext;
import static vn.techlifegroup.wesell.utils.Constants.refEmployee;
import static vn.techlifegroup.wesell.utils.Constants.refEmployees;


public class AdapterHistoryChat extends RecyclerView.Adapter<AdapterHistoryChat.HistoryChatViewHolder> {

    Context context;

    private String saleManEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

    private List<ChatHistoryModel> item;
    private ChatHistoryModel chat;
    private Activity activity;

    private String content;

    private HashMap<String, Integer> mapChatRoom = new HashMap<>();

    private List<ChatHistoryModel> historyChatList = new ArrayList<>();

    public static String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public AdapterHistoryChat() {
        super();

    }

    public AdapterHistoryChat(Context context, List<ChatHistoryModel> item, Activity activity) {
        this.context = context;
        this.item = item;
        this.activity = activity;
    }

    @Override
    public HistoryChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history_chat, parent, false);
        return new HistoryChatViewHolder(v);

    }

    @Override
    public void onBindViewHolder(final HistoryChatViewHolder holder, int position) {

        chat = item.get(position);

        final String idFr = chat.getId();

        refEmployees.child(saleManEmail).child("friend").child(idFr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ChatHistoryModel chat = dataSnapshot.getValue(ChatHistoryModel.class);

                holder.tvTime.setText(Utils.getHourFromTimeStamp(Long.parseLong(chat.getLastTimeStamp())));
                holder.tvDate.setText(Utils.getDateFromTimeStamp(Long.parseLong(chat.getLastTimeStamp())));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


       // holder.tvTime.setText(Utils.getHourFromTimeStamp(Long.parseLong(timeStr)));
      //  holder.tvDate.setText(Utils.getDateFromTimeStamp(Long.parseLong(timeStr)));

        refEmployees.child(saleManEmail).child("friend").child(idFr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ChatHistoryModel valueChat = dataSnapshot.getValue(ChatHistoryModel.class);

                //Glide.with(context).load(valueChat.getImageFr()).apply(RequestOptions.circleCropTransform()).into(holder.ivChatContact);

                String name = valueChat.getNameFr();
                String role = valueChat.getRoleFr();

                holder.tvName.setText(name);
                holder.tvRole.setText(role);

                Glide.with(getApplicationContext()).load(valueChat.getImageFr()).into(holder.ivChatContact);


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        refEmployees.child(saleManEmail).child("friend").child(idFr).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    ChatHistoryModel chat = dataSnapshot.getValue(ChatHistoryModel.class);

                    boolean isRead = chat.isRead();

                    String messageHint = chat.getLastContent();
                    holder.tvMessageHint.setText(messageHint);


                if(isRead == true){
                    holder.tvMessageHint.setTypeface(Typeface.DEFAULT);

                }if(isRead == false){
                    holder.tvMessageHint.setTypeface(Typeface.DEFAULT_BOLD);

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return item.size();
    }

    public class HistoryChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvRole, tvMessageHint, tvTime, tvDate;
        ImageView ivChatContact;

        public HistoryChatViewHolder(View itemView) {
            super(itemView);

            tvName        = itemView.findViewById(R.id.tv_item_history_chat_name);
            tvRole        = itemView.findViewById(R.id.tv_item_history_chat_role);
            tvMessageHint = itemView.findViewById(R.id.tv_item_history_chat_last_content);
            tvTime        = itemView.findViewById(R.id.tv_item_history_chat_time);
            tvDate        = itemView.findViewById(R.id.tv_item_history_chat_date);
            ivChatContact = itemView.findViewById(R.id.iv_logo_chat);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);

                    int pos = getLayoutPosition();

                    ChatHistoryModel chat = item.get(pos);

                    String idFr = chat.getId();

                    refEmployees.child(saleManEmail).child("friend").child(idFr).child("isRead").setValue(true);

                    Intent it = new Intent(context, MainChatActivity.class);

                    it.putExtra("ID Friend", idFr);

                    context.startActivity(it);

                }
            });
        }
    }
}

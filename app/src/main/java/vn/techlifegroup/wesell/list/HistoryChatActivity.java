package vn.techlifegroup.wesell.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.ChatHistoryModel;
import vn.techlifegroup.wesell.utils.AdapterHistoryChat;
import vn.techlifegroup.wesell.utils.Utils;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;
import static vn.techlifegroup.wesell.utils.Constants.refEmployees;
import static vn.techlifegroup.wesell.utils.Utils.sortByValues;

public class HistoryChatActivity extends AppCompatActivity {

    private HashMap<String, Long> mapHistoryChat = new HashMap<>();
    private List<ChatHistoryModel> historyChatList = new ArrayList<>();

    private AdapterHistoryChat adapterHistoryChat;

    private RecyclerView rvHistoryChat;
    private LinearLayoutManager linearLayoutManager;

    public static String saleManEmail, userUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_chat);

        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar__history_chat);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();

        saleManEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");
        userUid      = FirebaseAuth.getInstance().getCurrentUser().getUid();

        rvHistoryChat = findViewById(R.id.rv_history_chat);

        //refDatabase.child("userUid").setValue(userUid);

    }

    @Override
    protected void onResume() {
        super.onResume();

        linearLayoutManager = new LinearLayoutManager(HistoryChatActivity.this);
        rvHistoryChat.setHasFixedSize(false);
        rvHistoryChat.setLayoutManager(linearLayoutManager);

        mapHistoryChat.clear();

        refEmployees.child(userUid).child("friend").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                int i = 0;

                Iterable<DataSnapshot> snapFriend = dataSnapshot.getChildren();

                for (final DataSnapshot itemFriend : snapFriend) {

                    i = i + 1;

                    final int countKeyFriend = (int) dataSnapshot.getChildrenCount();

                    final String keyFriend = itemFriend.getKey();

                    if(i == countKeyFriend){

                        refEmployees.child(userUid).child("friend").child(keyFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                historyChatList.clear();

                                ChatHistoryModel chat = dataSnapshot.getValue(ChatHistoryModel.class);

                                Long lastTimeStampLong = Long.parseLong(chat.getLastTimeStamp());

                                mapHistoryChat.put(keyFriend, lastTimeStampLong);

                                Map<String,Long> sortMap = new HashMap<>();

                                sortMap = sortByValues(mapHistoryChat);

                                for (Map.Entry<String, Long> entryFinal : sortMap.entrySet()) {

                                    String idFriendFinal      = entryFinal.getKey();
                                    Long  lastTimeStampLongFinal = entryFinal.getValue();

                                    String lastTimeStampStrFinal = Utils.getHourDateFromTimeStamp(lastTimeStampLongFinal);

                                    ChatHistoryModel idDateFinal = new ChatHistoryModel(idFriendFinal, lastTimeStampStrFinal);

                                    historyChatList.add(idDateFinal);

                                    adapterHistoryChat = new AdapterHistoryChat(getApplicationContext(), historyChatList, HistoryChatActivity.this);
                                    adapterHistoryChat.notifyDataSetChanged();

                                    rvHistoryChat.setAdapter(adapterHistoryChat);

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    }else{

                        refEmployees.child(userUid).child("friend").child(keyFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                ChatHistoryModel chat = dataSnapshot.getValue(ChatHistoryModel.class);

                                Long lastTimeStampLong = Long.parseLong(chat.getLastTimeStamp());

                                mapHistoryChat.put(keyFriend, lastTimeStampLong);

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

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == android.R.id.home){
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}


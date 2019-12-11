package vn.techlifegroup.wesell.list;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

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

import static vn.techlifegroup.wesell.utils.Constants.refEmployees;
import static vn.techlifegroup.wesell.utils.Utils.sortByValues;

public class HistoryChatActivity extends AppCompatActivity {

    private HashMap<String, Long> mapHistoryChat = new HashMap<>();
    private List<ChatHistoryModel> historyChatList = new ArrayList<>();

    private AdapterHistoryChat adapterHistoryChat;

    private RecyclerView rvHistoryChat;
    private LinearLayoutManager linearLayoutManager;

    private String saleManEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_chat);

        android.support.v7.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar__history_chat);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = this.getIntent();

        saleManEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail().replace(".",",");

        rvHistoryChat = findViewById(R.id.rv_history_chat);


/*
        refUsers.child(userUid).child("chat").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int i = 0;

                Iterable<DataSnapshot> snapFriend = dataSnapshot.getChildren();

                for (final DataSnapshot itemFriend : snapFriend) {

                    i = i + 1;

                    final int countKeyFriend = (int) dataSnapshot.getChildrenCount();

                    final String keyFriend = itemFriend.getKey();

                    final Query lastNodeChat = refUsers.child(userUid).child("chat").child(keyFriend)
                            .limitToLast(1);

                    if (i == countKeyFriend) {

                        lastNodeChat.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> snapChat = dataSnapshot.getChildren();

                                historyChatList.clear();

                                for (DataSnapshot itemChat : snapChat) {
                                    Chat chat = itemChat.getValue(Chat.class);

                                    String timeStampQuery = Utils.getHourDateFromTimeStamp(Long.parseLong(chat.getTimeStamp()));

                                    lastTimeStampDate = new Date(timeStampQuery);

                                    mapHistoryChat.put(keyFriend, lastTimeStampDate);

                                    //Toast.makeText(getApplicationContext(), mapChatRoom+"", Toast.LENGTH_LONG).show();


                                    HashMap<String, Date> mapChatRoomFinal = new HashMap<>();

                                    mapChatRoomFinal = sortByValues(mapHistoryChat);

                                    for (Map.Entry<String, Date> entryFinal : mapChatRoomFinal.entrySet()) {

                                        String idFriendFinal = entryFinal.getKey();

                                        Date lastTimeStampFinal = entryFinal.getValue();

                                        String lastTimeStampStrFinal = DateFormat.format("dd/MM/yy hh:mm", lastTimeStampFinal).toString();

                                        Chat idDateFinal = new Chat(idFriendFinal, lastTimeStampStrFinal);

                                        historyChatList.add(idDateFinal);

                                        //Toast.makeText(getApplicationContext(), mapChatRoomFinal+"", Toast.LENGTH_LONG).show();

                                        adapterHistoryChat = new AdapterHistoryChat(getApplicationContext(), historyChatList, HistoryChatActivity.this);
                                        adapterHistoryChat.notifyDataSetChanged();

                                        rvHistoryChat.setAdapter(adapterHistoryChat);

                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                    } else {


                        lastNodeChat.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Iterable<DataSnapshot> snapChat = dataSnapshot.getChildren();

                                for (DataSnapshot itemChat : snapChat) {
                                    Chat chat = itemChat.getValue(Chat.class);

                                    //Toast.makeText(getApplicationContext(), contentQuery, Toast.LENGTH_LONG).show();

                                    String timeStampQuery = Utils.getHourDateFromTimeStamp(Long.parseLong(chat.getTimeStamp()));

                                    lastTimeStampDate = new Date(timeStampQuery);

                                    //Toast.makeText(getApplicationContext(), lastTimeStampDate+"", Toast.LENGTH_LONG).show();

                                    mapHistoryChat.put(keyFriend, lastTimeStampDate);


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

 */

    }

    @Override
    protected void onResume() {
        super.onResume();

        linearLayoutManager = new LinearLayoutManager(HistoryChatActivity.this);
        rvHistoryChat.setHasFixedSize(false);
        rvHistoryChat.setLayoutManager(linearLayoutManager);
        //rvChatRoom.setDividerHeight(1);

        mapHistoryChat.clear();

        refEmployees.child(saleManEmail).child("friend").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {

                int i = 0;

                Iterable<DataSnapshot> snapFriend = dataSnapshot.getChildren();

                for (final DataSnapshot itemFriend : snapFriend) {

                    i = i + 1;

                    final int countKeyFriend = (int) dataSnapshot.getChildrenCount();

                    final String keyFriend = itemFriend.getKey();

                    if(i == countKeyFriend){

                        refEmployees.child(saleManEmail).child("friend").child(keyFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                historyChatList.clear();

                                ChatHistoryModel chat = dataSnapshot.getValue(ChatHistoryModel.class);

                                Long lastTimeStampLong = Long.parseLong(chat.getLastTimeStamp());

                                //String lastTimeStamp = Utils.getHourDateFromTimeStamp(Long.parseLong(chat.getLastTimeStamp()));

                                //lastTimeStampDate = new Date(lastTimeStamp);

                                mapHistoryChat.put(keyFriend, lastTimeStampLong);

                                Map<String,Long> sortMap = new HashMap<>();

                                sortMap = sortByValues(mapHistoryChat);

                                for (Map.Entry<String, Long> entryFinal : sortMap.entrySet()) {

                                    String idFriendFinal      = entryFinal.getKey();
                                    Long  lastTimeStampLongFinal = entryFinal.getValue();

                                    //Date lastTimeStampFinal = entryFinal.getValue();

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

                        refEmployees.child(saleManEmail).child("friend").child(keyFriend).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                ChatHistoryModel chat = dataSnapshot.getValue(ChatHistoryModel.class);

                                Long lastTimeStampLong = Long.parseLong(chat.getLastTimeStamp());

                                //lastTimeStampDate = new Date(lastTimeStamp);

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


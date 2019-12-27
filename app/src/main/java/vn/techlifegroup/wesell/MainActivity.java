package vn.techlifegroup.wesell;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import vn.techlifegroup.wesell.bytask.ActionList;
import vn.techlifegroup.wesell.bytask.DeliveryManActivity;
import vn.techlifegroup.wesell.bytask.debt.DebtManActivity;
import vn.techlifegroup.wesell.bytask.distribution.DistributionManActivity;
import vn.techlifegroup.wesell.bytask.warehouse.WarehouseManActivity;
import vn.techlifegroup.wesell.pos.PosActivity;
import vn.techlifegroup.wesell.pos.ShopChainActivity;
import vn.techlifegroup.wesell.pos.ShopManagerActivity;
import vn.techlifegroup.wesell.utils.Constants;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;
import static vn.techlifegroup.wesell.utils.Constants.refRole;

public class MainActivity extends AppCompatActivity {

    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private String defaultTrial,default_saleman,userToken;
    private String userName;
    private String userEmail;
    private String userPass;
    private FirebaseUser user;

    public static String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    public static String userPhone = FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    public static DatabaseReference refUser = refDatabase.child("Users");

    public static DatabaseReference refUserUid = refDatabase.child("Users").child(userUid);
    public static DatabaseReference refBlockChain = refDatabase.child("BlockChain");
    public static int POINT_VALUE = 1000;
    public static int BLOCK_HEIGHT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent it = this.getIntent();
        userName = it.getStringExtra("UserName");
        userPhone = it.getStringExtra("UserPhone");
        userPass = it.getStringExtra("UserPass");


        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        defaultTrial = mFirebaseRemoteConfig.getString("default_trial");
        default_saleman = mFirebaseRemoteConfig.getString("default_saleman");

        fetchValue();

        DateFormat df = new SimpleDateFormat("dd/MM/yy");
        final String date = df.format(Calendar.getInstance().getTime());

        user = FirebaseAuth.getInstance().getCurrentUser();

        userEmail = user.getEmail().replace(".",",");
        //Toast.makeText(getApplicationContext(),userEmail, Toast.LENGTH_LONG).show();

        refRole.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(userEmail)){
                    refRole.child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            final String userRole = dataSnapshot.getValue().toString();
                            Intent intent = new Intent();

                            switch (userRole) {

                                case "SaleMan":
                                    intent = new Intent(MainActivity.this, ActionList.class);
                                    intent.putExtra("SaleMan",true);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    break;

                                case "Supervisor":
                                    intent = new Intent(MainActivity.this, ActionList.class);
                                    intent.putExtra("Supervisor",true);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    break;



                                default: {
                                    intent = new Intent(MainActivity.this, ActionList.class);
                                    intent.putExtra("SaleMan",true);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    break;
                                }
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Tài khoản không tồn tại! Vui lòng liên hệ với Quản trị hệ thống!");
                    builder.setPositiveButton("Đồng ý", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            Intent it = new Intent(getApplicationContext(),MainActivity.class);
                            it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(it);
                        }
                    }).show();


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void fetchValue() {

        long cacheExpiration = 3600; // 1 hour in seconds.
        // If in developer mode cacheExpiration is set to 0 so each fetch will retrieve values from
        // the server.
        if (mFirebaseRemoteConfig.getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }


        mFirebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            //Toast.makeText(MainActivity.this, "Fetch Succeeded",Toast.LENGTH_SHORT).show();
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            //Toast.makeText(MainActivity.this, "Fetch Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END fetch_config_with_callback]

    }

    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }
}
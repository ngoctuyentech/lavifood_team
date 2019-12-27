package vn.techlifegroup.wesell;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import java.util.Calendar;

import vn.techlifegroup.wesell.model.Cash;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.model.SubBlock;
import vn.techlifegroup.wesell.model.TransactionModel;
import vn.techlifegroup.wesell.utils.Constants;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import vn.techlifegroup.wesell.utils.Utils;

import static vn.techlifegroup.wesell.MainActivity.BLOCK_HEIGHT;
import static vn.techlifegroup.wesell.MainActivity.POINT_VALUE;
import static vn.techlifegroup.wesell.MainActivity.refBlockChain;
import static vn.techlifegroup.wesell.MainActivity.refUserUid;
import static vn.techlifegroup.wesell.utils.Constants.refDatabase;


public class ScanCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView mScannerView;
    private FirebaseAuth mAuth;
    private String clientCode;
    private DatabaseReference refClient,refApprovedCashRequest;
    private static int MY_REQUEST_CODE = 1;
    private ValueEventListener eventBlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_code);


        //mScannerView = (ZXingScannerView) findViewById(R.id.scan_activity_view);

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);

        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();

        //constraintLayout = (RelativeLayout)findViewById(R.id.scan_code);

    }

    @Override
    protected void onResume() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(),android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        MY_REQUEST_CODE);
            }

        }else{
            mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            mScannerView.startCamera();          // Start camera on resume
        }

        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
                mScannerView.startCamera();          // Start camera on resume
            }

        }
    }

    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(final Result result) {
         clientCode = result.toString();

         refClient = refDatabase.child("Users").child(clientCode);

        refApprovedCashRequest = refDatabase.child("1-System/Approved/CashRequest").child(clientCode);

        refApprovedCashRequest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){

                    refApprovedCashRequest.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Cash cash = dataSnapshot.getValue(Cash.class);
                            final float point = Float.parseFloat(cash.getCashValue().replace(",","").trim())/POINT_VALUE;
                            Toast.makeText(getApplicationContext(), cash.getCashValue(), Toast.LENGTH_LONG).show();

                            refBlockChain.child("AdminAccount/beingCalled").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    boolean blockBeingUsed = dataSnapshot.getValue(Boolean.class);

                                    if(blockBeingUsed){

                                        Utils.showProgressDialog(ScanCodeActivity.this);

                                        eventBlock = refBlockChain.child("AdminAccount/beingCalled").addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                boolean blockBeingUsed = dataSnapshot.getValue(Boolean.class);
                                                if(!blockBeingUsed){

                                                    Utils.addPoint(point, getApplicationContext(), ScanCodeActivity.this, "AdminLavifood",clientCode, refApprovedCashRequest);
                                                    refBlockChain.child("AdminAccount/beingCalled").removeEventListener(eventBlock);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                                    }else{
                                        Utils.addPoint(point, getApplicationContext(), ScanCodeActivity.this, "AdminLavifood",clientCode, refApprovedCashRequest);

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }else{
                    Toast.makeText(getApplicationContext(),"Không có yêu cầu rút tiền hoặc yêu cầu đang chờ duyệt!",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onBackPressed() {
        mScannerView.stopCamera();          // Stop camera on pause

        super.onBackPressed();
    }
}

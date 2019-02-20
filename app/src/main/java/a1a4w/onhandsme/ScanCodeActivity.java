package a1a4w.onhandsme;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

import a1a4w.onhandsme.utils.Constants;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ScanCodeActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView mScannerView;
    private RelativeLayout constraintLayout;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_code);

        mAuth = FirebaseAuth.getInstance();

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Toast.makeText(getApplicationContext(),"Lỗi truy cập hệ thống. Vui lòng thực hiện lại.",Toast.LENGTH_LONG).show();
                        }


                    }
                });


        //mScannerView = (ZXingScannerView) findViewById(R.id.scan_activity_view);

        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);

        //mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        //mScannerView.startCamera();

        //constraintLayout = (RelativeLayout)findViewById(R.id.scan_code);

    }

    @Override
    public void onResume() {
        super.onResume();
        mAuth.signInAnonymously()
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Toast.makeText(getApplicationContext(),"Lỗi truy cập hệ thống. Vui lòng thực hiện lại.",Toast.LENGTH_LONG).show();
                        }


                    }
                });
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }


    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
        mAuth.signOut();
// Stop camera on pause
    }

    @Override
    public void handleResult(final Result result) {
        String resultScan = result.toString();
        final String key = resultScan.substring(0,20);

        //showMessageDialog(key);
        messageDialog(key);
    }

    private void messageDialog(final String key ) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") final View dialogView = inflater.inflate(R.layout.dialog_scan, null);
        builder.setView(dialogView);
        final Dialog dialog = builder.create();
        dialog.show();

        final TextView tvVerifiedCode = (TextView)dialogView.findViewById(R.id.tv_dialog_scan_code);
        final ProgressBar pbScan = (ProgressBar)dialogView.findViewById(R.id.pB_dialog_scan);

        pbScan.setVisibility(View.VISIBLE);

        //tvVerifiedCode.setText("Đang truy xuất...");

        Constants.refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("QrCode")){
                    Constants.refDatabase.child("QrCode").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(key)){
                                Constants.refDatabase.child("QrCode").child(key).child("Value").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        String message = dataSnapshot.getValue().toString();
                                        tvVerifiedCode.setVisibility(View.VISIBLE);
                                        tvVerifiedCode.setText(message);
                                        pbScan.setVisibility(View.GONE);
                                        mAuth.signOut();

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                tvVerifiedCode.setVisibility(View.VISIBLE);
                                tvVerifiedCode.setText("Code không hợp lệ");
                                pbScan.setVisibility(View.GONE);
                                mAuth.signOut();

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else{
                    tvVerifiedCode.setVisibility(View.VISIBLE);
                    tvVerifiedCode.setText("Code không hợp lệ");
                    pbScan.setVisibility(View.GONE);
                    mAuth.signOut();

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
        mAuth.signOut();

        super.onBackPressed();
    }
}

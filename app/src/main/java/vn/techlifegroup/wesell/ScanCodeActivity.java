package vn.techlifegroup.wesell;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    private static ProgressDialog mProgressDialog;
    private String clientCode;
    private DatabaseReference refClient,refApprovedCashRequest;

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
         clientCode = result.toString();

         refClient = refDatabase.child("Users").child(clientCode);

        refApprovedCashRequest = refDatabase.child("1-System/Approved/CashRequest").child(clientCode);

        refApprovedCashRequest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChildren()){

                }else{
                    Toast.makeText(getApplicationContext(),"Không có yêu cầu rút tiền hoặc yêu cầu đang chờ duyệt!",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

        refDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("QrCode")){
                    refDatabase.child("QrCode").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(key)){
                                refDatabase.child("QrCode").child(key).child("Value").addListenerForSingleValueEvent(new ValueEventListener() {
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

    public void addPoint(final float accPoint, final Context context, final Activity activiy, final String sender) {

        showProgressDialog(activiy);

        refUserUid.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("Transaction")){
                    Query lastUserTx = refUserUid.child("Transaction").limitToLast(1);
                    lastUserTx.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> txSnap = dataSnapshot.getChildren();
                            TransactionModel tx = txSnap.iterator().next().getValue(TransactionModel.class);
                            String blockKey = tx.getBlockKey();
                            String parentBlock = tx.getParentBlock();
                            String txAgent = tx.getAgent();
                            String type = tx.getType();

                            refBlockChain.child("AdminAccount").child("beingCalled").setValue(true);


                            if(type.equals("receiver")){
                                refBlockChain.child(parentBlock).child(blockKey).child("transaction/receiverBalance").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userBL = dataSnapshot.getValue().toString();

                                        refBlockChain.child("AdminAccount").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("Transaction")){
                                                    Query querySender = refBlockChain.child("AdminAccount").child("Transaction").limitToLast(1);
                                                    querySender.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Iterable<DataSnapshot> txSnap = dataSnapshot.getChildren();
                                                            for (DataSnapshot txItem:txSnap){
                                                                TransactionModel txBlockInfo =txItem.getValue(TransactionModel.class);
                                                                final String blockKey = txBlockInfo.getBlockKey();

                                                                refBlockChain.child("CurrentSubChain/name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        String subName = dataSnapshot.getValue().toString();

                                                                        refBlockChain.child(subName).child(blockKey).child("transaction").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                TransactionModel adminTx = dataSnapshot.getValue(TransactionModel.class);
                                                                                final String senderBalance = adminTx.getSenderBalance();

                                                                                updateSubChain(senderBalance,userBL,accPoint,context,activiy,sender);
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

                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }else{
                                                    updateSubChain("9999999999",userBL,accPoint,context,activiy,sender);

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

                            }else {
                                refBlockChain.child(parentBlock).child(blockKey).child("transaction/senderBalance").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        final String userBL = dataSnapshot.getValue().toString();

                                        refBlockChain.child("AdminAccount").child("beingCalled").setValue("true");
                                        refBlockChain.child("AdminAccount").addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.hasChild("Transaction")){
                                                    Query querySender = refBlockChain.child("AdminAccount").child("Transaction").limitToLast(1);
                                                    querySender.addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Iterable<DataSnapshot> txSnap = dataSnapshot.getChildren();
                                                            for (DataSnapshot txItem:txSnap){
                                                                TransactionModel txBlockInfo =txItem.getValue(TransactionModel.class);
                                                                final String blockKey = txBlockInfo.getBlockKey();

                                                                refBlockChain.child("CurrentSubChain/name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                    @Override
                                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                                        String subName = dataSnapshot.getValue().toString();

                                                                        refBlockChain.child(subName).child(blockKey).child("transaction").addListenerForSingleValueEvent(new ValueEventListener() {
                                                                            @Override
                                                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                                                TransactionModel adminTx = dataSnapshot.getValue(TransactionModel.class);
                                                                                final String senderBalance = adminTx.getSenderBalance();

                                                                                updateSubChain(senderBalance,userBL,accPoint,context,activiy,sender);
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

                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }else{
                                                    updateSubChain("9999999999",userBL,accPoint,context,activiy,sender);

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

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else{

                    refBlockChain.child("AdminAccount").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild("Transaction")){
                                Query querySender = refBlockChain.child("AdminAccount").child("Transaction").limitToLast(1);
                                querySender.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        Iterable<DataSnapshot> txSnap = dataSnapshot.getChildren();
                                        for (DataSnapshot txItem:txSnap){
                                            TransactionModel txBlockInfo =txItem.getValue(TransactionModel.class);
                                            final String blockKey = txBlockInfo.getBlockKey();

                                            refBlockChain.child("CurrentSubChain/name").addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    String subName = dataSnapshot.getValue().toString();

                                                    refBlockChain.child(subName).child(blockKey).child("transaction").addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            TransactionModel adminTx = dataSnapshot.getValue(TransactionModel.class);
                                                            final String senderBalance = adminTx.getSenderBalance();

                                                            updateSubChain(senderBalance,"0",accPoint,context,activiy,sender);
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

                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }else{
                                updateSubChain("999999999","0",accPoint,context,activiy,sender);

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void updateSubChain(final String adminBalance, final String receiverBalance, final float accPoint, final Context context, final Activity activity, final String sender) {

        refBlockChain.child("CurrentSubChain").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                SubBlock subBlock = dataSnapshot.getValue(SubBlock.class);
                final String subname = subBlock.getName();
                final int txTotal = Integer.parseInt(subBlock.getTxTotal());

                Query queryLast = refBlockChain.child(subname).limitToLast(1);
                queryLast.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> blockSnap = dataSnapshot.getChildren();

                        for(DataSnapshot itemBlock:blockSnap){
                            SubBlock lastBlock = itemBlock.getValue(SubBlock.class);
                            final String timeStamp = Calendar.getInstance().getTime().getTime()+"";

                            String updateAdminBL = (Double.parseDouble(adminBalance)-accPoint)+"";
                            String updateUserBL = (Double.parseDouble(receiverBalance)+accPoint)+"";

                            final TransactionModel transaction = new TransactionModel(
                                    sender,
                                    clientCode,
                                    accPoint+"",
                                    updateAdminBL,
                                    updateUserBL,
                                    timeStamp
                            );

                            final String updateBlockHash = new SubBlock(
                                    lastBlock.getHash(),transaction
                            ).calculateHash();

                            final SubBlock newBlock = new SubBlock(
                                    lastBlock.getIndex()+1,
                                    lastBlock.getHash(),
                                    transaction,
                                    updateBlockHash,
                                    timeStamp
                            );

                            if(txTotal<BLOCK_HEIGHT){

                                refBlockChain.child("CurrentSubChain").child("txTotal").setValue(txTotal+1+"");

                                final DatabaseReference blockPush = refBlockChain.child(subname).push();
                                final TransactionModel blockInfo = new TransactionModel("SubChain",updateBlockHash,blockPush.getKey(),"MyCompany","receiver");

                                blockPush.setValue(newBlock).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                        builder.setCancelable(false);

                                        builder.setMessage("Đã xác nhận trừ khoản tiền là " + Utils.convertNumber(accPoint * POINT_VALUE +"" + " đ"));

                                        builder.setPositiveButton("Xong", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        }).show();

                                        refClient.child("Transaction").child(blockPush.getKey()).setValue(blockInfo);
                                        refApprovedCashRequest.setValue(null);

                                        refBlockChain.child("AdminAccount/Transaction").child(blockPush.getKey()).setValue(blockInfo);
                                        refBlockChain.child("AdminAccount").child("beingCalled").setValue(false);

                                        hideProgressDialog();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context,"Giao dịch lỗi, vui lòng liên hệ với Xom Nho!",Toast.LENGTH_LONG).show();
                                    }
                                });

                            }else{
                                refBlockChain.child("SubChainList").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        long itemCount = dataSnapshot.getChildrenCount();
                                        String subName = "SubChain"+(itemCount+1);

                                        refBlockChain.child("CurrentSubChain").child("name").setValue(subName);
                                        refBlockChain.child("CurrentSubChain").child("txTotal").setValue("1");

                                        final DatabaseReference blockPush = refBlockChain.child(subName).push();

                                        final TransactionModel blockInfo = new TransactionModel(subName,updateBlockHash,blockPush.getKey(),"MyCompany","receiver");

                                        blockPush.setValue(newBlock).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                                                builder.setCancelable(false);

                                                builder.setMessage("Đã xác nhận trừ số tiền là " + Utils.convertNumber(accPoint*POINT_VALUE +"") + " từ ví thưởng của Khách hàng!");
                                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                }).show();

                                                refClient.child("Transaction").child(blockPush.getKey()).setValue(blockInfo);
                                                refApprovedCashRequest.setValue(null);

                                                refBlockChain.child("AdminAccount/Transaction").child(blockPush.getKey()).setValue(blockInfo);
                                                refBlockChain.child("AdminAccount").child("beingCalled").setValue(false);


                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(context,"Giao dịch lỗi, vui lòng liên hệ với Xom Nho!",Toast.LENGTH_LONG).show();
                                            }
                                        });

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

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public static void showProgressDialog(Activity context) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage(context.getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();

    }

    public static void  hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    @Override
    public void onBackPressed() {
        mScannerView.stopCamera();          // Stop camera on pause
        mAuth.signOut();

        super.onBackPressed();
    }
}

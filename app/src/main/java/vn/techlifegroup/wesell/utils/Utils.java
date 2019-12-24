package vn.techlifegroup.wesell.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.print.PageRange;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import vn.techlifegroup.wesell.MainActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.SubBlock;
import vn.techlifegroup.wesell.model.TransactionModel;

import static vn.techlifegroup.wesell.MainActivity.BLOCK_HEIGHT;
import static vn.techlifegroup.wesell.MainActivity.POINT_VALUE;
import static vn.techlifegroup.wesell.MainActivity.refBlockChain;
import static vn.techlifegroup.wesell.MainActivity.refUser;
import static vn.techlifegroup.wesell.MainActivity.refUserUid;

/**
 * Created by toila on 08/01/2017.
 */

public class Utils {

    private static ProgressDialog mProgressDialog;

    public abstract static class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {

        public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

        private int previousTotal = 0; // The total number of items in the dataset after the last load
        private boolean loading = true; // True if we are still waiting for the last set of data to load.
        private int visibleThreshold = 3; // The minimum amount of items to have below your current scroll position before loading more.
        int firstVisibleItem, visibleItemCount, totalItemCount;

        private int current_page = 1;

        private LinearLayoutManager mLinearLayoutManager;

        public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
            this.mLinearLayoutManager = linearLayoutManager;
        }

        public static String applySha256(String input){
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                //Applies sha256 to our input,
                byte[] hash = digest.digest(input.getBytes("UTF-8"));
                StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
                for (int i = 0; i < hash.length; i++) {
                    String hex = Integer.toHexString(0xff & hash[i]);
                    if(hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString();
            }
            catch(Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);

            visibleItemCount = recyclerView.getChildCount();
            totalItemCount = mLinearLayoutManager.getItemCount();
            firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount)
                    <= (firstVisibleItem + visibleThreshold)) {
                // End has been reached

                // Do something
                current_page++;

                onLoadMore(current_page);

                loading = true;
            }
        }

        public abstract void onLoadMore(int current_page);


    }
    public static HashMap sortIncreaseByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public static HashMap sortDecreaseByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public static void addImage(Document document, byte[] byteArray)
    {
        Image image = null;
        try
        {
            image = Image.getInstance(byteArray);
            image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
            image.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());

        }
        catch (BadElementException | IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // image.scaleAbsolute(150f, 150f);
        try
        {
            document.add(image);
        } catch (DocumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getDate(String timeStampStr){

        try{
            @SuppressLint("SimpleDateFormat") DateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm");
            Date netDate = (new Date(Long.parseLong(timeStampStr)));
            return sdf.format(netDate);
        }
        catch(Exception ex){
            return "";
        }
    }

    public static String getHourDateFromTimeStamp(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = android.text.format.DateFormat.format("dd/MM/yy hh:mm", cal).toString();
        return date;
    }

    public static String getHourFromTimeStamp(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = android.text.format.DateFormat.format("hh:mm aa", cal).toString();
        return date;
    }

    public static String getDateFromTimeStamp(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = android.text.format.DateFormat.format("dd/MM/yyyy", cal).toString();
        return date;
    }

    public static String convertNumber(String numString){
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        float numStringFloat = Float.parseFloat(numString);
        String covertNum = numberFormat.format(numStringFloat);
        return covertNum;
    }

    public static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue())
                        .compareTo(((Map.Entry) (o1)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

    public static String getDateCurrentTimeZone(long timestamp) {
        try{
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(timestamp);
            Date currenTimeZone = c.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy HH:mm");
            return sdf.format(currenTimeZone);
        }catch (Exception e) {
        }
        return "";
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean pageRangesContainPage(int pageNumber, PageRange[] ranges)
    {
        for(PageRange range : ranges) {
            if(pageNumber >= range.getStart() && pageNumber <= range.getEnd()) {
                return true;
            }
        }
        return false;
    }

    public static String applySha256(String input){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //Applies sha256 to our input,
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer(); // This will contain hash as hexidecimal
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void addPoint(final float accPoint, final Context context, final Activity activiy, final String sender, final String receiver, final DatabaseReference refApproved) {

        showProgressDialog(activiy);

        final DatabaseReference refUserUid = refUser.child(receiver);

        refUserUid.child(receiver).addListenerForSingleValueEvent(new ValueEventListener() {
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

                                                                                updateSubChain(senderBalance,userBL,accPoint,context,activiy,sender,receiver,refApproved);
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
                                                    updateSubChain("9999999999",userBL,accPoint,context,activiy,sender,receiver,refApproved);

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

                                                                                updateSubChain(senderBalance,userBL,accPoint,context,activiy,sender,receiver,refApproved);
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
                                                    updateSubChain("9999999999",userBL,accPoint,context,activiy,sender,receiver,refApproved);

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

                                                            updateSubChain(senderBalance,"0",accPoint,context,activiy,sender,receiver,refApproved);
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
                                updateSubChain("999999999","0",accPoint,context,activiy,sender,receiver,refApproved);

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

    private static void updateSubChain(final String adminBalance, final String receiverBalance, final float accPoint, final Context context, final Activity activity,
                                final String sender, final String receiver, final DatabaseReference refApproved) {

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
                                    receiver,
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

                                        builder.setMessage("Xác nhận thanh toán khoản tiền là " + Utils.convertNumber(accPoint * POINT_VALUE +"") + " đ, từ tài khoản "
                                        + sender + " đến tài khoản " + receiver + ".");

                                        builder.setPositiveButton("Xong", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                refApproved.setValue(null);

                                                Intent it = new Intent(context, MainActivity.class);
                                                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                context.startActivity(it);

                                            }
                                        }).show();

                                        refUser.child(receiver).child("Transaction").child(blockPush.getKey()).setValue(blockInfo);

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

                                                builder.setMessage("Xác nhận thanh toán khoản tiền là " + Utils.convertNumber(accPoint * POINT_VALUE +"") + " đ, từ tài khoản "
                                                        + sender + " đến tài khoản " + receiver + ".");

                                                builder.setPositiveButton("Xong", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        refApproved.setValue(null);

                                                        Intent it = new Intent(context, MainActivity.class);
                                                        it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                        context.startActivity(it);

                                                    }
                                                }).show();

                                                refUserUid.child("Transaction").child(blockPush.getKey()).setValue(blockInfo);

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



    public static File createFileFromInputStream(InputStream inputStream,String pathName) {

        try{
            File f = new File(pathName);
            OutputStream outputStream = new FileOutputStream(f);
            byte buffer[] = new byte[1024];
            int length = 0;

            while((length=inputStream.read(buffer)) > 0) {
                outputStream.write(buffer,0,length);
            }

            outputStream.close();
            inputStream.close();

            return f;
        }catch (IOException e) {
            //Logging exception
        }

        return null;
    }

    public static Bitmap getImageFromAssetsFile(Context ctx, String fileName) {
        Bitmap image = null;
        AssetManager am = ctx.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;

    }

     public static class NumberTextWatcherForThousand implements TextWatcher {

        EditText editText;


        public NumberTextWatcherForThousand(EditText editText) {
            this.editText = editText;


        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            try {
                editText.removeTextChangedListener(this);
                String value = editText.getText().toString();


                if (value != null && !value.equals("")) {

                    if (value.startsWith(".")) {
                        editText.setText("0.");
                    }
                    if (value.startsWith("0") && !value.startsWith("0.")) {
                        editText.setText("");

                    }


                    String str = editText.getText().toString().replaceAll(",", "");
                    if (!value.equals(""))
                        editText.setText(getDecimalFormattedString(str));
                    editText.setSelection(editText.getText().toString().length());
                }
                editText.addTextChangedListener(this);
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                editText.addTextChangedListener(this);
            }

        }


    }

    public static String getDecimalFormattedString(String value)     {
        StringTokenizer lst = new StringTokenizer(value, ".");
        String str1 = value;
        String str2 = "";
        if (lst.countTokens() > 1)
        {
            str1 = lst.nextToken();
            str2 = lst.nextToken();
        }
        String str3 = "";
        int i = 0;
        int j = -1 + str1.length();
        if (str1.charAt( -1 + str1.length()) == '.')
        {
            j--;
            str3 = ".";
        }
        for (int k = j;; k--)
        {
            if (k < 0)
            {
                if (str2.length() > 0)
                    str3 = str3 + "." + str2;
                return str3;
            }
            if (i == 3)
            {
                str3 = "," + str3;
                i = 0;
            }
            str3 = str1.charAt(k) + str3;
            i++;
        }

    }

    public static LatLng getLocationFromAddress(Context context,String strAddress) {

        Geocoder coder = new Geocoder(context);
        List<Address> address;
        LatLng p1 = null;

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null) {
                return null;
            }

            Address location = address.get(0);
            p1 = new LatLng(location.getLatitude(), location.getLongitude() );

        } catch (IOException ex) {

            ex.printStackTrace();
        }

        return p1;
    }

}

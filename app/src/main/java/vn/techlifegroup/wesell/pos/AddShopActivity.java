package vn.techlifegroup.wesell.pos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Shop;
import vn.techlifegroup.wesell.utils.Constants;

public class AddShopActivity extends AppCompatActivity {
    ImageView ivRotatePic, ivShopPic,ivLibrary,ivCheckPic;
    EditText edtShopName, edtShopPhone,edtShopAddress;
    Spinner spinShop;
    private int PICK_IMAGE_REQUEST = 1;
    private static final int PLACE_PICKER_REQUEST = 3;
    private Bitmap bmp;
    private int counter=0;
    private ProgressDialog mProgressDialog;
    private Bundle b = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_shop);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_shop);
        setSupportActionBar(toolbar);

        ivShopPic = (ImageView)findViewById(R.id.iv_add_shop_image);
        ivLibrary = (ImageView)findViewById(R.id.iv_add_shop_library);
        ivRotatePic = (ImageView)findViewById(R.id.iv_add_shop_rotatepic);
        ivCheckPic = (ImageView)findViewById(R.id.iv_add_shop_checkpic);

        edtShopName = (EditText)findViewById(R.id.edt_add_shop_name);
        edtShopPhone = (EditText)findViewById(R.id.edt_add_shop_phone);
        edtShopAddress = (EditText)findViewById(R.id.edt_add_shop_address);



        ivLibrary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always show the chooser (if there are multiple options available)
                startActivityForResult(Intent.createChooser(intent, "Chọn hình ảnh"), PICK_IMAGE_REQUEST);
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));
                if (bmp !=null) {
                    ivShopPic.setImageBitmap(bmp);
                    ivRotatePic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            counter++ ;

                            Matrix matrix = new Matrix();
                            matrix.postRotate(counter*90);
                            Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                            ivShopPic.setImageBitmap(resizedBitmap);

                        }
                    });

                    ivCheckPic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //progressBar.setVisibility(View.VISIBLE);
                            showProgressDialog();
                            if(ivShopPic !=null){
                                ivShopPic.setDrawingCacheEnabled(true);
                                ivShopPic.buildDrawingCache();
                                Bitmap bitmapUp = ivShopPic.getDrawingCache();
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                bitmapUp.compress(Bitmap.CompressFormat.PNG, 50, stream);
                                byte[] databitmap = stream.toByteArray();

                                long timeStamp = Calendar.getInstance().getTime().getTime();

                                StorageReference chefImagesRef = Constants.storageRef.child("EmployeePic")
                                        .child(timeStamp+"");

                                UploadTask uploadTask = chefImagesRef.putBytes(databitmap);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                        String imgUrl = "https:" + taskSnapshot.getDownloadUrl().getEncodedSchemeSpecificPart();
                                        b.putString("ShopUrl", imgUrl);
                                        hideProgressDialog();
                                    }
                                });
                            }
                        }
                    });

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_shop,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add_shop){
            showProgressDialog();
            String shopName = edtShopName.getText().toString();
            String shopPhone = edtShopPhone.getText().toString();
            String shopAddress = edtShopAddress.getText().toString().replace(".",",");
            String shopUrl = b.getString("ShopUrl");

            if(TextUtils.isEmpty(shopName)){
                Toast.makeText(getApplicationContext(), "Vui lòng nhập tên cửa hàng", Toast.LENGTH_LONG).show();

            }else if(TextUtils.isEmpty(shopPhone)){
                Toast.makeText(getApplicationContext(), "Vui lòng nhập số điện thoại cửa hàng", Toast.LENGTH_LONG).show();

            }
            else if(TextUtils.isEmpty(shopAddress)){
                Toast.makeText(getApplicationContext(), "Vui lòng nhập địa chỉ cửa hàng", Toast.LENGTH_LONG).show();

            }else if(bmp==null){
                Toast.makeText(getApplicationContext(), "Vui lòng chọn hình ảnh cửa hàng và nhấn vào biểu tượng Chọn hình", Toast.LENGTH_LONG).show();
            }
            else {

                final Shop shop = new Shop (shopName,shopPhone,shopAddress,shopUrl);

                Constants.refShopPOS.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        long shopOrder = dataSnapshot.getChildrenCount()+1;
                        String shopCode = "CH"+shopOrder;
                        Constants.refShopPOS.child(shopCode).setValue(shop);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

}

package vn.techlifegroup.wesell.pos;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import vn.techlifegroup.wesell.MainActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Employee;
import vn.techlifegroup.wesell.model.Shop;
import vn.techlifegroup.wesell.utils.Constants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class AddEmployeeActivity extends AppCompatActivity {
    ImageView ivEmployeePic, ivRotatePic,ivLibrary,ivCheckPic;
    EditText edtEmployeeName, edtEmployeePhone,edtEmployeeEmail,edtEmployeeRole;
    private LinearLayoutManager linearLayoutManager;
    TextView tvChooseShop;
    Spinner spinShop;
    private int PICK_IMAGE_REQUEST = 1;
    private static final int PLACE_PICKER_REQUEST = 3;
    private Bitmap bmp;
    private int counter=0;
    private ProgressDialog mProgressDialog;
    private Bundle b = new Bundle();
    private FirebaseRecyclerAdapter<Shop,ShopViewHolder> adapterFirebase;
    private ProgressBar progressBar;
    private String emailLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_employee);
        ivEmployeePic = (ImageView)findViewById(R.id.iv_add_employee_image);
        ivLibrary = (ImageView)findViewById(R.id.iv_add_employee_library);
        ivRotatePic = (ImageView)findViewById(R.id.iv_add_employee_rotatepic);
        ivCheckPic = (ImageView)findViewById(R.id.iv_add_employee_checkpic);
        tvChooseShop = (TextView)findViewById(R.id.tv_add_employee_chooseShop);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_add_employee);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

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


        edtEmployeeName = (EditText)findViewById(R.id.edt_add_employee_name);
        edtEmployeePhone = (EditText)findViewById(R.id.edt_add_employee_phone);
        edtEmployeeEmail = (EditText)findViewById(R.id.edt_add_employee_email);
        edtEmployeeRole = (EditText)findViewById(R.id.edt_add_employee_role);

        tvChooseShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                AlertDialog.Builder builder = new AlertDialog.Builder(AddEmployeeActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                View dialogView = inflater.inflate(R.layout.dialog_shop_list,null);
                builder.setView(dialogView);

                final RecyclerView shopList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_shop_list);
                //progressBar = (ProgressBar)dialogView.findViewById(R.id.pB_dialog_shop_list);
             //   progressBar.setVisibility(View.VISIBLE);
                shopList.setHasFixedSize(true);
                linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                shopList.setLayoutManager(linearLayoutManager);

                adapterFirebase = new FirebaseRecyclerAdapter<Shop, ShopViewHolder>(
                        Shop.class,
                        R.id.item_shop,
                        ShopViewHolder.class,
                        Constants.refDatabase.child(emailLogin).child("Z_POS_Shop")
                ) {
                    @Override
                    public ShopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop,parent,false);
                        return new ShopViewHolder(v);
                    }


                    @Override
                    protected void populateViewHolder(ShopViewHolder viewHolder, Shop model, int position) {
                        viewHolder.shopName.setText(model.getShopName());
                        viewHolder.shopAddress.setText(model.getShopAddress());
                        Glide.with(getApplicationContext()).load(model.getShopUrl()).error(R.drawable.storefront).fitCenter().override(300,200).into(viewHolder.shopPic);


                    }
                };

                shopList.setAdapter(adapterFirebase);
                adapterFirebase.notifyDataSetChanged();

                builder.show();
               // progressBar.setVisibility(View.GONE);
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
                    ivEmployeePic.setImageBitmap(bmp);
                    ivRotatePic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            counter++ ;

                            Matrix matrix = new Matrix();
                            matrix.postRotate(counter*90);
                            Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                            ivEmployeePic.setImageBitmap(resizedBitmap);

                        }
                    });

                    ivCheckPic.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //progressBar.setVisibility(View.VISIBLE);
                            showProgressDialog();
                            if(ivEmployeePic !=null){
                                ivEmployeePic.setDrawingCacheEnabled(true);
                                ivEmployeePic.buildDrawingCache();
                                Bitmap bitmapUp = ivEmployeePic.getDrawingCache();
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
                                        b.putString("EmployeeUrl", imgUrl);
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
        getMenuInflater().inflate(R.menu.menu_add_employee,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_add_employee){
            showProgressDialog();
            String employeeName = edtEmployeeName.getText().toString();
            String employeePhone = edtEmployeePhone.getText().toString();
            String employeeEmail = edtEmployeeEmail.getText().toString().replace(".",",");
            String employeeRole = edtEmployeeRole.getText().toString();
            String employeeUrl = b.getString("EmployeeUrl");
            String employeeOrder = b.getString("EmployeeOrder");
            String shopCode = b.getString("ShopCode");
            String employeeRoleDatabase;

            if(shopCode == null ){
                Toast.makeText(getApplicationContext(), "Đang xử lý ...", Toast.LENGTH_LONG).show();

            }else if(TextUtils.isEmpty(employeeName)){
                Toast.makeText(getApplicationContext(), "Vui lòng nhập tên nhân viên", Toast.LENGTH_LONG).show();

            }else if(TextUtils.isEmpty(employeePhone)){
                Toast.makeText(getApplicationContext(), "Vui lòng nhập số điện thoại nhân viên", Toast.LENGTH_LONG).show();

            }
            else if(TextUtils.isEmpty(employeeEmail)){
                Toast.makeText(getApplicationContext(), "Vui lòng nhập số email cấp cho nhân viên", Toast.LENGTH_LONG).show();

            }
            else if(TextUtils.isEmpty(employeeRole)){
                Toast.makeText(getApplicationContext(), "Vui lòng nhập vai trò của nhân viên", Toast.LENGTH_LONG).show();

            }
            else {
                String employeeCode = "NV"+employeeOrder;
                if(employeeRole.equals("Bán hàng")){
                    employeeRoleDatabase = "Seller";

                }else{
                    employeeRoleDatabase = "StoreMan";
                }
                Employee employee = new Employee(employeeCode,employeeName,employeePhone,employeeUrl,employeeEmail,employeeRole,shopCode);

                Constants.refRole.child(employeeEmail.replace(".",",")).setValue(employeeRoleDatabase);
                Constants.refDatabase.child(emailLogin).child("Z_POS_Employee").child(employeeEmail.replace(".",",")).setValue(employee).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                });

            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class ShopViewHolder extends RecyclerView.ViewHolder {
        TextView shopName,shopAddress;
        ImageView shopPic;

        ShopViewHolder(View itemView) {
            super(itemView);
            shopName = (TextView) itemView.findViewById(R.id.tv_item_client_sale_name);
            shopAddress = (TextView) itemView.findViewById(R.id.tv_item_shop_address);
            shopPic = (ImageView)itemView.findViewById(R.id.iv_item_shop_pic);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    String itemKey = adapterFirebase.getRef(position).getKey();
                    b.putString("ShopCode",itemKey);
                    tvChooseShop.setText(shopName.getText().toString());
                }
            });



        }

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

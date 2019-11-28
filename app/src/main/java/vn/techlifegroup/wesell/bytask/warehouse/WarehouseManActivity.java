package vn.techlifegroup.wesell.bytask.warehouse;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import net.glxn.qrgen.android.QRCode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Random;

import vn.techlifegroup.wesell.LoginActivity;
import vn.techlifegroup.wesell.MainActivity;
import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.list.AddSupplierActivity;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.model.Product;
import vn.techlifegroup.wesell.model.Supplier;
import vn.techlifegroup.wesell.model.WarehouseIn;
import vn.techlifegroup.wesell.order.ViewOrderDetailActivity;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

import static vn.techlifegroup.wesell.utils.Constants.buttonClick;
import static vn.techlifegroup.wesell.utils.Constants.refDatabase;


public class WarehouseManActivity extends AppCompatActivity {

    private RecyclerView recyclerViewApproved, recyclerViewDenied, recyclerViewWarehouseout, recyclerViewPreparation;
    private FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderApproved> adapterFirebaseApproved;
    private FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderDenied> adapterFirebaseDenied;
    private FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderWarehouseout> adapterFirebaseWarehouseout;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct;

    private FirebaseRecyclerAdapter<Supplier, SupplierViewHolder> adapterFirebaseSupplier;

    private LinearLayoutManager linearLayoutManager;
    private LinearLayout boxApproved, boxUnApproved,boxCancelled;
    private DatabaseReference refApproved, refDenied, refWarehouseout, refPreparation;
    private TextView tvApproved, tvDenied, tvWarehouseOut;
    private Bundle b = new Bundle();
    private ProgressDialog mProgressDialog;
    private String thisYearString, thisMonthString, thisDateString, choosenProduct,emailLogin;
    private float productStorageEnd;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    private TextView tvChoosenProduct,tvProductInStorage;
    private Dialog dialogProductList;
    private String productQuantity;
    private String supplierName;
    private String productStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            b.clear();
        }
        setContentView(R.layout.activity_warehouse_man);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_warehouse);
        setSupportActionBar(toolbar);

        Intent intent = this.getIntent();
        emailLogin = intent.getStringExtra("EmailLogin");

        thisYearString = Calendar.getInstance().get(Calendar.YEAR) + "";
        thisMonthString = (Calendar.getInstance().get(Calendar.MONTH) + 1) + "";
        thisDateString = Calendar.getInstance().get(Calendar.DATE) + "";


        tvApproved = (TextView) findViewById(R.id.tv_warehouse_approved);
        tvDenied = (TextView) findViewById(R.id.tv_warehouse_denied);
        tvWarehouseOut = (TextView) findViewById(R.id.tv_warehouse_warehouseout);
        recyclerViewApproved = (RecyclerView) findViewById(R.id.order_approved_recyclerview_warehouse);
        recyclerViewDenied = (RecyclerView) findViewById(R.id.order_denied_recyclerview_warehouse);
        recyclerViewWarehouseout = (RecyclerView) findViewById(R.id.recyclerview_warehouseman_warehouseout);

        boxApproved = (LinearLayout)findViewById(R.id.warehouse_man_boxApproved);
        boxUnApproved = (LinearLayout)findViewById(R.id.warehouse_man_boxUnApproved);
        boxCancelled = (LinearLayout)findViewById(R.id.warehouse_man_boxCanceled);

        boxUnApproved.setBackgroundColor(Color.WHITE);
        recyclerViewWarehouseout.setVisibility(View.VISIBLE);
        tvWarehouseOut.setTextColor(getResources().getColor(R.color.colorAccent));

        boxApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                boxApproved.setBackgroundColor(Color.WHITE);
                boxCancelled.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxUnApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                recyclerViewApproved.setVisibility(View.VISIBLE);
                recyclerViewDenied.setVisibility(View.INVISIBLE);
                recyclerViewWarehouseout.setVisibility(View.INVISIBLE);

                tvApproved.setTextColor(getResources().getColor(R.color.colorAccent));
                tvWarehouseOut.setTextColor(getResources().getColor(android.R.color.white));
                tvDenied.setTextColor(getResources().getColor(android.R.color.white));
            }
        });

        boxCancelled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                boxCancelled.setBackgroundColor(Color.WHITE);
                boxApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxUnApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                recyclerViewApproved.setVisibility(View.INVISIBLE);
                recyclerViewDenied.setVisibility(View.VISIBLE);
                recyclerViewWarehouseout.setVisibility(View.INVISIBLE);

                tvDenied.setTextColor(getResources().getColor(R.color.colorAccent));
                tvWarehouseOut.setTextColor(getResources().getColor(android.R.color.white));
                tvApproved.setTextColor(getResources().getColor(android.R.color.white));
            }
        });

        boxUnApproved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                boxUnApproved.setBackgroundColor(Color.WHITE);
                boxApproved.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                boxCancelled.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                recyclerViewApproved.setVisibility(View.INVISIBLE);
                recyclerViewDenied.setVisibility(View.INVISIBLE);
                recyclerViewWarehouseout.setVisibility(View.VISIBLE);

                tvWarehouseOut.setTextColor(getResources().getColor(R.color.colorAccent));
                tvApproved.setTextColor(getResources().getColor(android.R.color.white));
                tvDenied.setTextColor(getResources().getColor(android.R.color.white));
            }
        });

        getApprovedOrder();
        getDeniedOrder();
        getWarehouseoutOrder();


    }

    private void getWarehouseoutOrder() {
        recyclerViewWarehouseout = (RecyclerView) findViewById(R.id.recyclerview_warehouseman_warehouseout);
        recyclerViewWarehouseout.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewWarehouseout.setLayoutManager(linearLayoutManager);

        refWarehouseout = Constants.refDatabase.child(emailLogin+"/Order").child("WarehouseOut");

        adapterFirebaseWarehouseout = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderWarehouseout>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderWarehouseout.class,
                refWarehouseout
        ) {
            @Override
            public OrderViewHolderWarehouseout onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                return new OrderViewHolderWarehouseout(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolderWarehouseout viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerViewWarehouseout.setAdapter(adapterFirebaseWarehouseout);
        adapterFirebaseWarehouseout.notifyDataSetChanged();
    }

    private void getApprovedOrder() {
        recyclerViewApproved = (RecyclerView) findViewById(R.id.order_approved_recyclerview_warehouse);
        recyclerViewApproved.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewApproved.setLayoutManager(linearLayoutManager);

        refApproved = Constants.refDatabase.child(emailLogin+"/Order").child("Approved");

        adapterFirebaseApproved = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderApproved>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderApproved.class,
                refApproved
        ) {
            @Override
            public OrderViewHolderApproved onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                return new OrderViewHolderApproved(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolderApproved viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerViewApproved.setAdapter(adapterFirebaseApproved);
        adapterFirebaseApproved.notifyDataSetChanged();
    }

    private void getDeniedOrder() {
        recyclerViewDenied = (RecyclerView) findViewById(R.id.order_denied_recyclerview_warehouse);
        recyclerViewDenied.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewDenied.setLayoutManager(linearLayoutManager);

        refDenied = Constants.refDatabase.child(emailLogin+"/Order").child("DeniedWarehouse");

        adapterFirebaseDenied = new FirebaseRecyclerAdapter<OrderDetail, OrderViewHolderDenied>(
                OrderDetail.class,
                R.id.order_cardview,
                OrderViewHolderDenied.class,
                refDenied
        ) {
            @Override
            public OrderViewHolderDenied onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
                return new OrderViewHolderDenied(v);
            }


            @Override
            protected void populateViewHolder(OrderViewHolderDenied viewHolder, OrderDetail model, int position) {
                viewHolder.orderName.setText(model.getOrderName());
            }
        };

        recyclerViewDenied.setAdapter(adapterFirebaseDenied);
        adapterFirebaseDenied.notifyDataSetChanged();
    }

    //ViewHolder
    private class OrderViewHolderApproved extends RecyclerView.ViewHolder {
        TextView orderName;

        OrderViewHolderApproved(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemRef = adapterFirebaseApproved.getRef(position);
                    String itemKeyString = itemRef.getKey();

                    Intent intent = new Intent(getApplicationContext(), ViewOrderDetailActivity.class);
                    intent.putExtra("OrderPushKey", itemKeyString);
                    intent.putExtra("EmailLogin", emailLogin);
                    intent.putExtra("Warehouse",true);
                    startActivity(intent);

                }
            });

        }
    }

    private class OrderViewHolderDenied extends RecyclerView.ViewHolder {
        TextView orderName;

        public OrderViewHolderDenied(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemRef = adapterFirebaseDenied.getRef(position);
                    String itemKeyString = itemRef.getKey();

                    Intent intent = new Intent(getApplicationContext(), ViewOrderDetailActivity.class);
                    intent.putExtra("OrderPushKey", itemKeyString);
                    intent.putExtra("EmailLogin", emailLogin);
                    intent.putExtra("Denied",true);
                    startActivity(intent);

                }
            });
        }
    }

    private class OrderViewHolderWarehouseout extends RecyclerView.ViewHolder {
        TextView orderName;

        public OrderViewHolderWarehouseout(View itemView) {
            super(itemView);
            orderName = (TextView) itemView.findViewById(R.id.tv_order_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemRef = adapterFirebaseWarehouseout.getRef(position);
                    String itemKeyString = itemRef.getKey();

                    Intent intent = new Intent(getApplicationContext(), ViewOrderDetailActivity.class);
                    intent.putExtra("OrderPushKey", itemKeyString);
                    intent.putExtra("EmailLogin", emailLogin);

                    startActivity(intent);


                }
            });


        }
    }

    //Product new stock
    private void chooseSupplierDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_choose_supplier, null);
        builder.setView(dialogView);
        builder.setTitle("Chọn Nhà cung cấp");

        final RecyclerView supplierList = (RecyclerView) dialogView.findViewById(R.id.recyclerView_dialog_choose_supplier_list);
        // Button btnAddSupplier = (Button)dialogView.findViewById(R.id.btn_dialog_choose_supplier_add);

        supplierList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        supplierList.setLayoutManager(linearLayoutManager);

        supplierList.addOnScrollListener(new Utils.EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                int lastFirstVisiblePosition = ((LinearLayoutManager) supplierList.getLayoutManager()).findFirstVisibleItemPosition();
                supplierList.getLayoutManager().scrollToPosition(lastFirstVisiblePosition);
            }
        });


        adapterFirebaseSupplier = new FirebaseRecyclerAdapter<Supplier, SupplierViewHolder>(
                Supplier.class,
                R.id.item_client,
                SupplierViewHolder.class,
                Constants.refDatabase.child(emailLogin+"/Supplier")
        ) {
            @Override
            public SupplierViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);
                return new SupplierViewHolder(v);
            }


            @Override
            protected void populateViewHolder(SupplierViewHolder viewHolder, Supplier model, int position) {
                viewHolder.supplier.setText(model.getSupplierName());
            }
        };

        supplierList.setAdapter(adapterFirebaseSupplier);
        adapterFirebaseSupplier.notifyDataSetChanged();

        builder.setPositiveButton("Thêm NCC", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Intent it = new Intent(getApplicationContext(), AddSupplierActivity.class);
                it.putExtra("EmailLogin",emailLogin);
                startActivity(it);
            }
        });
        builder.show();
    }

    private void wareHouseInDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_warehouse_in, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(dialogView.getContext());
        builder.setView(dialogView);

        tvChoosenProduct = (TextView) dialogView.findViewById(R.id.tv_warehouse_in_chooseProduct);
        tvChoosenProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                productListDialog();
            }
        });

        final EditText edtQuantity = (EditText) dialogView.findViewById(R.id.edt_warehouse_in_quantity);
        final Button btnOk = (Button) dialogView.findViewById(R.id.btn_dialog_warehouse_in_ok);
        tvProductInStorage = (TextView) dialogView.findViewById(R.id.tv_warehouse_in_storage);

        builder.setTitle("Nhập kho");

        final Dialog dialog = builder.create();
        dialog.show();

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                v.startAnimation(Constants.buttonClick);
                productQuantity = edtQuantity.getText().toString();

                String timeStamp = Calendar.getInstance().getTime().getTime()+"";

                if (TextUtils.isEmpty(productQuantity)) {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập số lượng hàng nhập kho", Toast.LENGTH_SHORT).show();

                } else {
                    btnOk.setVisibility(View.INVISIBLE);

                    final String endStorage = (Float.parseFloat(productStorage)+Float.parseFloat(productQuantity))+"";

                    WarehouseIn warehouseIn = new WarehouseIn(supplierName, choosenProduct, productQuantity, timeStamp);
                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("In").child(timeStamp).setValue(warehouseIn);

                    Constants.refDatabase.child(emailLogin+"/WarehouseMan").child("ProductIn").child(choosenProduct).child(timeStamp).setValue(warehouseIn).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            dialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(WarehouseManActivity.this);
                            builder.setTitle("Xác nhận nhập kho:");
                            builder.setMessage("Tên sản phẩm: "+ choosenProduct+"\n"+
                                                "Tồn đầu: "+ Utils.convertNumber(productStorage)+"\n"+
                                                "Nhập thêm: "+ Utils.convertNumber(productQuantity)+"\n"+
                                                "Tồn cuối: "+ Utils.convertNumber(endStorage));
                            builder.setPositiveButton("Xác nhận", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    refDatabase.child(emailLogin).child("WarehouseMan/StorageMan").child(choosenProduct)
                                            .child("unitQuantity").setValue(endStorage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(getApplicationContext(),"Nhập kho thành công!",Toast.LENGTH_LONG).show();
                                            hideProgressDialog();
                                        }
                                    });
                                }
                            }).show();
                        }
                    });



                }
            }
        });
    }
    private void productListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_product_list,null);
        builder.setView(dialogView);
        builder.setMessage("Chọn sản phẩm (trượt dọc để xem tiếp)");

        dialogProductList = builder.create();

        Button btnNew = dialogView.findViewById(R.id.btn_product_list_new);

        btnNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.startAnimation(buttonClick);
            }
        });

        final RecyclerView productList = (RecyclerView)dialogView.findViewById(R.id.recycler_dialog_product_list);
        productList.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        productList.setLayoutManager(linearLayoutManager);

        adapterFirebaseProduct = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product_pos,
                ProductViewHolder.class,
                refDatabase.child(emailLogin+"/Product")
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_pos,parent,false);
                return new ProductViewHolder(v);
            }


            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());

            }
        };

        productList.setAdapter(adapterFirebaseProduct);
        adapterFirebaseProduct.notifyDataSetChanged();

        dialogProductList.show();
    }

    //Utils
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

    //Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.warehouse, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout_warehouse) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

        if (id == R.id.action_warehouse_in) {
            wareHouseInDialog();
        }

        if (id == R.id.action_warehouse_view_storage) {
            Intent it = new Intent(getApplicationContext(), ViewStorageActivity.class);
            it.putExtra("EmailLogin",emailLogin);
            startActivity(it);
        }

        if (id == R.id.action_warehouse_view_in_history) {
            Intent it = new Intent(getApplicationContext(), ViewInOutHistory.class);
            it.putExtra("EmailLogin",emailLogin);
            startActivity(it);
        }

        if (id == R.id.action_warehouse_view_out_history) {
            Intent it = new Intent(getApplicationContext(), ViewInOutHistory.class);
            it.putExtra("Out",true);
            it.putExtra("EmailLogin",emailLogin);
            startActivity(it);
        }

        /*
        if (id == R.id.action_warehouse_generate_QRCode) {
            final int writeExternalStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (writeExternalStorage != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            generateQRCodeDialog();
        }
         */


        return super.onOptionsItemSelected(item);
    }

    private void generateQRCodeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_generate_qr, null);
        builder.setView(dialogView);
        builder.setTitle("Tạo QR Code");
        final Dialog dialog = builder.create();
        dialog.show();

        final EditText edtNumber = (EditText) dialogView.findViewById(R.id.edt_dialog_qr_number);
        final Button btnOk = (Button) dialogView.findViewById(R.id.btn_dialog_qr_ok);


        File pdfDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "NestArt");

        if (!pdfDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            pdfDir.mkdir();
        }
        // fix
        //noinspection ResultOfMethodCallIgnored
        pdfDir.setExecutable(true);
        //noinspection ResultOfMethodCallIgnored
        pdfDir.setReadable(true);
        //noinspection ResultOfMethodCallIgnored
        pdfDir.setWritable(true);

        ActivityCompat.requestPermissions(WarehouseManActivity.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
        //Now create the name of your PDF file that you will generate
        final File pdfFile = new File(pdfDir, "qrNestArt.pdf");

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                btnOk.setEnabled(false);
                v.startAnimation(Constants.buttonClick);
                String number = edtNumber.getText().toString();
                if (TextUtils.isEmpty(number)) {
                    Toast.makeText(getApplicationContext(), "Vui lòng nhập số lượng code cần tạo", Toast.LENGTH_LONG).show();
                } else {
                    //showProgressDialog();

                    final int pageNumber = Integer.parseInt(number);
                    final int tableNumber = pageNumber * 14;

                    try {
                        final Document document = new Document();
                        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                        document.open();


                        float[] columnWidths = {1, 1, 1, 1, 1, 1, 1, 1};
                        final PdfPTable[] tables = new PdfPTable[tableNumber];

                        for (int i = 0; i < tableNumber; i++) {

                            PdfPCell[] cells = new PdfPCell[8];
                            tables[i] = new PdfPTable(columnWidths);

                            for (int y = 0; y < 4; y++) {
                                DatabaseReference qrCodePush = Constants.refDatabase.child("QrCode").push();
                                String qrCodePushKey = qrCodePush.getKey();

                                final String randomNumberStringKey = getRandomNumber(1000, 9999) + "";
                                final String randomNumberStringValue = getRandomNumber(1000, 9999) + "";
                                qrCodePush.child("Key").setValue(randomNumberStringKey);
                                qrCodePush.child("Value").setValue(randomNumberStringValue);

                                for (int z=0; z<2;z++){
                                    if (z % 2 == 0) {
                                        Bitmap qrBitmap = QRCode.from(qrCodePushKey+"\nVui long nhap chuoi so "+randomNumberStringKey+" tai websites nestart.vn de xac thuc san pham.").bitmap();
                                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                        qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);

                                        byte[] byteArray = stream.toByteArray();
                                        Image img1 = Image.getInstance(byteArray);
                                        //img1.scaleAbsolute(120f, 80f);

                                        cells[y] = new PdfPCell(img1, true);
                                        cells[y].setHorizontalAlignment(Element.ALIGN_CENTER);
                                        cells[y].setVerticalAlignment(Element.ALIGN_CENTER);

                                        cells[y].setBorderWidthLeft(0f);
                                        cells[y].setBorderWidthBottom(0f);
                                        cells[y].setBorderWidthTop(4f);
                                        cells[y].setBorderWidthRight(0f);
                                        tables[i].addCell(cells[y]);

                                    }else{
                                        cells[y] = new PdfPCell(new Phrase(randomNumberStringValue));
                                        cells[y].setHorizontalAlignment(Element.ALIGN_CENTER);
                                        cells[y].setVerticalAlignment(Element.ALIGN_MIDDLE);

                                        cells[y].setBorderWidthLeft(0f);
                                        cells[y].setBorderWidthBottom(0f);
                                        cells[y].setBorderWidthTop(4f);
                                        cells[y].setBorderWidthRight(4f);
                                        tables[i].addCell(cells[y]);
                                    }

                                }


                            }


                            document.add(tables[i]);

                        }

                        document.close();


                    } catch (Exception e) {
                        e.printStackTrace();

                    }

                    dialog.dismiss();
                    //hideProgressDialog();

                }
            }
        });


    }

    private class SupplierViewHolder extends RecyclerView.ViewHolder {
        TextView supplier;

        public SupplierViewHolder(View itemView) {
            super(itemView);
            supplier = (TextView) itemView.findViewById(R.id.tv_item_client_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    int position = getLayoutPosition();
                    DatabaseReference itemRef = adapterFirebaseSupplier.getRef(position);
                    supplierName = itemRef.getKey();

                    wareHouseInDialog();
                }
            });
        }
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName;

        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView) itemView.findViewById(R.id.tv_item_product_name);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.startAnimation(Constants.buttonClick);
                    if(dialogProductList!=null) dialogProductList.dismiss();
                    int position = getLayoutPosition();
                    DatabaseReference keyRef = adapterFirebaseProduct.getRef(position);

                    final Product p = adapterFirebaseProduct.getItem(position);
                    choosenProduct = p.getProductName();
                    tvChoosenProduct.setText(choosenProduct);

                    refDatabase.child(emailLogin).child("WarehouseMan/StorageMan").child(p.getProductName())
                            .child("unitQuantity").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            productStorage = dataSnapshot.getValue().toString();
                            tvProductInStorage.setText(Utils.convertNumber(productStorage));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });

        }
    }

    @Override
    public void onBackPressed() {
        b.clear();
        startActivity(new Intent(this, MainActivity.class));

        super.onBackPressed();
    }

    private int getRandomNumber(int min, int max) {
        return (new Random()).nextInt((max - min) + 1) + min;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    generateQRCodeDialog();
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Không thể tạo tập tin!", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}

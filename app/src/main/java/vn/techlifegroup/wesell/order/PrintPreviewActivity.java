package vn.techlifegroup.wesell.order;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Locale;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.model.Client;
import vn.techlifegroup.wesell.model.Company;
import vn.techlifegroup.wesell.model.OrderDetail;
import vn.techlifegroup.wesell.model.Product;
import vn.techlifegroup.wesell.model.VatModel;
import vn.techlifegroup.wesell.utils.Constants;
import vn.techlifegroup.wesell.utils.Utils;

import static vn.techlifegroup.wesell.utils.Constants.refDatabase;
import static vn.techlifegroup.wesell.utils.Constants.refOrderList;

public class PrintPreviewActivity extends AppCompatActivity {
    private String orderName;
    private RecyclerView recyclerViewProduct, recyclerViewPromotion;
    private LinearLayoutManager linearLayoutManager;
    private FirebaseRecyclerAdapter<Product,ProductViewHolder> adapterFirebaseProduct,adapterFirebasePromotion;
    private DatabaseReference refProduct, refPromotion;
    private TextView tvClientName, tvClientType, tvPayment, tvDeliveryDate, tvNotVAT, tvVAT,tvFinalPayment,tvClientDebt,tvNote,tvClientAddress,tvDeliveryName,tvCompanyName,tvCompanyInfo;
    private String orderPushKey,userRole,emailLogin,companyName,companyInfo;
    private ImageView ibPrint;
    private Bundle b = new Bundle();
    private PrintManager mgr=null;
    private int pageCount;
    private ImageView ivLogo;
    public static int MY_REQUEST_CODE = 1;
    private Bitmap logo;
    private String VAT,notVAT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print_preview);

        Intent intent = this.getIntent();
        orderPushKey = intent.getStringExtra("OrderPushKey");
        orderName = intent.getStringExtra("OrderName");
        emailLogin = intent.getStringExtra("EmailLogin");

        initializeScreen();
        viewVAT();
        viewOtherInformation();
        getProductInfo();
        getCompanyInfo();
    }

    private void getCompanyInfo() {
        refDatabase.child(emailLogin).child("Company").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Company company = dataSnapshot.getValue(Company.class);
                assert company != null;
                String url = company.getCompanyLogo();

                Glide.with(getApplicationContext()).load(url).into(ivLogo);

                companyName = company.getCompanyName();
                tvCompanyName.setText(companyName);
                tvCompanyInfo.setText(company.getCompanyInfo());

                ivLogo.setDrawingCacheEnabled(true);
                ivLogo.buildDrawingCache();
                logo = Bitmap.createBitmap(ivLogo.getDrawingCache());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void getProductInfo() {

        refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                int i = 0;
                for(DataSnapshot it:iterable){
                    Product p = it.getValue(Product.class);
                    b.putString("ProductName"+i,p.getProductName());
                    b.putString("ProductQuantity"+i,p.getUnitQuantity());
                    b.putString("ProductPrice"+i,p.getUnitPrice());
                    i++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();
                int i = 0;
                for(DataSnapshot it:iterable){
                    Product p = it.getValue(Product.class);
                    b.putString("PromotionName"+i,p.getProductName());
                    b.putString("PromotionQuantity"+i,p.getUnitQuantity());
                    i++;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void initializeScreen() {
        recyclerViewProduct = (RecyclerView)findViewById(R.id.recycler_print_preview_product);
        recyclerViewPromotion = (RecyclerView)findViewById(R.id.recyclerview_print_preview_promotion);
        tvClientName = (TextView)findViewById(R.id.tv_print_preview_client_name);
        tvClientType = (TextView)findViewById(R.id.tv_print_preview_client_type);
        tvPayment = (TextView)findViewById(R.id.tv_print_preview_payment_type);
        tvDeliveryDate = (TextView)findViewById(R.id.tv_print_preview_delivery_date);
        tvNotVAT = (TextView)findViewById(R.id.tv_print_preview_notVAT);
        tvVAT = (TextView)findViewById(R.id.tv_print_preview_VAT);
        tvClientDebt = (TextView)findViewById(R.id.tv_print_preview_client_debt);
        tvFinalPayment = (TextView)findViewById(R.id.tv_print_preview_final_payment);
        tvNote = (TextView)findViewById(R.id.tv_print_preview_note);
        tvClientAddress =  (TextView)findViewById(R.id.tv_print_preview_client_name_address);
        tvDeliveryName =  (TextView)findViewById(R.id.tv_print_preview_delivery_name);
        ibPrint = (ImageView)findViewById(R.id.ib_print_preview_print);
        ivLogo = findViewById(R.id.iv_print_preview_logo);
        tvCompanyName = findViewById(R.id.tv_print_company_name);
        tvCompanyInfo = findViewById(R.id.tv_print_company_contact);

        ibPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.startAnimation(Constants.buttonClick);
                printOrderDialog();

            }
        });

        initializeRecyclerViewProduct();
        initializeRecyclerViewPromotion();

        getFontFile();
    }

    private void getFontFile() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_REQUEST_CODE);
            }

        }else{
            AssetManager am = getAssets();
            InputStream inputStream1 = null;
            InputStream inputStream2 = null;

            try {
                inputStream1 = am.open("vuArial.ttf");
                Utils.createFileFromInputStream(inputStream1,
                        Environment.getExternalStorageDirectory().getAbsolutePath()+"/vuArial.ttf");
                inputStream2 = am.open("vuArialBold.ttf");

                Utils.createFileFromInputStream(inputStream2,
                        Environment.getExternalStorageDirectory().getAbsolutePath()+"/vuArialBold.ttf");

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void printOrderDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(PrintPreviewActivity.this);
        builder.setTitle("In đơn hàng");
        builder.setMessage("Vui lòng chờ cho đến khi thông báo này ẩn đi.");

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_REQUEST_CODE);
                    }

                }else if(ivLogo==null){
                    Toast.makeText(getApplicationContext(),"Đang xử lý hình ảnh, vui lòng đợi!",Toast.LENGTH_LONG).show();
                }else{

                    File fontFile = new File(Environment.getExternalStorageDirectory(),"vuArialBold.ttf");
                    File fontFileBold = new File(Environment.getExternalStorageDirectory(),"vuArialBold.ttf");

                    File pdfDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),companyName);

                    if (!pdfDir.exists()){
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
                    MediaScannerConnection.scanFile(PrintPreviewActivity.this, new String[] {pdfDir.toString()}, null, null);

                    //Now create the name of your PDF file that you will generate
                    final File pdfFile = new File(pdfDir, "DH"+companyName+".pdf");

                    try {
                        final Document document = new Document();
                        //ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
                        // PdfWriter.getInstance(document, baos);
                        document.open();

                        BaseFont bfBold = BaseFont.createFont(fontFileBold.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                        BaseFont bfNormal = BaseFont.createFont(fontFile.getPath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

                        Font fontTitle = new Font(bfBold,25);
                        Font fontSubTitle = new Font(bfBold,15);
                        Font fontSmall = new Font(bfNormal,10);
                        final Font fontNormal = new Font(bfNormal,12);

                        float[] columnWidths = {2,10};
                        final PdfPTable table = new PdfPTable(columnWidths);

                        for (int i = 0; i < 1; i++) {
                            PdfPCell cell;
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            //Bitmap bmp = BitmapFactory.decodeFile());
                            //Bitmap logo = BitmapFactory.decodeResource(getResources(),R.drawable.nestart_logo);

                            logo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            byte[] byteArray = stream.toByteArray();
                            Image img1 = Image.getInstance(byteArray);
                            img1.scaleAbsolute(120f, 80f);
                            cell = new PdfPCell(img1,true);
                            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                            cell.setBorderWidthLeft(0);
                            cell.setBorderWidthBottom(0);
                            cell.setBorderWidthTop(0);
                            cell.setBorderWidthRight(0);
                            table.addCell(cell);

                            cell = new PdfPCell(new Phrase(companyName+"\n" +
                                    companyInfo+"\n",fontSmall));
                            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            cell.setBorderWidthLeft(0);
                            cell.setBorderWidthBottom(0);
                            cell.setBorderWidthTop(0);
                            cell.setBorderWidthRight(0);
                            table.addCell(cell);
                        }
                        document.add(table);

                        Paragraph pTitle = new Paragraph("Đơn hàng", fontTitle);
                        pTitle.setAlignment(Element.ALIGN_CENTER);
                        document.add(pTitle);
                        Paragraph pTTC = new Paragraph("Thông tin chung", fontSubTitle);
                        pTitle.setAlignment(Element.ALIGN_LEFT);
                        document.add(pTTC);

                        final PdfPTable table2 = new PdfPTable(columnWidths);

                        for (int i = 0; i < 1; i++) {
                            final String clientName = b.getString("OrderName");
                            if(clientName==null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Tên khách hàng",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table2.addCell(cell);

                                cell = new PdfPCell(new Phrase(clientName,fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table2.addCell(cell);
                            }
                        }
                        document.add(table2);

                        final PdfPTable table3 = new PdfPTable(columnWidths);

                        for (int i = 0; i < 1; i++) {
                            String clientType = b.getString("ClientType");
                            if(clientType==null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Loại khách hàng",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table3.addCell(cell);

                                cell = new PdfPCell(new Phrase(clientType,fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table3.addCell(cell);
                            }
                        }
                        document.add(table3);

                        final PdfPTable table4 = new PdfPTable(columnWidths);

                        for (int i = 0; i < 1; i++) {
                            String payment = b.getString("Payment");

                            if(payment==null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Hình thức thanh toán",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table4.addCell(cell);

                                cell = new PdfPCell(new Phrase(payment,fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table4.addCell(cell);
                            }
                        }
                        document.add(table4);

                        final PdfPTable table5 = new PdfPTable(columnWidths);

                        for (int i = 0; i < 1; i++) {
                            String deliveryDate = b.getString("DeliveryDate");
                            if(deliveryDate==null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Ngày giao hàng",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table5.addCell(cell);

                                cell = new PdfPCell(new Phrase(deliveryDate,fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table5.addCell(cell);
                            }
                        }
                        document.add(table5);

                        final PdfPTable table6 = new PdfPTable(columnWidths);

                        for (int i = 0; i < 1; i++) {
                            String clientAddress = b.getString("ClientAddress");
                            String clientDistrict = b.getString("ClientDistrict");
                            String clientCity = b.getString("ClientCity");
                            if(clientAddress==null ||clientDistrict==null ||clientCity==null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Địa chỉ",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table6.addCell(cell);

                                cell = new PdfPCell(new Phrase(clientAddress+", "+clientDistrict+ ", "+clientCity,fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table6.addCell(cell);
                            }
                        }
                        document.add(table6);

                        final PdfPTable table7 = new PdfPTable(columnWidths);

                        for (int i = 0; i < 1; i++) {
                            String deliveryName = b.getString("DeliveryName");
                            if(deliveryName==null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Chành xe",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table7.addCell(cell);

                                cell = new PdfPCell(new Phrase(deliveryName,fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table7.addCell(cell);
                            }
                        }
                        document.add(table7);

                        final PdfPTable table8 = new PdfPTable(columnWidths);

                        for (int i = 0; i < 1; i++) {
                            String note = b.getString("Note");
                            if(note==null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Ghi chú",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table8.addCell(cell);

                                cell = new PdfPCell(new Phrase(note,fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table8.addCell(cell);
                            }
                        }
                        document.add(table8);

                        Paragraph pTTSP = new Paragraph("Thông tin sản phẩm", fontSubTitle);
                        pTitle.setAlignment(Element.ALIGN_LEFT);
                        document.add(pTTSP);

                        int productNumber = Integer.parseInt(b.getString("ProductNumber"));
                        int promotionNumber = Integer.parseInt(b.getString("PromotionNumber"));

                        final float[] columnWidthsProduct = {9,2,1};
                        final PdfPTable table9 = new  PdfPTable(columnWidthsProduct);

                        for (int x = 0; x < 1; x++) {
                            PdfPCell cell;
                            cell = new PdfPCell(new Phrase("Tên",fontNormal));
                            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cell.setBorderWidthLeft(0);
                            cell.setBorderWidthBottom(0);
                            cell.setBorderWidthTop(0);
                            cell.setBorderWidthRight(0);
                            table9.addCell(cell);

                            cell = new PdfPCell(new Phrase("Đơn giá",fontNormal));
                            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            cell.setBorderWidthLeft(0);
                            cell.setBorderWidthBottom(0);
                            cell.setBorderWidthTop(0);
                            cell.setBorderWidthRight(0);
                            table9.addCell(cell);

                            cell = new PdfPCell(new Phrase("SL",fontNormal));
                            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            cell.setBorderWidthLeft(0);
                            cell.setBorderWidthBottom(0);
                            cell.setBorderWidthTop(0);
                            cell.setBorderWidthRight(0);
                            table9.addCell(cell);
                        }
                        document.add(table9);

                        final PdfPTable[] tables = new  PdfPTable[productNumber];

                        for(int i =0; i<productNumber;i++){
                            String productName = b.getString("ProductName"+i);
                            String productQuantity = b.getString("ProductQuantity"+i);
                            String productPrice = Utils.convertNumber(b.getString("ProductPrice"+i));

                            if(productName == null ||productQuantity == null ||productPrice == null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                tables[i] = new PdfPTable(columnWidthsProduct);

                                for (int x = 0; x < 1; x++) {
                                    PdfPCell cell;
                                    cell = new PdfPCell(new Phrase(productName,fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthBottom(0);
                                    cell.setBorderWidthTop(0);
                                    cell.setBorderWidthRight(0);
                                    tables[i].addCell(cell);

                                    cell = new PdfPCell(new Phrase(productPrice,fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthBottom(0);
                                    cell.setBorderWidthTop(0);
                                    cell.setBorderWidthRight(0);
                                    tables[i].addCell(cell);

                                    cell = new PdfPCell(new Phrase(productQuantity,fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthBottom(0);
                                    cell.setBorderWidthTop(0);
                                    cell.setBorderWidthRight(0);
                                    tables[i].addCell(cell);
                                }
                                document.add(tables[i]);

                            }

                        }

                        Paragraph pTTKM = new Paragraph("Thông tin khuyến mãi", fontSubTitle);
                        pTitle.setAlignment(Element.ALIGN_LEFT);
                        document.add(pTTKM);

                        final PdfPTable table10 = new  PdfPTable(columnWidthsProduct);

                        for (int x = 0; x < 1; x++) {
                            PdfPCell cell;
                            cell = new PdfPCell(new Phrase("Tên",fontNormal));
                            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                            cell.setBorderWidthLeft(0);
                            cell.setBorderWidthBottom(0);
                            cell.setBorderWidthTop(0);
                            cell.setBorderWidthRight(0);
                            table10.addCell(cell);

                            cell = new PdfPCell(new Phrase("Đơn giá",fontNormal));
                            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            cell.setBorderWidthLeft(0);
                            cell.setBorderWidthBottom(0);
                            cell.setBorderWidthTop(0);
                            cell.setBorderWidthRight(0);
                            table10.addCell(cell);

                            cell = new PdfPCell(new Phrase("SL",fontNormal));
                            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                            cell.setBorderWidthLeft(0);
                            cell.setBorderWidthBottom(0);
                            cell.setBorderWidthTop(0);
                            cell.setBorderWidthRight(0);
                            table10.addCell(cell);
                        }
                        document.add(table10);

                        final PdfPTable[] tablesPromotion = new  PdfPTable[promotionNumber];

                        for(int i =0; i<promotionNumber;i++){
                            String promtionName = b.getString("PromotionName"+i);
                            String promtionQuantity = b.getString("PromotionQuantity"+i);

                            if(promtionName == null ||promtionQuantity == null ){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                tablesPromotion[i] = new PdfPTable(columnWidthsProduct);

                                for (int x = 0; x < 1; x++) {
                                    PdfPCell cell;
                                    cell = new PdfPCell(new Phrase(promtionName,fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthBottom(0);
                                    cell.setBorderWidthTop(0);
                                    cell.setBorderWidthRight(0);
                                    tablesPromotion[i].addCell(cell);

                                    cell = new PdfPCell(new Phrase("",fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthBottom(0);
                                    cell.setBorderWidthTop(0);
                                    cell.setBorderWidthRight(0);
                                    tablesPromotion[i].addCell(cell);

                                    cell = new PdfPCell(new Phrase(promtionQuantity,fontNormal));
                                    cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                    cell.setBorderWidthLeft(0);
                                    cell.setBorderWidthBottom(0);
                                    cell.setBorderWidthTop(0);
                                    cell.setBorderWidthRight(0);
                                    tablesPromotion[i].addCell(cell);
                                }
                                document.add(tablesPromotion[i]);

                            }

                        }


                        Paragraph pTT = new Paragraph("Thanh toán", fontSubTitle);
                        pTitle.setAlignment(Element.ALIGN_LEFT);
                        document.add(pTT);

                        final PdfPTable table11 = new PdfPTable(columnWidths);

                        for (int i = 0; i < 1; i++) {
                            String notVat = b.getString("NotVat");
                            if(notVat == null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Chưa VAT",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table11.addCell(cell);

                                cell = new PdfPCell(new Phrase(Utils.convertNumber(notVat),fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table11.addCell(cell);
                            }
                        }
                        document.add(table11);

                        final PdfPTable table12 = new PdfPTable(columnWidths);

                        for (int i = 0; i < 1; i++) {
                            String vat = b.getString("Vat");
                            if(vat == null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Gồm VAT",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table12.addCell(cell);

                                cell = new PdfPCell(new Phrase(Utils.convertNumber(vat),fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table12.addCell(cell);
                            }
                        }
                        document.add(table12);

                        final PdfPTable table14 = new PdfPTable(columnWidths);

                        for (int i = 0; i < 1; i++) {
                            String finalPayment = b.getString("FinalPayment");
                            if(finalPayment == null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Giá trị cuối cùng",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table14.addCell(cell);

                                cell = new PdfPCell(new Phrase(Utils.convertNumber(finalPayment),fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table14.addCell(cell);
                            }
                        }
                        document.add(table14);

                        final PdfPTable table16 = new PdfPTable(columnWidths);

                        table16.setSpacingAfter(20f);

                        for (int i = 0; i < 1; i++) {
                            String clientDebt = b.getString("ClientDebt");

                            if(clientDebt==null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Công nợ khách hàng",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table16.addCell(cell);

                                cell = new PdfPCell(new Phrase(Utils.convertNumber(clientDebt),fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table16.addCell(cell);
                            }
                        }
                        document.add(table16);



                        float [] column = {5,5};

                        final PdfPTable table15 = new PdfPTable(column);


                        for (int i = 0; i < 1; i++) {
                            String finalPayment = b.getString("FinalPayment");
                            if(finalPayment == null){
                                Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                            }else{
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase("Người lập",fontSmall));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table15.addCell(cell);

                                cell = new PdfPCell(new Phrase("Khách hàng",fontNormal));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthBottom(0);
                                cell.setBorderWidthTop(0);
                                cell.setBorderWidthRight(0);
                                table15.addCell(cell);
                            }
                        }
                        document.add(table15);

                        document.close();

                    /*
                     Paragraph pAddress = new Paragraph(shopAddress, fontSmall);
                    pAddress.setAlignment(Element.ALIGN_CENTER);
                    document.add(pAddress);

                    Paragraph pPaymentBill = new Paragraph("PHIẾU THANH TOÁN", fontSubTitle);
                    pPaymentBill.setAlignment(Element.ALIGN_CENTER);
                    document.add(pPaymentBill);

                    Paragraph pBillInfo = new Paragraph("Số HĐ: " + billCode+ "    "+ Utils.getDateCurrentTimeZone(timeStamp), fontNormal);
                    pBillInfo.setAlignment(Element.ALIGN_CENTER);
                    document.add(pBillInfo);

                    Paragraph pEmployee = new Paragraph("Tên NV: " + employeeName, fontNormal);
                    pEmployee.setAlignment(Element.ALIGN_CENTER);
                    pEmployee.setSpacingAfter(20f);
                    document.add(pEmployee);

                    float[] columnWidths = {1,8,2,3,3};
                    final PdfPTable table = new PdfPTable(columnWidths);

                    for (int i = 0; i < 1; i++) {
                        PdfPCell cell;
                        cell = new PdfPCell(new Phrase("TT",fontNormal));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setBorderWidthLeft(0);
                        cell.setBorderWidthRight(0);
                        table.addCell(cell);

                        cell = new PdfPCell(new Phrase("Sản phẩm",fontNormal));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setBorderWidthLeft(0);
                        cell.setBorderWidthRight(0);
                        table.addCell(cell);

                        cell = new PdfPCell(new Phrase("SL",fontNormal));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setBorderWidthLeft(0);
                        cell.setBorderWidthRight(0);
                        table.addCell(cell);

                        cell = new PdfPCell(new Phrase("Đ.Giá",fontNormal));
                        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                        cell.setBorderWidthRight(0);
                        cell.setBorderWidthLeft(0);
                        table.addCell(cell);

                        cell = new PdfPCell(new Phrase("T.Tiền",fontNormal));
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell.setBorderWidthLeft(0);
                        cell.setBorderWidthRight(0);
                        table.addCell(cell);
                    }
                    document.add(table);

                    PdfPTable [] tables = new  PdfPTable[forIntValue];

                    for(int i =0; i<forIntValue;i++){
                        String productName = b.getString("ProductName"+i);
                        String productOrder = b.getString("ProductOrder"+i);
                        String productQuantity = b.getString("ProductQuantity"+i);
                        String productPrice = b.getString("ProductPrice"+i);
                        String productTotal = b.getString("ProductTotal"+i);

                        if(productName == null ||productOrder == null ||productQuantity == null ||productPrice == null ||productTotal == null){
                            Toast.makeText(getApplicationContext(),"Đang xử lý...",Toast.LENGTH_LONG).show();
                        }else{

                            tables[i] = new PdfPTable(columnWidths);

                            for (int x = 0; x < 1; x++) {
                                PdfPCell cell;
                                cell = new PdfPCell(new Phrase(productOrder,fontNormal));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthRight(0);
                                tables[i].addCell(cell);

                                cell = new PdfPCell(new Phrase(productName,fontNormal));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthRight(0);
                                tables[i].addCell(cell);

                                cell = new PdfPCell(new Phrase(productQuantity,fontNormal));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthRight(0);
                                tables[i].addCell(cell);

                                cell = new PdfPCell(new Phrase(Utils.convertNumber(productPrice),fontNormal));
                                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                                cell.setBorderWidthRight(0);
                                cell.setBorderWidthLeft(0);
                                tables[i].addCell(cell);

                                cell = new PdfPCell(new Phrase(Utils.convertNumber(productTotal),fontNormal));
                                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                                cell.setBorderWidthLeft(0);
                                cell.setBorderWidthRight(0);
                                tables[i].addCell(cell);
                            }
                            document.add(tables[i]);

                        }

                    }

                    float[] columnWidth2s = {5,5};
                    final PdfPTable table2 = new PdfPTable(columnWidth2s);

                    for (int i = 0; i < 1; i++) {
                        PdfPCell cell;
                        cell = new PdfPCell(new Phrase("Thành tiền", fontNormal));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidthLeft(0);
                        cell.setBorderWidthRight(0);
                        table2.addCell(cell);

                        cell = new PdfPCell(new Phrase(Utils.convertNumber(billPayment+""), fontNormal));
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell.setBorderWidthLeft(0);
                        cell.setBorderWidthRight(0);
                        table2.addCell(cell);
                    }
                    table2.setSpacingAfter(20f);
                    document.add(table2);

                    final PdfPTable table3 = new PdfPTable(columnWidth2s);

                    for (int i = 0; i < 1; i++) {
                        PdfPCell cell;
                        cell = new PdfPCell(new Phrase("Tiền khách đưa", fontNormal));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidthLeft(0);
                        cell.setBorderWidthBottom(0);
                        cell.setBorderWidthTop(0);
                        cell.setBorderWidthRight(0);
                        table3.addCell(cell);

                        cell = new PdfPCell(new Phrase(Utils.convertNumber(customerCash), fontNormal));
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell.setBorderWidthLeft(0);
                        cell.setBorderWidthBottom(0);
                        cell.setBorderWidthTop(0);
                        cell.setBorderWidthRight(0);
                        table3.addCell(cell);
                    }

                    document.add(table3);

                    final PdfPTable table4 = new PdfPTable(columnWidth2s);

                    for (int i = 0; i < 1; i++) {
                        PdfPCell cell;
                        cell = new PdfPCell(new Phrase("Tiền thừa", fontNormal));
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setBorderWidthLeft(0);
                        cell.setBorderWidthBottom(0);
                        cell.setBorderWidthTop(0);
                        cell.setBorderWidthRight(0);
                        table4.addCell(cell);

                        cell = new PdfPCell(new Phrase(Utils.convertNumber(cashBack), fontNormal));
                        cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                        cell.setBorderWidthLeft(0);
                        cell.setBorderWidthBottom(0);
                        cell.setBorderWidthTop(0);
                        cell.setBorderWidthRight(0);
                        table4.addCell(cell);
                    }

                    document.add(table4);

                    document.close();
                     */





                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    PrintManager printManager = (PrintManager) PrintPreviewActivity.this.getSystemService(Context.PRINT_SERVICE);
                    String jobName = PrintPreviewActivity.this.getString(R.string.app_name) + " Document";

                    PrintDocumentAdapter pda = new PrintDocumentAdapter() {
                        private int pageCount;
                        @Override
                        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                            // Prepare the layout.
                            int newPageCount;
                            // Mils is 1/1000th of an inch. Obviously.
                            if(newAttributes.getMediaSize().getHeightMils() < 1000) {
                                newPageCount = 2;
                            } else {
                                newPageCount = 1;
                            }
                            // Has the layout actually changed?
                            boolean layoutChanged = newPageCount != pageCount;
                            pageCount = newPageCount;
                            PrintDocumentInfo info = new PrintDocumentInfo
                                    .Builder("print_output.pdf")
                                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                    .setPageCount(pageCount)
                                    .build();

                            // Not actually going to do anything for now
                            callback.onLayoutFinished(info, layoutChanged);
                        }

                        @Override
                        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
                            InputStream input = null;
                            OutputStream output = null;
                            try {
                                input =  new FileInputStream(pdfFile);
                                //input =  new ByteArrayInputStream(baos.toByteArray());
                                output = new FileOutputStream(destination.getFileDescriptor());
                                byte[] buf = new byte[1024];
                                int bytesRead;

                                while ((bytesRead = input.read(buf)) > 0) {
                                    output.write(buf, 0, bytesRead);
                                }

                                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});



                            } catch (Exception e) {
                                //Catch exception
                            } finally {
                                try {
                                    if (input != null) {
                                        input.close();
                                    }
                                    if (output != null) {
                                        output.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                        }
                    };

                    PrintAttributes printAttributes = new PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_C8).build();

                    printManager.print(jobName, pda, printAttributes);

                    dialog.dismiss();

                }


            }

        });
        builder.show();

    }

    private void viewOtherInformation() {
        refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("OtherInformation").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                OrderDetail orderDetail = dataSnapshot.getValue(OrderDetail.class);
                if(orderDetail!=null){
                    String clientName = orderDetail.getOrderName();
                    String clientType = orderDetail.getClientType();
                    String payment = orderDetail.getPaymentType();
                    String deliveryDate = orderDetail.getDateDelivery();
                    final String clientCode = orderDetail.getClientCode();
                    String orderNote = orderDetail.getOrderNote();

                    b.putString("OrderName",clientName);
                    b.putString("ClientType",clientType);
                    b.putString("Payment",payment);
                    b.putString("DeliveryDate",deliveryDate);
                    b.putString("Note",orderNote);

                    tvClientName.setText(clientName);
                    tvClientType.setText(clientType);
                    tvPayment.setText(payment);
                    tvDeliveryDate.setText(deliveryDate);
                    tvNote.setText(orderNote);

                    refDatabase.child(emailLogin+"/Client").child(clientCode).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Client client = dataSnapshot.getValue(Client.class);
                            String clientAddress = client.getClientStreet();
                            String clientDistrict = client.getClientDistrict();
                            String clientCity = client.getClientCity();
                            String clientDeliveryName = client.getClientDeliveryName();
                            tvClientAddress.setText(clientAddress+", "+clientDistrict+", "+clientCity);
                            b.putString("ClientAddress",clientAddress);
                            b.putString("ClientDistrict",clientDistrict);
                            b.putString("ClientCity",clientCity);
                            b.putString("DeliveryName",clientDeliveryName);

                            tvDeliveryName.setText(clientDeliveryName);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //Client debt

                    refDatabase.child(emailLogin+"/Client").child(clientCode).child("clientDebt").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String clientDebt = dataSnapshot.getValue().toString();
                            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
                            float clientDebtFloat = Float.parseFloat(clientDebt);
                            String clientDebtFloatConverted = numberFormat.format(clientDebtFloat);
                            tvClientDebt.setText(clientDebtFloatConverted);
                            b.putString("ClientDebt",clientDebt);
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
    private void viewVAT() {
        refOrderList.child(orderPushKey).child("VAT").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                VatModel currentVat = dataSnapshot.getValue(VatModel.class);
                if(currentVat!=null){

                    VAT = currentVat.getIncludedVat();

                    tvVAT.setText(Utils.convertNumber(VAT+""));

                    String finalPayment = currentVat.getFinalPayment();
                    tvFinalPayment.setText(Utils.convertNumber(finalPayment+""));

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void initializeRecyclerViewPromotion() {
        recyclerViewPromotion.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewPromotion.setLayoutManager(linearLayoutManager);

        refPromotion = refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("Promotion");

        refPromotion.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                b.putString("PromotionNumber",count+"");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapterFirebasePromotion = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product_print2,
                ProductViewHolder.class,
                refPromotion
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_print2,parent,false);
                return new ProductViewHolder(v);
            }

            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productQuantity.setText(model.getUnitQuantity());
            }
        };

        recyclerViewPromotion.setAdapter(adapterFirebasePromotion);
        adapterFirebasePromotion.notifyDataSetChanged();
    }
    private void initializeRecyclerViewProduct() {
        recyclerViewProduct.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerViewProduct.setLayoutManager(linearLayoutManager);

        refProduct = refDatabase.child(emailLogin+"/OrderList").child(orderPushKey).child("ProductList");

        refProduct.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long count = dataSnapshot.getChildrenCount();
                b.putString("ProductNumber",count+"");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapterFirebaseProduct = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(
                Product.class,
                R.id.item_product_print2,
                ProductViewHolder.class,
                refProduct
        ) {
            @Override
            public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_print2,parent,false);
                return new ProductViewHolder(v);
            }

            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, Product model, int position) {
                viewHolder.productName.setText(model.getProductName());
                viewHolder.productPrice.setText(Utils.convertNumber(model.getUnitPrice()));
                viewHolder.productQuantity.setText(model.getUnitQuantity());
            }
        };

        recyclerViewProduct.setAdapter(adapterFirebaseProduct);
        adapterFirebaseProduct.notifyDataSetChanged();
    }
    public class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView productName, productPrice, productQuantity;


        public ProductViewHolder(View itemView) {
            super(itemView);
            productName = (TextView)itemView.findViewById(R.id.tv_item_product_name);
            productPrice = (TextView)itemView.findViewById(R.id.tv_item_product_price);
            productQuantity = (TextView) itemView.findViewById(R.id.tv_item_product_quantity);

        }
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                printOrderDialog();

            }

        }
    }
/*
    public void main(String[] args) {

        Runnable r = new Runnable() {
            public void run() {
                refDatabase.child(emailLogin).child("Company").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Company company = dataSnapshot.getValue(Company.class);
                        assert company != null;

                        try {
                            logo = Glide.with(getApplicationContext()).load(company.getCompanyLogo()).asBitmap().into(-1,-1).get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        new Thread(r).start();
        //this line will execute immediately, not waiting for your task to complete
    }
*/
}






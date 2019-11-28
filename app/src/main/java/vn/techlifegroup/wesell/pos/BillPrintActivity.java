package vn.techlifegroup.wesell.pos;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import vn.techlifegroup.wesell.R;
import vn.techlifegroup.wesell.utils.Utils;

public class BillPrintActivity extends AppCompatActivity {
    String shopName,shopAddress,billCode,employeeName,customerCash,cashBack;
    private Bundle b = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_print);

        Intent intent = this.getIntent();
        shopName = intent.getStringExtra("ShopName");
        shopAddress = intent.getStringExtra("ShopAddress");
        billCode = intent.getStringExtra("BillCode");
        employeeName = intent.getStringExtra("EmployeeName");
        customerCash = intent.getStringExtra("CustomerCash");
        cashBack = intent.getStringExtra("CashBack");


        int forIntValue = intent.getIntExtra("ForIntValue",0);
        float billPayment = intent.getFloatExtra("BillPayment",0);

        long timeStamp = Calendar.getInstance().getTime().getTime();


        //Print to File and printer
        File fontFile = new File(Environment.getExternalStorageDirectory(),"resources/fonts/vuArial.ttf");
        File fontFileBold = new File(Environment.getExternalStorageDirectory(),"resources/fonts/vuArialBold.ttf");

        File pdfDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "FreshLife");
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

        // MediaScannerConnection.scanFile(context, new String[] {pdfDir.toString()}, null, null);
        String timeStampString = (Calendar.getInstance().getTime().getTime())+"";

        //Now create the name of your PDF file that you will generate
        final File pdfFile = new File(pdfDir, timeStampString+"TextPDF.pdf");

        try {
            final Document document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            BaseFont bfBold = BaseFont.createFont(fontFileBold.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            BaseFont bfNormal = BaseFont.createFont(fontFile.getAbsolutePath(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            Font fontTitle = new Font(bfBold,25);
            Font fontSubTitle = new Font(bfBold,15);
            Font fontSmall = new Font(bfNormal,10);
            final Font fontNormal = new Font(bfNormal,12);

            Paragraph pTitle = new Paragraph(shopName, fontTitle);
            pTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(pTitle);

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

            float[] columnWidths = {1,5,2,3,3};
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

        }
        catch (Exception e){
            e.printStackTrace();
        }

        PrintManager printManager = (PrintManager) BillPrintActivity.this.getSystemService(Context.PRINT_SERVICE);
        String jobName = BillPrintActivity.this.getString(R.string.app_name) + " Document";

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

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this,PosActivity.class));
        super.onBackPressed();
    }
}

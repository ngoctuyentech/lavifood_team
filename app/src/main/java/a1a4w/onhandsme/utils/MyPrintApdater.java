package a1a4w.onhandsme.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.support.annotation.RequiresApi;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by toila on 23/04/2017.
 */

@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class MyPrintApdater  extends PrintDocumentAdapter {

    private int pageCount;
    private Context context;
    private static final int SCALE_MODE_FILL = 2;
    private static final int SCALE_MODE_FIT = 1;
    private PrintedPdfDocument pdfDocument;
    private PrintAttributes mAttributes;
    final int fittingMode = SCALE_MODE_FIT;
    private Bitmap bmp;

    public MyPrintApdater(Context ctx, Bitmap bitmap ) {
        bmp = bitmap;
        context = ctx;
    }


    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, final LayoutResultCallback callback, Bundle extras) {

        // Register a cancellation listener
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                callback.onLayoutCancelled();
            }
        });

        mAttributes = newAttributes;

        // Prepare the layout.
        int newPageCount;
        // Mils is 1/1000th of an inch. Obviously.
        if(newAttributes.getMediaSize().getHeightMils() < 10000) {
            newPageCount = 2;
        } else {
            newPageCount = 1;
        }

        // Create the PDF document we'll use later


        // Has the layout actually changed?
        boolean layoutChanged = newPageCount != pageCount;
        pageCount = newPageCount;

        // Create the doc info to return
        PrintDocumentInfo info = new PrintDocumentInfo
                .Builder("print_output.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(pageCount)
                .build();

        // Not actually going to do anything for now
        callback.onLayoutFinished(info, layoutChanged);
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, final WriteResultCallback callback) {

        // Register a cancellation listener
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                // If cancelled then ensure that the PDF doc gets thrown away
                pdfDocument.close();
                pdfDocument = null;
                // And callback
                callback.onWriteCancelled();
            }
        });



        // Iterate through the pages
        for (int currentPageNumber = 0; currentPageNumber < pageCount; currentPageNumber++) {
            // Has this page been requested?
            if(Utils.pageRangesContainPage(currentPageNumber, pages)) {
                pdfDocument = new PrintedPdfDocument(context, mAttributes);
                // Start the current page
                PdfDocument.Page page = pdfDocument.startPage(currentPageNumber);

                // Get the canvas for this page
                Canvas canvas = page.getCanvas();
                RectF content = new RectF(page.getInfo().getContentRect());
                Matrix matrix = getMatrix(bmp.getWidth(), bmp.getHeight(),content, fittingMode);
                canvas.drawBitmap(bmp, matrix,null);

                // Finish the page
                pdfDocument.finishPage(page);
            }



            /*
                //Paint paint = new Paint();
         //   paint.setAntiAlias(true);
          //  paint.setFilterBitmap(true);
         //   paint.setDither(true);
            if (pageCount == 1) {
                // We're putting everything on one page
                //Rect imageRect = new Rect(36, 36, canvas.getWidth() - 36, canvas.getHeight() - 36);
                canvas.drawBitmap(scaleScreen, 0, 0, paint);

            } else {
                // Same rect for image and text
                // Rect contentRect = new Rect(36, 36, canvas.getWidth() - 36, canvas.getHeight() - 36);
                // Image on page 0, text on page 1
                canvas.drawBitmap(scaleScreen, 0, 0, paint);

            }
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
             */

        }

        // Attempt to send the completed doc out
        try {
            pdfDocument.writeTo(new FileOutputStream(destination.getFileDescriptor()));
        } catch (IOException e) {
            callback.onWriteFailed(e.toString());
            return;
        } finally {
            pdfDocument.close();
            pdfDocument = null;
        }

        // The print is complete
        callback.onWriteFinished(pages);
    }


    private boolean pageRangesContainPage(int pageNumber, PageRange[] ranges)
    {
        for(PageRange range : ranges) {
            if(pageNumber >= range.getStart() && pageNumber <= range.getEnd()) {
                return true;
            }
        }
        return false;
    }

    private Matrix getMatrix(int imageWidth, int imageHeight, RectF content, int fittingMode) {
        Matrix matrix = new Matrix();

        // Compute and apply scale to fill the page.
        float scale = content.width() / imageWidth;
        if (fittingMode == SCALE_MODE_FILL) {
            scale = Math.max(scale, content.height() / imageHeight);
        } else {
            scale = Math.min(scale, content.height() / imageHeight);
        }
        matrix.postScale(scale, scale);

        // Center the content.
        final float translateX = (content.width()
                - imageWidth * scale) / 2;
        final float translateY = (content.height()
                - imageHeight * scale) / 2;
        matrix.postTranslate(translateX, translateY);
        return matrix;
    }





}
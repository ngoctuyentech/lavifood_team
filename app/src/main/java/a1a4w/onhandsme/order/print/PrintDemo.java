package a1a4w.onhandsme.order.print;


import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import a1a4w.onhandsme.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class PrintDemo extends Fragment implements ImageAndTextContainer {

    public PrintDemo() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_print, container, false);

        /*


        // Wire up some button handlers
        rootView.findViewById(R.id.print_image_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintHelper printHelper = new PrintHelper(getActivity());
                printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                // Get the image
                Bitmap image = getImage();
                if (image != null) {
                    // Send it to the print helper
                    printHelper.printBitmap("PrintShop", image);
                }

            }
        });
         */


        final ImageAndTextContainer imageAndTextContainer = this;
        /*
        rootView.findViewById(R.id.print_page_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a PrintDocumentAdapter
                PrintOrderAdapter adapter = new PrintOrderAdapter(imageAndTextContainer, getActivity());
                // Get the print manager from the context
                PrintManager printManager = (PrintManager)getActivity().getSystemService(Context.PRINT_SERVICE);
                // And print the document
                printManager.print("PrintShop", adapter, null);
            }
        });
         */

        return rootView;
    }

    @Override
    public String getText() {
        TextView textView = (TextView) getView().findViewById(R.id.textView);
        return textView.getText().toString();
    }

    @Override
    public Bitmap getImage() {
        ImageView imageView = (ImageView) getView().findViewById(R.id.iv_print_preview_logo);
        Bitmap image = null;
        // Get the image
        if ((imageView.getDrawable()) != null) {
            // Send it to the print helper
            image = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        }
        return image;
    }
}
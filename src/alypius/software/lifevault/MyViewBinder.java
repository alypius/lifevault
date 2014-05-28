package alypius.software.lifevault;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter.ViewBinder;

/**
 * ViewBinder to set Bitmaps in each
 * list element when using SimpleAdapter
 */
public class MyViewBinder implements ViewBinder {


    public boolean setViewValue(View view, Object data, String textRepresentation) {

        // Check if trying to put Bitmap on ImageView
        if ((view instanceof ImageView) & (data instanceof Bitmap)) {
            // Set bitmap on imageview
            ImageView iv = (ImageView) view;
            Bitmap bm = (Bitmap) data;
            iv.setImageBitmap(bm);
            return true;
        }
        // failed
        return false;
    }
}

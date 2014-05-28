package alypius.software.lifevault;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Helper methods
 */
public class Helper {

    //Get a thumbnail from a a filepath
    public static Bitmap getImageFromFileName(String fileName) {

        try {
            // Decode image size
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(fileName, options);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 50;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = options.outWidth, height_tmp =
                    options.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 <
                        REQUIRED_SIZE)
                    break;

                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = scale;
            return BitmapFactory.decodeFile(fileName, options2);
        } catch (Exception e) {
            return null;
        }
    }

    //Get a readable Date as String from miliseconds
    public static String msToDate(String ms) {
        long miliseconds = Long.parseLong(ms);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy hh:mm aaa");

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(miliseconds);
        return sdf.format(c.getTime());
    }
}

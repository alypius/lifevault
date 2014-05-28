package alypius.software.lifevault.models;

import alypius.software.lifevault.interfaces.*;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.Activity;

public class ViewRow implements IViewRow {
    private IViewRowElement[] viewRowElements;
    private int displayIndex;
    private ISetRowCell setRowCell;

    public ViewRow(int[] imageViews, int[] textViews, ISetRowCell setRowCell) {
        if (imageViews.length != textViews.length)
            throw new IllegalArgumentException("Image and text views must have same length");
        viewRowElements = new IViewRowElement[imageViews.length];
        for (int i = 0; i < imageViews.length; i++) {
            viewRowElements[i] = new ViewRowElement(imageViews[i], textViews[i]);
        }
        displayIndex = 0;
        this.setRowCell = setRowCell;
    }

    public IViewRowElement[] getViewRowElements() {
        return viewRowElements;
    }

    protected void clearRowCell(ImageView imageView, TextView textView) {
        textView.setText("");
        imageView.setTag("");
        imageView.setImageBitmap(null);
    }

    public void fillViewRow(Activity activity, String[] urls) {
        for (int i = displayIndex; i < displayIndex + viewRowElements.length; i++) {
            IViewRowElement viewRowElement = viewRowElements[i - displayIndex];
            ImageView imageView = ((ImageView) activity.findViewById(viewRowElement.getImageViewId()));
            TextView textView = ((TextView) activity.findViewById(viewRowElement.getTextViewId()));

            if (i < urls.length && !urls[i].equals("")) {
                setRowCell.call(imageView, textView, urls[i]);
            } else {
                clearRowCell(imageView, textView);
            }
        }
    }

    public void previousCell(Activity activity, String[] urls) {
        if (displayIndex > 0) {
            displayIndex--;
            fillViewRow(activity, urls);
        }
    }

    public void nextCell(Activity activity, String[] urls) {
        if (displayIndex < urls.length - viewRowElements.length) {
            displayIndex++;
            fillViewRow(activity, urls);
        }
    }

    public void setCalculatedDisplayIndex(String[] urls) {
        displayIndex = urls.length > viewRowElements.length ? urls.length - viewRowElements.length : 0;
    }
}

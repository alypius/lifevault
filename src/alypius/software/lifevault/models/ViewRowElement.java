package alypius.software.lifevault.models;

import alypius.software.lifevault.interfaces.IViewRowElement;

public class ViewRowElement implements IViewRowElement {

    private int imageViewId;
    private int textViewId;

    public ViewRowElement(int imageViewId, int textViewId) {
        this.imageViewId = imageViewId;
        this.textViewId = textViewId;
    }

    public int getImageViewId() {
        return imageViewId;
    }

    public int getTextViewId() {
        return textViewId;
    }
}

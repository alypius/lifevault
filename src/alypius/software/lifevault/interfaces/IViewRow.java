package alypius.software.lifevault.interfaces;

import android.app.Activity;

public interface IViewRow {
    IViewRowElement[] getViewRowElements();
    void fillViewRow(Activity activity, String[] urls);
    void previousCell(Activity activity, String[] urls);
    void nextCell(Activity activity, String[] urls);
    void setCalculatedDisplayIndex(String[] urls);
}

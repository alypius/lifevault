package alypius.software.lifevault.controllers;

import android.app.Dialog;

public class DialogManager {
    private Dialog imageDialog;
    private Dialog audioDialog;
    private Dialog videoDialog;
    private Dialog titleDialog;
    private Dialog descriptionDialog;

    public Dialog getImageDialog() {
        return imageDialog;
    }

    public Dialog getAudioDialog() {
        return audioDialog;
    }

    public Dialog getVideoDialog() {
        return videoDialog;
    }

    public Dialog getTitleDialog() {
        return titleDialog;
    }

    public Dialog getDescriptionDialog() {
        return descriptionDialog;
    }

    public void setImageDialog(Dialog dialog) {
        imageDialog = dialog;
    }

    public void setAudioDialog(Dialog dialog) {
        audioDialog = dialog;
    }

    public void setVideoDialog(Dialog dialog) {
        videoDialog = dialog;
    }

    public void setTitleDialog(Dialog dialog) {
        titleDialog = dialog;
    }

    public void setDescriptionDialog(Dialog dialog) {
        descriptionDialog= dialog;
    }
}

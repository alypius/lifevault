package alypius.software.lifevault.controllers;

import android.view.View;

public class DialogViewManager {
    private View imageDialogView;
    private View audioDialogView;
    private View videoDialogView;
    private View titleDialogView;
    private View descriptionDialogView;

    public View getImageDialogView() {
        return imageDialogView;
    }

    public View getAudioDialogView() {
        return audioDialogView;
    }

    public View getVideoDialogView() {
        return videoDialogView;
    }

    public View getTitleDialogView() {
        return titleDialogView;
    }

    public View getDescriptionDialogView() {
        return descriptionDialogView;
    }

    public void setImageDialogView(View dialogView) {
        imageDialogView = dialogView;
    }

    public void setAudioDialogView(View dialogView) {
        audioDialogView = dialogView;
    }

    public void setVideoDialogView(View dialogView) {
        videoDialogView = dialogView;
    }

    public void setTitleDialogView(View dialogView) {
        titleDialogView = dialogView;
    }

    public void setDescriptionDialogView(View dialogView) {
        descriptionDialogView = dialogView;
    }
}

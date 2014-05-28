package alypius.software.lifevault.controllers;

import alypius.software.lifevault.interfaces.*;
import alypius.software.lifevault.models.ViewRow;

public class ViewRowManager {
    private IViewRow imageViewRow;
    private IViewRow audioViewRow;
    private IViewRow videoViewRow;
    private IViewRow contactViewRow;

    public ViewRowManager(Builder builder) {
        this.imageViewRow = builder.imageViewRow;
        this.audioViewRow = builder.audioViewRow;
        this.videoViewRow = builder.videoViewRow;
        this.contactViewRow = builder.contactViewRow;
    }

    public IViewRow getImageViewRow() {
        return imageViewRow;
    }

    public IViewRow getAudioViewRow() {
        return audioViewRow;
    }

    public IViewRow getVideoViewRow() {
        return videoViewRow;
    }

    public IViewRow getContactViewRow() {
        return contactViewRow;
    }

    public static class Builder {
        public ViewRowManager build() {
            return new ViewRowManager(this);
        }

        public Builder setImageViewRow(int[] imageImageViews, int[] imageTextViews, ISetRowCell setRowCell) {
            this.imageViewRow = new ViewRow(imageImageViews, imageTextViews, setRowCell);
            return this;
        }

        public Builder setAudioViewRow(int[] audioImageViews, int[] audioTextViews, ISetRowCell setRowCell) {
            this.audioViewRow = new ViewRow(audioImageViews, audioTextViews, setRowCell);
            return this;
        }

        public Builder setVideoViewRow(int[] videoImageViews, int[] videoTextViews, ISetRowCell setRowCell) {
            this.videoViewRow = new ViewRow(videoImageViews, videoTextViews, setRowCell);
            return this;
        }

        public Builder setContactViewRow(int[] contactImageViews, int[] contactTextViews, ISetRowCell setRowCell) {
            this.contactViewRow = new ViewRow(contactImageViews, contactTextViews, setRowCell);
            return this;
        }

        private IViewRow imageViewRow;
        private IViewRow audioViewRow;
        private IViewRow videoViewRow;
        private IViewRow contactViewRow;
    }
}

package com.bysj.imageutil.bean;

import android.graphics.Bitmap;

public class SpliceBean {

    private Bitmap image;
    private boolean hasImage;
    private String name;

    public SpliceBean() {

        this(null, null, false);
    }

    public SpliceBean(Bitmap image, String name) {

        this(image, name, true);
    }

    public SpliceBean(Bitmap image, String name, boolean hasImage) {

        this.image = image;
        this.name = name;
        this.hasImage = hasImage;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getName() {

        return this.name;
    }

    public void setImage(Bitmap image) {

        this.image = image;
        if ( image != null ) {

            this.setHasImage(true);
        }
    }

    public void setHasImage(boolean hasImage) {

        this.hasImage = hasImage;
    }

    public Bitmap getImage() {

        return this.image;
    }

    public boolean getHasImage() {

        return this.hasImage;
    }
}

package com.bysj.imageutil.bean;

import android.graphics.Bitmap;
import android.widget.BaseAdapter;

public class ICropResBean {

    private Bitmap bitmap;

    public ICropResBean(Bitmap bitmap) {

        this.bitmap = bitmap;
    }

    public void setImage(Bitmap bitmap) {

        this.bitmap = bitmap;
    }

    public Bitmap getImage() {

        return this.bitmap;
    }

    public int getWidth() {

        return this.bitmap.getWidth();
    }

    public int getHeight() {

        return this.bitmap.getHeight();
    }
}

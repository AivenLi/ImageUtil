package com.bysj.imgevaluation.bean;

import android.graphics.Bitmap;

/**
 * 用于存放图片处理结果
 *
 * Create on 2021-2-27
 */

public class EvaluatBean {

    private Bitmap oldBitmap;
    private Bitmap newBitmap;
    private String dimension;
    private float  value;

    public EvaluatBean(Bitmap oldBitmap) {

        this.oldBitmap = oldBitmap;
        this.newBitmap = null;
        this.dimension = null;
        this.value     = -1.0f;
    }

    public EvaluatBean(Bitmap oldBitmap, String dimension) {

        this.oldBitmap = oldBitmap;
        this.dimension = dimension;
        this.newBitmap = null;
        this.value     = -1.0f;
    }

    public EvaluatBean(Bitmap oldBitmap, Bitmap newBitmap, String dimension, float value) {

        this.oldBitmap = oldBitmap;
        this.newBitmap = newBitmap;
        this.dimension = dimension;
        this.value     = value;
    }

    public void setNewBitmap(Bitmap bitmap) {

        newBitmap = bitmap;
    }

    public void setDimension(String dimension) {

        this.dimension = dimension;
    }

    public void setValue(float v) {

        this.value = v;
    }

    public Bitmap getOldBitmap() {

        return oldBitmap;
    }

    public Bitmap getNewBitmap() {

        return newBitmap;
    }

    public String getDimension() {

        return dimension;
    }

    public float getValue() {

        return value;
    }
}

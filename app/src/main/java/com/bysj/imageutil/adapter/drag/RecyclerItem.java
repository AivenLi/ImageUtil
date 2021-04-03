package com.bysj.imageutil.adapter.drag;

import android.graphics.Bitmap;



public class RecyclerItem {
    private Bitmap icon;
    private String text;

    public RecyclerItem() {
    }

    public RecyclerItem(Bitmap icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    public Bitmap getIcon() {
        return icon;
    }

    public void setIcon(Bitmap icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "RecyclerItem{" +
                "icon=" + icon +
                ", text='" + text + '\'' +
                '}';
    }
}

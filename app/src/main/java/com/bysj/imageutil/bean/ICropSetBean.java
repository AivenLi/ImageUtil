package com.bysj.imageutil.bean;

public class ICropSetBean {

    private String title;
    private int num;

    public ICropSetBean(String title, int num) {

        this.title = title;
        this.num = num;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public String getTitle() {

        return this.title;
    }

    public void setNum(int num) {

        this.num = num;
    }

    public int getNum() {

        return this.num;
    }
}

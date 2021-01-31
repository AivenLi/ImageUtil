package com.bysj.imageutil;

import android.app.Application;

import com.bysj.imageutil.util.LogCat;

public class MyApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
        /**
         * 全局运行打印运行日志，发布时关闭
         */
        LogCat.setEnableLogCat(true);
    }
}

package com.bysj.imageutil.util;

import android.util.Log;

public class LogCat {

    public static boolean enable = false;

    public static void setEnableLogCat(boolean enable1) {

        enable = enable1;
    }

    public static void d(String tag, String value) {

        if ( enable ) {

            Log.d(tag, value);
        }
    }

    public static void d(String tag, int value) {

        if ( enable ) {

            Log.d(tag, value + "");
        }
    }

    public static void e(String tag, String value) {

        if ( enable ) {

            Log.e(tag, value);
        }
    }

    public static void i(String tag, String value) {

        if ( enable ) {

            Log.i(tag, value);
        }
    }
}

package com.bysj.imageutil.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * 操作CacheImgDBHelper中的数据库帮助类
 *
 * Create on 2021-2-27
 */

public class ImgSqlHelper {

    private static final String TAG       = "imgSqlHelper";
    private static final String tableName = CacheImgDBHelper.TABLE_NAME;
    private static ImgSqlHelper mInstance = null;
    private CacheImgDBHelper    ciDBHelper;
    private SQLiteDatabase      db;

    public static ImgSqlHelper getInstance(Context context) {

        if ( mInstance == null ) {

            synchronized (ImgSqlHelper.class) {

                mInstance = new ImgSqlHelper(context);
            }
        }
        return mInstance;
    }

    private ImgSqlHelper(Context context) {

        ciDBHelper = new CacheImgDBHelper(context, null, 1);
    }
}

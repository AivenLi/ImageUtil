package com.bysj.imageutil.sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 主要用于保存当前选中图片的文件名
 *
 * Create on 2021-2-27
 */

public class CacheImgDBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cache_img.db";
    public  static final String TABLE_NAME = "cache_img_table";

    public CacheImgDBHelper(Context context, SQLiteDatabase.CursorFactory factory, int version) {

        super(context, DB_NAME, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "file_name TEXT" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {

        /**
         * 在数据库需要升级时被调用，一般用来删除旧的数据库表，并将数据转移到新版本的数据库表中
         */
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}

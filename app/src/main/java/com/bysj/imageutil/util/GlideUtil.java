package com.bysj.imageutil.util;

import android.content.Context;

/**
 * Glide工具类
 */

import com.bumptech.glide.Glide;

public class GlideUtil {

    /**
     * 清除Glide在磁盘中的缓存
     * 注：清除Glide磁盘上的缓存必须在子线程中完成。
     * @param context
     */
    public static void clearDiskCache(Context context) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                Glide.get(context).clearDiskCache();
            }
        }).start();
    }

    /**
     * 清除Glide在内存中的缓存
     * 注：只能在主线程中执行该操作
     * @param context
     */
    public static void clearMemoryCache(Context context) {

        Glide.get(context).clearMemory();
    }
}

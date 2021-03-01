package com.bysj.imageutil.util;

import android.graphics.Bitmap;

import com.bysj.imageutil.bean.ICropResBean;

import java.util.ArrayList;

/**
 * 图片裁剪类
 *
 * Create on 2021-3-2
 */

public class ImageCropUtil {

    public static ArrayList<ICropResBean> crop(Bitmap bitmap, int sheets) {

        if ( bitmap == null ) {

            return null;
        }
        ArrayList<ICropResBean> data = new ArrayList<>();

        return data;
    }
}

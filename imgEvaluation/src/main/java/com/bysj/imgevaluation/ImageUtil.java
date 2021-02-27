package com.bysj.imgevaluation;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * 图片工具类
 *
 * Create on 2021-2-27
 */

public class ImageUtil {

    /**
     * Bitmap转Base64
     * @param bitmap Bitmap图片
     * @return Base64图片
     */
    public static String BitmapToBase64(Bitmap bitmap) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bytes = bos.toByteArray();
        return android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
    }

    /**
     * Base64转Bitmap
     * @param base64Img Base64图片
     * @return Bitmap图片
     */
    public static Bitmap Base64ToBitmap(String base64Img) {

        byte[] bytes = Base64.decode(base64Img, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}

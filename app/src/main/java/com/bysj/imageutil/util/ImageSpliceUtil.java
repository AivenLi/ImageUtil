package com.bysj.imageutil.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.bysj.imageutil.bean.SpliceBean;

import java.util.List;

/**
 * 图片拼接工具类，该工具类只能拼接3*3（行列）下任意组合的图片，
 * 且组合必须构成矩形，单行单列也算。
 *
 * Create on 2021-4-4
 */

public class ImageSpliceUtil {

    public static Bitmap spliceImage(List<SpliceBean> line1, List<SpliceBean> line2, List<SpliceBean> line3) {

        Bitmap bitmap = null;

        if ( line1 != null && line2 == null && line3 == null ) {

            int size = line1.size();
            if ( size == 2 ) {

                Bitmap bitmap1 = line1.get(0).getImage();
                Bitmap bitmap2 = line1.get(1).getImage();
                int width = bitmap1.getWidth() + bitmap2.getWidth();
                int height = Math.min(bitmap1.getHeight(), bitmap2.getHeight());
                int[] pixel = new int[width * height];

            } else if ( size == 3 ) {

            }
        }

        return bitmap;
    }

    public static Bitmap spliceImage(Bitmap bitmap1, Bitmap bitmap2, boolean line) {

        int w1 = bitmap1.getWidth();
        int h1 = bitmap1.getHeight();
        int w2 = bitmap2.getWidth();
        int h2 = bitmap2.getHeight();

        if ( line ) {

            int newWidth = w1+w2;
            int newHeight = Math.min(h1, h2);
            int overHeight = Math.abs(h1-h2);
            int[] pixels = new int[newWidth * newHeight];
            /**
             * 如果图片高度一样，不用裁剪直接拼接
             */
            if ( overHeight == 0 ) {

                bitmap1.getPixels(pixels, 0, newWidth, 0, 0, w1, h1);
                bitmap2.getPixels(pixels, w1, newWidth, 0, 0, w2, h2);
                return Bitmap.createBitmap(pixels, 0, newWidth, newWidth, newHeight, Bitmap.Config.ARGB_8888);
            } else if ( h1 > h2 ) {

                h1 = h1-overHeight;
                int[] pix = new int[w1*h1];
                bitmap1.getPixels(pix, 0, w1, 0, 0, w1, h1);
                bitmap1 = Bitmap.createBitmap(pix, 0, w1, w1, h1, Bitmap.Config.ARGB_8888);
                bitmap1.getPixels(pixels, 0, newWidth, 0, 0, w1, h1);
                bitmap2.getPixels(pixels, w1, newWidth, 0, 0, w2, h2);
                return Bitmap.createBitmap(pixels, 0, newWidth, newWidth, newHeight, Bitmap.Config.ARGB_8888);
            } else {

                h2 = h2-overHeight;
                int[] pix = new int[w2*h2];
                bitmap2.getPixels(pix, 0, w2, 0, 0, w2, h2);
                bitmap2 = Bitmap.createBitmap(pix, 0, w2, w2, h2, Bitmap.Config.ARGB_8888);
                bitmap1.getPixels(pixels, 0, newWidth, 0, 0, w1, h1);
                bitmap2.getPixels(pixels, w1, newWidth, 0, 0, w2, h2);
                return Bitmap.createBitmap(pixels, 0, newWidth, newWidth, newHeight, Bitmap.Config.ARGB_8888);
            }
        } else {

            int newWidth = Math.min(w1, w2);
            int overWidth = Math.abs(w1-w2);
            int newHeight = h1+h2;
            int[] pixels = new int[newWidth*newHeight];
            if ( overWidth == 0 ) {

                bitmap1.getPixels(pixels, 0, newWidth, 0, 0, newWidth, h1);
                bitmap2.getPixels(pixels, w1*h1, newWidth, 0, 0, newWidth, h2);
                return Bitmap.createBitmap(pixels, 0, newWidth, newWidth, newHeight, Bitmap.Config.ARGB_8888);
            } else if ( w1 > w2 ) {

                w1 = w1-overWidth;
                int[] pix = new int[w1*h1];
                bitmap1.getPixels(pix, 0, w1, 0, 0, w1, h1);
                bitmap1 = Bitmap.createBitmap(pix, 0, w1, w1, h1, Bitmap.Config.ARGB_8888);
                bitmap1.getPixels(pixels, 0, newWidth, 0, 0, w1, h1);
                bitmap2.getPixels(pixels, w1*h1, newWidth, 0, 0, w2, h2);
                return Bitmap.createBitmap(pixels, 0, newWidth, newWidth, newHeight, Bitmap.Config.ARGB_8888);
            } else {

                w2 = w2-overWidth;
                int[] pix = new int[w2*h2];
                bitmap2.getPixels(pix, 0, w2, 0, 0, w2, h2);
                bitmap2 = Bitmap.createBitmap(pix, 0, w2, w2, h2, Bitmap.Config.ARGB_8888);
                bitmap1.getPixels(pixels, 0, newWidth, 0, 0, newWidth, h1);
                bitmap2.getPixels(pixels, w1*h1, newWidth, 0, 0, newWidth, h2);
                return Bitmap.createBitmap(pixels, 0, newWidth, newWidth, newHeight, Bitmap.Config.ARGB_8888);
            }
        }
    }
}

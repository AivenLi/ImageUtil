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

    private static final String TAG = "imageCropUtil";

    private static int span;
    /**
     * 图片裁剪，注意，本方法主要是根据sheets参数将图片分割，例如sheets为4，则将图片
     * 分割成等份的四张图片。例如sheets为2，则将图片分割成等份的2张图片，根据图片的宽高来
     * 计算横向分割还是纵向分割，如果宽度大于高度，则纵向分割，反之；如果相等，默认纵向分割。
     *
     * 注意：如果参数sheets不在范围内，将返回原图
     *
     * @param bitmap 要裁剪的图片
     * @param sheets 份数
     * @return 分割之后的图片列表
     */
    public static ArrayList<ICropResBean> crop(Bitmap bitmap, int sheets) {

        ArrayList<ICropResBean> data = new ArrayList<>();
        if ( bitmap == null  ||
                sheets <= 0  ||
                sheets == 5  ||
                sheets == 7  ||
                sheets == 11 ||
                sheets == 13 ||
                sheets == 14 ||
                sheets > 16  ||
                bitmap.getHeight() < 100
                || bitmap.getHeight() < 100 ) {
            data.add(new ICropResBean(bitmap));
            return data;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if ( sheets == 1 ) {

            data.add(new ICropResBean(bitmap));
            return data;
        }


        int[] pixel = new int[width * height];

        bitmap.getPixels(pixel, 0, width, 0, 0, width, height);

        int rows;
        int cols;

        int blockWidth;
        int blockHeight;

        switch ( sheets ) {

            case 2:
                if ( width >= height ) {

                    rows = 2;
                    cols = 1;
                } else {

                    rows = 1;
                    cols = 2;
                }
                break;
            case 3:
                if ( width >= height ) {

                    rows = 3;
                    cols = 1;
                } else {

                    rows = 1;
                    cols = 3;
                }
                break;
            case 4:
                rows = 2;
                cols = 2;
                break;
            case 6:
                if ( width >= height ) {

                    rows = 3;
                    cols = 2;
                } else {

                    rows = 2;
                    cols = 3;
                }
                break;
            case 8:
                if ( width >= height ) {

                    rows = 4;
                    cols = 2;
                } else {

                    rows = 2;
                    cols = 4;
                }
                break;
            case 9:
                rows = 3;
                cols = 3;
                break;
            case 12:
                if ( width >= height ) {
                    
                    rows = 4;
                    cols = 3;
                } else {
                    
                    rows = 3;
                    cols = 4;
                }
                break;
            case 15:
                if ( width >= height ) {

                    rows = 5;
                    cols = 3;
                } else {

                    rows = 3;
                    cols = 5;
                }
                break;
            case 16:
                rows = cols = 4;
                break;
            default:

                return null;
        }
        span = rows;
        blockWidth = width / rows;
        blockHeight = height / cols;
       // LogCat.d(TAG, "Width: " + width + ", Height: " + height + ",blockW: " + blockWidth + ", blockH: " + blockHeight);
        return crop(pixel, rows, cols, width, blockWidth, blockHeight);
    }

    private static ArrayList<ICropResBean> crop(int[] pixel, int rows, int cols, int width, int cWidth, int cHeight) {

        ArrayList<ICropResBean> data = new ArrayList<>();

        for ( int i = 0; i < cols; ++i ) {

            for ( int j = 0; j < rows; ++j ) {

                Bitmap bitmap = Bitmap.createBitmap(pixel, j * cWidth + i * cHeight * width, width, cWidth, cHeight, Bitmap.Config.ARGB_8888);
                data.add(new ICropResBean(bitmap));
            }
        }

        return data;
    }

    public static int getSpan() {

        return span;
    }
}

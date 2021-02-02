package com.bysj.opencv450;

import android.graphics.Bitmap;

import java.io.File;

/**
 * 调用C++库，对外提供接口。
 * 单例模式
 * Create on 2021-2-1
 */

public class OpenCVUtil {

    private static OpenCVUtil mInstance = null;
    /**
     * 加载C++库
     */
    static {
        System.loadLibrary("native-lib");
    }

    public static OpenCVUtil getInstance() {

        if ( mInstance == null ) {

            synchronized (OpenCVUtil.class) {

                mInstance = new OpenCVUtil();
            }
        }
        return mInstance;
    }

    private OpenCVUtil() {

    }

    /**
     * 增强清晰度
     * @param bitmap 图片的Bitmap
     * @return 增强后的Bitmap
     */
    public Bitmap iClarytyEnhance(Bitmap bitmap) {

        return bitmap;
    }

    /**
     * 增强对比度
     * @param bitmap
     * @return
     */
    public Bitmap iContrastEnhance(Bitmap bitmap) {

        return bitmap;
    }

    /**
     * 增强饱和度
     * @param bitmap
     * @return
     */
    public Bitmap iSaturationEnhance(Bitmap bitmap) {

        return bitmap;
    }

    public Bitmap getBitmap(Bitmap bitmap){
        // 第一步：确定图片大小
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 第二步：将Bitmap->像素数组
        int[] pixArr = new int[width*height];
        bitmap.getPixels(pixArr,0,width,0,0,width,height);
        // 第三步：调用native方法
        cppImageProcess(width,height,pixArr,60);
        // 返回一张新的图片
        Bitmap newBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);
        // 给我们的图片填充数据
        newBitmap.setPixels(pixArr,0,width,0,0,width,height);
        return newBitmap;
    }

    public Bitmap lightPixels(Bitmap bitmap) {

        // 第一步：确定图片大小
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 第二步：将Bitmap->像素数组
        int[] pixArr = new int[width*height];
        bitmap.getPixels(pixArr,0,width,0,0,width,height);
        // 第三步：调用native方法
        pixArr = lightPixels(pixArr, width, height);
        // 返回一张新的图片
        Bitmap newBitmap = Bitmap.createBitmap(width,height, Bitmap.Config.RGB_565);
        // 给我们的图片填充数据
        newBitmap.setPixels(pixArr,0,width,0,0,width,height);
        return newBitmap;
    }

    private native void cppImageProcess(int w, int h, int[] arr, int ld);

    private native int[] lightPixels(int[] arr, int w, int j);
}

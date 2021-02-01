package com.bysj.opencv450;

import android.graphics.Bitmap;

public class OpenCV {

    static {
        System.loadLibrary("native-lib");
    }

    public OpenCV() {

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

    private native void cppImageProcess(int w, int h, int[] arr, int ld);
}

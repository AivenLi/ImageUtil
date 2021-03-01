package com.bysj.opencv450;

/**
 * 图片矩阵类
 */

import android.graphics.Bitmap;

public class PictureMatrix {

    private int width;
    private int height;
    private int[] matrix;

    public PictureMatrix(Bitmap bitmap) {

        width = bitmap.getWidth();
        height = bitmap.getHeight();
        matrix = new int[width * height];
        bitmap.getPixels(matrix, 0, width, 0, 0, width, height);
    }

    public PictureMatrix(Bitmap bitmap, int startX, int startY, int endX, int endY) {

        width = Math.abs(endX - startX);
        height = Math.abs(endY - startY);
        matrix = new int[width * height];
        //bitmap.getPixel(matrix, startX, )
    }

    public void setMatrix(int[] matrix) {

        this.matrix = matrix;
    }

    public int getWidth() {

        return width;
    }

    public int getHeight() {

        return height;
    }

    public int[] getMatrix() {

        return matrix;
    }

    public Bitmap toBitmap() {

        return toBitmap(Bitmap.Config.RGB_565);
    }

    public Bitmap toBitmap(Bitmap.Config config) {

        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        bitmap.setPixels(matrix, 0, width, 0, 0, width, height);
        return bitmap;
    }
}

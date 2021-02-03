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
     * 增强饱和度
     * @param bitmap
     * @return
     */
    public Bitmap iSaturationEnhance(Bitmap bitmap) {

        return bitmap;
    }

    /**
     * 自适应算法增强图像对比度
     * @param bitmap 原始图片
     * @return 增强对比度之后的图片
     */
    public Bitmap changeContrast(Bitmap bitmap) {

        /**
         * 创建图片矩阵对象
         */
        PictureMatrix pictureMatrix = new PictureMatrix(bitmap);
        /**
         * 调用C++库，自适应增强对比度算法
         */
        int[] resultMatrix = changeContrast(pictureMatrix.getMatrix(), pictureMatrix.getWidth(), pictureMatrix.getHeight(), 0.2f);
        /**
         * 保存结果（矩阵）
         */
        pictureMatrix.setMatrix(resultMatrix);
        /**
         * 返回bitmap结果
         */
        return pictureMatrix.toBitmap();
    }

    /**
     * 自适应算法增强图像对比度
     * @param bitmap 原始图片
     * @param coefficient 增强系数
     * @return 增强对比度之后的图片
     */
    public Bitmap changeContrast(Bitmap bitmap, float coefficient) {

        /**
         * 检查系数
         */
        if ( coefficient < 0.01f ) {

            coefficient = 0.01f;
        } else if ( coefficient > 1.0000f ) {

            coefficient = 0.9f;
        }
        /**
         * 创建图片矩阵对象
         */
        PictureMatrix pictureMatrix = new PictureMatrix(bitmap);
        /**
         * 调用C++库，自适应增强对比度算法
         */
        int[] resultMatrix = changeContrast(pictureMatrix.getMatrix(), pictureMatrix.getWidth(), pictureMatrix.getHeight(), coefficient);
        /**
         * 保存结果（矩阵）
         */
        pictureMatrix.setMatrix(resultMatrix);
        /**
         * 返回bitmap结果
         */
        return pictureMatrix.toBitmap();
    }

    /**
     * 自适应改变图片对比度异步
     * @param bitmap 原始图片
     * @param listener 完成后的回调
     */
    public void changeContrastSync(Bitmap bitmap, HandleImageListener listener) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                /**
                 * 创建图片矩阵对象
                 */
                PictureMatrix pictureMatrix = new PictureMatrix(bitmap);
                /**
                 * 调用C++库，自适应增强对比度算法
                 */
                int[] resultMatrix = changeContrast(pictureMatrix.getMatrix(), pictureMatrix.getWidth(), pictureMatrix.getHeight(), 0.2f);
                /**
                 * 保存结果
                 */
                pictureMatrix.setMatrix(resultMatrix);
                /**
                 * 回传结果给调用者
                 */
                if ( listener != null ) {

                    listener.done(pictureMatrix.toBitmap());
                }
            }
        }).start();
    }

    /**
     * 自适应改变图片对比度异步
     * @param bitmap 原始图片
     * @param coefficient 增强系数
     * @param listener 完成后的回调
     */
    public void changeContrastSync(Bitmap bitmap, float coefficient, HandleImageListener listener) {

        /**
         * 检查系数
         */
        if ( coefficient < 0.01f ) {

            coefficient = 0.01f;
        } else if ( coefficient > 1.0000f ) {

            coefficient = 0.9f;
        }
        final float cft = coefficient;
        new Thread(new Runnable() {
            @Override
            public void run() {
                /**
                 * 创建图片矩阵对象
                 */
                PictureMatrix pictureMatrix = new PictureMatrix(bitmap);
                /**
                 * 调用C++库，自适应增强对比度算法
                 */
                int[] resultMatrix = changeContrast(pictureMatrix.getMatrix(), pictureMatrix.getWidth(), pictureMatrix.getHeight(), cft);
                /**
                 * 保存结果
                 */
                pictureMatrix.setMatrix(resultMatrix);
                /**
                 * 回传结果给调用者
                 */
                if ( listener != null ) {

                    listener.done(pictureMatrix.toBitmap());
                }
            }
        }).start();
    }

    /**
     * 连接C++库方法
     * @param matrix 图片矩阵
     * @param width 图片宽度
     * @param height 图片高度
     * @param coefficient 增强系数
     * @return 修改之后的图片矩阵
     */
    private native int[] changeContrast(int[] matrix, int width, int height, float coefficient);
}

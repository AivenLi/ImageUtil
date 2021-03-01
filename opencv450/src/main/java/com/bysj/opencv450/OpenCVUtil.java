package com.bysj.opencv450;

import android.graphics.Bitmap;

import java.io.File;

/**
 * 调用C++库，对外提供接口。
 * Create on 2021-2-1
 */

public class OpenCVUtil {

    /**
     * 使用单例模式
     */
    private static OpenCVUtil mInstance = null;

    /**
     * 加载C++库
     */
    static {

        System.loadLibrary("ienhance");
    }

    /**
     * 获取实例
     * @return
     */
    public static OpenCVUtil getInstance() {

        if ( mInstance == null ) {

            synchronized (OpenCVUtil.class) {

                mInstance = new OpenCVUtil();
            }
        }
        return mInstance;
    }

    /**
     * 私有构造方法
     */
    private OpenCVUtil() {

    }

    /**
     * 自适应增强图片，异步模式
     * @param bitmap
     * @param listener
     */
    public void adaptiveEnhanceSync(Bitmap bitmap, HandleImageListener listener) {

        if ( bitmap == null ) {

            if ( listener != null ) {

                listener.done(null);
            }
        } else {

            changeContrastSync(bitmap, new HandleImageListener() {
                @Override
                public void done(Bitmap bitmap) {

                    changeSaturationSync(bitmap, new HandleImageListener() {
                        @Override
                        public void done(Bitmap bitmap) {

                            changeClaritySync(bitmap, listener);
                        }
                    });
                }
            });
        }
    }

    /**
     * 调整图像对比度，同步模式
     * @param bitmap 原始图片
     * @return 增强对比度之后的图片
     */
    public Bitmap changeContrast(Bitmap bitmap) {

        return changeContrast(bitmap, 0.2f);
    }

    /**
     * 调整图像对比度，同步模式
     * @param bitmap 原始图片
     * @param coefficient 增强系数
     * @return 增强对比度之后的图片
     */
    public Bitmap changeContrast(Bitmap bitmap, float coefficient) {

        /**
         * 检查系数
         */
        coefficient = checkContrastCoefficientBorder(coefficient);
        /**
         * 创建图片矩阵对象
         */
        PictureMatrix pictureMatrix = new PictureMatrix(bitmap);
        /**
         * 调用C++库，自适应增强对比度算法
         */
        int[] resultMatrix = changeContrast1(pictureMatrix.getMatrix(), pictureMatrix.getWidth(), pictureMatrix.getHeight(), coefficient);
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
     * 改变图片对比度，异步模式
     * @param bitmap 原始图片
     * @param listener 完成后的回调
     */
    public void changeContrastSync(Bitmap bitmap, HandleImageListener listener) {

        changeContrastSync(bitmap, 0.2f, listener);
    }

    /**
     * 改变图片对比度，异步模式
     * @param bitmap 原始图片
     * @param coefficient 增强系数
     * @param listener 完成后的回调
     */
    public void changeContrastSync(Bitmap bitmap, float coefficient, HandleImageListener listener) {

        final float cft = checkContrastCoefficientBorder(coefficient);
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
                int[] resultMatrix = changeContrast1(pictureMatrix.getMatrix(), pictureMatrix.getWidth(), pictureMatrix.getHeight(), cft);
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
     * 自适应图像对比度，同步模式
     * @param bitmap 原始图片
     * @param coefficient 增强系数
     * @return 增强对比度之后的图片
     */
    public Bitmap changeContrastAdaptive(Bitmap bitmap, float coefficient) {

        /**
         * 检查系数
         */
        coefficient = checkContrastCoefficientBorder(coefficient);
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
     * 自适应图片对比度，异步模式
     * @param bitmap 原始图片
     * @param listener 完成后的回调
     */
    public void changeContrastAdaptiveSync(Bitmap bitmap, HandleImageListener listener) {

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
     * 自适应增强图片饱和度，同步模式
     * @param bitmap 原图
     * @return 调整之后的图
     */
    public Bitmap changeSaturation(Bitmap bitmap) {

        return changeSaturation(bitmap, 80.0f);
    }

    /**
     * 调整图片饱和度，同步模式
     * @param bitmap 原图
     * @param coefficient 系数
     * @return 调整之后的图
     */
    public Bitmap changeSaturation(Bitmap bitmap, float coefficient) {

        /**
         * 创建图片矩阵对象
         */
        PictureMatrix picture = new PictureMatrix(bitmap);
        /**
         * 调用C++库，自适应调整饱和度算法
         */
        int[] matrix = changeSaturation(picture.getMatrix(), picture.getWidth(), picture.getHeight(), coefficient);
        /**
         * 保存结果（矩阵）
         */
        picture.setMatrix(matrix);
        /**
         * 将矩阵转换为Bitmap格式图片并返回。
         */
        return picture.toBitmap();
    }

    /**
     * 自适应增强图片饱和度，异步模式
     * @param bitmap 原图
     * @param listener 完成后的回调
     */
    public void changeSaturationSync(Bitmap bitmap, HandleImageListener listener) {

        changeSaturationSync(bitmap, 80.00f, listener);
    }

    /**
     * 调整图片饱和度，异步模式
     * @param bitmap 原图
     * @param coefficient 系数
     * @param listener 完成后的回调
     */
    public void changeSaturationSync(Bitmap bitmap, float coefficient, HandleImageListener listener) {

        final float cft = checkSaturationCoefficientBorder(coefficient);
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
                int[] resultMatrix = changeSaturation(pictureMatrix.getMatrix(), pictureMatrix.getWidth(), pictureMatrix.getHeight(), cft);
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
     * 自适应增强图片清晰度，同步模式
     * @param bitmap 原图
     * @return 增强之后的图
     */
    public Bitmap changeClarity(Bitmap bitmap) {

        return changeClarity(bitmap, 1.25f);
    }

    /**
     * 增强图片清晰度，同步。
     * @param bitmap 原图
     * @param coefficient 系数
     * @return 处理之后的图
     */
    public Bitmap changeClarity(Bitmap bitmap, float coefficient) {

        /**
         * 创建图片矩阵对象
         */
        PictureMatrix pictureMatrix = new PictureMatrix(bitmap);
        /**
         * 调用C++库，自适应增强对比度算法
         */
        int[] resultMatrix = changeClarity(pictureMatrix.getMatrix(), pictureMatrix.getWidth(), pictureMatrix.getHeight(), coefficient);
        /**
         * 保存结果
         */
        pictureMatrix.setMatrix(resultMatrix);
        /**
         * 返回结果
         */
        return pictureMatrix.toBitmap();
    }

    /**
     * 增强图片清晰度，异步。自适应
     * @param bitmap 原图
     * @param listener 回调
     */
    public void changeClaritySync(Bitmap bitmap, HandleImageListener listener) {

        changeClaritySync(bitmap, 1.25f, listener);
    }

    /**
     * 增强图片清晰度，异步。
     * @param bitmap 原图
     * @param coefficient 系数
     * @param listener 回调
     */
    public void changeClaritySync(Bitmap bitmap, float coefficient, HandleImageListener listener) {

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
                int[] resultMatrix = changeClarity(pictureMatrix.getMatrix(), pictureMatrix.getWidth(), pictureMatrix.getHeight(), coefficient);
                /**
                 * 保存结果
                 */
                pictureMatrix.setMatrix(resultMatrix);
                /**
                 * 回调
                 */
                if ( listener != null ) {

                    listener.done(pictureMatrix.toBitmap());
                }
            }
        }).start();
    }

    /**
     * 检查对比度系数，如果系数不在范围内，则调整系数至极限值：[0.01, 0.9]
     * @param coefficient 系数
     * @return
     */
    private float checkContrastCoefficientBorder(float coefficient) {

        /**
         * 检查系数
         */
        if ( coefficient < 0.01f ) {

            coefficient = 0.01f;
        } else if ( coefficient > 1.0000f ) {

            coefficient = 0.9f;
        }
        return coefficient;
    }

    /**
     * 检查饱和度系数，如果系数不在范围内，则调整系数至极限值：[30, 130]
     * 系数为30表示图片灰度化，如果系数值不在该范围内，则做截断处理
     * @param coefficient 系数
     * @return
     */
    private float checkSaturationCoefficientBorder(float coefficient) {

        if ( coefficient < 30.00f ) {

            coefficient = 30.00f;
        } else if ( coefficient > 130.00f ) {

            coefficient = 130.00f;
        }
        return coefficient;
    }

    /**
     * 连接C++库方法，调整对比度
     * @param matrix 图片矩阵
     * @param width 图片宽度
     * @param height 图片高度
     * @param coefficient 增强系数
     * @return 修改之后的图片矩阵
     */
    private native int[] changeContrast1(int[] matrix, int width, int height, float coefficient);

    /**
     * 连接C++库方法，调整对比度
     * @param matrix 图片矩阵
     * @param width 图片宽度
     * @param height 图片高度
     * @param coefficient 增强系数
     * @return 修改之后的图片矩阵
     */
    private native int[] changeContrast(int[] matrix, int width, int height, float coefficient);

    /**
     * 连接C++库方法，调整饱和度
     * @param matrix 图片矩阵
     * @param width 图片宽度
     * @param height 图片高度
     * @param coefficient 增强系数
     * @return 修改之后的图片矩阵
     */
    private native int[] changeSaturation(int[] matrix, int width, int height, float coefficient);

    /**
     * 连接C++库方法，调整清晰度
     * @param matrix 图片矩阵
     * @param width 图片宽度
     * @param height 图片高度
     * @param coefficient 增强系数
     * @return 修改之后的图片矩阵
     */
    private native int[] changeClarity(int[] matrix, int width, int height, float coefficient);
}

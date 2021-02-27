package com.bysj.imgevaluation;

import android.content.Context;
import android.graphics.Bitmap;

import com.bysj.imgevaluation.bean.EvaluatBean;
import com.bysj.imgevaluation.listener.EvaluatAllListener;
import com.bysj.imgevaluation.listener.EvaluatListener;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.util.ArrayList;


/**
 * 图片评价工具类
 *
 * Create on 2021-2-26
 */

public class EvaluatUtil {

    /**
     * 定义python文件名
     */
    private static final String DETECT_BLUR             = "detect_blur";
    private static final String DETECT_CONTRAST_RATIO   = "detect_contrast_ratio";
    private static final String DETECT_SATURATION       = "detect_saturation";
    /**
     * 定义python方法名
     */
    private static final String BLUR_FUNC_IMG           = "detectBlurImg";
    private static final String CONTRAST_RATIO_FUNC_IMG = "getContrastRatioImg";
    private static final String SATURATION_FUNC_IMG     = "getSaturationImg";
    private static final String BLUR_FUNC_VAL           = "detectBlurValue";
    private static final String CONTRAST_RATIO_FUNC_VAL = "getContrastRatioValue";
    private static final String SATURATION_FUNC_VAL     = "getSaturationValue";

    private static EvaluatUtil mInstance                = null;
    private Python python                               = null;
    private PyObject pyModuleBlur                       = null;
    private PyObject pyModuleContrast                   = null;
    private PyObject pyModuleSaturation                 = null;

    /**
     * 获取本类实例
     * @param context 上下文
     * @return 本类实例
     */
    public static EvaluatUtil getInstance(Context context) {

        if ( mInstance == null ) {

            synchronized (EvaluatUtil.class) {

                mInstance = new EvaluatUtil(context);
            }
        }
        return mInstance;
    }

    /**
     * 构造方法
     * @param context 上下文，用于启动python解释器
     */
    private EvaluatUtil(Context context) {

        /**
         * 启动Python
         */
        if ( !Python.isStarted() ) {

            Python.start(new AndroidPlatform(context));
        }
        python = Python.getInstance();
        pyModuleBlur       = python.getModule(DETECT_BLUR);
        pyModuleContrast   = python.getModule(DETECT_CONTRAST_RATIO);
        pyModuleSaturation = python.getModule(DETECT_SATURATION);
    }

    /**
     * 计算图片的清晰度，结果将以水印的形式嵌入原图
     * @param bitmap 要计算的图片
     * @return 处理后的图片
     */
    public Bitmap detectBlur(final Bitmap bitmap) {

        return detectImageQuality(bitmap, pyModuleBlur, BLUR_FUNC_IMG);
    }

    /**
     * 计算图片的对比度，结果将以水印的形式嵌入原图
     * @param bitmap 原图
     * @return 处理后的图片
     */
    public Bitmap detectContrastRatio(final Bitmap bitmap) {

        return detectImageQuality(bitmap, pyModuleContrast, CONTRAST_RATIO_FUNC_IMG);
    }

    /**
     * 计算图片的饱和度，结果将以水印的形式嵌入原图
     * @param bitmap 原图
     * @return 处理后的图片
     */
    public Bitmap detectSaturation(final Bitmap bitmap) {

        return detectImageQuality(bitmap, pyModuleSaturation, SATURATION_FUNC_IMG);
    }

    /**
     * 计算图片质量，结果将以水印的形式嵌入原图
     * @param bitmap 原图
     * @param pyModule 要调用的python模块
     * @param function 要调用的函数
     * @return 处理后的图片
     */
    private Bitmap detectImageQuality(final Bitmap bitmap, final PyObject pyModule, final String function) {

        if ( bitmap == null ) {

            return null;
        }
        String base64Img = ImageUtil.BitmapToBase64(bitmap);
        base64Img = pyModule.callAttr(function, base64Img).toString();
        return ImageUtil.Base64ToBitmap(base64Img);
    }

    /**
     * 计算图片清晰度值
     * @param bitmap 要计算的图片
     * @return 清晰度值
     */
    public float detectBlurValue(final Bitmap bitmap) {

        return detectDimensionValue(bitmap, pyModuleBlur, BLUR_FUNC_VAL);
    }

    /**
     * 计算图片对比度值
     * @param bitmap 要计算的图片
     * @return 对比度值
     */
    public float detectContrastValue(final Bitmap bitmap) {

        return detectDimensionValue(bitmap, pyModuleContrast, CONTRAST_RATIO_FUNC_VAL);
    }

    /**
     * 计算图片饱和度值
     * @param bitmap 要计算的图片
     * @return 饱和度值
     */
    public float detectSaturationValue(final Bitmap bitmap) {

        return detectDimensionValue(bitmap, pyModuleSaturation, SATURATION_FUNC_VAL);
    }


    /**
     * 计算图片某一维度的值（对比度、饱和度或清晰度）
     * @param bitmap 原图
     * @param pyModule python模块
     * @param function python方法名称
     * @return 该维度值
     */
    private float detectDimensionValue(final Bitmap bitmap, final PyObject pyModule, final String function) {

        if ( bitmap == null ) {

            return -1.0f;
        }
        String base64Img = ImageUtil.BitmapToBase64(bitmap);
        return pyModule.callAttr(function, base64Img).toFloat();
    }

    /**
     * 计算图片清晰度值，异步模式
     * @param bitmap 要计算的图片
     * @param listener 处理结果回调
     */
    public void detectBlurValueSync(final Bitmap bitmap, final EvaluatListener listener) {

        detectDimensionValueSync(bitmap, pyModuleBlur, BLUR_FUNC_VAL, "清晰度", listener);
    }

    /**
     * 计算图片对比度值，异步模式
     * @param bitmap 要计算的图片
     * @param listener 处理结果回调
     */
    public void detectContrastValueSync(final Bitmap bitmap, final EvaluatListener listener) {

        detectDimensionValueSync(bitmap, pyModuleContrast, CONTRAST_RATIO_FUNC_VAL, "对比度", listener);
    }

    /**
     * 计算图片饱和度值，异步模式
     * @param bitmap 要计算的图片
     * @param listener 处理结果回调
     */
    public void detectSaturationValueSync(final Bitmap bitmap, final EvaluatListener listener) {

        detectDimensionValueSync(bitmap, pyModuleSaturation, SATURATION_FUNC_VAL, "饱和度", listener);
    }

    /**
     * 计算图片某一维度值，异步模式
     * @param bitmap 要计算的图片
     * @param pyModule python模块
     * @param function python函数
     * @param dimension 维度
     * @param listener 结果回调
     */
    private void detectDimensionValueSync(final Bitmap bitmap, final PyObject pyModule, final String function, final String dimension, final EvaluatListener listener) {

        if ( bitmap == null ) {

            if ( listener != null ) {

                listener.failure("图片为空");
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

                float result = detectDimensionValue(bitmap, pyModule, function);
                if ( listener != null ) {

                    if ( result == -1.0f ) {

                        listener.failure("图片为空");
                    } else {

                        listener.success(new EvaluatBean(bitmap, null, dimension, result));
                    }
                }
            }
        }).start();
    }

    /**
     * 计算图片的清晰度，结果将以水印的形式嵌入原图
     * @param bitmap 要计算的图片
     * @param listener 处理结果回调
     * @return 处理后的图片
     */
    public void detectBlurSync(final Bitmap bitmap, final EvaluatListener listener) {

        detectImageQualitySync(bitmap, pyModuleBlur, BLUR_FUNC_IMG, listener);
    }

    /**
     * 计算图片的清晰度、对比度和饱和度值，异步模式。
     * 该方法回调参数是一个列表，元素值意义如下：
     *      0：清晰度
     *      1：对比度
     *      2：饱和度
     * @param bitmap 要计算的图片
     * @param listener 处理结果回调
     */
    public void detectAllDimensionSync(final Bitmap bitmap, final EvaluatAllListener<Float> listener) {

        if ( bitmap == null ) {

            if ( listener != null ) {

                listener.failure("图片为空");
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

                if ( listener != null ) {

                    float blurValue = detectBlurValue(bitmap);
                    float contrastValue = detectContrastValue(bitmap);
                    float saturationValue = detectSaturationValue(bitmap);
                    ArrayList<Float> data = new ArrayList<>();
                    data.add(blurValue);
                    data.add(contrastValue);
                    data.add(saturationValue);
                    listener.success(data);
                }
            }
        }).start();
    }

    /**
     * 计算图片的所有维度的值，异步模式。
     * 该方法可能比较耗时，它的执行时间和python代码的算法有关，这里不做考虑。
     * 该方法回调参数是一个列表。
     * @param bitmap
     * @param listener
     */
    public void detectAllSync(final Bitmap bitmap, final EvaluatAllListener<EvaluatBean> listener) {

        if ( bitmap == null ) {

            if ( listener != null ) {

                listener.failure("图片为空");
            }
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

                if ( listener != null ) {

                    ArrayList<EvaluatBean> data = new ArrayList<>();
                    Bitmap blurImg = detectBlur(bitmap);
                    Bitmap contrastImg = detectContrastRatio(bitmap);
                    Bitmap saturationImg = detectSaturation(bitmap);
                    float  blurValue = detectBlurValue(bitmap);
                    float  contrastValue = detectContrastValue(bitmap);
                    float  saturationValue = detectSaturationValue(bitmap);
                    data.add(new EvaluatBean(bitmap, blurImg, "清晰度", blurValue));
                    data.add(new EvaluatBean(bitmap, contrastImg, "对比度", contrastValue));
                    data.add(new EvaluatBean(bitmap, saturationImg, "饱和度", saturationValue));
                    listener.success(data);
                }
            }
        }).start();
    }

    /**
     * 计算图片的对比度，结果将以水印的形式嵌入原图
     * @param bitmap 要计算的图片
     * @param listener 处理结果回调
     * @return 处理后的图片
     */
    public void detectContrastRatioSync(final Bitmap bitmap, final EvaluatListener listener) {

        detectImageQualitySync(bitmap, pyModuleContrast, CONTRAST_RATIO_FUNC_IMG, listener);
    }

    /**
     * 计算图片的饱和度，结果将以水印的形式嵌入原图
     * @param bitmap 要计算的图片
     * @param listener 处理结果回调
     * @return 处理后的图片
     */
    public void detectSaturationSync(final Bitmap bitmap, final EvaluatListener listener) {

        detectImageQualitySync(bitmap, pyModuleSaturation, SATURATION_FUNC_IMG, listener);
    }

    /**
     * 计算图片质量，结果将以水印的形式嵌入原图， 异步模式
     * @param bitmap 原图
     * @param pyModule 要调用的python模块
     * @param function 要调用的函数
     * @param listener 处理结果回调
     * @return 处理后的图片
     */
    private void detectImageQualitySync(final Bitmap bitmap, final PyObject pyModule, final String function, final EvaluatListener listener) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                Bitmap resBitmap = detectImageQuality(bitmap, pyModule, function);
                if ( listener != null ) {

                    if ( resBitmap == null ) {

                        listener.failure("图片为空");
                    } else {

                        listener.success(new EvaluatBean(bitmap));
                    }
                }
            }
        }).start();
    }
}

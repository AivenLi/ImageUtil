#include <jni.h>
#include <string>
#include <vector>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/types_c.h>
#include "ienhance.h"

// C++命名空间->类似于java包
using namespace cv;
using namespace std;

/**
 * 改变图像对比度
 * @param env JNI环境变量，JNI自动传递，固定参数
 * @param jobeject 固定参数
 * @param sourceArray 源图像矩阵
 * @param width 图像宽度
 * @param height 图像高度
 * @param coefficient 调整系数，该值越大对比度越大
 * @return jintArray 处理后的图片矩阵
 */
extern "C"
JNIEXPORT jintArray JNICALL
Java_com_bysj_opencv450_OpenCVUtil_changeContrast(JNIEnv *env, jobject,
        jintArray sourceArray, jint width, jint height, jfloat coefficient) {

    /**
     * 将Java层的数组转为C++数组
     * 如果为空，返回原值。
     */
    jint *cPixArr = env->GetIntArrayElements(sourceArray, JNI_FALSE);
    if ( cPixArr == NULL ) {

        return sourceArray;
    }

    /**
     * 将图片矩阵转为OpenCV图片矩阵
     */
    Mat mat_image_src(height, width, CV_8UC4, (unsigned char *) cPixArr);

    /**
     * 将4通道转换为3通道
     */
    Mat mat_image_dst;
    cvtColor(mat_image_src, mat_image_dst, COLOR_RGB2YCrCb, 3);

    /**
     * 克隆3通道图片，保留原图作为基准参数，然后对克隆图片进行处理。
     */
    Mat mat_image_clone = mat_image_src.clone();

    int row = mat_image_clone.rows;
    int col = mat_image_clone.cols;

    /**
     * 分离图片通道
     */
    vector<Mat> channels(3);
    split(mat_image_clone, channels);

    /**
     * 用于存放局部均值和局部标准差
     */
    Mat localMeansMatrix(row, col, CV_32FC1);
    Mat localVarianceMatrix(row, col, CV_32FC1);

    /**
     * 对图片做边界扩充，以处理图片边界
     */
    Mat mat_image_border;
    int const borderSize = ( WIN_SIZE - 1 ) / 2;
    copyMakeBorder(channels[0], mat_image_border, borderSize, borderSize, borderSize, borderSize, BORDER_REFLECT);

    /**
     * 遍历图片的像素点，并计算像素点的局部均值和局部标准差
     */
    for ( int i = borderSize; i < row - borderSize; i++ ) {

        for ( int j = borderSize; j < col - borderSize; j++ ) {

            Mat temp = mat_image_border(Rect(j-borderSize, i-borderSize, WIN_SIZE, WIN_SIZE));
            Scalar mean;
            Scalar dev;
            meanStdDev(temp, mean, dev);
            localVarianceMatrix.at<float>(i-borderSize, j-borderSize) = dev.val[0];
            localMeansMatrix.at<float>(i-borderSize, j-borderSize) = mean.val[0];
        }
    }

    /**
     * 获取全局均值
     */
    Mat temp = channels[0].clone();
    Scalar mean;
    Scalar dev;
    meanStdDev(temp, mean, dev);

    float meansGlobal = mean.val[0];

    Mat enhance(row, col, CV_8UC1);

    /**
     * 处理图片对比度
     */
    for ( int i = 0; i < row; i++ ) {

        for ( int j = 0; j < col; j++ ) {

            if ( localVarianceMatrix.at<float>(i, j) >= 0.01f ) {

                float cg = coefficient * meansGlobal / localVarianceMatrix.at<float>(i, j);
                float cgs = GET_MIN(cg, MAX_CG);
                cgs = GET_MAX(cgs, 1);
                int e = (int)(localMeansMatrix.at<float>(i, j) + cgs * ( (float)temp.at<uchar>(i, j) - localMeansMatrix.at<float>(i, j)));
                if ( e > 255 ) {

                    e = 255;
                } else if ( e < 0 ) {

                    e = 0;
                }
                enhance.at<uchar>(i, j) = e;
            } else {

                enhance.at<uchar>(i, j) = temp.at<uchar>(i, j);
            }
        }
    }
    channels[0] = enhance;

    /**
     * 合并通道
     */
    merge(channels, mat_image_clone);

    /**
     * 将克隆的图片转回四通道并赋给原图
     */
    cvtColor(mat_image_clone, mat_image_src, CV_RGB2RGBA, 3);

    /**
     * 转为Java数组然后资源，最后返回处理结果
     * 如果不释放资源，会造成内存泄露，这是C/C++容易犯的错误。
     */
    env->ReleaseIntArrayElements(sourceArray, cPixArr, 0);

    return sourceArray;
}


/**
 * 改变图像饱和度
 * @param env JNI环境变量，JNI自动传递，固定参数
 * @param jobeject 固定参数
 * @param sourceArray 源图像矩阵
 * @param width 图像宽度
 * @param height 图像高度
 * @param coefficient 调整系数，该值越大对比度越大
 * @return jintArray 处理后的图片矩阵
 */
extern "C"
JNIEXPORT jintArray JNICALL
Java_com_bysj_opencv450_OpenCVUtil_changeSaturation(JNIEnv *env, jobject,
        jintArray sourceArray, jint width, jint height, jfloat coefficient) {

    /**
     * 将Java层的数组转为C++数组
     * 如果为空，返回原值。
     */
    jint *cPixArr = env->GetIntArrayElements(sourceArray, JNI_FALSE);
    if ( cPixArr == NULL ) {

        return sourceArray;
    }

    /**
     * 将图片矩阵转为OpenCV图片矩阵
     */
    Mat mat_image_src(height, width, CV_8UC4, (unsigned char *) cPixArr);

    /**
     * 将4通道转换为3通道
     */
    Mat mat_image_dst;
    cvtColor(mat_image_src, mat_image_dst, CV_RGB2BGR, 3);

    /**
     * 克隆3通道图片，保留原图作为基准参数，然后对克隆图片进行处理。
     */
    Mat mat_image_clone = mat_image_dst.clone();

    int row = mat_image_clone.rows;
    int col = mat_image_clone.cols;

    float increment = ( coefficient - 80.0f ) * 1.0f / MAX_INCREMENT;
    Mat new_img(row, col, mat_image_clone.type());
    for ( int c = 0; c < col; c++ ) {

        for ( int r = 0; r < row; r++ ) {

            uchar blue   = mat_image_clone.at<Vec3b>(r, c)[0];
            uchar green = mat_image_clone.at<Vec3b>(r, c)[1];
            uchar red  = mat_image_clone.at<Vec3b>(r, c)[2];
            float maxn = GET_MAX(green, blue);
            maxn = GET_MAX(red, maxn);
            float minn = GET_MIN(green, blue);
            minn = GET_MIN(red, minn);

            float delta, value;

            delta = ( maxn - minn ) / 255;
            value = ( maxn + minn ) / 255;

            float new_r, new_g, new_b;

            if ( delta == 0 ) {

                new_img.at<Vec3b>(r, c)[0] = red;
                new_img.at<Vec3b>(r, c)[1] = green;
                new_img.at<Vec3b>(r, c)[2] = blue;
                continue;
            }

            float light, sat, alpha;

            light = value / 2;
            if ( light < 0.5 ) {

                sat = delta / value;
            } else {

                sat = delta / ( 2 - value );
            }

            if ( increment >= 0 ) {

                if ( ( increment + sat ) >= 1 ) {

                    alpha = sat;
                } else {

                    alpha = 1 - increment;
                }
                alpha = 1 / alpha - 1;
                new_r = red + ( red - light * 255 ) * alpha;
                new_g = green + ( green - light * 255 ) * alpha;
                new_b = blue + ( blue - light * 255 ) * alpha;
            } else {

                alpha = increment;
                new_r = light * 255 + ( red - light * 255 ) * ( 1 + alpha );
                new_g = light * 255 + ( green - light * 255 ) * ( 1 + alpha );
                new_b = light * 255 + ( blue - light * 255 ) * ( 1 + alpha );
            }
            new_img.at<Vec3b>(r, c)[0] = new_b;
            new_img.at<Vec3b>(r, c)[1] = new_g;
            new_img.at<Vec3b>(r, c)[2] = new_r;
        }
    }

    /**
     * 将图片转回四通道
     */
    cvtColor(new_img, mat_image_src, CV_RGB2BGR, 4);

    /**
     * 将处理结果转为Java数组并释放资源，最后返回处理结果
     * 如果不释放资源，会造成内存泄露，这是C/C++容易犯的错误。
     */
    env->ReleaseIntArrayElements(sourceArray, cPixArr, 0);

    return sourceArray;
}

/**
 * 改变图像清晰度
 * @param env JNI环境变量，JNI自动传递，固定参数
 * @param jobeject 固定参数
 * @param sourceArray 源图像矩阵
 * @param width 图像宽度
 * @param height 图像高度
 * @param coefficient 调整系数，该值越大对比度越大
 * @return jintArray 处理后的图片矩阵
 */
extern "C"
JNIEXPORT jintArray JNICALL
Java_com_bysj_opencv450_OpenCVUtil_changeClarity(JNIEnv *env, jobject,
                                                    jintArray sourceArray, jint width, jint height, jfloat coefficient) {

    /**
     * 将Java层的数组转为C++数组
     * 如果为空，返回原值。
     */
    jint *cPixArr = env->GetIntArrayElements(sourceArray, JNI_FALSE);
    if ( cPixArr == NULL ) {

        return sourceArray;
    }

    /**
     * 将图片矩阵转为OpenCV图片矩阵
     */
    Mat mat_image_src(height, width, CV_8UC4, (unsigned char *) cPixArr);

    /**
     * 将4通道转换为3通道
     */
    Mat mat_image_dst;
    cvtColor(mat_image_src, mat_image_dst, CV_RGB2RGBA, 3);

    /**
     * 克隆3通道图片，保留原图作为基准参数，然后对克隆图片进行处理。
     */
    Mat mat_image_clone = mat_image_dst.clone();

    int row = mat_image_clone.rows;
    int col = mat_image_clone.cols;

    Mat blur_img;

    /**
     * 高斯滤波
     */
    GaussianBlur(mat_image_clone, blur_img, Size(9, 9), 0, 0);

    Mat DiffMask, dst;
    DiffMask = Mat::zeros(row, col, mat_image_clone.type());
    float factor = coefficient;
    int value_diff[3] = {0};
    for ( int i = 0; i < row; i++ ) {

        for ( int j = 0; j < col; j++ ) {

            for ( int k = 0; k < 3; k++ ) {

                value_diff[k] = abs(mat_image_clone.at<Vec3b>(i, j)[k] - blur_img.at<Vec3b>(i, j)[k]);
                if ( value_diff[k] < THRESHOLD ) {

                    DiffMask.at<Vec3b>(i, j)[k] = 1;
                } else {

                    DiffMask.at<uchar>(i, j) = 0;
                }
            }
        }
    }

    /**
     * 将图片原图和增强后的图片进行融合
     */
    addWeighted(mat_image_clone, 1 + factor, blur_img, -factor, 0, dst);

    /**
     * 将克隆的图片转回四通道并赋给原图
     */
    //cvtColor(mat_image_clone, mat_image_src, CV_RGB2RGBA, 4);
    cvtColor(dst, mat_image_src, CV_RGB2RGBA, 4);
    /**
     * 转为Java数组然后资源，最后返回处理结果
     * 如果不释放资源，会造成内存泄露，这是C/C++容易犯的错误。
     */
    env->ReleaseIntArrayElements(sourceArray, cPixArr, 0);

    return sourceArray;
}
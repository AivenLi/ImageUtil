#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/types_c.h>

// C++命名空间->类似于java包
using namespace cv;
using namespace std;

#define TAG "mainAC++"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C"
JNIEXPORT void JNICALL
Java_com_bysj_opencv450_OpenCVUtil_cppImageProcess(JNIEnv *env,
                                                                                jobject jobj,
                                                                                jint jw,
                                                                                jint jh,
                                                                                jintArray jPixArr,
                                                                                jint jld) {
    // 第一步：导入OpenCV头文件
    // 第二步：将Java数组->C/C++数组
    jint *cPixArr = env->GetIntArrayElements(jPixArr, JNI_FALSE);
    if (cPixArr == NULL) {
        return;
    }
    // 第三步：将C/C++图片->Opencv图片
    Mat mat_image_src(jh, jw, CV_8UC4, (unsigned char *) cPixArr);
    // 增加一个往往会忽略的一步,将4通道Mat转换为3通道Mat，才能进行图像处理
    Mat mat_image_dst;
    cvtColor(mat_image_src, mat_image_dst, CV_RGBA2BGR, 3);
    // 第四步：进行图片处理
    // 克隆一张图片
    Mat mat_image_clone = mat_image_dst.clone();
    for (int i = 0; i < jh; i++) {
        for (int j = 0; j < jw; j++) {
            // 获取颜色值->修改颜色值
            // mat_image_clone.at<Vec3b>(i,j);获取像素点值，颜色值数组
            // 颜色值->Blue
            // mat_image_clone.at<Vec3b>(i,j)[0]表示获取蓝色值,saturate_cast<uchar>(),截取uchar长度的数据
            mat_image_clone.at<Vec3b>(i, j)[0] = saturate_cast<uchar>(
                    mat_image_dst.at<Vec3b>(i, j)[0] + jld);
            // 颜色值->Red
            // mat_image_clone.at<Vec3b>(i,j)[1]表示获取蓝色值
            mat_image_clone.at<Vec3b>(i, j)[1] = saturate_cast<uchar>(
                    mat_image_dst.at<Vec3b>(i, j)[1] + jld);
            // 颜色值->Green
            // mat_image_clone.at<Vec3b>(i,j)[2]表示获取蓝色值
            mat_image_clone.at<Vec3b>(i, j)[2] = saturate_cast<uchar>(
                    mat_image_dst.at<Vec3b>(i, j)[2] + jld);
        }
    }
    // 第五步：将修改后的数据赋值给原始Mat->mat_image_src
    cvtColor(mat_image_clone, mat_image_src, CV_RGB2RGBA, 4);
    // 第六步：更新Java数组
    // 0:表示处理完成之后，将C的内存释放掉
    env->ReleaseIntArrayElements(jPixArr, cPixArr, 0);
}

#define WIN_SIZE 15
#define MAX_CG 7.5
#define GET_MAX(a, b) ( a > b ? a : b )
#define GET_MIN(a, b) ( a > b ? b : a )

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
    // 第一步：导入OpenCV头文件
    // 第二步：将Java数组->C/C++数组
    jint *cPixArr = env->GetIntArrayElements(sourceArray, JNI_FALSE);
    if (cPixArr == NULL) {
        return sourceArray;
    }
    // 第三步：将C/C++图片->Opencv图片
    Mat mat_image_src(height, width, CV_8UC4, (unsigned char *) cPixArr);
    // 增加一个往往会忽略的一步,将4通道Mat转换为3通道Mat，才能进行图像处理
    Mat mat_image_dst;
    cvtColor(mat_image_src, mat_image_dst, CV_RGB2RGBA, 3);
    // 第四步：进行图片处理
    // 克隆一张图片
    Mat mat_image_clone = mat_image_dst.clone();
    //获取行数和列数
    int row = mat_image_clone.rows;
    int col = mat_image_clone.cols;

    vector<Mat> channels(3);
    split(mat_image_clone, channels);

    Mat localMeansMatrix(row, col, CV_32FC1);
    Mat localVarianceMatrix(row, col, CV_32FC1);
    // 边界扩充，以处理边界
    Mat mat_image_border;
    int const borderSize = ( WIN_SIZE - 1 ) / 2;
    copyMakeBorder(channels[0], mat_image_border, borderSize, borderSize, borderSize, borderSize, BORDER_REFLECT);

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

    Mat temp = channels[0].clone();
    Scalar mean;
    Scalar dev;
    meanStdDev(temp, mean, dev);

    float meansGlobal = mean.val[0];

    Mat enhance(row, col, CV_8UC1);

    for ( int i = 0; i < row; i++ ) {

        for ( int j = 0; j < col; j++ ) {

            if ( localVarianceMatrix.at<float>(i, j) >= 0.01 ) {

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
    merge(channels, mat_image_clone);
    // 第五步：将修改后的数据赋值给原始Mat->mat_image_src
    cvtColor(mat_image_clone, mat_image_src, CV_RGB2RGBA, 4);
    // 第六步：更新Java数组
    // 0:表示处理完成之后，将C的内存释放掉
    env->ReleaseIntArrayElements(sourceArray, cPixArr, 0);
    return sourceArray;
}


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
Java_com_bysj_opencv450_OpenCVUtil_changeSaturation(JNIEnv *env, jobject,
        jintArray sourceArray, jint width, jint height, jfloat coefficient) {

    // 第一步：导入OpenCV头文件
    // 第二步：将Java数组->C/C++数组
    jint *cPixArr = env->GetIntArrayElements(sourceArray, JNI_FALSE);
    if (cPixArr == NULL) {
        return sourceArray;
    }
    // 第三步：将C/C++图片->Opencv图片
    Mat mat_image_src(height, width, CV_8UC4, (unsigned char *) cPixArr);
    // 增加一个往往会忽略的一步,将4通道Mat转换为3通道Mat，才能进行图像处理
    Mat mat_image_dst;
    cvtColor(mat_image_src, mat_image_dst, CV_RGB2BGR, 3);
    // 第四步：进行图片处理
    // 克隆一张图片
    Mat mat_image_clone = mat_image_dst.clone();
    //获取行数和列数
    int row = mat_image_clone.rows;
    int col = mat_image_clone.cols;
#if 0
    Mat temp;
    vector<Mat> channels;
    /**
     * 将图片转到HSL空间
     */
    cvtColor(mat_image_clone, temp, CV_RGB2HSV);
    split(temp, channels);
    /**
     * channels[0], 色调
     * channels[1], 饱和度
     * channels[2], 亮度
     */
#endif
#define MAX_INCREMENT 50
    LOGD("%0.2f\n", coefficient);
    float increment = ( coefficient - 80.0f ) * 1.0f / MAX_INCREMENT;
    Mat new_img(row, col, mat_image_clone.type());
    //new_img = cvCreateMat(row, col, mat_image_clone.type());
    for ( int c = 0; c < col; c++ ) {

        for ( int r = 0; r < row; r++ ) {

            uchar blue   = mat_image_clone.at<Vec3b>(r, c)[0]; // blue
            uchar green = mat_image_clone.at<Vec3b>(r, c)[1]; // green
            uchar red  = mat_image_clone.at<Vec3b>(r, c)[2]; // red
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
    // 第五步：将修改后的数据赋值给原始Mat->mat_image_src
    cvtColor(new_img, mat_image_src, CV_RGB2BGR, 4);
    // 第六步：更新Java数组
    // 0:表示处理完成之后，将C的内存释放掉
    env->ReleaseIntArrayElements(sourceArray, cPixArr, 0);
    return sourceArray;
}

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
Java_com_bysj_opencv450_OpenCVUtil_changeClarity(JNIEnv *env, jobject,
                                                    jintArray sourceArray, jint width, jint height) {

    // 第一步：导入OpenCV头文件
    // 第二步：将Java数组->C/C++数组
    jint *cPixArr = env->GetIntArrayElements(sourceArray, JNI_FALSE);
    if (cPixArr == NULL) {
        return sourceArray;
    }
    // 第三步：将C/C++图片->Opencv图片
    Mat mat_image_src(height, width, CV_8UC4, (unsigned char *) cPixArr);
    // 增加一个往往会忽略的一步,将4通道Mat转换为3通道Mat，才能进行图像处理
    Mat mat_image_dst;
    cvtColor(mat_image_src, mat_image_dst, CV_RGB2BGR, 3);
    // 第四步：进行图片处理
    // 克隆一张图片
    Mat mat_image_clone = mat_image_dst.clone();
    //获取行数和列数
    int row = mat_image_clone.rows;
    int col = mat_image_clone.cols;

    for ( int i = 0; i < row; i++ ) {

        for ( int j = 0; j < col; j++ ) {

            int temp = 0;//用来计算新的中心像素点值

            int oper[3][3] = {{-1,-1,-1},{-1,10,-1},{-1,-1,-1}};//拉普拉斯算子

            for(int k = 0;k < 3;k++){

                for(int l = 0;l < 3;l++){

                    //当算子位于边缘区域时，排除图像中不存在的点

                    if((i-1+k < 0) || (j-1+l < 0) || (i-1+k >= row) || (j-1+l >= col))

                        continue;

                    temp += oper[k][l]*mat_image_clone.at<Vec3b>(i-1+k,j-1+l)[0];

                }

            }

            //当计算结果超出255，就把它置为255

            if(temp > 255){

                temp = 255;

            }

            //当计算结果小于0，就把它置为0

            if(temp < 0){

                temp = 0;

            }

            //将计算结果赋给中心像素点的三通道值

            for(int m = 0;m < 3;m++){

                mat_image_clone.at<Vec3b>(i,j)[m] = temp;
            }
        }
    }

    // 第五步：将修改后的数据赋值给原始Mat->mat_image_src
    cvtColor(mat_image_clone, mat_image_src, CV_RGB2BGR, 4);
    // 第六步：更新Java数组
    // 0:表示处理完成之后，将C的内存释放掉
    env->ReleaseIntArrayElements(sourceArray, cPixArr, 0);
    return sourceArray;
}
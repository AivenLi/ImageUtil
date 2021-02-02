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

extern "C"
JNIEXPORT jintArray JNICALL
Java_com_bysj_opencv450_OpenCVUtil_lightPixels(JNIEnv *env, jobject ,
                                                           jintArray pixels_, jint w, jint h) {
    // 第一步：导入OpenCV头文件
    // 第二步：将Java数组->C/C++数组
    jint *cPixArr = env->GetIntArrayElements(pixels_, JNI_FALSE);
    if (cPixArr == NULL) {
        return pixels_;
    }
    // 第三步：将C/C++图片->Opencv图片
    Mat mat_image_src(h, w, CV_8UC4, (unsigned char *) cPixArr);
    // 增加一个往往会忽略的一步,将4通道Mat转换为3通道Mat，才能进行图像处理
    Mat mat_image_dst;
    cvtColor(mat_image_src, mat_image_dst, CV_RGBA2BGR, 3);
    // 第四步：进行图片处理
    // 克隆一张图片
    Mat mat_image_clone = mat_image_dst.clone();
    //获取行数和列数
    int row = mat_image_clone.rows;
    int col = mat_image_clone.cols;

    int n = 7;
    int c = 5;
    int max = 8;
    Mat sdLocal(mat_image_clone.size(), mat_image_clone.type());
    Mat a;
    Mat meanLocal;
    blur(mat_image_clone.clone(), meanLocal, Size(n, n));
    Mat highFre = mat_image_clone - meanLocal;
    Mat varSingle = highFre.mul(highFre);
    Mat varLocal;
    blur(varSingle, varLocal, Size(n, n));
    a = meanLocal + c * highFre;


    cvtColor(highFre, mat_image_src, CV_RGB2RGBA, 4);
    // 第五步：将修改后的数据赋值给原始Mat->mat_image_src
    //cvtColor(mat_image_clone, mat_image_src, CV_RGB2RGBA, 4);
    // 第六步：更新Java数组
    // 0:表示处理完成之后，将C的内存释放掉
    env->ReleaseIntArrayElements(pixels_, cPixArr, 0);
    return pixels_;
}
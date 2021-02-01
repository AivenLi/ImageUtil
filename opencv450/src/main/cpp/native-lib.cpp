#include <jni.h>
#include <string>
#include <opencv2/opencv.hpp>
#include <opencv2/imgproc/types_c.h>

// C++命名空间->类似于java包
using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_com_bysj_opencv450_OpenCV_cppImageProcess(JNIEnv *env,
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

extern "C"
JNIEXPORT void JNICALL
Java_com_example_changxiaoyu_jniopencvdemo_CppImageProcessUtils_cppImageThreshold(JNIEnv *env,
                                                                                   jclass jclass,
                                                                                   jint jw, jint jh,
                                                                                   jintArray jpixArr) {
    jint* cPixArr = env->GetIntArrayElements(jpixArr,JNI_FALSE);
    if(cPixArr == NULL){
        return;
    }
    Mat mat_image_src(jh,jw,CV_8UC4,(unsigned char*)cPixArr);
    Mat mat_image_dst;
    cvtColor(mat_image_src,mat_image_dst,CV_RGBA2GRAY,1);
    Mat mat_image_thereshold;
    cv::adaptiveThreshold(mat_image_dst,mat_image_thereshold,255,ADAPTIVE_THRESH_GAUSSIAN_C,CV_THRESH_BINARY,31,9);
    cvtColor(mat_image_thereshold,mat_image_src,CV_GRAY2BGRA,4);
    env->ReleaseIntArrayElements(jpixArr,cPixArr,0);
}
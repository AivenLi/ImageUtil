/**
 * Created by Administrator on 2021/2/12.
 *
 * @description:
 *      图片增强，应用于图片的对比度、饱和度和清晰度;
 *      基于Android平台。
 */

#ifndef IMAGEUTIL_IENHANCE_H
#define IMAGEUTIL_IENHANCE_H

#ifndef TRUE
#define TRUE 1
#endif

#ifndef FALSE
#define FALSE 0
#endif

/**
 * 调试时允许打印日志。
 */
#define ANDROID_DEBUG TRUE

/**
 * 正式版不需要打印日志
 */
//#define ANDROID_DEBUG FALSE

#if ( ANDROID_DEBUG == TRUE )
#include <android/log.h>
#define TAG "android C++ log"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#endif

/**
 * 对比度，
 * 扩充边界时的边界大小
 */
#define WIN_SIZE 15
/**
 * 对比度，
 * 增强幅度最大值
 */
#define MAX_CG 7.5
/**
 * 获取两个值大的那个值
 */
#define GET_MAX(a, b) ( a > b ? a : b )
/**
 * 获取两个值小的那个值
 */
#define GET_MIN(a, b) ( a > b ? b : a )
/**
 * 清晰度，
 * 最大阈值
 */
#define THRESHOLD 30
/**
 * 饱和度，
 * 最大阈值
 */
#define MAX_INCREMENT 50

#endif //IMAGEUTIL_IENHANCE_H

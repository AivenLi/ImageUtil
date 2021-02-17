package com.bysj.imageutil.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.bysj.imageutil.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 文件工具类
 */
public class FileUtils {

    public interface CopyFileListener {

        void success(String filePath);
        void failure(String error);
    }
    /**
     *  复制单个文件
     * @param oldPath 原文件路径 如：c:/fqf.txt String
     * @param newPath 复制后路径 如：f:/fqf.txt
     * @param listener 复制状态回调
     */
    public static void copyFile(final String oldPath, final String newPath, final CopyFileListener listener) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    LogCat.d("mainA复制文件", "开始");
                    //int bytesum = 0;
                    int byteread = 0;
                    File oldfile = new File(oldPath);
                    if ( oldfile.exists() ) {//文件存在时

                        LogCat.d("mainA复制文件", "文件存在");
                        InputStream inStream = new FileInputStream(oldPath);//读入原文件
                        FileOutputStream fs = new FileOutputStream(newPath);
                        byte[] buffer = new byte[1444];
                        //int length;
                        //int value = 0 ;
                        while ( ( byteread = inStream.read(buffer) ) != -1 ) {

                            //bytesum += byteread;//字节数 文件大小
                            //value++ ;  //计数
                            fs.write(buffer, 0, byteread);
                        }
                        inStream.close();
                        if ( listener != null ) {

                            listener.success(newPath);
                        }
                    } else {

                        if ( listener != null ) {

                            listener.failure("文件不存在");
                        }
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                    if ( listener != null ) {

                        listener.failure(e.toString());
                    }
                }
            }
        }).start();
    }

    private static final String GALLERY_NAME = "IEnhance";
    private static boolean saving = false;
    public interface SaveImageListener {

        void success(String filePath);
        void failure(String error);
    }

    /**
     * 保存图片到系统图库
     * @param context 上下文
     * @param bmp 原图
     * @param listener 执行结果回调
     */
    public static void saveImageToGallery(Context context, Bitmap bmp, SaveImageListener listener) {

        /**
         * 如果已经有一个任务在执行了，则直接返回
         */
        if ( saving ) {

            if ( listener != null ) {

                listener.failure("正在保存其他图片，请稍后再试");
            }
            return;
        }
        saving = true;
        new Thread(new Runnable() {
            @Override
            public void run() {

                /**
                 * 创建文件
                 */
                File appDir = new File(Environment.getExternalStorageDirectory(), GALLERY_NAME);

                /**
                 * 创建目录，如果不存在的话
                 */
                if (!appDir.exists()) {

                    appDir.mkdir();
                }

                /**
                 * 设置文件名
                 */
                String fileName = System.currentTimeMillis() + ".jpg";
                File file = new File(appDir, fileName);
                try {

                    /**
                     * 写入文件
                     */
                    FileOutputStream fos = new FileOutputStream(file);
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                    fos.flush();
                    fos.close();
                    MediaStore.Images.Media.insertImage(context.getContentResolver(),
                            file.getAbsolutePath(), fileName, null);
                    /**
                     * 通知系统图库更新
                     */
                    String[] paths = new String[]{file.getAbsolutePath()};
                    MediaScannerConnection.scanFile(context, paths, null, null);
                    saving = false;
                    if ( listener != null ) {

                        listener.success(file.getAbsolutePath());
                    }
                } catch (IOException e) {

                    e.printStackTrace();
                    saving = false;
                    if ( listener != null ) {

                        listener.failure(e.toString());
                    }
                }
            }
        }).start();
    }
}

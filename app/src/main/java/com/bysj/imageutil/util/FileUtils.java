package com.bysj.imageutil.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
}

package com.bysj.imageutil.ui.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bysj.imageutil.R;
import com.bysj.imageutil.base.BaseActivity;
import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.ui.components.dialog.DialogPromptListener;
import com.bysj.imageutil.util.GlideUtil;
import com.bysj.imageutil.util.IntentKeys;
import com.bysj.imageutil.util.LogCat;
import com.bysj.opencv450.TestCpp;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    /** 日志标志 */
    private static final String TAG            = "mainActivity";
    /** 图片控件 */
    private ImageView           mImg;
    /** 选择图片控件 */
    private Button              mBtnChooseImg;
    /** 图片处理 */
    private Button              mBtnHandle;
    /** 再按一次退出程序 */
    private long                exitTime        = 0;
    /** 相机拍照后的图片固定路径 */
    private String              capturePath     = null;
    /** 相机拍照后的图片文件名 */
    private String              captureFilePath = null;
    /** 相册选择的图片的路径 */
    private String              photosPath      = null;
    /** 相机拍照后的图片文件 */
    private File                captureFile     = null;
    /** 是否是相机拍照的图片 */
    private boolean             isCapture;

    /**
     * 加载C++库
     */
    static {
        System.loadLibrary("netive-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        /** 加载opencv动态库 */
        boolean opencv = OpenCVLoader.initDebug();
        if ( opencv ) {

            LogCat.d(TAG, "Loaded OpenCV success");
        } else {

            LogCat.d(TAG, "Loaded OpenCV failure");
        }
        mDialog = new DialogPrompt(mContext);
        capturePath = mContext.getCacheDir().getPath() + "/";
    }

    @Override
    protected int initLayout() {

        return R.layout.activity_main;
    }

    @Override
    protected void initView() {

        mImg = findViewById(R.id.test_image);
        mBtnChooseImg = findViewById(R.id.btn_choose_img);
        mBtnHandle = findViewById(R.id.btn_handle);
        mBtnChooseImg.setText(new TestCpp().stringFromJNI());
    }

    @Override
    protected void setViewOnClick() {

        mImg.setOnClickListener(this);
        mBtnHandle.setOnClickListener(this);
        mBtnChooseImg.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        switch ( view.getId() ) {

            case R.id.test_image:
            case R.id.btn_choose_img:
                chooseImg();
                break;
            case R.id.btn_handle:
                handleImg();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        LogCat.d(TAG, requestCode + "," + resultCode);
        /**
         * 判断是否成功获取到图片
         */
        if ( resultCode == RESULT_OK ) {
            /**
             * 系统相册
             */
            if ( requestCode == IntentKeys.MAIN_TO_PHOTOS ) {

                isCapture = false;
                Uri uri = data.getData();
                photosPath = uri.getPath();
                LogCat.d(TAG, photosPath);
                mImg.setImageURI(uri);
            }
            /**
             * 相机
             */
            if ( requestCode == IntentKeys.MAIN_TO_CAMERA ) {

                isCapture = true;
                GlideUtil.clearDiskCache(mContext);
                Glide.with(mImg.getContext())
                        .load(captureFilePath)
                        .placeholder(android.R.color.darker_gray)
                        .into(mImg);
                clearCacheCapture(captureFilePath);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 选择图片
     */
    private void chooseImg() {

        mDialog.setTitle(getString(R.string.dia_choose_img_title))
                .setContent("")
                .setYesText(getString(R.string.dia_choose_img_camera))
                .setCancelText(getString(R.string.dia_choose_img_photos))
                .show(new DialogPromptListener() {
                    @Override
                    public void yes() {

                        //调用系统相机的意图
                        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        captureFilePath = capturePath + System.currentTimeMillis() + "_capture.jpg";
                        LogCat.d(TAG, captureFilePath);

                        captureFile = new File(captureFilePath);

                        Uri uri = FileProvider.getUriForFile(mContext, "com.bysj.imageutil.fileprovider", captureFile);
                        takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(takePhotoIntent, IntentKeys.MAIN_TO_CAMERA);
                    }

                    @Override
                    public void cancel() {

                        //调用系统图库的意图
                        Intent choosePicIntent = new Intent(Intent.ACTION_PICK, null);
                        choosePicIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(choosePicIntent, IntentKeys.MAIN_TO_PHOTOS);
                    }
                });
    }

    /**
     * 处理图片
     */
    private void handleImg() {

        String imgPath = isCapture ? captureFilePath : photosPath;
        if ( TextUtils.isEmpty(imgPath) ) {

            myToast(getString(R.string.img_is_null));
        } else {

            LogCat.d(TAG, imgPath);
            Mat src = new Mat();
            Mat dst = new Mat();
            Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
            Utils.bitmapToMat(bitmap, src);
            Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);
            Utils.matToBitmap(dst, bitmap);
            mImg.setImageBitmap(bitmap);
        }
    }

    /**
     * 清除相机拍照后产生的缓存图片
     * @param saveFileName 保留的图片
     */
    private void clearCacheCapture(String saveFileName) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                File dir = mContext.getCacheDir();
                String[] files = dir.list();
                if ( files == null ) {

                    return;
                }
                for (String s : files) {

                    File file = new File(dir, s);
                    String fileName = file.getPath();
                    if ( fileName.contains(".jpg") && fileName.contains("capture") && !fileName.equals(saveFileName)) {

                        LogCat.d(MainActivity.TAG, fileName);
                        if ( file.exists() ) {

                            file.delete();
                            LogCat.d(MainActivity.TAG, fileName);
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 再按一次退出程序
     */
    public void exit() {

        if ((System.currentTimeMillis() - exitTime) > 2000) {

            Toast.makeText(getApplicationContext(), getString(R.string.exit_app),
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {

            super.onBackPressed();
            finish();
            System.exit(0);
        }
    }
}
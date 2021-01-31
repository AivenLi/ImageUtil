package com.bysj.imageutil.ui.activity;

import androidx.core.content.FileProvider;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bysj.imageutil.R;
import com.bysj.imageutil.base.BaseActivity;
import com.bysj.imageutil.ui.components.RegulatorView;
import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.ui.components.dialog.DialogPromptListener;
import com.bysj.imageutil.util.FileUtils;
import com.bysj.imageutil.util.GetImgPath;
import com.bysj.imageutil.util.GlideUtil;
import com.bysj.imageutil.util.HandleKeys;
import com.bysj.imageutil.util.IntentKeys;
import com.bysj.imageutil.util.LogCat;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.Objects;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    /** 日志标志 */
    private static final String TAG            = "mainActivity";
    /** 源图片控件 */
    private ImageView           mImgSource;
    /** 修改后图片控件 */
    private ImageView           mImgTarget;
    /** 提示添加图片控件 */
    private LinearLayout        mLytAddImgPrompt;
    /** 调整图片控制面板 */
    private LinearLayout        mLytController;
    /** 清晰度调节器 */
    private RegulatorView       mRglClarity;
    /** 对比度调节器 */
    private RegulatorView       mRglContrast;
    /** 饱和度调节器 */
    private RegulatorView       mRglSaturation;
    /** 保存图片按钮 */
    private TextView            mTvSave;

    /** 再按一次退出程序 */
    private long                exitTime        = 0;
    /** 选取图片后图片副本的固定路径 */
    private String              imagePath     = null;
    /** 相机拍照后的图片文件名 */
    private String              imageFilePath = null;
    /** 相册选择的图片的路径 */
    private String              photosPath      = null;
    /** 相机拍照后的图片文件 */
    private File                captureFile     = null;
    /** 清晰度的当前值 */
    private float               clarityValue;
    /** 对比度的当前值 */
    private float               contrastValue;
    /** 饱和度的当前值 */
    private float               saturationValue;

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

        mImgSource       = findViewById(R.id.img_source);
        mImgTarget       = findViewById(R.id.img_target);
        mLytAddImgPrompt = findViewById(R.id.lyt_add_image_prompt);
        mRglClarity      = findViewById(R.id.rgl_clarity);
        mRglContrast     = findViewById(R.id.rgl_contrast);
        mRglSaturation   = findViewById(R.id.rgl_saturation);
        mLytController   = findViewById(R.id.lyt_controller);
        mTvSave          = findViewById(R.id.tv_save);

        setShowImage(false);
        initRglRatio();
    }

    @Override
    protected void setViewOnClick() {

        mImgSource.setOnClickListener(this);
        mImgTarget.setOnClickListener(this);
        mLytAddImgPrompt.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if ( id == R.id.img_source || id == R.id.img_target || id == R.id.lyt_add_image_prompt ) {

            chooseImg();
        } else if ( id == R.id.tv_save ) {

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

                String path = GetImgPath.getPath(mContext, data.getData());
                if ( !TextUtils.isEmpty(path) ) {

                    final File file = new File(path);
                    isCapture = false;
                    FileUtils.copyFile(file.getPath(), capturePath + System.currentTimeMillis() + "_capture.jpg", new FileUtils.CopyFileListener() {
                        @Override
                        public void success(String filePath) {

                            sendMessage(HandleKeys.COPY_FILE_SUCCESS, filePath);
                        }

                        @Override
                        public void failure(String error) {

                            LogCat.d(TAG, error);
                            sendMessage(HandleKeys.COPY_FILE_FAILURE, error);
                        }
                    });
                    mImgSource.setImageURI(data.getData());
                    setShowImage(true);
                }
            }
            /**
             * 相机
             */
            if ( requestCode == IntentKeys.MAIN_TO_CAMERA ) {

                isCapture = true;
                GlideUtil.clearDiskCache(mContext);
                Glide.with(mImgSource.getContext())
                        .load(captureFilePath)
                        .placeholder(android.R.color.darker_gray)
                        .into(mImgSource);
                clearCacheCapture(captureFilePath);
                setShowImage(true);
            }
        }
    }

    @Override
    protected void onHandleMessage(final Message msg) {

        int what = msg.what;
        if ( what == HandleKeys.COPY_FILE_SUCCESS ) {

            photosPath = (String)msg.obj;
            LogCat.d(TAG + "获取成功", photosPath);
        } else if ( what == HandleKeys.COPY_FILE_FAILURE ) {

            LogCat.d(TAG + "获取失败", (String)msg.obj);
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
     * 如果用户添加了图片，则显示图片，如果没有添加，则提示添加
     * @param hasImg
     */
    private void setShowImage(boolean hasImg) {

        if ( !hasImg ) {

            mTvSave.setVisibility(View.GONE);
            mImgSource.setVisibility(View.GONE);
            mImgTarget.setVisibility(View.GONE);
            mLytController.setVisibility(View.GONE);
            mLytAddImgPrompt.setVisibility(View.VISIBLE);
        } else {

            mTvSave.setVisibility(View.VISIBLE);
            mImgSource.setVisibility(View.VISIBLE);
            mImgTarget.setVisibility(View.VISIBLE);
            mLytController.setVisibility(View.VISIBLE);
            mLytAddImgPrompt.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化调节器，监听调节器值变化事件
     */
    private void initRglRatio() {

        mRglSaturation.setShowText(true);
        mRglSaturation.setCanAdjust(true);
        mRglContrast.setShowText(true);
        mRglContrast.setCanAdjust(true);
        mRglClarity.setShowText(true);
        mRglClarity.setCanAdjust(true);

        mRglSaturation.setOnValueChangeListener(new RegulatorView.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {

                saturationValue = value;
                handleImg();
            }
        });
        mRglContrast.setOnValueChangeListener(new RegulatorView.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {

                contrastValue = value;
                handleImg();
            }
        });
        mRglClarity.setOnValueChangeListener(new RegulatorView.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {

                clarityValue = value;
                handleImg();
            }
        });
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

                        captureFilePath = capturePath + System.currentTimeMillis() + "_capture.jpg";
                        LogCat.d(TAG, captureFilePath);

                        captureFile = new File(captureFilePath);
                        //调用系统图库的意图
                        /*
                        Intent choosePicIntent = new Intent(Intent.ACTION_GET_CONTENT, null);
                        choosePicIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                        startActivityForResult(choosePicIntent, IntentKeys.MAIN_TO_PHOTOS);
*/
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("image/*");
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            //如果大于等于7.0使用
                            Uri uriForFile= FileProvider.getUriForFile(mContext,
                                    "com.bysj.imageutil.fileprovider", captureFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        startActivityForResult(intent, IntentKeys.MAIN_TO_PHOTOS);
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
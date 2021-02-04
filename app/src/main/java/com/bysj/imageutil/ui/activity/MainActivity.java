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
import com.bysj.imageutil.ui.components.dialog.DialogLoading;
import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.ui.components.dialog.DialogPromptListener;
import com.bysj.imageutil.util.FileUtils;
import com.bysj.imageutil.util.GetImgPath;
import com.bysj.imageutil.util.GlideUtil;
import com.bysj.imageutil.util.HandleKeys;
import com.bysj.imageutil.util.IntentKeys;
import com.bysj.imageutil.util.LogCat;
import com.bysj.opencv450.HandleImageListener;
import com.bysj.opencv450.OpenCVUtil;


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
    private long                exitTime            = 0;
    /** 选取图片后图片副本的固定路径 */
    private String              imgConstantPath     = null;
    /** 相机拍照后的图片文件名 */
    private String              imgChoosePath       = null;
    /** 清晰度的当前值 */
    private float               clarityValue;
    /** 对比度的当前值 */
    private float               contrastValue;
    /** 饱和度的当前值 */
    private float               saturationValue;
    /** 是否正在处理图片 */
    private boolean             isHandling         = false;
    /** OpenCV工具 */
    private OpenCVUtil          opencv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        /** 加载opencv动态库 */
        opencv = OpenCVUtil.getInstance();
        mDialog = new DialogPrompt(mContext);
        mLoading = new DialogLoading(mContext);
        imgConstantPath = mContext.getCacheDir().getPath() + "/";
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

            //handleImg();
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
                    FileUtils.copyFile(file.getPath(), imgConstantPath + System.currentTimeMillis() + "_capture.jpg", new FileUtils.CopyFileListener() {
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
                }
            }
            /**
             * 相机
             */
            if ( requestCode == IntentKeys.MAIN_TO_CAMERA ) {

                setShowImage(true);
            }
        }
    }

    @Override
    protected void onHandleMessage(final Message msg) {

        int what = msg.what;
        if ( what == HandleKeys.COPY_FILE_SUCCESS ) {

            imgChoosePath = (String)msg.obj;
            LogCat.d(TAG + "获取成功", imgChoosePath);
            setShowImage(true);
        } else if ( what == HandleKeys.COPY_FILE_FAILURE ) {

            LogCat.d(TAG + "获取失败", (String)msg.obj);
        } else if ( what == HandleKeys.ENHANCE_CONTRAST_DONE ) {

            hideLoading();
            isHandling = false;
            tempSetImageSource((Bitmap)msg.obj);
        } else if ( what == HandleKeys.ENHANCE_SATURATION_DONE ) {

            hideLoading();
            isHandling = false;
            tempSetImageSource((Bitmap)msg.obj);
        }
    }

    /**
     * 项目快完工时不再需要本方法。
     * 项目还没有全部完成，后面可能还有一些细节要调整，先用该方法来展示结果。
     * @param bitmap
     */
    private void tempSetImageSource(Bitmap bitmap) {

        Glide.with(mImgSource.getContext())
                .load(bitmap)
                .placeholder(android.R.color.darker_gray)
                .into(mImgSource);
        Glide.with(mImgTarget.getContext())
                .load(bitmap)
                .placeholder(android.R.color.darker_gray)
                .into(mImgTarget);
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
            loadImage();
        }
    }

    /**
     * 将图片显示到UI
     */
    private void loadImage() {

        GlideUtil.clearDiskCache(mContext);
        Glide.with(mImgSource.getContext())
                .load(imgChoosePath)
                .placeholder(android.R.color.darker_gray)
                .into(mImgSource);
        Glide.with(mImgTarget.getContext())
                .load(imgChoosePath)
                .placeholder(android.R.color.darker_gray)
                .into(mImgTarget);
        clearCacheCapture(imgChoosePath);
    }

    /**
     * 初始化调节器，监听调节器值变化事件
     */
    private void initRglRatio() {

        mRglSaturation.setShowText(true);
        mRglSaturation.setCanAdjust(true);
        mRglSaturation.setChangingCallback(false);
        mRglContrast.setShowText(true);
        mRglContrast.setCanAdjust(true);
        mRglContrast.setChangingCallback(false);
        mRglClarity.setShowText(true);
        mRglClarity.setCanAdjust(true);
        mRglClarity.setChangingCallback(false);

        mRglSaturation.setOnValueChangeListener(new RegulatorView.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {

                saturationValue = value;
                changeSaturation(value+50.0f, false);
            }
        });
        mRglContrast.setOnValueChangeListener(new RegulatorView.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {

                contrastValue = value;
                enhanceContrast(value / 100.0f, false);
            }
        });
        mRglClarity.setOnValueChangeListener(new RegulatorView.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {

                clarityValue = value;
                myToast("暂未实现该功能");
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
                        imgChoosePath = imgConstantPath + System.currentTimeMillis() + "_capture.jpg";

                        File file = new File(imgChoosePath);

                        Uri uri = FileProvider.getUriForFile(mContext, "com.bysj.imageutil.fileprovider", file);
                        takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(takePhotoIntent, IntentKeys.MAIN_TO_CAMERA);
                    }

                    @Override
                    public void cancel() {

                        imgChoosePath = imgConstantPath + System.currentTimeMillis() + "_capture.jpg";

                        File file = new File(imgChoosePath);
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
                                    "com.bysj.imageutil.fileprovider", file);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        startActivityForResult(intent, IntentKeys.MAIN_TO_PHOTOS);
                    }
                });
    }

    /**
     * 增强对比图处理
     * @param coefficient 增强系数
     * @param adaptive 是否自适应
     */
    private void changeSaturation(float coefficient, boolean adaptive) {

        if ( hasImage() ) {

            if ( isHandling ) {

                myToast(getString(R.string.handing));
                return;
            }
            isHandling = true;
            showLoading();
            Bitmap bitmap = BitmapFactory.decodeFile(imgChoosePath);
            if ( adaptive ) {

                opencv.changeSaturationSync(bitmap, saturationListener);
            } else {

                opencv.changeSaturationSync(bitmap, coefficient, saturationListener);
            }
        } else {

            myToast(getString(R.string.img_is_null));
        }
    }

    /**
     * 增强对比图处理
     * @param coefficient 增强系数
     * @param adaptive 是否自适应
     */
    private void enhanceContrast(float coefficient, boolean adaptive) {

        if ( hasImage() ) {

            if ( isHandling ) {

                myToast(getString(R.string.handing));
                return;
            }
            isHandling = true;
            showLoading();
            Bitmap bitmap = BitmapFactory.decodeFile(imgChoosePath);
            if ( adaptive ) {

                opencv.changeContrastSync(bitmap, contrastListener);
            } else {

                opencv.changeContrastSync(bitmap, coefficient, contrastListener);
            }
        } else {

            myToast(getString(R.string.img_is_null));
        }
    }

    HandleImageListener contrastListener = new HandleImageListener() {
        @Override
        public void done(Bitmap bitmap) {

            sendMessage(HandleKeys.ENHANCE_CONTRAST_DONE, bitmap);
        }
    };

    HandleImageListener clarityListener = new HandleImageListener() {
        @Override
        public void done(Bitmap bitmap) {

            sendMessage(HandleKeys.ENHANCE_CLARITY_DONE, bitmap);
        }
    };

    HandleImageListener saturationListener = new HandleImageListener() {
        @Override
        public void done(Bitmap bitmap) {

            sendMessage(HandleKeys.ENHANCE_SATURATION_DONE, bitmap);
        }
    };

    private boolean hasImage() {

        return !TextUtils.isEmpty(imgChoosePath);
    }

    /**
     * 清除相机拍照后产生的缓存图片
     * @param saveFileName 保留的图片
     */
    private void clearCacheCapture(String saveFileName) {

        if ( TextUtils.isEmpty(saveFileName) ) {

            return;
        }
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

                        LogCat.d(MainActivity.TAG + "删除文件", fileName);
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
package com.bysj.imageutil.ui.activity;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
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
import com.bysj.imgevaluation.EvaluatUtil;
import com.bysj.imgevaluation.listener.EvaluatAllListener;
import com.bysj.opencv450.HandleImageListener;
import com.bysj.opencv450.OpenCVUtil;
import com.bysj.opencv450.PictureMatrix;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends BaseActivity implements View.OnClickListener{

    /** 日志标志 */
    private static final String TAG                 = "mainActivity";
    /** Fragment列表 */
    private ArrayList<Fragment> fragments;
    /** 当前页 */
    private int                 currentPage;

    /** 源图片控件 */
    private ImageView           mImgSource;
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
    /** 一键自适应 */
    private TextView            mTvAdaptive;
    /** 保存图片按钮 */
    private TextView            mTvSave;
    /** 还原图片 */
    private TextView            mTvReduction;
    /** 再按一次退出程序 */
    private long                exitTime            = 0;
    /** 选取图片后图片副本的固定路径 */
    private String              imgConstantPath     = null;
    /** 相机拍照后的图片文件名 */
    private String              imgChoosePath       = null;
    /** 清晰度的当前值 */
    private float               clarityValue        = 0.0f;
    /** 对比度的当前值 */
    private float               contrastValue       = 0.0f;
    /** 饱和度的当前值 */
    private float               saturationValue     = 0.0f;
    /** 是否正在处理图片 */
    private boolean             isHandling          = false;
    /** 如果处理完图片，则需要用户确认是否使用
     * 处理后的图片。如果不确认，调整图片时是在原图
     * 的基础上进行调整
     */
    private boolean             useResultImg        = false;
    /** OpenCV工具 */
    private OpenCVUtil          opencv;
    /** 评价图片质量工具 */
    private EvaluatUtil         evaluatUtil;
    /** 处理后的图片 */
    private Bitmap              targetBitmap        = null;
    /** 确认使用处理后的图片 */
    private Bitmap              useBitmap           = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        opencv = OpenCVUtil.getInstance();
        evaluatUtil = EvaluatUtil.getInstance(mContext);
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
        mLytAddImgPrompt = findViewById(R.id.lyt_add_image_prompt);
        mRglClarity      = findViewById(R.id.rgl_clarity);
        mRglContrast     = findViewById(R.id.rgl_contrast);
        mRglSaturation   = findViewById(R.id.rgl_saturation);
        mLytController   = findViewById(R.id.lyt_controller);
        mTvAdaptive      = findViewById(R.id.tv_adaptive);
        mTvSave          = findViewById(R.id.tv_save);
        mTvReduction     = findViewById(R.id.tv_reduction);

        setShowImage(false);
        initRglRatio();
    }

    @Override
    protected void setViewOnClick() {

        mImgSource.setOnClickListener(this);
        mLytAddImgPrompt.setOnClickListener(this);
        mTvAdaptive.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
        mTvReduction.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if ( id == R.id.img_source || id == R.id.lyt_add_image_prompt ) {

            if ( isHandling ) {

                myToast(getString(R.string.handing));
            } else {

                chooseImg();
            }
        } else if ( id == R.id.tv_save ) {

            String sAction = mTvSave.getText().toString();
            if ( sAction.equals(getString(R.string.save_image)) ) {

                if ( isHandling ) {

                    myToast(getString(R.string.handing));
                } else {

                    if ( targetBitmap == null ) {

                        myToast(getString(R.string.img_is_null));
                        return;
                    }

                    /**
                     * 保存图片到系统图库
                     */
                    FileUtils.saveImageToGallery(mContext, targetBitmap, new FileUtils.SaveImageListener() {
                        @Override
                        public void success(String filePath) {

                            sendMessage(HandleKeys.SAVE_IMAGE_SUCCESS, filePath);
                        }

                        @Override
                        public void failure(String error) {

                            sendMessage(HandleKeys.SAVE_IMAGE_FAILURE, error);
                        }
                    });
                }
            } else {

                useResultImg = false;
                useBitmap = targetBitmap;
                setBtnAction(useResultImg);
                resetImgParam();
            }
        } else if ( id == R.id.tv_reduction ) {

            String tCancel = mTvReduction.getText().toString();
            if ( tCancel.equals(getString(R.string.cancel)) ) {

                targetBitmap = useBitmap;
                if ( targetBitmap == null ) {

                    targetBitmap = getFileToBitmap();
                }
                useResultImg = false;
                loadImage();
                resetImgParam();
                setBtnAction(useResultImg);
            } else if ( !isHandling ) {

                targetBitmap = getFileToBitmap();
                useBitmap = targetBitmap;
                if (targetBitmap == null) {

                    myToast(getString(R.string.img_is_null));
                } else {

                    loadImage();
                    resetImgParam();
                }
            } else {

                myToast(getString(R.string.handing));
            }
        } else if ( id == R.id.tv_adaptive ) {

            /**
             * 一键自适应调整
             */
            if ( isHandling ) {

                myToast(getString(R.string.handing));
                return;
            }
            Bitmap bitmap = getFileToBitmap();
            if ( bitmap == null ) {

                myToast(getString(R.string.img_is_null));
                return;
            }
            isHandling = true;
            showLoading();
            opencv.adaptiveEnhanceSync(bitmap, new HandleImageListener() {
                @Override
                public void done(Bitmap bitmap) {

                    sendMessage(HandleKeys.ENHANCE_ADAPTIVE_DONE, bitmap);
                }
            });
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

                targetBitmap = null;
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
            else if ( requestCode == IntentKeys.MAIN_TO_CAMERA ) {

                targetBitmap = null;
                setShowImage(true);
                resetImgParam();
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
            resetImgParam();
        } else if ( what == HandleKeys.COPY_FILE_FAILURE ) {

            LogCat.d(TAG + "获取失败", (String)msg.obj);
        } else if ( what == HandleKeys.ENHANCE_CONTRAST_DONE ||
                    what == HandleKeys.ENHANCE_SATURATION_DONE ||
                    what == HandleKeys.ENHANCE_CLARITY_DONE ||
                    what == HandleKeys.ENHANCE_ADAPTIVE_DONE ) {

            isHandling = false;
            useResultImg = true;
            hideLoading();
            targetBitmap = (Bitmap) msg.obj;
            evaluatUtil.detectAllDimensionSync(targetBitmap, new EvaluatAllListener<Float>() {
                @Override
                public void success(ArrayList<Float> data) {

                    LogCat.d(TAG, "清晰度：" + data.get(0) + "，对比度：" + data.get(1) + "，饱和度：" + data.get(2));
                }

                @Override
                public void failure(String error) {

                }
            });
            loadImage();
            setBtnAction(useResultImg);
        } else if ( what == HandleKeys.SAVE_IMAGE_SUCCESS ) {

            Toast.makeText(mContext, getString(R.string.saved, (String)msg.obj), Toast.LENGTH_SHORT).show();
        } else if ( what == HandleKeys.SAVE_IMAGE_FAILURE ) {

            Toast.makeText(mContext, "保存失败：" + (String)msg.obj, Toast.LENGTH_LONG).show();
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
     * 复位图片参数
     */
    private void resetImgParam() {

        saturationValue = 50.0f;
        clarityValue    = 50.0f;
        contrastValue   = 50.0f;
        /**
         * 将调整的参数复原，不回调
         */
        mRglSaturation.setCurrentValue(saturationValue, false);
        mRglContrast.setCurrentValue(contrastValue, false);
        mRglClarity.setCurrentValue(clarityValue, false);
    }

    /**
     * 如果用户添加了图片，则显示图片，如果没有添加，则提示添加
     * @param hasImg
     */
    private void setShowImage(boolean hasImg) {

        if ( !hasImg ) {

            mTvAdaptive.setVisibility(View.GONE);
            mTvSave.setVisibility(View.GONE);
            mTvReduction.setVisibility(View.GONE);
            mImgSource.setVisibility(View.GONE);
            mLytController.setVisibility(View.GONE);
            mLytAddImgPrompt.setVisibility(View.VISIBLE);
        } else {

            mTvAdaptive.setVisibility(View.VISIBLE);
            mTvSave.setVisibility(View.VISIBLE);
            mTvReduction.setVisibility(View.VISIBLE);
            mImgSource.setVisibility(View.VISIBLE);
            mLytController.setVisibility(View.VISIBLE);
            mLytAddImgPrompt.setVisibility(View.GONE);
            loadImage();
        }
    }

    /**
     * 如果是刚处理完图片，则需要确认是否更改图片。
     * @param showSaveAndReduction 是否显示“保存图片和还原”
     */
    private void setBtnAction(boolean showSaveAndReduction) {

        if ( showSaveAndReduction ) {

            mTvSave.setText(getString(R.string.yes));
            mTvReduction.setText(getString(R.string.cancel));
        } else {

            mTvSave.setText(getString(R.string.save_image));
            mTvReduction.setText(getString(R.string.reduction));
        }
    }

    /**
     * 将图片显示到UI
     */
    private void loadImage() {

        GlideUtil.clearDiskCache(mContext);
        if ( targetBitmap == null ) {

            Glide.with(mImgSource.getContext())
                    .load(imgChoosePath)
                    .placeholder(android.R.color.darker_gray)
                    .into(mImgSource);
        } else {

            Glide.with(mImgSource.getContext())
                    .load(targetBitmap)
                    .placeholder(android.R.color.darker_gray)
                    .into(mImgSource);
        }
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
        mRglClarity.setCurrentValue(0.0f, false);

        mRglSaturation.setOnValueChangeListener(new RegulatorView.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {

                saturationValue = value;
                changeSaturation(saturationValue+30, false);
            }
        });
        mRglContrast.setOnValueChangeListener(new RegulatorView.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {

                contrastValue = value;
                enhanceContrast(contrastValue / 100.0f, false);
            }
        });
        mRglClarity.setOnValueChangeListener(new RegulatorView.OnValueChangeListener() {
            @Override
            public void onValueChange(float value) {

                clarityValue = value - 50.00f;
                enhanceClarity(clarityValue / 10.0f);
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
     * 增强饱和度处理
     * @param coefficient 增强系数
     * @param adaptive 是否自适应
     */
    private void changeSaturation(float coefficient, boolean adaptive) {

        Bitmap bitmap = getTargetBitmap();
        if ( bitmap != null ) {

            if ( adaptive ) {

                opencv.changeSaturationSync(bitmap, contrastListener);
            } else {

                opencv.changeSaturationSync(bitmap, coefficient, contrastListener);
            }
        }
    }

    /**
     * 增强对比图处理
     * @param coefficient 增强系数
     * @param adaptive 是否自适应
     */
    private void enhanceContrast(float coefficient, boolean adaptive) {

        Bitmap bitmap = getTargetBitmap();
        if ( bitmap != null ) {

            if ( adaptive ) {

                opencv.changeContrastSync(bitmap, contrastListener);
            } else {

                opencv.changeContrastSync(bitmap, coefficient, contrastListener);
            }
        }
    }

    /**
     * 增强图片清晰度
     */
    private void enhanceClarity(float coefficient) {

        Bitmap bitmap = getTargetBitmap();
        if ( bitmap != null ) {

            opencv.changeClaritySync(bitmap, coefficient, clarityListener);
        }
    }

    /**
     * 处理图片时调用本方法，本方法返回将要被处理的图片
     * @return Bitmap 将被处理的图片
     */
    private Bitmap getTargetBitmap() {

        if ( hasImage() ) {

            if ( isHandling ) {

                myToast(getString(R.string.handing));
                return null;
            }
            isHandling = true;
            showLoading();
            Bitmap bitmap;
            if ( targetBitmap != null ) {

                bitmap = targetBitmap;
            } else {

                bitmap = getFileToBitmap();
            }
            return bitmap;
        } else {

            myToast(getString(R.string.img_is_null));
            return null;
        }
    }

    private Bitmap getFileToBitmap() {

        if ( hasImage() ) {

            return BitmapFactory.decodeFile(imgChoosePath);
        } else {

            return null;
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
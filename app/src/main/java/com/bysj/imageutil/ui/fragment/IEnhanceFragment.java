package com.bysj.imageutil.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bysj.imageutil.R;
import com.bysj.imageutil.base.BaseFragment;
import com.bysj.imageutil.ui.activity.MainActivity;
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
import com.bysj.imageutil.util.ShareUtil;
import com.bysj.imgevaluation.EvaluatUtil;
import com.bysj.imgevaluation.bean.EvaluatBean;
import com.bysj.imgevaluation.listener.EvaluatAllListener;
import com.bysj.opencv450.HandleImageListener;
import com.bysj.opencv450.OpenCVUtil;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * 增强图片以及图片评价Fragment
 *
 * Create on 2021-2-27
 */

public class IEnhanceFragment extends BaseFragment implements View.OnClickListener {

    private static final String    TAG = "iEnhanceFragment";
    private static final String    IMG_SUFFIX = "_capture.jpg";
    /** 源图片控件 */
    private ImageView              mImgSource;
    /** 提示添加图片控件 */
    private LinearLayout           mLytAddImgPrompt;
    /** 调整图片控制面板 */
    private LinearLayout           mLytController;
    /** 清晰度调节器 */
    private RegulatorView          mRglClarity;
    /** 对比度调节器 */
    private RegulatorView          mRglContrast;
    /** 饱和度调节器 */
    private RegulatorView          mRglSaturation;
    /** 一键自适应 */
    private TextView               mTvAdaptive;
    /** 保存图片按钮 */
    private TextView               mTvSave;
    /** 分享图片 */
    private TextView               mTvShare;
    /** 还原图片 */
    private TextView               mTvReduction;
    /** 选取图片后图片副本的固定路径 */
    private String                 imgConstantPath     = null;
    /** 相机拍照后的图片文件名 */
    private String                 imgChoosePath       = null;
    /** 清晰度的当前值 */
    private float                  clarityValue        = 0.0f;
    /** 对比度的当前值 */
    private float                  contrastValue       = 0.0f;
    /** 饱和度的当前值 */
    private float                  saturationValue     = 0.0f;
    /** 是否正在处理图片 */
    private boolean                isHandling          = false;
    /** 如果处理完图片，则需要用户确认是否使用
     * 处理后的图片。如果不确认，调整图片时是在原图
     * 的基础上进行调整
     */
    private boolean                useResultImg        = false;
    /** 分享时需要保存图片，该标志用于判断保存的图片是不是被
     * 分享调用
     */
    private boolean                isShare             = false;
    /** OpenCV工具 */
    private OpenCVUtil             opencv;
    /** 评价图片质量工具 */
    private EvaluatUtil            evaluatUtil;
    /** 处理后的图片 */
    private Bitmap                 targetBitmap        = null;
    /** 确认使用处理后的图片 */
    private Bitmap                 useBitmap           = null;
    /** 存放评价后的图片参数列表 */
    private ArrayList<EvaluatBean> evaluatBeans;

    public IEnhanceFragment() {
        // Required empty public constructor
    }

    /**
     * 源图片改变，图片增强后通知承载本Fragment的Activity接口。
     */
    public interface ImageChangedListener {

        void refreshEvaluatFragment(ArrayList<EvaluatBean> evaluatBeans, boolean isSource);
        void startDetectImgParam();
    }

    ImageChangedListener mCallback;

    @Override
    public void onAttach(@NonNull Context context) {

        super.onAttach(context);

        try {

            mCallback = (ImageChangedListener)context;
        } catch (ClassCastException e) {

            throw new ClassCastException(context.toString() + " must implement ImageChandedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    private void initMyView(View view) {

        mImgSource       = view.findViewById(R.id.img_source);
        mLytAddImgPrompt = view.findViewById(R.id.lyt_add_image_prompt);
        mRglClarity      = view.findViewById(R.id.rgl_clarity);
        mRglContrast     = view.findViewById(R.id.rgl_contrast);
        mRglSaturation   = view.findViewById(R.id.rgl_saturation);
        mLytController   = view.findViewById(R.id.lyt_controller);
        mTvAdaptive      = view.findViewById(R.id.tv_adaptive);
        mTvSave          = view.findViewById(R.id.tv_save);
        mTvReduction     = view.findViewById(R.id.tv_reduction);
        mTvShare         = view.findViewById(R.id.tv_share);
/*
        mRglSaturation.setVisibility(View.GONE);
        mRglClarity.setVisibility(View.GONE);
        mRglContrast.setVisibility(View.GONE);
*/
        setShowImage(false);
        initRglRatio();
        setMyViewOnClick();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_i_enhance, container, false);
        initMyView(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        opencv = OpenCVUtil.getInstance();
        evaluatUtil = EvaluatUtil.getInstance(mContext);
        mDialog = new DialogPrompt(mContext);
        mLoading = new DialogLoading(mContext);
        imgConstantPath = mContext.getCacheDir().getPath() + "/";
    }

    @Override
    protected boolean isLazyLoad() {

        return true;
    }

    @Override
    protected int getLayoutResId() {

        return R.layout.fragment_i_enhance;
    }

    @Override
    protected void onHandleMessage(Message msg) {

        int what = msg.what;
        if ( what == HandleKeys.COPY_FILE_SUCCESS ) {

            targetBitmap = null;
            imgChoosePath = (String)msg.obj;
            LogCat.d(TAG + "获取成功", imgChoosePath);
            setShowImage(true);
            resetImgParam();
            getImageEva(getFileToBitmap(), true);
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
            getImageEva(targetBitmap, false);
            loadImage();
            setBtnAction(useResultImg);
            myToast("对比度增强功能尚未完成");
        } else if ( what == HandleKeys.SAVE_IMAGE_SUCCESS ) {

            if ( isShare ) {

                String filePath = (String)msg.obj;
                isShare = false;
                ShareUtil.share(mContext, filePath);/*
                File file = new File(filePath);
                if ( file.exists() ) {

                    file.delete();
                }*/
            } else {

                myToast(getString(R.string.saved, (String)msg.obj));
            }
        } else if ( what == HandleKeys.SAVE_IMAGE_FAILURE ) {

            isShare = false;
            myToast("保存失败：" + (String)msg.obj);
            //Toast.makeText(mContext, "保存失败：" + (String)msg.obj, Toast.LENGTH_LONG).show();
        } else if ( what == HandleKeys.GET_EVALUAT_SUCCESS ) {

            mCallback.refreshEvaluatFragment(evaluatBeans, (boolean)msg.obj);
        } else if ( what == HandleKeys.GET_EVALUAT_FAILURE ) {

            myToast((String)msg.obj);
        }
    }

    private void setMyViewOnClick() {

        mImgSource.setOnClickListener(this);
        mLytAddImgPrompt.setOnClickListener(this);
        mTvAdaptive.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
        mTvReduction.setOnClickListener(this);
        mTvShare.setOnClickListener(this);
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

                    saveImage();
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
            mCallback.startDetectImgParam();
            opencv.adaptiveEnhanceSync(bitmap, new HandleImageListener() {
                @Override
                public void done(Bitmap bitmap) {

                    sendMessage(HandleKeys.ENHANCE_ADAPTIVE_DONE, bitmap);
                }
            });
        } else if ( id == R.id.tv_share ) {

            isShare = true;
            saveImage();
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
                LogCat.d(TAG, path);
                if ( !TextUtils.isEmpty(path) ) {

                    final File file = new File(path);
                    FileUtils.copyFile(file.getPath(), imgConstantPath + System.currentTimeMillis() + IMG_SUFFIX, new FileUtils.CopyFileListener() {
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

                sendMessage(HandleKeys.COPY_FILE_SUCCESS, imgChoosePath);
            }
        }
    }

    private void saveImage() {

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
            mTvShare.setVisibility(View.GONE);
            mLytAddImgPrompt.setVisibility(View.VISIBLE);
        } else {

            mTvAdaptive.setVisibility(View.VISIBLE);
            mTvSave.setVisibility(View.VISIBLE);
            mTvReduction.setVisibility(View.VISIBLE);
            mImgSource.setVisibility(View.VISIBLE);
            mLytController.setVisibility(View.VISIBLE);
            mTvShare.setVisibility(View.VISIBLE);
            mLytAddImgPrompt.setVisibility(View.GONE);
            loadImage();
        }
        /*
        mRglSaturation.setVisibility(View.GONE);
        mRglClarity.setVisibility(View.GONE);
        mRglContrast.setVisibility(View.GONE);*/
    }

    /**
     * 计算图片质量
     * @param bitmap 要计算的图片
     * @param isSource 是否是原图
     */
    private void getImageEva(Bitmap bitmap, final boolean isSource) {

        if ( bitmap != null ) {

            evaluatUtil.detectAllSync(bitmap, new EvaluatAllListener<EvaluatBean>() {
                @Override
                public void success(ArrayList<EvaluatBean> data) {

                    evaluatBeans = data;
                    sendMessage(HandleKeys.GET_EVALUAT_SUCCESS, isSource);
                }

                @Override
                public void failure(String error) {

                    sendMessage(HandleKeys.GET_EVALUAT_FAILURE, error);
                }
            });
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
                /*
                contrastValue = value;
                enhanceContrast(contrastValue / 100.0f, false);
                 */
                myToast("对比度增强功能尚未完成");
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
                        imgChoosePath = imgConstantPath + System.currentTimeMillis() + IMG_SUFFIX;

                        File file = new File(imgChoosePath);

                        Uri uri = FileProvider.getUriForFile(mContext, "com.bysj.imageutil.fileprovider", file);
                        takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                        startActivityForResult(takePhotoIntent, IntentKeys.MAIN_TO_CAMERA);
                    }

                    @Override
                    public void cancel() {

                        imgChoosePath = imgConstantPath + System.currentTimeMillis() + IMG_SUFFIX;

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

                        LogCat.d(TAG + "删除文件", fileName);
                        if ( file.exists() ) {

                            file.delete();
                            LogCat.d(TAG, fileName);
                        }
                    }
                }
            }
        }).start();
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
}
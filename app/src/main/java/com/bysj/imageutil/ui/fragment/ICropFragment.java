package com.bysj.imageutil.ui.fragment;

import android.app.ActivityManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
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

import com.bumptech.glide.Glide;
import com.bysj.imageutil.R;
import com.bysj.imageutil.base.BaseFragment;
import com.bysj.imageutil.ui.activity.MainActivity;
import com.bysj.imageutil.ui.components.dialog.DialogLoading;
import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.ui.components.dialog.DialogPromptListener;
import com.bysj.imageutil.util.ActivityManageHelper;
import com.bysj.imageutil.util.FileUtils;
import com.bysj.imageutil.util.GetImgPath;
import com.bysj.imageutil.util.GlideUtil;
import com.bysj.imageutil.util.HandleKeys;
import com.bysj.imageutil.util.IntentKeys;
import com.bysj.imageutil.util.LogCat;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropActivity;

import java.io.File;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.HARDWARE_PROPERTIES_SERVICE;


public class ICropFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG             = "iCropFragment";
    private static final String IMG_SUFFIX      = "_crop.jpg";

    private UCrop.Options       ucrop;

    private ImageView           mImgSource;
    private ImageView           mImgCrop;
    private LinearLayout        mLytPrompt;
    private TextView            mTvSave;
    private TextView            mTvCrop;

    private String              imgChoosePath   = null;
    private String              imgConstantPath = null;
    private String              imgCropPath     = null;
    private File                imgFile         = null;
    private File                tempFile        = null;

    private Bitmap              mBmp            = null;
    private Uri                 sourceUri       = null;
    private boolean             isSaving        = false;

    public ICropFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        ucrop = new UCrop.Options();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crop, container, false);

        mImgSource      = view.findViewById(R.id.img_source);
        mImgCrop        = view.findViewById(R.id.img_crop_result);
        mLytPrompt      = view.findViewById(R.id.lyt_add_image_prompt);
        mTvCrop         = view.findViewById(R.id.tv_crop);
        mTvSave         = view.findViewById(R.id.tv_save);

        mImgSource.setOnClickListener(this);
        mLytPrompt.setOnClickListener(this);
        mTvCrop.setOnClickListener(this);
        mTvSave.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        mDialog  = new DialogPrompt(mContext);
        mLoading = new DialogLoading(mContext);

        imgConstantPath = mContext.getCacheDir().getPath() + "/";
    }

    @Override
    protected int getLayoutResId() {

        return R.layout.fragment_crop;
    }

    @Override
    protected void onHandleMessage(Message msg) {

        int what = msg.what;

        if ( what == HandleKeys.COPY_FILE_SUCCESS ) {

            //mTvSave.setVisibility(View.GONE);
            imgChoosePath = (String)msg.obj;
            LogCat.d(TAG + "获取成功", imgChoosePath);
            mBmp = BitmapFactory.decodeFile(imgChoosePath);
            imgFile = tempFile;
            sourceUri = Uri.fromFile(new File(imgChoosePath));
            isShowImg(true);
        } else if ( what == HandleKeys.COPY_FILE_FAILURE ) {

            LogCat.d(TAG + "获取失败", (String)msg.obj);
        } else if ( what == HandleKeys.SAVE_IMAGE_SUCCESS ) {

            hideLoading();
            isSaving = false;
            myToast(getString(R.string.saved, (String)msg.obj));
        } else if ( what == HandleKeys.SAVE_IMAGE_FAILURE || what == HandleKeys.SAVE_IMGS_FAILURE ) {

            myToast("保存失败：" + (String)msg.obj);
            hideLoading();
            isSaving = false;
        } else if ( what == HandleKeys.SAVE_IMGS_SUCCESS ) {


        } else if ( what == HandleKeys.CROP_IMG_SUCCESS ) {

            showCropImg();
        } else if ( what == HandleKeys.CROP_IMG_FAILURE ) {

            myToast("裁剪失败");
        }
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if ( id == R.id.lyt_add_image_prompt || id == R.id.img_source ) {

            chooseImg();
        } else if ( id == R.id.tv_save && !isSaving ) {

            saveImgs();
        } else if ( id == R.id.tv_crop ) {

            if ( sourceUri == null ) {

                myToast(getString(R.string.img_is_null));
                return;
            }
            startCropImage();
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
            } else if ( requestCode == UCrop.REQUEST_CROP ) {

                final Uri resultUri = UCrop.getOutput(data);
                if ( resultUri != null ) {

                    imgCropPath = resultUri.getPath();
                    sendEmptyMessage(HandleKeys.CROP_IMG_SUCCESS);
                } else {

                    imgCropPath = null;
                    sendEmptyMessage(HandleKeys.CROP_IMG_FAILURE);
                }
                LogCat.d(TAG + "裁剪结果", imgCropPath);
            }
        } else if ( resultCode == UCrop.RESULT_ERROR ) {

            imgCropPath = null;
            final Throwable cropError = UCrop.getError(data);
            assert cropError != null;
            LogCat.d(TAG + "裁剪失败", cropError.getMessage());
            sendEmptyMessage(HandleKeys.CROP_IMG_FAILURE);
        }
    }

    private void startCropImage() {

        Uri dst = Uri.fromFile(new File(mContext.getCacheDir(), System.currentTimeMillis() + "cp_result.jpg"));
        LogCat.d(TAG + "开始裁剪", sourceUri.getPath());
        UCrop uCrop = UCrop.of(sourceUri, dst);
        UCrop.Options options = new UCrop.Options();
        options.setAllowedGestures(UCropActivity.SCALE, UCropActivity.ROTATE, UCropActivity.ALL);
        options.setToolbarColor(ActivityCompat.getColor(mContext, R.color.red));
        options.setStatusBarColor(ActivityCompat.getColor(mContext, R.color.red));
        //options.setFreeStyleCropEnabled(true);
        uCrop.withOptions(options);
        uCrop.start(mContext, ICropFragment.this, UCrop.REQUEST_CROP);
    }

    private void saveImgs() {

        isSaving = true;
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

                        tempFile = new File(imgChoosePath);

                        sourceUri = FileProvider.getUriForFile(mContext, "com.bysj.imageutil.fileprovider", tempFile);
                        takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, sourceUri);
                        startActivityForResult(takePhotoIntent, IntentKeys.MAIN_TO_CAMERA);
                    }

                    @Override
                    public void cancel() {

                        imgChoosePath = imgConstantPath + System.currentTimeMillis() + IMG_SUFFIX;

                        tempFile = new File(imgChoosePath);
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
                            sourceUri = FileProvider.getUriForFile(mContext,
                                    "com.bysj.imageutil.fileprovider", tempFile);
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, sourceUri);
                            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        }
                        startActivityForResult(intent, IntentKeys.MAIN_TO_PHOTOS);
                    }
                });
    }

    private void isShowImg(boolean isShowImg) {

        if ( isShowImg ) {

            mImgSource.setVisibility(View.VISIBLE);
            mImgCrop.setVisibility(View.GONE);
            mTvSave.setVisibility(View.GONE);
            mLytPrompt.setVisibility(View.GONE);
            mTvCrop.setVisibility(View.VISIBLE);
            showImg();
        } else {

            //mImgSource.setVisibility(View.GONE);
            //mLytPrompt.setVisibility(View.VISIBLE);
            mImgCrop.setVisibility(View.VISIBLE);
            mTvSave.setVisibility(View.VISIBLE);
            mTvCrop.setVisibility(View.GONE);
        }
    }

    private void showImg() {

        GlideUtil.clearDiskCache(mContext);
        Glide.with(mImgSource.getContext())
                .load(imgChoosePath)
                .placeholder(android.R.color.darker_gray)
                .into(mImgSource);
        clearCacheCapture(imgChoosePath, ".jpg", "crop");
    }

    private void showCropImg() {

        mImgCrop.setVisibility(View.VISIBLE);
        mTvSave.setVisibility(View.VISIBLE);
        GlideUtil.clearDiskCache(mContext);
        Glide.with(mImgCrop.getContext())
                .load(imgCropPath)
                .placeholder(android.R.color.darker_gray)
                .into(mImgCrop);
        clearCacheCapture(imgCropPath, ".jpg", "cp_result");
    }

    /**
     * 清除相机拍照后产生的缓存图片
     * @param saveFileName 保留的图片
     */
    private void clearCacheCapture(String saveFileName, String saveFormat, String saveContains) {

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
                    if ( fileName.contains(saveFormat) && fileName.contains(saveContains) && !fileName.equals(saveFileName)) {

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
}
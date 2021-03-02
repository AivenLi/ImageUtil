package com.bysj.imageutil.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bysj.imageutil.R;
import com.bysj.imageutil.adapter.ICropResAdapter;
import com.bysj.imageutil.adapter.ICropSetAdapter;
import com.bysj.imageutil.adapter.listener.ICropOnClickListener;
import com.bysj.imageutil.base.BaseFragment;
import com.bysj.imageutil.bean.ICropResBean;
import com.bysj.imageutil.bean.ICropSetBean;
import com.bysj.imageutil.ui.components.dialog.DialogLoading;
import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.ui.components.dialog.DialogPromptListener;
import com.bysj.imageutil.util.FileUtils;
import com.bysj.imageutil.util.GetImgPath;
import com.bysj.imageutil.util.GlideUtil;
import com.bysj.imageutil.util.HandleKeys;
import com.bysj.imageutil.util.ImageCropUtil;
import com.bysj.imageutil.util.IntentKeys;
import com.bysj.imageutil.util.LogCat;
import com.bysj.opencv450.OpenCVUtil;

import java.io.File;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;

/**
 * 图片拼接啥的Fragment
 *
 * Create on 2021-2-27
 */

public class IEditFragment extends BaseFragment implements View.OnClickListener,
        ICropOnClickListener {

    private static final String TAG = "iEditFragment";
    private static final String IMG_SUFFIX = "_crop.jpg";

    private ImageView mImgSource;
    private LinearLayout mLytPrompt;
    private TextView mTvSave;

    private String imgChoosePath = null;
    private String imgConstantPath = null;

    private RecyclerView icSetRecycler;
    private RecyclerView icResRecycler;

    private ICropSetAdapter iCropSetAdapter;
    private ICropResAdapter iCropResAdapter;

    private ArrayList<ICropResBean> iCropResBeans = new ArrayList<>();
    private ArrayList<ICropSetBean> iCropSetBeans = new ArrayList<>();

    private int screenWidth;
    private int screenHeight;

    private Bitmap mBmp = null;

    private boolean isSaving;

    public IEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public void initData() {

        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_2), 2));
        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_3), 3));
        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_4), 4));
        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_6), 6));
        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_8), 8));
        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_9), 9));
        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_12), 12));
        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_15), 15));
        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_16), 16));

        isSaving = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_i_edit, container, false);

        mImgSource      = view.findViewById(R.id.img_source);
        mLytPrompt      = view.findViewById(R.id.lyt_add_image_prompt);
        mTvSave         = view.findViewById(R.id.tv_save);

        icResRecycler   = view.findViewById(R.id.rlv_crop_result);
        icSetRecycler   = view.findViewById(R.id.rlv_crop_sheets);

        iCropResAdapter = new ICropResAdapter(iCropResBeans);
        iCropSetAdapter = new ICropSetAdapter(iCropSetBeans, this);

        mTvSave.setOnClickListener(this);
        mImgSource.setOnClickListener(this);
        mLytPrompt.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        icSetRecycler.setLayoutManager(new GridLayoutManager(mContext, 3));
        icSetRecycler.setAdapter(iCropSetAdapter);
        mDialog         = new DialogPrompt(mContext);
        mLoading        = new DialogLoading(mContext);
        imgConstantPath = mContext.getCacheDir().getPath() + "/";
        int[] screenWH  = getPhoneScreenInfo();
        screenWidth     = screenWH[0];
        screenHeight    = screenWH[1];
        isShowImg(false);
    }

    @Override
    protected int getLayoutResId() {

        return R.layout.fragment_i_edit;
    }

    @Override
    protected void onHandleMessage(Message msg) {

        int what = msg.what;

        if ( what == HandleKeys.COPY_FILE_SUCCESS ) {

            icResRecycler.setVisibility(View.GONE);
            mTvSave.setVisibility(View.GONE);
            iCropResBeans.clear();
            imgChoosePath = (String)msg.obj;
            LogCat.d(TAG + "获取成功", imgChoosePath);
            mBmp = BitmapFactory.decodeFile(imgChoosePath);
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

            mLoading.setText(getString(R.string.saving_imgs_prompt, (int)msg.obj, iCropResBeans.size()));
        }
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if ( id == R.id.lyt_add_image_prompt || id == R.id.img_source ) {

            chooseImg();
        } else if ( id == R.id.tv_save && !isSaving ) {

            saveImgs();
        }
    }

    @Override
    public void onItemClick(int i) {

        LogCat.d(TAG, "点击回调");
        /**
         * 如果程序正在保存图片，则不能对其他图片进行裁剪，否则
         * 正在保存的图片可能会丢失或者出现崩溃的情况。
         */
        if ( !isSaving ) {

            ArrayList<ICropResBean> tempList = ImageCropUtil.crop(mBmp, iCropSetBeans.get(i).getNum());
            /**
             * 裁剪图片返回后要检查返回结果是否为空，避免空指针异常。
             */
            if ( tempList != null && tempList.size() != 0 ) {

                iCropResBeans.clear();
                iCropResBeans.addAll(tempList);
                ICropResBean iCropResBean = iCropResBeans.get(i);
                /**
                 * 根据裁剪后图片的大小计算控件的宽高。
                 * 如果一行只显示一张图片，则不需要计算了。
                 */
                int imgWidth    = iCropResBean.getWidth();
                int imgHeight   = iCropResBean.getHeight();
                /**
                 * 获取要显示的列数，在裁剪图片后计算得出。
                 */
                int span        = ImageCropUtil.getSpan();
                /**
                 * 计算一张图要占多用屏幕宽度的大小
                 */
                int scrWAverage = screenWidth / span;
                /**
                 * 根据图片显示宽度按比例缩放高度。
                 */
                float ratio     = (float) scrWAverage / (float) imgWidth;
                imgHeight       = (int) (imgHeight * ratio);
                imgWidth        = scrWAverage;
                icResRecycler.setLayoutManager(new GridLayoutManager(mContext, span));
                iCropResAdapter = new ICropResAdapter(iCropResBeans, imgWidth, imgHeight);
                icResRecycler.setAdapter(iCropResAdapter);
                iCropResAdapter.notifyDataSetChanged();
                icResRecycler.setVisibility(View.VISIBLE);
                mTvSave.setVisibility(View.VISIBLE);
            } else {

                myToast(getString(R.string.param_error));
            }
        } else {

            myToast(getString(R.string.saving_image));
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
            }
        }
    }

    private void isShowImg(boolean isShowImg) {

        if ( isShowImg ) {

            mImgSource.setVisibility(View.VISIBLE);
            mLytPrompt.setVisibility(View.GONE);
            icSetRecycler.setVisibility(View.VISIBLE);
            showImg();
        } else {

            mImgSource.setVisibility(View.GONE);
            mLytPrompt.setVisibility(View.VISIBLE);
            icSetRecycler.setVisibility(View.GONE);
        }
    }

    private void showImg() {

        GlideUtil.clearDiskCache(mContext);
        Glide.with(mImgSource.getContext())
                .load(imgChoosePath)
                .placeholder(android.R.color.darker_gray)
                .into(mImgSource);
        clearCacheCapture(imgChoosePath);
    }

    /**
     * 保存图片到图库
     */
    private void saveImgs() {
        /**
         * 图片不能为空
         */
        if ( iCropResBeans.size() == 0 ) {

            myToast(getString(R.string.img_is_null));
            return;
        }
        /**
         * 如果已经有保存任务了，则需要等待。
         */
        if ( isSaving ) {

            myToast(getString(R.string.saving_image));
            return;
        }
        /**
         * 开始执行保存操作
         */
        isSaving = true;
        mLoading.setText(getString(R.string.saving_imgs_prompt, 1, iCropResBeans.size())).show();
        final int imgSize = iCropResBeans.size();
        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    for ( int i = imgSize-1; i >= 0; --i ) {

                        final int ii = i+1;
                        String fileName = "crop_" + System.currentTimeMillis() + "_" + ii + ".png";
                        ICropResBean itemImg = iCropResBeans.get(i);
                        FileUtils.saveImageToGallery(mContext, itemImg.getImage(), fileName, new FileUtils.SaveImageListener() {
                            @Override
                            public void success(String filePath) {

                                if ( ii != 1 ) {

                                    sendMessage(HandleKeys.SAVE_IMGS_SUCCESS, imgSize-ii+1);
                                } else {

                                    sendMessage(HandleKeys.SAVE_IMAGE_SUCCESS, filePath);
                                }
                            }

                            @Override
                            public void failure(String error) {

                                sendMessage(HandleKeys.SAVE_IMAGE_FAILURE, error);
                            }
                        });
                       Thread.sleep(100);
                    }
                } catch (InterruptedException e) {

                    e.printStackTrace();
                    LogCat.d(TAG, e.toString());
                }
            }
        }).start();
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
                    if ( fileName.contains(".jpg") && fileName.contains("crop") && !fileName.equals(saveFileName)) {

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
     * 获取手机屏幕信息。
     *
     * @return 返回一个大小为2的int数组，分别代表手机屏幕宽高的大小（dp）
     */
    public int[] getPhoneScreenInfo() {

        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return new int[]{ dm.widthPixels, dm.heightPixels};
        //int width = dm.widthPixels;
        //int height = dm.heightPixels;
        //float density = dm.density;
        /**
         * 像素除以密度得dp
         *
        int screenWidth = (int) (width / density);
        int screenHeight = (int) (height / density);
        LogCat.d(TAG, "屏幕宽度（像素）：" + width);
        LogCat.d(TAG, "屏幕高度（像素）：" + height);
        LogCat.d(TAG, "屏幕密度（0.75 / 1.0 / 1.5）：" + density);
        LogCat.d(TAG, "屏幕密度dpi（120 / 160 / 240）：" + densityDpi);
        LogCat.d(TAG, "屏幕宽度（dp）：" + screenWidth);
        LogCat.d(TAG, "屏幕高度（dp）：" + screenHeight);
        */
    }
}
package com.bysj.imageutil.ui.fragment;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bysj.imageutil.R;
import com.bysj.imageutil.adapter.ICropResAdapter;
import com.bysj.imageutil.adapter.ICropSetAdapter;
import com.bysj.imageutil.adapter.listener.ICropOnClickListener;
import com.bysj.imageutil.base.BaseFragment;
import com.bysj.imageutil.bean.ICropResBean;
import com.bysj.imageutil.bean.ICropSetBean;
import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.ui.components.dialog.DialogPromptListener;
import com.bysj.imageutil.util.FileUtils;
import com.bysj.imageutil.util.GetImgPath;
import com.bysj.imageutil.util.GlideUtil;
import com.bysj.imageutil.util.HandleKeys;
import com.bysj.imageutil.util.IntentKeys;
import com.bysj.imageutil.util.LogCat;

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

    private String imgChoosePath = null;
    private String imgConstantPath = null;

    private RecyclerView icSetRecycler;
    private RecyclerView icResRecycler;

    private ICropSetAdapter iCropSetAdapter;
    private ICropResAdapter iCropResAdapter;

    private ArrayList<ICropResBean> iCropResBeans = new ArrayList<>();
    private ArrayList<ICropSetBean> iCropSetBeans = new ArrayList<>();

    private Bitmap mBmp = null;

    public IEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public void initData() {

        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_4)));
        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_6)));
        iCropSetBeans.add(new ICropSetBean(getString(R.string.img_crop_9)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_i_edit, container, false);

        mImgSource = view.findViewById(R.id.img_source);
        mLytPrompt = view.findViewById(R.id.lyt_add_image_prompt);

        icResRecycler = view.findViewById(R.id.rlv_crop_result);
        icSetRecycler = view.findViewById(R.id.rlv_crop_sheets);

        iCropResAdapter = new ICropResAdapter(iCropResBeans);
        iCropSetAdapter = new ICropSetAdapter(iCropSetBeans);

        mImgSource.setOnClickListener(this);
        mLytPrompt.setOnClickListener(this);


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        icSetRecycler.setLayoutManager(new GridLayoutManager(mContext, 3));
        icSetRecycler.setAdapter(iCropSetAdapter);
        //icResRecycler.setLayoutManager(new GridLayoutManager(mContext, 3));

        mDialog = new DialogPrompt(mContext);
        imgConstantPath = mContext.getCacheDir().getPath() + "/";
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

            imgChoosePath = (String)msg.obj;
            LogCat.d(TAG + "获取成功", imgChoosePath);
            mBmp = BitmapFactory.decodeFile(imgChoosePath);
            isShowImg(true);
        } else if ( what == HandleKeys.COPY_FILE_FAILURE ) {

            LogCat.d(TAG + "获取失败", (String)msg.obj);
        }
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();

        if ( id == R.id.lyt_add_image_prompt || id == R.id.img_source ) {

            chooseImg();
        }
    }

    @Override
    public void onItemClick(int i) {


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
}
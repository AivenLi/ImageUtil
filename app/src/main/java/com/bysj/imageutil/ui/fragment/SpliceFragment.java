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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bysj.imageutil.R;
import com.bysj.imageutil.adapter.drag.GapGridDecoration;
import com.bysj.imageutil.adapter.drag.RecyclerItem;
import com.bysj.imageutil.adapter.drag.SimpleRecyclerGridAdapter;
import com.bysj.imageutil.adapter.drag.darghelpercallback.GridSortHelperCallBack;
import com.bysj.imageutil.adapter.drag.darghelpercallback.VerticalDragSortHelperCallBack;
import com.bysj.imageutil.base.BaseFragment;
import com.bysj.imageutil.bean.ICropResBean;
import com.bysj.imageutil.ui.components.dialog.DialogLoading;
import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.ui.components.dialog.DialogPromptListener;
import com.bysj.imageutil.util.FileUtils;
import com.bysj.imageutil.util.GetImgPath;
import com.bysj.imageutil.util.HandleKeys;
import com.bysj.imageutil.util.IntentKeys;
import com.bysj.imageutil.util.LogCat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class SpliceFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "spliceFragment";

    private TextView mTvAddImg;
    private RecyclerView recyclerView;

    private List<RecyclerItem> list = new ArrayList<>();
    private ItemTouchHelper itemTouchHelper;
    private SimpleRecyclerGridAdapter adapter;

    private String imgChoosePath = null;
    private String imgConstantPath = null;
    private static final String IMG_SUFFIX = "splice.jpg";

    public SpliceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    private void myInitView() {


        recyclerView.setNestedScrollingEnabled(false);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splice, container, false);

        recyclerView = view.findViewById(R.id.ryv_item);
        mTvAddImg = view.findViewById(R.id.tv_add_img);
        mTvAddImg.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        imgConstantPath = mContext.getCacheDir().getPath() + "/";
        myInitView();
        mDialog = new DialogPrompt(mContext);
        mLoading = new DialogLoading(mContext);
        initEvents(savedInstanceState);
    }

    private void initEvents(Bundle savedInstanceState) {

        adapter = new SimpleRecyclerGridAdapter(list);
        recyclerView.setAdapter(adapter);
        int span = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, span));
        recyclerView.addItemDecoration(new GapGridDecoration(span));
        GridSortHelperCallBack callBack = new GridSortHelperCallBack(list);
        callBack.setOnDragListener(new VerticalDragSortHelperCallBack.OnDragListener() {
            @Override
            public void onItemMoved(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target, int fromPos, int toPos) {

                myToast("位置发生了改变：" + fromPos + "---->" + toPos);
            }
        });
        itemTouchHelper = new ItemTouchHelper(callBack);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected int getLayoutResId() {

        return R.layout.fragment_splice;
    }

    @Override
    protected void onHandleMessage(Message msg) {

        int what = msg.what;

        if ( what == HandleKeys.COPY_FILE_SUCCESS ) {

            imgChoosePath = (String)msg.obj;
            LogCat.d(TAG + "获取成功", imgChoosePath);
            Bitmap mBmp = BitmapFactory.decodeFile(imgChoosePath);
            list.add(new RecyclerItem(mBmp, "1"));
            adapter.notifyDataSetChanged();
        } else if ( what == HandleKeys.COPY_FILE_FAILURE ) {

            LogCat.d(TAG + "获取失败", (String)msg.obj);
        } else if ( what == HandleKeys.SAVE_IMAGE_SUCCESS ) {

            hideLoading();
            myToast(getString(R.string.saved, (String)msg.obj));
        } else if ( what == HandleKeys.SAVE_IMAGE_FAILURE ) {

            myToast("保存失败：" + (String)msg.obj);
            hideLoading();
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



    @Override
    public void onClick(View view) {

        int id = view.getId();

        if ( id == R.id.tv_add_img ) {

            chooseImg();
        }
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
}
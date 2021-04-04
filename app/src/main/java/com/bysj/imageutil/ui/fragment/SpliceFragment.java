package com.bysj.imageutil.ui.fragment;

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
import com.bysj.imageutil.adapter.drag.SimpleRecyclerGridAdapter;
import com.bysj.imageutil.adapter.drag.SimpleRecyclerListAdapter;
import com.bysj.imageutil.adapter.drag.SimpleRecyclerViewHolder;
import com.bysj.imageutil.adapter.drag.darghelpercallback.GridSortHelperCallBack;
import com.bysj.imageutil.adapter.drag.darghelpercallback.VerticalDragSortHelperCallBack;
import com.bysj.imageutil.base.BaseFragment;
import com.bysj.imageutil.bean.ICropResBean;
import com.bysj.imageutil.bean.SpliceBean;
import com.bysj.imageutil.ui.activity.SpliceResultActivity;
import com.bysj.imageutil.ui.components.dialog.DialogLoading;
import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.ui.components.dialog.DialogPromptListener;
import com.bysj.imageutil.util.ClickUtil;
import com.bysj.imageutil.util.FileUtils;
import com.bysj.imageutil.util.GetImgPath;
import com.bysj.imageutil.util.HandleKeys;
import com.bysj.imageutil.util.ImageSpliceUtil;
import com.bysj.imageutil.util.IntentKeys;
import com.bysj.imageutil.util.LogCat;
import com.bysj.imageutil.util.ShareUtil;
import com.bysj.imageutil.util.matrix.Graph;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class SpliceFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "spliceFragment";

    private TextView mTvSplice;
    private RecyclerView recyclerView;

    private List<SpliceBean> list = new ArrayList<>();
    private ItemTouchHelper itemTouchHelper;
    private SimpleRecyclerGridAdapter adapter;

    private String imgChoosePath = null;
    private String imgConstantPath = null;
    private static final String IMG_SUFFIX = "splice.jpg";
    private int currenItemIndex;
    private boolean isSplice = false;
    private int canNotMoveIndex = 2;

    public SpliceFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    protected boolean isLazyLoad() {

        return true;
    }

    @Override
    protected void initData() {

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                list.addAll(getRecycleViewEmptyItem());
                sendEmptyMessage(HandleKeys.INIT_DATA_FINISHED);
            }
        }, 100);
    }

    private void myInitView() {

        recyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_splice, container, false);

        recyclerView = view.findViewById(R.id.ryv_item);
        mTvSplice    = view.findViewById(R.id.tv_splice);
        mTvSplice.setOnClickListener(this);
        //mTvAddImg = view.findViewById(R.id.tv_add_img);
//        mTvAddImg.setOnClickListener(this);

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
        int span = 2;
        recyclerView.setLayoutManager(new GridLayoutManager(mContext, span));
        recyclerView.addItemDecoration(new GapGridDecoration(span));
        GridSortHelperCallBack callBack = new GridSortHelperCallBack(list);
        callBack.setOnDragListener(new VerticalDragSortHelperCallBack.OnDragListener() {
            @Override
            public void onItemMoved(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target, int fromPos, int toPos) {

                //myToast("位置发生了改变：" + fromPos + "---->" + toPos);
                if ( fromPos == canNotMoveIndex ) {

                    canNotMoveIndex = toPos;
                } else if ( toPos == canNotMoveIndex ) {

                    canNotMoveIndex = fromPos;
                }
            }
        });
        adapter.setOnItemCLickListener(new SimpleRecyclerListAdapter.OnItemCLickListener() {
            @Override
            public void onItemClick(SpliceBean recyclerItem, SimpleRecyclerViewHolder holder, int position) {

                if ( position != canNotMoveIndex ) {

                    currenItemIndex = position;
                    chooseImg();
                }
            }
        });
        adapter.setOnItemRemoveClickListener(new SimpleRecyclerListAdapter.OnItemRemoveClickListener() {
            @Override
            public void onItemRemoveClick(int position) {

                if ( position != canNotMoveIndex ) {

                    LogCat.d(TAG, "清除图片");
                    SpliceBean item = list.get(position);
                    deleteFile(item.getName());
                    item.setImage(getAddImageIcon());
                    item.setHasImage(false);
                    item.setName(null);
                    adapter.notifyDataSetChanged();
                }
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

        hideLoading();
        if ( what == HandleKeys.COPY_FILE_SUCCESS ) {

            imgChoosePath = (String)msg.obj;
            LogCat.d(TAG + "获取成功", imgChoosePath);
            Bitmap mBmp = BitmapFactory.decodeFile(imgChoosePath);
            SpliceBean item = list.get(currenItemIndex);
            item.setImage(mBmp);
            item.setName(imgChoosePath);
            adapter.notifyDataSetChanged();
        } else if ( what == HandleKeys.COPY_FILE_FAILURE ) {

            LogCat.d(TAG + "获取失败", (String)msg.obj);
        } else if ( what == HandleKeys.SAVE_IMAGE_SUCCESS ) {

            myToast(getString(R.string.saved, (String)msg.obj));
        } else if ( what == HandleKeys.SAVE_IMAGE_FAILURE ) {

            myToast("保存失败：" + (String)msg.obj);
        } else if ( what == HandleKeys.SPLICE_ONLY_ONE ) {

            isSplice = false;
            showDialog(getString(R.string.dia_error), getString(R.string.img_splice_error, getString(R.string.img_splice_error_only_one)),
                    getString(R.string.yes), getString(R.string.cancel));
        } else if ( what == HandleKeys.SPLICE_NOT_RECTANGLE ) {

            isSplice = false;
            showDialog(getString(R.string.dia_error), getString(R.string.img_splice_error, getString(R.string.img_splice_error_not_rectangle)),
                    getString(R.string.yes), getString(R.string.cancel));
        } else if ( what == HandleKeys.SPLICE_SUCCESS ) {

            // TODO 合成图片成功
            isSplice = false;
            File file = (File)msg.obj;
            Uri uri = Uri.fromFile(file);
            jumpOtherPage(SpliceResultActivity.class, uri);
        } else if ( what == HandleKeys.INIT_DATA_FINISHED ) {

            adapter.notifyDataSetChanged();
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
        /*
        if ( id == R.id.tv_add_img ) {

            chooseImg();
        }
        */
        if ( id == R.id.tv_splice && !ClickUtil.isFastDoubleClick() ) {

            int count = 0;
            for ( int i = 0; i < 3; ++i ) {

                if ( list.get(i).getHasImage() ) {

                    count++;
                }
            }
            LogCat.d(TAG, count);
            if ( count <= 1 ) {

                sendEmptyMessage(HandleKeys.SPLICE_ONLY_ONE);
                return;
            }
            spliceImagesSync();
        }
    }

    /**
     * 合成图片，合成图片是一个漫长的过程，使用线程进行处理避免主线程阻塞。
     */
    private void spliceImagesSync() {

        if ( isSplice ) {

            myToast(getString(R.string.img_splice_ing));
            return;
        }

        Bitmap bitmap1;
        Bitmap bitmap2;
        boolean line;
        if ( canNotMoveIndex == 0 ) {

            sendEmptyMessage(HandleKeys.SPLICE_NOT_RECTANGLE);
            return;
        } else if ( canNotMoveIndex == 1 ) {

            bitmap1 = list.get(0).getImage();
            bitmap2 = list.get(2).getImage();
            line = false;
        } else {

            bitmap1 = list.get(0).getImage();
            bitmap2 = list.get(1).getImage();
            line = true;
        }

        LogCat.d(TAG, "拼接" + ": " + canNotMoveIndex);
        isSplice = true;
        showLoading(getString(R.string.img_splice_action));

        new Thread(new Runnable() {
            @Override
            public void run() {

                Bitmap bitmap = ImageSpliceUtil.spliceImage(bitmap1, bitmap2, line);
                File file = FileUtils.bitmapToFile(bitmap, imgConstantPath + "splice_result.jpg");
                if ( file == null ) {

                    LogCat.d(TAG, "转换文件失败");
                } else {

                    sendMessage(HandleKeys.SPLICE_SUCCESS, file);
                }
            }
        }).start();
    }

    private List<SpliceBean> getRecycleViewEmptyItem() {

        List<SpliceBean> list = new ArrayList<>();

        for ( int i = 0; i < 3; ++i ) {

            list.add(getSpliceEmptyBean());
        }
        list.get(2).setImage(null);
        return list;
    }

    private SpliceBean getSpliceEmptyBean() {

        Bitmap bitmap = getAddImageIcon();
        return new SpliceBean(bitmap, null,false);
    }

    private Bitmap getAddImageIcon() {

        int redId = getResources().getIdentifier("add_icon", "mipmap", "com.bysj.imageutil");
        return BitmapFactory.decodeResource(getResources(), redId);
    }

    /**
     * 选择图片
     */
    private void chooseImg() {

        mDialog.setTitle(getString(R.string.dia_choose_img_title))
                .setContent("")
                .setYesText(getString(R.string.dia_choose_img_camera))
                .setCancelText(getString(R.string.dia_choose_img_photos))
                //.setYesCancelVisibility(View.VISIBLE)
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
     * 删除文件
     * @param fileName 文件名
     */
    private void deleteFile(String fileName) {

        if ( TextUtils.isEmpty(fileName) ) {

            return;
        }
        LogCat.d(TAG, "开始删除: " + fileName);
        File file = new File(fileName);
        if ( file.exists() ) {

            boolean result = file.delete();
            if ( result ) {

                LogCat.d(TAG, "删除成功");
            } else {

                LogCat.d(TAG, "删除失败");
            }
        } else {

            LogCat.d(TAG, "文件不存在");
        }
    }

    /**
     * 清除相机拍照后产生的缓存图片
     * @param saveFileName 保留的图片
     * @param contains 删除的图片必须包含某些特定的字符
     * @param format 图片格式
     */
    private void clearCacheCapture(String saveFileName, String format, String contains) {

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
                    if ( fileName.contains(format) && fileName.contains(contains) && !fileName.equals(saveFileName)) {

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
package com.bysj.imageutil.ui.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bysj.imageutil.R;
import com.bysj.imageutil.adapter.IEnAdapter;
import com.bysj.imageutil.base.BaseFragment;
import com.bysj.imageutil.ui.activity.MainActivity;
import com.bysj.imageutil.ui.components.MyListView;
import com.bysj.imageutil.util.ActivityManageHelper;
import com.bysj.imageutil.util.FileUtils;
import com.bysj.imageutil.util.HandleKeys;
import com.bysj.imageutil.util.LogCat;
import com.bysj.imgevaluation.bean.EvaluatBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cc.shinichi.library.ImagePreview;
import cc.shinichi.library.view.listener.OnDownloadClickListener;

/**
 * 评图Fragment
 *
 * Create on 2021-2-27
 */

public class IEvaluatFragment extends BaseFragment implements View.OnClickListener {
    /** 本页面标签，调试使用 */
    private static final String    TAG          = "evaluatFragment";
    /** 选择的图片 */
    private ImageView              mImgSource;
    /** 原图控件 */
    private TextView               mTvParam1;
    private TextView               mTvParam2;
    private TextView               mTvParam3;
    /** 原图参数列表 */
    private ArrayList<EvaluatBean> mListSource  = new ArrayList<>();
    /** 增强图片参数列表 */
    private ArrayList<EvaluatBean> mListEnhance = new ArrayList<>();
    /** 点击查看图片 */
    private List<String>           uriList      = new ArrayList<>();
    /** 原图 */
    private Bitmap                 sourceBmp    = null;
    /** 显示增强图参数列表的适配器 */
    private IEnAdapter             mEAdapter;
    private ListView               mListViewE;
    /** 加载框 */
    private LinearLayout           mLytHandle;
    /** 缓存路径 */
    private String                 cachePath;
    /** 评分图片 */
    private boolean                hasImage;

    /**
     * 本页面是否创建完成。
     * 该标志位很重要，当收到来自另一个页面的消息时，
     * 需要判断本页面是否创建完成，必须创建完成才能显示
     * 数据（图片等）。
     */
    private boolean                onCreated   = false;
    /** 标记原图参数列表的状态：展开/折叠 */

    public IEvaluatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public void initData() {
        /*
        new Thread(new Runnable() {
            @Override
            public void run() {


            }
        }).start();*/
    }

    @Override
    public boolean isLazyLoad() {

        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /**
         * Inflate the layout for this fragment
         */
        View view = inflater.inflate(R.layout.fragment_evaluat, container, false);
        /**
         * 初始化控件
         */
        mImgSource = view.findViewById(R.id.img_source);
        mTvParam1  = view.findViewById(R.id.tv_param1);
        mTvParam2  = view.findViewById(R.id.tv_param2);
        mTvParam3  = view.findViewById(R.id.tv_param3);
        mListViewE = view.findViewById(R.id.list_enhance);
        mLytHandle = view.findViewById(R.id.lyt_handle);
        /**
         * 初始化适配器数据
         */
        mEAdapter   = new IEnAdapter(mListEnhance);
        mListViewE.setAdapter(mEAdapter);
       // mListViewE.setNestedScrollingEnabled(true);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        onCreated = true;
        refreshImg();
        /**
         * 设置点击事件
         */
        mListViewE.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if ( hasImage ) {

                    MainActivity activity = (MainActivity)ActivityManageHelper.getInstance().currentActivity();
                    ImagePreview.getInstance()
                            .setContext(activity.getContext())
                            .setIndex(i)
                            .setImageList(uriList)
                            .setShowDownButton(true)
                            .setShowCloseButton(false)
                            .setEnableDragClose(true)
                            .setEnableClickClose(true)
                            .setDownloadClickListener(new OnDownloadClickListener() {

                                @Override
                                public void onClick(Activity activity1, View view1, int position) {

                                    FileUtils.saveImageToGallery(activity.getContext(), mListEnhance.get(position).getNewBitmap(), new FileUtils.SaveImageListener() {
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

                                @Override
                                public boolean isInterceptDownload() {

                                    return true;
                                }
                            })
                            .start();
                }
            }
        });
        cachePath = mContext.getCacheDir().getPath() + "/";
    }

    @Override
    protected int getLayoutResId() {

        return R.layout.fragment_evaluat;
    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
    }

    @Override
    protected void onHandleMessage(Message msg) {

        int what = msg.what;

        if ( what == HandleKeys.COPY_FILE_SUCCESS ) {

            hasImage = true;
        } else if ( what == HandleKeys.SAVE_IMAGE_SUCCESS ) {

            myToast(getString(R.string.saved, (String)msg.obj));
        } else if ( what == HandleKeys.SAVE_IMAGE_FAILURE ) {

            myToast(getString(R.string.toast_save_failed));
        }
    }

    /**
     * 在增强图片的Fragment中一旦有图发生变化，本Fragment
     * 就更新要显示的图片
     */
    private void refreshImg() {

        if ( onCreated ) {

            boolean showParam = !(sourceBmp == null);
            if ( showParam ) {

                mImgSource.setImageBitmap(sourceBmp);
                String p1 = getDimensionInfo(mListSource.get(0));
                String p2 = getDimensionInfo(mListSource.get(1));
                String p3 = getDimensionInfo(mListSource.get(2));
                mTvParam1.setText(p1);
                mTvParam2.setText(p2);
                mTvParam3.setText(p3);
            }
            setImageParamVisibility(showParam);
            mEAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 获取图片各个维度数据
     * @param evaluatBean
     * @return String，维度名称+维度值
     */
    private String getDimensionInfo(EvaluatBean evaluatBean) {

        return evaluatBean.getDimension() + ": " + evaluatBean.getValue();
    }

    /**
     * 设置原图参数是否显示
     * @param show 是否显示
     */
    private void setImageParamVisibility(boolean show) {

        if ( show ) {

            mTvParam1.setVisibility(View.VISIBLE);
            mTvParam2.setVisibility(View.VISIBLE);
            mTvParam3.setVisibility(View.VISIBLE);
        } else {

            mTvParam1.setVisibility(View.GONE);
            mTvParam2.setVisibility(View.GONE);
            mTvParam3.setVisibility(View.GONE);
        }
    }

    /**
     * 对外暴露，用于Fragment之间的通信，接收一组带有各个维度数据的图片。
     * @param imgList 图片列表
     * @param isSource true：原图参数， false：增强后的图片及参数
     */
    public void imgChanged(ArrayList<EvaluatBean> imgList, boolean isSource) {

        if ( imgList != null && imgList.size() != 0 ) {

            if ( isSource ) {

                sourceBmp = imgList.get(0).getOldBitmap();
                mListSource.clear();
                mListSource.addAll(imgList);
            } else {

                mListEnhance.clear();
                mListEnhance.addAll(imgList);
                if ( onCreated ) {

                    mLytHandle.setVisibility(View.GONE);
                    mListViewE.setVisibility(View.VISIBLE);
                }
                hasImage = false;
                setUriList();
                refreshImg();
            }
        }
    }

    private void setUriList() {

        if ( onCreated ) {

            List<String> list = new ArrayList<>();
            if (mListEnhance.size() > 0) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        uriList.clear();
                        for (int i = 0; i < 3; ++i) {

                            EvaluatBean evaluatBean = mListEnhance.get(i);
                            File file = FileUtils.bitmapToFile(evaluatBean.getNewBitmap(), cachePath + System.currentTimeMillis() + ".jpg");
                            if ( file != null ) {

                                uriList.add(file.getAbsolutePath());
                            }
                        }
                        sendEmptyMessage(HandleKeys.COPY_FILE_SUCCESS);
                    }
                }).start();
            }
        }
    }

    public void startDetectAllParam() {

        if ( onCreated ) {

            mListViewE.setVisibility(View.GONE);
            mLytHandle.setVisibility(View.VISIBLE);
        }
    }
}
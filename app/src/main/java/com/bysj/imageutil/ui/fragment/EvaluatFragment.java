package com.bysj.imageutil.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bysj.imageutil.R;
import com.bysj.imageutil.adapter.IEnAdapter;
import com.bysj.imageutil.base.BaseFragment;
import com.bysj.imageutil.ui.components.MyListView;
import com.bysj.imageutil.util.LogCat;
import com.bysj.imgevaluation.bean.EvaluatBean;

import java.util.ArrayList;

/**
 * 评图Fragment
 *
 * Create on 2021-2-27
 */

public class EvaluatFragment extends BaseFragment implements View.OnClickListener {
    /** 本页面标签，调试使用 */
    private static final String    TAG          = "evaluatFragment";
    /** 选择的图片 */
    private ImageView              mImgSource;
    /** 增强后的图片 */
    private ImageView              mImgEnhance;
    /** 原图参数列表 */
    private ArrayList<EvaluatBean> mListSource  = new ArrayList<>();
    /** 增强图片参数列表 */
    private ArrayList<EvaluatBean> mListEnhance = new ArrayList<>();
    /** 原图 */
    private Bitmap                 sourceBmp    = null;
    /** 增强图 */
    private Bitmap                 enhanceBmp   = null;
    /** 显示原图参数列表的适配器 */
    private IEnAdapter             mSAdapter;
    private MyListView             mListViewS;
    /** 显示增强图参数列表的适配器 */
    private IEnAdapter             mEAdapter;
    private MyListView             mListViewE;
    /**
     * 本页面是否创建完成。
     * 该标志位很重要，当收到来自另一个页面的消息时，
     * 需要判断本页面是否创建完成，必须创建完成才能显示
     * 数据（图片等）。
     */
    private boolean                onCreated   = false;
    /** 标记原图参数列表的状态：展开/折叠 */

    public EvaluatFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
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
        mImgSource  = view.findViewById(R.id.img_source);
        mListViewS  = view.findViewById(R.id.list_source);
        mImgEnhance = view.findViewById(R.id.img_enhance);
        mListViewE  = view.findViewById(R.id.list_enhance);
        //mListViewE.setNestedScrollingEnabled(true);
        //mListViewS.setNestedScrollingEnabled(true);
        /**
         * 初始化适配器数据
         */
        mSAdapter   = new IEnAdapter(mListSource);
        mEAdapter   = new IEnAdapter(mListEnhance);
        mListViewS.setAdapter(mSAdapter);
        mListViewE.setAdapter(mEAdapter);
        /**
         * 设置点击事件
         */

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        onCreated = true;
        showImgs();
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

    }

    private void showImgs() {

        if ( onCreated ) {

            if ( sourceBmp != null ) {

                mImgSource.setImageBitmap(sourceBmp);
            }
            if ( enhanceBmp != null ) {

                mImgEnhance.setImageBitmap(enhanceBmp);
            }
            mSAdapter.notifyDataSetChanged();
            mEAdapter.notifyDataSetChanged();
        }
    }

    public void imgChanged(ArrayList<EvaluatBean> imgList, boolean isSource) {

        if ( imgList != null && imgList.size() != 0 ) {

            if ( isSource ) {

                sourceBmp = imgList.get(0).getOldBitmap();
                mListSource.clear();
                mListSource.addAll(imgList);
            } else {

                enhanceBmp = imgList.get(0).getOldBitmap();
                mListEnhance.clear();
                mListEnhance.addAll(imgList);
            }
        }
        showImgs();
    }
}
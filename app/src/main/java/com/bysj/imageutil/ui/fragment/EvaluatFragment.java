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

import com.bysj.imageutil.R;
import com.bysj.imageutil.adapter.IEnAdapter;
import com.bysj.imageutil.base.BaseFragment;
import com.bysj.imageutil.util.LogCat;
import com.bysj.imgevaluation.bean.EvaluatBean;

import java.util.ArrayList;

/**
 * 评图Fragment
 *
 * Create on 2021-2-27
 */

public class EvaluatFragment extends BaseFragment {
    /** 本页面标签，调试使用 */
    private static final String TAG = "evaluatFragment";
    /** 选择的图片 */
    private ImageView mImgSource;
    /** 评价的图片 */
    private ArrayList<EvaluatBean> evaList = new ArrayList<>();
    /** 选择的图片 */
    private Bitmap choosedImg = null;
    /** 显示图片列表的适配器 */
    private IEnAdapter mAdapter;
    /**
     * 本页面是否创建完成。
     * 该标志位很重要，当收到来自另一个页面的消息时，
     * 需要判断本页面是否创建完成，必须创建完成才能显示
     * 数据（图片等）。
     */
    private boolean onCreated = false;

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

        mImgSource = view.findViewById(R.id.img_test);

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
    protected void onHandleMessage(Message msg) {

    }

    private void showImgs() {

        if ( onCreated ) {

            if ( choosedImg != null ) {

                mImgSource.setImageBitmap(choosedImg);
            }
            if ( evaList.size() != 0 ) {

                mAdapter.notifyDataSetChanged();
            }
        }
    }

    public void imgEnhanceed(ArrayList<EvaluatBean> evaluatBeans) {

        evaList.clear();
        evaList.addAll(evaluatBeans);
        showImgs();
    }

    public void imgChanged(Bitmap bitmap) {

        choosedImg = bitmap;
        showImgs();
    }
}
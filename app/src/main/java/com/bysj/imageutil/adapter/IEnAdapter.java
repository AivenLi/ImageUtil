package com.bysj.imageutil.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bysj.imageutil.R;
import com.bysj.imgevaluation.bean.EvaluatBean;

import java.util.ArrayList;

/**
 * 显示增强图片后各种参数列表适配器
 *
 * Create on 2021-2-27
 */

public class IEnAdapter extends BaseAdapter {

    private ArrayList<EvaluatBean> data = null;

    public IEnAdapter(ArrayList<EvaluatBean> data) {

        this.data = data;
    }

    @Override
    public int getCount() {

        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int i) {

        return data == null ? null : data.get(i).getDimension();
    }

    @Override
    public long getItemId(int i) {

        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolder viewHolder;
        EvaluatBean evaluatBean = data.get(i);

        if ( view == null ) {

            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_evaluation, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {

            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.mTvKey.setText(evaluatBean.getDimension());
        viewHolder.mTvValue.setText(evaluatBean.getValue() + "");
        Glide.with(viewHolder.mImg.getContext())
                .load(evaluatBean.getNewBitmap())
                .placeholder(android.R.color.darker_gray)
                .into(viewHolder.mImg);

        return view;
    }

    class ViewHolder {

        public TextView  mTvKey;
        public TextView  mTvValue;
        public ImageView mImg;
        public int mPosition;

        public ViewHolder(View view) {

            mTvKey   = view.findViewById(R.id.tv_dimension_key);
            mTvValue = view.findViewById(R.id.tv_dimension_value);
            mImg     = view.findViewById(R.id.img_img);
        }
    }
}

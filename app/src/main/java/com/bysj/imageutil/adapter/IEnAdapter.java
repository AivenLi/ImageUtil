package com.bysj.imageutil.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

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

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        return null;
    }
}

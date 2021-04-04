package com.bysj.imageutil.adapter.drag;

import com.bysj.imageutil.R;
import com.bysj.imageutil.bean.ICropResBean;
import com.bysj.imageutil.bean.SpliceBean;

import java.util.List;


public class SimpleRecyclerGridAdapter extends SimpleRecyclerListAdapter {

    public SimpleRecyclerGridAdapter(List<SpliceBean> recyclerItemList) {
        super(recyclerItemList);
    }

    @Override
    protected int getItemLayoutRes() {
        return R.layout.item_simple_grid;
    }
}

package com.bysj.imageutil.adapter.drag.darghelpercallback;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bysj.imageutil.bean.ICropResBean;
import com.bysj.imageutil.bean.SpliceBean;

import java.util.List;


public class GridSortHelperCallBack extends VerticalDragSortHelperCallBack {
    public GridSortHelperCallBack(List<SpliceBean> recyclerItemList) {
        super(recyclerItemList);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlag, ItemTouchHelper.ACTION_STATE_IDLE);
    }
}

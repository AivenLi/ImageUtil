package com.bysj.imageutil.adapter.drag.darghelpercallback;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


import com.bysj.imageutil.adapter.drag.RecyclerItem;

import java.util.List;

/**
 * 网格拖拽排序Helper
 *
 * @author JasonChen
 * @email chenjunsen@outlook.com
 * @createTime 2021/2/10 15:01
 */
public class GridSortHelperCallBack extends VerticalDragSortHelperCallBack {
    public GridSortHelperCallBack(List<RecyclerItem> recyclerItemList) {
        super(recyclerItemList);
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlag, ItemTouchHelper.ACTION_STATE_IDLE);
    }
}

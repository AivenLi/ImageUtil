package com.bysj.imageutil.adapter.drag.darghelpercallback;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bysj.imageutil.adapter.drag.RecyclerItem;

import java.util.List;

/**
 * 侧滑删除Helper
 *
 * @author JasonChen
 * @email chenjunsen@outlook.com
 * @createTime 2021/2/10 10:39
 */
public class SlideDeleteHelperCallBack extends ItemTouchHelper.Callback {
    private RecyclerView recyclerView;
    private List<RecyclerItem> recyclerItemList;
    private OnSwipedListener onSwipedListener;

    public void setOnSwipedListener(OnSwipedListener onSwipedListener) {
        this.onSwipedListener = onSwipedListener;
    }

    public SlideDeleteHelperCallBack(RecyclerView recyclerView, List<RecyclerItem> recyclerItemList) {
        this.recyclerView = recyclerView;
        this.recyclerItemList = recyclerItemList;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlag = ItemTouchHelper.ACTION_STATE_IDLE;
        int swipeFlag = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
        return makeMovementFlags(dragFlag, swipeFlag);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        //1、删除源数据
//        recyclerItemList.remove(viewHolder.getAdapterPosition());
        int pos = viewHolder.getAdapterPosition();
        RecyclerItem item = recyclerItemList.get(pos);
        recyclerItemList.remove(item);
        //2、通知视图刷新
        recyclerView.getAdapter().notifyItemRemoved(viewHolder.getAdapterPosition());
        //3、更正position,防止点击时，position错乱
        recyclerView.getAdapter().notifyItemRangeChanged(0, recyclerItemList.size());

        //4、可以用4替换2和3，但是就没有了删除后的收缩动画
//        recyclerView.getAdapter().notifyDataSetChanged();

        if (onSwipedListener != null) {
            onSwipedListener.onSwiped(viewHolder, item, pos);
        }
    }

    /**
     * 侧滑删除监听器
     *
     * @author JasonChen
     * @email chenjunsen@outlook.com
     * @createTime 2021/2/10 14:09
     */
    public interface OnSwipedListener {
        void onSwiped(RecyclerView.ViewHolder viewHolder, RecyclerItem deletedItem, int deletedPos);
    }

}

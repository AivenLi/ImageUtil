package com.bysj.imageutil.adapter.drag.darghelpercallback;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bysj.imageutil.adapter.drag.RecyclerItem;

import java.util.Collections;
import java.util.List;


/**
 * 垂直拖拽排序的Helper
 *
 * @author JasonChen
 * @email chenjunsen@outlook.com
 * @createTime 2021/2/9 16:40
 */
public class VerticalDragSortHelperCallBack extends ItemTouchHelper.Callback {

    private List<RecyclerItem> recyclerItemList;
    private OnDragListener onDragListener;

    public void setOnDragListener(OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
    }

    public VerticalDragSortHelperCallBack(List<RecyclerItem> recyclerItemList) {
        this.recyclerItemList = recyclerItemList;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        return makeMovementFlags(dragFlag, ItemTouchHelper.ACTION_STATE_IDLE);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        int fromPos = viewHolder.getAdapterPosition();
        int toPos = target.getAdapterPosition();
        Log.d("onMove", fromPos + "--->" + toPos);
        //1、从数据源上交换条目的位置
        Collections.swap(recyclerItemList, fromPos, toPos);
        //2、从视图上通知刷新视图的改变
        recyclerView.getAdapter().notifyItemMoved(fromPos, toPos);

        //3、更正实际点击的position,防止点击时，position错乱
        int startPos = Math.min(fromPos, toPos);
        int itemCount = Math.abs(fromPos - toPos) + 1;
        recyclerView.getAdapter().notifyItemRangeChanged(startPos, itemCount);

        //4、PS:2和3的合体步骤可以用4替代，但是就没有了替换动画了，而且刷新效率比上面低，是全局刷新
//        recyclerView.getAdapter().notifyDataSetChanged();

        //请注意该返回值:只有在返回true的时候，才会走onMoved方法
        return true;
    }

    @Override
    public void onMoved(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, int fromPos, @NonNull RecyclerView.ViewHolder target, int toPos, int x, int y) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        Log.d("onMoved", "第一个:" + recyclerItemList.get(0).toString() + " " + fromPos + "--->" + toPos);
        Log.d("onMoved", "第二个:" + recyclerItemList.get(1).toString() + " " + fromPos + "--->" + toPos);
        if (onDragListener != null) {
            onDragListener.onItemMoved(viewHolder, target, fromPos, toPos);
        }
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    /**
     * 拖拽监听器
     *
     * @author JasonChen
     * @email chenjunsen@outlook.com
     * @createTime 2021/2/10 14:12
     */
    public interface OnDragListener {
        void onItemMoved(RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target, int fromPos, int toPos);
    }
}

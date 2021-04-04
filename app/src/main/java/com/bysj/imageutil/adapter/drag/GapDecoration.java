package com.bysj.imageutil.adapter.drag;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


public class GapDecoration extends RecyclerView.ItemDecoration {
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.top = 20;//所有条目顶部间距20px
        //最后一个条目底部间距20px
        //注意：获取子元素的数量的时候只能用adapter的getItemCount,直接使用parent的getChildCount获取到的数量只是可见的数量，不准确
        if (parent.getChildAdapterPosition(view) == parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = 20;
        }
    }
}

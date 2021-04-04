package com.bysj.imageutil.adapter.drag;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;



public class GapGridDecoration extends RecyclerView.ItemDecoration {
    private int spanCount;

    public GapGridDecoration(int spanCount) {
        this.spanCount = spanCount;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (spanCount <= 0) {
            Log.w("GapGridDecoration", "spanCount should be bigger than zero!");
            return;
        }
        int currentPos = parent.getChildAdapterPosition(view);
        int totalCount = parent.getAdapter().getItemCount();
        int totalRows = ceil(totalCount, spanCount);//进一法获取最大行数
        int currentRow = ceil(currentPos + 1, spanCount);//进一法获取当前操作item所在行数

        boolean isLastRow = (currentRow == totalRows);//是否是最后一行
        boolean isRight = ((currentPos + 1) % spanCount == 0);//是否是最右边的item

        String fmt = "当前位置:%1$s 总数量:%2$s 最大行数:%3$S 当前行数:%4$s";
        Log.d("itemOffset", String.format(fmt, currentPos, totalCount, totalRows, currentRow));

        //以左上方向为基准绘制间隔
        outRect.top = 20;
        outRect.left = 20;
        outRect.right = isRight ? 20 : 0;
        outRect.bottom = isLastRow ? 20 : 0;
    }

    /**
     * 自定义进一法算法，不要使用Math的ceil,否则计算结果会有问题
     *
     * @param a
     * @param b
     * @return
     */
    private int ceil(int a, int b) {
        int n = a / b;
        int res = a % b;
        return res != 0 ? n + 1 : n;
    }
}

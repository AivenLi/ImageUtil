package com.bysj.imageutil.adapter.drag;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bysj.imageutil.R;

import java.util.List;

/**
 * 简单的垂直拖拽排序适配器
 * @author JasonChen
 * @email chenjunsen@outlook.com
 * @createTime 2021/2/10 10:34
 */
public class SimpleRecyclerListAdapter extends RecyclerView.Adapter<SimpleRecyclerViewHolder> {

    private List<RecyclerItem> recyclerItemList;
    private OnItemCLickListener onItemCLickListener;

    public void setOnItemCLickListener(OnItemCLickListener onItemCLickListener) {
        this.onItemCLickListener = onItemCLickListener;
    }

    public SimpleRecyclerListAdapter(List<RecyclerItem> recyclerItemList) {
        this.recyclerItemList = recyclerItemList;
    }

    @NonNull
    @Override
    public SimpleRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(getItemLayoutRes(), parent, false);
        return new SimpleRecyclerViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleRecyclerViewHolder holder, int position) {
        RecyclerItem item = recyclerItemList.get(position);
        holder.iconView.setImageBitmap(item.getIcon());
        holder.textView.setText(item.getText());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemCLickListener != null) {
                    onItemCLickListener.onItemClick(item, holder, position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recyclerItemList == null ? 0 : recyclerItemList.size();
    }

    public interface OnItemCLickListener {
        void onItemClick(RecyclerItem recyclerItem, SimpleRecyclerViewHolder holder, int position);
    }

    protected @LayoutRes int getItemLayoutRes(){
        return R.layout.item_simple_list;
    }
}

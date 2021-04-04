package com.bysj.imageutil.adapter.drag;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bysj.imageutil.R;
import com.bysj.imageutil.bean.ICropResBean;
import com.bysj.imageutil.bean.SpliceBean;

import java.util.List;


public class SimpleRecyclerListAdapter extends RecyclerView.Adapter<SimpleRecyclerViewHolder> {

    private List<SpliceBean> recyclerItemList;
    private OnItemCLickListener onItemCLickListener;
    private OnItemRemoveClickListener onItemRemoveClickListener;

    public void setOnItemCLickListener(OnItemCLickListener onItemCLickListener) {
        this.onItemCLickListener = onItemCLickListener;
    }

    public void setOnItemRemoveClickListener(OnItemRemoveClickListener onItemRemoveClickListener) {

        this.onItemRemoveClickListener = onItemRemoveClickListener;
    }

    public SimpleRecyclerListAdapter(List<SpliceBean> recyclerItemList) {
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
        SpliceBean item = recyclerItemList.get(position);
        Glide.with(holder.iconView.getContext())
                .load(item.getImage())
                .placeholder(android.R.color.darker_gray)
                .into(holder.iconView);
        if ( item.getHasImage() ) {

            holder.clearView.setVisibility(View.VISIBLE);
        } else {

            holder.clearView.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemCLickListener != null) {
                    onItemCLickListener.onItemClick(item, holder, position);
                }
            }
        });
        holder.clearView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if ( onItemRemoveClickListener != null ) {

                    onItemRemoveClickListener.onItemRemoveClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return recyclerItemList == null ? 0 : recyclerItemList.size();
    }

    public interface OnItemCLickListener {
        void onItemClick(SpliceBean recyclerItem, SimpleRecyclerViewHolder holder, int position);
    }

    public interface OnItemRemoveClickListener {

        void onItemRemoveClick(int position);
    }

    protected @LayoutRes int getItemLayoutRes(){
        return R.layout.item_simple_list;
    }
}

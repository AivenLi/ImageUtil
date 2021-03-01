package com.bysj.imageutil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bysj.imageutil.R;
import com.bysj.imageutil.adapter.listener.ICropOnClickListener;
import com.bysj.imageutil.bean.ICropSetBean;

import java.util.ArrayList;

/**
 * 图片裁剪设置适配器
 *
 * Create on 2021-3-2
 */

public class ICropSetAdapter extends RecyclerView.Adapter<ICropSetAdapter.SetViewHolder> {

    private ArrayList<ICropSetBean> data;
    private ICropOnClickListener listener = null;

    public ICropSetAdapter(ArrayList<ICropSetBean> data) {

        this.data = data;
    }

    public ICropSetAdapter(ArrayList<ICropSetBean> data, ICropOnClickListener listener) {

        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_crop_set_item, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetViewHolder holder, int position) {

        holder.mTv.setText(data.get(position).getTitle());
        holder.mPosition = position;
    }

    @Override
    public int getItemCount() {

        return data.size();
    }

    class SetViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTv;
        public int mPosition;

        public SetViewHolder(@NonNull View itemView) {

            super(itemView);

            mTv = itemView.findViewById(R.id.tv_crop);
        }

        @Override
        public void onClick(View view) {

            if ( listener != null ) {

                int id = view.getId();
                if ( id == R.id.tv_crop ) {

                    listener.onItemClick(mPosition);
                }
            }
        }
    }

    public void setOnItemClick(ICropOnClickListener listener) {

        this.listener = listener;
    }
}

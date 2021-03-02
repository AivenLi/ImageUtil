package com.bysj.imageutil.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bysj.imageutil.R;
import com.bysj.imageutil.bean.ICropResBean;
import com.bysj.imageutil.util.LogCat;
import com.bysj.imgevaluation.bean.EvaluatBean;

import java.util.ArrayList;

/**
 * 图片裁剪结果适配器
 *
 * Create on 2021-3-2
 */

public class ICropResAdapter extends RecyclerView.Adapter<ICropResAdapter.CropViewHolder> {

    private ArrayList<ICropResBean> data = null;
    private int width = -1;
    private int height = -1;

    public ICropResAdapter(ArrayList<ICropResBean> data) {

        this.data = data;
    }

    public ICropResAdapter(ArrayList<ICropResBean> data, int width, int height) {

        this.data = data;
        this.width = width;
        this.height = height;
    }

    @NonNull
    @Override
    public CropViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_crop_result, parent, false);
        return new CropViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CropViewHolder holder, int position) {

        //ICropResBean bean = data.get(position);
        Glide.with(holder.mImgView.getContext())
                .load(data.get(position).getImage())
                .placeholder(android.R.color.darker_gray)
                .into(holder.mImgView);
        holder.mPosition = position;
    }

    @Override
    public int getItemCount() {

        return data == null ? 0 : data.size();
    }

    class CropViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImgView;
        public int mPosition;

        public CropViewHolder(@NonNull View view) {

            super(view);
            mImgView = view.findViewById(R.id.img_crop_result);
            if ( width != -1 ) {

                ViewGroup.LayoutParams layoutParams = mImgView.getLayoutParams();
                layoutParams.width = width;
                layoutParams.height = height;
                mImgView.setLayoutParams(layoutParams);
            }
        }
    }
}

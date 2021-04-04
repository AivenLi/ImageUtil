package com.bysj.imageutil.adapter.drag;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import com.bysj.imageutil.R;

public class SimpleRecyclerViewHolder extends ViewHolder {
    public ImageView iconView;
    public ImageView clearView;

    public SimpleRecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        iconView = itemView.findViewById(R.id.icon);
        clearView = itemView.findViewById(R.id.img_remove);
    }
}

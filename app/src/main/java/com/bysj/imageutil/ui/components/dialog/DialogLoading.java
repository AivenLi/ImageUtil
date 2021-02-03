package com.bysj.imageutil.ui.components.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bysj.imageutil.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class DialogLoading {

    private AlertDialog alertDialog;
    private TextView mTvTitle;
    private CircleImageView mImgLogo;

    public DialogLoading(Context context) {

        initDialog(context, null, true);
    }

    public DialogLoading(Context context, String title, boolean cancelable) {

        initDialog(context, title, cancelable);
    }

    private void initDialog(Context context, String title, boolean cancelable) {

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View dialogView = layoutInflater.inflate(R.layout.dialog_loading, null);
        mTvTitle = dialogView.findViewById(R.id.tv_title);
        mImgLogo = dialogView.findViewById(R.id.img_logo);
        setText(title);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView);
        alertDialog = builder.create();
        setCancelable(cancelable);
        Window window = alertDialog.getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        window.setAttributes(lp);
    }

    public DialogLoading setText(String title) {

        if ( !TextUtils.isEmpty(title) ) {

            mTvTitle.setText(title);
        }
        return this;
    }

    public void setTextColor(int color) {

        mTvTitle.setTextColor(color);
    }

    public void setLogo(Drawable source) {

        if ( source != null ) {

            mImgLogo.setImageDrawable(source);
        }
    }

    public void setLogo(int source) {

        mImgLogo.setImageResource(source);
    }

    public void setLogo(Bitmap bitmap) {

        if ( bitmap != null ) {

            mImgLogo.setImageBitmap(bitmap);
        }
    }

    public void setLogo(Uri uri) {

        if ( uri != null ) {

            mImgLogo.setImageURI(uri);
        }
    }

    public DialogLoading setCancelable(boolean cancelable) {

        alertDialog.setCancelable(cancelable);
        return this;
    }

    public void show() {

        alertDialog.show();
    }

    public void hide() {

        alertDialog.dismiss();
    }

    public boolean isShowing() {

        return alertDialog.isShowing();
    }
}

package com.bysj.imageutil.ui.components.dialog;

/**
 * 自定义弹窗控件
 */

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bysj.imageutil.R;

public class DialogPrompt implements View.OnClickListener {

    private AlertDialog alertDialog;
    private Context mContext;
    private DialogPromptListener listener = null;
    private LinearLayout mLytBody;
    private TextView mTvTitle;
    private TextView mTvContent;
    private TextView mTvYes;
    private TextView mTvCancel;

    public DialogPrompt(Context context) {

        this.mContext = context;
        initDialog(null, null);
    }

    public DialogPrompt(Context context, String title, String content) {

        this.mContext = context;
        initDialog(title, content);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    private void initDialog(String title, String content) {

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View dialogView = layoutInflater.inflate(R.layout.dialog_prompt, null);

        //mLytBody   = dialogView.findViewById(R.id.lyt_body);
        mTvTitle   = dialogView.findViewById(R.id.tv_prompt_title);
        mTvContent = dialogView.findViewById(R.id.tv_prompt_content);
        mTvYes     = dialogView.findViewById(R.id.tv_prompt_yes);
        mTvCancel  = dialogView.findViewById(R.id.tv_prompt_cancel);
      //  TextView mTvSpace   = dialogView.findViewById(R.id.tv_prompt_space);

        setTitleContent(title, content);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setView(dialogView);
        alertDialog = builder.create();
        mTvYes.setOnClickListener(this);
        mTvCancel.setOnClickListener(this);
    }

    public void show(DialogPromptListener listener) {

        this.listener = listener;
        alertDialog.show();
    }

    public void hide() {

        alertDialog.dismiss();
    }

    public DialogPrompt setTitleContent(String title, String content) {

        setTitle(title);
        setContent(content);
        return this;
    }

    public void setBackground(Drawable resource) {

        mLytBody.setBackground(resource);
    }

    public void setBackgroundColor(int color) {

        mLytBody.setBackgroundColor(color);
    }

    public DialogPrompt setTitle(String title) {

        mTvTitle.setText(title);
        return this;
    }

    public void setTitleColor(int color) {

        mTvTitle.setTextColor(color);
    }

    public void setTitleBackground(Drawable background) {

        mTvTitle.setBackground(background);
    }

    public void setTitleBgColor(int color) {

        mTvTitle.setBackgroundColor(color);
    }

    public void setTitleAndColor(String title, int color) {

        setTitle(title);
        setTitleColor(color);
    }

    public DialogPrompt setContent(String content) {

        mTvContent.setText(content);
        return this;
    }

    public void setContentColor(int color) {

        mTvContent.setTextColor(color);
    }

    public void setContentBackground(Drawable background) {

        mTvContent.setBackground(background);
    }

    public void setContentBgColor(int color) {

        mTvContent.setBackgroundColor(color);
    }

    public void setContentAndColor(String content, int color) {

        setContent(content);
        setContentColor(color);
    }

    public DialogPrompt setYesText(String yesText) {

        if ( !TextUtils.isEmpty(yesText) ) {

            mTvYes.setText(yesText);
        }
        return this;
    }

    public void setYesTextColor(int color) {

        mTvYes.setTextColor(color);
    }

    public void setYesBackground(Drawable background) {

        mTvYes.setBackground(background);
    }

    public void setYesBgColor(int color) {

        mTvYes.setBackgroundColor(color);
    }

    public void setYesTextAndColor(String yesText, int color) {

        setYesText(yesText);
        setYesTextColor(color);
    }

    public DialogPrompt setCancelText(String cancelText) {

        if ( !TextUtils.isEmpty(cancelText) ) {

            mTvCancel.setText(cancelText);
        }
        return this;
    }

    public void setCancelTextColor(int color) {

        mTvCancel.setTextColor(color);
    }

    public void setCancelBackground(Drawable background) {

        mTvCancel.setBackground(background);
    }

    public void setCancelBgColor(int color) {

        mTvCancel.setBackgroundColor(color);
    }

    public void setCancelTextAndColor(String cancelText, int color) {

        setCancelText(cancelText);
        setCancelTextColor(color);
    }

    @Override
    public void onClick(View v) {

        switch ( v.getId() ) {

            case R.id.tv_prompt_yes:

                alertDialog.dismiss();
                if ( listener != null ) {

                    listener.yes();
                }
                break;
            case R.id.tv_prompt_cancel:

                alertDialog.dismiss();
                if ( listener != null ) {

                    listener.cancel();
                }
                break;
        }
    }
}

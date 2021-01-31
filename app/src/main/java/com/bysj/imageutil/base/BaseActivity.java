package com.bysj.imageutil.base;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.ui.components.dialog.DialogPromptListener;
import com.bysj.imageutil.util.ActivityManageHelper;
import com.bysj.imageutil.util.WeakHandler;

/**
 * Activity基类
 * Create on 2021-1-29
 */

public abstract class BaseActivity extends AppCompatActivity {

    /** 本页面 */
    protected Activity mActivity;
    /** 上下文 */
    protected Context  mContext;
    /** 弹窗 */
    protected DialogPrompt mDialog = null;

    @Override
    protected void onCreate(@Nullable Bundle saveInstanceState) {

        super.onCreate(saveInstanceState);
        getWindow().setBackgroundDrawable(null);
        //始终保持竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        /**
         * 这一句是为了让有的Activity可以全屏，该语句必须在setContentView之前，
         * 在引入SmartSwipe之后下面这一句导致app闪退，我怀疑是因为SmartSwipe
         * 也设置了这个。因此注释掉就可以了。
         */
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(initLayout());

        mActivity = this;
        mContext = this;
        //dialog = new DialogPrompt(mContext);
        ActivityManageHelper.getInstance().addActivity(this);
        initView();
        setViewOnClick();
    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    protected abstract void onHandleMessage(final Message msg);
    protected abstract int initLayout();
    protected abstract void initView();
    protected abstract void setViewOnClick();
    protected <T extends View> T findView(int id) {
        return (T) findViewById(id);
    }

    /**
     * Send a Message
     * */
    protected void sendMessage(int what, Object obj) {

        Message msg = new Message();
        msg.what = what;
        msg.obj  = obj;
        handler.sendMessage(msg);
    }

    protected void sendMessage(Message msg) {

        handler.sendMessage(msg);
    }

    protected void sendEmptyMessage(int what) {

        handler.sendEmptyMessage(what);
    }

    /**
     * 结束本页面
     */
    protected void finishThis() {

        ActivityManageHelper.getInstance().finshActivity(mActivity);
    }

    /**
     * 显示弹窗
     * @param title
     * @param content
     */
    protected void showDialog(String title, String content) {

        if ( mDialog != null ) {

            mDialog.setTitleContent(title, content).show(null);
        }
    }

    /**
     * 显示弹窗
     * @param title
     * @param content
     * @param listener
     */
    protected void showDialog(String title, String content, DialogPromptListener listener) {

        if ( mDialog != null ) {

            mDialog.setTitleContent(title, content).show(listener);
        }
    }

    /**
     * 隐藏弹窗
     */
    protected void hideDialog() {

        if ( mDialog != null ) {

            mDialog.hide();
        }
    }

    /**
     * 文本提示
     * @param str
     */
    protected void myToast(String str) {

        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
    }

    /**
     * Avoid memory leaks
     * WeakHandler
     */
    public WeakHandler handler = new WeakHandler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            onHandleMessage(msg);
        }
    };

}

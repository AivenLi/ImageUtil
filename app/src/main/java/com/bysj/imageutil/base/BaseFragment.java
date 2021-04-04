package com.bysj.imageutil.base;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bysj.imageutil.ui.components.dialog.DialogLoading;
import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.ui.components.dialog.DialogPromptListener;
import com.bysj.imageutil.util.WeakHandler;


/**
 * 可以懒加载的Fragment，使用androidx fragment的方式实现，区别传统方式
 */
public abstract class BaseFragment extends Fragment {

    private View mRootView;
    private boolean mIsInitData;

    protected DialogPrompt mDialog = null;
    protected DialogLoading mLoading = null;
    protected Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutResId(), container, false);
            initView();
            //setViewOnClick();
        }
        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!isLazyLoad()) {
            fetchData();
        }
        mContext = view.getContext();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchData();
   //     String simpleName = getClass().getSimpleName();
    }

    @Override
    public void onPause() {
        super.onPause();
   //     String simpleName = getClass().getSimpleName();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        if ( handler != null ) {

            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
   //     String simpleName = getClass().getSimpleName();
    }

    private void fetchData() {
        if (mIsInitData)
            return;
        initData();
        mIsInitData = true;
    }

    public <T extends View> T findViewById(@IdRes int id) {
        return mRootView.findViewById(id);
    }

    protected abstract int getLayoutResId();
    protected abstract void onHandleMessage(final Message msg);
    //protected abstract void setViewOnClick();

    protected void initView() {
    }

    protected void initData() {
    }

    protected void showDialog(String title, String content) {

        if ( mDialog != null ) {

            mDialog.setTitleContent(title, content).show(null);
        }
    }

    protected void showDialog(String title, String content, String yes, String cancel) {

        showDialog(title, content, yes, cancel, null);
    }

    protected void showDialog(String title, String content, String yes, String cancel, DialogPromptListener listener) {

        if ( mDialog != null ) {

            mDialog.setTitleContent(title, content)
                    .setYesText(yes)
                    .setCancelText(cancel)
                    .show(listener);
        }
    }

    protected void showDialog(String title, String content, DialogPromptListener listener) {

        if ( mDialog != null ) {

            mDialog.setTitleContent(title, content).show(listener);
        }
    }

    protected void hideDialog() {

        if ( mDialog != null ) {

            mDialog.hide();
        }
    }

    /**
     * “正在加载”弹窗标题
     * @param title
     */
    protected void showLoading(String title) {

        if ( mLoading != null ) {

            mLoading.setText(title).show();
        }
    }

    protected void showLoading() {

        if ( mLoading != null ) {

            mLoading.show();
        }
    }

    protected void hideLoading() {

        if ( mLoading != null ) {

            mLoading.hide();
        }
    }

    /**
     * 是否懒加载
     */
    protected boolean isLazyLoad() {
        return false;
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

    protected void jumpOtherPage(Class<?> cls, Uri uri) {

        Intent intent = new Intent(getActivity(), cls);
        intent.setData(uri);
        mContext.startActivity(intent);
    }

    /**
     * Toast.makeText
     * @param title
     */
    protected void myToast(String title) {

        Toast.makeText(getContext(), title, Toast.LENGTH_SHORT).show();
    }

    /**
     * Avoid memory leaks
     * WeakHandler
     */
    protected WeakHandler handler = new WeakHandler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            onHandleMessage(msg);
        }
    };
}

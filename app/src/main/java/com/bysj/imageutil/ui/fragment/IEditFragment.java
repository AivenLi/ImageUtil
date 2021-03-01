package com.bysj.imageutil.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bysj.imageutil.R;
import com.bysj.imageutil.base.BaseFragment;
import com.bysj.imageutil.util.LogCat;

/**
 * 图片拼接啥的Fragment
 *
 * Create on 2021-2-27
 */

public class IEditFragment extends BaseFragment {

    private static final String TAG = "iEditFragment";

    public IEditFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_i_edit, container, false);
    }

    @Override
    protected int getLayoutResId() {

        return R.layout.fragment_i_edit;
    }

    @Override
    protected void onHandleMessage(Message msg) {

    }

    public void test() {

        LogCat.d(TAG, "回调接收成功");
    }
}
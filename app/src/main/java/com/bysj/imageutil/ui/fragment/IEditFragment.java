package com.bysj.imageutil.ui.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bysj.imageutil.R;
import com.bysj.imageutil.base.BaseFragment;

/**
 * 图片拼接啥的Fragment
 *
 * Create on 2021-2-27
 */

public class IEditFragment extends BaseFragment {


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
}
package com.bysj.imageutil.ui.activity;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bysj.imageutil.R;
import com.bysj.imageutil.base.BaseActivity;
import com.bysj.imageutil.ui.components.RegulatorView;
import com.bysj.imageutil.ui.components.dialog.DialogLoading;
import com.bysj.imageutil.ui.components.dialog.DialogPrompt;
import com.bysj.imageutil.ui.components.dialog.DialogPromptListener;
import com.bysj.imageutil.ui.fragment.EvaluatFragment;
import com.bysj.imageutil.ui.fragment.IEditFragment;
import com.bysj.imageutil.ui.fragment.IEnhanceFragment;
import com.bysj.imageutil.ui.fragment.TabFragment;
import com.bysj.imageutil.util.FileUtils;
import com.bysj.imageutil.util.GetImgPath;
import com.bysj.imageutil.util.GlideUtil;
import com.bysj.imageutil.util.HandleKeys;
import com.bysj.imageutil.util.IntentKeys;
import com.bysj.imageutil.util.LogCat;
import com.bysj.imgevaluation.EvaluatUtil;
import com.bysj.imgevaluation.bean.EvaluatBean;
import com.bysj.imgevaluation.listener.EvaluatAllListener;
import com.bysj.opencv450.HandleImageListener;
import com.bysj.opencv450.OpenCVUtil;
import com.bysj.opencv450.PictureMatrix;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends BaseActivity implements View.OnClickListener,
        BottomNavigationView.OnNavigationItemSelectedListener,
        IEnhanceFragment.ImageChangedListener {

    /** 日志标志 */
    private static final String TAG                   = "mainActivity";
    /** Fragment列表 */
    private ArrayList<Fragment> fragments;
    /**
     * 本Activity装载两个Fragment，而第一个Fragment（TabFragment）
     * 又装载两个Fragment（分别是IEnhanceFragment和EvaluatFragment），
     * 这两个Fragment需要通信，因此使用本Activity作为通信桥梁，
     * 故在此创建这两个Fragment的实例。
     */
    private ArrayList<Fragment> tabFragments;
    /** 当前页 */
    private int                 currentPage;
    /** 再按一次退出程序 */
    private long                exitTime              = 0;
    /** Bottom tab */
    BottomNavigationView        bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        fragments = new ArrayList<>();
        tabFragments = new ArrayList<>();
        tabFragments.add(new IEnhanceFragment());
        tabFragments.add(new EvaluatFragment());
        fragments.add(new TabFragment(tabFragments));
        fragments.add(new IEditFragment());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.layout_content, fragments.get(0))
                .commitAllowingStateLoss();
        currentPage = 0;
    }

    @Override
    protected int initLayout() {

        return R.layout.activity_main;
    }

    @Override
    protected void initView() {

        bottomNavigationView = findViewById(R.id.nav_view);
    }

    @Override
    protected void setViewOnClick() {

        bottomNavigationView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int index;
        int itemId = item.getItemId();

        switch ( itemId ) {

            default:

            case R.id.tab_ienhance:
                index = 0;
                break;
            case R.id.tab_iedit:
                index = 1;
                break;
        }
        if ( index != currentPage ) {

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = fragments.get(index);
            Fragment curFragment = fragments.get(currentPage);
            if (fragment.isAdded()) {

                transaction.hide(curFragment).show(fragment);
            } else {

                transaction.add(R.id.layout_content, fragment).hide(curFragment);
            }
            transaction.commitAllowingStateLoss();
            currentPage = index;
        }
        return true;
    }

    @Override
    public void onClick(View view) {

    }

    @Override
    protected void onHandleMessage(final Message msg) {

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void imgChanged(Bitmap bitmap) {

        LogCat.d(TAG, "Fragment回调");
        EvaluatFragment fragment = (EvaluatFragment)tabFragments.get(1);
        fragment.imgChanged(bitmap);
    }

    @Override
    public void enhancedImage(ArrayList<EvaluatBean> evaluatBeans) {

    }

    /**
     * 再按一次退出程序
     */
    public void exit() {

        if ((System.currentTimeMillis() - exitTime) > 2000) {

            Toast.makeText(getApplicationContext(), getString(R.string.exit_app),
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {

            super.onBackPressed();
            finish();
            System.exit(0);
        }
    }
}
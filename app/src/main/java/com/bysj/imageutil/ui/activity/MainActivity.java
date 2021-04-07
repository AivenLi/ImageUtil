package com.bysj.imageutil.ui.activity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import com.bysj.imageutil.R;
import com.bysj.imageutil.base.BaseActivity;
import com.bysj.imageutil.ui.fragment.IEvaluatFragment;
import com.bysj.imageutil.ui.fragment.IEnhanceFragment;
import com.bysj.imageutil.ui.fragment.LeftTabFragment;
import com.bysj.imageutil.ui.fragment.RightTabFragment;
import com.bysj.imageutil.util.LogCat;
import com.bysj.imgevaluation.bean.EvaluatBean;
import com.google.android.material.bottomnavigation.BottomNavigationView;


import java.util.ArrayList;

public class MainActivity extends BaseActivity implements
        BottomNavigationView.OnNavigationItemSelectedListener,
        IEnhanceFragment.ImageChangedListener {

    /** 日志标志 */
    private static final String  TAG                   = "mainActivity";
    /** Fragment列表 */
    private ArrayList<Fragment>  fragments;
    /**
     * 本Activity装载两个Fragment，而第一个Fragment（LeftTabFragment）
     * 又装载两个Fragment（分别是IEnhanceFragment和EvaluatFragment），
     * 这两个Fragment需要通信，因此使用本Activity作为通信桥梁，
     * 故在此创建这两个Fragment的实例。
     */
    private ArrayList<Fragment>  tabFragments;
    /** 当前页 */
    private int                  currentPage;
    /** 再按一次退出程序 */
    private long                 exitTime              = 0;
    /** Bottom tab */
    private BottomNavigationView bottomNavigationView;

    private IEvaluatFragment     IEvaluatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        fragments = new ArrayList<>();
        tabFragments = new ArrayList<>();
        IEvaluatFragment = new IEvaluatFragment();
        tabFragments.add(new IEnhanceFragment());
        tabFragments.add(IEvaluatFragment);
        fragments.add(new LeftTabFragment(tabFragments));
        fragments.add(new RightTabFragment());
        getSupportFragmentManager().beginTransaction()
                .add(R.id.layout_content, fragments.get(0))
                .commitAllowingStateLoss();
        currentPage = 0;
        LogCat.d("shareUtil", Environment.getExternalStorageDirectory().getAbsolutePath());
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
    public void refreshEvaluatFragment(ArrayList<EvaluatBean> evaluatBeans, boolean isSource) {

        IEvaluatFragment.imgChanged(evaluatBeans, isSource);
    }

    @Override
    public void startDetectImgParam() {

        IEvaluatFragment.startDetectAllParam();
    }

    public Context getContext() {

        return mContext;
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
package com.bysj.imageutil.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bysj.imageutil.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

/**
 * 本Fragment只是用来存放另外多个Fragment
 *
 * Create on 2021-2-27
 */

public class LeftTabFragment extends Fragment {

    /** 调试使用 */
    private static final String TAG = "tabFragment";
    /** Fragment列表 */
    private ArrayList<Fragment> fragments = new ArrayList<>();
    /** Fragment标题列表 */
    List<String>                tabTitles = new ArrayList<>();
    /** 页面控件 */
    private ViewPager2          viewPager2;
    /** Tab控件 */
    private TabLayout           tabLayout;

    public LeftTabFragment() {
        // Required empty public constructor
    }

    public LeftTabFragment(ArrayList<Fragment> fragments) {

        this.fragments = fragments;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab, container, false);

        viewPager2  = view.findViewById(R.id.vp2);
        tabLayout    = view.findViewById(R.id.tab_layout);
        tabTitles.add(this.getString(R.string.i_enhance));
        tabTitles.add(this.getString(R.string.i_evaluation));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        /**
         * 设置页面适配器
         */
        viewPager2.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {

                return fragments.get(position);
            }

            @Override
            public int getItemCount() {

                return fragments.size();
            }
        });
        /**
         * 设置页面标题
         */
        new TabLayoutMediator(tabLayout, viewPager2, ((tab, position) -> {

            tab.setText(tabTitles.get(position));
        })).attach();
    }
}
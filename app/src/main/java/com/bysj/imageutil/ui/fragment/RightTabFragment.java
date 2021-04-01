package com.bysj.imageutil.ui.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bysj.imageutil.R;
import com.bysj.imageutil.base.BaseFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;


public class RightTabFragment extends BaseFragment {

    /** 调试使用 */
    private static final String TAG = "tabFragment";
    /** Fragment列表 */
    private ArrayList<Fragment> fragments = new ArrayList<>();
    /** Fragment标题列表 */
    List<String> tabTitles = new ArrayList<>();
    /** 页面控件 */
    private ViewPager2 viewPager2;
    /** Tab控件 */
    private TabLayout tabLayout;

    public RightTabFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        fragments.add(new ICropFragment());
        fragments.add(new SpliceFragment());
        fragments.add(new ISplitFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_right_tab, container, false);

        viewPager2  = view.findViewById(R.id.vp2);
        tabLayout   = view.findViewById(R.id.tab_layout);
        tabTitles.add(this.getString(R.string.i_crop));
        tabTitles.add(this.getString(R.string.i_splice));
        tabTitles.add(this.getString(R.string.i_split));

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

    @Override
    protected int getLayoutResId() {

        return R.layout.fragment_right_tab;
    }

    @Override
    protected void onHandleMessage(Message msg) {

    }
}
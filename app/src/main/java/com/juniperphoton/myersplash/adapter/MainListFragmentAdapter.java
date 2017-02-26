package com.juniperphoton.myersplash.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.juniperphoton.myersplash.fragment.MainListFragment;
import com.juniperphoton.myersplash.model.UnsplashCategory;

public class MainListFragmentAdapter extends FragmentStatePagerAdapter {
    private MainListFragment.Callback mCallback;

    public MainListFragmentAdapter(MainListFragment.Callback callback, FragmentManager fm) {
        super(fm);
        mCallback = callback;
    }

    @Override
    public Fragment getItem(int position) {
        MainListFragment fragment = new MainListFragment();
        switch (position) {
            case 0:
                fragment.setCategory(UnsplashCategory.getFeaturedCategory(), mCallback);
                break;
            case 1:
                fragment.setCategory(UnsplashCategory.getNewCategory(), mCallback);
                break;
            case 2:
                fragment.setCategory(UnsplashCategory.getRandomCategory(), mCallback);
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }
}

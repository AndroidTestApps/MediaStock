package com.example.mediastock.util;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.example.mediastock.activities.AbstractFragment;
import com.example.mediastock.activities.FilterImageFragment;
import com.example.mediastock.activities.FilterMusicFragment;
import com.example.mediastock.activities.FilterVideoFragment;

import java.util.List;

public class CustomPagerAdapter extends FragmentStatePagerAdapter {
    private List<AbstractFragment> fragments;
    private int numTabs;


    public CustomPagerAdapter(FragmentManager fm, int numTabs, List<AbstractFragment> fragments) {
        super(fm);
        this.fragments = fragments;
        this.numTabs = numTabs;
    }

    @Override
    public Parcelable saveState() {
        return super.saveState();
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        super.restoreState(state, loader);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){

            case 3:
                return new FilterImageFragment();

            case 4:
                return new FilterVideoFragment();

            case 5:
                return new FilterMusicFragment();

            default:
                return this.fragments.get(position);
        }


    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    @Override
    public int getCount() {
        return numTabs;
    }



}
package com.zebra.demo.rfidreader.settings;


import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.zebra.demo.rfidreader.common.Constants;


/**
 * Adapter for showing prefilters(2 tabs)
 */
public class PreFilterAdapter extends FragmentStatePagerAdapter {
    public final static int NO_OF_TABS = 2;

    /**
     * Constructor. Handles the initialization.
     *
     * @param fm - Fragment Manager to be used for handling fragments.
     */
    public PreFilterAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Constants.logAsMessage(Constants.TYPE_DEBUG, getClass().getSimpleName(), "1st Tab Selected");
                return PreFilter1ContentFragment.newInstance();
            case 1:
                Constants.logAsMessage(Constants.TYPE_DEBUG, getClass().getSimpleName(), "2nd Tab Selected");
                return PreFilter2ContentFragment.newInstance();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NO_OF_TABS;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        // Tab titles
        String[] tabs = {"Filter 1", "Filter 2"};
        return tabs[position];
    }
}

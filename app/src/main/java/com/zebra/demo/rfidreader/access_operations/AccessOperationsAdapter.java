package com.zebra.demo.rfidreader.access_operations;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import 	androidx.viewpager.widget.PagerAdapter;
import android.util.Log;
import android.view.ViewGroup;
import java.util.HashMap;

/**
 * Class to handle the details about access operations(No of Tabs, Class acting as UI for the tabs) etc..
 */
public class AccessOperationsAdapter extends FragmentStatePagerAdapter {
    private static final int NO_OF_TABS = 3;

    //Map to hold the references for currently active fragments so that we can acess them
    private HashMap<Integer, Fragment> currentlyActiveFragments;

    /**
     * Constructor. Handles the initialization
     *
     * @param fm - FragmentManager instance to be used for handling fragments
     */
    public AccessOperationsAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {
        if (currentlyActiveFragments == null)
            currentlyActiveFragments = new HashMap<>();

        Fragment fragment;

        switch (index) {
            case 0:
                Log.d(getClass().getSimpleName(), "1st Tab Selected");
                fragment = AccessOperationsReadWriteFragment.newInstance();
                break;
            case 1:
                Log.d(getClass().getSimpleName(), "2nd Tab Selected");
                fragment = AccessOperationsLockFragment.newInstance();
                break;
            case 2:
                Log.d(getClass().getSimpleName(), "3rd Tab Selected");
                fragment = AccessOperationsKillFragment.newInstance();
                break;
            default:
                fragment = null;
                break;
        }

        //Store the reference
        currentlyActiveFragments.put(index, fragment);
        return fragment;
    }

    /**
     * Get the active fragment at the given position
     *
     * @param key - Index to be used for fetching the fragment
     * @return - {@link androidx.core.app.Fragment} at the given index
     */
    public Fragment getFragment(int key) {
        if (currentlyActiveFragments != null)
            return currentlyActiveFragments.get(key);
        else
            return null;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        //Remove the reference
        currentlyActiveFragments.remove(position);
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return NO_OF_TABS;
    }

    @Override
    public int getItemPosition(Object object) {
        if (object instanceof AccessOperationsFragment.OnRefreshListener) {
            ((AccessOperationsFragment.OnRefreshListener) object).onRefresh();
        }
        return PagerAdapter.POSITION_NONE;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String[] tabs = {"Read \\ Write", "Lock", "Kill"};
        return tabs[position];
    }
}


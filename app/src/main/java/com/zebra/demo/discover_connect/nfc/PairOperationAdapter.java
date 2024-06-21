package com.zebra.demo.discover_connect.nfc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.PagerAdapter;

import com.zebra.demo.R;
import com.zebra.demo.rfidreader.reader_connection.CameraScanFragment;
import com.zebra.demo.rfidreader.reader_connection.ScanAndPairFragment;
import com.zebra.demo.rfidreader.reader_connection.ScanBarcodeAndPairFragment;
import com.zebra.demo.rfidreader.rfid.RFIDController;

import java.util.HashMap;

/**
 * Class to handle the details about pair operations(No of Tabs, Class acting as UI for the tabs) etc..
 */
public class PairOperationAdapter extends FragmentStatePagerAdapter {
    private static final int NO_OF_TABS = 4;
    String[] tabs = {"Tap ", "Scan ", "Barcode", "Camera"};
    int[] icons = {R.drawable.ic_tap_and_pair ,R.drawable.ic_scan_and_pair,R.drawable.ic_sample_barcode, R.drawable.ic_scan_and_pair};
    Context mContext;
    //Map to hold the references for currently active fragments so that we can acess them
    private HashMap<Integer, Fragment> currentlyActiveFragments;

    /**
     * Constructor. Handles the initialization
     *
     * @param fm - FragmentManager instance to be used for handling fragments
     */
    public PairOperationAdapter(Context ctx , FragmentManager fm) {
        super(fm);
        this.mContext =ctx;

    }

    @Override
    public Fragment getItem(int index) {

        if (currentlyActiveFragments == null)
            currentlyActiveFragments = new HashMap<>();

        Fragment fragment;

        switch (index) {
            case 0:
               // if(RFIDController.mConnectedReader.ReaderCapabilities.getModelName().contains("RFD8500")== true) {
                //    fragment = NoNfcFragment.newInstance();
                //} else
                {
                    Log.d(getClass().getSimpleName(), "1st Tab Selected");
                    fragment = ScanAndPairFragment.newInstance();
                    Bundle nfc_scan = new Bundle();
                    nfc_scan.putBoolean("nfc_pair", true);
                    fragment.setArguments(nfc_scan);
                }
                break;
            case 1:
                Log.d(getClass().getSimpleName(), "2nd Tab Selected");
                fragment = ScanAndPairFragment.newInstance();
                Bundle bt_scan = new Bundle();
                bt_scan.putBoolean("bt_pair", true);
                fragment.setArguments(bt_scan);
                break;
            case 2:
                    fragment = ScanBarcodeAndPairFragment.newInstance();
                break;
            case 3:
                fragment = CameraScanFragment.newInstance();
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
     * @return - {@link Fragment} at the given index
     */
    public Fragment getFragment(int key) {
        if (currentlyActiveFragments != null) {
            return currentlyActiveFragments.get(key);
        }
        else {
            return null;
        }
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
        return PagerAdapter.POSITION_NONE;
    }

    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        String title=tabs[position];
        int resId= icons[position];
        Drawable titleIcon = ContextCompat.getDrawable(mContext,resId);
        titleIcon.setBounds(0,0,titleIcon.getIntrinsicWidth(),titleIcon.getIntrinsicHeight());
        SpannableString spannable = new SpannableString("  "+title);
        ImageSpan imageSpan = new ImageSpan(titleIcon, ImageSpan.ALIGN_BOTTOM);
        spannable.setSpan(new RelativeSizeSpan((float) 0.74),2 ,title.length()+2,  Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(R.color.black_overlay), 0, title.length()+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(imageSpan, 0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;


       // return tabs[position];
    }


}


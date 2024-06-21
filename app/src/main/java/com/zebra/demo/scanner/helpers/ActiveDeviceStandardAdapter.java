package com.zebra.demo.scanner.helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.discover_connect.nfc.PairOperationsFragment;
import com.zebra.demo.rfidreader.access_operations.AccessOperationsFragment;
import com.zebra.demo.rfidreader.inventory.RFIDInventoryFragment;
import com.zebra.demo.rfidreader.locate_tag.LocateOperationsFragment;
import com.zebra.demo.rfidreader.manager.ManagerFragment;
import com.zebra.demo.rfidreader.manager.mainSettingsFragment;
import com.zebra.demo.rfidreader.rapidread.RapidReadFragment;
import com.zebra.demo.rfidreader.reader_connection.RFIDReadersListFragment;
import com.zebra.demo.rfidreader.settings.PreFilterFragment;
import com.zebra.demo.rfidreader.settings.SettingListFragment;
import com.zebra.demo.scanner.fragments.AdvancedFragment;


import static com.zebra.demo.scanner.helpers.Constants.DEBUG_TYPE.TYPE_DEBUG;

public class ActiveDeviceStandardAdapter extends ActiveDeviceAdapter {
    private final FragmentManager mFragmentManager;
    private final int mFunctionCount;



    ///String[] tabs = {"Readers","RFID","Scan","Settings",};
    String[] tabs = {"Readers","RFID","Settings",};
    int[] icons = {R.drawable.ic_rfid_reader,R.drawable.ic_rfid_tab,R.drawable.ic_tab_settings};
    private Context mContext;

    /**
     * Constructor. Handles the initialization.
     * @param fm - Fragment Manager to be used for handling fragments.
     */
    public ActiveDeviceStandardAdapter(Context ctx,FragmentManager fm, int fCount) {
        super(fm, fCount);
        mContext = ctx;
        mFragmentManager = fm;
        mFunctionCount = fCount;

    }



    @Override
    public CharSequence getPageTitle(int position) {
        String title=tabs[position];
        int resId= icons[position];
        Drawable titleIcon = ContextCompat.getDrawable(mContext,resId);
        titleIcon.setBounds(0,0,titleIcon.getIntrinsicWidth(),titleIcon.getIntrinsicHeight());
        SpannableString spannable = new SpannableString(" " + "\n"+title);
        ImageSpan imageSpan = new ImageSpan(titleIcon, ImageSpan.ALIGN_BOTTOM);
        spannable.setSpan(new ForegroundColorSpan(Color.WHITE), 0, title.length()+2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(imageSpan, 0,1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        //super.getItemPosition(object);
        //if( object  instanceof  BarcodeFargment)
        //    return POSITION_UNCHANGED;

        //super.getItemPosition(object);
        //if( object  instanceof  BarcodeFargment)
        //    return POSITION_UNCHANGED;

        //return POSITION_UNCHANGED;
        return POSITION_NONE;
    }



    @NonNull

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // fragment TAG = "android:switcher:" + container.getId + ":" + position;
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }






}

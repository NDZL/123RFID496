package com.zebra.demo.rfidreader.access_operations;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.DeviceDiscoverActivity;
import com.zebra.demo.rfidreader.home.RFIDBaseActivity;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.TagData;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.demo.application.Application;

import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.INVENTORY_TAB;


/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 * <p/>
 * Use the {@link AccessOperationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to act as a Holder for Access Tabs(Read/Write, Lock and Kill)
 */
public class AccessOperationsFragment extends Fragment {
    private ViewPager viewPager;
    private AccessOperationsAdapter mAdapter;
    private ActionBar actionBar;
    private int accessOperationCount = -1;
    private boolean rwAdvancedOptions = false;

    public AccessOperationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccessOperationsFragment.
     */
    public static AccessOperationsFragment newInstance() {
        return new AccessOperationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_advanced_option, menu);

        menu.findItem( R.id.action_inventory).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ((ActiveDeviceActivity) getActivity()).loadNextFragment(INVENTORY_TAB);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_inventory :
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_access_operations, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialization
        viewPager = (ViewPager) getActivity().findViewById(R.id.accessOperationsPager);
        mAdapter = new AccessOperationsAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        //
        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        rwAdvancedOptions = settings.getBoolean(Constants.ACCESS_ADV_OPTIONS, false);


    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        accessOperationCount = -1;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        RFIDController.isAccessCriteriaRead = false;
        RFIDBaseActivity.setAccessProfile(false);
    }

    public void handleTagResponse(TagData tagData) {
        if (mAdapter != null && viewPager != null) {
            Fragment fragment = mAdapter.getFragment(viewPager.getCurrentItem());
            if (fragment != null && fragment instanceof AccessOperationsReadWriteFragment) {
                ((AccessOperationsReadWriteFragment) fragment).handleTagResponse(tagData);
            }
        }
    }

    /**
     * Method to fetch one of (Read/Write, Lock or Kill) fragments currently being displayed
     *
     * @return - {@link androidx.fragment.app.Fragment} instance
     */
    public Fragment getCurrentlyViewingFragment() {
        if (mAdapter != null && viewPager != null) {
            return mAdapter.getFragment(viewPager.getCurrentItem());
        } else {
            return null;
        }
    }

    public void RFIDReaderDisappeared(ReaderDevice device) {
        Intent intent;
        intent = new Intent(getActivity(), DeviceDiscoverActivity.class);
        intent.putExtra("enable_toolbar", false);
        //startActivity(intent);
        getActivity().finish();
    }


    /**
     * interface to maintain last entered access tag id in access control fragments
     */
    public interface OnRefreshListener {
        /**
         * method to update accessControlTag value
         */
        void onUpdate();

        /**
         * method to refresh the fragment details with updated tag id
         */
        void onRefresh();
    }
}

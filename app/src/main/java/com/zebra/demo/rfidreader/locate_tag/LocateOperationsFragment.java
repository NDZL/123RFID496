package com.zebra.demo.rfidreader.locate_tag;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.ResponseHandlerInterfaces;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.demo.rfidreader.locate_tag.multitag_locate.MultiTagLocateFragment;

import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.INVENTORY_TAB;

/**
 * A simple {@link androidx.fragment.app.Fragment} subclass.
 * <p/>
 * Use the {@link LocateOperationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 * <p/>
 * Fragment to act as a Holder for Locate Tabs(SingleTag, MultiTag)
 */
public class LocateOperationsFragment extends Fragment implements ResponseHandlerInterfaces.TriggerEventHandler, ResponseHandlerInterfaces.ResponseStatusHandler {
    private ViewPager viewPager;
    private LocateOperationAdapter mAdapter;
    private ActionBar actionBar;

    public LocateOperationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AccessOperationsFragment.
     */
    public static LocateOperationsFragment newInstance() {
        return new LocateOperationsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_locate_tag, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_inventory :
                ((ActiveDeviceActivity)getActivity()).loadNextFragment(INVENTORY_TAB);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_locate_tag, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialization
        viewPager = (ViewPager) getActivity().findViewById(R.id.locateTagPager);
        mAdapter = new LocateOperationAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        //
/*        SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
        showAdvancedOptions = settings.getBoolean(Constants.ACCESS_ADV_OPTIONS, false);*/
    }

/*    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        accessOperationCount = -1;
    }*/

    @Override
    public void onDetach() {
        super.onDetach();
/*        RFIDController.isAccessCriteriaRead = false;
        setAccessProfile(false);*/
    }

    public void handleLocateTagResponse() {
        if (mAdapter != null && viewPager != null) {
            Fragment fragment = mAdapter.getFragment(viewPager.getCurrentItem());
            if (fragment != null) {
                if (fragment instanceof MultiTagLocateFragment) {
                    ((MultiTagLocateFragment) fragment).updateTagItemList();
                } else if (fragment instanceof SingleTagLocateFragment) {
                    ((SingleTagLocateFragment) fragment).handleLocateTagResponse();
                }
            }
        }
    }

    @Override
    public void triggerPressEventRecieved() {
        if (mAdapter != null && viewPager != null) {
            Fragment fragment = mAdapter.getFragment(viewPager.getCurrentItem());
            if (fragment != null) {
                if (fragment instanceof MultiTagLocateFragment) {
                    ((MultiTagLocateFragment) fragment).triggerPressEventRecieved();
                } else if (fragment instanceof SingleTagLocateFragment) {
                    ((SingleTagLocateFragment) fragment).triggerPressEventRecieved();
                }
            }
        }
    }

    @Override
    public void triggerReleaseEventRecieved() {
        if (mAdapter != null && viewPager != null) {
            Fragment fragment = mAdapter.getFragment(viewPager.getCurrentItem());
            if (fragment != null) {
                if (fragment instanceof MultiTagLocateFragment) {
                    ((MultiTagLocateFragment) fragment).triggerReleaseEventRecieved();
                } else if (fragment instanceof SingleTagLocateFragment) {
                    ((SingleTagLocateFragment) fragment).triggerReleaseEventRecieved();
                }
            }
        }
    }

    public void resetMultiTagLocateDetail(boolean isDeviceDisconnected) {
        if (mAdapter != null && viewPager != null) {
            Fragment fragment = mAdapter.getFragment(viewPager.getCurrentItem());
            if (fragment != null && fragment instanceof MultiTagLocateFragment) {
                ((MultiTagLocateFragment) fragment).resetMultiTagLocateDetail(isDeviceDisconnected);
            }
        }
    }

    /**
     * Method to fetch one of (SingleTag / MultiTag) fragments currently being displayed
     *
     * @return - {@link Fragment} instance
     */
    public Fragment getCurrentlyViewingFragment() {
        if (mAdapter != null && viewPager != null) {
            return mAdapter.getFragment(viewPager.getCurrentItem());
        } else {
            return null;
        }
    }

    @Override
    public void handleStatusResponse(RFIDResults results) {
        if (mAdapter != null && viewPager != null) {
            Fragment fragment = mAdapter.getFragment(viewPager.getCurrentItem());
            if (fragment != null) {
                if (fragment instanceof MultiTagLocateFragment) {
                    ((MultiTagLocateFragment) fragment).handleStatusResponse(results);
                } else if (fragment instanceof SingleTagLocateFragment) {
                    ((SingleTagLocateFragment) fragment).handleStatusResponse(results);
                }
            }
        }
    }

    public void resetLocationingDetails(boolean flag) {
        if (mAdapter != null && viewPager != null) {
            Fragment fragment = mAdapter.getFragment(viewPager.getCurrentItem());
            if (fragment != null) {
                if (fragment instanceof SingleTagLocateFragment) {
                    ((SingleTagLocateFragment) fragment).resetLocationingDetails(flag);
                }
            }
        }
    }

    public void enableGUIComponents(boolean flag) {
        if (mAdapter != null && viewPager != null) {
            Fragment fragment = mAdapter.getFragment(viewPager.getCurrentItem());
            if (fragment != null) {
                if (fragment instanceof MultiTagLocateFragment) {
                    ((MultiTagLocateFragment) fragment).enableGUIComponents(flag);
                }
            }
        }
    }

    public void handleReaderDisapeared() {
        if (mAdapter != null && viewPager != null) {
            Fragment fragment = mAdapter.getFragment(viewPager.getCurrentItem());
            if (fragment != null) {
                if (fragment instanceof MultiTagLocateFragment) {
                    ((MultiTagLocateFragment) fragment).handleReaderDisapeared();
                }
            }
        }
    }


    /**
     * interface to maintain locate tag id in locate tag fragments
     */
    public interface OnRefreshListener {
        /**
         * method to update locate tag value
         */
        void onUpdate();

        /**
         * method to refresh the fragment details with updated tag id
         */
        void onRefresh();
    }
}

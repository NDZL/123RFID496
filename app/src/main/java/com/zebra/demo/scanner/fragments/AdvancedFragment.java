package com.zebra.demo.scanner.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;

import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SCAN_DATAVIEW_TAB;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdvancedFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class AdvancedFragment extends Fragment {
    private View advancedFragmentView;
    public static AdvancedFragment newInstance() {
        return new AdvancedFragment();
    }
    public AdvancedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
 //       inflater.inflate(R.menu.menu_scan_settings, menu);
//        menu.findItem( R.id.action_scan).setOnMenuItemClickListener( new MenuItem.OnMenuItemClickListener() {
//            @Override
//            public boolean onMenuItemClick(MenuItem item) {
//                ((ActiveDeviceActivity) getActivity()).loadNextFragment(SCAN_DATAVIEW_TAB);
//                return true;
//            }
//        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        advancedFragmentView  = inflater.inflate(R.layout.fragment_advanced, container, false);
        return advancedFragmentView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}

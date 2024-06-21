package com.zebra.demo.rfidreader.manager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;

public class ScanHomeSettingsFragment extends Fragment {
    private static ScanHomeSettingsFragment scanHomeSettingsFragment = null;
    private View ScanHomeSettingsFragmentView;

    public static Fragment newInstance() {
        return new ScanHomeSettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ScanHomeSettingsFragmentView = inflater.inflate(R.layout.fragment_scan_main_settings, container, false);
        ((ActiveDeviceActivity)getActivity()).getSupportActionBar().setTitle("SCAN Settings");
        return ScanHomeSettingsFragmentView;
    }
}

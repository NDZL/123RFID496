package com.zebra.demo.wifi;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.ENUM_WIFI_STATE;
import com.zebra.rfid.api3.ENUM_WIFI_STATUS;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.RFIDWifi;
import com.zebra.rfid.api3.WifiProfile;
import com.zebra.rfid.api3.WifiScanData;

import java.util.ArrayList;
import java.util.List;

public class ReaderWifiSettingsFragment extends Fragment{

    String TAG =  "ReaderWifiSettingsFragment";;
    RecyclerView rvOtherAvailableNetworks, rvSavedWifiNetworks;
    public static Context mContext;
    AvailableWifiNetworkAdapter availableNetworkAdapter;
    public static SavedWifiNetworksAdapter savedWifiNetworksAdapter;
    List<WifiScanData> results  = new ArrayList<>();
    ImageButton btn_scannetworks;
    public static RelativeLayout rl_connectedWiFi;
    public static TextView tv_connected_wifi_name;
    ImageView ivWifiOptions ,ivConnectedWifiIcon;
    static List<SavedWifiInfo> savedWifiInfo = new ArrayList<SavedWifiInfo>();
    static List<WifiProfile> profilelist = new ArrayList<>();
    private static ReaderWifiSettingsFragment readerWifiSettingsFragment = null;
    public static FragmentManager fragmentManager ;
    private Animation animation;


    public ReaderWifiSettingsFragment() {
        // Required empty public constructor
    }

    public static ReaderWifiSettingsFragment newInstance () {

        return new ReaderWifiSettingsFragment();
    }
    public static ReaderWifiSettingsFragment getInstance() {
        if (readerWifiSettingsFragment == null)
            readerWifiSettingsFragment = new ReaderWifiSettingsFragment();
        return readerWifiSettingsFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_reader_wifi_settings, container, false);
        rvOtherAvailableNetworks = view.findViewById(R.id.rv_other_available_networks);
        rvSavedWifiNetworks = view.findViewById(R.id.rv_saved_wifi_networks);
        rl_connectedWiFi = view.findViewById(R.id.connected_wifi);
        btn_scannetworks = view.findViewById(R.id.btn_scannetworks);
        tv_connected_wifi_name = view.findViewById(R.id.connected_wifi_name);
        ivWifiOptions = view.findViewById(R.id.connected_wifi_options);
        ivConnectedWifiIcon = view.findViewById(R.id.connected_wif_icon);
        mContext = getActivity();
        fragmentManager = getChildFragmentManager();

        animation = new RotateAnimation(0.0f, 360.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        animation.setRepeatCount(-1);
        animation.setDuration(2000);

        ivWifiOptions.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(getActivity(), ivWifiOptions);
            menu.inflate(R.menu.wifi_connect_menu);
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.wifi_disconnect:
                        disconnectWifi();
                        break;
                }
                return false;
            });
            menu.show();
        });
        rl_connectedWiFi.setOnClickListener(v -> disconnectWifi());

        try {
            if (Application.mConnectedReader != null || RFIDController.mConnectedReader.Config != null) {
                RFIDWifi rfidWifi = new RFIDWifi();
                RFIDResults rfidResults = RFIDController.mConnectedReader.Config.wifi_getStatus(rfidWifi);
                if(rfidResults == RFIDResults.RFID_API_SUCCESS && rfidWifi.getWifiStatus()!=null) {
                    Log.d(TAG,  " "+rfidWifi.getWifiStatus().name());
                    if (rfidWifi.getWifiStatus().equals(ENUM_WIFI_STATUS.STATUS_DISABLED)) {
                        RFIDController.mConnectedReader.Config.wifi_enable();
                    }
                }
            }
        } catch (InvalidUsageException | OperationFailureException e) {
            Log.d(TAG,  "Returned SDK Exception wifi_getStatus, wifi_enable");
        }

        availableNetworkAdapter = new AvailableWifiNetworkAdapter( getActivity(),results);
        rvOtherAvailableNetworks.setAdapter(availableNetworkAdapter);

        loadSavedData();
        savedWifiNetworksAdapter = new SavedWifiNetworksAdapter(getActivity(),results,profilelist/*,"connectedreader"*/);
        rvSavedWifiNetworks.setAdapter(savedWifiNetworksAdapter);

        for(WifiProfile profile:profilelist) {
            if(profile.getstate() != null && profile.getstate().equals(ENUM_WIFI_STATE.STATE_CONNECTED)) {
                rl_connectedWiFi.setVisibility(View.VISIBLE);
                tv_connected_wifi_name.setText(profile.getssid());
            }
        }

        btn_scannetworks.setOnClickListener(v -> {
            try {
                results.clear();
                availableNetworkAdapter.notifyDataSetChanged();
                RFIDController.mConnectedReader.Config.wifi_scan();
            } catch (InvalidUsageException | OperationFailureException e) {
                Log.d(TAG,  "Returned SDK Exception wifi_scan");
            }
        });

        Thread tScan = new Thread(() -> {
            try {
                RFIDController.mConnectedReader.Config.wifi_scan();
            } catch (InvalidUsageException | OperationFailureException e) {
                Log.d(TAG,  "Returned SDK Exception wifi_scan in thread");
            }
        });
        tScan.setPriority(Thread.MAX_PRIORITY);
        tScan.start();

        return view;
    }

    private void disconnectWifi() {

        try {
            RFIDController.mConnectedReader.Config.wifi_disconnect();
        } catch (InvalidUsageException | OperationFailureException e) {
            Log.d(TAG,  "Returned SDK Exception wifi_disconnect");
        }
        Handler handler = new Handler();
        handler.postDelayed(() -> {
            rl_connectedWiFi.setVisibility(View.GONE);
        },1000);

    }

    private void loadSavedData() {

        profilelist.clear();
        try {
            profilelist = RFIDController.mConnectedReader.Config.wifi_listProfile();
            for(WifiProfile profile:profilelist) {
                if(profile.getstate() != null && profile.getstate().equals(ENUM_WIFI_STATE.STATE_CONNECTED)) {
                    rl_connectedWiFi.setVisibility(View.VISIBLE);
                    tv_connected_wifi_name.setText(profile.getssid());
                }
            }

        } catch (InvalidUsageException | OperationFailureException e) {
            Log.d(TAG,  "Returned SDK Exception wifi_listProfile");
        }finally {
            Handler handler = new Handler();
            handler.postDelayed(() -> {
                if(savedWifiNetworksAdapter != null){
                    savedWifiNetworksAdapter.notifyDataSetChanged();
                }
            },1000);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        results.clear();
    }

    public void ConnectWifi(int pos, List<WifiProfile> savedWifiInfo) {
        try {
            RFIDController.mConnectedReader.Config.wifi_connectNonroaming(savedWifiInfo.get(pos).getssid());
        } catch (InvalidUsageException | OperationFailureException e) {
            Log.d(TAG,  "Returned SDK Exception wifi_connectNonroaming");
        }

        try {
            profilelist.clear();
            profilelist = RFIDController.mConnectedReader.Config.wifi_listProfile();

        } catch (InvalidUsageException | OperationFailureException e) {
            Log.d(TAG,  "Returned SDK Exception wifi_listProfile");
        }

        Handler handler = new Handler();
        handler.postDelayed(() -> {

            for (WifiProfile profile : profilelist) {
                if (profile.getstate().toString().equals("STATE_CONNECTED")) {
                    rl_connectedWiFi.setVisibility(View.VISIBLE);
                    tv_connected_wifi_name.setText(profile.getssid());
                    savedWifiNetworksAdapter.notifyDataSetChanged();
                }

            }
        },1000);
    }
    public void openDialog(WifiScanData saveNetworkList) {

        WifiPasswordDialog wifiPasswordDialog = new WifiPasswordDialog(mContext,saveNetworkList.getssid());
        wifiPasswordDialog.show(fragmentManager,"password_dialog");
    }

    public void readWifiScanNotification(String scanStatus) {
        Log.d(TAG," readWifiScanNotification "+scanStatus);

        requireActivity().runOnUiThread(() -> {
            switch (scanStatus) {
                case "ScanStart":
                    btn_scannetworks.startAnimation(animation);
                    break;
                case "connect":
                case "disconnect":
                    break;
                case "ScanStop":
                    btn_scannetworks.clearAnimation();
                    loadSavedData();
                    break;
            }
            availableNetworkAdapter.notifyDataSetChanged();
        });
    }

    public void DeleteProfile(int pos, List<WifiProfile> wifiInfo) {
        try {

            RFIDController.mConnectedReader.Config.wifi_deleteProfile(wifiInfo.get(pos).getssid());

        } catch (InvalidUsageException | OperationFailureException e) {
            Log.d(TAG, "Returned SDK Exception wifi_deleteProfile");
        } finally {
            loadSavedData();
        }
    }

    public void updateScanResult(WifiScanData wifiscandata) {
        results.add(wifiscandata);
    }

    public void addProfile(String password, String wifiName) {
        WifiProfile wifiprofile = new WifiProfile();
        wifiprofile.setssid(wifiName);
        wifiprofile.setpassword(password);

        try {
            RFIDController.mConnectedReader.Config.wifi_addProfile(wifiprofile);
        } catch (InvalidUsageException | OperationFailureException e) {
            Log.d(TAG,  "Returned SDK Exception wifi_addProfile");
        }finally {
            loadSavedData();
        }
    }
}
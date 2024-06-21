package com.zebra.demo.rfidreader.settings;


import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.MAIN_RFID_SETTINGS_TAB;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.zebra.ASCII_SDK.Response_WifiConfig;
import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.CustomProgressDialog;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.ENUM_WIFI_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class WifiFragment extends BackPressedFragment {
    Context context;
    SharedPreferences mSharedPreferences;
    private CheckBox checkBoxWifi;
    private ProgressDialog progressDialog;
    private static final String TAG = "WifiFragment";


    private WifiFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment WifiFragment.
     */
    public static WifiFragment newInstance() {
        return new WifiFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootview = inflater.inflate(R.layout.fragment_wifi, container, false);

        context = rootview.getContext();
        checkBoxWifi = rootview.findViewById(R.id.checkboxwifi);
        String state = null;
        ENUM_WIFI_STATE wifiState = RFIDController.mConnectedReader.Config.getWifiState(state);
        if(wifiState == ENUM_WIFI_STATE.STATE_ON)
            checkBoxWifi.setChecked(true);

        return rootview;
    }

    @Override
    public void onBackPressed() {
        saveWiFiState();
    }

    private void saveWiFiState(){
        progressDialog = new CustomProgressDialog(getActivity(), getString(R.string.wifi_settings));
        progressDialog.show();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
               RFIDController.mConnectedReader.Config.wifi_poweron(checkBoxWifi.isChecked());
            } catch (InvalidUsageException | OperationFailureException e) {
                if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            }
            handler.post(() -> {
                progressDialog.hide();
                Toast.makeText(context, R.string.status_success_message, Toast.LENGTH_SHORT).show();
                if(getActivity() instanceof SettingsDetailActivity)
                    ((SettingsDetailActivity) getActivity()).callBackPressed();
                if(getActivity() instanceof ActiveDeviceActivity) {
                    ((ActiveDeviceActivity) getActivity()).callBackPressed();
                    ((ActiveDeviceActivity) getActivity()).loadNextFragment(MAIN_RFID_SETTINGS_TAB);
                }
            });
        });
    }

    /**
     * method to update battery screen when device got disconnected
     */
    public void deviceDisconnected() {
        checkBoxWifi.setChecked(false);
    }
}

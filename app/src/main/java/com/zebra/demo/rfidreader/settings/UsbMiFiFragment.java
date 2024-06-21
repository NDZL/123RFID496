package com.zebra.demo.rfidreader.settings;


import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.MAIN_RFID_SETTINGS_TAB;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.CustomProgressDialog;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class UsbMiFiFragment extends BackPressedFragment {
    Context context;
    private CheckBox checkBoxMiFi;
    private ProgressDialog progressDialog;
    private boolean isUsbMiFiEnabled;
    private static final String TAG = "UsbMiFiFragment";


    private UsbMiFiFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment UsbMiFiFragment.
     */
    public static UsbMiFiFragment newInstance() {
        return new UsbMiFiFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_usb_mifi, container, false);
        context = view.getContext();
        checkBoxMiFi = view.findViewById(R.id.check_box_usb_mifi);
        TextView tv = view.findViewById(R.id.mifi_mesg);

        if(!RFIDController.mConnectedReader.getHostName().startsWith("RFD40") &&
                !RFIDController.mConnectedReader.getHostName().startsWith("RFD90")) {
            tv.setVisibility(View.VISIBLE);
            checkBoxMiFi.setEnabled(false);
            checkBoxMiFi.setVisibility(View.INVISIBLE);

        }else{
            tv.setVisibility(View.VISIBLE);
            checkBoxMiFi.setEnabled(true);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            isUsbMiFiEnabled = RFIDController.mConnectedReader.Config.isUsbMiFiEnabled();
        } catch (OperationFailureException | InvalidUsageException e) {
            if(e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        }
        checkBoxMiFi.setChecked(isUsbMiFiEnabled);
    }

    @Override
    public void onBackPressed() {
        if(isUsbMiFiEnabled != checkBoxMiFi.isChecked()) {
            saveMiFiState();
        }else{
            if(getActivity() instanceof SettingsDetailActivity)
                ((SettingsDetailActivity) getActivity()).callBackPressed();
            if(getActivity() instanceof ActiveDeviceActivity) {
                ((ActiveDeviceActivity) getActivity()).callBackPressed();
                ((ActiveDeviceActivity) getActivity()).loadNextFragment(MAIN_RFID_SETTINGS_TAB);
            }
        }
    }

    private void saveMiFiState(){
        progressDialog = new CustomProgressDialog(getActivity(), getString(R.string.usb_mifi_settings));
        progressDialog.show();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
               RFIDController.mConnectedReader.Config.setUsbMiFiEnable(checkBoxMiFi.isChecked());
            } catch (InvalidUsageException | OperationFailureException e) {
                if(e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
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

}

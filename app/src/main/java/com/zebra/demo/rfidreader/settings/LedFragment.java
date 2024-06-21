package com.zebra.demo.rfidreader.settings;

/**
 * Created by XJR746 on 09-10-2017.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.rfid.RFIDController;


public class LedFragment extends BackPressedFragment {
    public static final String SHARED_PREF_NAME = "Switch";
    Context context;
    SharedPreferences mSharedPreferences;
    private CheckBox checkboxled;
    private TextView ledText;
    private static final String TAG = "LedFragment";

    //public static final String LEDSTATE = "LED_STATE";
    //Boolean Ledstate;

    public LedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BeeperFragment.
     */
    public static LedFragment newInstance() {
        return new LedFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        final View rootview = inflater.inflate(R.layout.fragment_led, container, false);
        mSharedPreferences = getActivity().getSharedPreferences("LEDPreferences", getContext().MODE_PRIVATE);
        String connectedReader = RFIDController.mConnectedReader.getHostName();
        if (connectedReader.startsWith("RFD40") || connectedReader.startsWith("RFD90") || connectedReader.startsWith("RFD8500")){
            RFIDController.ledState = mSharedPreferences.getBoolean("LED_STATE1", false);
        }
        else{
            RFIDController.ledState = mSharedPreferences.getBoolean("LED_STATE1", true);
        }

        context = rootview.getContext();
        ledText = (TextView) rootview.findViewById(R.id.ledText);
        //RFIDController.AUTO_DETECT_READERS = Ledstate.ge(Constants.AUTO_DETECT_READERS, true);
        checkboxled = (CheckBox) rootview.findViewById(R.id.checkboxled);

        checkboxled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkboxled.isChecked()) {
                    if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected())
                        try {
                            RFIDController.mConnectedReader.Config.setLedBlinkEnable(true);
                        } catch (InvalidUsageException e) {
                            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                        } catch (OperationFailureException e) {
                            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                        }
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putBoolean("LED_STATE1", true);
                    editor.apply();
                } else {
                    try {
                        if (RFIDController.mConnectedReader != null) {
                            RFIDController.mConnectedReader.Config.setLedBlinkEnable(false);
                        }
                    } catch (InvalidUsageException e) {
                        if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                    } catch (OperationFailureException e) {
                        if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                    }
                    SharedPreferences.Editor editor = mSharedPreferences.edit();
                    editor.putBoolean("LED_STATE1", false);
                    editor.apply();
                }
            }
        });
        if (RFIDController.mConnectedReader != null) {
            if (connectedReader.startsWith("RFD40") || connectedReader.startsWith("RFD90")|| connectedReader.startsWith("RFD8500")) {
                ledText.setTextColor(Color.LTGRAY);
                checkboxled.setChecked(false);
               checkboxled.setEnabled(false);
               if(connectedReader.startsWith("RFD8500") == false)
                Toast.makeText(getContext(),"This feature is not supported in "+ connectedReader.substring(0, 5) +" device",Toast.LENGTH_SHORT).show();
               else
                   Toast.makeText(getContext(),"This feature is not supported in "+ connectedReader.substring(0, 7) +" device",Toast.LENGTH_SHORT).show();
               return rootview;
            }

            if (RFIDController.ledState) {
                checkboxled.setChecked(true);
            } else {
                checkboxled.setChecked(false);
            }

        }
        
        return rootview;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onBackPressed() {
        if( getActivity() instanceof SettingsDetailActivity)
            ((SettingsDetailActivity) getActivity()).callBackPressed();
        else if(getActivity() instanceof ActiveDeviceActivity)
            ((ActiveDeviceActivity) getActivity()).callBackPressed();
    }

    /**
     * method to update battery screen when device got disconnected
     */
    public void deviceDisconnected() {
        checkboxled.setChecked(false);
    }
}

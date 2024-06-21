package com.zebra.demo.rfidreader.manager;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.zebra.ASCII_SDK.ASCIIProcessor;
import com.zebra.ASCII_SDK.Command;
import com.zebra.ASCII_SDK.Command_Reset;
import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.CustomProgressDialog;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.ENUM_SERVICE_STATUS;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.RfidStatusEvents;

import static com.zebra.demo.rfidreader.rfid.RFIDController.mIsInventoryRunning;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mReaderDisappeared;

public class DeviceResetFragment  extends Fragment {
    private static final String TAG = "DeviceResetFragment" ;
    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.radioreset_fragment, container, false);
        Button button = (Button) rootview.findViewById(R.id.radioresetbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( (RFIDController.mConnectedReader == null) ||  (RFIDController.mConnectedReader.getHostName().contains("RFD40") != true ) ){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Cannot perform reset if not connected or not RFD40XX", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else if( mIsInventoryRunning == true ) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Operation In Progress-Command Not Allowed", Toast.LENGTH_SHORT).show();
                        }
                    });

                }else {
                    sendCommand(new Command_Reset(), "Reader reset is in progress"/*getString(R.string.reset_progress_title)*/);
                }

            }
        });
        return rootview;
    }

    public static Fragment newInstance() {
        DeviceResetFragment fragment = new DeviceResetFragment();
        return fragment;
    }


    public void sendCommand(Command cmd, String title) {

        try {
            if (((ActiveDeviceActivity) getActivity()).deviceReset(ASCIIProcessor.getCommandString(cmd)) == false) {
                Log.d(TAG, "operation_not_allowed_reader_detached");
            } else {
                progressDialog = new CustomProgressDialog(getActivity(), title);
                progressDialog.show();
                timerDelayRemoveDialog(com.zebra.rfid.api3.Constants.RESPONSE_TIMEOUT, progressDialog, getString(R.string.status_failure_message), true);

            }
        } catch (InvalidUsageException e) {
           Log.d(TAG,  "Returned SDK Exception");
        } catch (OperationFailureException e) {
           Log.d(TAG,  "Returned SDK Exception");
        }
    }

    public void timerDelayRemoveDialog(long time, final Dialog d, final String command, final boolean isPressBack) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if ((getActivity() != null) && (d != null)) {
                    d.dismiss();
                }
            }
        }, time);
    }

    public void onDataReceived(String data) {
        if (data.contains("Command:reset")) {
            handleStatusResponse(data);
        }
    }

    private void handleStatusResponse(String data) {
    }

    public void  RFIDReaderDisappeared(ReaderDevice readerDevice) {
        Toast.makeText(getActivity(), "Device rebooting", Toast.LENGTH_SHORT).show();
        if( progressDialog != null ) {
            progressDialog.cancel();
            progressDialog = null;;
        }
        if(RFIDController.mConnectedDevice != null)
            RFIDController.mReaderDisappeared = RFIDController.mConnectedDevice;
    }

    public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {

    }

    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
    }
}

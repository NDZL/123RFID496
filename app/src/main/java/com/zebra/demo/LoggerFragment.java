package com.zebra.demo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.zebra.ASCII_SDK.ASCIIProcessor;
import com.zebra.ASCII_SDK.Command_Log;
import com.zebra.ASCII_SDK.Response_Status;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.ENUM_SERVICE_STATUS;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.IRFIDLogger;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.OperationFailureException;

public class LoggerFragment extends Fragment {


    public static final String SHARED_PREF_NAME             = "Switch";
    public static final String REALTIMELOGGERSTATE          = "REALTIMELOGGER_STATE";
    public static final String DEBUGLOGGERSTATE             = "DEBUGLOGGER_STATE";
    public static final String NGEERRORLOGSTATE             = "NGEERRORLOGSTATE";
    public static final String NGEEVENTLOGSTATE             = "NGEEVENTLOGSTATE";
    public static final String NGEPACKETLOGSTATE            = "NGEPACKETLOGSTATE";
    public static final String INTERNALRAMLOGSTATE          = "INTERNALRAMLOGSTATE";
    public static final String INTERNALFLASHLOGSTATE        = "INTERNALFLASHLOGSTATE";
    private static final int REQUEST_ID_READ_PERMISSION     = 100;
    private static final int REQUEST_ID_WRITE_PERMISSION    = 200;
    private static final String TAG                         = "RFIDMANAGERAPP-" + LoggerFragment.class.getSimpleName();
    private final String fileName                           = "RfidLog.txt";
    private final String BUFFEREDLOG_PREFIX                 = "Notification:Dump";
    Context mContext;
    Spinner mSprlogger;
    Switch RealtimeLoggerEnableButton, DebugLoggerEnableButton;
    String RealtimeLoggerstate, DebugLoggerstate, Bufferedlogsstate ;
    String NgeErrorlogsstate ,NgeEventlogstate,NgePacketlogstate ;
    private StringBuilder readerLogText;
    private CheckBox chechkboxlogs;
    private CheckBox NgeErrorlogEnableCheckbox;
    private CheckBox NgeEventlogEnableCheckbox;
    private CheckBox NgepacketlogEnableCheckbox;
    private Button retrieveramlogbtn;
    private Button retrieveflashlogbtn;
    private boolean canWrite;
    private boolean loggerstatus;
    private IRFIDLogger rfidLogger;

    public static LoggerFragment newInstance() {
        LoggerFragment fragment = new LoggerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if( RFIDController.mConnectedReader!= null)
            rfidLogger = RFIDController.mConnectedReader.Logger;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootview = inflater.inflate(R.layout.logger_fragment, container, false);
        mContext = rootview.getContext();
        if(RFIDController.mConnectedReader == null )
        {
            Toast.makeText(getActivity(), "Reader not connected", Toast.LENGTH_SHORT).show();

        }

        // setting switch button for application debug logging
        DebugLoggerEnableButton = (Switch) rootview.findViewById(R.id.enablelogger_switch);
        // setting switch button for real time logs
        RealtimeLoggerEnableButton = (Switch) rootview.findViewById(R.id.realtimelogger_switch);
        // Checkbox for enabling nge error logs
        NgeErrorlogEnableCheckbox = (CheckBox) rootview.findViewById(R.id.ngeerror_checkBox);
        // Checkbox for enabling nge event logs
        NgeEventlogEnableCheckbox = (CheckBox) rootview.findViewById(R.id.ngeeventlog_checkBox);
        // Checkbox for enabling nge packet logs
        NgepacketlogEnableCheckbox = (CheckBox) rootview.findViewById(R.id.ngepacketlogging_checkBox);
        // Checkbox for retrieving internal ram logs
        retrieveramlogbtn = (Button)rootview.findViewById(R.id.ramlogbutton);
        // Checkbox for retrieving internal flash logs
        retrieveflashlogbtn = (Button)rootview.findViewById(R.id.flashlogbutton);

        // get real time logger state from RFID service
        if(rfidLogger != null ) {

            RealtimeLoggerstate = rfidLogger.getLogConfig(REALTIMELOGGERSTATE);
            DebugLoggerstate = rfidLogger.getLogConfig(DEBUGLOGGERSTATE);
            NgeErrorlogsstate = rfidLogger.getLogConfig(NGEERRORLOGSTATE);
            NgeEventlogstate = rfidLogger.getLogConfig(NGEEVENTLOGSTATE);
            NgePacketlogstate = rfidLogger.getLogConfig(NGEPACKETLOGSTATE);

            // debug out put
            Log.d("TAG", "RealtimeLoggerState: " + RealtimeLoggerstate + ", DebugLoggerState: "
                    + DebugLoggerstate + ", BufferedLogState: " + Bufferedlogsstate);
            if (RealtimeLoggerstate.equalsIgnoreCase("ON")) {
                RealtimeLoggerEnableButton.setChecked(true);
            } else {
                RealtimeLoggerEnableButton.setChecked(false);
            }
            RealtimeLoggerEnableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    rfidLogger.setLogConfig(REALTIMELOGGERSTATE, isChecked);
                    try {
                        rfidLogger.setRfidReaderLog(REALTIMELOGGERSTATE, isChecked);
                    } catch (OperationFailureException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    } catch (InvalidUsageException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    }
                }

            });

            if (DebugLoggerstate.equalsIgnoreCase("ON")) {
                DebugLoggerEnableButton.setChecked(true);
            } else {
                DebugLoggerEnableButton.setChecked(false);
            }
            DebugLoggerEnableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    rfidLogger.setLogConfig(DEBUGLOGGERSTATE, isChecked);
                    rfidLogger.EnableDebugLogs(isChecked);
                }

            });

            if (NgeErrorlogsstate.equalsIgnoreCase("ON")) {
                NgeErrorlogEnableCheckbox.setChecked(true);
            } else {
                NgeErrorlogEnableCheckbox.setChecked(false);
            }
            NgeErrorlogEnableCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override

                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                    rfidLogger.setLogConfig(NGEERRORLOGSTATE, isChecked);
                    try {
                        rfidLogger.setRfidReaderLog(NGEERRORLOGSTATE, isChecked);
                    } catch (OperationFailureException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    } catch (InvalidUsageException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    }
                }

            });

            if (NgeEventlogstate.equalsIgnoreCase("ON")) {
                NgeEventlogEnableCheckbox.setChecked(true);
            } else {
                NgeEventlogEnableCheckbox.setChecked(false);
            }
            NgeEventlogEnableCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                    rfidLogger.setLogConfig(NGEEVENTLOGSTATE, isChecked);
                    try {
                        rfidLogger.setRfidReaderLog(NGEEVENTLOGSTATE, isChecked);
                    } catch (OperationFailureException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    } catch (InvalidUsageException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    }
                }


            });

            if (NgePacketlogstate.equalsIgnoreCase("ON")) {
                NgepacketlogEnableCheckbox.setChecked(true);
            } else {
                NgepacketlogEnableCheckbox.setChecked(false);
            }
            NgepacketlogEnableCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    rfidLogger.setLogConfig(NGEPACKETLOGSTATE, isChecked);
                    try {
                        rfidLogger.setRfidReaderLog(NGEPACKETLOGSTATE, isChecked);
                    } catch (OperationFailureException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    } catch (InvalidUsageException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    }
                }
            });

            retrieveramlogbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                        try {
                            retrieveramlogbtn.setEnabled(false);
                            Toast.makeText(getActivity(), "Retrieving internal RAM logs!", Toast.LENGTH_SHORT).show();
                            rfidLogger.getRfidReaderLogs(INTERNALRAMLOGSTATE, true);
                            retrieveramlogbtn.setEnabled(true);
                        } catch (OperationFailureException e) {
                           Log.d(TAG,  "Returned SDK Exception");
                        } catch (InvalidUsageException e) {
                           Log.d(TAG,  "Returned SDK Exception");
                        }
                    }

            });
            retrieveflashlogbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        retrieveflashlogbtn.setEnabled(false);
                        Toast.makeText(getActivity(), "Retrieving internal Flash logs!", Toast.LENGTH_SHORT).show();
                        rfidLogger.getRfidReaderLogs(INTERNALFLASHLOGSTATE, true);
                        retrieveflashlogbtn.setEnabled(true);
                    } catch (OperationFailureException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    } catch (InvalidUsageException e) {
                       Log.d(TAG,  "Returned SDK Exception");
                    }
                }

            });


        }

        if(RFIDController.mConnectedReader == null ||
                ((RFIDController.mConnectedReader != null ) && RFIDController.mConnectedReader.getHostName().startsWith("MC33"))){

       //     if(RFIDController.mConnectedReader != null )
        //        Toast.makeText(getActivity(), "Real time log not supported for MC33", Toast.LENGTH_SHORT).show();

            //chechkboxlogs.setVisibility(View.INVISIBLE);
            RealtimeLoggerEnableButton.setVisibility(View.INVISIBLE);
            DebugLoggerEnableButton.setVisibility(View.INVISIBLE);
            NgeErrorlogEnableCheckbox.setVisibility(View.INVISIBLE);
            NgeEventlogEnableCheckbox.setVisibility(View.INVISIBLE);
            NgepacketlogEnableCheckbox.setVisibility(View.INVISIBLE);
            retrieveramlogbtn.setVisibility(View.INVISIBLE);
            retrieveflashlogbtn.setVisibility(View.INVISIBLE);

        }

        return rootview;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void DisableSettingsUI() {
        RealtimeLoggerEnableButton.setEnabled(false);
    }

    private void EnableSettingsUI() {
        RealtimeLoggerEnableButton.setEnabled(true);
    }

    private void askPermissionAndWriteFile() {
        canWrite = this.askPermission(REQUEST_ID_WRITE_PERMISSION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
       /* if (canWrite) {
            this.writeFile();

        }*/
    }

    // With Android Level >= 23, you have to ask the user
    // for permission with device (For example read/write data on the device).
    private boolean askPermission(int requestId, String permissionName) {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            // Check if we have permission
            int permission = ActivityCompat.checkSelfPermission(getActivity(), permissionName);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // If don't have permission so prompt the user.
                this.requestPermissions(
                        new String[]{permissionName},
                        requestId
                );
                return false;
            }
        }
        return true;
    }

    // When you have the request results
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //
        // Note: If request is cancelled, the result arrays are empty.
        if (grantResults.length > 0) {
            switch (requestCode) {
                case REQUEST_ID_READ_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    }
                }
                case REQUEST_ID_WRITE_PERMISSION: {
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        //new WritefileTask().execute();
                    }
                }
            }
        } else {
            Toast.makeText(getActivity(), "Permission Cancelled!", Toast.LENGTH_SHORT).show();
            Log.d( TAG, "Permission Cancelled!" );
        }
    }



    public void onDataReceived(String data) {
        if (data.contains(Command_Log.commandName.toLowerCase())) {
            handleStatusResponse(data);
        }
        if (data.contains(BUFFEREDLOG_PREFIX)) {
            Log.d(TAG, data);
        }
    }

    public void handleStatusResponse(final String data) {
        Response_Status statusData = Response_Status.FromString(data);
        if (statusData.command.trim().equalsIgnoreCase(Command_Log.commandName)) {
            if (statusData.Status.trim().equalsIgnoreCase("OK")) {
                loggerstatus = true;
            }
        }
    }

    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        //Intent intent;
        //intent = new Intent(getActivity(), DeviceDiscoverActivity.class);
        //intent.putExtra("enable_toolbar", false);
       // startActivity(intent);

        //getActivity().finish();

    }
}


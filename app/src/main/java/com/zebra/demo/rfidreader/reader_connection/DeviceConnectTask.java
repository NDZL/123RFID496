package com.zebra.demo.rfidreader.reader_connection;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.demo.rfidreader.settings.SettingsDetailActivity;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;

import static com.zebra.demo.application.Application.RFIDBAseEventHandler;

/**
 * async task to go for BT connection with reader
 */
public class DeviceConnectTask extends AsyncTask<Void, String, Boolean> {
    private final ReaderDevice connectingDevice;
    private String prgressMsg;
    private OperationFailureException ex;
    private String password;
    private Activity activity;
    IRFIDConnectTaskHandlers taskHandlers;
    private static final String TAG = "DeviceConnectTask";

    DeviceConnectTask(Activity activeActivity, ReaderDevice connectingDevice, String prgressMsg, String Password, IRFIDConnectTaskHandlers handlers) {
        this.connectingDevice = connectingDevice;
        this.prgressMsg = prgressMsg;
        password = Password;
        activity = activeActivity;
        taskHandlers = handlers;


    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        taskHandlers.showProgressDialog(connectingDevice);
        taskHandlers.setConnectionProgressState(true);
    }



    @Override
    protected Boolean doInBackground(Void... a) {
        taskHandlers.CancelReconnect();
        try {
            if (password != null)
                connectingDevice.getRFIDReader().setPassword(password);
            connectingDevice.getRFIDReader().connect();
            if (password != null) {
                SharedPreferences.Editor editor = activity.getSharedPreferences(Constants.READER_PASSWORDS, 0).edit();
                editor.putString(connectingDevice.getName(), password);
                editor.commit();
            }
        } catch (InvalidUsageException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
        } catch (OperationFailureException e) {
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            ex = e;
        }
        if (connectingDevice.getRFIDReader().isConnected()) {
            RFIDController.mConnectedReader = connectingDevice.getRFIDReader();
            taskHandlers.StoreConnectedReader();
            try {
                //RFIDController.mConnectedReader.Events.addEventsListener(RFIDHomeActivity.eventHandler);
                RFIDController.mConnectedReader.Events.removeEventsListener(RFIDBAseEventHandler);
                RFIDController.mConnectedReader.Events.addEventsListener(RFIDBAseEventHandler);
            } catch (InvalidUsageException e) {
                if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            } catch (OperationFailureException e) {
                if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            }


           // if(RFIDController.mConnectedReader.getHostName().startsWith("RFD8500") ||
           //         RFIDController.mConnectedReader.getHostName().startsWith("RFD40P")||
           //         RFIDController.mConnectedReader.getHostName().startsWith("RFD40+"))

            connectingDevice.getRFIDReader().Events.setBatchModeEvent(true);
            connectingDevice.getRFIDReader().Events.setReaderDisconnectEvent(true);
            connectingDevice.getRFIDReader().Events.setBatteryEvent(true);
            connectingDevice.getRFIDReader().Events.setInventoryStopEvent(true);
            connectingDevice.getRFIDReader().Events.setInventoryStartEvent(true);
            RFIDController.mConnectedReader.Events.setTagReadEvent(true);
            RFIDController.mConnectedReader.Events.setHandheldEvent(true);
            RFIDController.mConnectedReader.Events.setWPAEvent(true);
            RFIDController.mConnectedReader.Events.setScanDataEvent(true);
            // if no exception in connect
            if (ex == null) {
                //                        RFIDController.getInstance().updateReaderConnection(false);
            } else {
                RFIDController.clearSettings();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        //if (activity != null && !activity.isFinishing())
        //    taskHandlers.cancelProgressDialog();

        if (ex != null) {
            if (ex.getResults() == RFIDResults.RFID_CONNECTION_PASSWORD_ERROR) {
                taskHandlers.showPasswordDialog(connectingDevice);

                taskHandlers.ReaderDeviceConnected(connectingDevice);
            } else if (ex.getResults() == RFIDResults.RFID_BATCHMODE_IN_PROGRESS) {
                RFIDController.isBatchModeInventoryRunning = true;
                RFIDController.mIsInventoryRunning = true;

                taskHandlers.ReaderDeviceConnected(connectingDevice);
                if (RFIDController.NOTIFY_READER_CONNECTION)
                    taskHandlers.sendNotification(Constants.ACTION_READER_CONNECTED, "Connected to " + connectingDevice.getName());
            } else if (ex.getResults() == RFIDResults.RFID_READER_REGION_NOT_CONFIGURED) {


                RFIDController.regionNotSet = true;
                taskHandlers.sendNotification(Constants.ACTION_READER_SET_REGION, "Please set the region");
                taskHandlers.ReaderDeviceConnected(connectingDevice);
                Intent detailsIntent = new Intent(activity, ActiveDeviceActivity.class);
                detailsIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //detailsIntent.putExtra(Constants.SETTING_ITEM_ID, R.id.regulatory);
                //activity.startActivity(detailsIntent);
            } else if(ex.getResults() == RFIDResults.RFID_COMM_OPEN_ERROR){

                taskHandlers.cancelProgressDialog();
                taskHandlers.ReaderDeviceConnFailed(connectingDevice);

            }else {
                taskHandlers.cancelProgressDialog();
                taskHandlers.ReaderDeviceConnFailed(connectingDevice);
            }
        } else {
            if (result) {
                if (RFIDController.NOTIFY_READER_CONNECTION)
                    taskHandlers.sendNotification(Constants.ACTION_READER_CONNECTED, "Connected to " + connectingDevice.getName());
                //Application.isAnyScannerConnected = true;
                taskHandlers.ReaderDeviceConnected(connectingDevice);
            } else {
                taskHandlers.ReaderDeviceConnFailed(connectingDevice);
            }
        }
        taskHandlers.onTaskDataCleanUp();
        taskHandlers.setConnectionProgressState(false);

    }

    @Override
    protected void onCancelled() {
        taskHandlers.onTaskDataCleanUp();
        super.onCancelled();
    }

    public ReaderDevice getConnectingDevice() {
        return connectingDevice;
    }



}
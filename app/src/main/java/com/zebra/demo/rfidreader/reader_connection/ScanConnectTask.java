package com.zebra.demo.rfidreader.reader_connection;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.demo.scanner.helpers.AvailableScanner;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;

import java.util.List;

public class ScanConnectTask extends AsyncTask<Void, String, Boolean>{

    /**
     * async task to go for BT connection with reader
     */

        private ReaderDevice connectingDevice;
        private String prgressMsg;
        private OperationFailureException ex;
        private String password;
        private String scannerName;
        private String scannerAddress;
        private String scannerSerialNumber;
        private List<DCSScannerInfo> availablescannerList;
        private List<DCSScannerInfo> activescannerList;
        Activity activity;
        private IScanConnectHandlers scanTaskHandlers;
    private AvailableScanner curAvailableScanner;

    ScanConnectTask(Activity mActivity, ReaderDevice toConnectDevice, String prgressMsg, String Password, IScanConnectHandlers handlers) {
            this.connectingDevice = toConnectDevice;
            scanTaskHandlers = handlers;
            this.prgressMsg = prgressMsg;
            password = Password;
            scannerName = connectingDevice.getName();
            scannerSerialNumber  = connectingDevice.getSerialNumber();
            scannerAddress = connectingDevice.getAddress();
            activity = mActivity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }


        @Override
        protected Boolean doInBackground(Void... a) {

            availablescannerList = Application.sdkHandler.dcssdkGetAvailableScannersList();
            activescannerList = Application.sdkHandler.dcssdkGetActiveScannersList();

            for (DCSScannerInfo device : activescannerList) {
                String scnName = device.getScannerName();
                if (scnName.equalsIgnoreCase(scannerName) == true) {
                    AvailableScanner availableScanner = new AvailableScanner(device);
                    curAvailableScanner = availableScanner;
                    curAvailableScanner.setConnected(true);
                    Application.currentScannerName = availableScanner.getScannerName();
                    Application.currentScannerAddress = availableScanner.getScannerAddress();
                    Application.currentAutoReconnectionState = availableScanner.isAutoReconnection();
                    Application.currentScannerId = availableScanner.getScannerId();
                    Application.currentConnectedScannerID = availableScanner.getScannerId();
                    Application.currentConnectedScanner = device;
                    return true;

                }
            }

            for (DCSScannerInfo device : availablescannerList) {
                //String scnName = device.getScannerHWSerialNumber();
                String scnName = device.getScannerName();
                //scnName = "Symbol Bar Code Scanner";
                if((scnName.startsWith("RFD8500") == true) || ((scnName.startsWith("RFD40+") == true))
                        || ((scnName.startsWith("RFD40P") == true))
                        || ((scnName.startsWith("RFD90+") == true)))
                    scnName = scnName+"::EA";

                Log.d(Application.TAG, " scannerName-barcode = " + scnName );
                Log.d(Application.TAG, " scannerName-zeti = " + scannerName+"::EA" );

                if (scnName.equalsIgnoreCase(scannerName+"::EA") == true) {
                    AvailableScanner availableScanner = new AvailableScanner(device);
                    //ConnectToScanner(availableScanner);

                    if (availableScanner != null) {
                        if (!availableScanner.isConnected()) {

                            if ((curAvailableScanner != null) && (!availableScanner.getScannerAddress().equals(curAvailableScanner.getScannerAddress()))) {
                                if (curAvailableScanner.isConnected())
                                    scanTaskHandlers.disconnect(curAvailableScanner.getScannerId());
                            }
                            DCSSDKDefs.DCSSDK_RESULT result = DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS;

                            if( availableScanner.isConnected() == false) {
                                result = scanTaskHandlers.connect(availableScanner.getScannerId());
                            }
                            if (result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS) {
                                Application.sdkHandler.dcssdkEnableAutomaticSessionReestablishment(false, availableScanner.getScannerId());
                                curAvailableScanner = availableScanner;
                                curAvailableScanner.setConnected(true);
                                Application.currentConnectedScannerID = availableScanner.getScannerId();
                                Application.currentScannerId = availableScanner.getScannerId();
                                Application.currentConnectedScanner = device;
                                return true;
                            } else {
                                curAvailableScanner = null;
                                return false;
                            }
                        }
                    } else {
                        curAvailableScanner = availableScanner;
                        Application.currentScannerName = availableScanner.getScannerName();
                        Application.currentScannerAddress = availableScanner.getScannerAddress();
                        Application.currentAutoReconnectionState = availableScanner.isAutoReconnection();
                        Application.currentScannerId = availableScanner.getScannerId();
                    }
                    break;
                }

            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            scanTaskHandlers.reInit();
            scanTaskHandlers.cancelScanProgressDialog();
            //progressDialog.cancel();
            Application.curAvailableScanner = curAvailableScanner;
            if(curAvailableScanner == null && RFIDController.mConnectedReader != null){

            }
            scanTaskHandlers.scanTaskDone(connectingDevice);


        }

        @Override
        protected void onCancelled() {

            scanTaskHandlers.scanTaskDone(connectingDevice);
            super.onCancelled();
        }

        public ReaderDevice getConnectingDevice() {
            return connectingDevice;
        }


}


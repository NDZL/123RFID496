package com.zebra.demo.scanner.activities;


import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.BuildConfig;
import com.zebra.demo.rfidreader.common.CustomToast;
import com.zebra.demo.rfidreader.notifications.NotificationUtil;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.scanner.helpers.Constants;
import com.zebra.demo.scanner.helpers.CustomProgressDialog;
import android.os.AsyncTask;
import org.xmlpull.v1.XmlPullParser;
import java.io.StringReader;


import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_MODEL_NUMBER;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_SERIAL_NUMBER;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_FW_VERSION;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_CONFIG_NAME;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_DOM;


/**
 * Created by pndv47 on 1/30/2015.
 */
public class AssertFragment extends Fragment {

    int scannerID;
     View rootview=null;
    private static final String TAG = "AssertFragment";

    public static AssertFragment newInstance() {
        //if(mUpdateFragment == null )
        return new AssertFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        rootview = inflater.inflate(R.layout.content_asset_information,container, false);

        Configuration configuration = getResources().getConfiguration();
        if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            if(configuration.smallestScreenWidthDp<Application.minScreenWidth){
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }else{
            if(configuration.screenWidthDp<Application.minScreenWidth){
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        //retry for fetching scanner version

        if(RFIDController.mConnectedReader != null) {
                try {
                    String ModelName =  RFIDController.mConnectedReader.ReaderCapabilities.getModelName();

                    String ScannerVersionInfo;
                    if(ModelName.startsWith("RFD8500"))
                        ScannerVersionInfo = Application.versionInfo.get("PL33");
                    else
                        ScannerVersionInfo = Application.versionInfo.get("PL5000");
                    if(ScannerVersionInfo != null && ScannerVersionInfo.isEmpty()){
                        try {
                            RFIDController.mConnectedReader.Config.getDeviceVersionInfo(Application.versionInfo);
                        } catch (InvalidUsageException | OperationFailureException e) {
                            Log.e(TAG, String.valueOf(e));
                        }
                    }
                }
               catch (Exception e) {
                    Log.d(TAG,  "Returned SDK Exception");
                }
            }
        return rootview;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchDeviceInfo();
    }
    private void fetchDeviceInfo()
    {
        if(RFIDController.mConnectedReader != null) {
            try {
                String ModelName =  RFIDController.mConnectedReader.ReaderCapabilities.getModelName();
                ((TextView) ((ActiveDeviceActivity)getActivity()).findViewById(R.id.txtModel)).setText(ModelName);
                String Firmware =  RFIDController.mConnectedReader.ReaderCapabilities.getFirwareVersion();
                ((TextView) ((ActiveDeviceActivity)getActivity()).findViewById(R.id.txtFW)).setText(Firmware);
                String pcfw = Application.versionInfo.get("CRIMAN_DEVICE");
                if(RFIDController.mConnectedReader.getHostName().startsWith("RFD8500")){
                    pcfw = Application.versionInfo.get("GENX_DEVICE");
                }
                if(RFIDController.mConnectedReader.getHostName().startsWith("MC33")){
                    pcfw = Application.versionInfo.get("RFID_DEVICE");
                }

                String nge = Application.versionInfo.get("NGE");
                ((TextView) ((ActiveDeviceActivity)getActivity()).findViewById(R.id.txtFW)).setText(pcfw);
                ((TextView) ((ActiveDeviceActivity)getActivity()).findViewById(R.id.txtRadio)).setText(nge);
                String SerialNo =  RFIDController.mConnectedReader.ReaderCapabilities.getSerialNumber();
                ((TextView) ((ActiveDeviceActivity)getActivity()).findViewById(R.id.txtSerial)).setText(SerialNo);
                String ManufactureDate =  RFIDController.mConnectedReader.ReaderCapabilities.getManufacturingDate();
                ((TextView) ((ActiveDeviceActivity)getActivity()).findViewById(R.id.txtDOM)).setText(ManufactureDate);

                String ScannerVersionInfo;
                if(ModelName.startsWith("RFD8500"))
                    ScannerVersionInfo = Application.versionInfo.get("PL33");
                else
                    ScannerVersionInfo = Application.versionInfo.get("PL5000");


                TextView Scannerinfo = (TextView) ((ActiveDeviceActivity)getActivity()) .findViewById(R.id.ScannerVersionTextView);
                if(!ScannerVersionInfo.equals("") || !ScannerVersionInfo.isEmpty()) {
                    Scannerinfo.setText(ScannerVersionInfo);
                }else{
                    TableRow entry  = (TableRow) ((ActiveDeviceActivity)getActivity()) .findViewById(R.id.scannertablerowid);
                    entry.setVisibility(View.GONE);

                }
             //   String ScannerName = RFIDController.mConnectedReader.ReaderCapabilities.getScannerName();
                //((TextView) findViewById(R.id.txt_scanner_name)).setText(ScannerName);

            } catch (Exception e) {
               Log.d(TAG,  "Returned SDK Exception");
            }

        }

        ((TextView) ((ActiveDeviceActivity)getActivity()).findViewById(R.id.sdkVersion)).setText(com.zebra.rfid.api3.BuildConfig.VERSION_NAME);
        ((TextView)((ActiveDeviceActivity)getActivity()). findViewById(R.id.appVersion)).setText(BuildConfig.VERSION_NAME);
    }

    private void fetchAssertInfo() {
       // int scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        scannerID = Application.currentScannerId;

        if (scannerID != -1) {

            String in_xml = "<inArgs><scannerID>" + scannerID + " </scannerID><cmdArgs><arg-xml><attrib_list>";
            in_xml+=RMD_ATTR_MODEL_NUMBER;
            in_xml+=",";
            in_xml+=RMD_ATTR_SERIAL_NUMBER;
            in_xml+=",";
            in_xml+=RMD_ATTR_FW_VERSION;
            in_xml+=",";
            in_xml+=RMD_ATTR_CONFIG_NAME;
            in_xml+=",";
            in_xml+=RMD_ATTR_DOM;
            in_xml += "</attrib_list></arg-xml></cmdArgs></inArgs>";

            new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET).execute(new String[]{in_xml});
        } else {
            Toast.makeText(((ActiveDeviceActivity)getActivity()), Constants.INVALID_SCANNER_ID_MSG, Toast.LENGTH_SHORT).show();
        }
    }



    public void sendNotification(String action, String data) {
        if (ActiveDeviceActivity.isActivityVisible()) {
            if (action.equalsIgnoreCase(com.zebra.demo.rfidreader.common.Constants.ACTION_READER_BATTERY_CRITICAL) || action.equalsIgnoreCase(com.zebra.demo.rfidreader.common.Constants.ACTION_READER_BATTERY_LOW)) {
                new CustomToast(getActivity(), R.layout.toast_layout, data).show();
            } else {
                Toast.makeText(((ActiveDeviceActivity)getActivity()), data, Toast.LENGTH_SHORT).show();
            }
        } else {
            /*Intent i = new Intent(this, NotificationsService.class);
            i.putExtra(Constants.INTENT_ACTION, action);
            i.putExtra(Constants.INTENT_DATA, data);
            startService(i);*/

            if(getActivity() != null)
            NotificationUtil.displayNotificationforSettingsDeialActivity(getActivity(), action, data);
        }
    }

    public void RFIDReaderAppeared(ReaderDevice readerDevice) {

    }


    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {

        if (RFIDController.NOTIFY_READER_AVAILABLE)
            sendNotification(com.zebra.demo.rfidreader.common.Constants.ACTION_READER_AVAILABLE, readerDevice.getName() + " is unavailable.");
       // RFIDController.mReaderDisappeared = readerDevice;
        getActivity().onBackPressed();

    }

    private class MyAsyncTask extends AsyncTask<String,Integer,Boolean>{
        int scannerId;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        public MyAsyncTask(int scannerId,  DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode){
            this.scannerId=scannerId;
            this.opcode=opcode;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(getActivity(), "Execute Command...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder() ;
            boolean result =  ((ActiveDeviceActivity)getActivity()).executeCommand(opcode, strings[0], sb, scannerId);
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET) {
                if (result) {
                    try {
                        Log.i(TAG,sb.toString());
                        int i = 0;
                        int attrId = -1;
                        XmlPullParser parser = Xml.newPullParser();

                        parser.setInput(new StringReader(sb.toString()));
                        int event = parser.getEventType();
                        String text = null;
                        while (event != XmlPullParser.END_DOCUMENT) {
                            String name = parser.getName();
                            switch (event) {
                                case XmlPullParser.START_TAG:
                                    break;
                                case XmlPullParser.TEXT:
                                    text = parser.getText();
                                    break;

                                case XmlPullParser.END_TAG:
                                    Log.i(TAG,"Name of the end tag: "+name);
                                    if(text!=null) {
                                        if (name.equals("id")) {
                                            attrId = Integer.parseInt(text.trim());
                                            Log.i(TAG, "ID tag found: ID: " + attrId);
                                        } else if (name.equals("value")) {
                                            final String attrVal = text.trim();
                                            Log.i(TAG, "Value tag found: Value: " + attrVal);
                                            if (RMD_ATTR_MODEL_NUMBER == attrId) {

                                                getActivity(). runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((TextView) rootview.findViewById(R.id.txtModel)).setText(attrVal);
                                                    }
                                                });

                                            } else if (RMD_ATTR_SERIAL_NUMBER == attrId) {

                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((TextView) rootview.findViewById(R.id.txtSerial)).setText(attrVal);
                                                    }
                                                });

                                            } else if (RMD_ATTR_FW_VERSION == attrId) {

                                                getActivity(). runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((TextView) rootview.findViewById(R.id.txtFW)).setText(attrVal);
                                                    }
                                                });

                                            } else if (RMD_ATTR_DOM == attrId) {

                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        ((TextView) rootview.findViewById(R.id.txtDOM)).setText(attrVal);
                                                    }
                                                });

                                            } else if (RMD_ATTR_CONFIG_NAME == attrId) {

                                                getActivity().runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        //((TextView) findViewById(R.id.txtConfigName)).setText(attrVal);
                                                    }
                                                });

                                            }
                                        }
                                    }
                                    break;
                            }
                            event = parser.next();
                        }
                    } catch (Exception e) {
                        Log.e(TAG,e.toString());
                    }

                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

        }


    }
}

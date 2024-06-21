package com.zebra.demo.scanner.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.home.RFIDBaseActivity;
import com.zebra.demo.rfidreader.reader_connection.ScanPair;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.demo.scanner.helpers.CustomProgressDialog;
import com.zebra.demo.scanner.helpers.DotsProgressBar;
import com.zebra.demo.scanner.helpers.ScannerAppEngine;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.scannercontrol.BarCodeView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;
import com.zebra.scannercontrol.FirmwareUpdateEvent;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedDevice;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mIsInventoryRunning;

public class UpdateFirmware extends Fragment implements  NavigationView.OnNavigationItemSelectedListener,ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate, ScannerAppEngine.IScannerAppEngineDevEventsDelegate {
    private static UpdateFirmware mUpdateFragment = null;
    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;
    int scannerID;
    static MyAsyncTask cmdExecTask=null;
    static File selectedPlugIn = null;
    static DCSScannerInfo fwUpdatingScanner;
    static final int PERMISSIONS_REQUEST_READ_EX_STORAGE = 10;
    static boolean fwReboot = false;
    static Dialog dialogFwProgress;
    static Dialog dialogFwRebooting;
    static Dialog dialogFwReconnectScanner;
    static boolean processMultiplePlugIn = false;
    static Dialog dialogFWHelp;
    int dialogFWProgessX = 90;
    int dialogFWProgessY = 220;
    int dialogFWReconnectionY = 250;
    int dialogFWReconnectionX = 50;
    ProgressBar progressBar;
    DotsProgressBar dotProgressBar;
    TextView txtPercentage;
    public static boolean isWaitingForFWUpdateToComplete;
    BarCodeView barCodeView = null;
    FrameLayout llBarcode = null;
    int fwMax;
    static UpdateFirmware mUpdateFirmware = null;
    ScanPair scanPair = null;

    TableRow selectFirmwareRow,tblRowFW;

    private RFIDBaseActivity mRfidBaseActivity;
    private int fwupdateState = -1;
    private String failureReason = "";
    private String TAG = "RFIDUpdateFrimware";
    private Boolean viewCreated = false;

    private HashMap<String,String> devicemodelMap=null;
    private HashMap<String,String> deviceskuMap=null;
    private Uri documentUri;

    public static UpdateFirmware newInstance() {
        //if(mUpdateFragment == null )

        mUpdateFragment = new UpdateFirmware();
        return mUpdateFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        viewCreated = false;
        setHasOptionsMenu(true);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final View rootview = inflater.inflate(R.layout.content_update_firmware, container, false);

        Configuration configuration = getResources().getConfiguration();
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
//            if(configuration.smallestScreenWidthDp<Application.minScreenWidth){
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            }
//        }else{
//            if(configuration.screenWidthDp<Application.minScreenWidth){
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//            }
//        }


        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
       // ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
       //         this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
       // drawer.setDrawerListener(toggle);
       // toggle.syncState();

//        navigationView = (NavigationView) rootview.findViewById(R.id.nav_view);
//        navigationView.setNavigationItemSelectedListener(this);
//        menu = navigationView.getMenu();
//        pairNewScannerMenu = menu.findItem(R.id.nav_pair_device);
//        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);



        selectFirmwareRow = (TableRow)rootview.findViewById(R.id.tbl_row_select_firmware);
        selectFirmwareRow.setVisibility(View.VISIBLE);

        tblRowFW = (TableRow)rootview.findViewById(R.id.tbl_row_fw_update);
        tblRowFW.setVisibility(View.GONE);

        //scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        scannerID = Application.currentScannerId;
        isWaitingForFWUpdateToComplete= false;
        fwUpdatingScanner = Application.currentConnectedScanner;
        Log.i("ScannerControl", "Adding Update FW IScannerAppEngineDevEventsDelegate into list");
        //((ActiveDeviceActivity)getActivity()).addDevConnectionsDelegate(this);
        //((ActiveDeviceActivity)getActivity()).addDevEventsDelegate(this);
        //mUpdateFirmware = this;
        //mRfidBaseActivity = RFIDBaseActivity.getInstance();
        //mRfidBaseActivity.setReaderstatuscallback(this);

        return rootview;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if((getActivity() != null) && (viewCreated == false )) {
            initview(getActivity());
            viewCreated = true;
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
     }

    public static UpdateFirmware getmUpdateFirmwareInstance()
    {
        return mUpdateFirmware;
    }

    public void initview(Activity activity) {
        //super.onResume();

        if (dialogFWHelp != null && dialogFWHelp.isShowing()) {
            dialogFWHelp.dismiss();
            dialogFWHelp = null;
        }

        if(RFIDController.mConnectedReader==null)
        {
            Toast.makeText(((ActiveDeviceActivity)getActivity()), "Cannot perform the action, Reader not connected", Toast.LENGTH_SHORT).show();
            return;
        }
        if(!RFIDController.mConnectedReader.getHostName().startsWith("RFD40") &&
                !RFIDController.mConnectedReader.getHostName().startsWith("RFD90") &&
                !RFIDController.mConnectedReader.getHostName().startsWith("RFD8500"))
        {
            dialogFWHelp = new Dialog(((ActiveDeviceActivity)getActivity()));
            dialogFWHelp.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogFWHelp.setContentView(R.layout.dialog_rfd8500_firmware_help);
            dialogFWHelp.setCancelable(false);
            dialogFWHelp.setCanceledOnTouchOutside(false);
            dialogFWHelp.show();
            TextView declineButton = (TextView) dialogFWHelp.findViewById(R.id.btn_ok);
            // if decline button is clicked, close the custom dialog
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialogFWHelp.dismiss();
                    dialogFWHelp = null;
                    //finish();
                }
            });
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //fwReboot = getIntent().getBooleanExtra(Constants.FW_REBOOT, false);

                if(fwReboot){
                    Application.isFirmwareUpdateInProgress =false;
                    UIUpdater uiUpdater = new UIUpdater(fwReboot);
                    uiUpdater.execute();
                }

                if (!fwReboot && isWaitingForFWUpdateToComplete) {
                    Log.i("ScannerControl","Waiting for fw update to be completed");
                } else {
                    if (dialogFwProgress == null && dialogFwRebooting == null) {
                        if (Application.currentConnectedScanner != null) {
                            if( RFIDController.mConnectedReader == null )
                            {
                                requireActivity().runOnUiThread(() -> {
                                    LinearLayout updateFirmwarelayout = (LinearLayout) getView().findViewById(R.id.layout_update_firmware);
                                    updateFirmwarelayout.setVisibility(View.INVISIBLE);
                                    updateFirmwarelayout.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), "Cannot perform the action, Reader not connected", Toast.LENGTH_SHORT).show();
                                });
                            }else{
                                UIUpdater uiUpdater = new UIUpdater(fwReboot);
                                uiUpdater.execute();
                            }
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.firmware, menu);
        return ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.firmware_help: {
               // if(!isFinishing())
                {
                    dialogFWHelp = new Dialog(getActivity());
                    dialogFWHelp.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogFWHelp.setContentView(R.layout.dialog_firmware_help);
                    dialogFWHelp.setCancelable(false);
                    dialogFWHelp.setCanceledOnTouchOutside(false);
                    dialogFWHelp.show();
                  /*  TextView textView = (TextView) dialogFWHelp.findViewById(R.id.url);
                    if (textView != null) {
                        textView.setMovementMethod(LinkMovementMethod.getInstance());
                    }*/

                    TextView declineButton = (TextView) dialogFWHelp.findViewById(R.id.btn_ok);
                    // if decline button is clicked, close the custom dialog
                    declineButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Close dialog
                            dialogFWHelp.dismiss();
                            dialogFWHelp = null;
                        }
                    });
                }
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean ShowFirmwareInfo(boolean bFwReboot) {

        if (bFwReboot) {
            ProcessPlugIn(selectedPlugIn, true);
        } else {
            String scannerFirmwareVersion = getScannerFirmwareVersion(scannerID); // 20012
            requireActivity().runOnUiThread(() -> {
                UpdateScannerFirmwareVersion(scannerFirmwareVersion, false);
            });
        }
        return true;
    }

    private void ShowMultipleFWFilesFoundDialog(boolean isPlugIn) {
        //if(!isFinishing())
        {
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_multiple_dat_files);
            if (isPlugIn) {
                TextView msg = (TextView) dialog.findViewById(R.id.txt_msg);
                msg.setText(R.string.multiple_plug_in_files_msg_1);
                TextView title = (TextView) dialog.findViewById(R.id.txt_title);
                title.setText(R.string.multiple_plugin_files);
            }
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            TextView declineButton = (TextView) dialog.findViewById(R.id.btn_ok);
            // if decline button is clicked, close the custom dialog
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();
                }
            });
        }
    }

    private ScannerPlugIn getLatestPlugIn(List<ScannerPlugIn> matchingPlugins) {
        ScannerPlugIn latestPlugIn = matchingPlugins.get(0);

        for (ScannerPlugIn plugIn:matchingPlugins) {
            if(Integer.valueOf(plugIn.getRevision())>Integer.valueOf(latestPlugIn.getRevision())){
                latestPlugIn = plugIn;
            }
        }
        return latestPlugIn;
    }

    private void ProcessPlugIn(File plugInFile, final boolean bFwReboot) {
        if (isPlugIn(plugInFile)) {
            // This is a plug-in file
            String unzipLocation = ((ActiveDeviceActivity)getActivity()).getCacheDir().getAbsolutePath() + File.separator;
            String metaDataFilePath = extractMetaData(unzipLocation, plugInFile.getAbsolutePath());
            String scannerModel = getScannerModelNumber(scannerID); // 533
            List<String> metaSupportedModels = getSupportedModels(metaDataFilePath);
            if (metaSupportedModels.contains(scannerModel)) {
                // Matching model found in meta.xml
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout updateFirmwarelayout = (LinearLayout) getView().findViewById(R.id.layout_update_firmware);
                        updateFirmwarelayout.setVisibility(View.VISIBLE);
                    }
                });
                final String scannerFirmwareVersion = getScannerFirmwareVersion(scannerID); // 20012
                final String plugInName = getPlugInName(metaDataFilePath);
                final String plugInRev = getPlugInRev(metaDataFilePath);
                final String plugInDate = getPlugInDate(metaDataFilePath);
                final String matchingPlugInFwName = getMatchingFWName(getPlugInFwNames(metaDataFilePath), scannerFirmwareVersion);
                selectedPlugIn = plugInFile;
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        UpdatePlugInName(plugInName);

                        if (bFwReboot && (Application.isFirmwareUpdateSuccess == true)) {
                            TurnOffLEDPattern();
                            isWaitingForFWUpdateToComplete = false;
                            if (matchingPlugInFwName.equals(scannerFirmwareVersion)) { // Update success
                                UpdateScannerFirmwareVersion(scannerFirmwareVersion, true);
                                UpdateToFirmwareVersion(plugInRev, plugInDate, matchingPlugInFwName, true);
                                UpdateButton();
                            } else { // Update failed
                                UpdateScannerFirmwareVersion(scannerFirmwareVersion, false);
                                UpdateToFirmwareVersion(plugInRev, plugInDate, matchingPlugInFwName, false);
                                UpdateStatus();
                                Application.isFirmwareUpdateSuccess = true;
                            }
                        } else {
                            UpdateScannerFirmwareVersion(scannerFirmwareVersion, false);
                            UpdateToFirmwareVersion(plugInRev, plugInDate, matchingPlugInFwName, false);
                        }
                    }
                });

            } else {
                // Plug-in model mis match
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LinearLayout updateFirmwarelayout = (LinearLayout) getView().findViewById(R.id.layout_update_firmware);
                        updateFirmwarelayout.setVisibility(View.INVISIBLE);
                        updateFirmwarelayout.setVisibility(View.GONE);
                        ShowPlugInScannerMismatchDialog();
                    }
                });
            }
        } else {
            // This is a DAT file
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    LinearLayout updateFirmwarelayout = (LinearLayout) getView().findViewById(R.id.layout_update_firmware);
                    updateFirmwarelayout.setVisibility(View.VISIBLE);
                }
            });
            final String scannerFirmwareVersion = getScannerFirmwareVersion(scannerID); // 20012
            final String plugInName = plugInFile.getName();
            selectedPlugIn = plugInFile;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UpdatePlugInName(plugInName);

                    if (bFwReboot && (Application.isFirmwareUpdateSuccess == true )) {
                        TurnOffLEDPattern();
                        isWaitingForFWUpdateToComplete = false;
                        UpdateScannerFirmwareVersion(scannerFirmwareVersion, true);
                        UpdateToFirmwareVersion(plugInName, true);
                        UpdateButton();

                    } else {
                        UpdateScannerFirmwareVersion(scannerFirmwareVersion, false);
                        UpdateToFirmwareVersion(plugInName, false);
                        Application.isFirmwareUpdateSuccess = true;
                    }
                }
            });
        }
    }



    private boolean isPlugIn(File plugInFile) {
        if(plugInFile.getAbsolutePath().endsWith(".SCNPLG") || plugInFile.getAbsolutePath().endsWith(".scnplg") ){
            return true;
        }
        return false;
    }

    private String getMatchingFWName(ArrayList<String> plugInFwNames, String scannerFirmwareVersion) {
        String matchingFWName = "";
        for (String plugInFwName : plugInFwNames) {
            if (scannerFirmwareVersion.equals(plugInFwName)) {
                matchingFWName = plugInFwName;
                break;
            }
        }

        if(matchingFWName.equals("")){
            for (String plugInFwName : plugInFwNames) {
                if (plugInFwName.length()>3 && scannerFirmwareVersion.startsWith(plugInFwName.substring(0, 3))) {
                    matchingFWName = plugInFwName;
                    break;
                }
            }
        }

        return matchingFWName;
    }

    private void UpdateStatus() {
        TableRow tblRowFwStatus = (TableRow) getView().findViewById(R.id.tbl_row_fw_status);
        tblRowFwStatus.setVisibility(View.VISIBLE);

    }

    private void UpdateButton() {
        TableRow tblRowFWSuccess = (TableRow)getView().findViewById(R.id.tbl_row_fw_update_success);
        if (tblRowFWSuccess != null) {
            tblRowFWSuccess.setVisibility(View.VISIBLE);
        }
        TableRow tblRowFW = (TableRow)getView().findViewById(R.id.tbl_row_fw_update);
        if (tblRowFW != null) {
            tblRowFW.setVisibility(View.INVISIBLE);
            tblRowFW.setVisibility(View.GONE);
        }
    }

    private void UpdateToFirmwareVersion(String plugInRev, String plugInDate, String plugInCombinedFwName, boolean afterFWUpdate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(dateFormat.parse(plugInDate));// all done
        } catch (ParseException e) {
           Log.d(TAG,  "Returned SDK Exception");
        }


        String toString;
        String month = String.valueOf(cal.get(Calendar.MONTH)+1);
        if(month.length()==1){
            month = "0"+month;
        }
        String date = String.valueOf(cal.get(Calendar.DATE));
        if(date.length()==1){
            date = "0"+date;
        }
        if(afterFWUpdate){
            toString = "Current: Release " + plugInRev + " - " + cal.get(Calendar.YEAR)+"."+ month +"."+ date+  " (" + plugInCombinedFwName + ")";
        }else {
            toString = "To: Release " + plugInRev + " - " + cal.get(Calendar.YEAR)+"."+ month +"."+ date+ " (" + plugInCombinedFwName + ")";
        }
        TextView textView = (TextView) getView().findViewById(R.id.txt_to_fw_version);
        textView.setText(toString);
    }

    private void UpdateToFirmwareVersion(String datFileName, boolean afterFWUpdate) {

        String toString;

        if(afterFWUpdate){
            //toString = "Current: "+datFileName;
            toString = "";
        }else {
            toString = "To: "+datFileName;
        }
        TextView textView = (TextView) getView().findViewById(R.id.txt_to_fw_version);
        textView.setText(toString);
    }

    private void UpdateScannerFirmwareVersion(String scannerFirmwareVersion, boolean afterFWUdate) {
        TextView textView = (TextView) getView().findViewById(R.id.txt_from_fw_version);
        if(afterFWUdate){
            textView.setText("");
        }else {
            String pcfw = Application.versionInfo.get("CRIMAN_DEVICE");
            if (pcfw !=null) {
                textView.setText("From: " + pcfw.replaceFirst("P", "S"));
            }else{
                textView.setText("From: " + scannerFirmwareVersion.replaceFirst("C", "S"));
            }
        }
    }

    private void UpdateFilePath(String path){
        TextView textView = (TextView) requireView().findViewById(R.id.txt_file_path);
        textView.setText("Path: " +path);
    }

    private void UpdatePlugInName(String plugInName) {
        TextView textView = (TextView) requireView().findViewById(R.id.txt_to_fw_version);
        textView.setText("To: "  + plugInName);
    }

    private ArrayList<String> getPlugInFwNames(String metaDataFilePath) {
        ArrayList<String> plugInFWVersion = new ArrayList<String>();
        try {

            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xppf.newPullParser();
            InputStream input = new FileInputStream(metaDataFilePath);
            parser.setInput(input, null);

            int event = parser.getEventType();
            String text = null;
            while (event != XmlPullParser.END_DOCUMENT) {
                String name = parser.getName();
                switch (event) {
                    case XmlPullParser.START_TAG:
                        if (name.equals("combined-firmware")) {
                            plugInFWVersion.add(parser.getAttributeValue(null, "name").trim());
                        }
                        break;
                    case XmlPullParser.TEXT:
                         text = parser.getText().trim();
                        break;

                    case XmlPullParser.END_TAG:
                        if (name.equals("component")) {
                            if(text!=null) {
                                plugInFWVersion.add(text.trim());
                            }
                        }

                        break;

                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return plugInFWVersion;
    }

    private String getPlugInDate(String metaDataFilePath) {
        String plugInRev = "";
        try {

            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xppf.newPullParser();
            InputStream input = new FileInputStream(metaDataFilePath);
            parser.setInput(input, null);

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
                        if (name.equals("release-date") && text!=null) {
                            plugInRev =text.trim();
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return plugInRev;
    }

    private String getPlugInRev(String metaDataFilePath) {
        String plugInRev = "";
        try {

            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xppf.newPullParser();
            InputStream input = new FileInputStream(metaDataFilePath);
            parser.setInput(input, null);

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
                        if (name.equals("revision") && text!=null) {
                            plugInRev =text.trim();
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return plugInRev;
    }

    private String getPlugInName(String metaDataFilePath) {
        StringBuilder plugInName = new StringBuilder();
        try {

            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xppf.newPullParser();
            InputStream input = new FileInputStream(metaDataFilePath);
            parser.setInput(input, null);

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
                        if(text!=null) {
                            if (name.equals("family")) {
                                plugInName = new StringBuilder(text.trim());
                                plugInName.append("-");
                            }
                            if (name.equals("name")) {
                                plugInName.append(text.trim());
                            }
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return plugInName.toString();
    }

    private void ShowPlugInScannerMismatchDialog() {
        //if(!isFinishing())
        {
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_plugin_scanner_mismatch);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            TextView declineButton = (TextView) dialog.findViewById(R.id.btn_ok);
            // if decline button is clicked, close the custom dialog
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();
                }
            });
        }
    }

    private List<String> getSupportedModels(String metaDataFilePath) {
        List<String> modelList = new ArrayList<String>();

        try {

            XmlPullParserFactory xppf = XmlPullParserFactory.newInstance();
            XmlPullParser parser = xppf.newPullParser();
            InputStream input = new FileInputStream(metaDataFilePath);
            parser.setInput(input, null);

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
                        if (name.equals("model")) {
                            if(text!=null) {
                                modelList.add(text.trim());
                            }
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return modelList;
    }


    private String extractMetaData(String extractPath, String zipPath)
    {
        InputStream is;
        ZipInputStream zis;
        String unzipFile="";
        try
        {
            String filename;
            is = new FileInputStream(zipPath);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                filename = ze.getName();
                if(filename.equalsIgnoreCase("Metadata.xml")){
                    unzipFile = extractPath + filename;

                    FileOutputStream fout = new FileOutputStream(unzipFile);

                    // cteni zipu a zapis
                    while ((count = zis.read(buffer)) != -1)
                    {
                        fout.write(buffer, 0, count);
                    }
                    fout.close();
                }
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
           Log.d(TAG,  "Returned SDK Exception");
            return null;
        }
        return unzipFile;
    }

    private String getScannerFirmwareVersion(int scannerID) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>20012</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        ((ActiveDeviceActivity)getActivity()).executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET,in_xml,outXML,scannerID);
        return getSingleStringValue(outXML);
    }

    private String getSingleStringValue(StringBuilder outXML) {
        String attr_val = "";
        try {
            XmlPullParser parser = Xml.newPullParser();

            parser.setInput(new StringReader(outXML.toString()));
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
                        if (name.equals("value")&& text!=null) {
                            attr_val = text.trim();
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return attr_val;
    }

    private String getScannerModelNumber(int scannerID) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>533</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        ((ActiveDeviceActivity)getActivity()).executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET,in_xml,outXML,scannerID);
        return getSingleStringValue(outXML);
    }

    private void ShowNoPlugInFoundDialog() {
        //if(!isFinishing())
        {
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_no_plugin);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            TextView declineButton = (TextView) dialog.findViewById(R.id.btn_ok);
            // if decline button is clicked, close the custom dialog
            declineButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialog.dismiss();
                }
            });
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;
        if (id == R.id.nav_pair_device) {
            ((ActiveDeviceActivity)getActivity()).disconnect(scannerID);
            //Application.barcodeData.clear();
            Application.currentScannerId = Application.SCANNER_ID_NONE;
            //finish();
            intent = new Intent(getActivity(), ScannerHomeActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_devices) {
            intent = new Intent(getActivity(), ScannersActivity.class);

            startActivity(intent);
        }else if (id == R.id.nav_find_cabled_scanner) {
            AlertDialog.Builder dlg = new  AlertDialog.Builder(getActivity());
            dlg.setTitle("This will disconnect your current scanner");
            //dlg.setIcon(android.R.drawable.ic_dialog_alert);
            dlg.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                    ((ActiveDeviceActivity)getActivity()).disconnect(scannerID);
                    //Application.barcodeData.clear();
                    Application.currentScannerId = Application.SCANNER_ID_NONE;
                    //finish();
                    Intent intent = new Intent(getActivity(), FindCabledScanner.class);
                    startActivity(intent);
                }
            });

            dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int arg) {

                }
            });
            dlg.show();
        }else if (id == R.id.nav_connection_help) {
            intent = new Intent(getActivity(), ConnectionHelpActivity2.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_about) {
            intent = new Intent(getActivity(), AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) getView().findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        drawer.setSelected(true);
        return true;
    }

    @Override
    public boolean scannerHasAppeared(int scannerID) {
        if(fwUpdatingScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI){
            Application.sdkHandler.dcssdkEstablishCommunicationSession(fwUpdatingScanner.getScannerID());
        }
        return true;
    }

    @Override
    public boolean scannerHasDisappeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasConnected(int scannerID) {
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);
        if(isWaitingForFWUpdateToComplete && dialogFwRebooting!=null){
            dialogFwRebooting.dismiss();
            dialogFwRebooting = null;
        }
        if(isWaitingForFWUpdateToComplete && dialogFwReconnectScanner!=null){
            dialogFwReconnectScanner.dismiss();
            dialogFwReconnectScanner = null;
        }
        return false;
    }


    private Handler pnpHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if(dialogFwRebooting!=null){
                        dialogFwRebooting.dismiss();
                        dialogFwRebooting =null;
                        ShowReconnectScanner();
                    }
                    break;
            }
        }
    };

    private void ShowReconnectScanner() {
        //if(!isFinishing())
        {
            dialogFwReconnectScanner = new Dialog(getActivity());
            dialogFwReconnectScanner.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialogFwReconnectScanner.setContentView(R.layout.dialog_fw_reconnect_scanner);
            TextView cancelButton = (TextView) dialogFwReconnectScanner.findViewById(R.id.btn_cancel);
            // if decline button is clicked, close the custom dialog
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Close dialog
                    dialogFwReconnectScanner.dismiss();
                    dialogFwReconnectScanner = null;
                    //finish();
                }
            });
            llBarcode = (FrameLayout) dialogFwReconnectScanner.findViewById(R.id.scan_to_connect_barcode);
            barCodeView = null;

            if (fwUpdatingScanner.getConnectionType() == DCSSDKDefs.DCSSDK_CONN_TYPES.DCSSDK_CONNTYPE_USB_SNAPI) {
                barCodeView = Application.sdkHandler.dcssdkGetUSBSNAPIWithImagingBarcode();
            }else{
                barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(BaseActivity.selectedProtocol, BaseActivity.selectedConfig);
                if(barCodeView==null){
                    barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(BaseActivity.selectedProtocol, BaseActivity.selectedConfig, ScannerHomeActivity.btAddress);
                }
            }
                //dialogFwReconnectScanner.getWindow().setLayout(getXReconnection(), getYReconnection());
            if (barCodeView != null && llBarcode != null) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                int x = width * 7 / 10;
                int y = x / 3;
                barCodeView.setSize(x, y);
                llBarcode.addView(barCodeView, layoutParams);
            }
            dialogFwReconnectScanner.setCancelable(false);
            dialogFwReconnectScanner.setCanceledOnTouchOutside(false);
            dialogFwReconnectScanner.show();
            Window window = dialogFwReconnectScanner.getWindow();
            if(window!=null) {
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, getY());
            }
        }
    }

    public static int dpToPx(int dp)
    {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px)
    {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    private int getY() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int y = (int) (dialogFWProgessY * scale + 0.5f);
        return y;
    }

    private int getYReconnection() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int y = (int) (dialogFWReconnectionY * scale + 0.5f);
        return y;
    }

    private int getXReconnection() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int x = (int) (dialogFWReconnectionX * scale + 0.5f);
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        return width - x;
    }

    private int getX() {
        final float scale = this.getResources().getDisplayMetrics().density;
        int x = (int) (dialogFWReconnectionX * scale + 0.5f);
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        int width = size.x;
        return width - x;
    }


    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);
        if(isWaitingForFWUpdateToComplete){
            ((ActiveDeviceActivity)getActivity()).setWaitingForFWReboot(true);
            pnpHandler.sendMessageDelayed(pnpHandler.obtainMessage(1),180000);
        }else {
            //Application.barcodeData.clear();
            //finish();
        }

        if(dialogFwProgress !=null) {
            dialogFwProgress.dismiss();
            dialogFwProgress = null;

        }
        isWaitingForFWUpdateToComplete= true;
        tblRowFW.setVisibility(View.GONE);
        showRebooting();
        return true;
    }


    static int TIME_OUT = 40;
    static final int TIME_TICK = 1000;
    static final int MSG_DISMISS_DIALOG = 0;

    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_DIALOG:
                    if( (TIME_OUT > 0) && (dialogFwRebooting!=null)) {
                        TextView counter = (TextView) dialogFwRebooting.findViewById(R.id.counter);
                        counter.setText(String.valueOf(TIME_OUT));
                        TIME_OUT--;
                        mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, TIME_TICK);
                        break;
                    }

                    if (dialogFwRebooting != null && dialogFwRebooting.isShowing()) {
                        dialogFwRebooting.dismiss();
                        dialogFwRebooting = null;
                        TIME_OUT = 40;
                    }
                    break;

                default:
                    break;
            }
        }
    };


    private void showRebooting() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("ScannerControl","Show Rebooting dialog");
                //if(!isFinishing())
                {
                    dialogFwRebooting = new Dialog(getActivity());
                    dialogFwRebooting.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogFwRebooting.setContentView(R.layout.dialog_fw_rebooting);
                    TextView reason = (TextView) dialogFwRebooting.findViewById(R.id.fwstatus);
                    reason.setText(failureReason + reason.getText());
                    // if decline button is clicked, close the custom dialog
//                    cancelButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
                            // Close dialog
//                            dialogFwRebooting.dismiss();
//                            dialogFwRebooting = null;
//                            finish();
//                        }
//                    });

                    dotProgressBar = (DotsProgressBar) dialogFwRebooting.findViewById(R.id.progressBar);
                    dotProgressBar.setDotsCount(6);

                    Window window = dialogFwRebooting.getWindow();
                    if(window!=null) {
                        dialogFwRebooting.getWindow().setLayout(getX(), getY());
                    }
                    dialogFwRebooting.setCancelable(false);
                    dialogFwRebooting.setCanceledOnTouchOutside(false);
                    Log.i("ScannerControl","Showing dot progress dialog");
                    dialogFwRebooting.show();
                    TIME_OUT = 50;
                    mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, TIME_TICK);

                }
            }
        });
    }

    public void selectedFile(Uri data){

        selectedPlugIn = new File(data.getPath());
        documentUri = data;
        selectedPlugIn = new File(selectedPlugIn.toString().replace("/document/primary:","/storage/emulated/0/"));
        UIUpdater uiUpdater = new UIUpdater(fwReboot);
        uiUpdater.execute();
        UpdateFilePath(selectedPlugIn.toString());
        UpdatePlugInName(selectedPlugIn.getName());

        selectFirmwareRow.setVisibility(View.GONE);
        tblRowFW.setVisibility(View.VISIBLE);
    }


    public void updateFirmware(View view) {

        if(mIsInventoryRunning){
            Toast.makeText(getActivity(),"Firmware update not allowed while inventory is running",Toast.LENGTH_SHORT).show();
            return;
        }

        if(selectedPlugIn != null) {
            File file = new File(selectedPlugIn.getAbsolutePath());
            if(file != null && file.exists()) {
                String strFileName = file.getName();
                if (strFileName.endsWith(".FCDAT") || strFileName.endsWith(".fcdat")) {
                    InitializeDeviceCompatibilityMap();
                    if(!CheckFCDATCompatability(selectedPlugIn.getAbsolutePath())){
                        Toast.makeText(getActivity(),"Selected .FCDAT is not compatible with connected reader!Firmware update not allowed!",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        }

        if(selectedPlugIn == null )
            return;
        Application.isFirmwareUpdateInProgress = true;
        TurnOnLEDPattern();
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-string>" + documentUri + "</arg-string></cmdArgs></inArgs>";
        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_UPDATE_FIRMWARE,null);
        cmdExecTask.execute(new String[]{in_xml});

    }
    private void InitializeDeviceCompatibilityMap() {

        devicemodelMap = new HashMap<String, String>();
        deviceskuMap = new HashMap<String, String>();

        if (devicemodelMap != null) {
            devicemodelMap.put("STANDARD", "1");
            devicemodelMap.put("PREMIUM (WiFi)", "2");
            devicemodelMap.put("PREMIUM (WiFi & SCAN)", "3");
        }
        if (deviceskuMap != null) {
            deviceskuMap.put("US", "1");
            deviceskuMap.put("E8", "2");
            deviceskuMap.put("JP", "3");
            deviceskuMap.put("IL", "4");
            deviceskuMap.put("WR", "5");
            deviceskuMap.put("SL", "6");
            deviceskuMap.put("TN", "7");
            deviceskuMap.put("TH", "8");
            deviceskuMap.put("IN", "9");
        }
    }

    private boolean CheckFCDATCompatability(String filename){

        FileInputStream InputStream = null;
        int bytesread=0;
        byte[] buffer = new byte[4];
        int model_len =0;
        String Devicemodel=null;
        String readersku=null;
        int Devicemodelvalue=0;
        int readerskuvalue=0;

        if(filename == null ){
            Log.e(TAG, "Invalid Input .FCDAT file\n");
            return false;
        }

        if(mConnectedReader != null  ) {
            try {
                InputStream = new FileInputStream(filename);
                if(InputStream == null){
                    Log.e(TAG, "Error getting the file e\n");
                    return false;
                }

                if(buffer!=null) {
                    /*Read first 4 bytes of data from the .FCDAT file*/
                    bytesread = InputStream.read(buffer);
                }
                if (bytesread != 4) {
                    Log.e(TAG,"Error reading from FCDAT file");
                    // releases all system resources from the streams
                    if(InputStream !=null)
                        InputStream.close();
                    return false;
                }

                /*Get model name and sku of connected reader*/
                String modelname=mConnectedReader.ReaderCapabilities.getModelName();
                model_len = mConnectedReader.ReaderCapabilities.getModelName().length();
                readersku = mConnectedReader.ReaderCapabilities.getModelName().substring(model_len - 2, model_len);
                if(mConnectedDevice!=null) {
                    Devicemodel = mConnectedDevice.getDeviceCapability(modelname);
                }

                /*Get hashmap values corresponding to connecterd readers sku and model*/
                if(devicemodelMap!=null){
                    if(devicemodelMap.get(Devicemodel)!=null) {
                        Devicemodelvalue = Integer.valueOf(devicemodelMap.get(Devicemodel));
                    }else{
                        Log.e(TAG,"Unsupported Device model");
                        if(InputStream !=null)
                            InputStream.close();
                        return false;
                    }
                }
                if(deviceskuMap !=null){
                    if(deviceskuMap.get(readersku)!=null) {
                        readerskuvalue = Integer.valueOf(deviceskuMap.get(readersku));
                    } else {
                        Log.e(TAG,"Unsupported sku model");
                        if(InputStream !=null)
                            InputStream.close();
                        return false;
                    }
                }

                int a = Character.digit(buffer[1], 10);
                int b = Character.digit(buffer[3], 10);

                if(a == -1 || b == -1){
                    Log.e(TAG,"Invalid FCDAT data");
                    if(InputStream !=null)
                        InputStream.close();
                    return false;
                }

                if( (Devicemodelvalue != a) ||  (readerskuvalue != b)){
                    if(InputStream !=null)
                        InputStream.close();
                    return false;
                }
                // releases all system resources from the streams
                if(InputStream !=null)
                    InputStream.close();

            } catch (IOException ex) {
                if( ex!= null){ Log.e(TAG, ex.getMessage()); }
            }
        }else{
            Log.e(TAG,"Connected reader is invalid!");
            return false;
        }
        return true;
    }

    private void TurnOnLEDPattern() {
        String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-int>" +
                85 + "</arg-int></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        ((ActiveDeviceActivity)getActivity()).executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML, outXML, scannerID);
    }

    private void TurnOffLEDPattern() {
        String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-int>" +
                90 + "</arg-int></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        ((ActiveDeviceActivity)getActivity()).executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION,inXML,outXML,scannerID);
    }

    @Override
    public void scannerBarcodeEvent(byte[] barcodeData, int barcodeType, int scannerID) {

    }

    @Override
    public void  scannerImageEvent(byte[] imageData) {

    }

    @Override
    public void  scannerVideoEvent(byte[] videoData) {
    }
    @Override
    public void scannerFirmwareUpdateEvent(FirmwareUpdateEvent firmwareUpdateEvent) {
        Log.i("ScannerControl","scannerFirmwareUpdateEvent type = "+firmwareUpdateEvent.getEventType());

        if(firmwareUpdateEvent.getEventType() == DCSSDKDefs.DCSSDK_FU_EVENT_TYPE.SCANNER_UF_SESS_START){

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialogFwProgress = new Dialog((ActiveDeviceActivity)getActivity());
                    dialogFwProgress.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialogFwProgress.setContentView(R.layout.dialog_fw_progress);
//                    //TextView cancelButton = (TextView) dialogFwProgress.findViewById(R.id.btn_cancel);
                    final int scannerIDFWSessionStarted = firmwareUpdateEvent.getScannerInfo().getScannerID();
                    // if decline button is clicked, close the custom dialog
//                    cancelButton.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            String in_xml = "<inArgs><scannerID>" + scannerIDFWSessionStarted+ "</scannerID></inArgs>";
//                            cmdExecTask = new MyAsyncTask(scannerIDFWSessionStarted,DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_ABORT_UPDATE_FIRMWARE,null);
//                            cmdExecTask.execute(new String[]{in_xml});
//                        }
//                    });

                    progressBar = (ProgressBar)dialogFwProgress.findViewById(R.id.progressBar);
                    progressBar.setMax(firmwareUpdateEvent.getMaxRecords());
                    fwMax = firmwareUpdateEvent.getMaxRecords();

                    Window window = dialogFwProgress.getWindow();
                    if(window!=null) {
                        dialogFwProgress.getWindow().setLayout(getX(), getY());
                    }
                    dialogFwProgress.setCancelable(false);
                    dialogFwProgress.setCanceledOnTouchOutside(false);
                    //if(!isFinishing())
                    {
                        Log.i("ScannerControl","Show Progress dialog");
                        dialogFwProgress.show();
                        txtPercentage = (TextView) dialogFwProgress.findViewById(R.id.txt_percentage);
                        txtPercentage.setText("0%");
                    }

                }


            });




        }
        if(firmwareUpdateEvent.getEventType() == DCSSDKDefs.DCSSDK_FU_EVENT_TYPE.SCANNER_UF_DL_PROGRESS){

            if(progressBar !=null){
                progressBar.setProgress(firmwareUpdateEvent.getCurrentRecord());
                double percentage = (firmwareUpdateEvent.getCurrentRecord()*100.0/fwMax);
                int iPercentage = (int) percentage;
                if(txtPercentage !=null) {
                    txtPercentage.setText(String.format("%s%%", Integer.toString(iPercentage)));
                }
            }
            if(dialogFwProgress !=null && !dialogFwProgress.isShowing() ){
                Log.i("ScannerControl","Show Progress dialog");
                dialogFwProgress.show();
            }
        }

        if(firmwareUpdateEvent.getEventType() == DCSSDKDefs.DCSSDK_FU_EVENT_TYPE.SCANNER_UF_SESS_END){

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(dialogFwProgress !=null) {
                        if(txtPercentage !=null) {
                            txtPercentage.setText("100%");
                        }
                        dialogFwProgress.dismiss();
                        dialogFwProgress = null;
                        String in_xml = "<inArgs><scannerID>" + firmwareUpdateEvent.getScannerInfo().getScannerID() + "</scannerID></inArgs>";
                        StringBuilder outXML = new StringBuilder();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                           Log.d(TAG,  "Returned SDK Exception");
                        }
                        ((ActiveDeviceActivity)getActivity()).executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_START_NEW_FIRMWARE, in_xml, outXML, firmwareUpdateEvent.getScannerInfo().getScannerID());
                        isWaitingForFWUpdateToComplete= true;
                        Application.isFirmwareUpdateSuccess = true;
                        tblRowFW.setVisibility(View.GONE);
                        showRebooting();
                    }

                }
            });
        }

        if (firmwareUpdateEvent.getEventType() == DCSSDKDefs.DCSSDK_FU_EVENT_TYPE.SCANNER_UF_STATUS) {

            TurnOffLEDPattern();
            isWaitingForFWUpdateToComplete = false;
            if (dialogFwProgress != null) {
                dialogFwProgress.dismiss();
                dialogFwProgress = null;
            }
            failureReason  = getFlashResponseErrorDescription(firmwareUpdateEvent.getStatus());
            Log.i(TAG, "" + failureReason);
            if (firmwareUpdateEvent.getStatus() != DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FIRMWARE_UPDATE_ABORTED) {
                if (firmwareUpdateEvent.getStatus() == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_FAILURE) {
                    Log.i(TAG, "Aborted" + failureReason);
                    Application.isFirmwareUpdateInProgress = false;
                    Application.isFirmwareUpdateSuccess = false;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UpdateStatus();
                            Toast.makeText(getActivity(), "Aborted" + failureReason, Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UpdateStatus();
                        isWaitingForFWUpdateToComplete = true;
                        tblRowFW.setVisibility(View.GONE);
                        showRebooting();
                        Application.isFirmwareUpdateSuccess = false;
                    }
                });

            }
        }
    }




    /**
     * Retrieves the flash response error description for the given status code
     *
     * @param statusCode status code for the flash response error
     * @return Corresponding flash response error description
     */
    private String getFlashResponseErrorDescription(DCSSDKDefs.DCSSDK_RESULT statusCode) {
        String errorDescription = "";
        switch (statusCode) {
            case DCSSDK_RESULT_FIRMWARE_UPDATE_FAILED_LOW_BATTERY_LEVEL:
                errorDescription = this.getResources().getString(R.string.update_failed_low_battery_level);
                break;
            case DCSSDK_RESULT_FIRMWARE_UPDATE_FAILED_COMMANDS_ARE_OUT_OF_SYNC:
                errorDescription = this.getResources().getString(R.string.update_failed_commands_are_out_of_sync);
                break;
            case DCSSDK_RESULT_FIRMWARE_UPDATE_FAILED_HAS_OVERLAPPING_ADDRESS:
                errorDescription = this.getResources().getString(R.string.update_failed_has_overlapping_address);
                break;
            case DCSSDK_RESULT_FIRMWARE_UPDATE_FAILED_LOAD_COUNT_ERROR:
                errorDescription = this.getResources().getString(R.string.update_failed_load_count_error);
                break;
        }
        return errorDescription;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if(dialogFwProgress!=null) {
            Window window = dialogFwProgress.getWindow();
            if(window!=null) {
                window.setLayout(getX(), getY());
            }
        }
        if(dialogFwRebooting!=null) {
            Window window = dialogFwRebooting.getWindow();
            if(window!=null) {
                window.setLayout(getX(), getY());
            }
        }
        if(dialogFwReconnectScanner!=null) {
            if(barCodeView !=null && llBarcode !=null) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                int width = size.x;
                int height = size.y;
                int x = width * 7 / 10;
                int y = x / 3;
                barCodeView.setSize(x, y);
                llBarcode.removeAllViews();
                llBarcode.addView(barCodeView, layoutParams);
            }
            Window window = dialogFwReconnectScanner.getWindow();
            if(window!=null) {
                window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, getY());
            }
        }

    }


    public void RFIDReaderAppeared(ReaderDevice device) {

        if( dialogFwRebooting != null) {
            dialogFwRebooting.dismiss();
            dialogFwRebooting = null;
        }
        //mUpdateFirmware.finish();
        if(dialogFwProgress != null ){
            dialogFwProgress.dismiss();
            dialogFwProgress = null;
        }
        isWaitingForFWUpdateToComplete = false;
        Application.isFirmwareUpdateInProgress = false;
        Application.updateReaderConnection = true;

    }


    public void RFIDReaderDisappeared(ReaderDevice device) {

        if( dialogFwRebooting != null)
        {
            dialogFwRebooting.dismiss();
            dialogFwRebooting = null;
        }
        if(dialogFwProgress != null ){
            dialogFwProgress.dismiss();
            dialogFwProgress = null;
        }

        //mUpdateFirmware.finish();
       // isWaitingForFWUpdateToComplete = false;
       // Application.isFirmwareUpdateInProgress = false;
      //  Application.updateReaderConnection = true;


    }

    private class MyAsyncTask extends AsyncTask<String,Integer,Boolean> {
        int scannerId;
        StringBuilder outXML;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        private CustomProgressDialog progressDialog;

        public MyAsyncTask(int scannerId,  DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode,StringBuilder outXML){
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.outXML = outXML;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(getActivity(), "Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }


        @Override
        protected Boolean doInBackground(String... strings) {

            return  ((ActiveDeviceActivity)getActivity()).executeCommand(opcode,strings[0],outXML,scannerId);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if(!b){
                Toast.makeText(getActivity(), "Cannot perform the action", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class UIUpdater extends AsyncTask<String,Integer,Boolean> {
        private CustomProgressDialog progressDialog;
        private boolean bFwReboot;

        public UIUpdater(boolean fwReboot){
            bFwReboot = fwReboot;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(getActivity(), "Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }


        @Override
        protected Boolean doInBackground(String... strings) {
            return  ShowFirmwareInfo(bFwReboot);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if(!b){
                Toast.makeText(getActivity(), "Cannot perform the action", Toast.LENGTH_SHORT).show();
            }
        }
    }

}

package com.zebra.demo.scanner.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Xml;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.scanner.helpers.Constants;
import com.zebra.demo.scanner.helpers.ScannerAppEngine;
import com.zebra.scannercontrol.BarCodeView;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.DCSScannerInfo;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.ArrayList;

public class ScannerHomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener,ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate{
    //private FrameLayout llBarcode;
    private NavigationView navigationView;
    Menu menu;
    MenuItem pairNewScannerMenu;
    private static final int PERMISSIONS_ACCESS_COARSE_LOCATION = 10;
    private static final int MAX_ALPHANUMERIC_CHARACTERS = 12;
    private static final int MAX_BLUETOOTH_ADDRESS_CHARACTERS = 17;
    private static final String DEFAULT_EMPTY_STRING = "";
    private static final String COLON_CHARACTER = ":";
    public static final String BLUETOOTH_ADDRESS_VALIDATOR = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$";
    static boolean firstRun = true;
    Dialog dialog;
    Dialog dialogBTAddress;
    static String btAddress;
    static String userEnteredBluetoothAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_scanner);

        Configuration configuration = getResources().getConfiguration();
        if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            if(configuration.smallestScreenWidthDp<Application.minScreenWidth){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }else{
            if(configuration.screenWidthDp<Application.minScreenWidth){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

         navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        menu = navigationView.getMenu();
        pairNewScannerMenu = menu.findItem(R.id.nav_pair_device);
        pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        }else{
            initialize();
            Intent intent = new Intent(this, ScannersActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
         }
    }

    private void initialize() {
        initializeDcsSdk();
        //llBarcode = (FrameLayout) findViewById(R.id.scan_to_connect_barcode);
        addDevConnectionsDelegate(this);
        //setTitle("Pair New Scanner");
        broadcastSCAisListening();
    }

    private void broadcastSCAisListening() {
        Intent intent = new Intent();
        intent.setAction("com.zebra.scannercontrol.LISTENING_STARTED");
        sendBroadcast(intent);
    }

    /*private void updateBarcodeView(LinearLayout.LayoutParams layoutParams, BarCodeView barCodeView) {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        int orientation =this.getResources().getConfiguration().orientation;
        int x = width * 9 / 10;
        int y = x / 3;
        if(getDeviceScreenSize()>6){ // TODO: Check 6 is ok or not
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                x =  width /2;
                y = x/3;
            }else {
                x =  width *2/3;
                y = x/3;
            }
        }
        barCodeView.setSize(x, y);
        //llBarcode.addView(barCodeView, layoutParams);
    }*/

    private double getDeviceScreenSize() {
        double screenInches = 0;
        WindowManager windowManager = getWindowManager();
        Display display = windowManager.getDefaultDisplay();

        int mWidthPixels;
        int mHeightPixels;

        try {
            Point realSize = new Point();
            Display.class.getMethod("getRealSize", Point.class).invoke(display, realSize);
            mWidthPixels = realSize.x;
            mHeightPixels = realSize.y;
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            double x = Math.pow(mWidthPixels/dm.xdpi,2);
            double y = Math.pow(mHeightPixels/dm.ydpi,2);
            screenInches = Math.sqrt(x+y);
        } catch (Exception ignored) {
        }
        return screenInches;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_ACCESS_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    initialize();

                } else {
                    finish();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void initializeDcsSdk(){
        Application.sdkHandler.dcssdkEnableAvailableScannersDetection(true);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        // getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if(id==R.id.nav_pair_device){
            selectItem(1);
        }else if (id == R.id.nav_devices) {
            selectItem(2);
        }else if (id == R.id.nav_find_cabled_scanner) {
            selectItem(3);
        }else if (id == R.id.nav_connection_help) {
            selectItem(4);
        } else if (id == R.id.nav_settings) {
            selectItem(5);
        } else if (id == R.id.nav_about) {
            selectItem(6);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }


    @Override
    protected void onDestroy(){
    // TODO: https://jiraemv.zebra.com/browse/SSDK-5961
    // Application.sdkHandler.dcssdkClose();
        super.onDestroy();
    }


    @Override
    protected void onPause() {
        super.onPause();
        removeDevConnectiosDelegate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDevConnectionsDelegate(this);
        navigationView.getMenu().findItem(R.id.nav_about).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_pair_device).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_pair_device).setCheckable(false);
        navigationView.getMenu().findItem(R.id.nav_devices).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_connection_help).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_settings).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_find_cabled_scanner).setChecked(false);
        navigationView.getMenu().findItem(R.id.nav_find_cabled_scanner).setCheckable(false);
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        //TextView txtBarcodeType = (TextView)findViewById(R.id.scan_to_connect_barcode_type);
        //TextView txtScannerConfiguration = (TextView)findViewById(R.id.scan_to_connect_scanner_config);
        String sourceString = "";
        //txtBarcodeType.setText(Html.fromHtml(sourceString));
        //txtScannerConfiguration.setText("");
        boolean dntShowMessage = settings.getBoolean(Constants.PREF_DONT_SHOW_INSTRUCTIONS, false);
        int barcode = settings.getInt(Constants.PREF_PAIRING_BARCODE_TYPE, 0);
        boolean setDefaults = settings.getBoolean(Constants.PREF_PAIRING_BARCODE_CONFIG, true);
        int protocolInt = settings.getInt(Constants.PREF_COMMUNICATION_PROTOCOL_TYPE, 0);
        String strProtocol = "SSI over Classic Bluetooth";
        //llBarcode = (FrameLayout) findViewById(R.id.scan_to_connect_barcode);
        DCSSDKDefs.DCSSDK_BT_PROTOCOL protocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.LEGACY_B;
        DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG config = DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG.KEEP_CURRENT;
        if(barcode ==0){
            //txtBarcodeType.setText("");
            //txtScannerConfiguration.setText("");
            sourceString = "STC Barcode ";
            //txtBarcodeType.setText(Html.fromHtml(sourceString));
            switch (protocolInt){
                case 0:
                    protocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_CRADLE_HOST;//SSI over Classic Bluetooth
                    strProtocol = "SSI over Classic Bluetooth";
                    break;
                case 1:
                    protocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_LE;//SSI over Bluetooth LE
                    strProtocol = "Bluetooth LE";
                    break;
                default:
                    protocol = DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_CRADLE_HOST;//SSI over Classic Bluetooth
                    break;
            }
            if(setDefaults){
                config = DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG.SET_FACTORY_DEFAULTS;
                //txtScannerConfiguration.setText(Html.fromHtml("<i> Set Factory Defaults, Com Protocol = "+strProtocol+"</i>"));
            }else{
                //txtScannerConfiguration.setText(Html.fromHtml("<i> Keep Current Settings, Com Protocol = "+strProtocol+"</i>"));
            }
        }else{
            sourceString = "Legacy Pairing ";
            //txtBarcodeType.setText(Html.fromHtml(sourceString));
            //txtScannerConfiguration.setText("");
        }
        selectedProtocol = protocol;
        selectedConfig = config;

    }

    private void generatePairingBarcode() {
        SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);
        BarCodeView barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(selectedProtocol, selectedConfig);
        if(barCodeView!=null) {
            //updateBarcodeView(layoutParams, barCodeView);
        }else{
            // SDK was not able to determine Bluetooth MAC. So call the dcssdkGetPairingBarcode with BT Address.

            btAddress= getDeviceBTAddress(settings);
            if(btAddress.equals("")){
                //llBarcode.removeAllViews();
            }else {
                Application.sdkHandler.dcssdkSetBTAddress(btAddress);
             //shashi                barCodeView = Application.sdkHandler.dcssdkGetPairingBarcode(selectedProtocol, selectedConfig, btAddress);
             //shashi                if (barCodeView != null) {
             //shashi                    updateBarcodeView(layoutParams, barCodeView);
             //shashi                    }
            }
        }
    }

    private String getDeviceBTAddress(SharedPreferences settings) {
        String bluetoothMAC = settings.getString(Constants.PREF_BT_ADDRESS, "");
        if (bluetoothMAC.equals("")) {
            if (dialogBTAddress == null) {
                dialogBTAddress = new Dialog(ScannerHomeActivity.this);
                dialogBTAddress.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogBTAddress.setContentView(R.layout.dialog_get_bt_address);

                final TextView cancelContinueButton = (TextView) dialogBTAddress.findViewById(R.id.cancel_continue);
                final TextView abtPhoneButton = (TextView) dialogBTAddress.findViewById(R.id.abt_phone);
                final TextView skipButton = dialogBTAddress.findViewById(R.id.skip);
                abtPhoneButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent statusSettings = new Intent(Settings.ACTION_DEVICE_INFO_SETTINGS);
                        startActivity(statusSettings);
                    }

                });
                cancelContinueButton.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("ApplySharedPref")
                    @Override
                    public void onClick(View view) {
                        if (cancelContinueButton.getText().equals(getResources().getString(R.string.cancel))) {
                            finish();
                        } else {
                            Application.sdkHandler.dcssdkSetSTCEnabledState(true);
                            SharedPreferences.Editor settingsEditor = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
                            settingsEditor.putString(Constants.PREF_BT_ADDRESS, userEnteredBluetoothAddress).commit();// Commit is required here. So suppressing warning.
                            if (dialogBTAddress != null) {
                                dialogBTAddress.dismiss();
                                dialogBTAddress = null;
                            }
                            startHomeActivityAgain();
                        }
                    }
                });

                skipButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Application.sdkHandler.dcssdkSetSTCEnabledState(false);
                        Intent intent = new Intent(ScannerHomeActivity.this, ScannersActivity.class);
                        startActivity(intent);
                    }
                });

                final EditText editTextBluetoothAddress = (EditText) dialogBTAddress.findViewById(R.id.text_bt_address);
                editTextBluetoothAddress.addTextChangedListener(new TextWatcher() {
                    String previousMac = null;
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String enteredMacAddress = editTextBluetoothAddress.getText().toString().toUpperCase();
                        String cleanMacAddress = clearNonMacCharacters(enteredMacAddress);
                        String formattedMacAddress = formatMacAddress(cleanMacAddress);

                        int selectionStart = editTextBluetoothAddress.getSelectionStart();
                        formattedMacAddress = handleColonDeletion(enteredMacAddress, formattedMacAddress, selectionStart);
                        int lengthDiff = formattedMacAddress.length() - enteredMacAddress.length();

                        setMacEdit(cleanMacAddress, formattedMacAddress, selectionStart, lengthDiff);

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        userEnteredBluetoothAddress = s.toString();
                        if(userEnteredBluetoothAddress.length() > MAX_BLUETOOTH_ADDRESS_CHARACTERS)
                            return;

                        if (isValidBTAddress(userEnteredBluetoothAddress)) {

                            Drawable dr = getResources().getDrawable(R.drawable.tick);
                            dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
                            editTextBluetoothAddress.setCompoundDrawables(null, null, dr, null);
                            cancelContinueButton.setText(getResources().getString(R.string.continue_txt));

                        } else {
                            editTextBluetoothAddress.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                            cancelContinueButton.setText(getResources().getString(R.string.cancel));

                        }
                    }

                    /**
                     * Strips all characters from a string except A-F and 0-9
                     * (Keep Bluetooth address allowed characters only).
                     *
                     * @param inputMacString User input string.
                     * @return String containing bluetooth MAC-allowed characters.
                     */
                    private String clearNonMacCharacters(String inputMacString) {
                        return inputMacString.toString().replaceAll("[^A-Fa-f0-9]", DEFAULT_EMPTY_STRING);
                    }

                    /**
                     * Adds a colon character to an unformatted bluetooth MAC address after
                     * every second character (strips full MAC trailing colon)
                     *
                     * @param cleanMacAddress Unformatted MAC address.
                     * @return Properly formatted MAC address.
                     */
                    private String formatMacAddress(String cleanMacAddress) {
                        int groupedCharacters = 0;
                        String formattedMacAddress = DEFAULT_EMPTY_STRING;

                        for (int i = 0; i < cleanMacAddress.length(); ++i) {
                            formattedMacAddress += cleanMacAddress.charAt(i);
                            ++groupedCharacters;

                            if (groupedCharacters == 2) {
                                formattedMacAddress += COLON_CHARACTER;
                                groupedCharacters = 0;
                            }
                        }

                        // Removes trailing colon for complete MAC address
                        if (cleanMacAddress.length() == MAX_ALPHANUMERIC_CHARACTERS)
                            formattedMacAddress = formattedMacAddress.substring(0, formattedMacAddress.length() - 1);

                        return formattedMacAddress;
                    }

                    /**
                     * Upon users colon deletion, deletes bluetooth MAC character preceding deleted colon as well.
                     *
                     * @param enteredMacAddress     User input MAC.
                     * @param formattedMacAddress   Formatted MAC address.
                     * @param selectionStartPosition MAC EditText field cursor position.
                     * @return Formatted MAC address.
                     */
                    private String handleColonDeletion(String enteredMacAddress, String formattedMacAddress, int selectionStartPosition) {
                        if (previousMac != null && previousMac.length() > 1) {
                            int previousColonCount = colonCount(previousMac);
                            int currentColonCount = colonCount(enteredMacAddress);

                            if (currentColonCount < previousColonCount) {
                               try {
                                   formattedMacAddress = formattedMacAddress.substring(0, selectionStartPosition - 1) + formattedMacAddress.substring(selectionStartPosition);
                               }catch (Exception e){
                                  Log.d(TAG,  "Returned SDK Exception");
                               }
                                String cleanMacAddress = clearNonMacCharacters(formattedMacAddress);
                                formattedMacAddress = formatMacAddress(cleanMacAddress);
                            }
                        }
                        return formattedMacAddress;
                    }

                    /**
                     * Gets bluetooth MAC address current colon count.
                     *
                     * @param formattedMacAddress Formatted MAC address.
                     * @return Current number of colons in MAC address.
                     */
                    private int colonCount(String formattedMacAddress) {
                        return formattedMacAddress.replaceAll("[^:]", DEFAULT_EMPTY_STRING).length();
                    }

                    /**
                     * Removes TextChange listener, sets MAC EditText field value,
                     * sets new cursor position and re-initiates the listener.
                     *
                     * @param cleanMacAddress       Clean MAC address.
                     * @param formattedMacAddress   Formatted MAC address.
                     * @param selectionStartPosition MAC EditText field cursor position.
                     * @param characterDifferenceLength     Formatted/Entered MAC number of characters difference.
                     */
                    private void setMacEdit(String cleanMacAddress, String formattedMacAddress, int selectionStartPosition, int characterDifferenceLength) {
                        editTextBluetoothAddress.removeTextChangedListener(this);
                        if (cleanMacAddress.length() <= MAX_ALPHANUMERIC_CHARACTERS) {
                            editTextBluetoothAddress.setText(formattedMacAddress);

                            editTextBluetoothAddress.setSelection(selectionStartPosition + characterDifferenceLength);
                            previousMac = formattedMacAddress;
                        } else {
                            editTextBluetoothAddress.setText(previousMac);
                            editTextBluetoothAddress.setSelection(previousMac.length());
                        }
                        editTextBluetoothAddress.addTextChangedListener(this);
                    }

                });

                dialogBTAddress.setCancelable(false);
                dialogBTAddress.setCanceledOnTouchOutside(false);
                dialogBTAddress.show();
                Window window = dialogBTAddress.getWindow();
                if(window!=null) window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                bluetoothMAC = settings.getString(Constants.PREF_BT_ADDRESS, "");
            } else {
                dialogBTAddress.show();
            }
        }
        return bluetoothMAC;
    }

    private void startHomeActivityAgain() {
        Intent i = new Intent(this, ScannerHomeActivity.class);
        i.setAction(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        startActivity(i);
    }

    public boolean isValidBTAddress(String text) {
        return text != null && text.length() > 0 && text.matches(BLUETOOTH_ADDRESS_VALIDATOR);
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reloadBarcode();
        if(dialog !=null){
            Window window = dialog.getWindow();
            if(window!=null) window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }
    }

    private void reloadBarcode() {
       generatePairingBarcode();
    }

    @Override
    public boolean scannerHasAppeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasDisappeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasConnected(int scannerID) {


        SsaSetSymbologyActivity.resetScanSpeedAnalyticSettings();

        ArrayList<DCSScannerInfo> activeScanners = new ArrayList<DCSScannerInfo>();
        Application.sdkHandler.dcssdkGetActiveScannersList(activeScanners);
        Intent intent = new Intent(ScannerHomeActivity.this, ActiveScannerActivity.class);
        pairNewScannerMenu.setTitle(R.string.menu_item_device_disconnect);

        for (DCSScannerInfo scannerInfo : Application.mScannerInfoList) {
            if (scannerInfo.getScannerID() == scannerID) {
                intent.putExtra(Constants.SCANNER_NAME, scannerInfo.getScannerName());
                intent.putExtra(Constants.SCANNER_TYPE, scannerInfo.getConnectionType().value);
                intent.putExtra(Constants.SCANNER_ADDRESS, scannerInfo.getScannerHWSerialNumber());
                intent.putExtra(Constants.SCANNER_ID, scannerInfo.getScannerID());
                intent.putExtra(Constants.AUTO_RECONNECTION, scannerInfo.isAutoCommunicationSessionReestablishment());
                intent.putExtra(Constants.CONNECTED, true);
                intent.putExtra(Constants.PICKLIST_MODE,getPickListMode(scannerID));

                if(scannerInfo.getScannerModel() !=null && scannerInfo.getScannerModel().startsWith("PL3300")){ // remove this condition when CS4070 get the capability
                    intent.putExtra(Constants.PAGER_MOTOR_STATUS, true);
                }else {
                    intent.putExtra(Constants.PAGER_MOTOR_STATUS, isPagerMotorAvailable(scannerID));
                }

                intent.putExtra(Constants.BEEPER_VOLUME,getBeeperVolume(scannerID));

                Application.isAnyScannerConnected = true;
                Application.currentConnectedScannerID = scannerID;
                Application.currentConnectedScanner = scannerInfo;
                Application.lastConnectedScanner = Application.currentConnectedScanner;
                startActivity(intent);
                break;
            }
        }
        return true;
    }

    private int getBeeperVolume(int scannerID) {
        int beeperVolume = 0;

        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>140</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET,in_xml,outXML,scannerID);

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
                        if (name.equals("value")) {
                            beeperVolume = Integer.parseInt(text != null ? text.trim() : null);
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        if(beeperVolume == 0){
            return 100;
        }else if(beeperVolume == 1){
            return 50;
        }else{
            return 0;
        }
    }

    private boolean isPagerMotorAvailable(int scannerID) {
        boolean isFound = false;
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>613</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET,in_xml,outXML,scannerID);
        if(outXML.toString().contains("<id>613</id>")){
            isFound = true;
        }
        return isFound;
    }


    private int getPickListMode(int scannerID) {
        int attrVal = 0;
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>402</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET,in_xml,outXML,scannerID);

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
                        if (name.equals("value")) {
                            attrVal = Integer.parseInt(text != null ? text.trim() : null);
                        }
                        break;
                }
                event = parser.next();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return attrVal;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        pairNewScannerMenu.setTitle(R.string.menu_item_device_pair);
        Application.isAnyScannerConnected = false;
        Application.currentConnectedScannerID = -1;
        Application.lastConnectedScanner = Application.currentConnectedScanner;
        Application.currentConnectedScanner = null;
        return false;
    }

    /**
     * Paired Bluetooth in {@link com.zebra.demo.scanner.activities.PairNewScannerActivity} is selected
     */
    public void pairBtClicked(View view) {
        selectNavigationMenuItem(0);
        selectItem(1);
    }

    /**
     * Available Scaner in {@link com.zebra.demo.scanner.activities.ScannersActivity} is selected
     */
    public void availListClicked(View view) {
        selectNavigationMenuItem(1);
        selectItem(2);
    }

    /**
     * Paired Bluetooth in {@link com.zebra.demo.scanner.activities.PairNewScannerActivity} is selected
     */
    public void findCabledScanClicked(View view) {
        selectNavigationMenuItem(2);
        selectItem(3);
    }

    /**
     * Paired Bluetooth in {@link com.zebra.demo.scanner.activities.PairNewScannerActivity} is selected
     */
    public void connHelpClicked(View view) {
        selectNavigationMenuItem(3);
        selectItem(4);
    }

    /**
     * Paired Bluetooth in {@link com.zebra.demo.scanner.activities} is selected
     */
    public void appSettingsClicked(View view) {
        selectNavigationMenuItem(4);
        selectItem(5);
    }

    /**
     * Paired Bluetooth in {@link com.zebra.demo.scanner.activities} is selected
     */
    public void appOverviewClicked(View view) {
        selectNavigationMenuItem(5);
        selectItem(6);
    }

    public void selectNavigationMenuItem(int pos) {
        navigationView.getMenu().getItem(pos).setChecked(true);
    }

    void selectItem(int position) {
        Intent intent;
        switch (position) {
            case 1:
                if(Application.isAnyScannerConnected) {
                    AlertDialog.Builder dlg = new  AlertDialog.Builder(this);
                    dlg.setTitle("This will disconnect your current scanner");
                    //dlg.setIcon(android.R.drawable.ic_dialog_alert);
                    dlg.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg) {

                            disconnect(Application.currentConnectedScannerID);
                            barcodeQueue.clear();
                            Application.currentScannerId =Application.SCANNER_ID_NONE;
                            finish();
                            Intent intent = new Intent(ScannerHomeActivity.this, PairNewScannerActivity.class);
                            startActivity(intent);
                        }
                    });

                    dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int arg) {

                        } });
                    dlg.show();
                } else {
                    Intent intent1 = new Intent(ScannerHomeActivity.this, PairNewScannerActivity.class);
                    startActivity(intent1);
                }
                break;
            case 2:
                intent = new Intent(this, ScannersActivity.class);
                startActivity(intent);
                break;
            case 3:
                intent = new Intent(this, FindCabledScanner.class);
                startActivity(intent);
                break;
            case 4:
                intent = new Intent(this, ConnectionHelpActivity2.class);
                startActivity(intent);
                break;
            case 5:
                intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                break;
            case 6:
                intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                break;
            default :
                break;
        }
    }
}

package com.zebra.demo;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.zebra.demo.application.Application;
import com.zebra.demo.discover_connect.nfc.PairOperationsFragment;
import com.zebra.demo.rfidreader.common.CustomProgressDialog;
import com.zebra.demo.rfidreader.common.CustomToast;
import com.zebra.demo.rfidreader.common.ResponseHandlerInterfaces;
import com.zebra.demo.rfidreader.home.RFIDEventHandler;
import com.zebra.demo.rfidreader.notifications.NotificationUtil;
import com.zebra.demo.rfidreader.reader_connection.InitReadersListFragment;
import com.zebra.demo.rfidreader.reader_connection.PasswordDialog;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.demo.rfidreader.settings.BatteryFragment;
import com.zebra.demo.rfidreader.settings.SettingsContent;
import com.zebra.demo.rfidreader.settings.SettingsDetailActivity;
import com.zebra.demo.scanner.activities.BaseActivity;
import com.zebra.demo.scanner.activities.NavigationHelpActivity;
import com.zebra.demo.scanner.activities.UpdateFirmware;
import com.zebra.demo.scanner.fragments.ReaderDetailsFragment;
import com.zebra.demo.scanner.helpers.AvailableScanner;
import com.zebra.demo.scanner.helpers.Constants;
import com.zebra.demo.scanner.helpers.ScannerAppEngine;
import com.zebra.rfid.api3.BuildConfig;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.VersionInfo;
import com.zebra.scannercontrol.DCSSDKDefs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.zebra.demo.rfidreader.rfid.RFIDController.TAG;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;
//import com.zebra.scannercontrol.SDKHandler;

public class DeviceDiscoverActivity extends BaseActivity implements Readers.RFIDReaderEventHandler, ResponseHandlerInterfaces.ReaderDeviceFoundHandler, ResponseHandlerInterfaces.BatteryNotificationHandler, ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate {

    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 100;
    DrawerLayout mDrawerLayout;
    NavigationView navigationView;
    Button btnRfid;
    Button btnScanner;
    Button btnFirmware;
    Button btnDiscovery;
    Fragment fragment = null;
    public static RFIDEventHandler mEventHandler;
    private CustomProgressDialog progressDialog;
    private AvailableScanner curAvailableScanner;
    private DeviceDiscoverActivity mDeviceDiscoverActivity;
    Toolbar toolbar;
    ImageView iv_batteryLevel ,iv_headerImageView;
    TextView battery_percentage;
    Button btn_disconnect;
    //private boolean launchAppHome = false;
    public String nfcData;
    public static ReaderDevice mConnectedReaderDetails;
    private Bundle mSavedInstanceState = null;
    private int vendorId = 0x05E0;
    private int productId = 0x1701;
    private static final String INTENT_ACTION_GRANT_USB = "com.zebra.rfid.app.USB_PERMISSION";


    protected Boolean isActivityRunning(Class activityClass)
    {
        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (activityClass.getCanonicalName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDeviceDiscoverActivity = this;
        mSavedInstanceState = savedInstanceState;
        //setContentView(R.layout.activity_settings_detail);
        if(mConnectedReader != null )
        {
            Log.d(TAG, "There is no way you can come here ");
        }
        setContentView(R.layout.discover_activity_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.dis_toolbar);
        setSupportActionBar(toolbar);
        toolbar = (Toolbar) findViewById(R.id.dis_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_empty_readers));
        mDrawerLayout = (DrawerLayout) findViewById(R.id.discover_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout,toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        iv_batteryLevel = (ImageView) findViewById(R.id.disbatteryLevel);
        battery_percentage = (TextView) findViewById(R.id.battery_percentage);
        btn_disconnect = findViewById(R.id.disconnect_btn);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (RFIDController.BatteryData != null){
                    batteryStatus(RFIDController.BatteryData.getLevel(), RFIDController.BatteryData.getCharging(), RFIDController.BatteryData.getCause());
                }
                if(mConnectedReader != null && mConnectedReader.isConnected()) {
                    btn_disconnect.setEnabled(true);
                    btn_disconnect.setText("DISCONNECT "+ mConnectedReader.getHostName());
                } else {
                    btn_disconnect.setEnabled(false);
                    btn_disconnect.setText(R.string.disconnectrfid);
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                battery_percentage.setText(String.valueOf(0)+"%");
                iv_batteryLevel.setImageLevel(0);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.disnav_view);
        navigationView.setNavigationItemSelectedListener(this::onOptionsItemSelected);
        View headerImageView = navigationView.getHeaderView(0);
        iv_headerImageView = headerImageView.findViewById(R.id.imageView);
        iv_headerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
            }
        });


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        deleteDWProfile();
        // RFIDHomeActivity.addReaderDeviceFoundHandler(this);
        // RFIDHomeActivity.addBatteryNotificationHandler(this);

        if (RFIDController.readers == null) {
            RFIDController.readers = new Readers(this.getApplicationContext(), ENUM_TRANSPORT.ALL);
        }
        // attach to reader list handler
        RFIDController.readers.attach(this);
        if (savedInstanceState == null) {
            //loadReaders(this);
            mEventHandler = new RFIDEventHandler();
        }
        //Scanner Initializations
        //Handling Runtime BT permissions for Android 12 and higher
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT},
                        BLUETOOTH_PERMISSION_REQUEST_CODE);
            }else{
                initialize();
            }

        }else{
            initialize();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initialize();
            }
            else {
                Toast.makeText(this, "Bluetooth Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String str = getIntent().getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            nfcData = ((com.zebra.demo.application.Application)getApplication()).processNFCData(intent);
        }else if( NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
            processTAGData(intent);

    }
    public void deleteDWProfile()
    {
        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        String[] values = {"RFIDMobileApp"};
        i.putExtra("com.symbol.datawedge.api.DELETE_PROFILE", values);
        mDeviceDiscoverActivity.sendBroadcast(i);

    }
    private void processTAGData(Intent intent) {
        Log.i(TAG,"ProcessTAG data " );


        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_TAG);
        if (rawMessages != null && rawMessages.length > 0) {

            NdefMessage[] messages = new NdefMessage[rawMessages.length];

            for (int i = 0; i < rawMessages.length; i++) {

                messages[i] = (NdefMessage) rawMessages[i];

            }

            Log.i(TAG, "message size = " + messages.length);

            //TextView veiw = findViewById(R.id.viewdata);
            ///if ( veiw != null ) {
            // only one message sent during the beam
            NdefMessage msg = (NdefMessage) rawMessages[0];
            // record 0 contains the MIME type, record 1 is the AAR, if present
            String base = new String(msg.getRecords()[0].getPayload());
            String str = String.format(Locale.getDefault(), "Message entries=%d. Base message is %s", rawMessages.length, base);
            //    veiw.setText(str);
            Log.i(TAG, "message  = " + str);
            //}

        }
    }



    public String copyNfcContent() {
        return nfcData;

    }

    private void batteryStatus(int level, boolean charging, String cause) {
        battery_percentage.setText(String.valueOf(level)+"%");
        iv_batteryLevel.setImageLevel(level);
    }

    /* @Override
     protected void onNewIntent(Intent intent) {
         super.onNewIntent(intent);
         if(!intent.getBooleanExtra("enable_toolbar", true)) {
             getSupportActionBar().hide();
         }
     }*/
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (INTENT_ACTION_GRANT_USB.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            onPostPermissionGranted();
                        }
                    }
                    else {
                        Log.d(TAG, "USB permission denied for device " + device);
                    }
                }
            }
            unregisterReceiver(usbReceiver);
        }
    };


    private void initialize() {
        PendingIntent permissionIntent;
        UsbManager usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        if(usbManager.getDeviceList().size() > 0) {
            for (UsbDevice device : usbManager.getDeviceList().values()) {
                if ((device.getVendorId() == vendorId) && (device.getProductId() == productId)) {
                    if (!usbManager.hasPermission(device)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                             permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE);
                        }else{
                             permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(INTENT_ACTION_GRANT_USB), PendingIntent.FLAG_UPDATE_CURRENT);
                        }
                        IntentFilter filter = new IntentFilter(INTENT_ACTION_GRANT_USB);
                        registerReceiver(usbReceiver, filter);
                        usbManager.requestPermission(device, permissionIntent);
                        break;
                    } else {
                        onPostPermissionGranted();
                        break;
                    }
                }
            }
        }else {
            onPostPermissionGranted();
        }
    }

        private void onPostPermissionGranted(){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                requestBTEnable();
            }else{
                initializeDcsSdk(true);
            }
        }
    private void broadcastSCAisListening() {
        Intent intent = new Intent();
        intent.setAction("com.zebra.scannercontrol.LISTENING_STARTED");
        sendBroadcast(intent);
    }

    private void requestBTEnable() {
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activityResultLauncher.launch(enableBtIntent);
        }else{
            initializeDcsSdk(true);
        }
    }
    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> initializeDcsSdk(result.getResultCode() == Activity.RESULT_OK));

    private void initializeDcsSdk(boolean enableBTConnect){
        Application.sdkHandler.dcssdkEnableAvailableScannersDetection(true);
        if(enableBTConnect){
            Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL);
            Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE);
        }
        //Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_SNAPI);
        Application.sdkHandler.dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_USB_CDC);


        addDevConnectionsDelegate(this);

        broadcastSCAisListening();

        if (mSavedInstanceState == null) {
            fragment = InitReadersListFragment.getInstance();
            switchToFragment(fragment);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu_app_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        switch (item.getItemId()) {
            case R.id.nav_fw_update:
                if(mConnectedReader != null && mConnectedReader.isConnected()) {
                    loadUpdateFirmware(item.getActionView());
                } else {
                    Toast.makeText(this, "No device in connected state", Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.nav_connection_help:
                Intent helpIntent = new Intent(this, NavigationHelpActivity.class);
                startActivity(helpIntent);
                return true;

            case R.id.nav_battery_statics:

                Intent detailsIntent = new Intent(this, SettingsDetailActivity.class);
                detailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                detailsIntent.putExtra(com.zebra.demo.rfidreader.common.Constants.SETTING_ITEM_ID, Integer.parseInt(SettingsContent.ITEMS.get(3).id));
                startActivity(detailsIntent);

                return true;
            case R.id.action_add: {
                /*
                 * Show only paired bluetooth devices.
                 */
                updateScannersList();
                return true;
            }
            case R.id.action_info:
                //((RFIDHomeActivity) getActivity()).aboutClicked();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadUpdateFirmware(View actionView) {
        Intent intent = new Intent(this, UpdateFirmware.class);
        intent.putExtra(Constants.SCANNER_ID, Application.currentConnectedScannerID);
        intent.putExtra(Constants.SCANNER_NAME, getIntent().getStringExtra(Constants.SCANNER_NAME));
        //startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if(mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }

        /*if(RFIDController.mConnectedReader == null) {
            Intent intent = new Intent(this, DeviceDiscoverActivity.class);
            intent.putExtra("enable_toolbar", false);
            startActivity(intent);
        }*/
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (currentFragment != null && (currentFragment instanceof PairOperationsFragment) || currentFragment instanceof ReaderDetailsFragment){
            setActionBarTitle("Readers");
            Fragment fragment = InitReadersListFragment.getInstance();
            if (fragment != null) {
                switchToFragment(fragment);
            }
        } else {
            minimizeApp();

        }
    }
    private void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    @Override
    protected void onResume() {
        super.onResume();
      //  setActionBarTitle(getResources().getString(R.string.title_empty_readers));

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_rfid :
                startRfidActivity();
                break;
            case R.id.btn_scanner :
                //startScannerActivity();
                break;
            case R.id.btn_update_firmware :
                startFirmwareUpdate();
                break;
            case R.id.btn_discovery :
                // Yet to implement
                break;
            default:
                break;
        }
    }

    private void startRfidActivity() {
    }



    private void startFirmwareUpdate() {
        if(Application.currentConnectedScannerID != -1 && Application.currentConnectedScanner != null) {
            Intent intent = new Intent(this, UpdateFirmware.class);
            intent.putExtra(Constants.SCANNER_ID, Application.currentConnectedScannerID);
            intent.putExtra(Constants.SCANNER_NAME, Application.currentConnectedScanner.getScannerName());
            intent.putExtra(Constants.FW_REBOOT, true);
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //startActivity(intent);
            //setWaitingForFWReboot(false);
        } else {
            Toast.makeText(this, R.string.toast_scanner_not_attached, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        runOnUiThread(() -> {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
            if (fragment instanceof InitReadersListFragment) {
                ((InitReadersListFragment) fragment).RFIDReaderAppeared(readerDevice);
            }
        });
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice readerDevice) {
        runOnUiThread(() -> {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
            if (fragment instanceof InitReadersListFragment) {
                ((InitReadersListFragment) fragment).RFIDReaderDisappeared(readerDevice);
            }
        });
    }

    @Override
    public void ReaderDeviceConnected(ReaderDevice device) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof InitReadersListFragment) {
            ((InitReadersListFragment) fragment).ReaderDeviceConnected(device);
        }
    }

    @Override
    public void ReaderDeviceDisConnected(ReaderDevice device) {
        PasswordDialog.isDialogShowing = false;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof InitReadersListFragment) {
            ((InitReadersListFragment) fragment).ReaderDeviceDisConnected(device);
            ((InitReadersListFragment) fragment).readerDisconnected(device, false);
        }
    }

    @Override
    public void ReaderDeviceConnFailed(ReaderDevice device) {

    }

    @Override
    public void deviceStatusReceived(int level, boolean charging, String cause) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof BatteryFragment) {
            ((BatteryFragment) fragment).deviceStatusReceived(level, charging, cause);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RFIDController.readers.deattach(this);
        if(mConnectedReader != null ) {
            try {
                mConnectedReader.Events.removeEventsListener(mEventHandler);

            } catch (InvalidUsageException e) {
                if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            } catch (OperationFailureException e) {
                if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            }
        }
    }

    /**
     * method to know whether bluetooth is enabled or not
     *
     * @return - true if bluetooth enabled
     * - false if bluetooth disabled
     */
    public static boolean isBluetoothEnabled() {
        return BluetoothAdapter.getDefaultAdapter().isEnabled();
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
        return false;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        return false;
    }

    public void sendNotification(String action, String data) {
        if (action.equalsIgnoreCase(com.zebra.demo.rfidreader.common.Constants.ACTION_READER_BATTERY_CRITICAL) || action.equalsIgnoreCase(com.zebra.demo.rfidreader.common.Constants.ACTION_READER_BATTERY_LOW)) {
            new CustomToast(this, R.layout.toast_layout, data).show();
        } else {
            Toast.makeText(this, data, Toast.LENGTH_SHORT).show();
        }

        NotificationUtil.displayNotification(getApplicationContext(), action, data);
    }

    public void switchToFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings_content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();
        }
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public void loadReaderDetails(ReaderDevice readerDevice) {
        connectedReaderDetails(readerDevice);
        fragment = ReaderDetailsFragment.newInstance();
        switchToFragment(fragment);
    }
    private void connectedReaderDetails(ReaderDevice readerDevice) {

        mConnectedReaderDetails = readerDevice;
    }

    public ReaderDevice connectedReaderDetails() {
        return mConnectedReaderDetails;
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics() );
        return res;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (newConfig.fontScale != 1)//Non-default
            getResources();
        super.onConfigurationChanged(newConfig);

    }

    public NfcAdapter.CreateNdefMessageCallback _onNfcCreateCallback = new NfcAdapter.CreateNdefMessageCallback() {
        @Override
        public NdefMessage createNdefMessage(NfcEvent inputNfcEvent) {
            Log.i(TAG, "createNdefMessage");
            return createMessage();
        }
    };

    private NdefMessage createMessage() {
        String text = ("Hello there from another device!\n\n" +
                "Beam Time: " + System.currentTimeMillis());
        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { NdefRecord.createMime(
                        "application/com.bluefletch.nfcdemo.mimetype", text.getBytes())
                        /**
                         * The Android Application Record (AAR) is commented out. When a device
                         * receives a push with an AAR in it, the application specified in the AAR
                         * is guaranteed to run. The AAR overrides the tag dispatch system.
                         * You can add it back in to guarantee that this
                         * activity starts when receiving a beamed message. For now, this code
                         * uses the tag dispatch system.
                        */
                        //,NdefRecord.createApplicationRecord("com.example.android.beam")
                });

        return msg;
    }


}

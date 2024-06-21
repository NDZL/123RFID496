    package com.zebra.demo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.zebra.demo.application.Application;
import com.zebra.demo.discover_connect.nfc.PairOperationsFragment;
import com.zebra.demo.rfidreader.access_operations.AccessOperationsFragment;
import com.zebra.demo.rfidreader.common.CustomToast;
import com.zebra.demo.rfidreader.common.Inventorytimer;
import com.zebra.demo.rfidreader.common.MatchModeFileLoader;
import com.zebra.demo.rfidreader.common.ResponseHandlerInterfaces;
import com.zebra.demo.rfidreader.data_export.DataExportTask;
import com.zebra.demo.rfidreader.home.RFIDBaseActivity;
import com.zebra.demo.rfidreader.inventory.InventoryListItem;
import com.zebra.demo.rfidreader.inventory.RFIDInventoryFragment;
import com.zebra.demo.rfidreader.locate_tag.LocateOperationsFragment;
import com.zebra.demo.rfidreader.manager.FactoryResetFragment;
import com.zebra.demo.rfidreader.manager.ManagerFragment;
import com.zebra.demo.rfidreader.manager.ScanHomeSettingsFragment;
import com.zebra.demo.rfidreader.notifications.NotificationUtil;
import com.zebra.demo.rfidreader.rapidread.RapidReadFragment;
import com.zebra.demo.rfidreader.reader_connection.InitReadersListFragment;
import com.zebra.demo.rfidreader.reader_connection.RFIDReadersListFragment;
import com.zebra.demo.rfidreader.reader_connection.ScanAndPairFragment;
import com.zebra.demo.rfidreader.rfid.ConnectionController;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.demo.rfidreader.rfid.RfidListeners;
import com.zebra.demo.rfidreader.settings.AdvancedOptionItemFragment;
import com.zebra.demo.rfidreader.settings.AdvancedOptionsContent;
import com.zebra.demo.rfidreader.settings.AntennaSettingsFragment;
import com.zebra.demo.rfidreader.settings.ApplicationSettingsFragment;
import com.zebra.demo.rfidreader.settings.BackPressedFragment;
import com.zebra.demo.rfidreader.settings.BatteryFragment;
import com.zebra.demo.rfidreader.settings.BatteryStatsFragment;
import com.zebra.demo.rfidreader.settings.BeeperFragment;
import com.zebra.demo.rfidreader.settings.ChargeTerminalFragment;
import com.zebra.demo.rfidreader.settings.DPOSettingsFragment;
import com.zebra.demo.rfidreader.settings.ISettingsUtil;
import com.zebra.demo.rfidreader.settings.KeyRemapFragment;
import com.zebra.demo.rfidreader.settings.LedFragment;
import com.zebra.demo.rfidreader.settings.PreFilterFragment;
import com.zebra.demo.rfidreader.settings.ProfileFragment;
import com.zebra.demo.rfidreader.settings.RegulatorySettingsFragment;
import com.zebra.demo.rfidreader.settings.SaveConfigurationsFragment;
import com.zebra.demo.rfidreader.settings.SettingListFragment;
import com.zebra.demo.rfidreader.settings.SettingsContent;
import com.zebra.demo.rfidreader.settings.SettingsDetailActivity;
import com.zebra.demo.rfidreader.settings.SingulationControlFragment;
import com.zebra.demo.rfidreader.settings.StartStopTriggersFragment;
import com.zebra.demo.rfidreader.settings.TagReportingFragment;
import com.zebra.demo.rfidreader.settings.UsbMiFiFragment;
import com.zebra.demo.rfidreader.settings.WifiFragment;
import com.zebra.demo.scanner.activities.AssertFragment;
import com.zebra.demo.scanner.activities.BaseActivity;
import com.zebra.demo.scanner.activities.BatteryStatistics;
import com.zebra.demo.scanner.activities.BeeperActionsActivity;
import com.zebra.demo.scanner.activities.BeeperActionsFragment;
import com.zebra.demo.scanner.activities.ImageActivity;
import com.zebra.demo.scanner.activities.IntelligentImageCaptureActivity;
import com.zebra.demo.scanner.activities.LEDActivity;
import com.zebra.demo.scanner.activities.NavigationHelpActivity;
import com.zebra.demo.scanner.activities.SampleBarcodes;
import com.zebra.demo.scanner.activities.ScaleActivity;
import com.zebra.demo.scanner.activities.ScanSpeedAnalyticsActivity;
import com.zebra.demo.scanner.activities.SsaSetSymbologyActivity;
import com.zebra.demo.scanner.activities.SymbologiesActivity;
import com.zebra.demo.scanner.activities.SymbologiesFragment;
import com.zebra.demo.scanner.activities.UpdateFirmware;
import com.zebra.demo.scanner.activities.VibrationFeedback;
import com.zebra.demo.scanner.fragments.AdvancedFragment;
import com.zebra.demo.scanner.fragments.BarcodeFargment;
import com.zebra.demo.scanner.fragments.ReaderDetailsFragment;
import com.zebra.demo.scanner.fragments.SettingsFragment;
import com.zebra.demo.scanner.fragments.Static_ipconfig;
import com.zebra.demo.scanner.helpers.ActiveDeviceAdapter;
import com.zebra.demo.scanner.helpers.ActiveDevicePremiumAdapter;
import com.zebra.demo.scanner.helpers.ActiveDeviceStandardAdapter;
import com.zebra.demo.scanner.helpers.Constants;
import com.zebra.demo.scanner.helpers.CustomProgressDialog;
import com.zebra.demo.scanner.helpers.DotsProgressBar;
import com.zebra.demo.scanner.helpers.ScannerAppEngine;
import com.zebra.demo.scanner.receivers.NotificationsReceiver;
import com.zebra.demo.wifi.ReaderWifiSettingsFragment;
import com.zebra.demo.wifi.WifiPasswordDialog;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDResults;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.scannercontrol.DCSSDKDefs;
import com.zebra.scannercontrol.FirmwareUpdateEvent;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.Serializable;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import static com.zebra.demo.application.Application.DEVICE_PREMIUM_PLUS_MODE;
import static com.zebra.demo.application.Application.DEVICE_STD_MODE;
import static com.zebra.demo.application.Application.RFD_DEVICE_MODE;
import static com.zebra.demo.application.Application.TAG_LIST_LOADED;
import static com.zebra.demo.application.Application.TAG_LIST_MATCH_MODE;
import static com.zebra.demo.application.Application.TOTAL_TAGS;
import static com.zebra.demo.application.Application.UNIQUE_TAGS;
import static com.zebra.demo.application.Application.UNIQUE_TAGS_CSV;
import static com.zebra.demo.application.Application.currentFragment;
import static com.zebra.demo.application.Application.inventoryList;
import static com.zebra.demo.application.Application.mConnectedDevice;
import static com.zebra.demo.application.Application.matchingTags;
import static com.zebra.demo.application.Application.matchingTagsList;
import static com.zebra.demo.application.Application.memoryBankId;
import static com.zebra.demo.application.Application.missedTags;
import static com.zebra.demo.application.Application.missingTagsList;
import static com.zebra.demo.application.Application.tagListMap;
import static com.zebra.demo.application.Application.tagsListCSV;
import static com.zebra.demo.application.Application.tagsReadForSearch;
import static com.zebra.demo.application.Application.tagsReadInventory;
import static com.zebra.demo.application.Application.unknownTagsList;
import static com.zebra.demo.rfidreader.rfid.RFIDController.EXPORT_DATA;
import static com.zebra.demo.rfidreader.rfid.RFIDController.clearInventoryData;
import static com.zebra.demo.rfidreader.rfid.RFIDController.getInstance;
import static com.zebra.demo.rfidreader.rfid.RFIDController.isInventoryAborted;
import static com.zebra.demo.rfidreader.rfid.RFIDController.isLocatingTag;
import static com.zebra.demo.rfidreader.rfid.RFIDController.isLocationingAborted;
import static com.zebra.demo.rfidreader.rfid.RFIDController.isTriggerRepeat;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mInventoryStartPending;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mIsInventoryRunning;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mRRStartedTime;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mReaderDisappeared;
import static com.zebra.demo.rfidreader.rfid.RFIDController.tagListMatchNotice;
import static com.zebra.demo.scanner.activities.UpdateFirmware.isWaitingForFWUpdateToComplete;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.ANTENNA_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.APPLICATION_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.ASSERT_DEVICE_INFO_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.BARCODE_SYMBOLOGIES_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.BARCODE_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.BATTERY_STATISTICS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.BEEPER_ACTION_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.CHARGE_TERMINAL_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.DEVICE_PAIR_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.DEVICE_RESET_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.DPO_SETTING_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.FACTORY_RESET_FRAGMENT_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.INVENTORY_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.KEYREMAP_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.LOCATE_TAG_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.LOGGER_FRAGMENT_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.MAIN_GENERAL_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.MAIN_HOME_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.MAIN_RFID_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.NONOPER_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RAPID_READ_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.READERS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.READER_DETAILS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.READER_LIST_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.READER_WIFI_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_ACCESS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_ADVANCED_OPTIONS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_BEEPER_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_LED_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_PREFILTERS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_PROFILES_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_REGULATORY_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.RFID_WIFI_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SAVE_CONFIG_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SCAN_ADVANCED_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SCAN_DATAVIEW_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SCAN_HOME_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SCAN_SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SCAN_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SETTINGS_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.SINGULATION_CONTROL_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.START_STOP_TRIGGER_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.STATIC_IP_CONFIG;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.TAG_REPORTING_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.UPDATE_FIRMWARE_TAB;
import static com.zebra.demo.scanner.helpers.ActiveDeviceAdapter.USB_MIFI_TAB;
import static com.zebra.demo.scanner.helpers.Constants.DEBUG_TYPE.TYPE_DEBUG;
import static com.zebra.scannercontrol.RMDAttributes.NUM_STATUS_DECODE_OTHER_CNT_HEX_LI;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_ACTION_HIGH_HIGH_LOW_LOW_BEEP;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_2_OF_5;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_AZTEC;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODEBAR;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_11;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_128;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_39;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_93;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_COMPOSITE;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_COUPON;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_DATAMARIX;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_EAN_JAN;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_OTHER;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_UPC;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_EAN_JAN;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_DATABAR;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_DATAMATRIX;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_QR_CODE;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_MAXICODE;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_MSI;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_OCR;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER_1D;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER_2D;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_PDF;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_POSTAL_CODES;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_QR;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_UNUSED_ID;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_DECODE_COUNT_UPC;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_2_OF_5;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_AZTEC;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_CODEBAR;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_11;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_128;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_39;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_93;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_COMPOSITE;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_COUPON;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_DATAMARIX;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_DIGIMARC_EAN_JAN;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_DIGIMARC_OTHER;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_DIGIMARC_UPC;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_EAN_JAN;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_GS1_DATABAR;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_GS1_DATAMATRIX;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_GS1_QR_CODE;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_MAXICODE;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_MSI;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_OCR;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_OTHER;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_OTHER_1D;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_OTHER_2D;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_PDF;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_POSTAL_CODES;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_QR;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_UNUSED_ID;
import static com.zebra.scannercontrol.RMDAttributes.RMD_ATTR_VALUE_SSA_HISTOGRAM_UPC;

public class ActiveDeviceActivity extends BaseActivity implements AdvancedOptionItemFragment.OnAdvancedListFragmentInteractionListener, ActionBar.TabListener, ScannerAppEngine.IScannerAppEngineDevEventsDelegate, ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate,
        ISettingsUtil, NavigationView.OnNavigationItemSelectedListener, RFIDBaseActivity.CreateFileInterface {
    private static final String TAG_RFID_FRAGMENT = "RFID_FRAGMENT";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    private static boolean activityVisible;
    private ViewPager viewPager;
    ActiveDeviceAdapter mAdapter;
    TabLayout tabLayout;
    private ExpandableListView expandableListView;
    private HashMap<String, List<String>> listDataChild;
    private List<String> listDataHeader;
    static int picklistMode;
    ExtendedFloatingActionButton inventoryBT = null;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 10;
    private static final int REQUEST_CODE_ASK_PERMISSIONS_CSV = 11;
    private static RFIDBaseActivity mRFIDBaseActivity;
    private TabLayoutMediator mTabLayoutMediator;
    public static final String BRAND_ID = "brandid";
    public static final String EPC_LEN = "epclen";
    public static final String IS_BRANDID_CHECK = "brandidcheck";
    private ActiveDeviceActivity mActiveDeviceActivity;
    private boolean onSaveInstanceState = false;
    private Dialog dialogFwRebooting;
    private DotsProgressBar dotProgressBar;
    protected ProgressDialog progressDialog;
    public String nfcData;
    public static ReaderDevice mConnectedReaderDetails;


    public boolean isPagerMotorAvailable() {
        return pagerMotorAvailable;
    }

    static boolean pagerMotorAvailable;
    private int scannerID;
    private int scannerType;
    TextView barcodeCount;
    int iBarcodeCount;


    static MyAsyncTask cmdExecTask = null;
    Button btnFindScanner = null;
    static final int ENABLE_FIND_NEW_SCANNER = 1;
    static int[] icon = {R.drawable.nav_available_scanners, R.drawable.nav_pair_new_bt_scanner,
            R.drawable.ic_firmware_update, R.drawable.nav_about};
    static int[] managexx_icon = {R.drawable.ic_reset_factory, R.drawable.ic_btn_reset, R.drawable.ic_logging,
            R.drawable.ic_export_config, R.drawable.ic_report};

    List<Integer> ssaSupportedAttribs;
    DrawerLayout drawer;
    ImageView iv_batteryLevel ,iv_headerImageView;
    TextView battery_percentage;
    Button btn_disconnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(mConnectedReader != null) {
            initializeView();
        } else {
            Intent intent = new Intent(this, DeviceDiscoverActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            finishAffinity();
            startActivity(intent);
            finish();
        }

    }

    private void initializeView() {

        Configuration configuration = getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (configuration.smallestScreenWidthDp < Application.minScreenWidth) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            if (configuration.screenWidthDp < Application.minScreenWidth) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
        setContentView(R.layout.activity_active_scanner);
        ssaSupportedAttribs = new ArrayList<Integer>();
        mActiveDeviceActivity = this;
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_empty_readers));

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mActiveDeviceActivity, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) ;

        iv_batteryLevel = (ImageView) findViewById(R.id.batterylevel);
        battery_percentage = (TextView) findViewById(R.id.battery_percentage);
        btn_disconnect = findViewById(R.id.disconnect_btn);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(mActiveDeviceActivity);
        View headerImageView = navigationView.getHeaderView(0);
        iv_headerImageView = headerImageView.findViewById(R.id.imageView);
        iv_headerImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if( viewPager.getCurrentItem() != mAdapter.getCurrentActivePosition()){
                    viewPager.setCurrentItem(mAdapter.getCurrentActivePosition());
                }
                if (RFIDController.BatteryData != null ){
                    deviceStatusReceived(RFIDController.BatteryData.getLevel(), RFIDController.BatteryData.getCharging(), RFIDController.BatteryData.getCause());
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

        btn_disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mConnectedReader != null && mConnectedReader.isConnected()) {
                    viewPager.setCurrentItem(READERS_TAB);
                    Fragment fragment = getCurrentFragment(READERS_TAB);
                    if(fragment instanceof PairOperationsFragment) {
                        loadNextFragment(READER_LIST_TAB);
                        fragment = getCurrentFragment(READERS_TAB);
                    }
                    if(fragment instanceof ReaderDetailsFragment) {
                        loadNextFragment(READER_LIST_TAB);
                        fragment = getCurrentFragment(READERS_TAB);
                    }
                    if (fragment instanceof RFIDReadersListFragment ) {
                        ((RFIDReadersListFragment) fragment).disconnectConnectedReader();
                    }

                    DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        drawer.closeDrawer(GravityCompat.START);
                    }
                }
            }
        });

        addDevConnectionsDelegate(mActiveDeviceActivity);
        scannerID = getIntent().getIntExtra(Constants.SCANNER_ID, -1);
        BaseActivity.lastConnectedScannerID = scannerID;
        String scannerName = getIntent().getStringExtra(Constants.SCANNER_NAME);
        String address = getIntent().getStringExtra(Constants.SCANNER_ADDRESS);
        scannerType = getIntent().getIntExtra(Constants.SCANNER_TYPE, -1);

        picklistMode = getIntent().getIntExtra(Constants.PICKLIST_MODE, 0);
        pagerMotorAvailable = getIntent().getBooleanExtra(Constants.PAGER_MOTOR_STATUS, false);

        Application.currentScannerId = scannerID;
        Application.currentScannerName = scannerName;
        Application.currentScannerAddress = address;

        mRFIDBaseActivity =  RFIDBaseActivity.getInstance();
        mRFIDBaseActivity.onCreate(mActiveDeviceActivity);
        viewPager = (ViewPager) findViewById(R.id.activeScannerPager);

        if(mConnectedReader != null) {
            RFD_DEVICE_MODE = mRFIDBaseActivity.getDeviceMode(mConnectedReader.getHostName(), Application.currentScannerId);
            if (RFD_DEVICE_MODE == DEVICE_STD_MODE)
                mAdapter = new ActiveDeviceStandardAdapter(this, getSupportFragmentManager(), DEVICE_STD_MODE);
            else
                mAdapter = new ActiveDevicePremiumAdapter(this, getSupportFragmentManager(), DEVICE_PREMIUM_PLUS_MODE);

            mAdapter.setDeviceModelName(mConnectedReader.getHostName());
            viewPager.setAdapter(mAdapter);
            viewPager.addOnAdapterChangeListener(new ViewPager.OnAdapterChangeListener() {
                @Override
                public void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter oldAdapter, @Nullable PagerAdapter newAdapter) {

                    viewPager.getAdapter().notifyDataSetChanged();

                }
            });
        }

        tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener(){

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mAdapter.setCurrentActivePosition(tab.getPosition());
                Log.d(TAG, tab.toString());
                switch (tab.getPosition())
                {
                    case RFID_TAB:
                        if( mAdapter.getRFIDMOde() == RFID_ACCESS_TAB) {
                            // mAdapter.notifyDataSetChanged();
                        }else if( mAdapter.getRFIDMOde() == INVENTORY_TAB) {
                            // mAdapter.notifyDataSetChanged();
                            Fragment fragment = getCurrentFragment(RFID_TAB);

                            if (fragment != null && fragment instanceof RFIDInventoryFragment) {
                                //  ((RFIDInventoryFragment)fragment).onRFIDFragmentSelected();
                            }

                        }else if( mAdapter.getRFIDMOde() == RAPID_READ_TAB) {
                            Fragment fragment = getCurrentFragment(RFID_TAB);
                            if(fragment != null && fragment instanceof RapidReadFragment)
                            {
                                //       ((RapidReadFragment)fragment).onRapidReadSelected();
                            }
                        }
                        break;

                    case SETTINGS_TAB:
                        break;
                    case SCAN_TAB:
                        break;
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Log.d(TAG, tab.toString());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                Log.d(TAG, tab.toString());
            }
        });

        mAdapter.notifyDataSetChanged();
        iBarcodeCount = 0;

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Constants.logAsMessage(TYPE_DEBUG, getClass().getSimpleName(), " Position is --- " + position);
                mAdapter.setCurrentActivePosition(position);
                switch (position) {
                    case RFID_TAB:
                        if(mAdapter.getReaderListMOde() == UPDATE_FIRMWARE_TAB )
                        {
                            getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(mAdapter.getSettingsTab())).commit();
                            getSupportFragmentManager().beginTransaction().addToBackStack(null);
                            getSupportFragmentManager().executePendingTransactions();
                            mAdapter.setReaderListMOde(MAIN_HOME_SETTINGS_TAB);

                        }else{

                            if( getCurrentFragment(mAdapter.getSettingsTab()) != null ){
                                getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(mAdapter.getSettingsTab())).commit();
                                getSupportFragmentManager().beginTransaction().addToBackStack(null);
                                getSupportFragmentManager().executePendingTransactions();
                            }

                        }


                        if( (mConnectedReader != null) && mConnectedReader.getHostName().startsWith("RFD8500")  )
                            setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE);

                        if(mAdapter.getRFIDMOde() == RAPID_READ_TAB)
                            loadNextFragment(RAPID_READ_TAB);
                        else if(mAdapter.getRFIDMOde() == INVENTORY_TAB)
                            loadNextFragment(INVENTORY_TAB);
                        else if(mAdapter.getRFIDMOde() == RFID_ACCESS_TAB)
                            loadNextFragment(RFID_ACCESS_TAB);
                        else  if(mAdapter.getRFIDMOde() == RFID_PREFILTERS_TAB)
                            loadNextFragment(RFID_PREFILTERS_TAB);
                        else if(mAdapter.getRFIDMOde() == LOCATE_TAG_TAB)
                            loadNextFragment(LOCATE_TAG_TAB);
                        else if(mAdapter.getRFIDMOde() == RFID_SETTINGS_TAB)
                            loadNextFragment(RFID_SETTINGS_TAB);
                        else if(mAdapter.getRFIDMOde() == NONOPER_TAB)
                            loadNextFragment(RAPID_READ_TAB);

                        break;
                    case READERS_TAB:
                        if(mAdapter.getReaderListMOde() == UPDATE_FIRMWARE_TAB )
                        {
                            getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(mAdapter.getSettingsTab())).commit();
                            getSupportFragmentManager().beginTransaction().addToBackStack(null);
                            getSupportFragmentManager().executePendingTransactions();
                            mAdapter.setReaderListMOde(MAIN_HOME_SETTINGS_TAB);
                        }
                        loadNextFragment(READER_LIST_TAB);
                        break;
                    case SCAN_TAB:
                        if(RFD_DEVICE_MODE == DEVICE_PREMIUM_PLUS_MODE) {
                            if( getCurrentFragment(mAdapter.getSettingsTab()) != null ){
                                getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(mAdapter.getSettingsTab())).commit();
                                getSupportFragmentManager().beginTransaction().addToBackStack(null);
                                getSupportFragmentManager().executePendingTransactions();
                            }

                            loadNextFragment(SCAN_DATAVIEW_TAB);
                            barcodeCount = (TextView) findViewById(R.id.barcodesListCount);
                            barcodeCount.setText("Barcodes Scanned: " + Integer.toString(iBarcodeCount));
                            if(mAdapter.getReaderListMOde() == UPDATE_FIRMWARE_TAB )
                            {
                                getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(mAdapter.getSettingsTab())).commit();
                                getSupportFragmentManager().beginTransaction().addToBackStack(null);
                                getSupportFragmentManager().executePendingTransactions();
                                mAdapter.setReaderListMOde(MAIN_HOME_SETTINGS_TAB);
                            }
                            if( (mConnectedReader != null) && mConnectedReader.getHostName().startsWith("RFD8500")  )
                                setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE);
                            break;

                        } else if (RFD_DEVICE_MODE == DEVICE_STD_MODE){
                            getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(mAdapter.getSettingsTab())).commit();
                            getSupportFragmentManager().beginTransaction().addToBackStack(null);
                            getSupportFragmentManager().executePendingTransactions();

                        }
                    case SETTINGS_TAB:
                        loadNextFragment(MAIN_HOME_SETTINGS_TAB);
                        //   if(mAdapter.getReaderListMOde() == MAIN_GENERAL_SETTINGS_TAB )
                        //       loadNextFragment(MAIN_GENERAL_SETTINGS_TAB);
                        //   else  if(mAdapter.getReaderListMOde() == MAIN_HOME_SETTINGS_TAB)
                        //       loadNextFragment(MAIN_HOME_SETTINGS_TAB);
                        //  else  if(mAdapter.getReaderListMOde() == MAIN_RFID_SETTINGS_TAB)
                        //      loadNextFragment(MAIN_RFID_SETTINGS_TAB);
                        break;

                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        if (getIntent().getBooleanExtra(Constants.SHOW_BARCODE_VIEW, false))
            viewPager.setCurrentItem(BARCODE_TAB);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) mActiveDeviceActivity.getSystemService(ns);
        if (nMgr != null) {
            nMgr.cancel(NotificationsReceiver.DEFAULT_NOTIFICATION_ID);
        }

        setActionBarTitle("Rapid");
        viewPager.setCurrentItem(RFID_TAB);
        mAdapter.setCurrentActivePosition(RFID_TAB);

        reInit();

        try {
            if (RFIDController.regionNotSet == false) {
                RFIDController.getInstance().updateReaderConnection(false);
            }
        } catch (InvalidUsageException e) {
            Log.d(TAG,  "Returned SDK Exception");
        } catch (OperationFailureException e) {
            Log.d(TAG,  "Returned SDK Exception");
        }
        initScanner();
        setParentActivity(this);
        createDWProfile();
    }


    public void initBatchRequest(View v) {
        String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_BATCH_REQUEST, inXML, null, scannerID);
    }

    public void deviceStatusReceived(final int level, final boolean charging, final String cause) {
        battery_percentage.setText(String.valueOf(level)+"%");
        iv_batteryLevel.setImageLevel(level);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // do your stuff here after SecondActivity finished.
        Log.d(TAG, "Set regulatory done..");


    }

    public void setRFIDTab() {
        viewPager.setCurrentItem(RFID_TAB);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();


        if (waitingForFWReboot) {
            showFwRebootdialog();

        }

        if((onSaveInstanceState == true) && (mReaderDisappeared != null) && (RFIDController.regionNotSet == false )) {
            //loadNextFragment(MAIN_HOME_SETTINGS_TAB);
            setCurrentTabFocus(READERS_TAB);
        }
        onSaveInstanceState = false;

        mRFIDBaseActivity.activityResumed();
       if(RFIDController.regionNotSet == true ) {

        Intent detailsIntent = new Intent(getApplicationContext(), SettingsDetailActivity.class);
        detailsIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
        detailsIntent.putExtra(com.zebra.demo.rfidreader.common.Constants.SETTING_ITEM_ID, R.id.regulatory);
        detailsIntent.putExtra(com.zebra.demo.rfidreader.common.Constants.SETTING_ON_FACTORY, true);
        startActivityForResult(detailsIntent, 0);

       }else if(Application.updateReaderConnection == true) {

           try {
               RFIDController.getInstance().updateReaderConnection(false);
               Application.updateReaderConnection = false;
           } catch (InvalidUsageException e) {
              Log.d(TAG,  "Returned SDK Exception");
           } catch (OperationFailureException e) {
               if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
           }
       }else{

       }
    }




    public void showFwRebootdialog()
    {
        String fwStatus;
        dialogFwRebooting = new Dialog(mActiveDeviceActivity);
        dialogFwRebooting.setContentView(R.layout.dialog_fw_rebooting);

        TextView Status = (TextView) dialogFwRebooting.findViewById(R.id.fwstatus);
        if(Application.isFirmwareUpdateSuccess == true) {
            fwStatus = "Firmware update Success. ";  //Toast.makeText(this, "Firmware update Success", Toast.LENGTH_SHORT).show();
        } else {
            fwStatus = "Firmware update failed. ";  //Toast.makeText(this, "Firmware update Failed", Toast.LENGTH_SHORT).show();
            Status.setTextColor(Color.RED);
        }

        //dialogFwRebooting.requestWindowFeature(Window.FEATURE_NO_TITLE);
        TextView counter = (TextView) dialogFwRebooting.findViewById(R.id.counter);
        Status.setText(fwStatus + Status.getText());



        dotProgressBar = (DotsProgressBar) dialogFwRebooting.findViewById(R.id.progressBar);
        dotProgressBar.setDotsCount(6);


        dialogFwRebooting.setCancelable(false);
        dialogFwRebooting.setCanceledOnTouchOutside(false);
        dialogFwRebooting.show();
    }

    public boolean isWaitingForFWReboot() {


        if (waitingForFWReboot) {
            setWaitingForFWReboot(false);
            if(dialogFwRebooting != null) {
                dialogFwRebooting.dismiss();
                dialogFwRebooting = null;
            }
            return true;
        }
        return false;
    }


    public void reInit() {

        removeDevEventsDelegate(this);
        addDevEventsDelegate(this);
        removeDevConnectiosDelegate(this);
        addDevConnectionsDelegate(this);
        //addMissedBarcodes();

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) this.getSystemService(ns);
        if (nMgr != null) {
            nMgr.cancel(NotificationsReceiver.DEFAULT_NOTIFICATION_ID);
        }

        scannerID = Application.currentConnectedScannerID;
        mRFIDBaseActivity.reInit(this);
        //SetTunnelMode(null);
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
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            nfcData = ((com.zebra.demo.application.Application)getApplication()).processNFCData(intent);
        }else if( NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
            processTAGData(intent);

    }

    private void processTAGData(Intent intent) {
        Log.d(TAG,"ProcessTAG data " );

        Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_TAG);
        if (rawMessages != null && rawMessages.length > 0) {

            NdefMessage[] messages = new NdefMessage[rawMessages.length];

            for (int i = 0; i < rawMessages.length; i++) {

                messages[i] = (NdefMessage) rawMessages[i];

            }

            Log.i(TAG, "message size = " + messages.length);

            NdefMessage msg = (NdefMessage) rawMessages[0];
            String base = new String(msg.getRecords()[0].getPayload());
            String str = String.format(Locale.getDefault(), "Message entries=%d. Base message is %s", rawMessages.length, base);
            Log.i(TAG, "message  = " + str);


        }
    }


    public String copyNfcContent() {
        return nfcData;

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeDevEventsDelegate(this);
        removeDevConnectiosDelegate(this);
        if (mRFIDBaseActivity != null) {
            mRFIDBaseActivity.onDestroy();
        }
    }

    private void minimizeApp() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        int position = getCurrentTabPosition();
        Fragment fragment = getCurrentFragment(position);
        switch(position) {


            case READERS_TAB:
                if (fragment instanceof PairOperationsFragment || fragment instanceof ReaderDetailsFragment || fragment instanceof ReaderWifiSettingsFragment) {
                    loadNextFragment(READER_LIST_TAB);
                } else {
                    minimizeApp();
                }
                break;
            case SCAN_TAB:
                if (RFD_DEVICE_MODE == DEVICE_PREMIUM_PLUS_MODE) {
                    minimizeApp();
                }
            case SETTINGS_TAB:
                if (fragment != null && fragment instanceof BackPressedFragment) {
                    ((BackPressedFragment) fragment).onBackPressed();
                }
                if (fragment instanceof ManagerFragment) {
                    loadNextFragment(MAIN_HOME_SETTINGS_TAB);

                } else if (fragment instanceof SettingListFragment) {
                    loadNextFragment(MAIN_HOME_SETTINGS_TAB);

                } else if(fragment instanceof  BeeperActionsFragment) {
                    loadNextFragment(SCAN_SETTINGS_TAB);
                }else if(fragment instanceof SymbologiesFragment) {
                        loadNextFragment(SCAN_SETTINGS_TAB);
                }else if (fragment instanceof ScanHomeSettingsFragment) {
                    loadNextFragment(MAIN_HOME_SETTINGS_TAB);
                } else if (fragment instanceof SettingsFragment) {
                    loadNextFragment(MAIN_HOME_SETTINGS_TAB);
                } else if (fragment instanceof AdvancedFragment) {
                    loadNextFragment(MAIN_HOME_SETTINGS_TAB);
                } else if ((fragment instanceof AdvancedOptionItemFragment)
                        || (fragment instanceof ProfileFragment)
                        || (fragment instanceof LedFragment)) {
                    loadNextFragment(MAIN_RFID_SETTINGS_TAB);
                    //RFID_ADVANCED_OPTIONS_TAB
                } else if (fragment instanceof BeeperFragment) {

                } else if (fragment instanceof RegulatorySettingsFragment) {

                }else if (fragment instanceof WifiFragment) {

                }else if (fragment instanceof ChargeTerminalFragment) {

                } else if ((fragment instanceof AntennaSettingsFragment)) {
                    loadNextFragment(RFID_ADVANCED_OPTIONS_TAB);

                } else if (fragment instanceof StartStopTriggersFragment) {

                } else if (fragment instanceof SingulationControlFragment) {
                    //loadNextFragment(RFID_ADVANCED_OPTIONS_TAB);

                } else if (fragment instanceof DPOSettingsFragment) {

                } else if (fragment instanceof SaveConfigurationsFragment) {

                } else if (fragment instanceof TagReportingFragment) {

                } else if (fragment instanceof FactoryResetFragment) {
                    loadNextFragment(MAIN_GENERAL_SETTINGS_TAB);
                }else if (fragment instanceof LoggerFragment){
                        loadNextFragment(MAIN_GENERAL_SETTINGS_TAB);
                  } else if (fragment instanceof ApplicationSettingsFragment){
                    loadNextFragment(MAIN_HOME_SETTINGS_TAB);
                }else if (fragment instanceof KeyRemapFragment) {
                    loadNextFragment(MAIN_GENERAL_SETTINGS_TAB);
                }else if (fragment instanceof UpdateFirmware) {
                    loadNextFragment(MAIN_GENERAL_SETTINGS_TAB);
                }else if (fragment instanceof AssertFragment) {
                    loadNextFragment(MAIN_GENERAL_SETTINGS_TAB);
                }
                else if (fragment instanceof Static_ipconfig) {
                    loadNextFragment(MAIN_GENERAL_SETTINGS_TAB);
                }
                else if(fragment instanceof BatteryStatsFragment) {
                    loadNextFragment(MAIN_GENERAL_SETTINGS_TAB);
                }else if(fragment instanceof BatteryFragment) {
                    loadNextFragment(MAIN_GENERAL_SETTINGS_TAB);
                }else if(fragment instanceof UsbMiFiFragment) {

                }

                else{
                    minimizeApp();
                }

                break;
            case RFID_TAB:
                if( fragment instanceof PreFilterFragment) {
                    ((PreFilterFragment) fragment).onBackPressed();
                    loadNextFragment(INVENTORY_TAB);

                }else if( fragment instanceof LocateOperationsFragment || fragment instanceof AccessOperationsFragment) {
                    loadNextFragment(RAPID_READ_TAB);

                }else {
                    minimizeApp();
                }
                break;
        }
       // Fragment fragment = getCurrentFragment(SETTINGS_TAB);

        return;


    }


    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        Constants.logAsMessage(TYPE_DEBUG, getClass().getSimpleName(), "onTabSelected() Position is --- " + tab.getPosition());
        // on tab selected
        // show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }


    public void startFirmware(View view) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_START_NEW_FIRMWARE, null);
        cmdExecTask.execute(new String[]{in_xml});
    }

    public void abortFirmware(View view) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_ABORT_UPDATE_FIRMWARE, null);
        cmdExecTask.execute(new String[]{in_xml});
    }

    public void loadLedActions(View view) {
        Intent intent = new Intent(this, LEDActivity.class);
        intent.putExtra(Constants.SCANNER_ID, scannerID);
        intent.putExtra(Constants.SCANNER_NAME, getIntent().getStringExtra(Constants.SCANNER_NAME));
        startActivity(intent);
    }

    public void loadBeeperActions(View view) {
        loadNextFragment(BEEPER_ACTION_TAB);
    }
    public void beeperAction(View view) {
        int position = getCurrentTabPosition();
        Fragment fragment = getCurrentFragment(position);
        if(fragment instanceof BeeperActionsFragment)
            ((BeeperActionsFragment)fragment).beeperAction(view);
    }
    public void factoryResetClicked(View view)
    {
        loadNextFragment(FACTORY_RESET_FRAGMENT_TAB);
    }

    public void enableLoggingClicked(View view)
    {
        if((RFIDController.mConnectedReader != null ) && RFIDController.mConnectedReader.getHostName().startsWith("MC33")) {
             Toast.makeText(this, "Real time log not supported for MC33", Toast.LENGTH_SHORT).show();
        }
            loadNextFragment(LOGGER_FRAGMENT_TAB);
        return;
    }

    public void generalSettingsClicked(View view) {
        loadNextFragment(MAIN_GENERAL_SETTINGS_TAB);
    }

    public void showRFIDSettings( View view ){
        loadNextFragment(RFID_SETTINGS_TAB);
    }

    public void scanSettingsClicked( View view ){

        loadNextFragment(SCAN_SETTINGS_TAB);
    }

    public void applicationSettingsClicked(View view)
    {
        loadNextFragment(APPLICATION_SETTINGS_TAB);
        return;
    }


    public void deviceResetClicked(View view)
    {
        loadNextFragment(DEVICE_RESET_TAB);
    }

    public void showDeviceInfoClicked(View view)
    {
        loadNextFragment(ASSERT_DEVICE_INFO_TAB);

    }
    public void staticIpConfig(View view){
        loadNextFragment(STATIC_IP_CONFIG);
    }

    public void keyRemapClicked(View view) {
        if( RFIDController.mConnectedReader != null ) {
            if (RFIDController.mConnectedReader.getHostName().startsWith("RFD40")
            || RFIDController.mConnectedReader.getHostName().startsWith("RFD90")) {
                loadNextFragment(KEYREMAP_TAB);
            } else {
                view.setEnabled(false);
                Toast.makeText(this, "Trigger Mapping feature is not supported for " + mConnectedReader.getHostName(), Toast.LENGTH_LONG).show();
            }
        }else{
            Toast.makeText(this, "Trigger Mapping feature is not supported when device not connected " , Toast.LENGTH_LONG).show();
        }
    }




    public void loadAssert(View view) {
        loadNextFragment(ASSERT_DEVICE_INFO_TAB);

    }

    public void symbologiesClicked(View view) {
        loadNextFragment(BARCODE_SYMBOLOGIES_TAB);
    }

    public void enableScanning(View view) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_ENABLE, null);
        cmdExecTask.execute(new String[]{in_xml});
    }

    public void disableScanning(View view) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_SCAN_DISABLE, null);
        cmdExecTask.execute(new String[]{in_xml});
    }


    public void aimOn(View view) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_AIM_ON, null);
        cmdExecTask.execute(new String[]{in_xml});
    }

    public void aimOff(View view) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_AIM_OFF, null);
        cmdExecTask.execute(new String[]{in_xml});
    }

    public void vibrationFeedback(View view) {

        Intent intent = new Intent(this, VibrationFeedback.class);
        intent.putExtra(Constants.SCANNER_ID, scannerID);
        intent.putExtra(Constants.SCANNER_NAME, getIntent().getStringExtra(Constants.SCANNER_NAME));
        startActivity(intent);

    }

    public void setTriggerMode(ENUM_TRIGGER_MODE triggerMode) {

        mRFIDBaseActivity.setTriggerMode(triggerMode);

    }

    public void pullTrigger(View view) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_PULL_TRIGGER, null);
        cmdExecTask.execute(new String[]{in_xml});
    }

    public void releaseTrigger(View view) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_RELEASE_TRIGGER, null);
        cmdExecTask.execute(new String[]{in_xml});
       // view.setEnabled(true);
       // view.setOnClickListener(new View.OnClickListener() {
       //     @Override
        //    public void onClick(View v) {
        //        scanTrigger(v);
        //    }
       // });

    }

    public void SetTunnelMode(View view) {

        String inXML = "<inArgs><scannerID>" + Application.currentConnectedScannerID + "</scannerID><cmdArgs><arg-int>" +
                18 + "</arg-int></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML, outXML, Application.currentConnectedScannerID);
    }

    public int getPickListMode() {
        //String in_xml = "<inArgs><scannerID>" + Application.currentConnectedScannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>402</attrib_list></arg-xml></cmdArgs></inArgs>";
        int attrVal = 0;
        String in_xml = "<inArgs><scannerID>" + Application.currentConnectedScannerID + "</scannerID><cmdArgs><arg-xml><attrib_list>402</attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GET,in_xml,outXML,Application.currentConnectedScannerID);

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


    public int getScannerID() {
        return scannerID;
    }

    private void addMissedBarcodes() {
        if (barcodeQueue.size() != iBarcodeCount) {

            for (int i = iBarcodeCount; i < barcodeQueue.size(); i++) {
                scannerBarcodeEvent(barcodeQueue.get(i).getBarcodeData(), barcodeQueue.get(i).getBarcodeType(), barcodeQueue.get(i).getFromScannerID());
            }
        }
    }

    @Override
    public synchronized void scannerBarcodeEvent(byte[] barcodeData, int barcodeType, int scannerID) {


        if(viewPager.getCurrentItem() != BARCODE_TAB ) {
            Log.d(TAG, "Cached barcode Data");
            return;
        }

        Log.d(TAG, "Rendering barcode Data");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           // Fragment fragment = mAdapter.getRegisteredFragment(BARCODE_TAB);
                            // if((fragment instanceof BarcodeFargment) == false)
                            //     return;

                            BarcodeFargment barcodeFargment = (BarcodeFargment) mAdapter.getRegisteredFragment(BARCODE_TAB);
                            if (barcodeFargment != null) {

                                barcodeFargment.showBarCode();

                                barcodeCount = (TextView) findViewById(R.id.barcodesListCount);
                                barcodeCount.setText("Barcodes Scanned: " + Integer.toString(++iBarcodeCount));
                                if (iBarcodeCount > 0) {
                                    Button btnClear = (Button) findViewById(R.id.btnClearList);
                                    btnClear.setEnabled(true);
                                }
                                if (!Application.isFirmwareUpdateInProgress) {
                                    //viewPager.setCurrentItem(BARCODE_TAB);
                                }

                            }
                        }
                    });

   }

    @Override
    public void scannerFirmwareUpdateEvent(FirmwareUpdateEvent firmwareUpdateEvent) {
        int setTab = RFD_DEVICE_MODE == DEVICE_STD_MODE ? SCAN_TAB :SETTINGS_TAB;
        Fragment fragment = getCurrentFragment(setTab);
        if(fragment instanceof UpdateFirmware )
            ((UpdateFirmware) fragment).scannerFirmwareUpdateEvent( firmwareUpdateEvent);
    }

    @Override
    public void scannerImageEvent(byte[] imageData) {

    }

    @Override
    public void scannerVideoEvent(byte[] videoData) {
    }

    public void clearList(View view) {
        BarcodeFargment barcodeFargment = (BarcodeFargment) mAdapter.getRegisteredFragment(BARCODE_TAB);
        if (barcodeFargment != null) {
            barcodeFargment.clearList();
            barcodeCount = (TextView) findViewById(R.id.barcodesListCount);
            iBarcodeCount = 0;
            barcodeCount.setText("Barcodes Scanned: " + Integer.toString(iBarcodeCount));
            Button btnClear = (Button) findViewById(R.id.btnClearList);
            btnClear.setEnabled(false);
        }
    }

    public void scanTrigger(View view) {

        String in_xml = "<inArgs><scannerID>" + Application.currentConnectedScannerID + "</scannerID></inArgs>";
        cmdExecTask = new MyAsyncTask(Application.currentConnectedScannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_PULL_TRIGGER, null);
        cmdExecTask.execute(new String[]{in_xml});
    }

    /**
     * Navigate to Scale view
     *
     * @param view
     */
    public void loadScale(View view) {

        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        new AsyncTaskScaleAvailable(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_READ_WEIGHT, this, ScaleActivity.class).execute(new String[]{in_xml});

    }

    public void locationingButtonClicked(final View v) {
        mRFIDBaseActivity.locationingButtonClicked(v);
    }

    public void accessOperationsReadClicked(View v) {
        mRFIDBaseActivity.accessOperationsReadClicked(v);
    }

    public void accessOperationLockClicked(View v) {
        mRFIDBaseActivity.accessOperationLockClicked(v);
    }

    public void accessOperationsWriteClicked(View v) {
        mRFIDBaseActivity.accessOperationsWriteClicked(v);
    }

    public void accessOperationsKillClicked(View v) {
        mRFIDBaseActivity.accessOperationsKillClicked(v);
    }

    public synchronized void locationingButtonClicked(FloatingActionButton btn_locate) {
        mRFIDBaseActivity.locationingButtonClicked(btn_locate);
    }

    public synchronized void multiTagLocateStartOrStop(View view) {
       mRFIDBaseActivity.multiTagLocateStartOrStop(view);
    }

    public synchronized void multiTagLocateAddTagItem(View view) {
        mRFIDBaseActivity.multiTagLocateAddTagItem(view);
    }

    public synchronized void multiTagLocateDeleteTagItem(View view) {
        mRFIDBaseActivity.multiTagLocateDeleteTagItem(view);
    }

    public synchronized void multiTagLocateReset(View view) {
        mRFIDBaseActivity.multiTagLocateReset(view);
    }
    public synchronized void multiTagLocateClearTagItems(View view) {
        mRFIDBaseActivity.multiTagLocateClearTagItems(view);
    }
    public void showBatteryStatsClicked(View view){
        loadNextFragment(BATTERY_STATISTICS_TAB);
    }

    public void showBatteryStats(){
        viewPager.setCurrentItem(mAdapter.getSettingsTab());
        loadNextFragment(BATTERY_STATISTICS_TAB);
    }


    public void callBackPressed() {
        mRFIDBaseActivity.callBackPressed();
    }

    public void selectItem(int i) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawer.closeDrawer(GravityCompat.START);
        int id = item.getItemId();
        if(item.getItemId() == android.R.id.home) {
            finish();

        }
        switch(id)
        {
           case android.R.id.home:
                    onBackPressed();
                    return true;
            case R.id.nav_fw_update:
                if(mConnectedReader != null && mConnectedReader.isConnected()) {
                    loadUpdateFirmware(item.getActionView());
                } else {
                    Toast.makeText(this, "No device in connected state", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.nav_battery_statics:

//                Intent detailsIntent = new Intent(this, SettingsDetailActivity.class);
//                detailsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                detailsIntent.putExtra(com.zebra.demo.rfidreader.common.Constants.SETTING_ITEM_ID, R.id.battery);
//                startActivity(detailsIntent);
                showBatteryStats();
                 break;
            case R.id.nav_about:
                showDeviceInfoClicked(item.getActionView());
                break;
            case R.id.nav_settings:
                viewPager.setCurrentItem(mAdapter.getSettingsTab());
                loadNextFragment(MAIN_HOME_SETTINGS_TAB);


                break;
            case R.id.menu_readers:
                Fragment fragment = getCurrentFragment(READERS_TAB);

                if(fragment instanceof PairOperationsFragment)
                    loadNextFragment(READER_LIST_TAB);

                viewPager.setCurrentItem(READERS_TAB);
                break;
            case R.id.nav_connection_help:
                Intent helpIntent = new Intent(this, NavigationHelpActivity.class);
                startActivity(helpIntent);
                break;

        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        drawer.setSelected(true);
        return true;
    }

    public void setActionBarTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public int getCurrentTabPosition() {
        return mAdapter.getCurrentActivePosition();
    }


    @Override
    protected void onStop() {
        super.onStop();
        onSaveInstanceState = true;
    }



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
            // Always call the superclass so it can save the view hierarchy state

        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("default_tab", "readers");
        onSaveInstanceState = true;
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);



        if (waitingForFWReboot  == false) {
            //viewPager.setCurrentItem(READERS_TAB);
            setCurrentTabFocus(READERS_TAB);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            }
        }
        onSaveInstanceState = false;

    }



    public void setCurrentTabFocus(int pos) {
        if(onSaveInstanceState == false) {
            //loadNextFragment(MAIN_HOME_SETTINGS_TAB);
            if(mAdapter.getCurrentActivePosition() != pos )
                    viewPager.setCurrentItem(pos);
        }

    }

    public void setCurrentTabFocus(int pos, int fragment) {
        if(!onSaveInstanceState) {
            if(mAdapter.getCurrentActivePosition() != pos ) {
                viewPager.setCurrentItem(pos);
                loadNextFragment(fragment);
            }
        }

    }

    public void sendNotification(String actionReaderStatusObtained, String info) {

        mRFIDBaseActivity.sendNotification(actionReaderStatusObtained, info );
    }

    public void loadWifiReaderSettings() {
        if(mConnectedReader != null && mConnectedReader.isConnected())
            loadNextFragment(READER_WIFI_SETTINGS_TAB);

    }

    public void loadScanSettings(View view) {
        loadNextFragment(SCAN_SETTINGS_TAB);
    }

    public void loadScanAdvancedSettings(View view) {
        loadNextFragment(SCAN_ADVANCED_TAB);
    }

    @Override
    public void OnAdvancedListFragmentInteractionListener(AdvancedOptionsContent.SettingItem item) {
        Fragment fragment = null;
        int settingItemSelected = Integer.parseInt(item.id);
        //Show the selected item
        switch (settingItemSelected) {
            case R.id.antenna:

                loadNextFragment(ANTENNA_SETTINGS_TAB);
                break;
            case R.id.singulation_control:

                loadNextFragment(SINGULATION_CONTROL_TAB);
                break;
            case R.id.start_stop_triggers:

                loadNextFragment(START_STOP_TRIGGER_TAB);
                break;
            case R.id.tag_reporting:

                loadNextFragment(TAG_REPORTING_TAB);
                break;
            case R.id.save_configuration:

                loadNextFragment((SAVE_CONFIG_TAB));

                break;
            case R.id.power_management:
                fragment = DPOSettingsFragment.newInstance();
                loadNextFragment(DPO_SETTING_TAB);
                break;
        }
        setTitle(item.content);
    }


    public void timerDelayRemoveDialog(long time, final Dialog d, final String command, final boolean isPressBack) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (d != null && d.isShowing()) {
                    sendNotification(com.zebra.demo.rfidreader.common.Constants.ACTION_READER_STATUS_OBTAINED, command + " timeout");
                    d.dismiss();
                    if (ActiveDeviceActivity.isActivityVisible() && isPressBack)
                        callBackPressed();
                }
            }
        }, time);
    }

    /**
     * Method called when save config button is clicked
     *
     * @param v - View to be addressed
     */
    public void saveConfigClicked(View v) {
        if (mConnectedReader != null && mConnectedReader.isConnected()) {
            progressDialog = new com.zebra.demo.rfidreader.common.CustomProgressDialog(this, getString(R.string.save_config_progress_title));
            progressDialog.show();
            timerDelayRemoveDialog(com.zebra.demo.rfidreader.common.Constants.SAVE_CONFIG_RESPONSE_TIMEOUT, progressDialog, getString(R.string.status_failure_message), false);
            new AsyncTask<Void, Void, Boolean>() {
                private OperationFailureException operationFailureException;

                @Override
                protected Boolean doInBackground(Void... voids) {
                    boolean bResult = false;
                    try {
                        mConnectedReader.Config.saveConfig();
                        bResult = true;
                    } catch (InvalidUsageException e) {
                        if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                    } catch (OperationFailureException e) {
                        if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                        operationFailureException = e;
                    }
                    return bResult;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    super.onPostExecute(result);
                    progressDialog.dismiss();
                    if (!result) {
                        Toast.makeText(getApplicationContext(), operationFailureException.getVendorMessage(), Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.status_success_message), Toast.LENGTH_SHORT).show();
                }
            }.execute();
        } else
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_disconnected), Toast.LENGTH_SHORT).show();

    }

    public void changeAdapter(int tabCount) {
        if(tabCount == DEVICE_PREMIUM_PLUS_MODE)
            mAdapter = new ActiveDevicePremiumAdapter(this,getSupportFragmentManager(), DEVICE_PREMIUM_PLUS_MODE);
        else
            mAdapter = new ActiveDeviceStandardAdapter(this,getSupportFragmentManager(), DEVICE_STD_MODE);

        RFD_DEVICE_MODE = tabCount;
        viewPager.setAdapter(mAdapter);
        mAdapter.setDeviceModelName(mConnectedReader.getHostName());
        //viewPager.getAdapter().notifyDataSetChanged();

    }

    public void onFactoryReset(ReaderDevice readerDevice) {
        mRFIDBaseActivity.onFactoryReset(readerDevice);
    }

    @Override
    public void createFile1(Uri uri) {

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/csv");
        intent.putExtra(Intent.EXTRA_TITLE, getFilename());
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);

        exportresultLauncher.launch(intent);
    }

    private String getFilename() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss.SSS");
       /* if(ActiveProfile.id.equals("1")) {
            if(Application.cycleCountProfileData != null) {
                File root = Environment.getExternalStorageDirectory();
                File dir = new File(root.getAbsolutePath() + "/inventory");
                File file = new File(dir, Application.cycleCountProfileData);
                if(file.exists()) {
                    file.delete();
                }
            }
            Application.cycleCountProfileData = mConnectedReader + "_" + sdf.format(new Date()) + ".csv";
            return Application.cycleCountProfileData;
        }*/
        return "RFID" + "_" + sdf.format(new Date()) + ".csv";
    }

    public ActiveDeviceAdapter getDeviceAdapter() {
        return mAdapter;
    }


    /**
     * scale availability check
     */
    private class AsyncTaskScaleAvailable extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        Context context;
        Class targetClass;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;

        public AsyncTaskScaleAvailable(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, Context context, Class targetClass) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.context = context;
            this.targetClass = targetClass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(ActiveDeviceActivity.this, "Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            boolean result = executeCommand(opcode, strings[0], sb, scannerId);
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_READ_WEIGHT) {
                if (result) {
                    return true;
                }
            }
            return false;
        }


        @Override
        protected void onPostExecute(Boolean scaleAvailability) {
            super.onPostExecute(scaleAvailability);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

            Intent intent = new Intent(context, targetClass);
            intent.putExtra(Constants.SCANNER_ID, scannerID);
            intent.putExtra(Constants.SCANNER_NAME, getIntent().getStringExtra(Constants.SCANNER_NAME));
            intent.putExtra(Constants.SCALE_STATUS, scaleAvailability);
            startActivity(intent);
        }


    }

    public void updateBarcodeCount() {
        if (barcodeQueue.size() != iBarcodeCount) {
            barcodeCount = (TextView) findViewById(R.id.barcodesListCount);
            iBarcodeCount = barcodeQueue.size();
            barcodeCount.setText("Barcodes Scanned: " + Integer.toString(iBarcodeCount));
            if (iBarcodeCount > 0) {
                Button btnClear = (Button) findViewById(R.id.btnClearList);
                btnClear.setEnabled(true);
            }
        }

    }

    @Override
    public boolean scannerHasAppeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasDisappeared(int scannerID) {
        if (null != cmdExecTask) {
            cmdExecTask.cancel(true);
        }
        barcodeQueue.clear();
        return true;
    }

    @Override
    public boolean scannerHasConnected(int scannerID) {
        barcodeQueue.clear();

        return false;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        barcodeQueue.clear();
        return true;
    }


    public void setPickListMode(int picklistInt) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-xml><attrib_list><attribute><id>" + 402 + "</id><datatype>B</datatype><value>" + picklistInt + "</value></attribute></attrib_list></arg-xml></cmdArgs></inArgs>";
        StringBuilder outXML = new StringBuilder();
        cmdExecTask = new MyAsyncTask(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_SET, outXML);
        cmdExecTask.execute(new String[]{in_xml});
    }

    public void updateFirmware(View view) {

        int setTab = RFD_DEVICE_MODE == DEVICE_STD_MODE ? SCAN_TAB :SETTINGS_TAB;
        Fragment fragment = getCurrentFragment(setTab);
        if(fragment instanceof UpdateFirmware )
            ((UpdateFirmware) fragment).updateFirmware(view);

    }

    /**
     * select firmware file
     * @param view user interface
     */
    public void selectFirmware(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
        intent.putExtra("DocumentsContract.EXTRA_INITIAL_URI", uri);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        activityResultLauncher.launch(intent);
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri documentUri;
                        if (data != null) {
                            if (data.getData().toString().contains("content://com.android.providers")) {
                                runOnUiThread(this::ShowPlugInPathChangeDialog);
                            } else {
                                int setTab = RFD_DEVICE_MODE == DEVICE_STD_MODE ? SCAN_TAB :SETTINGS_TAB;
                                Fragment fragment = getCurrentFragment(setTab);
                                if(fragment instanceof UpdateFirmware ) {
                                    documentUri =data.getData();
                                    //((UpdateFirmware) fragment).selectedFile(data.getData());
                                    ((UpdateFirmware) fragment).selectedFile(documentUri);
                                }
                            }
                        }
                    }
                }

                private void ShowPlugInPathChangeDialog() {
                    if (!isFinishing()) {
                        final Dialog dialog = new Dialog(ActiveDeviceActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.dialog_plugin_path_change);
                        dialog.setCancelable(false);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.show();
                        TextView declineButton = (TextView) dialog.findViewById(R.id.btn_ok);
                        declineButton.setOnClickListener(v -> dialog.dismiss());
                    }
                }
            });



    ActivityResultLauncher<Intent> exportresultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        Uri uri = data.getData();
                        if (data != null) {
                            mRFIDBaseActivity.exportData(uri);
                        }
                    }
                }

            });



    public void loadUpdateFirmware(View view) {

            int tab = RFD_DEVICE_MODE == DEVICE_STD_MODE ? SCAN_TAB:SETTINGS_TAB;
            setCurrentTabFocus(tab);
            loadNextFragment(UPDATE_FIRMWARE_TAB);

    }
    public void loadReaderDetails(ReaderDevice readerDevice) {

        connectedReaderDetails(readerDevice);
        loadNextFragment(READER_DETAILS_TAB);
    }
    private void connectedReaderDetails(ReaderDevice readerDevice) {

        mConnectedReaderDetails = readerDevice;
    }

    public ReaderDevice connectedReaderDetails() {
        return mConnectedReaderDetails;
    }

    public void ImageVideo(View view) {
        if (scannerType != 2) {
            String message = "Video feature not supported in bluetooth scanners.";
            alertShow(message, false);
        } else {
            loadImageVideo();
        }
    }

    private void alertShow(String message, boolean error) {

        if (error) {
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(ActiveDeviceActivity.this);
            dialog.setTitle("Video not supported")
                    .setMessage(message)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialoginterface, int i) {
                            loadImageVideo();
                        }
                    }).show();
        }
    }

    private void loadImageVideo() {
        Intent intent = new Intent(this, ImageActivity.class);
        intent.putExtra(Constants.SCANNER_ID, scannerID);
        intent.putExtra(Constants.SCANNER_NAME, getIntent().getStringExtra(Constants.SCANNER_NAME));
        intent.putExtra(Constants.SCANNER_TYPE, scannerType);
        startActivity(intent);
    }

    public void loadIdc(View view) {
        Intent intent = new Intent(this, IntelligentImageCaptureActivity.class);
        intent.putExtra(Constants.SCANNER_ID, scannerID);
        intent.putExtra(Constants.SCANNER_NAME, getIntent().getStringExtra(Constants.SCANNER_NAME));
        startActivity(intent);
    }

    public void loadBatteryStatistics(View view) {
        String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
        new AsyncTaskBatteryAvailable(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GETALL, this, BatteryStatistics.class).execute(new String[]{in_xml});

    }

    /**
     * Navigate to Scan Speed Analytics views
     *
     * @param view
     */
    public void loadScanSpeedAnalytics(View view) {
        // Scan speed analytics symbology type has set
        if (SsaSetSymbologyActivity.SSA_SYMBOLOGY_ENABLED_FLAG) {
            // navigate to scan speed analytics view
            Intent intent = new Intent(this, ScanSpeedAnalyticsActivity.class);
            intent.putExtra(Constants.SCANNER_ID, scannerID);
            intent.putExtra(Constants.SYMBOLOGY_SSA_ENABLED, SsaSetSymbologyActivity.SSA_ENABLED_SYMBOLOGY_OBJECT);
            intent.putExtra(Constants.SCANNER_NAME, getIntent().getStringExtra(Constants.SCANNER_NAME));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            int ssaStatus = 0;
            if (scannerType != 1) {
                ssaStatus = 2;
            }
            intent.putExtra(Constants.SSA_STATUS, ssaStatus);

            getApplicationContext().startActivity(intent);

        } else { // Scan speed analytics symbology type has not set
            // navigate to Scan speed analytics set view
            String in_xml = "<inArgs><scannerID>" + scannerID + "</scannerID></inArgs>";
            new AsyncTaskSSASvailable(scannerID, DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GETALL, this, SsaSetSymbologyActivity.class).execute(new String[]{in_xml});
        }
    }

    private class AsyncTaskBatteryAvailable extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        Context context;
        Class targetClass;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;

        public AsyncTaskBatteryAvailable(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, Context context, Class targetClass) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.context = context;
            this.targetClass = targetClass;
            RFIDResults res;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(ActiveDeviceActivity.this, "Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            boolean result = executeCommand(opcode, strings[0], sb, scannerId);
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GETALL) {
                if (result) {
                    try {
                        int i = 0;
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
                                    if (name.equals("attribute")) {
                                        if (text != null && text.trim().equals("30018")) {
                                            return true;
                                        }
                                    }
                                    break;
                            }
                            event = parser.next();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
                    }
                }
            }
            return false;
        }


        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

            Intent intent = new Intent(context, targetClass);
            intent.putExtra(Constants.SCANNER_ID, scannerID);
            intent.putExtra(Constants.SCANNER_NAME, getIntent().getStringExtra(Constants.SCANNER_NAME));
            intent.putExtra(Constants.BATTERY_STATUS, b);
            startActivity(intent);
        }


    }

    private class AsyncTaskSSASvailable extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        Context context;
        Class targetClass;
        private CustomProgressDialog progressDialog;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;

        public AsyncTaskSSASvailable(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, Context context, Class targetClass) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.context = context;
            this.targetClass = targetClass;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(ActiveDeviceActivity.this, "Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            StringBuilder sb = new StringBuilder();
            boolean result = executeCommand(opcode, strings[0], sb, scannerId);
            if (opcode == DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_RSM_ATTR_GETALL) {
                if (result) {
                    try {
                        int i = 0;
                        XmlPullParser parser = Xml.newPullParser();

                        parser.setInput(new StringReader(sb.toString()));
                        int event = parser.getEventType();
                        String text = null;
                        ssaSupportedAttribs = new ArrayList<Integer>();
                        while (event != XmlPullParser.END_DOCUMENT) {
                            String name = parser.getName();
                            switch (event) {
                                case XmlPullParser.START_TAG:
                                    break;
                                case XmlPullParser.TEXT:
                                    text = parser.getText();
                                    break;
                                case XmlPullParser.END_TAG:
                                    if (name.equals("attribute")) {
                                        if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_UPC))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_UPC);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_EAN_JAN))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_EAN_JAN);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_2_OF_5))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_2_OF_5);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_CODEBAR))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODEBAR);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_11))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_11);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_128))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_128);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_39))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_39);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_CODE_93))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_CODE_93);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_COMPOSITE))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_COMPOSITE);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_GS1_DATABAR))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_DATABAR);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_MSI))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_MSI);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_DATAMARIX))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_DATAMARIX);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_PDF))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_PDF);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_POSTAL_CODES))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_POSTAL_CODES);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_QR))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_QR);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_AZTEC))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_AZTEC);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_OCR))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_OCR);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_MAXICODE))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_MAXICODE);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_GS1_DATAMATRIX))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_DATAMATRIX);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_GS1_QR_CODE))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_GS1_QR_CODE);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_COUPON))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_COUPON);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_DIGIMARC_UPC))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_UPC);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_DIGIMARC_EAN_JAN))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_EAN_JAN);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_DIGIMARC_OTHER))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_DIGIMARC_OTHER);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_OTHER_1D))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER_1D);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_OTHER_2D))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER_2D);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_OTHER))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_OTHER);
                                            result = true;
                                        } else if (text != null && text.trim().equals(Integer.toString(RMD_ATTR_VALUE_SSA_HISTOGRAM_UNUSED_ID))) {
                                            ssaSupportedAttribs.add(RMD_ATTR_VALUE_SSA_DECODE_COUNT_UNUSED_ID);
                                            result = true;
                                        }
                                    }
                                    break;
                            }
                            event = parser.next();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, e.toString());
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
            SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
            int ssaStatus = 0;
            if (ssaSupportedAttribs.size() == 0) {
                ssaStatus = 1;
            } else if (scannerType != 1) {
                ssaStatus = 2;
            }

            Intent intent = new Intent(context, targetClass);
            intent.putExtra(Constants.SCANNER_ID, scannerID);
            intent.putIntegerArrayListExtra(Constants.SYMBOLOGY_SSA, (ArrayList<Integer>) ssaSupportedAttribs);
            intent.putExtra(Constants.SSA_STATUS, ssaStatus);
            startActivity(intent);
        }
    }

    /**
     * method to send connect command request to reader
     * after connect button clicked on connect password pairTaskDailog
     *
     * @param password     - reader password
     * @param readerDevice
     */
    public void connectClicked(String password, ReaderDevice readerDevice) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_RFID_FRAGMENT);
        if (fragment instanceof RFIDReadersListFragment) {
            ((RFIDReadersListFragment) fragment).ConnectwithPassword(password, readerDevice);
        }
    }

    /**
     * method which will exe cute after cancel button clicked on connect pwd pairTaskDailog
     *
     * @param readerDevice
     */
    public void cancelClicked(ReaderDevice readerDevice) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_RFID_FRAGMENT);
        if (fragment instanceof RFIDReadersListFragment) {
            ((RFIDReadersListFragment) fragment).readerDisconnected(readerDevice, true);
        }
    }


    public void findScanner(View view) {
        btnFindScanner = (Button) findViewById(R.id.btn_find_scanner);
        if (btnFindScanner != null) {
            btnFindScanner.setEnabled(false);
        }
        new FindScannerTask(scannerID).execute();
    }

    public void loadSampleBarcodes(View view) {
        Intent intent = new Intent(this, SampleBarcodes.class);
        intent.putExtra(Constants.SCANNER_ID, scannerID);
        intent.putExtra(Constants.SCANNER_NAME, getIntent().getStringExtra(Constants.SCANNER_NAME));
        startActivity(intent);
    }


    private class MyAsyncTask extends AsyncTask<String, Integer, Boolean> {
        int scannerId;
        StringBuilder outXML;
        DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode;
        private CustomProgressDialog progressDialog;

        public MyAsyncTask(int scannerId, DCSSDKDefs.DCSSDK_COMMAND_OPCODE opcode, StringBuilder outXML) {
            this.scannerId = scannerId;
            this.opcode = opcode;
            this.outXML = outXML;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new CustomProgressDialog(ActiveDeviceActivity.this, "Execute Command...");
            progressDialog.show();
        }


        @Override
        protected Boolean doInBackground(String... strings) {
            return executeCommand(opcode, strings[0], outXML, scannerId);
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (!b) {
                Toast.makeText(ActiveDeviceActivity.this, "Cannot perform the action", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private class FindScannerTask extends AsyncTask<String, Integer, Boolean> {
        int scannerId;

        public FindScannerTask(int scannerId) {
            this.scannerId = scannerId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }


        @Override
        protected Boolean doInBackground(String... strings) {

            long t0 = System.currentTimeMillis();

            TurnOnLEDPattern();
            BeepScanner();
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            }
            while (System.currentTimeMillis() - t0 < 3000) {
                VibrateScanner();
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                }
                VibrateScanner();
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                }
                BeepScanner();
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
                }
                VibrateScanner();
            }
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            }
            TurnOffLEDPattern();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean b) {
            super.onPostExecute(b);
            if (btnFindScanner != null) {
                btnFindScanner.setEnabled(true);
            }

        }




        private void TurnOnLEDPattern() {
            String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-int>" +
                    88 + "</arg-int></cmdArgs></inArgs>";
            StringBuilder outXML = new StringBuilder();
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML, outXML, scannerID);
        }

        private void TurnOffLEDPattern() {
            String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-int>" +
                    90 + "</arg-int></cmdArgs></inArgs>";
            StringBuilder outXML = new StringBuilder();
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML, outXML, scannerID);
        }

        private void VibrateScanner() {
            String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs>";
            StringBuilder outXML = new StringBuilder();
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_DEVICE_VIBRATION_FEEDBACK, inXML, outXML, scannerID);
        }

        private void BeepScanner() {
            String inXML = "<inArgs><scannerID>" + scannerID + "</scannerID><cmdArgs><arg-int>" +
                    RMD_ATTR_VALUE_ACTION_HIGH_HIGH_LOW_LOW_BEEP + "</arg-int></cmdArgs></inArgs>";
            StringBuilder outXML = new StringBuilder();
            executeCommand(DCSSDKDefs.DCSSDK_COMMAND_OPCODE.DCSSDK_SET_ACTION, inXML, outXML, scannerID);
        }

    }


    /////////////////////   RFID  Functions  ///////////////

    private void PrepareMatchModeList() {
        Log.d(TAG, "PrepareMatchModeList");
        if (TAG_LIST_MATCH_MODE && !TAG_LIST_LOADED) {
            //This for loop will reset all the items in the tagsListCSV(making Tag count to zero)
            for (int i = 0; i < tagsListCSV.size(); i++) {
                InventoryListItem inv = null;
                if (tagsListCSV.get(i).getCount() != 0) {
                    inv = tagsListCSV.remove(i);
                    InventoryListItem inventoryListItem = new InventoryListItem(inv.getTagID(), 0, null, null, null, null, null, null);
                    inventoryListItem.setTagDetails(inv.getTagDetails());
                    tagsListCSV.add(i, inventoryListItem);
                } else {
                    if (tagsListCSV.get(i).isVisible()) {
                        tagsListCSV.get(i).setVisible(false);
                    }
                }
            }
            UNIQUE_TAGS_CSV = tagsListCSV.size();
            tagsReadInventory.addAll(tagsListCSV);
            inventoryList.putAll(tagListMap);
            missedTags = tagsListCSV.size();
            matchingTags = 0;
            TAG_LIST_LOADED = true;
            Log.d(TAG, "PrepareMatchModeList done");
        }
    }

    public synchronized  void inventoryStartOrStop(View view) {
        mRFIDBaseActivity.inventoryStartOrStop();
    }

    public void loadNextFragment(int fragmentType) {
        int settingsTab = mAdapter.getSettingsTab();
        String PageTitle= " " ;
     try {

         switch (fragmentType) {
             case RAPID_READ_TAB:
                 PageTitle = "Rapid";
                 mAdapter.setRFIDMOde(RAPID_READ_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(RFID_TAB)).commit();
                 break;
             case LOCATE_TAG_TAB:
                 PageTitle = "Locate";
                 mAdapter.setRFIDMOde(LOCATE_TAG_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(RFID_TAB)).commit();
                 break;
             case INVENTORY_TAB:
                 PageTitle = "Inventory";
                 mAdapter.setRFIDMOde(INVENTORY_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(RFID_TAB)).commit();
                 break;
             case RFID_PREFILTERS_TAB:
                 PageTitle = "Prefilter";
                 mAdapter.setRFIDMOde(RFID_PREFILTERS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(RFID_TAB)).commit();
                 break;
             case RFID_ACCESS_TAB:
                 PageTitle = "Tag Write";
                 mAdapter.setRFIDMOde(RFID_ACCESS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(RFID_TAB)).commit();
                 break;
             case RFID_SETTINGS_TAB:
                 PageTitle = "RFID Settings";
                 mAdapter.setSettingsMode(RFID_SETTINGS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case SCAN_SETTINGS_TAB:
                 PageTitle = "Scan";
                 mAdapter.setSettingsMode(SCAN_SETTINGS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case SCAN_DATAVIEW_TAB:
                 PageTitle = "Scan Data";
                 mAdapter.setSCANMOde(SCAN_DATAVIEW_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(SCAN_TAB)).commit();
                 break;
             case SCAN_ADVANCED_TAB:
                 PageTitle = "Advanced Scan";
                 mAdapter.setReaderListMOde(SCAN_ADVANCED_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case SCAN_HOME_SETTINGS_TAB:
                 PageTitle = "Settings";
                 mAdapter.setReaderListMOde(SCAN_HOME_SETTINGS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;

             case READER_LIST_TAB:
                 PageTitle = "Readers";
                 mAdapter.setReaderListMOde(READER_LIST_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(READERS_TAB)).commit();
                 break;
             case DEVICE_PAIR_TAB:
                 PageTitle = "Scan"; //"Pair";
                 mAdapter.setReaderListMOde(DEVICE_PAIR_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(READERS_TAB)).commit();
                 break;
             case READER_DETAILS_TAB:
                 PageTitle = "Details";
                 mAdapter.setReaderListMOde(READER_DETAILS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(READERS_TAB)).commit();
                 break;

             case READER_WIFI_SETTINGS_TAB:
                 PageTitle = "Wi-Fi";
                 mAdapter.setReaderListMOde(READER_WIFI_SETTINGS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(READERS_TAB)).commit();
                 break;


             case MAIN_RFID_SETTINGS_TAB:
                 PageTitle ="RFID Settings";
                 mAdapter.setSettingsMode(MAIN_RFID_SETTINGS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case MAIN_HOME_SETTINGS_TAB:
                 PageTitle = "Settings";
                 mAdapter.setSettingsMode(MAIN_HOME_SETTINGS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case MAIN_GENERAL_SETTINGS_TAB:
                 PageTitle = "General Settings";
                 mAdapter.setSettingsMode(MAIN_GENERAL_SETTINGS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case APPLICATION_SETTINGS_TAB:
                 PageTitle = "Application";
                 mAdapter.setSettingsMode(APPLICATION_SETTINGS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case RFID_PROFILES_TAB:
                 PageTitle = "Profiles";
                 mAdapter.setSettingsMode(RFID_PROFILES_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case RFID_ADVANCED_OPTIONS_TAB:
                 PageTitle = "RFID Advanced Settings";
                 mAdapter.setSettingsMode(RFID_ADVANCED_OPTIONS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;

             case RFID_REGULATORY_TAB:
                 PageTitle = "Regulatory";
                 mAdapter.setSettingsMode(RFID_REGULATORY_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;

             case RFID_BEEPER_TAB:
                 PageTitle = "Beeper";
                 mAdapter.setSettingsMode(RFID_BEEPER_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;

             case RFID_LED_TAB:
                 PageTitle = "LED";
                 mAdapter.setSettingsMode(RFID_LED_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case RFID_WIFI_TAB:
                 PageTitle = "WiFi";
                 mAdapter.setSettingsMode(RFID_WIFI_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case CHARGE_TERMINAL_TAB:
                 PageTitle = "Charge Terminal";
                 mAdapter.setSettingsMode(CHARGE_TERMINAL_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case ANTENNA_SETTINGS_TAB:
                 PageTitle = "Antenna";
                 mAdapter.setSettingsMode(ANTENNA_SETTINGS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;

             case SINGULATION_CONTROL_TAB:
                 PageTitle = "Singulation";
                 mAdapter.setSettingsMode(SINGULATION_CONTROL_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case START_STOP_TRIGGER_TAB:
                 PageTitle = "Start/Stop";
                 mAdapter.setSettingsMode(START_STOP_TRIGGER_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case TAG_REPORTING_TAB:
                 PageTitle = "Tag Reporting";
                 mAdapter.setSettingsMode(TAG_REPORTING_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case  SAVE_CONFIG_TAB:
                 PageTitle = "Save";
                 mAdapter.setSettingsMode(SAVE_CONFIG_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case DPO_SETTING_TAB:
                 PageTitle = "Power";
                 mAdapter.setSettingsMode(DPO_SETTING_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case FACTORY_RESET_FRAGMENT_TAB:
                 PageTitle = "Factory Reset";
                 mAdapter.setSettingsMode(FACTORY_RESET_FRAGMENT_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case LOGGER_FRAGMENT_TAB:
                 PageTitle = "Logging";
                 mAdapter.setSettingsMode(LOGGER_FRAGMENT_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case DEVICE_RESET_TAB:
                 PageTitle = "Device Reset";
                 mAdapter.setSettingsMode(DEVICE_RESET_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case KEYREMAP_TAB:
                 PageTitle = "Trigger Map";
                 mAdapter.setSettingsMode(KEYREMAP_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case UPDATE_FIRMWARE_TAB:
                 PageTitle = "FW Update";
                 mAdapter.setSettingsMode(UPDATE_FIRMWARE_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case ASSERT_DEVICE_INFO_TAB:
                 PageTitle = "Device Info";
                 mAdapter.setSettingsMode(ASSERT_DEVICE_INFO_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case STATIC_IP_CONFIG:
                 PageTitle = "Network Ip Configuration";
                 mAdapter.setSettingsMode(STATIC_IP_CONFIG);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case BARCODE_SYMBOLOGIES_TAB:
                 PageTitle = "Symbologies";
                 mAdapter.setSettingsMode(BARCODE_SYMBOLOGIES_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case BEEPER_ACTION_TAB:
                 PageTitle = "Beeper";
                 mAdapter.setSettingsMode(BEEPER_ACTION_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case BATTERY_STATISTICS_TAB:
                 PageTitle = "Battery Statistics";
                 mAdapter.setSettingsMode(BATTERY_STATISTICS_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;
             case USB_MIFI_TAB:
                 PageTitle = "USB MiFi";
                 mAdapter.setSettingsMode(USB_MIFI_TAB);
                 getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(settingsTab)).commit();
                 break;

         }
         setActionBarTitle(PageTitle);
     }catch(NullPointerException ne)
     {
         return;
     }catch(IllegalStateException ise){

         return;
     }

        //getSupportFragmentManager().beginTransaction().remove(getCurrentFragment(RFID_TAB)).commit();
        getSupportFragmentManager().beginTransaction().addToBackStack(null);
        getSupportFragmentManager().executePendingTransactions();
        mAdapter.notifyDataSetChanged();
    }


    public Fragment getCurrentFragment(int position) {

        //return mAdapter.getCurrentFragment();
        return mAdapter.getRegisteredFragment(position);
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }
    void exportData() {
        if (mConnectedReader != null) {
            Uri uri = Uri.parse("content://com.android.externalstorage.documents/document/primary:Download");
            createFile1(uri);
            // new DataExportTask(getApplicationContext(), tagsReadInventory, mConnectedReader.getHostName(), TOTAL_TAGS, UNIQUE_TAGS, mRRStartedTime).execute();

        }
    }

    public void resetFactoryDefault() throws InvalidUsageException, OperationFailureException {
        Boolean btCon = false;
        try {

            if(mConnectedReader!= null && mConnectedReader.getTransport() != null &&
                    mConnectedReader.getTransport().equals("BLUETOOTH") ) {

                mRFIDBaseActivity.resetFactoryDefault();
                Thread.sleep(2000);
                mRFIDBaseActivity.onFactoryReset(RFIDController.mConnectedDevice);
            }else if( RFIDController.mConnectedDevice != null) {
                mRFIDBaseActivity.resetFactoryDefault();
            }

        }catch(OperationFailureException e){
            throw e;
        } catch (InterruptedException e) {
        }

    }




    public void onRadioButtonClicked(View view){

        Fragment fragment = getCurrentFragment(SETTINGS_TAB);
        if(fragment instanceof FactoryResetFragment){
            ((FactoryResetFragment)fragment).changeResetMode(view );
        }

    }
    public boolean deviceReset(String commandString) throws InvalidUsageException, OperationFailureException {
        return mRFIDBaseActivity.deviceReset();
    }

    void checkForExportPermission(final int code) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                switch (code) {
                    case REQUEST_CODE_ASK_PERMISSIONS:
                        exportData();
                        break;
                    case REQUEST_CODE_ASK_PERMISSIONS_CSV:
                        MatchModeFileLoader.getInstance(this).LoadMatchModeCSV();
                        break;
                }
            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    //Toast.makeText(this,"Write to external storage permission needed to export inventory.",Toast.LENGTH_LONG).show();
                    showMessageOKCancel("Write to external storage permission needed to export the inventory.",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            code);
                                }
                            });
                    return;
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, code);
            }
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(ActiveDeviceActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void disableScanner() {
        mRFIDBaseActivity.disableScanner();
    }

    public void enableScanner() {
        mRFIDBaseActivity.enableScanner();
    }

    @Override
    public void LoadTagListCSV() {
        Log.d(TAG, "LoadTagListCSV");
        mRFIDBaseActivity.LoadTagListCSV();

    }



    public void startbeepingTimer() {
        mRFIDBaseActivity.startbeepingTimer();
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private Context _context;
        private List<String> _listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<String, List<String>> _listDataChild;

        public ExpandableListAdapter(Context context,
                                     List<String> listDataHeader,
                                     HashMap<String, List<String>> listChildData) {
            this._context = context;
            this._listDataHeader = listDataHeader;
            this._listDataChild = listChildData;
        }

        @Override
        public Object getChild(int groupPosition, int childPosititon) {
            return this._listDataChild.get(
                    this._listDataHeader.get(groupPosition))
                    .get(childPosititon);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {

            final String childText = (String) getChild(groupPosition,
                    childPosition);

            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.drawer_list_sub_item, null);
            }
            TextView txtListChild = (TextView) convertView
                    .findViewById(R.id.drawerItemName);

            txtListChild.setText(childText);

            // adding icon to expandable list view
            ImageView imgListGroup = (ImageView) convertView
                    .findViewById(R.id.drawerIcon);

            if(groupPosition == 1) {
                imgListGroup.setImageResource(managexx_icon[childPosition]);
            }
            //imgListGroup.setImageResource(icon[groupPosition+childPosition]);

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return this._listDataChild.get(
                        this._listDataHeader.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this._listDataHeader.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return this._listDataHeader.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            String headerTitle = (String) getGroup(groupPosition);
            if (convertView == null) {
                LayoutInflater infalInflater = (LayoutInflater) this._context
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = infalInflater.inflate(R.layout.drawer_list_item, null);
            }

            TextView lblListHeader = (TextView) convertView
                    .findViewById(R.id.drawerItemName);
            lblListHeader.setText(headerTitle);

            // adding icon to expandable list view
            ImageView imgListGroup = (ImageView) convertView
                    .findViewById(R.id.drawerIcon);

            imgListGroup.setImageResource(icon[groupPosition]);

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
        public void performtagmatchClick(){
        if (inventoryBT != null) {
            if (mIsInventoryRunning) {
                inventoryBT.performClick();
                startbeepingTimer();
            }
        }

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
    private void ShowPlugInPathChangeDialog() {
        if (!isFinishing()) {
            final Dialog dialog = new Dialog(ActiveDeviceActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_plugin_path_change);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            TextView declineButton = (TextView) dialog.findViewById(R.id.btn_ok);
            declineButton.setOnClickListener(v -> {
                dialog.dismiss();
            });
        }
    }


    public void createDWProfile() {
        // MAIN BUNDLE PROPERTIES
        if((RFIDController.mConnectedReader != null ) && RFIDController.mConnectedReader.getHostName()!= null && RFIDController.mConnectedReader.getHostName().startsWith("MC33")) {


            Bundle bMain = new Bundle();
            bMain.putString("PROFILE_NAME", "RFIDMobileApp");
            bMain.putString("PROFILE_ENABLED", "true");              // <- that will be enabled
            bMain.putString("CONFIG_MODE", "CREATE_IF_NOT_EXIST");   // <- or created if necessary.
            // PLUGIN_CONFIG BUNDLE PROPERTIES
            Bundle scanBundle = new Bundle();
            scanBundle.putString("PLUGIN_NAME", "BARCODE"); // barcode plugin
            scanBundle.putString("RESET_CONFIG", "true");
            // PARAM_LIST BUNDLE PROPERTIES
            Bundle scanParams = new Bundle();
            scanParams.putString("scanner_selection", "auto");
            scanParams.putString("scanner_input_enabled", "false"); // Mainly disable scanner plugin
            // NEST THE BUNDLE "bParams" WITHIN THE BUNDLE "bConfig"
            scanBundle.putBundle("PARAM_LIST", scanParams);

            Bundle keystrokeBundle = new Bundle();
            keystrokeBundle.putString("PLUGIN_NAME", "KEYSTROKE");
            Bundle keyStrokeParams = new Bundle();
            keyStrokeParams.putString("keystroke_output_enabled", "false");
            keyStrokeParams.putString("keystroke_action_char", "9"); // 0, 9 , 10, 13
            keyStrokeParams.putString("keystroke_delay_extended_ascii", "500");
            keyStrokeParams.putString("keystroke_delay_control_chars", "800");
            keystrokeBundle.putBundle("PARAM_LIST", keyStrokeParams);

            Bundle rfidConfigParamList = new Bundle();
            rfidConfigParamList.putString("rfid_input_enabled", "false");
            Bundle rfidConfigBundle = new Bundle();
            rfidConfigBundle.putString("PLUGIN_NAME", "RFID");
            rfidConfigBundle.putString("RESET_CONFIG", "true");
            rfidConfigBundle.putBundle("PARAM_LIST", rfidConfigParamList);


            Bundle bConfigIntent = new Bundle();
            Bundle bParamsIntent = new Bundle();
            bParamsIntent.putString("intent_output_enabled", "true");
            bParamsIntent.putString("intent_action", "com.symbol.dwudiusertokens.udi");
            bParamsIntent.putString("intent_category", "zebra.intent.dwudiusertokens.UDI");
            bParamsIntent.putInt("intent_delivery", 2); //Use "0" for Start Activity, "1" for Start Service, "2" for Broadcast, "3" for start foreground service
            bConfigIntent.putString("PLUGIN_NAME", "INTENT");
            bConfigIntent.putString("RESET_CONFIG", "true");
            bConfigIntent.putBundle("PARAM_LIST", bParamsIntent);


            // THEN NEST THE "bConfig" BUNDLE WITHIN THE MAIN BUNDLE "bMain"
            ArrayList<Bundle> bundleArrayList = new ArrayList<>();
            bundleArrayList.add(scanBundle);
            bundleArrayList.add(rfidConfigBundle);
            bundleArrayList.add(keystrokeBundle);
            bundleArrayList.add(bConfigIntent);

            // following requires arrayList
            bMain.putParcelableArrayList("PLUGIN_CONFIG", bundleArrayList);
            // CREATE APP_LIST BUNDLES (apps and/or activities to be associated with the Profile)
            Bundle ActivityList = new Bundle();
            ActivityList.putString("PACKAGE_NAME", getPackageName());      // Associate the profile with this app
            ActivityList.putStringArray("ACTIVITY_LIST", new String[]{"*"});

            // NEXT APP_LIST BUNDLE(S) INTO THE MAIN BUNDLE
            bMain.putParcelableArray("APP_LIST", new Bundle[]{
                    ActivityList
            });
            Intent i = new Intent();
            i.setAction("com.symbol.datawedge.api.ACTION");
            i.putExtra("com.symbol.datawedge.api.SET_CONFIG", bMain);
            i.putExtra("SEND_RESULT", "true");
            i.putExtra("COMMAND_IDENTIFIER", Application.RFID_DATAWEDGE_PROFILE_CREATION);
            sendBroadcast(i);
        }
    }


}

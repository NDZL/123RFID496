package com.zebra.demo.rfidreader.settings;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.CustomProgressDialog;
import com.zebra.demo.rfidreader.common.CustomToast;
import com.zebra.demo.rfidreader.common.ResponseHandlerInterfaces;
import com.zebra.demo.rfidreader.home.RFIDBaseActivity;
import com.zebra.demo.rfidreader.notifications.NotificationUtil;
import com.zebra.demo.rfidreader.reader_connection.PasswordDialog;
import com.zebra.demo.rfidreader.reader_connection.RFIDReadersListFragment;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.rfid.api3.ENUM_KEYLAYOUT_TYPE;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;

import static com.zebra.demo.rfidreader.common.Constants.SETTING_ON_FACTORY;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mConnectedReader;

/**
 * Class to handle the UI for setting details like antenna config, singulation etc..
 * Hosts a fragment for UI.
 */
public class SettingsDetailActivity extends AppCompatActivity implements
        ResponseHandlerInterfaces.ReaderDeviceFoundHandler,
        Readers.RFIDReaderEventHandler,
        ResponseHandlerInterfaces.BatteryNotificationHandler,
        AdvancedOptionItemFragment.OnAdvancedListFragmentInteractionListener, AdapterView.OnItemSelectedListener {
    //Tag to identify the currently displayed fragment
    protected static final String TAG_CONTENT_FRAGMENT = "ContentFragment";
    public static final int application_id = 0x1000001;
    protected ProgressDialog progressDialog;
    private static String TAG = "RFID SettingsDetailActivity";
    public static boolean mSettingOnFactory = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_detail);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Application.contextSettingDetails = this;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RFIDBaseActivity.addReaderDeviceFoundHandler(this);
        RFIDBaseActivity.addBatteryNotificationHandler(this);
        RFIDBaseActivity.getInstance().setReaderstatuscallback(this);
        if (RFIDController.readers == null) {
            RFIDController.readers = new Readers(this, ENUM_TRANSPORT.ALL);
        }
        // attach to reader list handler
        RFIDController.readers.attach(this);
        mSettingOnFactory = getIntent().getBooleanExtra(SETTING_ON_FACTORY, false);

        if (savedInstanceState != null) {
            return;
        } else {
            startFragment(getIntent());
        }

    }


    /**
     * start the fragment based on intent data
     *
     * @param intent received intent from previous activity
     */
    protected void startFragment(Intent intent) {
        Fragment fragment = null;
        if(RFIDController.mConnectedReader == null ){
            Toast.makeText(this, "Reader not connected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        int settingItemSelected = intent.getIntExtra(Constants.SETTING_ITEM_ID, R.id.readers_list);
        //Show the selected item
        switch (settingItemSelected) {
            case 0:
//                fragment = InventoryFragment.newInstance();
                break;
            case R.id.readers_list:
                fragment = RFIDReadersListFragment.getInstance();
                //Intent deviceListIntent = new Intent(this, AvailableReaderActivity.class);
                //startActivity(deviceListIntent);
                break;
            //case R.id.application:
            case SettingsDetailActivity.application_id:
                fragment = ApplicationSettingsFragment.newInstance();
                break;
            case R.id.profiles:
                fragment = ProfileFragment.newInstance();
                break;
            case R.id.advanced_options:
                fragment = AdvancedOptionItemFragment.newInstance();
                break;
            case R.id.regulatory:
                fragment = RegulatorySettingsFragment.newInstance();
                break;
            case R.id.battery:
                fragment = BatteryFragment.newInstance();
                break;
            case R.id.beeper:
                fragment = BeeperFragment.newInstance();
                break;
            case R.id.led:
                fragment = LedFragment.newInstance();
                break;
            case R.id.wifi_power:
                fragment = WifiFragment.newInstance();
                break;
            case R.id.charge_terminal:
                fragment = ChargeTerminalFragment.newInstance();
                break;
            case R.id.usb_mifi:
                fragment = UsbMiFiFragment.newInstance();
                break;
        }
        if (fragment != null) {

            getSupportFragmentManager().beginTransaction().replace(R.id.settings_content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();

        }
        if( settingItemSelected == SettingsDetailActivity.application_id) {
            setTitle("Application");
        }
        else {
            if(settingItemSelected == R.id.battery){
                setTitle("Battery");
            }else {
                setTitle(SettingsContent.ITEM_MAP.get(settingItemSelected + "").content);
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        RFIDBaseActivity.activityResumed();
        Application.contextSettingDetails = this;
    }

    /**
     * call back of activity,which will call before activity went to paused
     */
    @Override
    public void onPause() {
        super.onPause();
        //RFIDBaseActivity.activityPaused();
        Application.contextSettingDetails = null;
        RFIDController.readers.deattach(this);
        // remove notification handlers
        RFIDBaseActivity.removeReaderDeviceFoundHandler(this);
        RFIDBaseActivity.removeBatteryNotificationHandler(this);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if(fragment instanceof ApplicationSettingsFragment) {
        }else if (isFinishing() == false) {
             finish();
        }

    }

    @Override
    protected void onDestroy() {
        // deattach to reader list handler
        //RFIDController.readers.deattach(this);
        // remove notification handlers
        //RFIDBaseActivity.removeReaderDeviceFoundHandler(this);
        //RFIDBaseActivity.removeBatteryNotificationHandler(this);
        RFIDBaseActivity.getInstance().resetReaderstatuscallback();
        super.onDestroy();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        startFragment(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.no_items, menu);
        if (findViewById(android.R.id.home) != null)
            findViewById(android.R.id.home).setPadding(0, 0, 20, 0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
            //return super.onOptionsItemSelected(item);
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //We are handling back pressed for saving settings(if any). Notify the appropriate fragment.
        //{@link BaseReceiverActivity # onBackPressed should be called by the fragment when the processing is done}
        //super.onBackPressed();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment != null && fragment instanceof BackPressedFragment) {
            ((BackPressedFragment) fragment).onBackPressed();
        } else {
            super.onBackPressed();
        }

    }

    /**
     * Method to be called from Fragments of this activity after handling the response from the reader(success / failure)
     */
    public void callBackPressed() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //if(!SettingsDetailActivity.super.isFinishing())
                //    SettingsDetailActivity.super.onBackPressed();
                if(isFinishing() == false) {
                    finish();
                }
            }
        });
    }

    /**
     * method to stop progress pairTaskDailog on timeout
     *
     * @param time    timeout of the progress pairTaskDailog
     * @param d       id of progress pairTaskDailog
     * @param command command that has been sent to the reader
     */
    public void timerDelayRemoveDialog(long time, final Dialog d, final String command, final boolean isPressBack) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (d != null && d.isShowing()) {
                    sendNotification(Constants.ACTION_READER_STATUS_OBTAINED, command + " timeout");
                    d.dismiss();
                    if (ActiveDeviceActivity.isActivityVisible() && isPressBack)
                        callBackPressed();
                }
            }
        }, time);
    }



    @Override
    public void ReaderDeviceConnected(ReaderDevice device) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof RFIDReadersListFragment) {
            ((RFIDReadersListFragment) fragment).ReaderDeviceConnected(device);
        } else if (fragment instanceof RegulatorySettingsFragment) {
            ((RegulatorySettingsFragment) fragment).deviceConnected();
        } else if (fragment instanceof TagReportingFragment) {
            ((TagReportingFragment) fragment).deviceConnected();
        } else if (fragment instanceof DPOSettingsFragment) {
            ((DPOSettingsFragment) fragment).deviceConnected();
        } else if (fragment instanceof AntennaSettingsFragment) {
            ((AntennaSettingsFragment) fragment).deviceConnected();
        } else if (fragment instanceof SaveConfigurationsFragment) {
            ((SaveConfigurationsFragment) fragment).deviceConnected();
        } else if (fragment instanceof SingulationControlFragment) {
            ((SingulationControlFragment) fragment).deviceConnected();
        }


    }

    @Override
    public void ReaderDeviceDisConnected(ReaderDevice device) {
        PasswordDialog.isDialogShowing = false;
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof RFIDReadersListFragment) {
            ((RFIDReadersListFragment) fragment).ReaderDeviceDisConnected(device);
            ((RFIDReadersListFragment) fragment).readerDisconnected(device, false);
        } else if (fragment instanceof BatteryFragment) {
            ((BatteryFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof TagReportingFragment) {
            ((TagReportingFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof DPOSettingsFragment) {
            ((DPOSettingsFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof AntennaSettingsFragment) {
            ((AntennaSettingsFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof RegulatorySettingsFragment) {
            ((RegulatorySettingsFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof SaveConfigurationsFragment) {
            ((SaveConfigurationsFragment) fragment).deviceDisconnected();
        } else if (fragment instanceof SingulationControlFragment) {
            ((SingulationControlFragment) fragment).deviceDisconnected();
        }
    }

    @Override
    public void ReaderDeviceConnFailed(ReaderDevice device) {
    }

    public void sendNotification(String action, String data) {
        if (ActiveDeviceActivity.isActivityVisible()) {
            if (action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_CRITICAL) || action.equalsIgnoreCase(Constants.ACTION_READER_BATTERY_LOW)) {
                new CustomToast(this, R.layout.toast_layout, data).show();
            } else {
                Toast.makeText(getApplicationContext(), data, Toast.LENGTH_SHORT).show();
            }
        } else {
            /*Intent i = new Intent(this, NotificationsService.class);
            i.putExtra(Constants.INTENT_ACTION, action);
            i.putExtra(Constants.INTENT_DATA, data);
            startService(i);*/

           NotificationUtil.displayNotificationforSettingsDeialActivity(this, action, data);
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
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
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
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof RFIDReadersListFragment) {
            ((RFIDReadersListFragment) fragment).readerDisconnected(readerDevice, true);
        }
    }

    @Override
    public void RFIDReaderAppeared(ReaderDevice device) {
        runOnUiThread(() -> {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
            if (fragment instanceof RFIDReadersListFragment) {
                ((RFIDReadersListFragment) fragment).RFIDReaderAppeared(device);
            }
            if (RFIDController.NOTIFY_READER_AVAILABLE) {
                if (!device.getName().equalsIgnoreCase("null"))
                    sendNotification(Constants.ACTION_READER_AVAILABLE, device.getName() + " is available.");
            }
        });
    }

    @Override
    public void RFIDReaderDisappeared(ReaderDevice device) {
        runOnUiThread(() -> {
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
            if (fragment instanceof RFIDReadersListFragment) {
                ((RFIDReadersListFragment) fragment).RFIDReaderDisappeared(device);
            }
            if (RFIDController.NOTIFY_READER_AVAILABLE)
                sendNotification(Constants.ACTION_READER_AVAILABLE, device.getName() + " is unavailable.");
            RFIDController.mReaderDisappeared = device;
            finish();
        });
    }

    @Override
    public void deviceStatusReceived(int level, boolean charging, String cause) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if (fragment instanceof BatteryFragment) {
            ((BatteryFragment) fragment).deviceStatusReceived(level, charging, cause);
        }
    }

    @Override
    public void OnAdvancedListFragmentInteractionListener(AdvancedOptionsContent.SettingItem item) {
        Fragment fragment = null;
        int settingItemSelected = Integer.parseInt(item.id);
        //Show the selected item
        switch (settingItemSelected) {
            case R.id.antenna:
                fragment = AntennaSettingsFragment.newInstance();
                break;
            case R.id.singulation_control:
                fragment = SingulationControlFragment.newInstance();
                break;
            case R.id.start_stop_triggers:
                fragment = StartStopTriggersFragment.newInstance();
                break;
            case R.id.tag_reporting:
                fragment = TagReportingFragment.newInstance();
                break;
            case R.id.save_configuration:
                fragment = SaveConfigurationsFragment.newInstance();
                break;
            case R.id.power_management:
                fragment = DPOSettingsFragment.newInstance();
                break;
        }
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.settings_content_frame, fragment, TAG_CONTENT_FRAGMENT).commit();
        }
        setTitle(item.content);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();

        // Showing selected spinner item
        if(mConnectedReader == null )
            return;

        try {
            switch(position)
            {
                case 0:
                   // Toast.makeText(parent.getContext(), "Trigger Selected: " + item, Toast.LENGTH_LONG).show();
                    mConnectedReader.Config.setKeylayoutType(ENUM_KEYLAYOUT_TYPE.UPPER_TRIGGER_FOR_RFID);
                    break;
                case 1:
                  //  Toast.makeText(parent.getContext(), "Trigger Selected: " + item, Toast.LENGTH_LONG).show();
                    mConnectedReader.Config.setKeylayoutType(ENUM_KEYLAYOUT_TYPE.UPPER_TRIGGER_FOR_SCAN);
                    break;
                case 2:
                   // Toast.makeText(parent.getContext(), "Trigger Selected: " + item, Toast.LENGTH_LONG).show();
                    mConnectedReader.Config.setKeylayoutType(ENUM_KEYLAYOUT_TYPE.LOWER_TRIGGER_FOR_SLED_SCAN);
                    break;
                case 3:
                  //  Toast.makeText(parent.getContext(), "TriggerTrigger Selected: " + item, Toast.LENGTH_LONG).show();
                    mConnectedReader.Config.setKeylayoutType(ENUM_KEYLAYOUT_TYPE.UPPER_TRIGGER_FOR_SLED_SCAN);
                    break;
            }
            if(RFIDController.mConnectedReader.getHostName().startsWith("RFD40")) {
                String SelectedKeyLayout = parent.getItemAtPosition(position).toString();
                ApplicationSettingsFragment.SetSpinnerText(SelectedKeyLayout);
            }

            else{
                String hostname = RFIDController.mConnectedReader.getHostName();
                Toast.makeText(parent.getContext(), "Trigger Mapping feature is not supported for "+hostname , Toast.LENGTH_LONG).show();
            }
            /*Adding to shared preference*/
            SharedPreferences settings = getSharedPreferences(Constants.APP_SETTINGS_STATUS, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(Constants.KEYLAYOUT, position);
            editor.commit();


        } catch (InvalidUsageException e) {
           Log.d(TAG,  "Returned SDK Exception");
        } catch (OperationFailureException e) {
           Log.d(TAG,  "Returned SDK Exception");
        }

    }



    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if(fragment instanceof ApplicationSettingsFragment)
        {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void showProfileSettings(View view) {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if(fragment instanceof RegulatorySettingsFragment) {
            ((RegulatorySettingsFragment) fragment).setRegulatory();
        }

    }

    public void saveProfileSelection(View view) {

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_CONTENT_FRAGMENT);
        if(fragment instanceof ProfileFragment) {
            SettingsDetailActivity.mSettingOnFactory = false;
            ((ProfileFragment) fragment).onBackPressed();
        }


    }
}
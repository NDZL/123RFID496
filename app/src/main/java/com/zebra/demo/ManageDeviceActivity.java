package com.zebra.demo;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;


import com.zebra.demo.rfidreader.home.RFIDBaseActivity;
import com.zebra.demo.rfidreader.manager.DeviceResetFragment;
import com.zebra.demo.rfidreader.manager.FactoryResetFragment;
import com.zebra.demo.rfidreader.rfid.RFIDController;
import com.zebra.demo.scanner.helpers.Constants;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;

import com.zebra.rfid.api3.STATUS_EVENT_TYPE;



public class ManageDeviceActivity extends AppCompatActivity  implements Readers.RFIDReaderEventHandler, RfidEventsListener {

    private static final String MANAGEDEVICEFRAGMENT = "ManageDeviceFragment";
    private RFIDBaseActivity mRfidBaseActivity;
    Fragment fragment = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_device_activity);
        this.setTitle("Device Logs");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if(getIntent().getExtras() != null)
        {
            switch(getIntent().getExtras().getInt(Constants.MNG_FRAGMENT_ID))
            {
                case 0:
                    fragment = FactoryResetFragment.newInstance();
                    break;
                case 1:
                    fragment = DeviceResetFragment.newInstance();
                    break;
                case 2:
                    fragment = LoggerFragment.newInstance();
                    break;
            }
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        mRfidBaseActivity = RFIDBaseActivity.getInstance();
        mRfidBaseActivity.setReaderstatuscallback(this);
        if (fragment != null) {
            switchToFragment(fragment);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        finish();
        return true;
    }
    public void switchToFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.manage_frame_layout, fragment, MANAGEDEVICEFRAGMENT).commit();
        }
    }

    public void resetFactoryDefault() throws InvalidUsageException, OperationFailureException {

        mRfidBaseActivity.resetFactoryDefault();
    }

    public boolean deviceReset(String commandString) throws InvalidUsageException, OperationFailureException {
        return mRfidBaseActivity.deviceReset();
    }

    @Override
    public void RFIDReaderAppeared(ReaderDevice readerDevice) {
        runOnUiThread(() -> {
            if (fragment != null) {
                if (fragment instanceof FactoryResetFragment) {
                    ((FactoryResetFragment) fragment).RFIDReaderAppeared(readerDevice);
                    //finish();

                } else if (fragment instanceof DeviceResetFragment) {
                    ((DeviceResetFragment) fragment).RFIDReaderAppeared(readerDevice);
                }
            }
        });
    }

    @Override
    public void RFIDReaderDisappeared(final ReaderDevice readerDevice) {
        //Intent intent;
        //intent = new Intent(getActivity(), DeviceDiscoverActivity.class);
        //intent.putExtra("enable_toolbar", false);
        //startActivity(intent);
        runOnUiThread(() -> {
            if (fragment != null) {
                if (fragment instanceof FactoryResetFragment) {
                    ((FactoryResetFragment) fragment).RFIDReaderDisappeared(readerDevice);

                } else if (fragment instanceof DeviceResetFragment) {
                    ((DeviceResetFragment) fragment).RFIDReaderDisappeared(readerDevice);
                } else if (fragment instanceof LoggerFragment) {
                    ((LoggerFragment) fragment).RFIDReaderDisappeared(readerDevice);
                }
            }
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        ///super.onBackPressed();
        finish();
        return;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //mRfidBaseActivity.resetReaderstatuscallback();
        //mRfidBaseActivity.resetEventcallback(this);
        //finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRfidBaseActivity.resetReaderstatuscallback();


    }

    @Override
    protected void onResume() {
        super.onResume();
        mRfidBaseActivity.setReaderstatuscallback(this);
    }

    @Override
    public void eventReadNotify(RfidReadEvents rfidReadEvents) {

    }

    @Override
    public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
        if (rfidStatusEvents.StatusEventData.getStatusEventType() == STATUS_EVENT_TYPE.DISCONNECTION_EVENT) {
            if(fragment != null) {
                if (fragment instanceof FactoryResetFragment )
                {
                    ((FactoryResetFragment) fragment).eventStatusNotify(rfidStatusEvents);

                }else if (fragment instanceof FactoryResetFragment )
                {
                    ((DeviceResetFragment) fragment).eventStatusNotify(rfidStatusEvents);
                }
            }

            RFIDController.mConnectedReader = null;
        }
    }
}

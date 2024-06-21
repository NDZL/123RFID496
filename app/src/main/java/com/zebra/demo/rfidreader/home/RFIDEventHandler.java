package com.zebra.demo.rfidreader.home;

import android.util.Log;

import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.RfidWifiScanEvents;


public class RFIDEventHandler implements RfidEventsListener {
        private static String TAG = "RFIDEventHandler";

        @Override
        public void eventReadNotify(RfidReadEvents e) {

            Log.d(TAG, "RFIDEventHandler eventReadNotify");
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Log.d(TAG, "RFIDEventHandler eventStatusNotify");
        }

    }


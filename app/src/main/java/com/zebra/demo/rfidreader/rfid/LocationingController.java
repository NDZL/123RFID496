package com.zebra.demo.rfidreader.rfid;

import android.os.AsyncTask;
import android.util.Log;

import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.demo.rfidreader.common.Constants;
import com.zebra.demo.rfidreader.common.asciitohex;

import static com.zebra.demo.rfidreader.rfid.RFIDController.isInventoryAborted;
import static com.zebra.demo.rfidreader.rfid.RFIDController.isLocatingTag;
import static com.zebra.demo.rfidreader.rfid.RFIDController.isLocationingAborted;
import static com.zebra.demo.rfidreader.rfid.RFIDController.mIsInventoryRunning;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class LocationingController {
    private static final String TAG = "LocationingController";

    Lock locateLock = new Lock() {
        private boolean isLocked = false;

        public synchronized void lock() {
            while(isLocked){
                try {
                    wait();
                } catch (InterruptedException e) {
                   Log.d(TAG,  "Returned SDK Exception");
                }
            }
            isLocked = true;
        }

        public synchronized void unlock(){
            isLocked = false;
            notify();
        }
        @Override
        public void lockInterruptibly() throws InterruptedException {

        }

        @Override
        public boolean tryLock() {
            return false;
        }

        @Override
        public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
            return false;
        }



        @Override
        public Condition newCondition() {
            return null;
        }
    };

    protected LocationingController() {
    }

    public void locationing(final String locateTag, final RfidListeners rfidListeners) {

        if (RFIDController.mConnectedReader != null && RFIDController.mConnectedReader.isConnected()) {
            if (!RFIDController.isLocatingTag) {
                RFIDController.currentLocatingTag = locateTag;
                RFIDController.TagProximityPercent = 0;
                if (locateTag != null && !locateTag.isEmpty()) {
                    RFIDController.isLocatingTag = true;
                    new AsyncTask<Void, Void, Boolean>() {

                        private InvalidUsageException invalidUsageException;
                        private OperationFailureException operationFailureException;

                        @Override
                        protected Boolean doInBackground(Void... voids) {
                            locateLock.lock();
                            try {
                                if (RFIDController.asciiMode) {
                                    RFIDController.mConnectedReader.Actions.TagLocationing.Perform(asciitohex.convert(locateTag), null, null);
                                    RFIDController.isLocatingTag = true;
                                }else {
                                    RFIDController.mConnectedReader.Actions.TagLocationing.Perform(locateTag, null, null);
                                    RFIDController.isLocatingTag = true;
                                }
                            } catch (InvalidUsageException e) {
                               Log.d(TAG,  "Returned SDK Exception");
                                invalidUsageException = e;
                            } catch (OperationFailureException e) {
                               Log.d(TAG,  "Returned SDK Exception");
                                operationFailureException = e;
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Boolean result) {
                            locateLock.unlock();
                            RFIDController.isLocatingTag = true;
                            if (invalidUsageException != null) {
                                RFIDController.currentLocatingTag = null;
                                RFIDController.isLocatingTag = false;
                                rfidListeners.onFailure(invalidUsageException);
                            } else if (operationFailureException != null) {
                                RFIDController.currentLocatingTag = null;
                                RFIDController.isLocatingTag = false;
                                rfidListeners.onFailure(operationFailureException);


                            } else
                                rfidListeners.onSuccess(null);
                        }
                    }.execute();
                } else {
                    Log.d(RFIDController.TAG, Constants.TAG_EMPTY);
                    rfidListeners.onFailure(Constants.TAG_EMPTY);
                }

            } else {
                isLocationingAborted = false;
                mIsInventoryRunning = false;
                isLocatingTag = false;
                isInventoryAborted = false;
                new AsyncTask<Void, Void, Boolean>() {
                    private InvalidUsageException invalidUsageException;
                    private OperationFailureException operationFailureException;

                    @Override
                    protected Boolean doInBackground(Void... voids) {
                        locateLock.lock();
                        try {
                            RFIDController.mConnectedReader.Actions.TagLocationing.Stop();
                            if (((RFIDController.settings_startTrigger != null && (RFIDController.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_HANDHELD || RFIDController.settings_startTrigger.getTriggerType() == START_TRIGGER_TYPE.START_TRIGGER_TYPE_PERIODIC)))
                                    || (RFIDController.isBatchModeInventoryRunning != null && RFIDController.isBatchModeInventoryRunning))
                                ConnectionController.operationHasAborted(rfidListeners);
                        } catch (InvalidUsageException e) {
                            invalidUsageException = e;
                           Log.d(TAG,  "Returned SDK Exception");
                        } catch (OperationFailureException e) {
                            operationFailureException = e;
                           Log.d(TAG,  "Returned SDK Exception");
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Boolean result) {
                        locateLock.unlock();
                        RFIDController.isLocatingTag = false;
                        RFIDController.currentLocatingTag = null;
                        if (invalidUsageException != null) {
                            rfidListeners.onFailure(invalidUsageException);

                        } else if (operationFailureException != null) {
                            rfidListeners.onFailure(operationFailureException);
                        } else
                            rfidListeners.onSuccess(null);
                    }
                }.execute();
            }
        } else
            rfidListeners.onFailure("No Active Connection with Reader");
    }


}

package com.zebra.demo.rfidreader.locate_tag.multitag_locate;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.fragment.app.Fragment;

import com.zebra.demo.ActiveDeviceActivity;
import com.zebra.demo.rfidreader.common.asciitohex;
import com.zebra.rfid.api3.TagData;
import com.zebra.demo.application.Application;
import com.zebra.demo.rfidreader.common.hextoascii;
import com.zebra.demo.rfidreader.locate_tag.LocateOperationsFragment;
import com.zebra.demo.rfidreader.rfid.RFIDController;

/**
 * Async Task, which will handle multi-tag locationing tag data response from reader.
 */
public class MultiTagLocateResponseHandlerTask extends AsyncTask<Void, Void, Boolean> {
    private Context mContext;
    private TagData tagData;
    private Fragment fragment;
    private static final String TAG = "MultiTagLocateResponseHandler";

    public MultiTagLocateResponseHandlerTask(Context context, TagData tagData, Fragment fragment) {
        mContext = context;
        this.tagData = tagData;
        this.fragment = fragment;

    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean success = false;
        try {
            String asciiTag = tagData.getTagID();
            if(RFIDController.asciiMode) {
                asciiTag = asciitohex.convert(asciiTag).toUpperCase();
                if(Application.multiTagLocateTagListMap.containsKey(asciiTag)) {
                    Application.multiTagLocateTagListMap.get(asciiTag).setProximityPercent(tagData.MultiTagLocateInfo.getRelativeDistance());
                    int readCount = Application.multiTagLocateTagListMap.get(asciiTag).getReadCount() + tagData.getTagSeenCount();
                    Application.multiTagLocateTagListMap.get(asciiTag).setReadCount(readCount);
                    success = true;
                    // beep on each tag read
                    ((ActiveDeviceActivity) mContext).startbeepingTimer();
                }
            } else if (Application.multiTagLocateTagListMap.containsKey(tagData.getTagID())) {
                Application.multiTagLocateTagListMap.get(tagData.getTagID()).setProximityPercent(tagData.MultiTagLocateInfo.getRelativeDistance());
                int readCount = Application.multiTagLocateTagListMap.get(tagData.getTagID()).getReadCount() + tagData.getTagSeenCount();
                Application.multiTagLocateTagListMap.get(tagData.getTagID()).setReadCount(readCount);
                success = true;
                // beep on each tag read
                ((ActiveDeviceActivity) mContext).startbeepingTimer();
            }
        }
        catch(Exception e){
            if( e!= null && e.getStackTrace().length>0){ Log.e(TAG, e.getStackTrace()[0].toString()); }
            success = false;
        }
        return success;
    }


    @Override
    protected void onPostExecute(Boolean result) {
        cancel(true);
        if (result) {
            if (fragment instanceof LocateOperationsFragment) {
                  ((LocateOperationsFragment) fragment).handleLocateTagResponse();
            }
            tagData = null;
        }
    }
}

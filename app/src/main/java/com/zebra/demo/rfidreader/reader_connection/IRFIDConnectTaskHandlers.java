package com.zebra.demo.rfidreader.reader_connection;

import com.zebra.rfid.api3.ReaderDevice;

public interface IRFIDConnectTaskHandlers {

    void StoreConnectedReader();
    void ReaderDeviceConnected(ReaderDevice device);
    void sendNotification(String action, String data);
    void ReaderDeviceConnFailed(ReaderDevice device);
    void onTaskDataCleanUp();
    void showPasswordDialog(ReaderDevice connectingDevice);
    void showProgressDialog(ReaderDevice connectingDevice);
    void cancelProgressDialog();
    void CancelReconnect();
    void setConnectionProgressState(boolean b);
}

package com.zebra.demo.rfidreader.reader_connection;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PairDataViewModel extends ViewModel {

    private final MutableLiveData<String> removedDevice =
            new MutableLiveData<>();

    public LiveData<String> unpairedDevice() {
        return removedDevice;
    }

    public void setUnpairedDevice(String item) {
        removedDevice.setValue(item);
    }
}



package com.zebra.demo.scanner.helpers;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by qvfr34 on 9/19/2014.
 */
public class Barcode implements Parcelable {
    byte[] barcodeData;
    int barcodeType;
    int fromScannerID;
    private boolean isVisible;

    public Barcode(byte[] barcodeData, int barcodeType, int fromScannerID) {
        this.barcodeData=barcodeData;
        this.barcodeType=barcodeType;
        this.fromScannerID=fromScannerID;
        this.isVisible = false;
    }
    public Barcode(Parcel in) {
        this.barcodeData=in.readString().getBytes();
        this.barcodeType=in.readInt();
        this.fromScannerID=in.readInt();
        this.isVisible = false;
    }

    public byte[] getBarcodeData() {
        return barcodeData;
    }

    public void setBarcodeData(byte[] barcodeData) {
        this.barcodeData = barcodeData;
    }

    public int getFromScannerID() {
        return fromScannerID;
    }

    public void setFromScannerID(int fromScannerID) {
        this.fromScannerID = fromScannerID;
    }

    public int getBarcodeType() {
        return barcodeType;
    }

    public void setBarcodeType(int barcodeType) {
        this.barcodeType = barcodeType;
    }

    /**
     * method which will tell whether tag details are visible
     *
     * @return true if tag details are showing currently or false if tag details are hidden
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * set visibility of the tag details
     *
     * @param visibility true for showing tag details false for hiding tag details
     */
    public void setVisible(boolean visibility) {
        this.isVisible = visibility;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(new String(barcodeData));
        parcel.writeInt(barcodeType);
        parcel.writeInt(fromScannerID);
    }
    public static final Creator<Barcode> CREATOR = new Creator<Barcode>() {

    @Override
    public Barcode createFromParcel(Parcel source) {
        return new Barcode(source);
    }

    @Override
    public Barcode[] newArray(int size) {
        return new Barcode[size];
    }
};
}

package com.zebra.demo.rfidreader.common;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;

/**
 * Custom Dialog to be shown when sending commands to the RFID Reader
 */
public class CustomProgressDialog extends ProgressDialog{
    private static final String MESSAGE = "Saving Settings....";

    /**
     * Constructor to handle the initialization
     *
     * @param context - Context to be used
     */
    public CustomProgressDialog(Context context, String message) {
        super(context);
        //ProgressDialog prgdlg = new ProgressDialog(context);
        setTitle(null);
        setCancelable(false);
        if (message != null)
            setMessage(message);
        else
            setMessage(MESSAGE);

    }
}

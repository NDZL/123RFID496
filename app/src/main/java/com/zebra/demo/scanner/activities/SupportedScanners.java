package com.zebra.demo.scanner.activities;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.scanner.helpers.Foreground;

public class SupportedScanners extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supported_scanners);
        Configuration configuration = getResources().getConfiguration();
        if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE){
            if(configuration.smallestScreenWidthDp<Application.minScreenWidth){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }else{
            if(configuration.screenWidthDp<Application.minScreenWidth){
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isInBackgroundMode(getApplicationContext())) {
            finish();
        }
    }
    public boolean isInBackgroundMode(final Context context) {
        return Foreground.get().isBackground();
    }
}

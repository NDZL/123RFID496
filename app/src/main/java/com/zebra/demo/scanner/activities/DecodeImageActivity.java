package com.zebra.demo.scanner.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.github.chrisbanes.photoview.PhotoView;
import com.zebra.demo.R;
import com.zebra.demo.application.Application;
import com.zebra.demo.scanner.helpers.ScannerAppEngine;

public class DecodeImageActivity extends BaseActivity implements ScannerAppEngine.IScannerAppEngineDevConnectionsDelegate{
    private PhotoView slowestDecodeImageImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode_image);

        Intent intent = getIntent();
        Bitmap bitmap = (Bitmap) intent.getParcelableExtra("slowestDecodeImageImage");

        slowestDecodeImageImageView = (PhotoView) findViewById(R.id.imgViewSlowestDecodeImage);
        slowestDecodeImageImageView.setImageBitmap(ScanSpeedAnalyticsActivity.retrievedSlowestDecodeImage);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle("Slowest Decode Image");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeDevConnectiosDelegate(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        addDevConnectionsDelegate(this);
    }

    public boolean onOptionsItemSelected(MenuItem item){

        int id = item.getItemId();

        if (id==android.R.id.home) {
            finish();
        }

        return true;

    }

    @Override
    public boolean scannerHasAppeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasDisappeared(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasConnected(int scannerID) {
        return false;
    }

    @Override
    public boolean scannerHasDisconnected(int scannerID) {
        barcodeQueue.clear();
        this.finish();
        Application.currentScannerId =Application.SCANNER_ID_NONE;
        Intent intent = new Intent(DecodeImageActivity.this, ScannerHomeActivity.class);
        startActivity(intent);
        return true;
    }
}

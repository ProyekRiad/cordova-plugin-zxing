package com.marceloburegio.zxingplugin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import com.google.zxing.client.android.Intents;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

@SuppressWarnings("deprecation")
public class ScannerActivity extends Activity {
    private final Context context = this;

    private CaptureManager capture = null;
    private int cameraId = -1;
    private boolean isTorchOn = false;

    private CompoundBarcodeView barcodeScannerView;
    private TextView txtPrompt;
    private AppCompatImageView switchFlash;
    private AppCompatImageView switchCam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // get intent config
        cameraId = getIntent().getIntExtra(Intents.Scan.CAMERA_ID, Camera.CameraInfo.CAMERA_FACING_BACK);
        isTorchOn = getIntent().getBooleanExtra(Intents.Scan.TORCH_ENABLED, false);

        // Initiate view
        barcodeScannerView = findViewById(R.id.barcodeScannerView);
        txtPrompt = findViewById(R.id.txtPrompt);
        switchFlash = findViewById(R.id.switchFlash);
        switchCam = findViewById(R.id.switchCam);

        setupView();

        // setup event
        switchFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isTorchOn = !isTorchOn;

                // Set torch on/off
                if (isTorchOn) barcodeScannerView.setTorchOn();
                else barcodeScannerView.setTorchOff();

                // re-setup view
                setupView();
            }
        });

        switchCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (barcodeScannerView.getBarcodeView().isPreviewActive())
                    barcodeScannerView.pause();

                //swap the id of the camera to be used
                cameraId = (cameraId == Camera.CameraInfo.CAMERA_FACING_BACK) ?
                        Camera.CameraInfo.CAMERA_FACING_FRONT
                        : Camera.CameraInfo.CAMERA_FACING_BACK;

                barcodeScannerView.getBarcodeView().getCameraSettings().setRequestedCameraId(cameraId);
                barcodeScannerView.resume();

                setupView();
            }
        });

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }

    private void setupView() {
        // prompt message
        txtPrompt.setText(getIntent().getStringExtra(Intents.Scan.PROMPT_MESSAGE));

        // setup flash switch
        boolean showFlashButton = hasFlash() && cameraId == Camera.CameraInfo.CAMERA_FACING_BACK;
        switchFlash.setVisibility((showFlashButton) ? View.VISIBLE : View.GONE);

        // Set torch icon on/off
        switchFlash.setImageResource((isTorchOn) ? R.drawable.app_torch_on : R.drawable.app_torch_off);

        // find camera count for switch button
        switchCam.setVisibility((Camera.getNumberOfCameras() > 1) ? View.VISIBLE : View.GONE);
    }

    private boolean hasFlash() {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (capture != null) capture.onResume();
    }

    @Override
    protected void onPause() {
        if (capture != null) capture.onPause();

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (capture != null) capture.onDestroy();

        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (capture != null) capture.onSaveInstanceState(outState);
    }
}
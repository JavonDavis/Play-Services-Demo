package com.javon.playservicesdemo.textdetection;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.text.TextRecognizer;
import com.javon.playservicesdemo.R;

public class TextDetectionActivity extends AppCompatActivity {

    private static String LOG_TAG = "TextDetectionActivity";

    CameraSource cameraSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_detection);
    }

    private void createCameraSource() {
        Context context = getApplicationContext();

        // Create the TextRecognizer
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        // Check if the TextRecognizer is operational.
        if (!textRecognizer.isOperational()) {
            Log.w(LOG_TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, "Low Storage", Toast.LENGTH_LONG).show();
                Log.w(LOG_TAG, "Low Storage");
            }
        }

        // Create the camera source
        cameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(15.0f)
                        .build();
    }
}

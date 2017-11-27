package com.javon.playservicesdemo.nearbymessaging;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.javon.playservicesdemo.R;

public class NearbyConnectionsActivity extends AppCompatActivity {

    private Button discoverButton;
    private Button advertiseButton;

    private final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private final String LOG_TAG = "NearbyConnections";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_connections);

        discoverButton = (Button) findViewById(R.id.discover_button);
        advertiseButton = (Button) findViewById(R.id.advertise_button);

        requestPermissions();
    }

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    logAndShowSnackbar("Permission granted");

                } else {
                    logAndShowSnackbar("Permission Denied");
                }
            }
        }
    }


    public void advertise(View view) {
        logAndShowSnackbar("Advertising");
    }

    public void discover(View view) {
        logAndShowSnackbar("Discovering");
    }

    public void sendPayload(View view) {

    }

    /**
     * Logs a message and shows a {@link Snackbar} using {@code text};
     *
     * @param text The text used in the Log message and the SnackBar.
     */
    private void logAndShowSnackbar(final String text) {
        Log.w(LOG_TAG, text);
        View container = findViewById(R.id.activity_main);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }
}

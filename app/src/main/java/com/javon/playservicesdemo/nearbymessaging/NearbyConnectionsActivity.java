package com.javon.playservicesdemo.nearbymessaging;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.nearby.Nearby;
import com.javon.playservicesdemo.R;

public class NearbyConnectionsActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private Button discoverButton;
    private Button advertiseButton;

    private final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private final String LOG_TAG = "NearbyConnections";
    private GoogleApiClient mGoogleApiClient;

    private boolean isDiscovering = false;
    private boolean isAdvertising = false;

    // Step 1: Define Endpoint Variable


    // Step 4: Define PayloadCallback


    // Step 5: Define Connection LifeCycle Callback


    // Step 6: Define Endpoint Discovery Callback


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_connections);

        discoverButton = (Button) findViewById(R.id.discover_button);
        advertiseButton = (Button) findViewById(R.id.advertise_button);

        createGoogleApiClient();
        requestPermissions();

    }


    private void createGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient =
                    new GoogleApiClient.Builder(this)
                            .addApi(Nearby.CONNECTIONS_API)
                            .addConnectionCallbacks(this)
                            .enableAutoManage(this, this)
                            .build();
        }

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        logAndShowSnackbar("Google Play Services Client connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        logAndShowSnackbar("Google Play Services Client suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logAndShowSnackbar("Google Play Services Client failed");
    }

    // Step 2: Define function to get a string to represent the device


    // Step 3: Define function get Service ID. This represents the action this connection is for. When discovering,
    // we'll verify that the advertiser has the same service id before we consider connecting to
    // them.


    // Step 7: Define function start advertising


    // Step 8: Define function to start discovering


    // Step 9: Define Function to stop advertising


    // Step 10: Define function to stop discovering


    public void advertise(View view) {
        // Step 11: Start or stop Advertising
    }

    public void discover(View view) {
        // Step 12: Start or stop Advertising
    }

    public void sendPayload(View view) {
        // Step 13: Send Payload messsage
    }

    public void disconnect(View view) {
        // Step 14: Disconnect from the Endpoint
    }
}

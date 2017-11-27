package com.javon.playservicesdemo.nearbymessaging;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
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
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
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
    private String mEndpointID;

    // Step 4: Define PayloadCallback
    private PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            byte[] bytes = payload.asBytes();
            if(payload.asBytes() != null) {
                String message = new String(bytes);
                logAndShowSnackbar(String.format("Message from Payload: %s", message));
            } else {
                logAndShowSnackbar("Payload was null");
            }
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate payloadTransferUpdate) {
            // Track progress
        }
    };

    // Step 5: Define Connection LifeCycle Callback
    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    logAndShowSnackbar("Connection Initiated");

                    Nearby.Connections.acceptConnection(mGoogleApiClient, endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            logAndShowSnackbar("We're connected! Can now start sending and receiving data.");
                            mEndpointID = endpointId;
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            logAndShowSnackbar("The connection was rejected by one or both sides.");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            logAndShowSnackbar("The connection broke before it was able to be accepted.");
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    logAndShowSnackbar("Connection Disconnected");
                }
            };

    // Step 6: Define Endpoint Discovery Callback
    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(
                        String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    Log.i(LOG_TAG, "Endpoint found");
                    // Might want to show users a list of users so they choose who they'd like to connect to
                    if (getServiceId().equals(discoveredEndpointInfo.getServiceId())) {
                        logAndShowSnackbar(String.format("Endpoint %s was discovered", discoveredEndpointInfo.getEndpointName()));
                        Nearby.Connections.requestConnection(mGoogleApiClient, getUserNickname(), endpointId, mConnectionLifecycleCallback);
                    }

            }

                @Override
                public void onEndpointLost(String endpointId) {
                    logAndShowSnackbar("Previous Endpoint gone");
                }
            };

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
    public String getUserNickname() {
        return Build.MODEL;
    }

    // Step 3: Define function get Service ID. This represents the action this connection is for. When discovering,
    // we'll verify that the advertiser has the same service id before we consider connecting to
    // them.
    public String getServiceId() {
        return "Plurasight";
    }

    // Step 7: Define function start advertising
    private void startAdvertising() {
        Nearby.Connections.startAdvertising(
                mGoogleApiClient,
                getUserNickname(),
                getServiceId(),
                mConnectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_CLUSTER));
    }

    // Step 8: Define function to start discovering
    private void startDiscovery() {
        Nearby.Connections.startDiscovery(
                mGoogleApiClient,
                getUserNickname(),
                mEndpointDiscoveryCallback,
                new DiscoveryOptions(Strategy.P2P_CLUSTER));
    }

    // Step 9: Define Function to stop advertising
    private void stopAdvertising() {
        logAndShowSnackbar("Stop Advertising");
        Nearby.Connections.stopAdvertising(mGoogleApiClient);
    }

    // Step 10: Define function to stop discovering
    private void stopDiscovering() {
        logAndShowSnackbar("Stop Discovering");
        Nearby.Connections.stopDiscovery(mGoogleApiClient);
    }


    public void advertise(View view) {
        logAndShowSnackbar("Advertising");
        // Step 11: Start or stop Advertising
        if(isAdvertising) {
            stopAdvertising();
            advertiseButton.setText("Advertise");
            isAdvertising = false;
        } else {
            startAdvertising();
            advertiseButton.setText("Stop Advertising");
            isAdvertising = true;
        }
    }

    public void discover(View view) {
        logAndShowSnackbar("Discovering");
        // Step 12: Start or stop Advertising
        if(isDiscovering) {
            stopDiscovering();
            discoverButton.setText("Discover");
            isDiscovering = false;
        } else {
            startDiscovery();
            discoverButton.setText("Stop Discovering");
            isDiscovering = true;
        }
    }

    public void sendPayload(View view) {
        // Step 13: Send Payload messsage
        Nearby.Connections.sendPayload(mGoogleApiClient, mEndpointID, Payload.fromBytes(String.format("Hello World! My name is %s", Build.MODEL).getBytes()));
    }

}

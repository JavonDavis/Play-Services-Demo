package com.javon.playservicesdemo.nearbymessaging;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.javon.playservicesdemo.R;

public class NearbyConnectionsActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private Button discoverButton;
    private Button advertiseButton;

    private final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private final String TAG = "NearbyConnections";

    /** We'll talk to Nearby Connections through the GoogleApiClient. */
    private GoogleApiClient mGoogleApiClient;
    private String mEndpointID;

    private PayloadCallback mPayloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            Log.i(TAG, String.format("onPayloadReceived(endpointId=%s, payload=%s)", endpointId, payload));
            byte[] bytes = payload.asBytes();
            if(payload.asBytes() != null) {
                String message = new String(bytes);
                Log.i(TAG, String.format("Message from Payload: %s", message));
            } else {
                Log.d(TAG, "Payload was null");
            }


        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate payloadTransferUpdate) {
            Log.i(TAG, String.format("onPayloadTransferUpdate(endpointId=%s, update=%s)", endpointId, payloadTransferUpdate));
        }
    };

    private final ConnectionLifecycleCallback mConnectionLifecycleCallback =
            new ConnectionLifecycleCallback() {
                @Override
                public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
                    Log.i(TAG, "Connection Initiated");

                    Nearby.Connections.acceptConnection(mGoogleApiClient, endpointId, mPayloadCallback);
                }

                @Override
                public void onConnectionResult(String endpointId, ConnectionResolution result) {
                    Log.i(TAG, "Connection Result");
                    switch (result.getStatus().getStatusCode()) {
                        case ConnectionsStatusCodes.STATUS_OK:
                            Log.i(TAG, "We're connected! Can now start sending and receiving data.");
                            mEndpointID = endpointId;
                            break;
                        case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                            Log.i(TAG, "The connection was rejected by one or both sides.");
                            break;
                        case ConnectionsStatusCodes.STATUS_ERROR:
                            Log.i(TAG, "The connection broke before it was able to be accepted.");
                            break;
                    }
                }

                @Override
                public void onDisconnected(String endpointId) {
                    Log.i(TAG, "Connection Disconnected");
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

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        PERMISSION_REQUEST_COARSE_LOCATION);
        }
    }

    private final EndpointDiscoveryCallback mEndpointDiscoveryCallback =
            new EndpointDiscoveryCallback() {
                @Override
                public void onEndpointFound(
                        String endpointId, DiscoveredEndpointInfo discoveredEndpointInfo) {
                    Log.i(TAG, "Endpoint was discovered");
                    // Might want to show users a list of users so they choose who they'd like to connect to
                    Nearby.Connections.requestConnection(mGoogleApiClient, getUserNickname(), endpointId, mConnectionLifecycleCallback);
                }

                @Override
                public void onEndpointLost(String endpointId) {
                    // A previously discovered endpoint has gone away.
                    Log.i(TAG, "Previous Endpoint gone");
                }
            };

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

    private void startAdvertising() {
        Nearby.Connections.startAdvertising(
                mGoogleApiClient,
                getUserNickname(),
                getUserNickname(),
                mConnectionLifecycleCallback,
                new AdvertisingOptions(Strategy.P2P_CLUSTER));
    }

    private void startDiscovery() {
        Nearby.Connections.startDiscovery(
                mGoogleApiClient,
                getUserNickname(),
                mEndpointDiscoveryCallback,
                new DiscoveryOptions(Strategy.P2P_CLUSTER));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i(TAG, "Permission Granted");

                } else {

                    Log.i(TAG, "Permission Denied");
                }
            }
        }
    }

    public String getUserNickname() {
        return "Mark Zuckerberg";
    }

    public int getServiceId() {
        return 0;
    }

    public void advertise(View view) {
        startAdvertising();
    }

    public void discover(View view) {
        startDiscovery();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "Connected to Google API");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection Failed");
    }

    public void sendPayload(View view) {

        Nearby.Connections.sendPayload(mGoogleApiClient, mEndpointID, Payload.fromBytes(String.format("Hello World! My name is %s", Build.MODEL).getBytes()));
    }
}

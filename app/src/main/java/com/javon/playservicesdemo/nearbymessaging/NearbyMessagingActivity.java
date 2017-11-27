package com.javon.playservicesdemo.nearbymessaging;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.Strategy;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.javon.playservicesdemo.R;

// Step 5: Implement Google API Interfaces
public class NearbyMessagingActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private static String LOG_TAG = "NearbyMessagingActivity";

    private Button publishButton;
    private Button subscribeButton;

    private boolean isSubscribing = false;
    private boolean isPublishing = false;

    // Step 2: Define Global Google API Client
    private GoogleApiClient mGoogleApiClient;

    // Step 7: Define a Strategy. For more about Strategies visit https://developers.google.com/android/reference/com/google/android/gms/nearby/messages/Strategy
    private static final int TTL_IN_SECONDS = 2 * 60; // Two minutes.
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS).build();

    // Step 8: Define Listener for messages
    private MessageListener mMessageListener;

    // Step 16: Define Message to be sent
    private Message publicMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        publishButton = (Button) findViewById(R.id.publish_button);
        subscribeButton = (Button) findViewById(R.id.subscribe_button);

        // Step 4: Build Google API Client
        buildGoogleApiClient();

        // Step 14: Implement Message Listener
        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                logAndShowSnackbar("Got message");
                Toast.makeText(NearbyMessagingActivity.this,
                        NearbyMessage.fromNearbyMessage(message).getBody(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLost(final Message message) {
                logAndShowSnackbar("Lost message");
                Toast.makeText(NearbyMessagingActivity.this,
                        NearbyMessage.fromNearbyMessage(message).getBody(), Toast.LENGTH_LONG).show();
            }
        };

        // Step 17: Set the Message to be sent
        publicMessage = NearbyMessage.newMessage();

        // Step 21: Define listener for subscribe button
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSubscribing) {
                    unsubscribe();
                    subscribeButton.setText("Subscribe");
                } else {
                    subscribe();
                }
            }
        });

        // Step 22: Define listener for publish button
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPublishing) {
                    unpublish();
                    publishButton.setText("Publish");
                } else {
                    publish();
                }
            }
        });

    }

    /**
     * Logs a message and shows a {@link Snackbar} using {@code text};
     *
     * @param text The text used in the Log message and the SnackBar.
     */
    private void logAndShowSnackbar(final String text) {
        Log.i(LOG_TAG, text);
        View container = findViewById(R.id.activity_main);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    // Step 3: Define function to Build Google API Client
    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();
    }

    // Step 6: Implement Google API Interface functions
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        logAndShowSnackbar("Connected to Google API");
    }

    @Override
    public void onConnectionSuspended(int i) {
        logAndShowSnackbar("Connection to Google API Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logAndShowSnackbar("Connection to Google API Failed");
    }

    // Step 15: Create function to Subscribe to Messages
    private void subscribe() {
        logAndShowSnackbar("Subscribing");
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        logAndShowSnackbar("No longer Subscribing");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                subscribeButton.setText("Subscribe");
                                isSubscribing = false;
                            }
                        });
                    }
                }).build();

        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        isSubscribing = status.isSuccess();
                        if (isSubscribing) {
                            logAndShowSnackbar("Subscribed successfully.");
                            subscribeButton.setText("Unsubscribe");
                        } else {
                            logAndShowSnackbar("Could not subscribe, status = " + status);
                            subscribeButton.setText("Subscribe");
                        }
                    }
                });
    }

    // Step 18: Create function to publish messages
    private void publish() {
        logAndShowSnackbar("Publishing");
        PublishOptions options = new PublishOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        logAndShowSnackbar("No longer publishing");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                publishButton.setText("Publish");
                                isPublishing = false;
                            }
                        });
                    }
                }).build();

        Nearby.Messages.publish(mGoogleApiClient, publicMessage, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        isPublishing = status.isSuccess();
                        if (isPublishing) {
                            logAndShowSnackbar("Publishing successfully.");
                            publishButton.setText("Stop publishing");
                        } else {
                            logAndShowSnackbar("Could not publish, status = " + status);
                            publishButton.setText("Publish");
                        }
                    }
                });
    }

    // Step 19: Define function to unsubcribe from messages
    private void unsubscribe() {
        logAndShowSnackbar("Unsubscribing");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
        isSubscribing = false;
        subscribeButton.setText("Subscribe");
    }

    // Step 20: Define function to stop publishing
    private void unpublish() {
        logAndShowSnackbar("Unpublishing");
        Nearby.Messages.unpublish(mGoogleApiClient, publicMessage);
        isPublishing = false;
        publishButton.setText("Publish");
    }
}



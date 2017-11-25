package com.javon.playservicesdemo.nearbymessaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.UUID;

public class NearbyMessagingActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static String LOG_TAG = "NearbyMessagingActivity";

    GoogleApiClient mGoogleApiClient;

    private static final int TTL_IN_SECONDS = 2 * 60; // Two minutes.

    private Message mActiveMessage;

    private MessageListener mMessageListener;

    private Button publishButton;
    private Button subscribeButton;

        // Key used in writing to and reading from SharedPreferences.
        private static final String KEY_UUID = "key_uuid";

    /**
     * {@link Message} object used to broadcast device information to other nearby devices
     */
    private Message publicMessage;

    /**
     * Sets the time in seconds for a published message or a subscription to live. Set to three
     * minutes in this sample.
     */
    private static final Strategy PUB_SUB_STRATEGY = new Strategy.Builder()
            .setTtlSeconds(TTL_IN_SECONDS).build();


    /**
     * Creates a UUID and saves it to {@link SharedPreferences}. The UUID is added to the published
     * message to avoid it being undelivered due to de-duplication. See {@link NearbyMessage} for
     * details.
     */
    private static String getUUID(SharedPreferences sharedPreferences) {
        String uuid = sharedPreferences.getString(KEY_UUID, "");
        if (TextUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
            sharedPreferences.edit().putString(KEY_UUID, uuid).apply();
        }
        return uuid;
    }

    private boolean isSubscribed = false;
    private boolean isPublishing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        publishButton = (Button) findViewById(R.id.publish_button);
        subscribeButton = (Button) findViewById(R.id.subscribe_button);

        // Build the message that is going to be published. This contains the device name and a
        // UUID.
        publicMessage = NearbyMessage.newMessage(getUUID(getSharedPreferences(
                getApplicationContext().getPackageName(), Context.MODE_PRIVATE)));


        mMessageListener = new MessageListener() {
            @Override
            public void onFound(final Message message) {
                // Called when a new message is found.
                Toast.makeText(NearbyMessagingActivity.this,
                        NearbyMessage.fromNearbyMessage(message).getBody(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onLost(final Message message) {
                // Called when a message is no longer detectable nearby.
                Toast.makeText(NearbyMessagingActivity.this,
                        NearbyMessage.fromNearbyMessage(message).getBody(), Toast.LENGTH_LONG).show();
            }
        };

        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPublishing) {
                    unpublish();
                    publishButton.setText("Stop Publishing");
                } else {
                    publish();
                }
                isPublishing = !isPublishing;
            }
        });

        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isSubscribed) {
                    unsubscribe();
                    subscribeButton.setText("Subscribe");
                } else {
                    subscribe();
                }
            isSubscribed = !isSubscribed;
            }
        });

        buildGoogleApiClient();
    }

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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        logAndShowSnackbar("Connection connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        logAndShowSnackbar("Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        logAndShowSnackbar("Connection failed");
    }

    /**
     * Subscribes to messages from nearby devices and updates the UI if the subscription either
     * fails or TTLs.
     */
    private void subscribe() {
        Log.i(LOG_TAG, "Subscribing");
        SubscribeOptions options = new SubscribeOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(LOG_TAG, "No longer subscribing");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                subscribeButton.setText("Subscribe");
                            }
                        });
                    }
                }).build();

        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(LOG_TAG, "Subscribed successfully.");
                            logAndShowSnackbar("Subscribed successfully.");
                            subscribeButton.setText("Unsubscribe");
                        } else {
                            logAndShowSnackbar("Could not subscribe, status = " + status);
                            subscribeButton.setText("Subscribe");
                        }
                    }
                });
    }

    /**
     * Publishes a message to nearby devices and updates the UI if the publication either fails or
     * TTLs.
     */
    private void publish() {
        Log.i(LOG_TAG, "Publishing");
        PublishOptions options = new PublishOptions.Builder()
                .setStrategy(PUB_SUB_STRATEGY)
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                        Log.i(LOG_TAG, "No longer publishing");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                publishButton.setText("Publish");
                            }
                        });
                    }
                }).build();

        Nearby.Messages.publish(mGoogleApiClient, publicMessage, options)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        if (status.isSuccess()) {
                            Log.i(LOG_TAG, "Published successfully.");
                            logAndShowSnackbar("Published successfully.");
                            publishButton.setText("Publish");
                        } else {
                            logAndShowSnackbar("Could not publish, status = " + status);
                            publishButton.setText("Publish");
                        }
                    }
                });
    }

    /**
     * Stops subscribing to messages from nearby devices.
     */
    private void unsubscribe() {
        Log.i(LOG_TAG, "Unsubscribing.");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }

    /**
     * Stops publishing message to nearby devices.
     */
    private void unpublish() {
        Log.i(LOG_TAG, "Unpublishing.");
        Nearby.Messages.unpublish(mGoogleApiClient, publicMessage);
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



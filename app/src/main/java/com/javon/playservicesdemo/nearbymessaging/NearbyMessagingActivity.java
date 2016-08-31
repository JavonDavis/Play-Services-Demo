package com.javon.playservicesdemo.nearbymessaging;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.android.gms.nearby.messages.PublishCallback;
import com.google.android.gms.nearby.messages.PublishOptions;
import com.google.android.gms.nearby.messages.SubscribeCallback;
import com.google.android.gms.nearby.messages.SubscribeOptions;
import com.javon.playservicesdemo.R;

public class NearbyMessagingActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    GoogleApiClient mGoogleApiClient;
    private static String LOG_TAG = "NearbyMessagingActivity";
    private Message mActiveMessage;
    private MessageListener mMessageListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Nearby.MESSAGES_API)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, this)
                .build();

        mMessageListener = new MessageListener() {
            @Override
            public void onFound(Message message) {
                String messageString = new String(message.getContent());
                Log.d(LOG_TAG, "Found message: " + messageString);
            }

            @Override
            public void onLost(Message message) {
                String messageString = new String(message.getContent());
                Log.d(LOG_TAG, "Lost message: " + messageString);
            }
        };
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOG_TAG, "connected");
        publish("Hello World");
        subscribe();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "failed");
    }

    @Override
    protected void onStop() {
        unpublish();
        unsubscribe();
        super.onStop();
    }

    private void publish(String message)
    {
        Log.i(LOG_TAG, "Publishing message: "+ message);
        mActiveMessage = new Message(message.getBytes());

        PublishOptions publishOptions = new PublishOptions.Builder()
                .setCallback(new PublishCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                    }
                })
                .build();
        Nearby.Messages.publish(mGoogleApiClient, mActiveMessage)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {

                    }
                });
    }

    private void unpublish() {
        Log.i(LOG_TAG, "Unpublishing.");
        if(mActiveMessage != null) {
            Nearby.Messages.unpublish(mGoogleApiClient, mActiveMessage);
            mActiveMessage = null;
        }
    }

    private void subscribe() {
        Log.i(LOG_TAG, "Subscribing.");

        SubscribeOptions subscribeOptions = new SubscribeOptions.Builder()
                .setCallback(new SubscribeCallback() {
                    @Override
                    public void onExpired() {
                        super.onExpired();
                    }
                })
                .build();

        Nearby.Messages.subscribe(mGoogleApiClient, mMessageListener)
        .setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {

            }
        });
    }

    private void unsubscribe() {
        Log.i(LOG_TAG, "Unsubscribing.");
        Nearby.Messages.unsubscribe(mGoogleApiClient, mMessageListener);
    }
}


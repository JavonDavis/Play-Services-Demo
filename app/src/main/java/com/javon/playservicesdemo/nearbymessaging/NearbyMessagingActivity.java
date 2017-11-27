package com.javon.playservicesdemo.nearbymessaging;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.javon.playservicesdemo.R;

public class NearbyMessagingActivity extends AppCompatActivity{

    private static String LOG_TAG = "NearbyMessagingActivity";

    private Button publishButton;
    private Button subscribeButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        publishButton = (Button) findViewById(R.id.publish_button);
        subscribeButton = (Button) findViewById(R.id.subscribe_button);

    }


    /**
     * Subscribes to messages from nearby devices and updates the UI if the subscription either
     * fails or TTLs.
     */
    private void subscribe() {
        logAndShowSnackbar("Subscribing");
    }

    /**
     * Publishes a message to nearby devices and updates the UI if the publication either fails or
     * TTLs.
     */
    private void publish() {
        logAndShowSnackbar("Publishing");
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
}



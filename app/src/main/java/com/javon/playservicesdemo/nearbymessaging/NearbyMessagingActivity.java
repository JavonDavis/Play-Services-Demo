package com.javon.playservicesdemo.nearbymessaging;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.javon.playservicesdemo.R;

// Step 5: Implement Google API Interfaces
public class NearbyMessagingActivity extends AppCompatActivity {

    private static String LOG_TAG = "NearbyMessagingActivity";

    private Button publishButton;
    private Button subscribeButton;

    private boolean isSubscribing = false;
    private boolean isPublishing = false;

    // Step 2: Define Global Google API Client


    // Step 7: Define a Strategy. For more about Strategies visit https://developers.google.com/android/reference/com/google/android/gms/nearby/messages/Strategy


    // Step 8: Define Listener for messages


    // Step 16: Define Message to be sent


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby);

        publishButton = (Button) findViewById(R.id.publish_button);
        subscribeButton = (Button) findViewById(R.id.subscribe_button);

        // Step 4: Build Google API Client


        // Step 14: Implement Message Listener


        // Step 17: Set the Message to be sent


        // Step 21: Define listener for subscribe button


        // Step 22: Define listener for publish button


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


    // Step 6: Implement Google API Interface functions


    // Step 15: Create function to Subscribe to Messages


    // Step 18: Create function to publish messages

    // Step 19: Define function to unsubscribe from messages

    // Step 20: Define function to stop publishing
}

package com.javon.playservicesdemo.nearbymessaging;

import android.os.Build;

import com.google.android.gms.nearby.messages.Message;
import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * Created by Javon Davis on 11/26/17.
 */

public class NearbyMessage {

    private static final Gson gson = new Gson();

    // Step 9: Define body of message
    private final String mBody;

    // Step 10: Define constructor to set the body to some text
    private NearbyMessage() {
        mBody = Build.MODEL;
    }

    // Step 11: Create Getter for body
    protected String getBody() {
        return mBody;
    }

    // Step 12: Create function to parse a Message and return a NearbyMessage
    public static NearbyMessage fromNearbyMessage(Message message) {
        String messageString = new String(message.getContent()).trim();
        return gson.fromJson(new String(messageString.getBytes(Charset.forName("UTF-8"))), NearbyMessage.class);
    }

    // Step 13: Create a function to return a Message from our NearbyMessage
    public static Message newMessage() {
        NearbyMessage message = new NearbyMessage();
        return new Message(gson.toJson(message).getBytes(Charset.forName("UTF-8")));
    }
}

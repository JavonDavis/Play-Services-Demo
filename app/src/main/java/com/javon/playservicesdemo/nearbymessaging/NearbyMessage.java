package com.javon.playservicesdemo.nearbymessaging;

import android.os.Build;

import com.google.android.gms.nearby.bootstrap.Device;
import com.google.android.gms.nearby.messages.Message;
import com.google.android.gms.nearby.messages.MessageListener;
import com.google.gson.Gson;

import java.nio.charset.Charset;

/**
 * @author Javon Davis
 *         Created by Javon Davis on 06/09/2016.
 *
 * Used to prepare a Nearby Message payload by attaching a unique ID. This
 * will help nearby distinguish between multiple devices with the same model name.
 */
public class NearbyMessage {

    private static final Gson gson = new Gson();

    private final String mUUID;
    private final String mBody;

    /**
     * Builds a new message from the @param id
     * @param id
     * @return Message object
     */
    public static Message newMessage(String id) {
        NearbyMessage message = new NearbyMessage(id);
        return new Message(gson.toJson(message).getBytes(Charset.forName("UTF-8")));
    }

    public static NearbyMessage fromNearbyMessage(Message message) {

        String messageString = new String(message.getContent()).trim();
        return gson.fromJson(new String(messageString.getBytes(Charset.forName("UTF-8"))),
                NearbyMessage.class);

    }

    private NearbyMessage(String uuid) {
        mUUID = uuid;
        mBody = Build.MODEL;
    }

    protected String getBody() {
        return mBody;
    }
}

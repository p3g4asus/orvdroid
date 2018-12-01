package com.orvdroid.lib.message;

import android.content.Intent;

/**
 * Created by Fujitsu on 27/01/2016.
 */
public class PingMessage extends RemoteMessage {
    @Override
    public Intent response2Message() {
        return null;
    }
}

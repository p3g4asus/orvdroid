package com.orvdroid.lib.message;

/**
 * Created by Fujitsu on 01/02/2016.
 */
public class StatechangeMessage extends RemoteMessage {
    public StatechangeMessage() {

    }
    public StatechangeMessage(Device d, int tz) {
        super(d);
        putParam(""+tz);
    }
}

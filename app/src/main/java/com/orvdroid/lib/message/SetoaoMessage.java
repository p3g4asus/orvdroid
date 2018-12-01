package com.orvdroid.lib.message;

/**
 * Created by Fujitsu on 01/02/2016.
 */
public class SetoaoMessage extends RemoteMessage {
    public SetoaoMessage() {

    }
    public SetoaoMessage(Device d, int tz) {
        super(d);
        putParam(""+tz);
    }
}

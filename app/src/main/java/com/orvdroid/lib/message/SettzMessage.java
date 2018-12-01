package com.orvdroid.lib.message;

/**
 * Created by Fujitsu on 01/02/2016.
 */
public class SettzMessage extends RemoteMessage {
    public SettzMessage() {

    }
    public SettzMessage(Device d, int tz) {
        super(d);
        putParam(""+tz);
    }
}

package com.orvdroid.lib.message;

/**
 * Created by Matteo on 24/01/2016.
 */
public class CleartimersMessage extends RemoteMessage {
    public CleartimersMessage() {

    }
    public CleartimersMessage(Device d, DeviceTimer dt) {
        super(d);
        if (dt!=null)
            putParam(""+dt.getCo());
    }
    public void addTimer(DeviceTimer dt) {
        putParam(""+dt.getCo());
    }
}

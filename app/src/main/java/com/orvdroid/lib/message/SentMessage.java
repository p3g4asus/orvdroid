package com.orvdroid.lib.message;

import com.orvdroid.lib.utils.ParcelableMessage;
import com.orvdroid.workers.ConnectionService;

public class SentMessage implements IBaseMessage {
    private RemoteMessage msg;
    private int my;
    public SentMessage(RemoteMessage m) {
        msg = m;
        my = m.getRandomId();
    }

    public boolean isMessage(RemoteMessage m) {
        return my==m.getRandomId();
    }
}
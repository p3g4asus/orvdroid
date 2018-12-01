package com.orvdroid.lib.message;

import android.content.Intent;

import com.orvdroid.lib.utils.ParcelableMessage;
import com.orvdroid.workers.ConnectionService;

/**
 * Created by Fujitsu on 28/01/2016.
 */
public class ConnectionStatusMessage implements IBaseMessage {
    private Intent intent;
    public ConnectionStatusMessage(Intent m) {
        intent = m;
    }
    public ParcelableMessage getMessage() {
        return intent.getParcelableExtra("except0");
    }

    public String getMessageId() {
        ParcelableMessage pm = intent.getParcelableExtra("except0");
        return pm!=null?pm.getId():"";
    }

    public boolean isConnectionError() {
        return getMessageId().equals(ConnectionService.CONN_CONNECTIONERROR_ID);
    }

    public boolean isConnectionLost() {
        return getMessageId().equals(ConnectionService.CONN_CONNECTIONLOST_ID);
    }

    public boolean isError() {
        return !getMessageId().isEmpty();
    }
}

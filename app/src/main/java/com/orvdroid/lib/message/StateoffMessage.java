package com.orvdroid.lib.message;

import android.content.Intent;
import android.os.Parcelable;

import com.orvdroid.lib.utils.ParcelableMessage;

/**
 * Created by Matteo on 23/01/2016.
 */
public class StateoffMessage extends RemoteMessage {
    public StateoffMessage() {

    }

    public StateoffMessage(Device d) {
        super(d);
    }

    @Override
    public Intent response2Message() {
        Intent rvi = super.response2Message();
        if (rvi != null) {
            ParcelableMessage rv = rvi.getParcelableExtra("except0");
            int exitv = rv.getInt(MSG_IDX_RV);
            if (exitv==1) {
                DeviceS20 d = (DeviceS20) rv.getParcelable(MSG_IDX_DEVICE);
                rvi.putExtra("except1", (Parcelable) new ParcelableMessage(getMessageId())
                        .put(d.getName())
                        .put(d.getState()==1?"ON":"OFF"));
            }
        }
        return rvi;
    }
}

package com.orvdroid.lib.message;

import android.content.Intent;
import android.os.Parcelable;

import com.orvdroid.lib.utils.ParcelableMessage;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by Matteo on 31/01/2016.
 */
public class IraskMessage extends RemoteMessage {
    public IraskMessage() {

    }

    @Override
    public Intent response2Message() {
        Intent rvi = super.response2Message();
        if (rvi!=null) {
            try {
                ParcelableMessage rv = rvi.getParcelableExtra("except0");
                int exitv = rv.getInt(MSG_IDX_RV);
                if (exitv==1) {
                    String msgid = getMessageId();
                    ParcelableMessage pm;
                    JSONObject obj = commandResponse.getJSONObject("action");
                    String irname = obj.getString("irname");
                    String dname = rv.getString(MSG_IDX_DEVICE_NAME);
                    pm = new ParcelableMessage(msgid);
                    pm.put(dname);
                    pm.put(irname);
                    rvi.putExtra("except1", (Parcelable) pm);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rvi;
    }
}

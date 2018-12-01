package com.orvdroid.lib.message;

import android.content.Intent;
import android.os.Parcelable;

import com.orvdroid.lib.utils.ParcelableMessage;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Matteo on 25/01/2016.
 */
public class EmitirMessage extends RemoteMessage {
    public EmitirMessage(Device d,DeviceAllOneKey k) {
        super(d);
        putParam(k.toString());
    }

    public EmitirMessage(Device d,List<DeviceAllOneKey> ks) {
        super(d);
        for (DeviceAllOneKey k:ks)
            putParam(k.toString());
    }

    public EmitirMessage() {

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
                    JSONArray irnames = obj.getJSONArray("irname"),ircs = obj.getJSONArray("irc");
                    String irname,irc,dname = rv.getString(MSG_IDX_DEVICE_NAME);
                    for (int i = 0,j = 0; i<irnames.length(); i++) {
                        irname = irnames.getString(i);
                        if (irname.charAt(0)!='$') {
                            pm = new ParcelableMessage(msgid);
                            irc = ircs.getString(j);
                            pm.put(dname);
                            pm.put(irname);
                            pm.put(irc);
                            pm.put(irc.length()/2);
                            j++;
                            rvi.putExtra("except"+j, (Parcelable) pm);
                        }
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rvi;
    }
}
package com.orvdroid.lib.message;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Fujitsu on 20/01/2016.
 */
public class InfoMessage extends RemoteMessage {
    private ArrayList<Device> deviceList = null;
    protected String secPath;
    public InfoMessage(String p2) {
        secPath = p2;
    }
    public ArrayList<Device> getDeviceList() {
        if (deviceList!=null)
            return deviceList;
        else {
            JSONObject deviceObj = getResponseO("action", secPath);
            if (deviceObj != null) {
                deviceList = new ArrayList<>();
                Iterator<?> keys = deviceObj.keys();
                Object oo;
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    try {
                        if ((oo = deviceObj.get(key)) instanceof JSONObject) {
                            Device d = Device.parse((JSONObject) oo);
                            if (d != null)
                                deviceList.add(d);
                        }
                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
                return deviceList;
            } else
                return null;
        }
    }
}

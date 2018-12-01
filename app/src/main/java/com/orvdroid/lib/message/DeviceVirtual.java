package com.orvdroid.lib.message;

import android.os.Parcel;

import com.orvdroid.gui.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class DeviceVirtual extends DeviceState {
    @Override
    protected Device parsePossibleStates(JSONObject obj) {
        try {
            JSONObject mp = obj.getJSONObject("nicks");
            Iterator<String> keys = mp.keys();
            possibleStates.clear();


            while(keys.hasNext()) {
                String key = keys.next();
                possibleStates.putInt(mp.getString(key),Integer.parseInt(key));
            }
            return this;
        }
        catch (JSONException je) {
            return null;
        }
    }

    public DeviceVirtual(Parcel p) {
        super(p);
    }

    public DeviceVirtual() {

    }

    @Override
    public int getTypeResource() {
        return R.string.devicevirtual;
    }

    @Override
    public int getDrawerIconResource() {
        return R.drawable.ic_action_virtual_orange;
    }

    @Override
    public int getTabIconResource() {
        return R.drawable.ic_action_virtual;
    }

    public static final Creator<DeviceVirtual> CREATOR = new Creator<DeviceVirtual>() {
        @Override
        public DeviceVirtual createFromParcel(Parcel parcel) {
            return new DeviceVirtual(parcel);
        }

        @Override
        public DeviceVirtual[] newArray(int i) {
            return new DeviceVirtual[i];
        }
    };
}

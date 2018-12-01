package com.orvdroid.lib.message;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;

import com.orvdroid.gui.R;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Fujitsu on 20/01/2016.
 */
public class DeviceS20 extends DeviceUDP implements StateDevice {
    @Override
    public int getState() {
        return state;
    }

    @Override
    public Bundle getPossibleStates() {
        return possibleStates;
    }
    protected int state = 0;


    protected Bundle possibleStates = StateDevice.defaultPossibleStates();


    public DeviceS20() {

    }

    public DeviceS20(Parcel p) {
        super(p);
        state = p.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeInt(state);
    }

    @Override
    protected Device parseFromObj(JSONObject obj) {
        try {
            state = obj.getInt("state");
            return this;
        }
        catch (JSONException je) {
            je.printStackTrace();
            return null;
        }
    }

    @Override
    public int getTypeResource() {
        return R.string.s20;
    }

    @Override
    public int getDrawerIconResource() {
        return R.drawable.ic_action_s20_orange;
    }

    @Override
    public int getTabIconResource() {
        return R.drawable.ic_action_s20;
    }

    @Override
    public int getStatusColor() {
        return state > 0 && !isOffline()? Color.parseColor("#8DC24D") : Color.parseColor("#F24236");
    }

    public static final Creator<DeviceS20> CREATOR = new Creator<DeviceS20>() {
        @Override
        public DeviceS20 createFromParcel(Parcel parcel) {
            return new DeviceS20(parcel);
        }

        @Override
        public DeviceS20[] newArray(int i) {
            return new DeviceS20[i];
        }
    };
}

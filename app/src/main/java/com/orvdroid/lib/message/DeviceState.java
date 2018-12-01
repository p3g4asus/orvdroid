package com.orvdroid.lib.message;

import android.os.Bundle;
import android.os.Parcel;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;

public abstract class DeviceState extends Device implements StateDevice {
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

    public DeviceState() {

    }

    public DeviceState(Parcel p) {
        super(p);
        state = p.readInt();
        possibleStates = p.readBundle();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeInt(state);
        dest.writeBundle(possibleStates);
    }

    @Override
    protected Device parseFromObj(JSONObject obj) {
        try {
            try {
                state = obj.getInt("state");
            } catch (JSONException e) {
                obj.getString("state");
                state = -1;
            }
            return parsePossibleStates(obj);
        }
        catch (JSONException je) {
            je.printStackTrace();
            return null;
        }
    }

    protected abstract Device parsePossibleStates(JSONObject obj);

    public static final Creator<DeviceState> CREATOR = new Creator<DeviceState>() {
        @Override
        public DeviceState createFromParcel(Parcel parcel) {
            String cls = Device.getClassName(parcel);
            try {
                Constructor c = Class.forName(cls).getConstructor(Parcel.class);
                return (DeviceState) c.newInstance(parcel);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public DeviceState[] newArray(int i) {
            return new DeviceState[i];
        }
    };
}

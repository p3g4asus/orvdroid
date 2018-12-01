package com.orvdroid.lib.message;

import android.graphics.Color;
import android.os.Parcel;

import com.orvdroid.gui.R;

import org.json.JSONException;
import org.json.JSONObject;

public class DevicePrimelan extends DeviceState {
    public int getSubtype() {
        return subtype;
    }
    protected int subtype = 0;
    public DevicePrimelan(Parcel p) {
        super(p);
        subtype = p.readInt();
    }

    public DevicePrimelan() {

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest,flags);
        dest.writeInt(subtype);
    }

    @Override
    protected Device parseFromObj(JSONObject obj) {
        Device d = super.parseFromObj(obj);
        if (d!=null) {
            try {
                subtype = obj.getInt("subtype");
                return this;
            } catch (JSONException je) {
                je.printStackTrace();
                return null;
            }
        }
        else
            return d;
    }


    @Override
    protected Device parsePossibleStates(JSONObject obj) {

        return this;
    }

    @Override
    public int getStatusColor() {
        if (subtype==2)
            return state>0 && !isOffline()? Color.parseColor("#8DC24D"):Color.parseColor("#F24236");
        else
            return super.getStatusColor();
    }

    @Override
    public int getTypeResource() {
        return R.string.primelan;
    }

    @Override
    public int getDrawerIconResource() {
        return R.drawable.ic_action_primelan_orange;
    }

    @Override
    public int getTabIconResource() {
        return R.drawable.ic_action_primelan;
    }

    public static final Creator<DevicePrimelan> CREATOR = new Creator<DevicePrimelan>() {
        @Override
        public DevicePrimelan createFromParcel(Parcel parcel) {
            return new DevicePrimelan(parcel);
        }

        @Override
        public DevicePrimelan[] newArray(int i) {
            return new DevicePrimelan[i];
        }
    };
}

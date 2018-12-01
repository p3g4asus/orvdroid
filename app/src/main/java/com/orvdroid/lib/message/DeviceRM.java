package com.orvdroid.lib.message;

import android.os.Parcel;
import android.os.Parcelable;

import com.orvdroid.gui.R;

/**
 * Created by Matteo on 29/01/2017.
 */

public class DeviceRM extends DeviceAllOne implements Parcelable{
    public DeviceRM(Parcel p) {
        super(p);
    }
    public DeviceRM() {
        super();
    }

    @Override
    protected boolean isUDP() {
        return false;
    }

    public static final Creator<DeviceRM> CREATOR = new Creator<DeviceRM>() {
        @Override
        public DeviceRM createFromParcel(Parcel parcel) {
            return new DeviceRM(parcel);
        }

        @Override
        public DeviceRM[] newArray(int i) {
            return new DeviceRM[i];
        }
    };

    @Override
    public int getTypeResource() {
        return R.string.blackbean;
    }

    @Override
    public int getDrawerIconResource() {
        return R.drawable.ic_action_blackbean_orange;
    }

    @Override
    public int getTabIconResource() {
        return R.drawable.ic_action_blackbean;
    }
}
package com.orvdroid.lib.message;

import android.os.Parcel;
import android.os.Parcelable;

import com.orvdroid.gui.R;

/**
 * Created by Matteo on 29/01/2017.
 */

public class DeviceCT10 extends DeviceAllOne implements Parcelable{
    public DeviceCT10(Parcel p) {
        super(p);
    }
    public DeviceCT10() {
        super();
    }

    @Override
    protected boolean isUDP() {
        return false;
    }

    public static final Creator<DeviceCT10> CREATOR = new Creator<DeviceCT10>() {
        @Override
        public DeviceCT10 createFromParcel(Parcel parcel) {
            return new DeviceCT10(parcel);
        }

        @Override
        public DeviceCT10[] newArray(int i) {
            return new DeviceCT10[i];
        }
    };

    @Override
    public int getTypeResource() {
        return R.string.ct10;
    }

    @Override
    public int getDrawerIconResource() {
        return R.drawable.ic_action_ct10_orange;
    }

    @Override
    public int getTabIconResource() {
        return R.drawable.ic_action_ct10;
    }
}

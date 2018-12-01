package com.orvdroid.lib.message;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Fujitsu on 20/01/2016.
 */
public class DeviceAllOneKey implements Parcelable {
    private String device = "",key = "";
    private int irsize = 0;

    public String getDevice() {
        return device;
    }

    public String getKey() {
        return key;
    }

    public int getIrsize() {
        return irsize;
    }

    /*public String getIr() {
        return ir;
    }*/

    public boolean isShortcut() {
        return device.charAt(0)=='@';
    }

    public boolean isDelay() {
        return device.charAt(0)=='$';
    }

    public DeviceAllOneKey(String d, String k) {
        device = d;
        key = k;
    }

    public DeviceAllOneKey(String d, String k, String i) {
        this(d,k);
        irsize = i.length();
    }

    public DeviceAllOneKey(Parcel p) {
        device = p.readString();
        key = p.readString();
        irsize = p.readInt();
    }

    @Override
    public boolean equals(Object o) {
        if (o==null || !(o instanceof DeviceAllOneKey))
            return false;
        else
            return toString().equals(o.toString());
    }

    @Override
    public String toString() {
        return isShortcut() || isDelay()?device:device+":"+key;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(device);
        dest.writeString(key);
        dest.writeInt(irsize);
    }

    public static final Creator<DeviceAllOneKey> CREATOR = new Creator<DeviceAllOneKey>() {
        @Override
        public DeviceAllOneKey createFromParcel(Parcel parcel) {
            return new DeviceAllOneKey(parcel);
        }

        @Override
        public DeviceAllOneKey[] newArray(int i) {
            return new DeviceAllOneKey[i];
        }
    };

    public DeviceAllOneKey(DeviceAllOneKey k) {
        set(k);
    }

    public void set(DeviceAllOneKey fromDk) {
        device = fromDk.device;
        key = fromDk.key;
        irsize = fromDk.irsize;
    }
}

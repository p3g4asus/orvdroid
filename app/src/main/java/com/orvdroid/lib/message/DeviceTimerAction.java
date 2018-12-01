package com.orvdroid.lib.message;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Fujitsu on 27/01/2016.
 */
public class DeviceTimerAction implements Parcelable {
    public static int ACTION_NONE = 0;
    public static int ACTION_ADD = 2;
    public static int ACTION_EDIT = 1;
    public static int ACTION_REMOVE = 3;
    private int action = ACTION_NONE;
    private DeviceTimer dt = null;
    private DeviceTimer dtO = null;

    public DeviceTimerAction(Parcel p) {
        action = p.readInt();
        dt = p.readParcelable(DeviceTimer.class.getClassLoader());
        dtO = p.readParcelable(DeviceTimer.class.getClassLoader());
    }

    public void setDtO(DeviceTimer dtO) {
        this.dtO = dtO;
    }

    public DeviceTimer getDtO() {
        return dtO;
    }

    public int getAction() {
        return action;
    }

    public DeviceTimer getDt() {
        return dt;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public void setDt(DeviceTimer dt) {
        this.dt = dt;
    }

    public DeviceTimerAction() {

    }
    public DeviceTimerAction(DeviceTimer t) {
        this(ACTION_NONE,t);
    }
    public DeviceTimerAction(int act, DeviceTimer t) {
        if (t!=null && act!=ACTION_ADD) {
            dt = new DeviceTimer(t);
            dtO = t;
        }
        else
            dt = t;
        action = act;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(action);
        dest.writeParcelable(dt,flags);
        dest.writeParcelable(dtO,flags);
    }

    public static final Creator<DeviceTimerAction> CREATOR = new Creator<DeviceTimerAction>() {
        @Override
        public DeviceTimerAction createFromParcel(Parcel parcel) {
            return new DeviceTimerAction(parcel);
        }

        @Override
        public DeviceTimerAction[] newArray(int i) {
            return new DeviceTimerAction[i];
        }
    };
}
package com.orvdroid.lib.message;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;

/**
 * Created by Matteo on 29/01/2017.
 */

public abstract class DeviceUDP extends Device implements Parcelable {
    protected int rTime;
    protected long dateDiff;
    protected long sec1900;
    protected long sst;
    protected int timer_off_after_on;
    protected int timezone;
    public int getRTime() {
        return rTime;
    }

    public long getSec1900() {
        return sec1900;
    }

    public long getSst() {
        return sst;
    }

    public long getDateDiff() {
        return dateDiff;
    }

    public int getTimer_off_after_on() {
        return timer_off_after_on;
    }

    public int getTimezone() {
        return timezone;
    }

    public DeviceUDP(Parcel p) {
        super(p);
        rTime = p.readInt();
        sst = p.readLong();
        sec1900 = p.readLong();
        dateDiff = p.readLong();
        timezone = p.readInt();
        timer_off_after_on = p.readInt();
    }

    protected DeviceUDP() {
        super();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(rTime);
        dest.writeLong(sst);
        dest.writeLong(sec1900);
        dest.writeLong(dateDiff);
        dest.writeInt(timezone);
        dest.writeInt(timer_off_after_on);
    }

    protected boolean isUDP() {
        return true;
    }

    protected Device parse(JSONObject obj, Device d) {
        try {
            if ((d = super.parse(obj, d))!=null) {
                if (isUDP()) {
                    DeviceUDP d2 = (DeviceUDP) d;
                    d2.rTime = obj.getInt("rtime");
                    d2.sst = obj.getLong("sst");
                    d2.sec1900 = obj.getLong("sec1900");
                    d2.dateDiff = System.currentTimeMillis() - obj.getLong("mytime");
                    d2.dateDiff += d2.sec1900;
                    d2.timer_off_after_on = obj.getInt("timer_off_after_on");
                    d2.timezone = obj.getInt("timezone");
                    return d2;
                }
                else
                    return d;
            }
            else
                return null;
        }
        catch (JSONException je) {
            je.printStackTrace();
            return null;
        }
    }

    public static final Creator<DeviceUDP> CREATOR = new Creator<DeviceUDP>() {
        @Override
        public DeviceUDP createFromParcel(Parcel parcel) {
            String cln = DeviceUDP.getClassName(parcel);
            try {
                Class<? extends DeviceUDP> c = (Class<? extends DeviceUDP>) Class.forName(cln);
                Constructor co = c.getConstructor(Parcel.class);
                return (DeviceUDP) co.newInstance(parcel);
            }
            catch (Exception e) {
            }
            return null;
        }

        @Override
        public DeviceUDP[] newArray(int i) {
            return new DeviceUDP[i];
        }
    };

}

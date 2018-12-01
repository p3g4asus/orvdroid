package com.orvdroid.lib.message;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;


/**
 * Created by Fujitsu on 20/01/2016.
 */
public abstract class Device implements Parcelable {

    protected String mac;
    protected String host;
    protected String name;
    protected ArrayList<DeviceTimer> timers = new ArrayList<DeviceTimer>();
    protected int offt;
    protected int offlimit;

    public String getMac() {
        return mac;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }


    public boolean isOffline() {
        return System.currentTimeMillis()/1000-offt<=offlimit;
    }

    public ArrayList<DeviceTimer> getTimers() {
        return timers;
    }

    @Override
    public boolean equals(Object o) {
        if (o==null || !(o instanceof Device))
            return false;
        else {
            Device d = (Device) o;
            return d.mac.equals(mac);
        }
    }

    protected Device() {

    }

    protected static String getClassName(Parcel p) {
        return p.readString();
    }

    public Device(Parcel p) {
        String tmp = p.readString();
        if (!tmp.equals("@"))
            p.readString();
        mac = p.readString();
        name = p.readString();
        host = p.readString();
        offt = p.readInt();
        offlimit = p.readInt();
        timers = p.readArrayList(DeviceTimer.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getClass().getName());
        dest.writeString("@");
        dest.writeString(mac);
        dest.writeString(name);
        dest.writeString(host);
        dest.writeInt(offt);
        dest.writeInt(offlimit);
        dest.writeList(timers);
    }

    @Override
    public String toString() {
        return name+" ("+getClass().getSimpleName()+":"+mac+")";
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    protected Device parse(JSONObject obj, Device d) {
        try {
            d.mac = obj.getString("mac");
            d.name = obj.getString("name");
            d.host = obj.getString("host");
            d.offt = obj.getInt("offt");
            d.offlimit = obj.getInt("offlimit");
            JSONArray arr = obj.getJSONArray("timers");
            for (int i = 0; i<arr.length(); i++) {
                DeviceTimer dt = DeviceTimer.parse(arr.getJSONObject(i));
                if (dt!=null)
                    timers.add(dt);
            }
            return d;
        }
        catch (JSONException je) {
            je.printStackTrace();
            return null;
        }

    }

    protected abstract Device parseFromObj(JSONObject obj);

    public static Device parse(JSONObject obj) {
        try {
            String cls = obj.getString("type");
            Class<Device> clso = (Class<Device>) Class.forName("com.orvdroid.lib.message."+cls);
            Device d = clso.newInstance();
            if (d.parse(obj, d)==null || d.parseFromObj(obj)==null)
                return null;
            else
                return d;
        }
        catch (Exception je) {
            je.printStackTrace();
            return null;
        }
    }

    public static final Creator<Device> CREATOR = new Creator<Device>() {
        @Override
        public Device createFromParcel(Parcel parcel) {
            String cln = Device.getClassName(parcel);
            try {
                Class<? extends Device> c = (Class<? extends Device>) Class.forName(cln);
                Constructor co = c.getConstructor(Parcel.class);
                return (Device) co.newInstance(parcel);
            }
            catch (Exception e) {
            }
            return null;
        }

        @Override
        public Device[] newArray(int i) {
            return new Device[i];
        }
    };

    public abstract int getTypeResource();

    public abstract int getDrawerIconResource();

    public abstract int getTabIconResource();

    public int getStatusColor() {
        return -1;
    }
}

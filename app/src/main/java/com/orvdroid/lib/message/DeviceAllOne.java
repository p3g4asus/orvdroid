package com.orvdroid.lib.message;

import android.os.Parcel;

import com.orvdroid.gui.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

/**
 * Created by Fujitsu on 20/01/2016.
 */
public class DeviceAllOne extends DeviceUDP {
    protected HashMap<String,ArrayList<DeviceAllOneKey>> d433 = new LinkedHashMap<String,ArrayList<DeviceAllOneKey>>();
    protected HashMap<String,ArrayList<DeviceAllOneKey>> dir = new LinkedHashMap<String,ArrayList<DeviceAllOneKey>>();
    protected HashMap<String,ArrayList<DeviceAllOneKey>> sh = new LinkedHashMap<String,ArrayList<DeviceAllOneKey>>();

    public HashMap<String, ArrayList<DeviceAllOneKey>> getDir() {
        return dir;
    }

    public HashMap<String, ArrayList<DeviceAllOneKey>> getSh() {
        return sh;
    }

    public HashMap<String, ArrayList<DeviceAllOneKey>> getD433() {
        return d433;
    }

    public DeviceAllOne() {

    }

    private void readMap(HashMap<String,ArrayList<DeviceAllOneKey>> d433,Parcel p) {
        int n = p.readInt();
        for (int i = 0; i<n; i++) {
            d433.put(p.readString(),p.readArrayList(DeviceAllOneKey.class.getClassLoader()));
        }
    }

    public DeviceAllOne(Parcel p) {
        super(p);
        readMap(d433,p);
        readMap(dir,p);
        readMap(sh,p);
    }

    private void writeMap(HashMap<String,ArrayList<DeviceAllOneKey>> d433,Parcel dest) {
        dest.writeInt(d433.size());
        Set<String> keys = d433.keySet();
        for (String k:keys){
            dest.writeString(k);
            dest.writeList(d433.get(k));
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        writeMap(d433, dest);
        writeMap(dir, dest);
        writeMap(sh,dest);
    }

    private void parseDir(JSONObject obj,String key,HashMap<String,ArrayList<DeviceAllOneKey>> m) {
        try {
            JSONArray l = obj.getJSONArray(key);
            ArrayList<DeviceAllOneKey> tmpl;
            String[] tmp;
            for (int i = 0; i<l.length(); i++) {
                tmp = l.getString(i).split(":");
                tmpl = m.get(tmp[0]);
                if (tmpl==null)
                    tmpl = new ArrayList<DeviceAllOneKey>();
                tmpl.add(new DeviceAllOneKey(tmp[0],tmp[1],tmp.length>2?tmp[2]:""));
                m.put(tmp[0],tmpl);
            }

        }
        catch (JSONException je) {
            je.printStackTrace();
        }
    }

    @Override
    protected Device parseFromObj(JSONObject obj) {
        try {
            parseDir(obj,"d433",d433);
            parseDir(obj,"dir",dir);
            parseDir(obj,"sh",sh);
            return this;
        }
        catch (Exception je) {
            je.printStackTrace();
            return null;
        }

    }

    @Override
    public int getTypeResource() {
        return R.string.allone;
    }

    @Override
    public int getDrawerIconResource() {
        return R.drawable.ic_action_allone_orange;
    }

    @Override
    public int getTabIconResource() {
        return R.drawable.ic_action_allone;
    }

    public static final Creator<DeviceAllOne> CREATOR = new Creator<DeviceAllOne>() {
        @Override
        public DeviceAllOne createFromParcel(Parcel parcel) {
            return new DeviceAllOne(parcel);
        }

        @Override
        public DeviceAllOne[] newArray(int i) {
            return new DeviceAllOne[i];
        }
    };
}

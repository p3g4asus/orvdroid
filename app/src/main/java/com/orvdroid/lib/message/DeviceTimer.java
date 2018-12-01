package com.orvdroid.lib.message;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

/**
 * Created by Fujitsu on 20/01/2016.
 */
public class DeviceTimer implements Parcelable {
    private int ho,mi,se,da,mo,ye,re,co;
    private String action = "";
    private GregorianCalendar calendar = null;

    public DeviceTimer(DeviceTimer d) {
        set(d);
    }

    public void set(DeviceTimer d) {
        ho = d.ho;
        mi = d.mi;
        se = d.se;
        da = d.da;
        mo = d.mo;
        ye = d.ye;
        re = d.re;
        co = d.co;
        action = d.action;
        adjustCalendar(this);
    }

    private static void adjustCalendar(DeviceTimer d) {
        d.calendar = new GregorianCalendar(d.ye,d.mo-1,d.da,d.ho,d.mi,d.se);
        d.adjustTimer();
    }

    @Override
    public int describeContents() {
        return 0;
    }


    public DeviceTimer(Parcel p) {
        ho = p.readInt();
        mi = p.readInt();
        se = p.readInt();
        da = p.readInt();
        mo = p.readInt();
        ye = p.readInt();
        re = p.readInt();
        co = p.readInt();
        action = p.readString();
        adjustCalendar(this);
    }

    public String getDateTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
        return sdf.format(calendar.getTime());
    }

    public String getRepeatString() {
        String rv = "";
        if ((re&255)>128) {
            GregorianCalendar cal = new GregorianCalendar(2016,0,11);
            SimpleDateFormat sdf = new SimpleDateFormat("EE");
            for (int i = 0; i<7; i++) {
                if ((re&(1<<i))>0) {
                    if (!rv.isEmpty()) rv+=",";
                    rv += sdf.format(cal.getTime());
                }
                cal.add(GregorianCalendar.DAY_OF_YEAR,1);
            }
        }
        return rv;

    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(ho);
        dest.writeInt(mi);
        dest.writeInt(se);
        dest.writeInt(da);
        dest.writeInt(mo);
        dest.writeInt(ye);
        dest.writeInt(re);
        dest.writeInt(co);
        dest.writeString(action);
    }

    public boolean copleteEquals(DeviceTimer d) {
        return ho==d.ho && mi==d.mi && se==d.se && da==d.da && mo==d.mo && ye==d.ye && re==d.re && co==d.co && action.equals(action);
    }

    public int getHo() { return ho; }

    public int getMi() {
        return mi;
    }

    public int getSe() {
        return se;
    }

    public int getDa() {
        return da;
    }

    public int getMo() {
        return mo;
    }

    public int getYe() {
        return ye;
    }

    public int getRe() {
        return re;
    }

    public int getCo() {
        return co;
    }

    public GregorianCalendar getCalendar() {
        return calendar;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setHo(int ho) {
        this.ho = ho;
    }

    public void setMi(int mi) {
        this.mi = mi;
    }

    public void setSe(int se) {
        this.se = se;
    }

    public void setDa(int da) {
        this.da = da;
    }

    public void setMo(int mo) {
        this.mo = mo;
    }

    public void setYe(int ye) {
        this.ye = ye;
    }

    public void setRe(int re) {
        this.re = re;
    }

    public void setCo(int co) {
        this.co = co;
    }

    @Override
    public boolean equals(Object o) {
        if (o==null || !(o instanceof DeviceTimer))
            return false;
        else
            return co==((DeviceTimer)o).co;
    }


    public static DeviceTimer parse(JSONObject t) {
        try {
            DeviceTimer d = new DeviceTimer();
            d.ho = t.getInt("hour");
            d.mi = t.getInt("minute");
            d.se = t.getInt("second");
            d.da = t.getInt("day");
            d.mo = t.getInt("month");
            d.ye = t.getInt("year");
            d.re = t.getInt("rep");
            d.action = t.getString("action");
            d.co = t.getInt("code");
            adjustCalendar(d);
            return d;
        }
        catch (JSONException je) {
            return null;
        }
    }

    public JSONObject toJsonObject() {
        try {
            JSONObject o = new JSONObject();
            o.put("hour", ho);
            o.put("minute", mi);
            o.put("second", se);
            o.put("day", da);
            o.put("month", mo);
            o.put("year", ye);
            o.put("action", action);
            o.put("code", co);
            o.put("rep", re);
            return o;
        }
        catch (JSONException je) {
            return null;
        }
    }

    public DeviceTimer() {

    }

    public DeviceTimer(int hov,int miv,int sev,int dav,int mov,int yev,int rev,int cov,String actionv) {
        ho = hov;
        mi = miv;
        se = sev;
        da = dav;
        mo = mov;
        ye = yev;
        re = rev;
        co = cov;
        action = actionv;
        adjustCalendar(this);
    }

    private void adjustTimer() {
        if (passed() && isRepeating()) {
            int dow;
            while (true) {
                dow = calendar.get(GregorianCalendar.DAY_OF_WEEK)-2;
                if (dow<0) dow+=7;
                calendar.add(GregorianCalendar.DAY_OF_YEAR, 1);
                if (!passed() && (re&dow)>0)
                    break;
            }
            da = calendar.get(GregorianCalendar.DAY_OF_MONTH);
            mo = calendar.get(GregorianCalendar.MONTH)+1;
            ye = calendar.get(GregorianCalendar.YEAR);
            ho = calendar.get(GregorianCalendar.HOUR_OF_DAY);
            mi = calendar.get(GregorianCalendar.MINUTE);
            se = calendar.get(GregorianCalendar.SECOND);
        }
    }

    public boolean isRepeating() {
        return (re&255)>128;
    }

    public int remaining() {
        if (passed()) {
            if (!isRepeating())
                return -1;
            else
                adjustTimer();
        }
        return (int) ((System.currentTimeMillis()-calendar.getTimeInMillis())/1000);
    }

    protected boolean passed() {
        return calendar.getTimeInMillis()<=System.currentTimeMillis();
    }

    public static final Creator<DeviceTimer> CREATOR = new Creator<DeviceTimer>() {
        @Override
        public DeviceTimer createFromParcel(Parcel parcel) {
            return new DeviceTimer(parcel);
        }

        @Override
        public DeviceTimer[] newArray(int i) {
            return new DeviceTimer[i];
        }
    };

}

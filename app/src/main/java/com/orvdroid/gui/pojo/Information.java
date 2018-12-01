package com.orvdroid.gui.pojo;

import android.content.res.Resources;

import com.orvdroid.lib.message.Device;

/**
 * Created by Windows on 22-12-2014.
 */
public class Information {
    public int iconId;
    public String title;
    public int color;
    public String mac;
    public Information(String m, String t,int ic,int col) {
        title = t;
        iconId = ic;
        color = col;
        mac = m;
    }

    public static Information fromDevice(Device d, Resources res) {
        return new Information(
                d.getMac(),
                res.getString(d.getTypeResource())+ " " + d.getName(),
                d.getDrawerIconResource(),
                d.getStatusColor());
    }

    @Override
    public boolean equals(Object o) {
        if (o==null || !(o instanceof Information))
            return false;
        else {
            Information d = (Information) o;
            return d.mac.equals(mac);
        }
    }

    public void copy(Information info) {
        title = info.title;
        iconId = info.iconId;
        color = info.color;
        mac = info.mac;
    }
}

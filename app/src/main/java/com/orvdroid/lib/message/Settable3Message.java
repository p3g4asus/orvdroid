package com.orvdroid.lib.message;

/**
 * Created by Matteo on 23/01/2016.
 */
public class Settable3Message extends RemoteMessage {
    public static int ACTION_ADD = 2;
    public static int ACTION_EDIT = 1;
    public Settable3Message(Device d,DeviceTimer dt,int action) {
        super(d);
        putParam(String.format("%02d/%02d/%04d", dt.getDa(), dt.getMo(), dt.getYe()));
        putParam(String.format("%02d:%02d:%02d", dt.getHo(), dt.getMi(), dt.getSe()));
        putParam(""+dt.getRe());
        putParam(""+(action==ACTION_ADD?-1:dt.getCo()));
        putParam(dt.getAction());
    }
    public Settable3Message(Device d,int dof,int hof,String act) {
        super(d);
        putParam(""+dof);
        putParam(""+hof);
        putParam("0");
        putParam("-1");
        putParam(act);
    }

    public Settable3Message() {

    }
}

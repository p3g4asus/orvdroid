package com.orvdroid.gui.app;

import android.app.Application;
import android.support.v4.content.LocalBroadcastManager;

public class CA extends Application {
	public static LocalBroadcastManager lbm = null;

    public CA() {
        // this method fires only once per application start. 
        // getApplicationContext returns null here
    }

    @Override
    public void onCreate() {
        super.onCreate();    

        // this method fires once as well as constructor 
        // but also application has context here

        lbm = LocalBroadcastManager.getInstance(getApplicationContext());
    }
}

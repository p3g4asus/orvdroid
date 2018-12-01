package com.orvdroid.gui.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.orvdroid.gui.R;
import com.orvdroid.gui.fragments.DeviceFragment;
import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.DeviceTimerAction;
import com.orvdroid.workers.ConnectionService;

import java.util.Date;

/**
 * Created by Matteo on 24/01/2016.
 */
public class TimerS20Dialog extends TimerDialog {
    private static final String SON_ACT = "SON_ACT.";
    protected RadioButton switchonRDB;
    protected RadioButton switchffRDB;

    public TimerS20Dialog(Device d, Date ifd, ConnectionService.ConnectionServiceBinder csb, DeviceFragment fr) {
        super(d, ifd, csb, fr);
    }

    @Override
    protected String getActionString() {
        return switchonRDB.isChecked()?"1":"0";
    }

    @Override
    protected void setViewVariables(View layout) {
        switchonRDB = (RadioButton) layout.findViewById(R.id.switchonRDB);
        switchffRDB = (RadioButton) layout.findViewById(R.id.switchoffRDB);
        super.setViewVariables(layout);
    }

    @Override
    protected int getActionViewResource() {
        return R.layout.onoffradio;
    }

    @Override
    protected boolean isActionOK() {
        return true;
    }

    @Override
    protected void actionAdjust(int pos, DeviceTimerAction dta) {
        if (dta!=null) {
            if (dta.getDt().getAction() == "1")
                switchonRDB.setChecked(true);
            else
                switchffRDB.setChecked(true);
        }
    }

    @Override
    protected void onSaveAction(String pf, Bundle b) {
        b.putBoolean(SON_ACT+pf,switchonRDB.isChecked());
    }

    @Override
    protected void onRestoreAction(String pf, Bundle b) {
        switchonRDB.setChecked(b.getBoolean(SON_ACT+pf));
    }

    @Override
    protected String getShName(String action, long dd, long ss) {
        return myDev.getName()+(action.equals("0")?" OFF ":"ON ")+(dd>0?dd+res.getString(R.string.days)+" ":"")+(ss>0?ss+res.getString(R.string.seconds):"");
    }

    @Override
    protected int getShIco(String action) {
        return action.equals("0")? R.drawable.ic_alarm_red: R.drawable.ic_alarm_green;
    }
}

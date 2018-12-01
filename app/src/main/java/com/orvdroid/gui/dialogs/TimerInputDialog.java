package com.orvdroid.gui.dialogs;

import android.text.InputType;

import com.orvdroid.gui.R;
import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.DeviceUDP;
import com.orvdroid.lib.message.SetoaoMessage;
import com.orvdroid.workers.ConnectionService;

/**
 * Created by Fujitsu on 01/02/2016.
 */
public class TimerInputDialog extends InputDialog{
    public TimerInputDialog(Device d, ConnectionService.ConnectionServiceBinder csb) {
        super(d, csb);
    }

    @Override
    protected int getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }

    @Override
    protected boolean validateInput(String txt) {
        try {
            int v = Integer.parseInt(txt);
            return v>=0 && v<=65535;
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    protected String getActualValue(Device d) {
        return ((DeviceUDP)d).getTimer_off_after_on()+"";
    }

    @Override
    protected void onOK(String val) {
        mBinder.write(new SetoaoMessage(myDev,Integer.parseInt(val)));
    }

    @Override
    protected int getTitleRes() {
        return R.string.id_timer;
    }
}

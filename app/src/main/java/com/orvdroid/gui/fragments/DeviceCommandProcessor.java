package com.orvdroid.gui.fragments;

import com.orvdroid.lib.message.ConnectionStatusMessage;
import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.InfoMessage;
import com.orvdroid.lib.message.IBaseMessage;
import com.orvdroid.lib.message.RemoteMessage;
import com.orvdroid.lib.utils.CommandProcessor;

import java.util.ArrayList;

/**
 * Created by Fujitsu on 29/01/2016.
 */
public class DeviceCommandProcessor implements CommandProcessor {
    private Device oldDev;
    private Refreshable frag;

    public Refreshable getFrag() {
        return frag;
    }

    public void setFrag(Refreshable frag) {
        this.frag = frag;
    }

    public Device getOldDev() {
        return oldDev;
    }

    public void setOldDev(Device oldDev) {
        this.oldDev = oldDev;
    }

    public DeviceCommandProcessor(Device d, Refreshable r) {
        oldDev = d;
        frag = r;
    }


    @Override
    public boolean processCommand(IBaseMessage hs2) {
        if (oldDev!=null) {
            if (hs2 instanceof InfoMessage) {
                InfoMessage gim = (InfoMessage) hs2;
                ArrayList<Device> devl = gim.getDeviceList();
                int idx = devl.indexOf(oldDev);
                if (idx >= 0) {
                    frag.refresh(devl.get(idx), gim);
                }
                return true;
            } else if (hs2 instanceof RemoteMessage) {
                RemoteMessage rm = (RemoteMessage) hs2;
                Device d;
                if ((d = rm.isDeviceModified()) != null && d.equals(oldDev)) {
                    frag.refresh(d, rm);
                }
                return true;
            } else if (hs2 instanceof ConnectionStatusMessage) {
                ConnectionStatusMessage m = (ConnectionStatusMessage) hs2;
                if (m.isError())
                    frag.refresh(null, hs2);
                return true;
            }
        }
        return false;
    }
}

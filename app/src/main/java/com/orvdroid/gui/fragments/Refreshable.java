package com.orvdroid.gui.fragments;

import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.IBaseMessage;
import com.orvdroid.lib.message.RemoteMessage;

/**
 * Created by Matteo on 23/01/2016.
 */
public interface Refreshable {
    void refresh(Device d,IBaseMessage m);
}

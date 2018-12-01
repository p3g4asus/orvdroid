package com.orvdroid.lib.message;

import java.util.List;

/**
 * Created by Matteo on 29/01/2016.
 */
public class CreateshMessage  extends RemoteMessage {
    public CreateshMessage(Device d,String shname,DeviceAllOneKey k) {
        super(d);
        putParam(shname);
        putParam(k.toString());
    }

    public CreateshMessage(Device d,String shname,List<DeviceAllOneKey> ks) {
        super(d);
        putParam(shname);
        for (DeviceAllOneKey k:ks)
            putParam(k.toString());
    }

    public CreateshMessage() {

    }
}

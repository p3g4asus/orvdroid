package com.orvdroid.lib.message;

import java.util.List;

/**
 * Created by Fujitsu on 25/01/2016.
 */
public class LearnirMessage extends EmitirMessage {
    public LearnirMessage(Device d,DeviceAllOneKey k) {
        super(d,k);
    }

    public LearnirMessage(Device d,List<DeviceAllOneKey> ks) {
        super(d,ks);
    }

    public LearnirMessage() {

    }

}
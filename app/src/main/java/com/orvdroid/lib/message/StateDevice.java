package com.orvdroid.lib.message;

import android.os.Bundle;

interface StateDevice {
    int getState();

    Bundle getPossibleStates();

    static Bundle defaultPossibleStates() {
        Bundle b = new Bundle();
        b.putInt("ON",1);
        b.putInt("OFF",0);
        return b;
    }
}

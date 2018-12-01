package com.orvdroid.gui.fragments;

public class StateHolder {
    public static final StateHolder STATUS_ON = new StateHolder("ON", 1);
    public static final StateHolder STATUS_OFF = new StateHolder("OFF", 0);
    public String first;
    public int second;

    public void copy(StateHolder s) {
        first = s.first;
        second = s.second;
    }
    public StateHolder(String s, int i) {
        first = s;
        second = i;
    }
}

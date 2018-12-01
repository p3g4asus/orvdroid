package com.orvdroid.gui.fragments;

import android.os.Bundle;
import android.view.View;

import com.orvdroid.gui.R;
import com.orvdroid.gui.adapters.DeviceMultipleStatusHolder;
import com.orvdroid.gui.adapters.DeviceS20StatusHolder;
import com.orvdroid.gui.adapters.GenericAdapter;
import com.orvdroid.gui.adapters.GenericViewHolder;
import com.orvdroid.gui.views.RecyclerViewEmptySupportGrid;
import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.DevicePrimelan;
import com.orvdroid.lib.message.DeviceState;
import com.orvdroid.lib.message.IBaseMessage;
import com.orvdroid.lib.message.StatechangeMessage;

import java.util.ArrayList;
import java.util.Set;

public class DeviceStateFragment extends DeviceFragment {
    //private TextView actionTV = null;
    //private TextView statusTV = null;
    protected StateHolder type2Status = new StateHolder("OFF", 0);
    private GenericAdapter<?,?> statusAPT;
    private GenericAdapter.ExtClickListener<StateHolder> statusECL;
    private RecyclerViewEmptySupportGrid statusRCV;
    private Bundle possibleStates = null;
    private DeviceState stDev = null;

    @Override
    public void refresh(Device d, IBaseMessage m) {
        myDev = d;
        resetDevice();
        adjustStateIcon();
    }

    @Override
    protected void onRestore(Bundle b) {

    }

    @Override
    protected void onSave(Bundle b) {
    }

    @Override
    protected void onServiceDisconnect() {
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.statefragment;
    }

    private void resetDevice() {
        stDev = (DeviceState) myDev;
        if (stDev!=null) {
            possibleStates = stDev.getPossibleStates();
            if (statusRCV!=null) {
                if (possibleStates.size()<5)
                    statusRCV.setColumnWidth(0);
                else
                    statusRCV.setColumnWidth(-1);
            }
            /*if (mynameTXV!=null)
                mynameTXV.setText(stDev.getName());*/
        }
        else {
            /*if (mynameTXV!=null)
                mynameTXV.setText("N/A");*/
            possibleStates = null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetDevice();
    }

    private void adjustStateIcon() {
        if (statusRCV!=null) {
            if (myDev == null || myDev.isOffline()) {
                statusRCV.setEnabled(false);
                type2Status.copy(StateHolder.STATUS_OFF);
                statusAPT.notifyItemChanged(0);
            }
            else {
                statusRCV.setEnabled(true);
                if (stDev.getState()>0)
                    type2Status.copy(StateHolder.STATUS_ON);
                else
                    type2Status.copy(StateHolder.STATUS_OFF);
                statusAPT.notifyItemChanged(0);
            }
        }
    }

    private void resetAdapter() {
        View v = getView();
        ArrayList<StateHolder> statusLST = new ArrayList<>();
        if (myDev==null || (myDev instanceof DevicePrimelan && ((DevicePrimelan) myDev).getSubtype() == 2)) {

            statusLST.add(type2Status);
            statusAPT = new GenericAdapter<>(statusLST, new DeviceS20StatusHolder(v, null, R.drawable.ic_rcv_primelan_on, R.drawable.ic_rcv_primelan_off), getActivity().getMenuInflater(), true);
        }
        else {
            Set<String> ss = possibleStates.keySet();
            for(String k:ss) {
                statusLST.add(new StateHolder(k,possibleStates.getInt(k)));
            }
            statusAPT = new GenericAdapter<>(statusLST, new DeviceMultipleStatusHolder(v, null), getActivity().getMenuInflater(), true);
        }
        ((GenericAdapter<StateHolder,? extends GenericViewHolder>) statusAPT).setOnItemSelectedListener(statusECL);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // get the url to open
        View v = getView();


        //statusTV = (TextView) v.findViewById(R.id.statusTV);
        //actionTV = (TextView) v.findViewById(R.id.actionTV);
        statusECL = new GenericAdapter.ExtClickListener<StateHolder>() {
            @Override
            public void onItemSelectionChanged(int pos, StateHolder dt,int longC) {
                if (longC==GenericAdapter.CT_CLICK) {
                    if (myDev instanceof DevicePrimelan && ((DevicePrimelan) myDev).getSubtype() == 2) {
                        int ston;
                        mBinder.write(new StatechangeMessage(myDev,
                                stDev.getState()==(ston = possibleStates.getInt("ON"))?
                                possibleStates.getInt("OFF"):ston));
                    }
                    else {
                        mBinder.write(new StatechangeMessage(myDev,dt.second));
                    }
                }
                else if (longC==GenericAdapter.CT_LONGCLICK) {
                    int ico;
                    if (myDev instanceof DevicePrimelan && ((DevicePrimelan) myDev).getSubtype() == 0) {
                        if (dt.second==1)
                            ico = R.drawable.ic_launcher_primelan_on;
                        else
                            ico = R.drawable.ic_launcher_primelan_off;
                    }
                    else
                        ico = R.drawable.ic_launcher_virtual;
                    shortcutAdd(myDev.getName()+" "+dt.first,new StatechangeMessage(myDev,dt.second), ico);
                }
                else if (longC==R.id.action_status_sh_off)
                    shortcutAdd(myDev.getName()+" OFF",new StatechangeMessage(myDev,possibleStates.getInt("ON")), R.drawable.ic_launcher_primelan_off);
                else if (longC==R.id.action_status_sh_on)
                    shortcutAdd(myDev.getName() + " ON", new StatechangeMessage(myDev,possibleStates.getInt("OFF")), R.drawable.ic_launcher_primelan_on);
            }
        };
        resetAdapter();
        statusRCV = v.findViewById(R.id.statusRCV);
        //mynameTXV = v.findViewById(R.id.mynameTXV);
        statusRCV.setAdapter(statusAPT);
        adjustStateIcon();


        //updateAction(connectionUpdates.getLastAction());
        //updateStatus(connectionUpdates.getLastStatus());
    }
}

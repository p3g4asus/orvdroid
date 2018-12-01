package com.orvdroid.gui.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.orvdroid.gui.R;
import com.orvdroid.gui.adapters.DeviceS20StatusHolder;
import com.orvdroid.gui.adapters.GenericAdapter;
import com.orvdroid.gui.dialogs.TimerInputDialog;
import com.orvdroid.gui.dialogs.TimerS20Dialog;
import com.orvdroid.gui.dialogs.TimezoneInputDialog;
import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.DeviceS20;
import com.orvdroid.lib.message.DeviceUDP;
import com.orvdroid.lib.message.IBaseMessage;
import com.orvdroid.lib.message.StateoffMessage;
import com.orvdroid.lib.message.StateonMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DeviceS20Fragment extends DeviceFragment {
	//private TextView actionTV = null;
	//private TextView statusTV = null;
	private Button timersBTN,timerBTN,timezoneBTN;
    private TextView mytimeTXV;
    protected TimezoneInputDialog timezoneInputDialog;
    protected TimerInputDialog timerInputDialog;
    protected StateHolder s20Status = new StateHolder("OFF", 0);
    private GenericAdapter<StateHolder, DeviceS20StatusHolder> statusAPT;
    private GenericAdapter.ExtClickListener<StateHolder> statusECL;
    private RecyclerView statusRCV;
    private Resources res;
    private static final String DATE_FORMAT_STR = "dd/MM/yyyy HH:mm:ss";
    private Timer timerSetTime = null;


    @Override
    public void refresh(Device d,IBaseMessage m) {
        myDev = d;
        adjustStateIcon();
    }

    @Override
    protected void onRestore(Bundle b) {
        timezoneInputDialog.restore(getActivity(), "timezoneInputDialog", b);
        timerInputDialog.restore(getActivity(), "timerInputDialog", b);
    }

    @Override
    protected void onSave(Bundle b) {
        timezoneInputDialog.save("timezoneInputDialog",b);
        timerInputDialog.save("timerInputDialog",b);
    }

    @Override
    protected void onServiceDisconnect() {
        timerInputDialog.setConnectionService(null);
        timezoneInputDialog.setConnectionService(null);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.s20fragment;
    }

    private void setMyTime(long v) {
        SimpleDateFormat sdfmt1 = new SimpleDateFormat(DATE_FORMAT_STR);
        mytimeTXV.setText(String.format(res.getString(R.string.current_time), sdfmt1.format(new Date(v))));
    }

    private void adjustStateIcon() {
        if (statusRCV!=null) {
            if (myDev == null || myDev.isOffline()) {
                statusRCV.setEnabled(false);
                timersBTN.setEnabled(false);
                timerBTN.setEnabled(false);
                timezoneBTN.setEnabled(false);
                timerDialog.dismiss();
                timezoneInputDialog.dismiss();
                timerInputDialog.dismiss();
                s20Status.copy(StateHolder.STATUS_OFF);
                statusAPT.notifyItemChanged(0);
                setMyTime(0);
                if (timerSetTime!=null) {
                    timerSetTime.cancel();
                    timerSetTime = null;
                }
            }
            else {
                if (timerSetTime==null) {
                    timerSetTime = new Timer();
                    timerSetTime.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Activity a = getActivity();
                            if (a != null) {
                                a.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (myDev!=null)
                                            setMyTime(System.currentTimeMillis()-((DeviceUDP)myDev).getDateDiff());
                                        else
                                            setMyTime(0);
                                    }
                                });
                            }

                        }
                    }, 1000, 1000);
                }
                statusRCV.setEnabled(true);
                timersBTN.setEnabled(true);
                timerBTN.setEnabled(true);
                timezoneBTN.setEnabled(true);
                if (((DeviceS20)myDev).getState() == 1)
                    s20Status.copy(StateHolder.STATUS_ON);
                else
                    s20Status.copy(StateHolder.STATUS_OFF);
                statusAPT.notifyItemChanged(0);
                setMyTime(System.currentTimeMillis()-((DeviceUDP)myDev).getDateDiff());
            }
            timerDialog.setDevice(myDev);
            timezoneInputDialog.setDevice(myDev);
            timerInputDialog.setDevice(myDev);
        }
    }
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
        timerDialog = new TimerS20Dialog(myDev,null,null,this);
        timezoneInputDialog = new TimezoneInputDialog(myDev,null);
        timerInputDialog = new TimerInputDialog(myDev,null);
        res = getActivity().getResources();
		// get the url to open
		View v = getView();
        ArrayList<StateHolder> statusLST = new ArrayList<>();
        statusLST.add(s20Status);

		//statusTV = (TextView) v.findViewById(R.id.statusTV);
		//actionTV = (TextView) v.findViewById(R.id.actionTV);
        statusAPT = new GenericAdapter<>(statusLST,new DeviceS20StatusHolder(v,null,R.drawable.ic_rcv_s20_on,R.drawable.ic_rcv_s20_off),getActivity().getMenuInflater(),true);
        statusECL = new GenericAdapter.ExtClickListener<StateHolder>() {
            @Override
            public void onItemSelectionChanged(int pos, StateHolder dt,int longC) {
                if (longC==GenericAdapter.CT_CLICK) {
                    if (((DeviceS20)myDev).getState() == 1)
                        mBinder.write(new StateoffMessage(myDev));
                    else
                        mBinder.write(new StateonMessage(myDev));
                }
                else if (longC==R.id.action_status_sh_off)
                    shortcutAdd(myDev.getName()+" OFF",new StateoffMessage(myDev), R.drawable.ic_launcher_s20_off);
                else if (longC==R.id.action_status_sh_on)
                    shortcutAdd(myDev.getName() + " ON", new StateonMessage(myDev), R.drawable.ic_launcher_s20_on);
            }
        };
        statusAPT.setOnItemSelectedListener(statusECL);
        statusRCV = (RecyclerView)v.findViewById(R.id.statusRCV);
        statusRCV.setAdapter(statusAPT);
        //statusECL.onItemSelectionChanged(statusAPT.getSelectedIndex(), statusAPT.getSelectedItem(), GenericAdapter.CT_CLICK);
		timersBTN = (Button) v.findViewById(R.id.timersBTN);
		timerBTN = (Button) v.findViewById(R.id.timerBTN);
		timezoneBTN = (Button) v.findViewById(R.id.timezoneBTN);
        mytimeTXV = (TextView) v.findViewById(R.id.mytimeTXV);

        timersBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Activity a = getActivity();
                if (a!=null && mBinder!=null) {
                    timerDialog.setConnectionService(mBinder);
                    timerDialog.show(a);
                }
            }
        });
        timerBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Activity a = getActivity();
                if (a!=null && mBinder!=null) {
                    timerInputDialog.setConnectionService(mBinder);
                    timerInputDialog.show(a);
                }
            }
        });
        timezoneBTN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Activity a = getActivity();
                if (a!=null && mBinder!=null) {
                    timezoneInputDialog.setConnectionService(mBinder);
                    timezoneInputDialog.show(a);
                }
            }
        });
        adjustStateIcon();

		
		//updateAction(connectionUpdates.getLastAction());
		//updateStatus(connectionUpdates.getLastStatus());
	}
}

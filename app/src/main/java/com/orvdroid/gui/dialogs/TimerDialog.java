package com.orvdroid.gui.dialogs;

/**
 * Created by Matteo on 23/01/2016.
 */


import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.orvdroid.gui.R;
import com.orvdroid.gui.adapters.DeviceTimerActionHolder;
import com.orvdroid.gui.adapters.GenericAdapter;
import com.orvdroid.gui.fragments.DeviceFragment;
import com.orvdroid.gui.views.RecyclerViewEmptySupport;
import com.orvdroid.lib.message.CleartimersMessage;
import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.DeviceTimer;
import com.orvdroid.lib.message.DeviceTimerAction;
import com.orvdroid.lib.message.Settable3Message;
import com.orvdroid.workers.ConnectionService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public abstract class TimerDialog extends GenericDialog {
    private static final String HH_SPN = "HH_SPN.";
    private static final String MM_SPN = "MM_SPN.";
    private static final String SS_SPN = "SS_SPN.";
    private static final String DD_CAL = "DD_CAL.";
    private static final String MM_CAL = "MM_CAL.";
    private static final String YY_CAL = "YY_CAL.";
    private static final String RE_TIM = "RE_TIM.";
    private static final String DTA_LST = "DTA_LST.";
    private static final String DTA_SEL = "DTA_SEL.";
    protected DatePicker timerDP;
    protected RecyclerViewEmptySupport timerRCV;
    protected RadioButton onceRDB,repeatRDB;
    protected CheckBox[] daysCBX = new CheckBox[7];
    protected Spinner hhSPN,mmSPN,ssSPN;
    protected Button addTimerBTN,editTimerBTN,delTimerBTN;
    protected Date initTimer = null;
    protected GenericAdapter<DeviceTimerAction,DeviceTimerActionHolder> timerAPT;
    protected ArrayList<DeviceTimerAction> actionsList = new ArrayList<>();
    protected DatePicker.OnDateChangedListener timerDCL;
    protected GenericAdapter.ExtClickListener<DeviceTimerAction> timerECL;
    private DeviceFragment devFr;

    public TimerDialog(Device d, Date ifd,ConnectionService.ConnectionServiceBinder csb,DeviceFragment fr) {
        super(d,csb);
        initTimer = ifd;
        devFr = fr;
    }

    protected abstract String getActionString();
    protected abstract int getActionViewResource();
    protected abstract boolean isActionOK();
    protected abstract void actionAdjust(int pos,DeviceTimerAction dta);
    protected abstract void onSaveAction(String pf, Bundle b);
    protected abstract void onRestoreAction(String pf, Bundle b);

    protected abstract String getShName(String action, long dd, long ss);
    protected abstract int getShIco(String action);


    @Override
    protected boolean needsNeutral() {
        return true;
    }

    @Override
    protected void onNeutral(DialogInterface d) {
        DeviceTimerAction dta = checkActionsSh();
        if (dta!=null) {
            DeviceTimer dt = dta.getDt();
            long diff = dt.getCalendar().getTimeInMillis()-System.currentTimeMillis();
            if (diff>0) {
                int dd = (int) (diff/86400000);
                int ss = (int) ((diff%86400000)/1000);
                String act;
                Settable3Message remmsg = new Settable3Message(myDev,dd,ss,act = dt.getAction());
                devFr.shortcutAdd(getShName(act,dd,ss),remmsg,getShIco(act));
            }
        }
    }

    @Override
    protected int getNeutralRes() {
        return R.string.ti_neutral;
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.timeralert;
    }

    @Override
    protected int getTitleRes() {
        return R.string.ti_title;
    }

    @Override
    protected void onShow(DialogInterface d) {
        setOKEnabled(false);
        setNeutralEnabled(false);
    }

    @Override
    protected void onOK(DialogInterface d) {
        initTimer = dateP2Calendar(timerDP, 0, 0, 1).getTime();
        CleartimersMessage dtm = null;
        for (DeviceTimerAction dta : actionsList) {
            int act = dta.getAction();
            if (mBinder == null)
                return;
            else if (act == DeviceTimerAction.ACTION_EDIT || act == DeviceTimerAction.ACTION_ADD)
                mBinder.write(new Settable3Message(myDev, dta.getDt(), act));
            else if (act == DeviceTimerAction.ACTION_REMOVE) {
                if (dtm==null)
                    dtm = new CleartimersMessage(myDev, dta.getDt());
                else
                    dtm.addTimer(dta.getDt());
            }
        }
        if (dtm!=null)
            mBinder.write(dtm);
    }

    @Override
    protected void onRestore(String pf, Bundle b) {
        int hh,mm,ss,dd,MM,yy,re;
        hh = b.getInt(HH_SPN + pf);
        mm = b.getInt(MM_SPN + pf);
        ss = b.getInt(SS_SPN + pf);
        dd = b.getInt(DD_CAL + pf);
        MM = b.getInt(MM_CAL + pf);
        yy = b.getInt(YY_CAL + pf);
        re = b.getInt(RE_TIM + pf);
        DeviceTimerAction ca = new DeviceTimerAction(new DeviceTimer(hh, mm, ss, dd, MM, yy, re, 2, ""));
        adjustDateTimeRep(0, ca);
        onRestoreAction(pf, b);
        for (int i = timerAPT.getItemCount()-1; i>=0; i--)
            timerAPT.deleteItem(0);
        ArrayList<DeviceTimerAction> dtas = b.getParcelableArrayList(DTA_LST + pf);
        hh = b.getInt(DTA_SEL + pf, -1);
        if (hh >= 0 && hh < dtas.size())
            timerAPT.setSelectedIndex(hh);
        DeviceTimer dt;
        final ArrayList<DeviceTimer> timerList = myDev.getTimers();
        for (DeviceTimerAction dta : dtas) {
            for (DeviceTimer newdt:timerList) {
                dt = dta.getDt();
                if (newdt.equals(dt)) {
                    mm = dta.getAction();
                    if (mm==DeviceTimerAction.ACTION_EDIT) {
                        if (newdt.copleteEquals(dt)) {
                            dta.setAction(DeviceTimerAction.ACTION_NONE);
                            dta.getDtO().set(newdt);
                        }
                    }
                    else if (mm==DeviceTimerAction.ACTION_REMOVE) {
                        if (!newdt.copleteEquals(dta.getDtO())) {
                            dta.setAction(DeviceTimerAction.ACTION_NONE);
                            dta.getDtO().set(newdt);
                            dt.set(newdt);
                        }
                    }
                    break;
                }
            }
            timerAPT.addItemNoSel(dta, timerAPT.getItemCount());
        }
        boolean tOk = isTimerOK();
        addTimerBTN.setEnabled(tOk);
        editTimerBTN.setEnabled(hh >= 0 && tOk);
        delTimerBTN.setEnabled(hh >= 0);
        setOKEnabled(checkActions());
        setNeutralEnabled(checkActionsSh()!=null);
    }

    @Override
    protected void onSave(String pf, Bundle b) {
        Calendar c = dateP2Calendar(timerDP, 1, 0, 0);
        b.putInt(HH_SPN+pf,hhSPN.getSelectedItemPosition());
        b.putInt(MM_SPN+pf,mmSPN.getSelectedItemPosition());
        b.putInt(SS_SPN+pf,ssSPN.getSelectedItemPosition());
        b.putInt(DD_CAL+pf,c.get(GregorianCalendar.DAY_OF_MONTH));
        b.putInt(MM_CAL+pf,c.get(GregorianCalendar.MONTH)+1);
        b.putInt(YY_CAL+pf,c.get(GregorianCalendar.YEAR));
        b.putInt(RE_TIM+pf,gui2RE());
        b.putParcelableArrayList(DTA_LST + pf, actionsList);
        b.putInt(DTA_SEL+pf,timerAPT.getSelectedIndex());
        onSaveAction(pf, b);
    }

    @Override
    protected void onDismiss(DialogInterface d) {
        initTimer = null;
    }

    private Calendar dateP2Calendar(DatePicker fromDP,int hr, int min, int sec) {
        Calendar c = Calendar.getInstance();
        c.set(fromDP.getYear(), fromDP.getMonth(), fromDP.getDayOfMonth(),hr,min,sec);
        return c;
    }
    protected DeviceTimer gui2Timer() {
        String action = getActionString();
        if (action==null || action.isEmpty())
            return null;
        else {
            Calendar c = dateP2Calendar(timerDP, 1, 0, 0);
            return new DeviceTimer(
                    hhSPN.getSelectedItemPosition(),
                    mmSPN.getSelectedItemPosition(),
                    ssSPN.getSelectedItemPosition(),
                    c.get(GregorianCalendar.DAY_OF_MONTH),
                    c.get(GregorianCalendar.MONTH)+1,
                    c.get(GregorianCalendar.YEAR),
                    gui2RE(),
                    getNewCo(),
                    action
            );
        }
    }

    protected boolean isTimerOK() {
        Calendar c = dateP2Calendar(timerDP, hhSPN.getSelectedItemPosition(), mmSPN.getSelectedItemPosition(), ssSPN.getSelectedItemPosition());
        int re = gui2RE();
        return (!(re==0 && !onceRDB.isChecked())) && isActionOK() && (c.getTimeInMillis()>System.currentTimeMillis() || (re&255)>128);
    }

    protected boolean checkActions() {
        for (DeviceTimerAction dta:actionsList) {
            if (dta.getAction()!=DeviceTimerAction.ACTION_NONE)
                return true;
        }
        return false;
    }

    protected DeviceTimerAction checkActionsSh() {
        int act;
        DeviceTimerAction mydta = null;
        for (DeviceTimerAction dta:actionsList) {
            if ((act = dta.getAction())==DeviceTimerAction.ACTION_ADD && !dta.getDt().isRepeating() && mydta==null)
                mydta = dta;
            else if (act!=DeviceTimerAction.ACTION_NONE)
                return null;
        }
        return mydta;
    }

    private int getNewCo() {
        int i = 1;
        boolean repeat;
        while (true) {
            repeat = false;
            for (DeviceTimerAction dta : actionsList) {
                DeviceTimer dt = dta.getDt();
                if (dt!=null) {
                    if (dt.getCo() == i)
                        repeat = true;
                }
            }
            if (!repeat)
                return i;
            else
                i++;
        }
    }

    protected int gui2RE() {
        if (onceRDB.isChecked())
            return 0;
        else {
            int re = 128,i = 0;
            for (CheckBox cb:daysCBX) {
                if (cb.isChecked())
                    re|=(1<<i);
                i++;
            }
            return re>128?re:0;
        }
    }

    @Override
    protected void onInflate(LayoutInflater inflater,View layout) {
        final LinearLayout timerLLT = (LinearLayout)layout.findViewById(R.id.timerLLT);
        inflater.inflate(getActionViewResource(), timerLLT);
    }


    @Override
    protected void setViewVariables(View layout) {
        timerDP = (DatePicker)layout.findViewById(R.id.timerDP);
        timerRCV = (RecyclerViewEmptySupport)layout.findViewById(R.id.timerRCV);
        onceRDB = (RadioButton)layout.findViewById(R.id.onceRDB);
        repeatRDB = (RadioButton)layout.findViewById(R.id.repeatRDB);
        daysCBX[0] = (CheckBox)layout.findViewById(R.id.monCBX);
        daysCBX[1] = (CheckBox)layout.findViewById(R.id.tueCBX);
        daysCBX[2] = (CheckBox)layout.findViewById(R.id.wedCBX);
        daysCBX[3] = (CheckBox)layout.findViewById(R.id.thuCBX);
        daysCBX[4] = (CheckBox)layout.findViewById(R.id.friCBX);
        daysCBX[5] = (CheckBox)layout.findViewById(R.id.satCBX);
        daysCBX[6] = (CheckBox)layout.findViewById(R.id.sunCBX);
        hhSPN = (Spinner)layout.findViewById(R.id.hhSPN);
        mmSPN = (Spinner)layout.findViewById(R.id.mmSPN);
        ssSPN = (Spinner)layout.findViewById(R.id.ssSPN);
        addTimerBTN = (Button)layout.findViewById(R.id.addTimerBTN);
        delTimerBTN = (Button)layout.findViewById(R.id.delTimerBTN);
        editTimerBTN = (Button)layout.findViewById(R.id.editTimerBTN);
        final ArrayList<DeviceTimer> timerList = myDev.getTimers();
        actionsList.clear();
        for (DeviceTimer dt:timerList) {
            actionsList.add(new DeviceTimerAction(dt));
        }
        final Calendar c = Calendar.getInstance();
        if (initTimer!=null)
            c.setTime(initTimer);
        timerAPT = new GenericAdapter<>(actionsList, new DeviceTimerActionHolder(new View(activity), null,null));
        timerECL = new GenericAdapter.ExtClickListener<DeviceTimerAction>() {
            @Override
            public void onItemSelectionChanged(int position, DeviceTimerAction dt,int longC) {
                editTimerBTN.setEnabled(position >= 0 && isTimerOK());
                delTimerBTN.setEnabled(position >= 0);
                adjustDateTimeRep(position,dt);
                actionAdjust(position,dt);
            }
        };
        timerAPT.setOnItemSelectedListener(timerECL);
        timerRCV.setEmptyView(layout.findViewById(R.id.timerEmptyTXV));
        timerRCV.setAdapter(timerAPT);
        timerECL.onItemSelectionChanged(timerAPT.getSelectedIndex(), timerAPT.getSelectedItem(), GenericAdapter.CT_CLICK);
        //timerRCV.getLayoutParams().height = 40;

        AdapterView.OnItemSelectedListener hhmmssSPNLST = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                addTimerBTN.setEnabled(isTimerOK());
                editTimerBTN.setEnabled(isTimerOK() && timerAPT.getSelectedItem()!=null);
                delTimerBTN.setEnabled(timerAPT.getSelectedItem()!=null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                addTimerBTN.setEnabled(false);
                editTimerBTN.setEnabled(false);
                delTimerBTN.setEnabled(timerAPT.getSelectedItem()!=null);
            }

        };
        hhSPN.setOnItemSelectedListener(hhmmssSPNLST);
        mmSPN.setOnItemSelectedListener(hhmmssSPNLST);
        ssSPN.setOnItemSelectedListener(hhmmssSPNLST);

        timerDCL = new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                addTimerBTN.setEnabled(isTimerOK());
                editTimerBTN.setEnabled(isTimerOK() && timerAPT.getSelectedItem()!=null);
                delTimerBTN.setEnabled(timerAPT.getSelectedItem()!=null);
            }
        };
        timerDP.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), timerDCL);
        hhSPN.setSelection(c.get(Calendar.HOUR_OF_DAY));
        mmSPN.setSelection(c.get(Calendar.MINUTE));
        ssSPN.setSelection(c.get(Calendar.SECOND));
        addTimerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceTimer dt = gui2Timer();
                if (dt != null) {
                    timerAPT.addItem(new DeviceTimerAction(DeviceTimerAction.ACTION_ADD, dt), timerAPT.getItemCount());
                    setOKEnabled(true);
                    setNeutralEnabled(checkActionsSh()!=null);
                }
            }
        });
        editTimerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceTimerAction dta = timerAPT.getSelectedItem();
                if (dta != null && isTimerOK()) {
                    DeviceTimer newdt = gui2Timer();
                    DeviceTimer olddt = dta.getDt();
                    newdt.setCo(olddt.getCo());
                    int act = dta.getAction();
                    if (act == DeviceTimerAction.ACTION_NONE)
                        dta.setAction(DeviceTimerAction.ACTION_EDIT);
                    else if (act == DeviceTimerAction.ACTION_EDIT && newdt.copleteEquals(dta.getDtO()))
                        dta.setAction(DeviceTimerAction.ACTION_NONE);
                    olddt.set(newdt);
                    timerAPT.notifySelectedItemChanged();
                    setOKEnabled(checkActions());
                    setNeutralEnabled(checkActionsSh()!=null);
                } else {
                    editTimerBTN.setEnabled(false);
                    delTimerBTN.setEnabled(false);
                }

            }
        });
        delTimerBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceTimerAction dta = timerAPT.getSelectedItem();
                if (dta != null) {
                    int act = dta.getAction();
                    if (act == DeviceTimerAction.ACTION_ADD)
                        timerAPT.deleteSelected();
                    else {
                        dta.setAction(DeviceTimerAction.ACTION_REMOVE);
                        timerAPT.notifySelectedItemChanged();
                    }

                    setOKEnabled(checkActions());
                    setNeutralEnabled(checkActionsSh()!=null);
                } else {
                    editTimerBTN.setEnabled(false);
                    delTimerBTN.setEnabled(false);
                }

            }
        });
        CompoundButton.OnCheckedChangeListener onceCCL = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                for (CheckBox cb : daysCBX) {
                    cb.setEnabled(!isChecked);
                }
                addTimerBTN.setEnabled(isTimerOK());
                editTimerBTN.setEnabled(isTimerOK() && timerAPT.getSelectedItem()!=null);
            }
        };
        onceRDB.setOnCheckedChangeListener(onceCCL);
        onceCCL.onCheckedChanged(onceRDB, onceRDB.isChecked());

        CompoundButton.OnCheckedChangeListener daysCCL = new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                addTimerBTN.setEnabled(isTimerOK());
                editTimerBTN.setEnabled(isTimerOK() && timerAPT.getSelectedItem()!=null);
            }
        };

        for (CheckBox cb:daysCBX)
            cb.setOnCheckedChangeListener(daysCCL);
    }

    private void adjustDateTimeRep(int position, DeviceTimerAction dta) {
        if (dta!=null) {
            DeviceTimer dt = dta.getDt();
            hhSPN.setSelection(dt.getHo());
            mmSPN.setSelection(dt.getMi());
            ssSPN.setSelection(dt.getSe());
            Calendar c = dt.getCalendar();
            timerDP.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), timerDCL);
            int re = dt.getRe();
            if ((re&255)<=128)
                onceRDB.setChecked(true);
            else {
                repeatRDB.setChecked(true);
                for (int i = 0; i<daysCBX.length; i++)
                    daysCBX[i].setChecked((re&(1<<i))!=0);
            }
            addTimerBTN.setEnabled(isTimerOK());
            editTimerBTN.setEnabled(isTimerOK() && timerAPT.getSelectedItem()!=null);
            delTimerBTN.setEnabled(timerAPT.getSelectedItem()!=null);
        }
    }
}

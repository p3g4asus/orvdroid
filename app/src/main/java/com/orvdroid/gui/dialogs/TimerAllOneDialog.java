package com.orvdroid.gui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.orvdroid.gui.R;
import com.orvdroid.gui.adapters.DeviceAllOneKeyHolder;
import com.orvdroid.gui.adapters.GenericAdapter;
import com.orvdroid.gui.fragments.DeviceFragment;
import com.orvdroid.gui.views.RecyclerViewEmptySupport;
import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.DeviceAllOne;
import com.orvdroid.lib.message.DeviceAllOneKey;
import com.orvdroid.lib.message.DeviceTimerAction;
import com.orvdroid.workers.ConnectionService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Matteo on 24/01/2016.
 */
public class TimerAllOneDialog extends TimerDialog {
    private static final String KEYS_LST = "KEYS_LST.";
    private static final String KEYS_SPN = "KEYS_SPN.";
    private static final String KEYS_SEL = "KEYS_SEL.";
    protected Spinner keysSPN;
    protected RecyclerViewEmptySupport keysRCV;
    protected Button addKeyBTN,editKeyBTN, delKeyBTN;
    protected DeviceAllOne dev;
    protected GenericAdapter<DeviceAllOneKey,DeviceAllOneKeyHolder> keysAPT;
    protected GenericAdapter.ExtClickListener<DeviceAllOneKey> keysECL;

    protected ArrayList<DeviceAllOneKey> keysLST = new ArrayList<>();
    public TimerAllOneDialog(Device d, Date ifd, ConnectionService.ConnectionServiceBinder csb,DeviceFragment fr) {
        super(d, ifd, csb, fr);
        dev = (DeviceAllOne) d;
    }

    @Override
    protected String getActionString() {
        String rv = "";
        for (DeviceAllOneKey kk:keysLST) {
            if (!rv.isEmpty())
                rv+=" ";
            rv += kk.toString();
        }
        return rv;
    }

    @Override
    protected void setViewVariables(View layout) {
        keysSPN = (Spinner)layout.findViewById(R.id.keysSPN);
        addKeyBTN = (Button)layout.findViewById(R.id.addKeyBTN);
        delKeyBTN = (Button)layout.findViewById(R.id.delKeyBTN);
        editKeyBTN = (Button)layout.findViewById(R.id.editKeyBTN);
        addKeyBTN.setEnabled(keysSPN.getSelectedItemPosition() >= 0);
        keysLST.clear();
        keysAPT = new GenericAdapter<>(keysLST,new DeviceAllOneKeyHolder(layout,null));
        keysECL = new GenericAdapter.ExtClickListener<DeviceAllOneKey>() {
            @Override
            public void onItemSelectionChanged(int pos, DeviceAllOneKey dt,int longC) {
                editKeyBTN.setEnabled(pos >= 0 && keysSPN.getSelectedItemPosition() >= 0);
                delKeyBTN.setEnabled(pos >= 0);
            }
        };
        keysAPT.setOnItemSelectedListener(keysECL);
        keysRCV = (RecyclerViewEmptySupport)layout.findViewById(R.id.keysRCV);
        keysRCV.setEmptyView(layout.findViewById(R.id.keysEmptyTXV));
        keysRCV.setAdapter(keysAPT);
        keysECL.onItemSelectionChanged(keysAPT.getSelectedIndex(),keysAPT.getSelectedItem(), GenericAdapter.CT_CLICK);

        keysSPN.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editKeyBTN.setEnabled(keysAPT.getSelectedItem() != null);
                addKeyBTN.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                editKeyBTN.setEnabled(false);
                addKeyBTN.setEnabled(false);
            }
        });

        addKeyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceAllOneKey dk = (DeviceAllOneKey) keysSPN.getSelectedItem();
                if (dk!=null)
                    keysAPT.addItem(dk, keysAPT.getItemCount());
                else {
                    addKeyBTN.setEnabled(false);
                    editKeyBTN.setEnabled(false);
                }
                addTimerBTN.setEnabled(isTimerOK());
            }
        });

        editKeyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DeviceAllOneKey dk = keysAPT.getSelectedItem();
                DeviceAllOneKey fromDk = (DeviceAllOneKey) keysSPN.getSelectedItem();
                if (dk!=null && fromDk!=null) {
                    dk.set(fromDk);
                    keysAPT.notifySelectedItemChanged();
                }
                if (dk==null) {
                    editKeyBTN.setEnabled(false);
                    delKeyBTN.setEnabled(false);
                }
                if (fromDk==null) {
                    editKeyBTN.setEnabled(false);
                    addKeyBTN.setEnabled(false);
                }
            }
        });

        delKeyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keysAPT.deleteSelected();
                addTimerBTN.setEnabled(isTimerOK());
            }
        });

        HashMap<String,ArrayList<DeviceAllOneKey>> k = dev.getSh();
        ArrayList<DeviceAllOneKey> allkeys = new ArrayList<>();
        Iterator<Map.Entry<String,ArrayList<DeviceAllOneKey>>> it = k.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,ArrayList<DeviceAllOneKey>> pair = it.next();
            allkeys.add(pair.getValue().get(0));
        }
        k = dev.getD433();
        it = k.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,ArrayList<DeviceAllOneKey>> pair = it.next();
            allkeys.addAll(pair.getValue());
        }
        k = dev.getDir();
        it = k.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,ArrayList<DeviceAllOneKey>> pair = it.next();
            allkeys.addAll(pair.getValue());
        }
        ArrayAdapter<DeviceAllOneKey> adapter = new ArrayAdapter<DeviceAllOneKey>(
                layout.getContext(), android.R.layout.simple_spinner_item, allkeys);
        keysSPN.setAdapter(adapter);
        super.setViewVariables(layout);
    }

    @Override
    protected int getActionViewResource() {
        return R.layout.keyemit;
    }

    @Override
    protected boolean isActionOK() {
        return keysLST.size()>0;
    }

    @Override
    protected void actionAdjust(int pos, DeviceTimerAction dta) {
        if (dta!=null) {
            for (int i = keysAPT.getItemCount()-1; i>=0; i--) {
                keysAPT.deleteItem(0);
            }
            String act = dta.getDt().getAction();
            if (act!=null) {
                String[] acts = act.split(" ");
                String lastDev = "";
                String[] keyparts;
                String p1 = "", p2 = "";
                char c;
                for (String a:acts) {
                    keyparts = a.split(":");
                    if ((c = keyparts[0].charAt(0))!='@' && c!='$') {
                        if (keyparts.length>1) {
                            p1 = lastDev = keyparts[0];
                            p2 = keyparts[1];
                        }
                        else {
                            p1 = lastDev;
                            p2 = keyparts[0];
                        }
                    }
                    else {
                        p1 = keyparts[0];
                        p2 = "";
                    }
                    keysAPT.addItem(new DeviceAllOneKey(p1,p2),keysAPT.getItemCount());
                }
            }
        }

    }

    @Override
    protected void onSaveAction(String pf, Bundle b) {
        b.putParcelableArrayList(KEYS_LST + pf, keysLST);
        b.putInt(KEYS_SEL + pf, keysAPT.getSelectedIndex());
        b.putParcelable(KEYS_SPN + pf, (Parcelable) keysSPN.getSelectedItem());
    }

    @Override
    protected void onRestoreAction(String pf, Bundle b) {
        ArrayList<DeviceAllOneKey> dks = b.getParcelableArrayList(KEYS_LST + pf);
        for (int i = keysAPT.getItemCount()-1; i>=0; i--)
            keysAPT.deleteItem(0);
        int sel = b.getInt(KEYS_SEL + pf, -1);
        if (sel >= 0 && sel < dks.size())
            keysAPT.setSelectedIndex(sel);
        for (DeviceAllOneKey dk : dks) {
            keysAPT.addItemNoSel(dk, keysAPT.getItemCount());
        }
        keysECL.onItemSelectionChanged(keysAPT.getSelectedIndex(), keysAPT.getSelectedItem(), GenericAdapter.CT_CLICK);
        DeviceAllOneKey dk = b.getParcelable(KEYS_SPN + pf);
        if (dk != null) {
            ArrayAdapter<DeviceAllOneKey> dka = (ArrayAdapter<DeviceAllOneKey>) keysSPN.getAdapter();
            sel = dka.getPosition(dk);
            if (sel >= 0)
                keysSPN.setSelection(sel);
        }
        addTimerBTN.setEnabled(isTimerOK());
    }

    @Override
    protected String getShName(String action, long dd, long ss) {
        return action+" "+(dd>0?dd+res.getString(R.string.days)+" ":"")+(ss>0?ss+res.getString(R.string.seconds):"");
    }

    @Override
    protected int getShIco(String action) {
        return R.drawable.ic_alarm_purple;
    }
}

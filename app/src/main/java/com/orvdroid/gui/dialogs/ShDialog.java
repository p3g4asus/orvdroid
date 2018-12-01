package com.orvdroid.gui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.orvdroid.gui.R;
import com.orvdroid.gui.adapters.DeviceAllOneKeyHolder;
import com.orvdroid.gui.adapters.GenericAdapter;
import com.orvdroid.gui.util.StringNotNullTextWatcher;
import com.orvdroid.gui.views.RecyclerViewEmptySupport;
import com.orvdroid.lib.message.CreateshMessage;
import com.orvdroid.lib.message.DeviceAllOne;
import com.orvdroid.lib.message.DeviceAllOneKey;
import com.orvdroid.workers.ConnectionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by Fujitsu on 29/01/2016.
 */
public class ShDialog extends GenericDialog {
    private static final String SH_ETX = "SH_ETX.";
    private static final String KEYS_LST = "KEYS_LST.";
    private static final String KEYS_SEL = "KEYS_SEL.";
    private static final String KEYS_SPN = "KEYS_SPN.";
    protected EditText shETX;
    protected Spinner keysSPN;
    protected RecyclerViewEmptySupport keysRCV;
    protected Button addKeyBTN,editKeyBTN, delKeyBTN;
    protected GenericAdapter<DeviceAllOneKey,DeviceAllOneKeyHolder> keysAPT;
    protected ArrayList<DeviceAllOneKey> keysLST = new ArrayList<DeviceAllOneKey>();
    protected StringNotNullTextWatcher shETXV;
    protected GenericAdapter.ExtClickListener<DeviceAllOneKey> keysECL;


    @Override
    protected void onDismiss(DialogInterface d) {
        keysLST.clear();
    }

    @Override
    protected void onOK(DialogInterface d) {
        if (mBinder != null)
            mBinder.write(new CreateshMessage(myDev, shETX.getText().toString(),keysLST));
        onDismiss(d);
    }

    @Override
    protected void onShow(DialogInterface d) {
        setOKEnabled(keysAPT.getItemCount() > 0);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.shalert;
    }

    @Override
    protected int getTitleRes() {
        return R.string.sh_title;
    }

    @Override
    protected void onRestore(String pf, Bundle b) {
        ArrayList<DeviceAllOneKey> dks = b.getParcelableArrayList(KEYS_LST+pf);
        shETX.setText(b.getString(SH_ETX + pf, ""));
        int sel = b.getInt(KEYS_SEL + pf, -1);
        if (sel >= 0 && sel < dks.size())
            keysAPT.setSelectedIndex(sel);
        for (DeviceAllOneKey dk : dks)
            keysAPT.addItemNoSel(dk, keysAPT.getItemCount());
        keysECL.onItemSelectionChanged(keysAPT.getSelectedIndex(), keysAPT.getSelectedItem(), GenericAdapter.CT_CLICK);
        DeviceAllOneKey dk = b.getParcelable(KEYS_SPN + pf);
        if (dk != null) {
            ArrayAdapter<DeviceAllOneKey> dka = (ArrayAdapter<DeviceAllOneKey>) keysSPN.getAdapter();
            sel = dka.getPosition(dk);
            if (sel >= 0)
                keysSPN.setSelection(sel);
        }
        setOKEnabled(shETXV.validateText());
    }

    @Override
    protected void onSave(String pf, Bundle b) {
        b.putString(SH_ETX+pf,shETX.getText().toString());
        b.putParcelableArrayList(KEYS_LST + pf, keysLST);
        b.putInt(KEYS_SEL + pf, keysAPT.getSelectedIndex());
        b.putParcelable(KEYS_SPN + pf, (Parcelable) keysSPN.getSelectedItem());
    }

    public ShDialog(DeviceAllOne d, ConnectionService.ConnectionServiceBinder csb) {
        super(d,csb);
    }

    @Override
    protected void setViewVariables(View layout) {
        final DeviceAllOne myD = (DeviceAllOne) myDev;
        shETX = (EditText)layout.findViewById(R.id.shETX);
        shETXV = new StringNotNullTextWatcher(shETX) {
            @Override
            protected void textChanged(boolean isOk) {
                if (isOk) {
                    if (keysAPT.getItemCount()==0) {
                        setOKEnabled(myD.getSh().containsKey("@"+shETX.getText().toString()));
                        return;
                    }
                }
                setOKEnabled(isOk);
            }
        };

        keysSPN = (Spinner)layout.findViewById(R.id.keysSPN);
        addKeyBTN = (Button)layout.findViewById(R.id.addKeyBTN);
        delKeyBTN = (Button)layout.findViewById(R.id.delKeyBTN);
        editKeyBTN = (Button)layout.findViewById(R.id.editKeyBTN);
        addKeyBTN.setEnabled(keysSPN.getSelectedItemPosition() >= 0);
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
                editKeyBTN.setEnabled(keysAPT.getSelectedIndex()>=0);
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
                setOKEnabled(shETXV.validateText());
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
                shETXV.afterTextChanged(shETX.getText());
            }
        });

        HashMap<String,ArrayList<DeviceAllOneKey>> k = myD.getSh();
        ArrayList<DeviceAllOneKey> allkeys = new ArrayList<>();
        Iterator<Map.Entry<String,ArrayList<DeviceAllOneKey>>> it = k.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,ArrayList<DeviceAllOneKey>> pair = it.next();
            allkeys.add(pair.getValue().get(0));
        }
        k = myD.getD433();
        it = k.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,ArrayList<DeviceAllOneKey>> pair = it.next();
            allkeys.addAll(pair.getValue());
        }
        k = myD.getDir();
        it = k.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,ArrayList<DeviceAllOneKey>> pair = it.next();
            allkeys.addAll(pair.getValue());
        }
        ArrayAdapter<DeviceAllOneKey> adapter = new ArrayAdapter<DeviceAllOneKey>(
                layout.getContext(), android.R.layout.simple_spinner_item, allkeys);
        keysSPN.setAdapter(adapter);
    }
}

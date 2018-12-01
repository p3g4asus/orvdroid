package com.orvdroid.gui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.orvdroid.gui.R;
import com.orvdroid.gui.adapters.DeviceAllOneKeyHolder;
import com.orvdroid.gui.adapters.GenericAdapter;
import com.orvdroid.gui.util.StringNotNullTextWatcher;
import com.orvdroid.gui.util.ValidatingTextWatcher;
import com.orvdroid.gui.views.RecyclerViewEmptySupport;
import com.orvdroid.lib.message.DeviceAllOne;
import com.orvdroid.lib.message.DeviceAllOneKey;
import com.orvdroid.lib.message.LearnirMessage;
import com.orvdroid.workers.ConnectionService;

import java.util.ArrayList;

/**
 * Created by Matteo on 25/01/2016.
 */
public class LearnIrDialog extends GenericDialog {
    private static final String KEY_ETX = "KEY_ETX.";
    private static final String DEVICE_ETX = "DEVICE_ETX.";
    private static final String KEYS_LST = "KEYS_LST.";
    private static final String KEYS_SEL = "KEYS_SEL.";
    protected RecyclerViewEmptySupport keysRCV;
    protected EditText keyETX,deviceETX;
    protected GenericAdapter<DeviceAllOneKey,DeviceAllOneKeyHolder> keysAPT;
    protected Button addKeyBTN,delKeyBTN,editKeyBTN;
    protected ArrayList<DeviceAllOneKey> keysLST = new ArrayList<DeviceAllOneKey>();
    protected StringNotNullTextWatcher deviceETXV;
    protected GenericAdapter.ExtClickListener<DeviceAllOneKey> keysECL;

    @Override
    protected void onDismiss(DialogInterface d) {
        keysLST.clear();
    }

    @Override
    protected void onOK(DialogInterface d) {
        if (mBinder != null)
            mBinder.write(new LearnirMessage(myDev, keysLST));
        onDismiss(d);
    }

    @Override
    protected void onShow(DialogInterface d) {
        setOKEnabled(keysAPT.getItemCount() > 0);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.keyalert;
    }

    @Override
    protected int getTitleRes() {
        return R.string.li_title;
    }

    @Override
    protected void onRestore(String pf, Bundle b) {
        ArrayList<DeviceAllOneKey> dks = b.getParcelableArrayList(KEYS_LST+pf);
        keyETX.setText(b.getString(KEY_ETX + pf, ""));
        deviceETX.setText(b.getString(DEVICE_ETX + pf, ""));
        int sel = b.getInt(KEYS_SEL + pf, -1);
        if (sel >= 0 && sel < dks.size())
            keysAPT.setSelectedIndex(sel);
        for (DeviceAllOneKey dk : dks)
            keysAPT.addItem(dk, keysAPT.getItemCount());
        keysECL.onItemSelectionChanged(keysAPT.getSelectedIndex(), keysAPT.getSelectedItem(), GenericAdapter.CT_CLICK);
    }

    @Override
    protected void onSave(String pf, Bundle b) {
        b.putString(KEY_ETX+pf,keyETX.getText().toString());
        b.putString(DEVICE_ETX+pf,deviceETX.getText().toString());
        b.putParcelableArrayList(KEYS_LST + pf, keysLST);
        b.putInt(KEYS_SEL+pf,keysAPT.getSelectedIndex());
    }

    public LearnIrDialog(DeviceAllOne d, ConnectionService.ConnectionServiceBinder csb) {
        super(d,csb);
    }

    private boolean isGenericKeyOk(String str) {
        return str.length() > 0 &&
                str.indexOf(':')<0 &&
                str.indexOf('/')<0 && str.indexOf('\\')<0 &&
                str.indexOf('\"')<0 &&
                str.indexOf('$')<0;
    }

    private String validSingleKey() {
        String v = keyETX.getText().toString();
        if (isGenericKeyOk(v) && v.indexOf(' ')<0)
            return v;
        else
            return null;
    }

    private String[] validMultipleKey() {
        String v = keyETX.getText().toString();
        if (isGenericKeyOk(v)) {
            return v.split(" ");
        }
        else
            return null;
    }

    @Override
    protected void setViewVariables(View layout) {
        addKeyBTN = (Button)layout.findViewById(R.id.addKeyBTN);
        delKeyBTN = (Button)layout.findViewById(R.id.delKeyBTN);
        editKeyBTN = (Button)layout.findViewById(R.id.editKeyBTN);
        keyETX = (EditText)layout.findViewById(R.id.keyETX);
        deviceETX = (EditText)layout.findViewById(R.id.deviceETX);
        ValidatingTextWatcher vtw = new ValidatingTextWatcher(keyETX) {

            @Override
            public boolean validateText(Editable s) {
                return isGenericKeyOk(s.toString());
            }

            @Override
            protected void textChanged(boolean isOk) {
                editKeyBTN.setEnabled(isOk && keysAPT.getSelectedIndex()>=0 && validSingleKey()!=null);
                addKeyBTN.setEnabled(isOk && validMultipleKey()!=null);
            }
        };
        /*keyETXV = new StringNotNullTextWatcher(keyETX) {
            @Override
            protected void textChanged(boolean isOk) {
                isOk = isOk && deviceETXV.validateText();
                editKeyBTN.setEnabled(isOk && keysAPT.getSelectedIndex()>=0);
                addKeyBTN.setEnabled(isOk);
            }
        };*/
        deviceETXV = new StringNotNullTextWatcher(deviceETX) {
            @Override
            protected void textChanged(boolean isOk) {
                //isOk = isOk && keyETXV.validateText();
                editKeyBTN.setEnabled(isOk && keysAPT.getSelectedIndex()>=0 && validSingleKey()!=null);
                addKeyBTN.setEnabled(isOk && validMultipleKey()!=null);
            }
        };
        vtw.afterTextChanged(null);
        deviceETXV.afterTextChanged(null);

        keysAPT = new GenericAdapter<>(keysLST,new DeviceAllOneKeyHolder(layout,null));
        keysECL = new GenericAdapter.ExtClickListener<DeviceAllOneKey>() {
            @Override
            public void onItemSelectionChanged(int pos, DeviceAllOneKey dt,int longC) {
                editKeyBTN.setEnabled(pos >= 0 && validSingleKey()!=null && deviceETXV.validateText());
                delKeyBTN.setEnabled(pos >= 0);
            }
        };
        keysAPT.setOnItemSelectedListener(keysECL);
        keysRCV = (RecyclerViewEmptySupport)layout.findViewById(R.id.keysRCV);
        keysRCV.setEmptyView(layout.findViewById(R.id.keysEmptyTXV));
        keysRCV.setAdapter(keysAPT);
        keysECL.onItemSelectionChanged(keysAPT.getSelectedIndex(), keysAPT.getSelectedItem(), GenericAdapter.CT_CLICK);

        addKeyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] keys;
                if (deviceETXV.validateText() && (keys = validMultipleKey())!=null) {
                    String dev = deviceETX.getText().toString();
                    for (String k:keys) {
                        if (!k.isEmpty()) {
                            DeviceAllOneKey dk = new DeviceAllOneKey(dev, k);
                            keysAPT.addItem(dk, keysAPT.getItemCount());
                        }
                    }
                    setOKEnabled(true);
                } else {
                    addKeyBTN.setEnabled(false);
                    editKeyBTN.setEnabled(false);
                }
            }
        });

        editKeyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String key = null;
                if (deviceETXV.validateText() && (key = validSingleKey())!=null) {
                    DeviceAllOneKey fromDk = new DeviceAllOneKey(deviceETX.getText().toString(), key);
                    DeviceAllOneKey dk = keysAPT.getSelectedItem();
                    if (dk != null) {
                        dk.set(fromDk);
                        keysAPT.notifySelectedItemChanged();
                    } else {
                        editKeyBTN.setEnabled(false);
                        delKeyBTN.setEnabled(false);
                    }
                } else {
                    editKeyBTN.setEnabled(false);
                    addKeyBTN.setEnabled(false);
                }
            }
        });

        delKeyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                keysAPT.deleteSelected();
                setOKEnabled(keysAPT.getItemCount() > 0);
            }
        });
    }
}

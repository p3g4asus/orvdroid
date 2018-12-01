package com.orvdroid.gui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.orvdroid.gui.R;
import com.orvdroid.lib.message.Device;
import com.orvdroid.workers.ConnectionService;

/**
 * Created by Fujitsu on 01/02/2016.
 */
public abstract class InputDialog extends GenericDialog {
    private static final String INPUTDIALOG_VALUE = "INPUTDIALOG_VALUE.";
    protected EditText valueETX;
    protected TextView valueTXV;
    protected TextView actualTXV;

    protected abstract int getInputType();

    protected abstract boolean validateInput(String txt);

    protected abstract String getActualValue(Device d);

    protected abstract void onOK(String val);

    protected void onRestore(String pf, Bundle b) {
        valueETX.setText(b.getString(INPUTDIALOG_VALUE+pf,""));
    }

    protected void onSave(String pf, Bundle b) {
        b.putString(INPUTDIALOG_VALUE+pf,valueETX.getText().toString());
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.inputalert;
    }

    @Override
    protected void onDismiss(DialogInterface d) {

    }

    @Override
    protected void onShow(DialogInterface d) {
        setOKEnabled(validateInput(valueETX.getText().toString()));
    }

    @Override
    protected void onOK(DialogInterface d) {
        onOK(valueETX.getText().toString());
    }

    public InputDialog(Device d, ConnectionService.ConnectionServiceBinder csb) {
        super(d,csb);
    }

    @Override
    protected void setViewVariables(View layout) {
        actualTXV = (TextView)layout.findViewById(R.id.actualTXV);
        actualTXV.setText(String.format(res.getString(R.string.id_actualvalue),getActualValue(myDev)));
        valueTXV = (TextView)layout.findViewById(R.id.valueTXV);
        valueTXV.setText(getTitleRes());
        valueETX = (EditText)layout.findViewById(R.id.valueETX);
        valueETX.setInputType(getInputType());
        valueETX.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                setOKEnabled(validateInput(s.toString()));
            }
        });

    }
}

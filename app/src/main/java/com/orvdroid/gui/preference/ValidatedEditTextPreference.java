package com.orvdroid.gui.preference;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.EditTextPreference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

public abstract class ValidatedEditTextPreference extends EditTextPreference {
    private Dialog myDLG;
    private EditText myETX;

    public ValidatedEditTextPreference(Context ctx, AttributeSet attrs,
                                       int defStyle) {
        super(ctx, attrs, defStyle);
    }

    public ValidatedEditTextPreference(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
    }

    public ValidatedEditTextPreference(Context ctx) {
        super(ctx);
    }

    public void setup(Dialog d, EditText e) {
        myDLG = d;
        myETX = e;
        e.setInputType(getInputType());
        e.removeTextChangedListener(m_watcher);
        e.addTextChangedListener(m_watcher);
        onEditTextChanged();
    }

    private class EditTextWatcher implements TextWatcher {
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int before,
                                      int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            onEditTextChanged();
        }
    }

    EditTextWatcher m_watcher = new EditTextWatcher();

    protected abstract boolean onCheckValue(String value);

    protected abstract int getInputType();

    /**
     * Return true in order to enable positive button or false to disable it.
     */

    protected void onEditTextChanged() {
        if (myETX != null && myDLG != null) {
            boolean enable = onCheckValue(myETX.getText().toString());
            if (myDLG instanceof AlertDialog) {
                AlertDialog alertDlg = (AlertDialog) myDLG;
                Button btn = alertDlg.getButton(AlertDialog.BUTTON_POSITIVE);
                if (btn!=null)
                    btn.setEnabled(enable);
            }
        }
    }

    protected void onDialogClosed(boolean positiveResult) {

        if (positiveResult) {
            String value = myETX.getText().toString();
            postPositiveClick(value);
        }
    }

    protected void postPositiveClick(String value) {

    }
}
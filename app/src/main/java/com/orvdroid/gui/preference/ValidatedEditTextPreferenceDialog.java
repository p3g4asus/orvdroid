package com.orvdroid.gui.preference;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.EditText;

/**
 * Created by Matteo on 06/01/2018.
 */

public class ValidatedEditTextPreferenceDialog extends EditTextPreferenceDialogFragmentCompat {
    private ValidatedEditTextPreference preference = null;
    private EditText myETX;
    private Dialog myDLG;
    public static ValidatedEditTextPreferenceDialog newInstance(ValidatedEditTextPreference pref) {
        final ValidatedEditTextPreferenceDialog
                fragment = new ValidatedEditTextPreferenceDialog();
        fragment.preference = pref;
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, pref.getKey());
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        myETX = view.findViewById(android.R.id.edit);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        preference.setup(myDLG = getDialog(),myETX);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        preference.onDialogClosed(positiveResult);
    }

}

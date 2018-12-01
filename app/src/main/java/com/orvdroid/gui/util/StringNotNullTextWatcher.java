package com.orvdroid.gui.util;

import android.text.Editable;
import android.widget.EditText;

public abstract class StringNotNullTextWatcher extends ValidatingTextWatcher {
    public StringNotNullTextWatcher(EditText etx, int wrongColor) {
        super(etx, wrongColor);
    }

    public StringNotNullTextWatcher(EditText etx) {
        super(etx);
    }

    public boolean validateText(Editable s) {
        String str;
        return s.length() > 0 &&
                (str = s.toString()).indexOf(':')<0 &&
                str.indexOf('/')<0 && str.indexOf('\\')<0 &&
                str.indexOf(' ')<0 && str.indexOf('\"')<0 &&
                str.indexOf('$')<0;
    }
}

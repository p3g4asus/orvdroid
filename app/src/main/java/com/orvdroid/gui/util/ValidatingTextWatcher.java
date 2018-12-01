package com.orvdroid.gui.util;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public abstract class ValidatingTextWatcher implements TextWatcher {
    protected EditText myETX = null;
    protected Drawable originalDrawable = null, newDrawable = null;
    public final static int DEFAULT_COLOR = Color.RED;

    public ValidatingTextWatcher(EditText etx, int wrongColor) {
        myETX = etx;
        myETX.addTextChangedListener(this);
        originalDrawable = etx.getBackground();
        newDrawable = new ColorDrawable(wrongColor);
    }

    public ValidatingTextWatcher(EditText etx) {
        this(etx,DEFAULT_COLOR);
    }

    public abstract boolean validateText(Editable s);

    public boolean validateText() {
        return validateText(myETX.getText());
    }

    @SuppressLint("NewApi")
    public void afterTextChanged(Editable s) {
        if (s == null)
            s = myETX.getText();
        boolean isOk;
        Drawable d = (isOk = validateText(s)) ? originalDrawable : newDrawable;
        int sdk = android.os.Build.VERSION.SDK_INT;
        int jellyBean = android.os.Build.VERSION_CODES.JELLY_BEAN;
        if (sdk < jellyBean) {
            myETX.setBackgroundDrawable(d);
        } else {
            myETX.setBackground(d);
        }
        textChanged(isOk);
    }

    protected abstract void textChanged(boolean isOk);

    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {
    }
}

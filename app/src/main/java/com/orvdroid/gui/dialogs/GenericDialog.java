package com.orvdroid.gui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.orvdroid.gui.R;
import com.orvdroid.lib.message.Device;
import com.orvdroid.workers.ConnectionService;

/**
 * Created by Fujitsu on 03/02/2016.
 */
public abstract class GenericDialog {
    private String SHOW_DIALOG = "SHOW_DiALOG.";
    protected Device myDev = null;
    protected ConnectionService.ConnectionServiceBinder mBinder = null;
    protected AlertDialog intDialog = null;
    protected Resources res;
    protected Activity activity;
    private Bundle restoreBundle = null;
    private String restorePostfix = "";

    protected abstract void onDismiss(DialogInterface d);
    protected abstract void onOK(DialogInterface d);
    protected abstract void onShow(DialogInterface d);
    protected abstract void setViewVariables(View layout);
    protected abstract int getLayoutRes();
    protected abstract int getTitleRes();

    protected abstract void onRestore(String pf, Bundle b);
    protected abstract void onSave(String pf, Bundle b);

    protected boolean needsNeutral() {
        return false;
    }

    protected void onNeutral(DialogInterface d) {

    }

    public void restore(Activity a,String pf,Bundle b) {
        if (a!=null && b!=null && b.getBoolean(SHOW_DIALOG+pf,false) && !(intDialog!=null && intDialog.isShowing())) {
            restoreBundle = b;
            restorePostfix = pf;
            show(a,true);
        }
        else
            restoreBundle = null;
    }

    public void save(String pf,Bundle b) {
        if (intDialog!=null && intDialog.isShowing()) {
            b.putBoolean(SHOW_DIALOG + pf, true);
            onSave(pf,b);
        }
    }

    protected int getDismissRes() {
        return R.string.ti_discard;
    }
    protected int getNeutralRes() {
        return 0;
    }
    protected int getOKRes() {
        return R.string.ti_ok;
    }

    protected void onInflate(LayoutInflater inflater,View layout) {

    }

    public void dismiss() {
        if (intDialog!=null) {
            intDialog.dismiss();
            onDismiss(intDialog);
            intDialog = null;
        }
    }

    public void setConnectionService(ConnectionService.ConnectionServiceBinder csb) {
        mBinder = csb;
    }

    public void setDevice(Device device) {
        this.myDev = device;
    }

    protected void setOKEnabled(boolean v) {
        if (intDialog!=null)
            intDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(v);
    }

    protected void setNeutralEnabled(boolean v) {
        if (intDialog!=null)
            intDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setEnabled(v);
    }

    public GenericDialog(Device d) {
        myDev = d;
    }

    public GenericDialog(Device d, ConnectionService.ConnectionServiceBinder b) {
        myDev = d;
        mBinder = b;
    }

    public void show(Activity a) {
        show(a,false);
    }

    protected void show(Activity a,boolean restore) {
        if (!restore)
            restoreBundle = null;
        activity = a;
        res = a.getResources();
        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        // Get the layout inflater
        LayoutInflater inflater = a.getLayoutInflater();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog
        // layout
        final View layout = inflater.inflate(getLayoutRes(), null);
        onInflate(inflater,layout);
        setViewVariables(layout);
        builder.setView(layout);
        intDialog = builder.create();
        intDialog.setTitle(res.getString(getTitleRes()));
        intDialog.setButton(AlertDialog.BUTTON_NEGATIVE, res.getString(getDismissRes()),// sett
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        if (needsNeutral())
            intDialog.setButton(AlertDialog.BUTTON_NEUTRAL, res.getString(getNeutralRes()),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            intDialog.dismiss();
                            intDialog = null;
                            onNeutral(dialog);
                        }
                    });

        intDialog.setButton(AlertDialog.BUTTON_POSITIVE, res.getString(getOKRes()),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        intDialog.dismiss();
                        intDialog = null;
                        onOK(dialog);
                    }
                });
        intDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                if (restoreBundle!=null)
                    onRestore(restorePostfix,restoreBundle);
                else
                    GenericDialog.this.onShow(dialog);
            }
        });
        intDialog.show();
    }
}

package com.orvdroid.gui.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orvdroid.gui.dialogs.TimerDialog;
import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.IBaseMessage;
import com.orvdroid.lib.message.RemoteMessage;
import com.orvdroid.lib.utils.CommandProcessor;
import com.orvdroid.workers.ConnectionService;

/**
 * Created by Fujitsu on 03/02/2016.
 */
public abstract class DeviceFragment extends Fragment implements Refreshable,CommandProcessor {
    private DeviceCommandProcessor devProc = null;
    protected DeviceServiceConnection mServiceConnection = null;
    protected ConnectionService.ConnectionServiceBinder mBinder = null;
    protected Context ctx;
    protected Device myDev = null;
    protected Device oldDev = null;
    protected TimerDialog timerDialog = null;
    private SharedPreferences sharedPref;

    protected abstract void onRestore(Bundle b);
    protected abstract void onSave(Bundle b);
    protected abstract void onServiceDisconnect();
    protected abstract int getLayoutRes();

    protected Intent createShortcutIntent(RemoteMessage msg) {
        try {
            String tp = sharedPref.getString("pref_sh_type","UDP");
            if (tp.equals("UDP")) {
                String ip = sharedPref.getString("pref_conn_ip", "");
                if (ip.length() > 0) {
                    int port = Integer.parseInt(sharedPref.getString("pref_conn_udp_port", ""));
                    Uri uri = Uri.parse("udp://" + ip + ":" + port + "/" + Uri.encode(msg.getCommand()));
                    Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);

                    return intent;
                }
            } else {
                Uri uri = Uri.parse("orvcmd://a/" + Uri.encode(msg.dumpCommand('/')).replace("%2F","/"));
                Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
                intent.addCategory(Intent.CATEGORY_DEFAULT);

                return intent;
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void shortcutAddOld(String name, RemoteMessage command, int ico) {
        // Intent to be send, when shortcut is pressed by user ("launched")
        Intent shIntent = createShortcutIntent(command);

        // Decorate the shortcut
        if (shIntent!=null) {
            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(ctx.getResources(), ico));

            // Inform launcher to create shortcut
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            ctx.getApplicationContext().sendBroadcast(addIntent);
        }
    }

    public void shortcutAdd(String name, RemoteMessage command, int ico) {
        // Intent to be send, when shortcut is pressed by user ("launched")
        Intent shIntent = createShortcutIntent(command);
        ShortcutInfoCompat pinShortcutInfo =
                new ShortcutInfoCompat.Builder(ctx, name)
                        .setIcon(IconCompat.createWithBitmap(BitmapFactory.decodeResource(ctx.getResources(), ico)))
                        .setShortLabel(name)
                        .setIntent(shIntent)
                        .build();

        ShortcutManagerCompat.requestPinShortcut(ctx, pinShortcutInfo, null);

        // Decorate the shortcut
        if (shIntent!=null) {
            Intent addIntent = new Intent();
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shIntent);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);
            addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, BitmapFactory.decodeResource(ctx.getResources(), ico));

            // Inform launcher to create shortcut
            addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            ctx.getApplicationContext().sendBroadcast(addIntent);
        }
    }

    public void shortcutDel(String name,RemoteMessage command) {
        // Intent to be send, when shortcut is pressed by user ("launched")
        Intent shIntent = createShortcutIntent(command);

        // Decorate the shortcut
        if (shIntent!=null) {
            // Decorate the shortcut
            Intent delIntent = new Intent();
            delIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shIntent);
            delIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, name);

            // Inform launcher to remove shortcut
            delIntent.setAction("com.android.launcher.action.UNINSTALL_SHORTCUT");
            ctx.getApplicationContext().sendBroadcast(delIntent);
        }
    }

    @Override
    public boolean processCommand(final IBaseMessage hs2) {
        Activity a = getActivity();
        if (a!=null) {
            a.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (devProc!=null)
                        devProc.processCommand(hs2);
                }
            });
            return true;
        }
        else
            return false;
    }

    private class DeviceServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (ConnectionService.ConnectionServiceBinder) service;
            mBinder.addCommandProcessor(DeviceFragment.this);
            if (oldDev!=null) {
                String mac = oldDev.getMac();
                Bundle b = mBinder.getDeviceBundle(mac);
                if (b!=null) {
                    onRestore(b);
                    if (timerDialog!=null)
                        timerDialog.restore(getActivity(),"timerDialog",b);
                }
                refresh(mBinder.getDevice(mac), null);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            try {
                ctx.unbindService(mServiceConnection);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mServiceConnection = null;
            mBinder = null;
            if (timerDialog!=null)
                timerDialog.setConnectionService(null);
            onServiceDisconnect();
        }

    }

    @Override
    public void onPause() {
        if (mServiceConnection != null) {
            try {
                if (mBinder != null) {
                    if (oldDev!=null) {
                        Bundle b = new Bundle();
                        onSave(b);
                        if (timerDialog!=null)
                            timerDialog.save("timerDialog",b);
                        mBinder.putDeviceBundle(oldDev.getMac(), b);
                    }
                    mBinder.removeCommandProcessor(this);
                }
                ctx.unbindService(mServiceConnection);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mServiceConnection = null;
            mBinder = null;
            if (timerDialog!=null)
                timerDialog.setConnectionService(null);
            onServiceDisconnect();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mServiceConnection == null) {
            mServiceConnection = new DeviceServiceConnection();
            ctx.bindService(new Intent(ctx, ConnectionService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        ctx = getActivity().getApplicationContext();
        Bundle args = getArguments();
        oldDev = myDev = args.getParcelable("dev");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        devProc = new DeviceCommandProcessor(oldDev,this);
        View view = inflater.inflate(getLayoutRes(), container, false);
        return view;
    }

}

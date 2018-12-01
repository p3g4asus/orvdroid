package com.orvdroid.gui.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;

import com.orvdroid.lib.message.ExitMessage;
import com.orvdroid.lib.message.IBaseMessage;
import com.orvdroid.lib.message.RemoteMessage;
import com.orvdroid.lib.message.SentMessage;
import com.orvdroid.lib.utils.CommandProcessor;
import com.orvdroid.workers.ConnectionService;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActivitySendTo extends Activity implements CommandProcessor {

    private ConnectionService.ConnectionServiceBinder mBinder = null;
    private ServiceConnection mServiceConnection = null;
    private RemoteMessage sentMessage = null;
    private boolean killService = false,settingsChange = false;
    public static final String TAG = "ActivitySendTo";

    private void intStartService() {
        if (!ConnectionService.isAlive()) {
            Intent gattServiceIntent = new Intent(this, ConnectionService.class);
            startService(gattServiceIntent);
            killService = true;
            Log.v(TAG,"Starting service");
        }
        else
            initServiceConnection();
    }

    @Override
    public boolean processCommand(IBaseMessage hs2) {
        if (hs2 instanceof SentMessage) {
            SentMessage sm = (SentMessage)hs2;
            if (sentMessage!=null && sm.isMessage(sentMessage)) {
                if (mBinder!=null && killService)
                    mBinder.processCommand(new ExitMessage());
                Log.v(TAG,"Received confirmation: exiting");
                finish();
                return true;
            }
        }
        return false;
    }

    private class DeviceManagerConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBinder = (ConnectionService.ConnectionServiceBinder) service;
            mBinder.addCommandProcessor(ActivitySendTo.this);
            if (sentMessage!=null) {
                mBinder.write(sentMessage, true);
                Log.v(TAG, "Sending " + sentMessage);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
			/*if (mChars!=null) {
				for (BluetoothGattCharacteristic gattCharacteristic : mChars)
					mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, false);
			}*/
            //intDisconnect();
        }

    }

    private void initServiceConnection() {
        if (mServiceConnection==null && !settingsChange) {
            Log.v(TAG,"Connecting to service");
            mServiceConnection = new DeviceManagerConnection();
            bindService(new Intent(this, ConnectionService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initServiceConnection();
    }

    @Override
    protected void onPause() {
        if (mServiceConnection!=null) {
            if (mBinder!=null) {
                mBinder.removeCommandProcessor(this);
                mBinder = null;
            }
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        final Uri uri = intent.getData();
        if (uri!=null) {
            List<String> sgm = uri.getPathSegments();
            String scheme = uri.getScheme();
            if (scheme.equals("orvstg")) {
                settingsChange = true;

                Pattern r = Pattern.compile("([^=]+)=([^$]+)");
                Matcher m;
                String key, val;
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = null;
                for (String s : sgm) {
                    m = r.matcher(s);
                    if (m.find()) {
                        key = m.group(1).trim();
                        val = m.group(2).trim();
                        if (editor == null)
                            editor = sharedPref.edit();
                        editor.putString(key, val);
                    }
                }
                if (editor != null)
                    editor.commit();
            } else if (scheme.equals("orvcmd")){
                sentMessage = RemoteMessage.fromSegments(sgm);
                Log.v(TAG, "SentMSG is " + sentMessage);
                if (sentMessage != null) {
                    intStartService();
                    return;
                }
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }
}

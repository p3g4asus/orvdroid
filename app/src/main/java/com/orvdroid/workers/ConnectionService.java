package com.orvdroid.workers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.orvdroid.gui.R;
import com.orvdroid.gui.app.CA;
import com.orvdroid.gui.util.Messages;
import com.orvdroid.lib.message.ConnectionStatusMessage;
import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.ExitMessage;
import com.orvdroid.lib.message.DevicedlMessage;
import com.orvdroid.lib.message.InfoMessage;
import com.orvdroid.lib.message.IBaseMessage;
import com.orvdroid.lib.message.PingMessage;
import com.orvdroid.lib.message.RemoteMessage;
import com.orvdroid.lib.message.SentMessage;
import com.orvdroid.lib.utils.CommandProcessor;
import com.orvdroid.lib.utils.ParcelableMessage;
import com.orvdroid.lib.utils.ParcelableUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Fujitsu on 19/01/2016.
 */
public class ConnectionService extends Service {
    public static String CONN_INVALIDPORT_ID = "exm_errs_conn_invalidport";
    public static String CONN_INVALIDIP_ID = "exm_errs_conn_invalidip";
    public static String CONN_CONNECTIONERROR_ID = "exm_errr_conn_connectionerror";
    public static String CONN_UNKNOWNIP_ID = "exm_errr_conn_connectionerror";
    public static String CONN_CONNECTING_ID = "exm_errr_conn_unknownip";
    public static String CONN_CONNECTIONLOST_ID = "exm_errr_conn_connectionlost";
    private static final String TAG = ConnectionService.class.getSimpleName();
    private SharedPreferences sharedPref;
    private String ip;
    private short port;
    private int connRetryDelay,infoPeriod;
    private boolean reloadSettings = true;
    private long connectingStatusT = 0;
    private Intent connectingStatus = null;
    private CommandReceiver commandReceiver = new CommandReceiver();
    private TCPClient tcpClient = null;
    private List<String> deviceIdList = Collections.synchronizedList(new ArrayList<String>());
    private ConnectionServiceBinder mBinder = null;
    private static boolean alive = false;

    public static boolean isAlive() {
        return alive;
    }

    private class TCPClient extends Thread {

        private String serverMessage;
        private boolean mRun = false;
        private WriteThread writeThread = null;

        private PrintWriter out = null;
        private BufferedReader in = null;
        private Socket socket = null;
        private ArrayList<RemoteMessage> sendingList = new ArrayList<>();
        private ConcurrentHashMap<Integer,RemoteMessage> waitingMap = new ConcurrentHashMap<Integer,RemoteMessage>();
        private Timer infoTimer = null,pingCheck = null;
        private long lastPing = 0;
        private RemoteMessage onConnectMsg = null;


        public void write(RemoteMessage r) {
            synchronized (sendingList) {
                sendingList.add(r);
                sendingList.notifyAll();
            }
        }

        public void ping() {
            lastPing = System.currentTimeMillis();
            //write(new PingresponseMessage());
        }

        public boolean isConnected() {
            return socket!=null && in!=null && out!=null && socket.isConnected();
        }

        public void setOnConnectMsg(RemoteMessage onConnectMsg) {
            this.onConnectMsg = onConnectMsg;
        }

        private class WriteThread extends Thread {
            @Override
            public void run() {
                RemoteMessage r;
                while (mRun) {
                    r = null;
                    synchronized (sendingList) {
                        if (sendingList.size()>0) {
                            r = sendingList.remove(0);
                        }
                        else {
                            try {
                                sendingList.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                mRun = false;
                            }
                        }
                    }
                    if (mRun && r!=null) {
                        if (r instanceof InfoMessage) {
                            stopInfoTimer();
                        }

                        try {
                            if (out!=null) {
                                out.println(r.getCommand());
                                Log.e(TAG, "S: RemMSG " + r + " cls=" + r.getClass().getSimpleName());
                                mBinder.notifyCommand(new SentMessage(r));
                                synchronized (waitingMap) {
                                    waitingMap.put(r.getRandomId(), r);
                                }
                            }
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        //timer per check keepalive

        /**
         * Constructor of the class. OnMessagedReceived listens for the messages received from server
         */
        public TCPClient() {
        }


        public void stopClient() {
            mRun = false;
            try {
                if (socket!=null)
                    socket.close();
                if (in!=null)
                    in.close();
                if (out!=null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (tcpClient!=null) {
                try {
                    tcpClient.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            synchronized (sendingList) {
                sendingList.notifyAll();
            }
        }

        public void stopInfoTimer() {
            if (infoTimer!=null) {
                infoTimer.cancel();
                infoTimer = null;
            }
        }

        public void initInfoTimer() {
            if (infoTimer==null) {
                infoTimer = new Timer();
                infoTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (!mRun)
                            cancel();
                        else if (in != null && out != null && socket != null)
                            write(new DevicedlMessage());
                        infoTimer = null;
                    }
                }, infoPeriod * 1000);
            }
        }

        @Override
        public void run() {

            mRun = true;
            writeThread = new WriteThread();
            writeThread.start();
            InetAddress serverAddr = null;
            RemoteMessage remmsg = null;
            reloadSettings();

            while (mRun) {
                try {
                    serverAddr = InetAddress.getByName(ip);

                    Log.e("TCP Client", "C: Connecting...");
                    //create a socket to make the connection with the server
                    socket = null;
                    try {
                        notifyConnecting();
                        socket = new Socket(serverAddr, port);
                        //send the message to the server
                        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);

                        Log.e("TCP Client", "C: Sent.");

                        Log.e("TCP Client", "C: Done.");

                        //receive the message which the server sends back
                        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE));
                    }
                    catch (IOException ioe) {
                        ioe.printStackTrace();
                        if (mRun)
                            setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) new ParcelableMessage("exm_errr_conn_connectionerror").put(ip).put(port).put(connRetryDelay)));
                    }

                    //in this while the client listens for the messages sent by the server
                    if (in!=null && out!=null) {
                        if (onConnectMsg==null) {
                            write(new DevicedlMessage());
                            pingCheck = new Timer();
                            pingCheck.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (System.currentTimeMillis() - lastPing >= 90000) {
                                        Log.e(TAG, "Closing connection by pingCheck");
                                        try {
                                            if (socket != null)
                                                socket.close();
                                            if (in != null)
                                                in.close();
                                            if (out != null)
                                                out.close();
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                            }, 120000, 120000);
                            ping();
                        }
                        else
                            write(onConnectMsg);
                        while (mRun) {
                            serverMessage = in.readLine();

                            if (serverMessage != null) {
                                //call the method messageReceived from MyActivity class
                                Log.e(TAG, "R: " + serverMessage);
                                serverMessage = serverMessage.trim();
                                try {
                                    JSONObject obj = new JSONObject(serverMessage);
                                    JSONObject actionobj = obj.getJSONObject("action");
                                    int rid = actionobj.getInt("randomid");
                                    Integer retval = null;
                                    try {
                                        retval = obj.getInt("retval");
                                    }
                                    catch (JSONException je) {
                                    }
                                    synchronized (waitingMap) {
                                        if (retval==null || retval>0)
                                            remmsg = waitingMap.remove(rid);
                                        else
                                            remmsg = waitingMap.get(rid);
                                    }
                                    if (remmsg==null)
                                        remmsg = RemoteMessage.fromResponse(obj);
                                    else
                                        Log.e(TAG,"RemMSG found waiting");
                                    if (remmsg!=null) {
                                        Log.e(TAG,"RemMSG "+remmsg+" cls="+remmsg.getClass().getSimpleName());
                                        remmsg.setResponse(obj);
                                        mBinder.notifyCommand(remmsg);
                                    }
                                }
                                catch (JSONException je) {
                                    je.printStackTrace();
                                    Log.e(TAG,"Invalid json "+serverMessage);
                                }

                            } else
                                throw new EOFException("serverMessage = null");

                        }
                    }
                } catch (UnknownHostException uhe) {
                    if (mRun)
                        setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) new ParcelableMessage("exm_errr_conn_unknownip").put(ip).put(port).put(connRetryDelay)));

                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("TCP", "S: Error", e);
                    if (mRun)
                        setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) new ParcelableMessage("exm_errr_conn_connectionlost").put(ip).put(port).put(connRetryDelay)));

                } finally {
                    //the socket must be closed. It is not possible to reconnect to this socket
                    // after it is closed, which means a new socket instance has to be created.
                    in = null;
                    out = null;
                    if (pingCheck!=null) {
                        pingCheck.cancel();
                        pingCheck = null;
                    }
                    try {
                        if (socket!=null) {
                            socket.close();
                            socket = null;
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (mRun) {
                        try {
                            Thread.sleep(connRetryDelay*1000);
                            if (reloadSettings) {
                                stopInfoTimer();
                                reloadSettings();
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            mRun = false;
                        }
                    }
                }
            }
            stopInfoTimer();
            tcpClient = null;
        }
    }

    private class CommandReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getAction();
            if (msg.equals(Messages.CMDGETCONSTATUS_MESSAGE)) {
                long timeIb = intent.getLongExtra("t", 0);
                if (connectingStatus!=null && timeIb<=connectingStatusT)
                    CA.lbm.sendBroadcast(connectingStatus.putExtra("response", true));
            }
        }
    }

    private void setupIO(boolean start) {
        if (start) {
            IntentFilter intentf = new IntentFilter();
            intentf.addAction(Messages.CMDGETCONSTATUS_MESSAGE);
            //intentf.addAction(Messages.CMDPLTPROCESSOR_MESSAGE);
            CA.lbm.registerReceiver(commandReceiver, intentf);
        }
        else {
            try {
                CA.lbm.unregisterReceiver(commandReceiver);
            } catch (Exception e) {
            }
        }
    }

    public class ConnectionServiceBinder extends Binder implements CommandProcessor {
        private List<CommandProcessor> mCommandProcessors = new Vector<>();
        private HashMap<String,Bundle> dBundleMap = new HashMap<>();

        private int logBundleSize(String d, Parcelable b) {
            Parcel pp = Parcel.obtain();
            pp.writeValue(b);
            byte[] tmp = pp.marshall();
            pp.recycle();
            Log.i(TAG,"Bundle "+d+" size is "+tmp.length+" Bytes");
            return tmp.length;
        }

        public void putDeviceBundle(String mac,Bundle b) {
            logBundleSize(mac,b);
            dBundleMap.put(mac,b);
        }

        public Bundle getDeviceBundle(String mac) {
            return dBundleMap.remove(mac);
        }

        private void notifyCommand(IBaseMessage b) {
            int i = 0;
            CommandProcessor c = null;
            while (true) {
                synchronized (mCommandProcessors) {
                    if (i<mCommandProcessors.size()) {
                        c = mCommandProcessors.get(i);
                        i++;
                    }
                }
                if (c!=null) {
                    c.processCommand(b);
                    c = null;
                }
                else
                    break;
            }
        }

        public void write(RemoteMessage m) {
            write(m,false);
        }

        public void write(RemoteMessage m, boolean force) {
            if (force && (tcpClient == null || !tcpClient.isConnected())) {
                tcpClient.setOnConnectMsg(m);
            }
            else if (tcpClient != null && tcpClient.isConnected())
                tcpClient.write(m);
        }

        public void addCommandProcessor(CommandProcessor c) {
            synchronized (mCommandProcessors) {
                mCommandProcessors.add(c);
            }
        }

        public List<String> getDeviceIdList() {
            if (tcpClient!=null && tcpClient.isConnected())
                return deviceIdList;
            else
                return null;
        }

        public void removeCommandProcessor(CommandProcessor c) {
            synchronized (mCommandProcessors) {
                mCommandProcessors.remove(c);
            }
        }

        public void stop() {
            processCommand(new ExitMessage());
        }

        public void clearDeviceIdList() {
            SharedPreferences.Editor e = sharedPref.edit();
            synchronized (deviceIdList) {
                for (String i : deviceIdList)
                    e.remove("dev_" + i);
                e.commit();
                deviceIdList.clear();
            }
        }

        @Override
        public boolean processCommand(IBaseMessage hs2) {
            if (hs2 instanceof ExitMessage) {
                ConnectionService.this.stop();
                return true;
            }
            else if (hs2 instanceof PingMessage) {
                if (tcpClient!=null)
                    tcpClient.ping();
                return true;
            }
            else if (hs2 instanceof InfoMessage) {
                InfoMessage gim = (InfoMessage) hs2;
                ArrayList<Device> devl = gim.getDeviceList();
                if (devl!=null) {
                    clearDeviceIdList();
                    SharedPreferences.Editor ed = sharedPref.edit();
                    boolean empty = true;
                    String id;
                    for (Device d:devl) {
                        logBundleSize("dev_"+d.getMac(),d);
                        ParcelableUtil.marshallToSharedPref(d,"dev_"+d.getMac(),ed);
                        empty = false;
                        synchronized (deviceIdList) {
                            if (!deviceIdList.contains(id = d.getMac()))
                                deviceIdList.add(id);
                        }
                    }
                    ed.commit();
                    if (empty && tcpClient!=null && tcpClient.isConnected()) {
                        tcpClient.initInfoTimer();
                    }
                }
                return true;
            }
            else if (hs2 instanceof RemoteMessage) {
                RemoteMessage rm = (RemoteMessage) hs2;
                Device d;
                if ((d = rm.isDeviceModified()) != null) {
                    String id = d.getMac();
                    logBundleSize("dev_"+id,d);
                    SharedPreferences.Editor ed = sharedPref.edit();
                    ParcelableUtil.marshallToSharedPref(d,"dev_"+id,ed);
                    synchronized (deviceIdList) {
                        if (!deviceIdList.contains(id))
                            deviceIdList.add(id);
                    }
                    ed.commit();
                    if (d.isOffline() && tcpClient!=null && tcpClient.isConnected()) {
                        tcpClient.initInfoTimer();
                    }
                    return true;
                }
            }
            return false;
        }

        public Device getDevice(String mac) {
            if (tcpClient!=null && tcpClient.isConnected())
                return ParcelableUtil.unmarshallFromSharedPref(Device.CREATOR,"dev_"+mac,sharedPref);
            else
                return null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private Intent setConnectingStatus(Intent newcs) {
        connectingStatus = newcs;
        connectingStatusT = System.currentTimeMillis();
        CA.lbm.sendBroadcast(newcs);
        mBinder.notifyCommand(new ConnectionStatusMessage(newcs));
        return newcs;
    }

    private boolean reloadSettings() {
        try {
            connRetryDelay = Integer.parseInt(sharedPref.getString("pref_conn_retrydelay", "30"));
            if (connRetryDelay < 5)
                connRetryDelay = 5;
        }
        catch (Exception e) {
            connRetryDelay = 30;
        }
        try {
            infoPeriod = Integer.parseInt(sharedPref.getString("pref_conn_infoperiod", "120"));
            if (infoPeriod < 60)
                infoPeriod = 60;
        }
        catch (Exception e) {
            infoPeriod = 120;
        }
        ip = sharedPref.getString("pref_conn_ip", "").trim();
        if (ip.length()==0) {
            setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) new ParcelableMessage("exm_errs_conn_invalidip").put(ip)));
            return false;
        }
        String portS = sharedPref.getString("pref_conn_port", "2802");
        try {
            port = Short.parseShort(portS);
        }
        catch(Exception ex) {
            setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) new ParcelableMessage("exm_errs_conn_invalidport").put(portS)));
            return false;
        }
        reloadSettings = false;
        return true;
    }

    private void showNotification() {
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, ConnectionService.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_manager).setContentTitle("ConnectionService start")
                .setContentText("ConnectionService start")
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis()).build();
        nm.notify(R.drawable.ic_stat_manager, notification);
    }

    private void removeNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(R.drawable.ic_stat_manager);
    }

    public void stop() {
        if (tcpClient!=null)
            tcpClient.stopClient();
        removeNotification();
        Log.i(TAG, "Service stopped");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
        alive = false;
    }

    private SharedPreferences.OnSharedPreferenceChangeListener sharedPrefChange = new SharedPreferences.OnSharedPreferenceChangeListener() {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {
            if (key.startsWith("pref_conn_"))
                reloadSettings = true;
        }
    };

    private void notifyConnecting() {
        ParcelableMessage connexc = new ParcelableMessage("exm_errp_conn_connecting")
                .put(ip).put(port).setType(ParcelableMessage.Type.OK);
        setConnectingStatus(new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) connexc));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (tcpClient==null) {
            tcpClient = new TCPClient();
            tcpClient.start();
        }
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        alive = true;
        Log.i(TAG, "Service Created.");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(sharedPrefChange);
        showNotification();
        setupIO(true);
        tcpClient = new TCPClient();
        tcpClient.start();
        mBinder = new ConnectionServiceBinder();
        mBinder.addCommandProcessor(mBinder);
    }
}

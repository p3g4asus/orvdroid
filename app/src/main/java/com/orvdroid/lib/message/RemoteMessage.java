package com.orvdroid.lib.message;

import android.content.Intent;
import android.os.Parcelable;
import android.util.Log;

import com.orvdroid.gui.util.Messages;
import com.orvdroid.lib.utils.ParcelableMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Fujitsu on 20/01/2016.
 */

public class RemoteMessage implements IBaseMessage {
    public static final String DEFAULT_MSG_ID = "exm_errr_remcmdrv_remotemessage";
    public static final int MSG_IDX_RV = 1;
    public static final int MSG_IDX_DEVICE_NAME = 2;
    public static final int MSG_IDX_DEVICE = 3;
    private static Object sync = new Object();
    private static int commandId = 0;
    private static int newId() {
        int c;
        synchronized (sync) {
            c = commandId++;
        }
        return c;
    }

    private Device innerDevice = null;
    private int my = -1;
    private String command = getClass().getSimpleName().toLowerCase().replace("message","");

    private ArrayList<String> commandParams = new ArrayList<String>();
    protected JSONObject commandResponse = null;

    private String actionClass = "";

    protected String getMessageId() {
        return "exm_errr_remcmdrv_"+getActionClass().toLowerCase();
    }

    public String getActionClass() {
        if (actionClass.isEmpty() && commandResponse!=null) {
            try {
                JSONObject obj = commandResponse.getJSONObject("action");
                actionClass = obj.getString("actionclass");
            }
            catch (JSONException je) {
                je.printStackTrace();
            }
        }
        return actionClass;
    }

    @Override
    public String toString() {
        return getActionClass()+" "+my;
    }

    public RemoteMessage(int id) {
        my = id;
    }

    public RemoteMessage(Device d) {
        this();
        innerDevice = d;
    }

    public RemoteMessage() {
        my = RemoteMessage.newId();
    }

    public static RemoteMessage fromResponse(JSONObject resp) {
        try {
            JSONObject obj = resp.getJSONObject("action");
            String cln = obj.getString("actionclass");
            int myid = obj.getInt("randomid");
            RemoteMessage remmsg;
            try {
                cln = cln.substring(6);
                Class<? extends RemoteMessage> cls = (Class<? extends RemoteMessage>) Class.forName("com.orvdroid.lib.message." + cln + "Message");
                remmsg = cls.newInstance();
            }
            catch (Exception ex) {
                remmsg = new RemoteMessage();
                ex.printStackTrace();
            }
            remmsg.my = myid;
            remmsg.setResponse(resp);
            return remmsg;
        }
        catch (JSONException je) {
            je.printStackTrace();
            return null;
        }

    }


    public boolean responseOK() {
        Integer rv = getRV();
        return rv!=null && rv==1;
    }

    protected ParcelableMessage.Type getMessageType() {
        Integer rv = getRV();
        if (rv==null)
            return ParcelableMessage.Type.ERROR;
        else if (rv==1)
            return ParcelableMessage.Type.OK;
        else
            return ParcelableMessage.Type.WARINIG;
    }

    public Device getDeviceFromResponse() {
        if (commandResponse!=null) {
            JSONObject o = getResponseO("action","device");
            if (o!=null) {
                return innerDevice = Device.parse(o);
            }
        }
        return null;
    }

    public Intent response2Message() {
        if (commandResponse!=null) {
            try {
                JSONObject obj = commandResponse.getJSONObject("action");
                String actcls = getActionClass();
                Integer rv = getRV();
                Device d = getDeviceFromResponse();
                ParcelableMessage msg = new ParcelableMessage(DEFAULT_MSG_ID).setType(getMessageType());
                msg.put(actcls);
                msg.put(rv==null?-1:(int)rv);
                if (d==null) {
                    msg.put("<?>");
                    msg.put((Parcelable)null);
                }
                else {
                    msg.put(d.getName());
                    msg.put(d);
                }
                return new Intent(Messages.EXCEPTION_MESSAGE).putExtra("except0", (Parcelable) msg);
            }
            catch (JSONException je) {
                je.printStackTrace();
            }
        }
        return null;
    }

    public String dumpCommand(char c) {
        String rv = command;
        if (innerDevice!=null)
            rv+=c+innerDevice.getName();
        for (String p:commandParams)
            rv+=c+p;
        return rv;
    }

    public String getCommand() {
        return "@"+my+" "+dumpCommand(' ');
    }

    public JSONObject getResponseO(String... args) {
        if (commandResponse==null)
            return null;
        JSONObject rv = commandResponse;
        try {
            for (String a : args) {
                rv = rv.getJSONObject(a);
            }
            return rv;
        }
        catch (JSONException je) {
            return null;
        }
    }

    public JSONArray getResponseA(String... args) {
        if (commandResponse==null)
            return null;
        JSONObject rv = commandResponse;
        try {
            for (int i = 0; i<args.length; i++) {
                if (i==args.length-1)
                    return rv.getJSONArray(args[i]);
                else
                    rv = rv.getJSONObject(args[i]);
            }
            return null;
        }
        catch (JSONException je) {
            return null;
        }
    }

    public void setResponse(JSONObject obj) {
        commandResponse = obj;
    }

    public JSONObject getResponse() {
        return commandResponse;
    }

    public int getRandomId() {
        return my;
    }

    public Device isDeviceModified() {
        Device d = null;
        if (commandResponse!=null) {
            try {
                if (responseOK()) {
                    JSONObject obj = commandResponse.getJSONObject("action");
                    d = obj.getInt("dev") != 0 ? getDeviceFromResponse() : null;
                }
                else {
                    d = getDeviceFromResponse();
                    d = d!=null && d.isOffline()?d:null;
                }
            }
            catch (JSONException je) {
                je.printStackTrace();
                d = null;
            }
        }
        Log.e("RemoteMsg","dev modded? ["+getActionClass()+"] "+d+" "+(d==null?"_":d.isOffline()));
        return d;
    }


    public Integer getRV() {
        if (commandResponse==null)
            return null;
        else {
            try {
                return commandResponse.getInt("retval");
            }
            catch (JSONException je) {
                return null;
            }
        }
    }

    public RemoteMessage putParam(String p) {
        if (p!=null && !p.isEmpty())
            commandParams.add(p);
        return this;
    }

    public String getParam(int i) {
        if (i<commandParams.size())
            return commandParams.get(i);
        else
            return null;
    }

    public static RemoteMessage fromSegments(List<String> sgm) {
        if (sgm.size()>1) {
            String cln;
            try {
                cln = sgm.get(0);
                cln = Character.toString(cln.charAt(0)).toUpperCase()+cln.substring(1);
                Class<? extends RemoteMessage> cls = (Class<? extends RemoteMessage>) Class.forName("com.orvdroid.lib.message." + cln + "Message");
                RemoteMessage remmsg = cls.newInstance();
                for (int i = 1; i < sgm.size(); i++) {
                    remmsg.putParam(sgm.get(i));
                }
                return remmsg;
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
}

package com.orvdroid.lib.utils;

import java.lang.reflect.Field;
import java.util.Map;

import com.orvdroid.gui.R;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableMessage extends Exception implements Parcelable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -540177181433476894L;
	protected String id;
	protected Bundle params = new Bundle();
	protected int nArgs = 0;
    protected Type msgType = Type.ERROR;

    public ParcelableMessage setType(Type t) {
        msgType = t;
        return this;
    }

    public Type getMsgType() {
        return msgType;
    }

	public void setId(String id) {
		this.id = id;
	}

	public static enum Type {
        ERROR,
        WARINIG,
        OK
    }
	
	public String getId() {
		return id;
	}

	public ParcelableMessage(String i) {
		id = i;
	}

	public ParcelableMessage(Parcel parcel) {
		id = parcel.readString();
		nArgs = parcel.readInt();
        msgType = (Type) parcel.readSerializable();
		params = parcel.readBundle();
	}

	public String getString(int idx) {
        return params.getString(idx+"",null);
    }

    public int getInt(int idx) {
        return params.getInt(idx + "", Integer.MIN_VALUE);
    }

    public double getDouble(int idx) {
        return params.getDouble(idx + "", Double.MIN_VALUE);
    }

    public Parcelable getParcelable(int idx) {
        return params.getParcelable(idx + "");
    }
	
	public ParcelableMessage put(String value) {
		params.putString(nArgs+"", value);
		nArgs++;
		return this;
	}
	
	public ParcelableMessage put(int value) {
		params.putInt(nArgs + "", value);
		nArgs++;
		return this;
	}
	
	public ParcelableMessage put(double value) {
		params.putDouble(nArgs + "", value);
		nArgs++;
		return this;
	}
	
	public ParcelableMessage put(Parcelable value) {
		params.putParcelable(nArgs + "", value);
		nArgs++;
		return this;
	}

	@Override
	public String toString() {
		return getMessage(null,null);
	}
	
	public String getMessage(Map<String,Integer> resMap,Resources res) {
		String base = "";
		Integer resid;
		if (resMap!=null && (resid = resMap.get(id))!=null)
			base = res.getString(resid);
        else if (res==null)
            base = id;
		else {
			try {
				Field f = R.string.class.getField(id);
				base = res.getString(f.getInt(null));
			} catch (Exception e) {
				e.printStackTrace();
				base = "";
			}
			
		}
		Object[] arr = new Object[nArgs];
		for (int i = 0; i<nArgs; i++) {
			arr[i] = params.get(i+"");
            if (arr[i] instanceof ParcelableMessage)
                arr[i] = ((ParcelableMessage)arr[i]).getMessage(resMap,res);
		}
		return String.format(base, arr);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeInt(nArgs);
        dest.writeSerializable(msgType);
		dest.writeBundle(params);

	}
	
	public static final Creator<ParcelableMessage> CREATOR = new Creator<ParcelableMessage>() {
        @Override
        public ParcelableMessage createFromParcel(Parcel parcel) {
            return new ParcelableMessage(parcel);
        }

        @Override
        public ParcelableMessage[] newArray(int i) {
            return new ParcelableMessage[i];
        }
    };

}

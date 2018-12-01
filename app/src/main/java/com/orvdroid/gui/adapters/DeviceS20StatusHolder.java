package com.orvdroid.gui.adapters;

import android.util.Pair;
import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.orvdroid.gui.R;
import com.orvdroid.gui.fragments.StateHolder;

/**
 * Created by Fujitsu on 28/06/2016.
 */
public class DeviceS20StatusHolder  extends GenericViewHolder<StateHolder> {
    private TextView statusTXV;
    private int iconOn;
    private int iconOff;

    @Override
    public GenericViewHolder newInstance(View itemView,SelectedItemManager selM,MenuInflater minf) {
        DeviceS20StatusHolder rv = (DeviceS20StatusHolder) super.newInstance(itemView,selM,minf);
        if (rv!=null) {
            rv.iconOff = iconOff;
            rv.iconOn = iconOn;
        }
        return rv;
    }

    public DeviceS20StatusHolder(View itemView,SelectedItemManager selM,MenuInflater minf) {
        super(itemView, selM, minf);
        statusTXV = itemView.findViewById(R.id.statusTXV);
    }

    public DeviceS20StatusHolder(View itemView,SelectedItemManager selM,int iconOn,int iconOff) {
        this(itemView,selM,null,iconOn,iconOff);
    }

    public DeviceS20StatusHolder(View itemView,SelectedItemManager selM,MenuInflater minf,int iOn,int iOff) {
        this(itemView,selM,minf);
        iconOff = iOff;
        iconOn = iOn;
    }

    @Override
    public int getMenuResourceId() {
        return R.menu.menu_context_status;
    }

    @Override
    public int getResourceId() {
        return R.layout.statusitem;
    }

    @Override
    public void fromO(StateHolder dt) {
        if (dt.first=="ON")
            statusTXV.setBackgroundResource(iconOn);
        else
            statusTXV.setBackgroundResource(iconOff);
    }
}
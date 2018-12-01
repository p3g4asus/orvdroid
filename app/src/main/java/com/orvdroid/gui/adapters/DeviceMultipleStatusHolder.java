package com.orvdroid.gui.adapters;

import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.orvdroid.gui.R;
import com.orvdroid.gui.fragments.StateHolder;

public class DeviceMultipleStatusHolder extends GenericViewHolder<StateHolder> {
    private TextView statusTXV;
    public DeviceMultipleStatusHolder(View itemView, SelectedItemManager selM) {
        this(itemView,selM,null);
    }

    public DeviceMultipleStatusHolder(View itemView, SelectedItemManager selM, MenuInflater minf) {
        super(itemView,selM,minf);
        statusTXV = (TextView)itemView.findViewById(R.id.statusTXV);
    }
    @Override
    public void fromO(StateHolder o) {
        statusTXV.setText(o.first);
        statusTXV.setTag(o);
    }

    @Override
    public int getResourceId() {
        return R.layout.statusitem;
    }
}

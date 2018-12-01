package com.orvdroid.gui.adapters;

import android.view.MenuInflater;
import android.view.View;
import android.widget.TextView;

import com.orvdroid.gui.R;
import com.orvdroid.gui.adapters.GenericViewHolder;
import com.orvdroid.gui.adapters.SelectedItemManager;
import com.orvdroid.lib.message.DeviceAllOneKey;

/**
 * Created by Matteo on 24/01/2016.
 */
public class DeviceAllOneKeyHolder extends GenericViewHolder<DeviceAllOneKey> {
    private TextView firstTXV,secondTXV;
    public DeviceAllOneKeyHolder(View itemView,SelectedItemManager selM) {
        this(itemView,selM,null);
    }

    public DeviceAllOneKeyHolder(View itemView,SelectedItemManager selM,MenuInflater minf) {
        super(itemView,selM,minf);
        firstTXV = (TextView)itemView.findViewById(R.id.firstTXV);
        secondTXV = (TextView)itemView.findViewById(R.id.secondTXV);
    }

    @Override
    public int getMenuResourceId() {
        return R.menu.menu_context_keys;
    }

    @Override
    public int getResourceId() {
        return R.layout.keyitem;
    }

    @Override
    public void fromO(DeviceAllOneKey dt) {
        firstTXV.setText(dt.getDevice());
        if (dt.isShortcut() || dt.isDelay()) {
            secondTXV.setVisibility(View.GONE);
        }
        else {
            secondTXV.setVisibility(View.VISIBLE);
            secondTXV.setText(dt.getKey());
        }
    }
}

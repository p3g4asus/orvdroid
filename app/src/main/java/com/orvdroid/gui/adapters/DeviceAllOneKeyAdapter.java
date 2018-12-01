package com.orvdroid.gui.adapters;

/**
 * Created by Matteo on 24/01/2016.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.orvdroid.gui.R;
import com.orvdroid.lib.message.DeviceAllOneKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 24/05/15
 */
public class DeviceAllOneKeyAdapter extends GenericAdapter<DeviceAllOneKey,DeviceAllOneKeyHolder> {

    public DeviceAllOneKeyAdapter(ArrayList<DeviceAllOneKey> myDataset,DeviceAllOneKeyHolder kh, MenuInflater minf) {
        super(myDataset,kh, minf,true);
    }

    public void animateTo(List<DeviceAllOneKey> models) {
        selM.setSelectedItem(-1,null,GenericAdapter.CT_NOCLICK);
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<DeviceAllOneKey> newModels) {
        for (int i = mDataset.size() - 1; i >= 0; i--) {
            final DeviceAllOneKey model = mDataset.get(i);
            if (!newModels.contains(model)) {
                deleteItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<DeviceAllOneKey> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final DeviceAllOneKey model = newModels.get(i);
            if (!mDataset.contains(model)) {
                addItem(model,i);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<DeviceAllOneKey> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final DeviceAllOneKey model = newModels.get(toPosition);
            final int fromPosition = mDataset.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

}
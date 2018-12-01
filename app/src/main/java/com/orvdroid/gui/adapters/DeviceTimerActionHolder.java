package com.orvdroid.gui.adapters;

import android.graphics.Paint;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.orvdroid.gui.R;
import com.orvdroid.lib.message.DeviceTimer;
import com.orvdroid.lib.message.DeviceTimerAction;

/**
 * Created by Matteo on 24/01/2016.
 */
public class DeviceTimerActionHolder extends GenericViewHolder<DeviceTimerAction> {
    protected TextView firstTXV;
    protected TextView secondTXV;
    protected ImageView actionICV;


    public DeviceTimerActionHolder(View itemView, SelectedItemManager sm,MenuInflater minf) {
        super(itemView, sm,minf);
        firstTXV = (TextView) itemView.findViewById(R.id.firstTXV);
        secondTXV = (TextView) itemView.findViewById(R.id.secondTXV);
        actionICV = (ImageView) itemView.findViewById(R.id.actionICV);
    }

    @Override
    public void fromO(DeviceTimerAction dta) {
        DeviceTimer dt = dta.getDt();
        String act = dt.getAction();
        actionICV.setImageResource(act=="0"?R.drawable.ic_action_switchoff:R.drawable.ic_action_switchon);
        firstTXV.setText(dt.getDateTimeString());
        String reps = dt.getRepeatString();
        if (reps.isEmpty())
            secondTXV.setText(R.string.ta_once);
        else
            secondTXV.setText(reps);
        if (dta.getAction()==DeviceTimerAction.ACTION_REMOVE) {
            firstTXV.setPaintFlags(firstTXV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            secondTXV.setPaintFlags(secondTXV.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            firstTXV.setPaintFlags(firstTXV.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            secondTXV.setPaintFlags(secondTXV.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    public int getResourceId() {
        return R.layout.timeritem;
    }
}

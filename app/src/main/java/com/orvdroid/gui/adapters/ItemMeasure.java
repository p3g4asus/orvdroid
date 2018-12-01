package com.orvdroid.gui.adapters;


import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Fujitsu on 26/01/2016.
 */
public interface ItemMeasure {
    void setItemMeasureListener(ItemMeasureListener list);
    interface ItemMeasureListener {
        void onItemMeasure(ViewGroup.LayoutParams p,View v);
    }
}

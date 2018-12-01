package com.orvdroid.gui.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.util.AttributeSet;

/**
 * Created by Matteo on 30/01/2016.
 */
public class RecyclerViewEmptySupportGrid extends RecyclerViewEmptySupport {
    private GridLayoutManager manager;
    private int columnWidth = -1;

    public RecyclerViewEmptySupportGrid(Context context) {
        super(context);
        init(context, null);
    }

    public RecyclerViewEmptySupportGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RecyclerViewEmptySupportGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        /*if (attrs != null) {
            int[] attrsArray = {
                    android.R.attr.columnWidth
            };
            TypedArray array = context.obtainStyledAttributes(attrs, attrsArray);
            columnWidth = array.getDimensionPixelSize(0, -1);
            array.recycle();
        }*/
        getDefaultColumnWidth();
        manager = new GridLayoutManager(context, 1);
        setLayoutManager(manager);
    }
    public void setColumnWidth(int v) {
        if (v<0)
            getDefaultColumnWidth();
        else
            columnWidth = v;
        remeasure();
    }

    private void getDefaultColumnWidth() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        try {
            columnWidth = Integer.parseInt(sharedPref.getString("pref_dev_allonecolwidth", "50"));
        }
        catch (NumberFormatException nfe) {
            columnWidth = 50;
        }
        if (columnWidth<20)
            columnWidth = 20;
    }

    private void remeasure() {
        if (columnWidth > 0) {
            int spanCount = Math.max(1, getMeasuredWidth() / columnWidth);
            manager.setSpanCount(spanCount);
        }
        else
            manager.setSpanCount(1);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        remeasure();
    }

}

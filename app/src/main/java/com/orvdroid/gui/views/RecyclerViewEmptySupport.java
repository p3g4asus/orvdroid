package com.orvdroid.gui.views;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.orvdroid.gui.adapters.ItemMeasure;

/**
 * Created by Fujitsu on 22/01/2016.
 */
public class RecyclerViewEmptySupport extends RecyclerView {
    private View emptyView;

    private class EmptyObserver extends AdapterDataObserver {
        ItemMeasure.ItemMeasureListener itemMeasureListener = null;
        public EmptyObserver (ItemMeasure.ItemMeasureListener iml) {
            itemMeasureListener = iml;
        }

        @Override
        public void onItemRangeInserted (int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public void onItemRangeRemoved (int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public void onChanged() {
            Adapter<?> adapter =  getAdapter();
            if (itemMeasureListener!=null)
                itemMeasureListener.onItemMeasure(null,null);
            if(adapter != null && emptyView != null) {
                if(adapter.getItemCount() == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                    RecyclerViewEmptySupport.this.setVisibility(View.GONE);
                }
                else {
                    emptyView.setVisibility(View.GONE);
                    RecyclerViewEmptySupport.this.setVisibility(View.VISIBLE);
                }
            }

        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Adapter a;
        itemMeasure.setMeasured(true);
        if (!itemMeasure.initDone() && (a = getAdapter()) != null && a.getItemCount() > 0)
            getLayoutParams().height = 100;
    }

    private class MyItemMeasure implements ItemMeasure.ItemMeasureListener {
        private int wi = -2;
        private int he = -2;
        private int oldsz = -1;
        private Adapter oldAdapter = null;
        private boolean init = false;
        private boolean measured = false;

        public MyItemMeasure() {
        }
        @Override
        public void onItemMeasure(ViewGroup.LayoutParams p, View v) {
            Adapter myAdapter = getAdapter();
            if (myAdapter!=oldAdapter) {
                oldAdapter = myAdapter;
                wi = -2;
                he = -2;
                oldsz = -1;
            }
            if (myAdapter!=null) {
                boolean setv = false;
                int ic;
                if (p == null && (ic = myAdapter.getItemCount()) != oldsz) {
                    if (measured && oldsz==0) {
                        getLayoutParams().height = 100;
                        init = true;
                    }
                    else if (ic==0)
                        init = false;
                    oldsz = ic;
                    if (he>0)
                        setv = true;
                } else {
                    if (oldsz>0)
                        init = true;
                    if (p.height >= 0 && (p.height != he || getLayoutParams().height!=p.height)) {
                        he = p.height;
                        setv = true;
                    } else if (p.width >= 0 && p.width != wi) {
                        wi = p.width;
                    }
                }
                if (setv) {
                    getLayoutParams().height = he * oldsz;
                }
            }
        }

        public boolean initDone() {
            return init;
        }

        public void setMeasured(boolean measured) {
            this.measured = measured;
        }
    }

    public RecyclerViewEmptySupport(Context context) {
        super(context);
        adjustLayoutManager(context);
    }

    protected void adjustLayoutManager(Context context) {
        LinearLayoutManager ll = new LinearLayoutManager(context);
        setLayoutManager(ll);
    }

    public RecyclerViewEmptySupport(Context context, AttributeSet attrs) {
        super(context, attrs);
        adjustLayoutManager(context);
    }

    public RecyclerViewEmptySupport(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        adjustLayoutManager(context);
    }



    private MyItemMeasure itemMeasure = new MyItemMeasure();
    private EmptyObserver emptyObserver = new EmptyObserver(itemMeasure);

    @Override
    public void setAdapter(final Adapter adapter) {
        super.setAdapter(adapter);

        if(adapter != null) {
            adapter.registerAdapterDataObserver(emptyObserver);
            if (adapter instanceof ItemMeasure)
                ((ItemMeasure) adapter).setItemMeasureListener(itemMeasure);
        }

        emptyObserver.onChanged();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }
}

package com.orvdroid.gui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.lang.reflect.InvocationTargetException;

import static android.support.v7.widget.RecyclerView.NO_POSITION;

/**
 * Created by Matteo on 24/01/2016.
 */
public abstract class GenericViewHolder<O> extends RecyclerView.ViewHolder {
    protected final MenuInflater mMInflater;
    protected View myBackground;
    protected SelectedItemManager selMan;

    public GenericViewHolder newInstance(View itemView,SelectedItemManager selM,MenuInflater minf) {
        try {
            return getClass().getConstructor(View.class,SelectedItemManager.class,MenuInflater.class).newInstance(itemView,selM,minf);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }


    public GenericViewHolder(View itemView,SelectedItemManager selM,MenuInflater minf) {
        super(itemView);
        myBackground = itemView;
        selMan = selM;
        mMInflater = minf;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickResp(GenericAdapter.CT_CLICK);
            }
        });
        if (getMenuResourceId()>=0) {
            itemView.setOnCreateContextMenuListener(mOnCreateContextMenuListener);
        }
        else
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    clickResp(GenericAdapter.CT_LONGCLICK);
                    return true;
                }
            });
    }

    private final View.OnCreateContextMenuListener mOnCreateContextMenuListener = new View.OnCreateContextMenuListener() {
        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            if (mMInflater!=null) {
                mMInflater.inflate(getMenuResourceId(), menu);
                for (int i = 0; i < menu.size(); i++)
                    menu.getItem(i).setOnMenuItemClickListener(mOnMyActionClickListener);
            }
        }
    };

    private final MenuItem.OnMenuItemClickListener mOnMyActionClickListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            clickResp(item.getItemId());
            return true;
        }
    };

    public void setSelected(boolean v) {
        myBackground.setSelected(v);
    }

    public abstract void fromO(O o);

    private void clickResp(int clickt) {
        int pos = getLayoutPosition();
        if (selMan!=null && pos!=NO_POSITION)
            selMan.setSelectedItem(pos,this,clickt);
    }

    public abstract int getResourceId();
    public int getMenuResourceId() {
        return -1;
    }
}

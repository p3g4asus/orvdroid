package com.orvdroid.gui.adapters;

/**
 * Created by Matteo on 24/01/2016.
 */
public class SelectedItemManager {

    public interface MyClickListener {
        void onItemSelectionChanged(int position,int idc);
    }
    public int getSelectedItem() {
        return selectedItem;
    }

    public void unselectHolder(GenericViewHolder vh) {
        vh.setSelected(false);
        if (vh==viewHolder)
            viewHolder = null;
    }

    public void setSelectedHolder(GenericViewHolder vh) {
        if (viewHolder!=vh) {
            if (viewHolder != null)
                viewHolder.setSelected(false);
            viewHolder = vh;
            if (viewHolder != null)
                viewHolder.setSelected(true);
        }
    }

    public int setSelectedItem(int selectedItem,GenericViewHolder vh, int clickt) {
        boolean select = false;
        if (viewHolder!=null) {
            if (vh!=viewHolder) {
                viewHolder.setSelected(false);
                select = true;
            }
        }
        else
            select = true;
        if (isClick) {
            this.selectedItem = selectedItem;
            if (myClickListener != null && selectedItem>=0)
                myClickListener.onItemSelectionChanged(selectedItem,clickt);
        }
        else {
            this.selectedItem = selectedItem = selectedItem==this.selectedItem?-1:selectedItem;
            if (myClickListener!=null)
                myClickListener.onItemSelectionChanged(selectedItem,clickt);
        }
        viewHolder = vh;
        if ((select || selectedItem<0) && viewHolder!=null)
            viewHolder.setSelected(selectedItem>=0);
        if (selectedItem<0)
            viewHolder = null;
        return selectedItem;
    }


    private int selectedItem = -1;
    private MyClickListener myClickListener = null;
    private boolean isClick = false;
    private GenericViewHolder viewHolder = null;

    public SelectedItemManager(MyClickListener  v) {
        myClickListener = v;
    }

    public SelectedItemManager(MyClickListener  v,boolean clickListen) {
        myClickListener = v;
        isClick = clickListen;
    }

    public void moveSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }
}

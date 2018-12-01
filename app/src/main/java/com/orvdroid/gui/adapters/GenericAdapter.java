package com.orvdroid.gui.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

/**
 * Created by Fujitsu on 22/01/2016.
 RecyclerViewEmptySupport list =
 (RecyclerViewEmptySupport)rootView.findViewById(R.id.list1);
 list.setLayoutManager(new LinearLayoutManager(context));
 list.setEmptyView(rootView.findViewById(R.id.list_empty));

 array di operazioni sui timer di default a none
 */
public class GenericAdapter<H,VH extends GenericViewHolder<H>> extends RecyclerView.Adapter<VH> implements ItemMeasure {
    private static String LOG_TAG = GenericAdapter.class.getSimpleName();
    private final VH vhInstance;
    private final MenuInflater mMInflater;
    protected ArrayList<H> mDataset;
    private ItemMeasureListener measureListener = null;
    public static final int CT_NOCLICK = -3;
    public static final int CT_CLICK = -2;
    public static final int CT_LONGCLICK = -1;
    private boolean isClick = false;

    public int getSelectedIndex() {
        int selectedItem =  selM.getSelectedItem();
        if (selectedItem<0 || selectedItem>=mDataset.size())
            return -1;
        else
            return selectedItem;
    }

    @Override
    public void setItemMeasureListener(ItemMeasureListener list) {
        measureListener = list;

    }

    public void setSelectedIndex(int sel) {
        selM.moveSelectedItem(sel);
    }

    public interface ExtClickListener<H> {
        void onItemSelectionChanged(int pos,H dt,int idc);
    }
    private ExtClickListener extClickListener;
    private SelectedItemManager.MyClickListener myClickListener = new SelectedItemManager.MyClickListener() {
        @Override
        public void onItemSelectionChanged(int position,int idc) {
            if (extClickListener!=null) {
                if (position>=0 && position<mDataset.size())
                    extClickListener.onItemSelectionChanged(position,mDataset.get(position),idc);
                else
                    extClickListener.onItemSelectionChanged(-1,null,CT_NOCLICK);
            }
        }
    };
    protected SelectedItemManager selM;





    public void notifySelectedItemChanged() {
        notifyItemChanged(selM.getSelectedItem());
    }


    public void setOnItemSelectedListener(ExtClickListener<H> myClickListener) {
        this.extClickListener = myClickListener;
    }

    public GenericAdapter(ArrayList<H> myDataset, VH vhInst) {
        this(myDataset,vhInst,null,false);
    }

    public GenericAdapter(ArrayList<H> myDataset, VH vhInst,MenuInflater minf,boolean clk) {
        mDataset = myDataset;
        vhInstance = vhInst;
        mMInflater = minf;
        isClick = clk;
        selM = new SelectedItemManager(myClickListener,isClick);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(vhInstance.getResourceId(), parent, false);
        VH dataObjectHolder = null;//(view,selM);
        try {
            dataObjectHolder = (VH) vhInstance.newInstance(view,selM,mMInflater);
            if (measureListener!=null)
                measureListener.onItemMeasure(dataObjectHolder.itemView.getLayoutParams(),dataObjectHolder.itemView);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        H dt = mDataset.get(position);
        holder.fromO(dt);
        if (selM.getSelectedItem()==position)
            selM.setSelectedHolder(holder);
        else
            selM.unselectHolder(holder);
    }

    public void addItemNoSel(H dataObj, int index) {
        mDataset.add(index, dataObj);
        notifyItemInserted(index);
    }

    public void addItem(H dataObj, int index) {
        addItem(dataObj, index, true);
    }

    public void addItem(H dataObj, int index,boolean notify) {

        int selectedItem = selM.getSelectedItem();
        if (selectedItem>=0 && selectedItem<=index && selectedItem<getItemCount()) {
            selectedItem++;
            selM.moveSelectedItem(selectedItem);
        }
        mDataset.add(index, dataObj);
        if (notify)
            notifyItemInserted(index);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final H model = deleteItem(fromPosition, false);
        addItem(model,toPosition,false);
        notifyItemMoved(fromPosition, toPosition);
    }



    public void deleteSelected() {
        int selectedItem = selM.getSelectedItem();
        if (selectedItem>=0)
            deleteItem(selectedItem);
    }

    public H deleteItem(int index) {
        return deleteItem(index,true);
    }

    public H deleteItem(int index,boolean notify) {
        int selectedItem = selM.getSelectedItem();
        if (selectedItem==index) {
            selM.setSelectedItem(-1,null,CT_NOCLICK);
        }
        H rem = mDataset.remove(index);
        if (notify)
            notifyItemRemoved(index);
        return rem;
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public H getSelectedItem() {
        int selectedItem = selM.getSelectedItem();
        if (selectedItem<0 || selectedItem>=mDataset.size())
            return null;
        else
            return mDataset.get(selectedItem);
    }


}
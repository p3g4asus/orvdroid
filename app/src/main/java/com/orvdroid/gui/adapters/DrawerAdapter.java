package com.orvdroid.gui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import com.orvdroid.gui.pojo.Information;
import com.orvdroid.gui.R;

/**
 * Created by Windows on 22-12-2014.
 */
public class DrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Information> data= Collections.emptyList();
    private static final int TYPE_HEADER=0;
    private static final int TYPE_ITEM=1;
    private LayoutInflater inflater;
    private Context context;
    public DrawerAdapter(Context context, List<Information> data){
        this.context=context;
        inflater=LayoutInflater.from(context);
        data.add(0,null);
        this.data=data;
    }

    public void add(Information info){
        data.add(info);
        notifyItemInserted(data.size()-1);
    }

    public void notifyModified(Information info) {
        int p = 0;
        for (Information t:data) {
            if (t!= null && t.equals(info)) {
                t.copy(info);
                notifyItemChanged(p);
                break;
            }
            p++;
        }
    }

    public void delete(int position){
        data.remove(position);
        notifyItemRemoved(position);
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType==TYPE_HEADER){
            View view=inflater.inflate(R.layout.ndrawerheader, parent,false);
            HeaderHolder holder=new HeaderHolder(view);
            return holder;
        }
        else{
            View view=inflater.inflate(R.layout.ndraweritem, parent,false);
            ItemHolder holder=new ItemHolder(view);
            return holder;
        }

    }

    @Override
    public int getItemViewType(int position) {
        if(position==0){
            return TYPE_HEADER;
        }
        else {
            return TYPE_ITEM;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof HeaderHolder ){

        }
        else{
            ItemHolder itemHolder= (ItemHolder) holder;
            Information current=data.get(position);
            itemHolder.title.setText(current.title);
            if (current.color==-1)
                itemHolder.title.setTextColor(itemHolder.title.getResources().getColor(R.color.colorSecondaryText,itemHolder.title.getContext().getTheme()));
            else
                itemHolder.title.setTextColor(current.color);
            itemHolder.icon.setImageResource(current.iconId);
        }

    }
    @Override
    public int getItemCount() {
        return data.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        TextView title;
        ImageView icon;
        public ItemHolder(View itemView) {
            super(itemView);
            title= (TextView) itemView.findViewById(R.id.listText);
            icon= (ImageView) itemView.findViewById(R.id.listIcon);
        }
    }
    class HeaderHolder extends RecyclerView.ViewHolder {

        public HeaderHolder(View itemView) {
            super(itemView);

        }
    }
}

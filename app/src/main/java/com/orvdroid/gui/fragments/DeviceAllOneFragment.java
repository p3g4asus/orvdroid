package com.orvdroid.gui.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.orvdroid.gui.R;
import com.orvdroid.gui.adapters.DeviceAllOneKeyAdapter;
import com.orvdroid.gui.adapters.DeviceAllOneKeyHolder;
import com.orvdroid.gui.adapters.GenericAdapter;
import com.orvdroid.gui.dialogs.LearnIrDialog;
import com.orvdroid.gui.dialogs.ShDialog;
import com.orvdroid.gui.dialogs.TimerAllOneDialog;
import com.orvdroid.gui.views.RecyclerViewEmptySupport;
import com.orvdroid.lib.message.CreateshMessage;
import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.DeviceAllOne;
import com.orvdroid.lib.message.DeviceAllOneKey;
import com.orvdroid.lib.message.EmitirMessage;
import com.orvdroid.lib.message.InfoMessage;
import com.orvdroid.lib.message.IBaseMessage;
import com.orvdroid.lib.message.LearnirMessage;
import com.orvdroid.lib.message.RemoteMessage;
import com.orvdroid.lib.message.SubscribeMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Matteo on 24/01/2016.
 */
public class DeviceAllOneFragment extends DeviceFragment implements SearchView.OnQueryTextListener {
    private static final String KEY_CBX = "DeviceAllOneFragment.KEY_CBX";
    private static final String TAG = "DeviceAllOneFragment";
    private static final long SUBSCRIBE_WAIT_TIME = 7000;
    private String mKeyCbx = "";
    private static final String KEY_SCROLL = "DeviceAllOneFragment.KEY_SCROLL";
    private Parcelable mKeyScroll = null;
    //private TextView actionTV = null;
    //private TextView statusTV = null;
    private Button timersBTN,learnIrBTN,shortcutBTN;
    private RecyclerViewEmptySupport keysRCV;
    private SearchView keysSRV;
    private RadioGroup keysRDG;
    private RadioButton keysAllRDB;
    private CompoundButton.OnCheckedChangeListener keysRDGCCL;
    protected LearnIrDialog learnDialog;
    protected ShDialog shDialog;
    private ArrayList<DeviceAllOneKey> keysLST = new ArrayList<>();
    protected DeviceAllOneKeyAdapter keysAPT;
    private long lastWrite = 0;
    private GenericAdapter.ExtClickListener<DeviceAllOneKey> keysECL;
    private SharedPreferences sharedPref;

    @Override
    public void refresh(Device d,IBaseMessage m) {
        myDev = d;
        //Log.e(getClass().getSimpleName(), "changing dev " + d + " " + d.getTimers().size());
        reloadKeys(m);
    }

    private ArrayList<DeviceAllOneKey> addRB(String text) {
        LayoutInflater inflater = (LayoutInflater) keysAllRDB.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.keyrb,keysRDG);
        RadioButton rb = (RadioButton) keysRDG.getChildAt(keysRDG.getChildCount()-1);
        rb.setText(text);
        rb.setOnCheckedChangeListener(keysRDGCCL);
        ArrayList<DeviceAllOneKey> tag = new ArrayList<>();
        //keysRDG.addView(rb);
        rb.setTag(tag);
        return tag;
    }

    private void reloadKeys(IBaseMessage m) {
        if (keysAPT!=null) {
            if (myDev != null && !myDev.isOffline()) {
                timersBTN.setEnabled(true);
                learnIrBTN.setEnabled(true);
                shortcutBTN.setEnabled(true);
                keysRCV.setEnabled(true);
                keysSRV.setEnabled(true);
                for (int i = 0; i<keysRDG.getChildCount(); i++) {
                    keysRDG.getChildAt(i).setEnabled(true);
                }
                if (m == null || m instanceof InfoMessage || m instanceof LearnirMessage || m instanceof CreateshMessage) {
                    String selectedItem = mKeyCbx;
                    Parcelable selectedScroll = mKeyScroll;
                    RadioButton rb;
                    mKeyCbx = "";
                    mKeyScroll = null;
                    if (selectedItem.isEmpty()) {
                        rb = getCheckedRB();
                        if (rb != null) {
                            selectedItem = rb.getText().toString();
                            selectedScroll = keysRCV.getLayoutManager().onSaveInstanceState();
                        }
                    }
                    keysRDG.check(R.id.keysAllRDB);
                    for (int i = keysRDG.getChildCount()-1; i>=1; i--) {
                        keysRDG.removeViewAt(1);
                    }
                    ArrayList<DeviceAllOneKey> allkeys = new ArrayList<>();
                    ArrayList<DeviceAllOneKey> kk = null,kk0;
                    HashMap<String, ArrayList<DeviceAllOneKey>> k = ((DeviceAllOne)myDev).getSh();
                    Iterator<Map.Entry<String, ArrayList<DeviceAllOneKey>>> it = k.entrySet().iterator();
                    String currId = "",shname;
                    DeviceAllOneKey shkey;
                    int shidx;
                    while (it.hasNext()) {
                        Map.Entry<String, ArrayList<DeviceAllOneKey>> pair = it.next();
                        shname = pair.getKey();
                        allkeys.add(shkey = pair.getValue().get(0));
                        shidx = shname.indexOf('_');
                        if (shidx>=2) {
                            shname = shname.substring(0,shidx);
                            if (!shname.equals(currId)) {
                                kk = addRB(shname);
                                currId = shname;
                            }
                            kk.add(shkey);
                        }
                        else
                            currId = "";
                    }
                    k = ((DeviceAllOne)myDev).getD433();
                    it = k.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, ArrayList<DeviceAllOneKey>> pair = it.next();
                        shname = pair.getKey();
                        kk0 = pair.getValue();
                        allkeys.addAll(kk0);
                        kk = addRB(shname);
                        kk.addAll(kk0);
                    }
                    k = ((DeviceAllOne)myDev).getDir();
                    it = k.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<String, ArrayList<DeviceAllOneKey>> pair = it.next();
                        shname = pair.getKey();
                        kk0 = pair.getValue();
                        allkeys.addAll(kk0);
                        kk = addRB(shname);
                        kk.addAll(kk0);
                    }
                    if (keysAreModified(keysLST, allkeys)) {
                        keysLST.clear();
                        keysLST.addAll(allkeys);
                        keysAPT.animateTo(filter(allkeys, keysSRV.getQuery().toString()));
                        keysRCV.scrollToPosition(0);
                    }
                    for (int i = 0; i<keysRDG.getChildCount(); i++) {
                        if ((rb = (RadioButton) keysRDG.getChildAt(i)).getText().equals(selectedItem)) {
                            rb.setChecked(true);
                            if (selectedScroll!=null)
                                keysRCV.getLayoutManager().onRestoreInstanceState(selectedScroll);
                            break;
                        }
                    }
                }
            } else {
                timersBTN.setEnabled(false);
                learnIrBTN.setEnabled(false);
                shortcutBTN.setEnabled(false);
                keysRCV.setEnabled(false);
                keysSRV.setEnabled(false);
                for (int i = 0; i<keysRDG.getChildCount(); i++) {
                    keysRDG.getChildAt(i).setEnabled(false);
                }
                timerDialog.dismiss();
                learnDialog.dismiss();
                shDialog.dismiss();
            }
        }
    }

    private boolean keysAreModified(ArrayList<DeviceAllOneKey> keysLST, ArrayList<DeviceAllOneKey> allkeys) {
        if (keysLST.size()!=allkeys.size())
            return true;
        else {
            for (int i = 0; i<keysLST.size(); i++) {
                if (!keysLST.get(i).equals(allkeys.get(i)))
                    return true;
            }
            return false;
        }
    }

    @Override
    protected void onRestore(Bundle b) {
        mKeyCbx = b.getString(KEY_CBX, "");
        mKeyScroll = b.getParcelable(KEY_SCROLL);
        learnDialog.restore(getActivity(),"learnDialog",b);
        shDialog.restore(getActivity(),"shDialog",b);
    }

    @Override
    protected void onSave(Bundle b) {
        RadioButton rb = getCheckedRB();
        b.putString(KEY_CBX, rb == null ? "" : rb.getText().toString());
        b.putParcelable(KEY_SCROLL, keysRCV.getLayoutManager().onSaveInstanceState());
        learnDialog.save("learnDialog", b);
        shDialog.save("shDialog", b);
    }

    @Override
    protected void onServiceDisconnect() {
        learnDialog.setConnectionService(null);
        shDialog.setConnectionService(null);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.allonefragment;
    }


    private RadioButton getCheckedRB() {
        RadioButton rb;
        for (int i = 0; i<keysRDG.getChildCount(); i++) {
            if ((rb = (RadioButton) keysRDG.getChildAt(i)).isChecked()) {
                return rb;
            }
        }
        return null;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        RadioButton rb = getCheckedRB();
        ArrayList<DeviceAllOneKey> kk = rb==null?keysLST: (ArrayList<DeviceAllOneKey>) rb.getTag();
        for (int i = 0; i < keysRDG.getChildCount(); i++) {
            if ((rb = (RadioButton) keysRDG.getChildAt(i)).isChecked()) {
                kk = (ArrayList<DeviceAllOneKey>) rb.getTag();
                break;
            }

        }
        final List<DeviceAllOneKey> filteredModelList = filter(kk, query);
        keysAPT.animateTo(filteredModelList);
        keysRCV.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private List<DeviceAllOneKey> filter(List<DeviceAllOneKey> models, String query) {
        query = query.toLowerCase();

        final List<DeviceAllOneKey> filteredModelList = new ArrayList<>();
        for (DeviceAllOneKey model : models) {
            final String text = model.toString().toLowerCase();
            if (text.contains(query)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    private void binderWrite(RemoteMessage m) {
        long now = System.currentTimeMillis();
        if (now-lastWrite>SUBSCRIBE_WAIT_TIME)
            mBinder.write(new SubscribeMessage(myDev));
        mBinder.write(m);
        lastWrite = now;
    }

    class DeviceKeyAdapter extends ArrayAdapter<DeviceAllOneKey> {
        private final Context context;
        private final List<DeviceAllOneKey> keys;
        private final List<DeviceAllOneKey> selected = new ArrayList<>();
        private final int btn_check_buttonless_off,btn_check_buttonless_on;

        public DeviceKeyAdapter(Context ctx, List<DeviceAllOneKey> all) {
            super(ctx, R.layout.shselect_dlg_layout, all);
            context = ctx;
            keys = all;
            Resources res = getActivity().getResources();
            btn_check_buttonless_off = res.getIdentifier("btn_check_buttonless_off", "drawable", "android");
            btn_check_buttonless_on = res.getIdentifier("btn_check_buttonless_on", "drawable", "android");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.shselect_dlg_layout, parent, false);
            final CheckedTextView textView = (CheckedTextView) rowView.findViewById(R.id.shselect_dlg_itemtxt);
            DeviceAllOneKey item = keys.get(position);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (((CheckedTextView) v).isChecked()) {
                        ((CheckedTextView) v).setCheckMarkDrawable(btn_check_buttonless_off);
                        selected.remove(textView.getTag());
                    }
                    else {
                        ((CheckedTextView) v).setCheckMarkDrawable(btn_check_buttonless_on);
                        selected.add((DeviceAllOneKey) textView.getTag());
                    }
                    ((CheckedTextView) v).toggle();
                }
            });
            textView.setTag(item);
            boolean contained = selected.contains(item);
            //LinearLayout outBox = (LinearLayout) rowView.findViewById(R.id.outBox);
            //outBox.setBackgroundColor(DeviceAllOneFragment.this.getColor(((position % 2) == 0 ? R.color.colorHighlight : R.color.colorWhite)));

            textView.setText(item.toString());
            textView.setChecked(contained);
            textView.setCheckMarkDrawable(contained?btn_check_buttonless_on:btn_check_buttonless_off);
            return rowView;
        }
    }

    private void openDialog(List<DeviceAllOneKey> keys) {
        // Creating and Building the Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.shselect_dlg_title));
        final DeviceKeyAdapter choiceArrayAdapter = new DeviceKeyAdapter(getActivity(),keys);
        builder.setNeutralButton(getString(android.R.string.cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setAdapter(choiceArrayAdapter, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setPositiveButton(getString(R.string.shselect_dlg_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EmitirMessage emr;
                Collections.sort(choiceArrayAdapter.selected, new Comparator<DeviceAllOneKey>() {
                    @Override
                    public int compare(DeviceAllOneKey lhs, DeviceAllOneKey rhs) {
                        return lhs.getKey().compareTo(rhs.getKey());
                    }
                });
                for(DeviceAllOneKey k:choiceArrayAdapter.selected) {
                    emr = new EmitirMessage(myDev, k);
                    shortcutAdd(k.toString(), emr, R.drawable.ic_launcher_allone);
                }
            }
        });
        //setDialogChoices(builder, sessions, items);
        AlertDialog mDialog = builder.create();
        mDialog.getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        mDialog.show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // get the url to open

        timerDialog = new TimerAllOneDialog(myDev, null, null,this);
        learnDialog = new LearnIrDialog((DeviceAllOne)myDev,null);
        shDialog = new ShDialog((DeviceAllOne)myDev,null);
        View v = getView();
        //statusTV = (TextView) v.findViewById(R.id.statusTV);
        //actionTV = (TextView) v.findViewById(R.id.actionTV);
        learnIrBTN = (Button) v.findViewById(R.id.learnIrBTN);
        learnIrBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Activity a = getActivity();
                if (a != null && mBinder != null && myDev!=null) {
                    learnDialog.setConnectionService(mBinder);
                    learnDialog.setDevice(myDev);
                    learnDialog.show(a);
                }
            }
        });
        shortcutBTN = (Button) v.findViewById(R.id.shortcutBTN);
        shortcutBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Activity a = getActivity();
                if (a != null && mBinder != null && myDev!=null) {
                    shDialog.setConnectionService(mBinder);
                    shDialog.setDevice(myDev);
                    shDialog.show(a);
                }
            }
        });
        keysRCV = (RecyclerViewEmptySupport) v.findViewById(R.id.keysRCV);
        keysRCV.setEmptyView(v.findViewById(R.id.keysEmptyTXV));
        keysSRV = (SearchView) v.findViewById(R.id.keysSRV);
        keysSRV.setOnQueryTextListener(this);
        keysAPT = new DeviceAllOneKeyAdapter(new ArrayList<>(keysLST),new DeviceAllOneKeyHolder(new View(getActivity()),null),getActivity().getMenuInflater());
        keysECL = new GenericAdapter.ExtClickListener<DeviceAllOneKey>() {
            @Override
            public void onItemSelectionChanged(int pos, DeviceAllOneKey k,int idc) {
                ArrayList<DeviceAllOneKey> lst = new ArrayList<DeviceAllOneKey>();
                int c = 0;
                boolean emit = false;
                EmitirMessage emr;
                if (idc==R.id.action_sh_key_remote) {
                    ArrayList<DeviceAllOneKey> widget = new ArrayList<>();
                    for (DeviceAllOneKey i:keysLST) {
                        if (i.getDevice().equals(k.getDevice()))
                            widget.add(i);
                    }
                    openDialog(widget);
                }
                else if (idc==GenericAdapter.CT_CLICK || idc==R.id.action_key_1 || idc==R.id.action_sh_key_1) {
                    c = 1;
                    emit = idc==GenericAdapter.CT_CLICK || idc==R.id.action_key_1;
                } else if (idc==R.id.action_key_5 || idc==R.id.action_sh_key_5) {
                    c = 5;
                    emit = idc==R.id.action_key_5;
                }
                else if (idc==R.id.action_key_10 || idc==R.id.action_sh_key_10) {
                    c = 10;
                    emit = idc==R.id.action_key_10;
                }
                if (c > 0) {
                    for (int i = 0; i < c; i++)
                        lst.add(k);
                    emr = new EmitirMessage(myDev, lst);
                    if (emit) {
                        if (mBinder != null)
                            binderWrite(emr);
                    } else
                        shortcutAdd(k.toString() + (c > 1 ? " X" + c : ""), emr, R.drawable.ic_launcher_allone);
                }
            }
        };
        keysAPT.setOnItemSelectedListener(keysECL);
        keysRCV.setAdapter(keysAPT);
        timersBTN = (Button) v.findViewById(R.id.timersBTN);
        timersBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Activity a = getActivity();
                if (a != null && mBinder != null && myDev!=null) {
                    timerDialog.setConnectionService(mBinder);
                    timerDialog.setDevice(myDev);
                    timerDialog.show(a);
                }
            }
        });
        keysRDGCCL = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    keysAPT.animateTo(filter((List<DeviceAllOneKey>) buttonView.getTag(), keysSRV.getQuery().toString()));
                    keysRCV.scrollToPosition(0);
                }
            }
        };
        keysRDG = (RadioGroup) v.findViewById(R.id.keysRDG);
        keysAllRDB = (RadioButton) v.findViewById(R.id.keysAllRDB);
        keysAllRDB.setOnCheckedChangeListener(keysRDGCCL);
        keysAllRDB.setTag(keysLST);
        reloadKeys(null);

        //updateAction(connectionUpdates.getLastAction());
        //updateStatus(connectionUpdates.getLastStatus());
    }
}
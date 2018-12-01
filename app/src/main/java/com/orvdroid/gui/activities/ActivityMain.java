package com.orvdroid.gui.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orvdroid.gui.R;
import com.orvdroid.gui.app.CA;
import com.orvdroid.gui.fragments.DeviceAllOneFragment;
import com.orvdroid.gui.fragments.DeviceFragment;
import com.orvdroid.gui.fragments.DeviceS20Fragment;
import com.orvdroid.gui.fragments.DeviceStateFragment;
import com.orvdroid.gui.fragments.DrawerFragment;
import com.orvdroid.gui.preference.ValidatedEditTextPreference;
import com.orvdroid.gui.preference.ValidatedEditTextPreferenceDialog;
import com.orvdroid.gui.util.Messages;
import com.orvdroid.lib.message.ConnectionStatusMessage;
import com.orvdroid.lib.message.Device;
import com.orvdroid.lib.message.DevicePrimelan;
import com.orvdroid.lib.message.DeviceS20;
import com.orvdroid.lib.message.DeviceVirtual;
import com.orvdroid.lib.message.ExitMessage;
import com.orvdroid.lib.message.GetinfoMessage;
import com.orvdroid.lib.message.IBaseMessage;
import com.orvdroid.lib.message.InfoMessage;
import com.orvdroid.lib.message.RemoteMessage;
import com.orvdroid.lib.utils.CommandProcessor;
import com.orvdroid.lib.utils.ParcelableMessage;
import com.orvdroid.lib.utils.ParcelableUtil;
import com.orvdroid.workers.ConnectionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import it.neokree.materialtabs.MaterialTab;
import it.neokree.materialtabs.MaterialTabHost;
import it.neokree.materialtabs.MaterialTabListener;


public class ActivityMain extends AppCompatActivity implements MaterialTabListener, CommandProcessor {

    private static final String TAG = "ActivityMain";
    private Resources res = null;
    private ConnectionService.ConnectionServiceBinder mBinder;

    private ProgressDialog progress;
    private DeviceManagerConnection mServiceConnection = null;

    private static final String HIBERNATION_TIME = "MainActivity.HIBERNATION_TIME";
    private long mLastIbernationTime = System.currentTimeMillis();
    private static final String SELECTED_TAB = "MainActivity.SELECTED_TAB";
    private String mSelectedTab = "";
    private Toolbar mToolbar;
    //a layout grouping the toolbar and the tabs together
    private ViewGroup mContainerToolbar;
    private MaterialTabHost mTabHost;
    private ViewPager mPager;
    private ViewPagerAdapter mAdapter;
    //private FloatingActionButton mFAB;
    //private FloatingActionMenu mFABMenu;
    private DrawerFragment mDrawerFragment;
    private List<String> deviceIdList = null;
    private LinkedHashMap<String,MaterialTabHolder> deviceTabs = new LinkedHashMap<>();
    private MaterialTab settingsTab = null;
    private ExceptionBroadcastReceiver messageReceiver = new ExceptionBroadcastReceiver();
    private SharedPreferences sharedPref;

    public void onDrawerSlide(float slideOffset) {
    }

    public static class SettingsFragment extends PreferenceFragmentCompat implements CommandProcessor {
        private ConnectionService.ConnectionServiceBinder mBinder = null;
        private Preference pActGetinfo;
        private ServiceConnection mServiceConnection;
        private Context ctx;
        private static final String DIALOG_FRAGMENT_TAG =
                "android.support.v7.preference.PreferenceFragment.DIALOG";

        private class SettingsConnection implements ServiceConnection {

            @Override
            public void onServiceConnected(ComponentName componentName,
                                           IBinder service) {
                mBinder = (ConnectionService.ConnectionServiceBinder) service;
                mBinder.addCommandProcessor(SettingsFragment.this);
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
			/*if (mChars!=null) {
				for (BluetoothGattCharacteristic gattCharacteristic : mChars)
					mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, false);
			}*/
                //intDisconnect();
            }

        }

        @Override
        public void onResume() {
            super.onResume();
            if (mServiceConnection==null) {
                mServiceConnection = new SettingsConnection();
                ctx.bindService(new Intent(ctx, ConnectionService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
            }
        }

        @Override
        public void onPause() {
            if (mServiceConnection!=null) {
                if (mBinder!=null) {
                    mBinder.removeCommandProcessor(this);
                    mBinder = null;
                }
                ctx.unbindService(mServiceConnection);
                mServiceConnection = null;
            }
            super.onPause();
        }
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
            ctx = getActivity().getApplicationContext();

            pActGetinfo = findPreference("pref_act_getinfo");
            pActGetinfo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (mBinder!=null)
                        mBinder.write(new GetinfoMessage());
                    return false;
                }
            });
        }

        @Override
        public boolean processCommand(final IBaseMessage b) {
            Activity a = getActivity();
            if (a!=null) {
                a.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (b instanceof ConnectionStatusMessage) {
                            ConnectionStatusMessage csb = (ConnectionStatusMessage) b;
                            pActGetinfo.setEnabled(!csb.isError());
                        }
                    }
                });
                return true;
            }
            else
                return false;
        }

        @Override
        public void onDisplayPreferenceDialog(Preference preference) {
            // check if dialog is already showing
            if (getFragmentManager().findFragmentByTag(DIALOG_FRAGMENT_TAG) != null) {
                return;
            }

            DialogFragment f = null;
            if (preference instanceof ValidatedEditTextPreference) {
                f = ValidatedEditTextPreferenceDialog.newInstance((ValidatedEditTextPreference) preference);
            } else {
                super.onDisplayPreferenceDialog(preference);
            }
            if (f != null) {
                f.setTargetFragment(this, 0);
                f.show(getFragmentManager(), DIALOG_FRAGMENT_TAG);
            }
        }


    }

    private void unProcessTabs() {

        for (Map.Entry<String, MaterialTabHolder> entry : deviceTabs.entrySet())
        {
            entry.getValue().setProcessed(0);
        }
    }

    private Device getNDevice(int n) {
        String id = null;
        synchronized (deviceIdList) {
            if (deviceIdList!=null && n<deviceIdList.size())
                id = deviceIdList.get(n);
        }
        return id==null?null:ParcelableUtil.unmarshallFromSharedPref(Device.CREATOR,"dev_"+id,sharedPref);
    }

    private class MaterialTabHolder {
        private MaterialTab tab;
        private int processed = 0;

        public MaterialTab getTab() {
            return tab;
        }

        public int getProcessed() {
            return processed;
        }

        public void setTab(MaterialTab tab) {
            this.tab = tab;
        }

        public void setProcessed(int processed) {
            this.processed = processed;
        }

        public MaterialTabHolder(MaterialTab m) {
            tab = m;
        }

        public MaterialTabHolder(Device d) {
            if (d!=null) {
                tab = mTabHost.newTab();
                tab.setIcon(getResources().getDrawable(d.getTabIconResource()))
                        .setTabListener(ActivityMain.this);
            }
        }
    }

    @Override
    public boolean processCommand(final IBaseMessage hs2) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (hs2 instanceof ExitMessage) {
                    showProgressDialog(false, "");
                    finish();
                    System.exit(0);
                }
                else if (hs2 instanceof RemoteMessage) {
                    RemoteMessage m = (RemoteMessage) hs2;
                    Device d;
                    if (hs2 instanceof InfoMessage) {
                        deviceIdList = mBinder.getDeviceIdList();
                        reloadTabs();
                    }
                    else if ((d = m.isDeviceModified()) != null)
                        mDrawerFragment.notifyDeviceStateChanged(d);
                    Intent msg = m.response2Message();
                    if (msg!=null)
                        messageReceiver.intent2Dialog(msg);
                }
                else if (hs2 instanceof ConnectionStatusMessage) {
                    ConnectionStatusMessage csm = (ConnectionStatusMessage) hs2;
                    if (csm.isError())
                        mPager.setCurrentItem(0);
                }
            }
        });
        return true;
    }

    private void intStartService() {
        Intent gattServiceIntent = new Intent(this, ConnectionService.class);
        startService(gattServiceIntent);
    }

    private class DeviceManagerConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName,
                                       IBinder service) {
            mBinder = (ConnectionService.ConnectionServiceBinder) service;
            mBinder.addCommandProcessor(ActivityMain.this);
            deviceIdList = mBinder.getDeviceIdList();
            reloadTabs();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
			/*if (mChars!=null) {
				for (BluetoothGattCharacteristic gattCharacteristic : mChars)
					mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, false);
			}*/
            //intDisconnect();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Intent intent = getIntent();
        String action = intent.getAction();
        Log.v(TAG,"Start Action is "+action);
        if ((!isTaskRoot() &&
                intent.hasCategory(Intent.CATEGORY_LAUNCHER) &&
                action != null && action.equals(Intent.ACTION_MAIN)) || !action.equals(Intent.ACTION_MAIN)) {
            finish();
            return;
        }
        if (savedInstanceState!=null) {
            List<Fragment> al = getSupportFragmentManager().getFragments();
            if (al != null) {
                for (Fragment frag : al)
                {
                    if (frag instanceof DeviceFragment || frag instanceof SettingsFragment) {
                        Log.e(TAG, "Removing " + frag);
                        getSupportFragmentManager().beginTransaction().remove(frag).commit();
                    }
                }
            }

            mLastIbernationTime = savedInstanceState.getLong(HIBERNATION_TIME);
            mSelectedTab = savedInstanceState.getString(SELECTED_TAB,"");
        }
        setContentView(R.layout.activity_main);
        initTabs();
        reloadTabs();
        initDrawer();
        //animate the Toolbar when it comes into the picture

        res = getResources();
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        intStartService();
        setupProgressDialog();
        // updateTimerInit();
    }

    private void showError(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setCancelable(false)
                .setNeutralButton(res.getString(R.string.change_settings), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                        //startActivity(intent);
                        mPager.setCurrentItem(mAdapter.getCount() - 1);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private class ExceptionBroadcastReceiver extends BroadcastReceiver {

        protected String getExceptionMessage(Intent m) {
            String rv = "";
            int i = 0;
            while(true) {
                if (m.hasExtra("except"+i)) {
                    if (!rv.isEmpty())
                        rv+="\n";

                    rv+=((ParcelableMessage)m.getParcelableExtra("except"+i)).getMessage(null,res);
                    i++;
                }
                else
                    break;
            }
            return rv;
        }

        public void intent2Dialog(Intent intent) {
            ParcelableMessage exc = (ParcelableMessage)intent.getParcelableExtra("except0");
            if (exc==null)
                showProgressDialog(false,"");
            else {
                String idexc = exc.getId();
                //int headerid = intent.getIntExtra("EXCEPTION_MESSAGE_HEADER", -1);
                //String header = headerid==-1?"":res.getString(headerid)+"\n";
                String msgexc = getExceptionMessage(intent);
                if (idexc.indexOf("_errp_")>=0) {
                    showProgressDialog(true,msgexc);
                }
                else if (idexc.indexOf("_errr_")>=0) {
                    showProgressDialog(false,"");
                    makeToast(msgexc,exc.getMsgType());
                    //Toast.makeText(ActivityMain.this, msgexc, Toast.LENGTH_LONG).show();
                }
                else if (idexc.indexOf("_errs_")>=0) {
                    showProgressDialog(false, "");
                    showError(res.getString(R.string.exm_errs_exception)+"\n"+msgexc);
                }
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getAction();
            if (msg.equals(Messages.EXCEPTION_MESSAGE)) {
                intent2Dialog(intent);
            }
        }

        private void makeToast(String msgexc, ParcelableMessage.Type msgType) {
            LayoutInflater inflater = LayoutInflater.from(ActivityMain.this);
            View layout = inflater.inflate(R.layout.toastmodel, null);

            ImageView image = (ImageView) layout.findViewById(R.id.toast_image);
            if (msgType == ParcelableMessage.Type.OK)
                image.setImageResource(R.drawable.ic_action_shield_ok);
            else if (msgType == ParcelableMessage.Type.ERROR)
                image.setImageResource(R.drawable.ic_action_shield_error);
            else
                image.setImageResource(R.drawable.ic_action_shield_warning);

            TextView textV = (TextView) layout.findViewById(R.id.toast_text);
            textV.setText(msgexc);

            Toast toast = new Toast(ActivityMain.this);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
    };

    private void setupProgressDialog() {
        progress = new ProgressDialog(this);
        progress.setMessage(res.getString(R.string.exm_errp_conn_connecting));
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);
    }

    private void showProgressDialog(final boolean show, final String string) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (show) {
                    progress.cancel();
                    progress.setMessage(string);
                    progress.show();
                } else
                    progress.cancel();
            }
        });
    }

    @Override
    public void onBackPressed() {
        int ci = mPager.getCurrentItem();
        mPager.setCurrentItem(ci > 0 ? ci - 1 : mAdapter.getCount() - 1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mServiceConnection==null) {
            mServiceConnection = new DeviceManagerConnection();
            bindService(new Intent(this, ConnectionService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
        }
        setupIO(true);
    }

    @Override
    protected void onPause() {
        setupIO(false);
        mLastIbernationTime = System.currentTimeMillis();
        if (mServiceConnection!=null) {
            if (mBinder!=null) {
                mBinder.removeCommandProcessor(this);
                mBinder = null;
            }
            unbindService(mServiceConnection);
            mServiceConnection = null;
        }
        super.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(HIBERNATION_TIME, mLastIbernationTime);
        outState.putString(SELECTED_TAB, mAdapter.getDeviceItem(mPager.getCurrentItem()));
    }

    private void setupIO(boolean start) {
        if (start) {
            try {
                CA.lbm.unregisterReceiver(messageReceiver);
            } catch (Exception e) {
            }
            IntentFilter intentf = new IntentFilter();
            intentf.addAction(Messages.EXCEPTION_MESSAGE);
            CA.lbm.registerReceiver(messageReceiver, intentf);
            CA.lbm.sendBroadcast(new Intent(Messages.CMDGETCONSTATUS_MESSAGE).putExtra("t", mLastIbernationTime));
        }
        else {
            try {
                CA.lbm.unregisterReceiver(messageReceiver);
            } catch (Exception e) {
            }
        }
    }

    private void initDrawer() {
        mToolbar = (Toolbar) findViewById(R.id.app_bar);
        mContainerToolbar = (ViewGroup) findViewById(R.id.container_app_bar);
        //set the Toolbar as ActionBar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //setup the NavigationDrawer
        mDrawerFragment = (DrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
        mDrawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
    }


    public void onDrawerItemClicked(int index) {
        mPager.setCurrentItem(index);
    }

    public View getContainerToolbar() {
        return mContainerToolbar;
    }

    private void initTabs() {
        mTabHost = (MaterialTabHost) findViewById(R.id.materialTabHost);
        mPager = (ViewPager) findViewById(R.id.viewPager);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        //when the page changes in the ViewPager, update the Tabs accordingly
        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mTabHost.setSelectedNavigationItem(position);

            }
        });
        //Add all the Tabs to the TabHost
    }

    private void reloadTabs() {
        int redo = 1;

        if (deviceIdList!=null) {
            mDrawerFragment.refresh(deviceIdList);
            unProcessTabs();
            String k = null;
            int n = 0;
            while (true) {
                synchronized (deviceIdList) {
                    if (n<deviceIdList.size()) {
                        k = deviceIdList.get(n);
                        n++;
                    }
                }
                if (k!=null) {
                    MaterialTabHolder mh = deviceTabs.get(k);
                    if (mh == null) {
                        Device d = ParcelableUtil.unmarshallFromSharedPref(Device.CREATOR, "dev_" + k, sharedPref);
                        if (d!=null) {
                            mAdapter.addTab(d);
                            mh = new MaterialTabHolder(d);
                            mh.setProcessed(2);
                            deviceTabs.put(k, mh);
                        }
                    } else
                        mh.setProcessed(1);
                    k = null;
                }
                else
                    break;
            }

            for (Iterator<Map.Entry<String, MaterialTabHolder>> it = deviceTabs.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, MaterialTabHolder> entry = it.next();
                MaterialTabHolder mh = entry.getValue();
                int proc = mh.getProcessed();
                if (proc == 0) {
                    if (redo==1) redo = 0;
                    mAdapter.removeTab(entry.getKey());
                    it.remove();
                    mTabHost.removeTab(mh.getTab().getPosition());
                } else if (proc == 2) {
                    redo = 2;
                }
            }
            //la prima volta che viene chiamato con device non nullo devo cancellare la variabile per evitare problemi
            if (!mSelectedTab.isEmpty()) {
                int item = mAdapter.getDeviceIdx(mSelectedTab);
                if (item>0)
                    mPager.setCurrentItem(item);
                mSelectedTab = "";
            }
        }

        if (settingsTab==null) {
            settingsTab = mTabHost.newTab()
                    .setIcon(getResources().getDrawable(R.drawable.ic_action_settings)).setTabListener(this);
            mTabHost.addTab(settingsTab);
        }
        else if (redo==2) {
            for (Map.Entry<String, MaterialTabHolder> entry : deviceTabs.entrySet()) {
                MaterialTabHolder mh = entry.getValue();
                if (mh.getProcessed() == 2)
                    mTabHost.addTab(mh.getTab());
            }
            mTabHost.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present. 
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_settings:
                mPager.setCurrentItem(0);
                return true;
            case R.id.action_exit:
                showProgressDialog(false,"");
                if (mBinder!=null)
                    mBinder.processCommand(new ExitMessage());
                finish();
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onTabSelected(MaterialTab materialTab) {
        //when a Tab is selected, update the ViewPager to reflect the changes
        mPager.setCurrentItem(materialTab.getPosition());
    }

    @Override
    public void onTabReselected(MaterialTab materialTab) {
    }

    @Override
    public void onTabUnselected(MaterialTab materialTab) {
    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        private final String SETTINGS_KEY = "$___$";
        private ArrayList<Device> tabsList = new ArrayList<>();
        private HashMap<String,Fragment> fragList = new HashMap<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragList.put(SETTINGS_KEY,new SettingsFragment());
            tabsList.add(null);
            //fragmentManager = fm;
        }

        @Override
        public int getItemPosition(Object object) {
            // refresh all fragments when data set changed
            return PagerAdapter.POSITION_NONE;
        }

        public void removeTab(Device d) {
            fragList.remove(d.getMac());
            tabsList.remove(d);
            notifyDataSetChanged();
        }

        public void addTab(Device d) {
            Fragment frg;
            if (d instanceof DeviceS20)
                frg = new DeviceS20Fragment();
            else if (d instanceof DeviceVirtual || d instanceof DevicePrimelan)
                frg = new DeviceStateFragment();
            else
                frg = new DeviceAllOneFragment();
            Bundle b = new Bundle();
            b.putParcelable("dev", d);
            frg.setArguments(b);
            fragList.put(d.getMac(), frg);
            tabsList.add(d);
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int num) {
//            L.m("getItem called for " + num);
            Log.e(TAG,"Getting tab "+num);
            if (num==0 || num>=tabsList.size())
                return fragList.get(SETTINGS_KEY);
            else
                return fragList.get(tabsList.get(num).getMac());
        }

        @Override
        public int getCount() {
            return fragList.size();
        }

        @Override
        public CharSequence getPageTitle(int num) {
            CharSequence s;
            Device d;
            if (num==0 || num>=tabsList.size())
                s = getResources().getString(R.string.settings);
            else if ((d = tabsList.get(num)) instanceof DeviceS20)
                s = getResources().getString(R.string.s20)+" "+d.getName();
            else
                s = getResources().getString(R.string.allone)+" "+d.getName();
            return s;
        }

        private Drawable getIcon(int num) {
            int id;
            if (num==0 || num>=tabsList.size())
                id = R.drawable.ic_action_settings;
            else if (tabsList.get(num) instanceof DeviceS20)
                id = R.drawable.ic_action_s20;
            else
                id = R.drawable.ic_action_allone;
            return getResources().getDrawable(id);
        }

        public void removeTab(String mac) {
            for (Iterator<Device> iterator = tabsList.iterator(); iterator.hasNext();) {
                Device d = iterator.next();
                if (d!=null && d.getMac().equals(mac)) {
                    // Remove the current element from the iterator and the list.
                    iterator.remove();
                    break;
                }
            }
            fragList.remove(mac);
            notifyDataSetChanged();
        }

        public String getDeviceItem(int currentItem) {
            if (currentItem<=0 || currentItem>=tabsList.size())
                return "";
            else
                return tabsList.get(currentItem).getMac();
        }

        public int getDeviceIdx(String mac) {
            int i = 0;
            for (Iterator<Device> iterator = tabsList.iterator(); iterator.hasNext();) {
                Device d = iterator.next();
                if (d!=null && d.getMac().equals(mac)) {
                    // Remove the current element from the iterator and the list.
                    return i;
                }
                i++;
            }
            return -1;
        }

        @Override
        public Parcelable saveState() {
            return null;
        }

        public void clearTabs() {
            fragList.clear();
            tabsList.clear();
            notifyDataSetChanged();
        }
    }
} 
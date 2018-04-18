package niyo.nfc.com.nfcori;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import niyo.nfc.com.nfcori.db.HomeTableColumns;
import niyo.nfc.com.nfcori.fragments.CameraFragment;
import niyo.nfc.com.nfcori.fragments.LightsFragment;
import niyo.nfc.com.nfcori.fragments.OnFragmentInteractionListener;
import niyo.nfc.com.nfcori.fragments.PresenceFragment;
import niyo.nfc.com.nfcori.fragments.SensorsFragment;

import static niyo.nfc.com.nfcori.HarelHome.AUTHORITY;

public class Main2Activity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        NavigationView.OnNavigationItemSelectedListener,
        OnFragmentInteractionListener {

    public static final String LOG_TAG = Main2Activity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;
//    Account mAccount;
//    public static final String s_url = "https://oriharel.herokuapp.com";

    public static final String ACCOUNT_TYPE = "harelHome";
    // The account name
    public static final String ACCOUNT = "harelAccount";

    public static final long SECONDS_PER_MINUTE = 60;
    public static final long SYNC_INTERVAL_IN_MINUTES = 5;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;
    private ContentObserver mObserver;
    Handler mHandler = new Handler();
    private ViewPager mViewPager;
    public Boolean tallLampState;
    public Boolean sofaLampState;
    public Boolean windowLampState;

    public Boolean oriState;
    public Boolean yifatState;
    public Boolean itchukState;

    public String oriLastStateTime;
    public String yifatLastStateTime;
    public String itchukLastStateTime;

    public Boolean doorStatus = false;
    public Long doorStatusTime = -1L;
    public Boolean ginaStatus = false;
    public Long ginaStatusTime = -1L;


    public String temp;

    public String lastUpdateTime;

    public byte[] homeImage64;
    public byte[] homeCamImage64;

    private List<LampStateListener> mLampListeners;
    private List<PresenceStateListener> mPresenceListeners;
    private List<CameraStateListener> mCameraListeners;
    private List<SensorsStateListener> mSensorsListeners;

    public boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate started");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (findViewById(R.id.leftPane) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
        }

        if (mTwoPane) {
            setUpRecyclerView();
        }
        else {
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setVisibility(View.INVISIBLE);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close);
            drawer.setDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            navigationView.getMenu().getItem(0).setChecked(true);

            SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.vpcontainer);
            mViewPager.setAdapter(mSectionsPagerAdapter);
        }

        mLampListeners = new ArrayList<>();
        mPresenceListeners = new ArrayList<>();
        mCameraListeners = new ArrayList<>();
        mSensorsListeners = new ArrayList<>();

        setNfcListener();
        Intent notifIntent = new Intent(this, NotificationReceiver.class);
        sendBroadcast(notifIntent);
        CreateSyncAccount();
        setUpSync();
        sendRegistrationKey();

        final Main2Activity context = this;
        mObserver = new ContentObserver(mHandler) {
            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }

            @Override
            public void onChange(boolean selfChange) {
                Log.d(LOG_TAG, "onChange called from observer");
                getLoaderManager().restartLoader(0, null, context);
            }
        };
        registerForChanges();
        refreshData();
    }

    private void sendRegistrationKey() {
        Log.d(LOG_TAG, "sendRegistrationKey with "+FirebaseInstanceId.getInstance().getToken());
        HomeFirebaseService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken());
    }

    private void setUpRecyclerView() {
        List<ListPaneItem> items = new ArrayList<>();
        items.add(new ListPaneItem(R.drawable.ic_lightbulb_outline_white_48dp, getString(R.string.lights), LightsFragment.NAME));
        items.add(new ListPaneItem(R.drawable.ic_group_white_48dp, getString(R.string.presence), PresenceFragment.NAME));
        items.add(new ListPaneItem(R.drawable.ic_visibility_white_48dp, getString(R.string.cameras), CameraFragment.NAME));
        items.add(new ListPaneItem(R.drawable.ic_visibility_white_48dp, getString(R.string.sensors), SensorsFragment.NAME));
        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.leftPane);
        recyclerView.setAdapter(new SimpleItemRecyclerViewAdapter(items));

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.item_detail_container, LightsFragment.newInstance(0))
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);

        final ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                Log.d(LOG_TAG, "cam stopped successfully");

            }

            @Override
            public void failure(Object data, String description) {
                Log.e(LOG_TAG, "failed to stop cam");
            }
        };

        GenericHttpRequestTask task = new GenericHttpRequestTask(caller);
        String camUrl = Utils.getHomeURL()+"/cam/stop?uuid=f589cad6-49bf-4d1b-9091-4ba9ef1d466b";
        task.execute(camUrl);
    }

    private void registerForChanges() {
        getContentResolver().registerContentObserver(HarelHome.HOME_STATE_URI, false, mObserver);
    }

    private void setNfcListener() {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Log.d(LOG_TAG, "App launched form NFC");
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                    NdefRecord record = msgs[i].getRecords()[0];
                    byte[] payload = record.getPayload();
                    try {
                        String decoded = new String(payload, "UTF-8");
                        Log.d(LOG_TAG, "received nfc with: " + decoded);
                        if (decoded.contains("homeDoor")) {
                            turnTheLights("off");
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            Log.d(LOG_TAG, "no NFC detected");
        }
    }

    private void setUpSync() {
        Log.d(LOG_TAG, "setUpSync started");
        AccountManager accountManager =
                (AccountManager) getSystemService(
                        ACCOUNT_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            Log.d(LOG_TAG, "need user to authorize get accounts");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS},
                    MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
        } else {
            Log.d(LOG_TAG, "get accounts permission already granted");
            Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);

            ContentResolver.setSyncAutomatically(accounts[0], AUTHORITY, true);
            Bundle syncExtras = new Bundle();
            ContentResolver.addPeriodicSync(accounts[0], AUTHORITY, syncExtras, SYNC_INTERVAL);
//            runSyncManually();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    setUpSync();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void CreateSyncAccount() {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            Log.d(LOG_TAG, "account added successfully");
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.d(LOG_TAG, "The account exists or some other error occurred");
        }
    }

    public void turnSingleLight(final String id, final ImageView bulbImage, final String socketName) {


        String currState = (String)bulbImage.getTag();
        if (currState == null) {
            currState = "off";
        }

        Log.d(LOG_TAG, "turnSingleLight started with "+id+" current state is: "+currState);

        String state = "on";
        if (currState.equals("on")) {
            state = "off";
        }

        final Context context = this;

        final ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                Log.d(LOG_TAG, id+" set state successfully");

                Snackbar.make(bulbImage, socketName + " turned  state  successfully", Snackbar.LENGTH_LONG)
                        .show();
                String dataStr = (String)data;
                try {
                    HomeStateFetchService.processState(context, new JSONObject(dataStr), getIntent());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "unable to parse "+dataStr);
                }

            }

            @Override
            public void failure(Object data, String description) {
                Snackbar.make(bulbImage, "Failed to turn state " + socketName, Snackbar.LENGTH_LONG)
                        .setAction("Retry", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Log.d(LOG_TAG, "wanna do something...");
                            }
                        }).show();
            }
        };
        GenericHttpRequestTask task = new GenericHttpRequestTask(caller);
        String url = Utils.getHomeURL() + "/sockets/" + id + "/" + state;
        task.execute(url, "true");
    }

    public void turnTheLights(final String state) {

        final Context context = this;

        final ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                Log.d(LOG_TAG, "all sockets are " + state + "?");
                String dataStr = (String)data;
                try {
                    HomeStateFetchService.processState(context, new JSONObject(dataStr), getIntent());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "unable to parse "+dataStr);
                }
            }

            @Override
            public void failure(Object data, String description) {

            }
        };

        GenericHttpRequestTask task = new GenericHttpRequestTask(caller);
        String url = Utils.getHomeURL() + "/all/sockets/" + state;
        task.execute(url, "true");

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this, HarelHome.HOME_STATE_URI,
                HarelHome.HOME_STATE_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        Log.d(LOG_TAG, "onLoadFinished started");

        if (cursor.moveToFirst()) {
            int colTallStateIndex = cursor.getColumnIndex(HomeTableColumns.TALL_LAMP_STATE);
            String tallLampStateStr = cursor.getString(colTallStateIndex);
            tallLampState = Boolean.valueOf(tallLampStateStr);

            int colSofaStateIndex = cursor.getColumnIndex(HomeTableColumns.SOFA_LAMP_STATE);
            String sofaLampStateStr = cursor.getString(colSofaStateIndex);
            sofaLampState = Boolean.valueOf(sofaLampStateStr);

            int colWindowStateIndex = cursor.getColumnIndex(HomeTableColumns.WINDOW_LAMP_STATE);
            String windowLampStateStr = cursor.getString(colWindowStateIndex);
            windowLampState = Boolean.valueOf(windowLampStateStr);

            int oriPresIndex = cursor.getColumnIndex(HomeTableColumns.ORI_PRESENCE);
            String oriStateStr = cursor.getString(oriPresIndex);
            oriState = oriStateStr.toLowerCase().equals("home");

            int oriLastIndex = cursor.getColumnIndex(HomeTableColumns.ORI_LAST_PRESENCE);
            oriLastStateTime = cursor.getString(oriLastIndex);

            int yifatPresIndex = cursor.getColumnIndex(HomeTableColumns.YIFAT_PRESENCE);
            String yifatStateStr = cursor.getString(yifatPresIndex);
            yifatState = yifatStateStr.toLowerCase().equals("home");

            int yifatLastIndex = cursor.getColumnIndex(HomeTableColumns.YIFAT_LAST_PRESENCE);
            yifatLastStateTime = cursor.getString(yifatLastIndex);

            int itchukPresIndex = cursor.getColumnIndex(HomeTableColumns.ITCHUK_PRESENCE);
            String itchukStateStr = cursor.getString(itchukPresIndex);
            itchukState = itchukStateStr.toLowerCase().equals("home");

            int itchukLastIndex = cursor.getColumnIndex(HomeTableColumns.ITCHUK_LAST_PRESENCE);
            itchukLastStateTime = cursor.getString(itchukLastIndex);

            int lastUpdateIndex = cursor.getColumnIndex(HomeTableColumns.LAST_UPDATE_TIME);
            lastUpdateTime = cursor.getString(lastUpdateIndex);

            int homeImageIndex = cursor.getColumnIndex(HomeTableColumns.HOME_PIC);
            homeImage64 = cursor.getBlob(homeImageIndex);

            int tempIndex = cursor.getColumnIndex(HomeTableColumns.HOME_TEMP);
            temp = cursor.getString(tempIndex);

            if (homeImage64 != null) {
                Log.d(LOG_TAG, "received imageBase64: "+homeImage64.length);
            }

            int camHomeImageIndex = cursor.getColumnIndex(HomeTableColumns.HOME_CAM_PIC);
            homeCamImage64 = cursor.getBlob(camHomeImageIndex);

            int doorStatusIndex = cursor.getColumnIndex(HomeTableColumns.DOOR_STATUS);
            if (doorStatusIndex >= 0) {
                String doorStatusStr = cursor.getString(doorStatusIndex);
                if (doorStatusStr != null) {
                    Log.d(LOG_TAG, "door status is: "+doorStatusStr);
                    doorStatus = doorStatusStr.equals("Closed");
                }

            }


            int doorStatusTimeIndex = cursor.getColumnIndex(HomeTableColumns.DOOR_STATUS_TIME);

            if (doorStatusTimeIndex >= 0) {
                doorStatusTime = cursor.getLong(doorStatusTimeIndex);
                Log.d(LOG_TAG, "doorStatusTime: "+doorStatusTime);
            }


            int ginaStatusIndex = cursor.getColumnIndex(HomeTableColumns.GINA_STATUS);
            if (ginaStatusIndex >= 0) {
                String ginaStatusStr = cursor.getString(ginaStatusIndex);
                if (ginaStatusStr != null) {
                    ginaStatus = ginaStatusStr.equals("Closed");
                }

            }

            int ginaStatusTimeIndex = cursor.getColumnIndex(HomeTableColumns.GINA_STATUS_TIME);

            if (ginaStatusTimeIndex >= 0) {
                ginaStatusTime = cursor.getLong(ginaStatusTimeIndex);
            }


            if (homeCamImage64 != null) {
                Log.d(LOG_TAG, "received homeCamImage64: "+homeCamImage64.length);
            }

            for (LampStateListener listener :
                    mLampListeners) {
                listener.onChange(tallLampState, sofaLampState, windowLampState, temp);
            }

            for (PresenceStateListener listener :
                    mPresenceListeners) {
                listener.onChange(
                        oriState,
                        oriLastStateTime,
                        yifatState,
                        yifatLastStateTime,
                        itchukState,
                        itchukLastStateTime,
                        lastUpdateTime);
            }

            for (CameraStateListener listener :
                    mCameraListeners) {
                listener.onChange(homeImage64);
            }

            for (SensorsStateListener listener :
                    mSensorsListeners) {
                listener.onChange(doorStatus, doorStatusTime, ginaStatus, ginaStatusTime);
            }

            Log.d(LOG_TAG, "tallLampState is: "+tallLampStateStr+
                    " sofaLampState: "+sofaLampStateStr+
                    " windowLampState: "+windowLampStateStr);
            Log.d(LOG_TAG, "ori is "+oriState);

            if (!mTwoPane) {
                NavigationView navView = (NavigationView)findViewById(R.id.nav_view);
                View navHeader = navView.getHeaderView(0);
                TextView greeting = (TextView)navHeader.findViewById(R.id.greetingText);
                if (oriState) {
                    greeting.setText("Welcome Home Ori!");
                }
                else {
                    greeting.setText("We miss you Ori!");
                }
            }
            else {
                TextView tempLarge = (TextView)findViewById(R.id.tempLarge);
                Double tempDbl = Double.valueOf(temp);
                String formattedTemp = new DecimalFormat("#.#").format(tempDbl) + "\u00B0";
                tempLarge.setText(formattedTemp);
            }

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {}

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private void refreshData(){
        Intent serviceIntent = new Intent(this, HomeStateFetchService.class);
        serviceIntent.putExtra(HomeStateFetchService.EVENT_NAMT_EXTRA,
                HomeStateFetchService.LAST_STATE_EVENT_NAME);
        startService(serviceIntent);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        if (id == R.id.action_refresh) {
            refreshData();
        }

        if (id == R.id.cam_activate) {
            cameraAction(true);
        }

        if (id == R.id.cam_deactivate) {
            cameraAction(false);
        }

        return super.onOptionsItemSelected(item);
    }

    private void cameraAction(boolean active) {
        final String action = active ? "activate" : "deActivate";
        final View parentView = findViewById(R.id.drawer_layout);

        ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {
                Snackbar.make(parentView, "Camera "+action+"d", Snackbar.LENGTH_LONG)
                        .show();
            }

            @Override
            public void failure(Object data, String description) {
                Snackbar.make(parentView, "Camera can not "+action, Snackbar.LENGTH_LONG)
                        .show();
            }
        };



        GenericHttpRequestTask task = new GenericHttpRequestTask(caller);
        String camUrl = Utils.getHomeURL()+"/"+action+"/cam?uuid=f589cad6-49bf-4d1b-9091-4ba9ef1d466b";
        task.execute(camUrl, "true");
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_lights) {
            mViewPager.setCurrentItem(0);
        } else if (id == R.id.nav_presence) {
            mViewPager.setCurrentItem(1);
        } else if (id == R.id.nav_cameras) {
            mViewPager.setCurrentItem(2);
        } else if (id == R.id.nav_sensors) {
            mViewPager.setCurrentItem(3);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    public void registerForLampsStateChange(LampStateListener listener) {
        mLampListeners.add(listener);
    }

    @Override
    public void registerForPresenceChange(PresenceStateListener listener) {
        mPresenceListeners.add(listener);
    }

    @Override
    public void registerForCameraChange(CameraStateListener listener) {
        mCameraListeners.add(listener);
    }

    @Override
    public void registerForSensorsChange(SensorsStateListener listener) {
        mSensorsListeners.add(listener);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0:
                    return LightsFragment.newInstance(position + 1);
                case 1:
                    return PresenceFragment.newInstance(position + 2);
                case 2:
                    return CameraFragment.newInstance(position + 3);
                case 3:
                    return SensorsFragment.newInstance(position + 4);
                default:
                    return LightsFragment.newInstance(position+1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Lights";
                case 1:
                    return "Presence";
                case 2:
                    return "Cameras";
                case 3:
                    return "Sensors";
            }
            return null;
        }
    }

    public class SimpleItemRecyclerViewAdapter extends
            RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {

        List<ListPaneItem> mItems;

        public SimpleItemRecyclerViewAdapter(List<ListPaneItem> items) {
            mItems = items;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.pane_item_layout, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mItem = mItems.get(position);
            holder.mImageView.setImageResource(mItems.get(position).mImageResource);
            holder.mTextView.setText(mItems.get(position).mText);

            holder.mLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ListPaneItem item = mItems.get(position);
                    Fragment fragment;
                    switch (item.mId) {
                        case(LightsFragment.NAME):
                            fragment = LightsFragment.newInstance(0);
                            break;
                        case(PresenceFragment.NAME):
                            fragment = PresenceFragment.newInstance(1);
                            break;
                        case(CameraFragment.NAME):
                            fragment = CameraFragment.newInstance(2);
                            break;
                        case(SensorsFragment.NAME):
                            fragment = SensorsFragment.newInstance(3);
                            break;
                        default:
                            fragment = LightsFragment.newInstance(0);
                    }

                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.item_detail_container, fragment)
                            .commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            final View mLayout;
            final ImageView mImageView;
            final TextView mTextView;
            ListPaneItem mItem;

            ViewHolder(View itemView) {
                super(itemView);
                mLayout = itemView;
                mImageView = (ImageView)itemView.findViewById(R.id.paneItemImage);
                mTextView = (TextView)itemView.findViewById(R.id.paneItemText);
            }
        }

    }

    private class ListPaneItem {
        int mImageResource;
        String mText;
        String mId;

        ListPaneItem(int imageResource, String text, String id) {
            mImageResource = imageResource;
            mText = text;
            mId = id;
        }
    }

}

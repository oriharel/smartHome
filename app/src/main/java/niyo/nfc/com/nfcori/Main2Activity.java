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
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import niyo.nfc.com.nfcori.db.HomeTableColumns;
import niyo.nfc.com.nfcori.fragments.CameraFragment;
import niyo.nfc.com.nfcori.fragments.LightsFragment;
import niyo.nfc.com.nfcori.fragments.OnFragmentInteractionListener;
import niyo.nfc.com.nfcori.fragments.PresenceFragment;

import static niyo.nfc.com.nfcori.HarelHome.AUTHORITY;

public class Main2Activity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        NavigationView.OnNavigationItemSelectedListener,
        OnFragmentInteractionListener {

    public static final String LOG_TAG = Main2Activity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;
//    Account mAccount;
    public static final String s_url = "https://oriharel.herokuapp.com";

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
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    public Boolean tallLampState;
    public Boolean sofaLampState;
    public Boolean windowLampState;

    public Boolean oriState;
    public Boolean yifatState;

    public String oriLastStateTime;
    public String yifatLastStateTime;

    public String lastUpdateTime;

    private List<LampStateListener> mLampListeners;
    private List<PresenceStateListener> mPresenceListeners;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.vpcontainer);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        PagerTabStrip strip = (PagerTabStrip)findViewById(R.id.vpstrip);
//        strip.setTextSpacing(40);
//        strip.setPadding(40, 40, 40, 40);
        mLampListeners = new ArrayList<>();
        mPresenceListeners = new ArrayList<>();

        setNfcListener();
        Intent notifIntent = new Intent("com.niyo.updateNotification");
        sendBroadcast(notifIntent);
        CreateSyncAccount();
        setUpSync();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this);
    }

    private void registerForChanges() {
        getContentResolver().registerContentObserver(HarelHome.HOME_STATE_URI, false, mObserver);
    }

    private void setNfcListener() {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
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
    }

    private void setUpSync() {
        AccountManager accountManager =
                (AccountManager) getSystemService(
                        ACCOUNT_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.GET_ACCOUNTS}, MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
        } else {
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
        Boolean stateBool = true;
        if (currState.equals("on")) {
            state = "off";
            stateBool = false;
        }

//        updateBulbImage(bulbImage, stateBool);

        final Context context = this;

        final ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                Log.d(LOG_TAG, id+" set state successfully");

                Snackbar.make(bulbImage, socketName + " turned  state  successfully", Snackbar.LENGTH_LONG)
                        .show();
                Intent serviceIntent = new Intent(context, HomeStateFetchService.class);
                serviceIntent.putExtra(HomeStateFetchService.EVENT_NAMT_EXTRA,
                        HomeStateFetchService.STATE_EVENT_NAME);
                context.startService(serviceIntent);

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
        String url = s_url + "/sockets/" + id + "/" + state;
        task.execute(url, "true");
    }

    public void turnTheLights(final String state) {

        final Context context = this;

        final ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                Log.d(LOG_TAG, "all sockets are " + state + "?");
                Intent serviceIntent = new Intent(context, HomeStateFetchService.class);
                serviceIntent.putExtra(HomeStateFetchService.EVENT_NAMT_EXTRA,
                        HomeStateFetchService.STATE_EVENT_NAME);
                context.startService(serviceIntent);
            }

            @Override
            public void failure(Object data, String description) {

            }
        };

        GenericHttpRequestTask task = new GenericHttpRequestTask(caller);
        String url = s_url + "/all/sockets/" + state;
        task.execute(url, "true");

    }

    private void runSyncManually() {
        Log.d(LOG_TAG, "runSyncManually started");
        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(
                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        /*
         * Request the sync for the default account, authority, and
         * manual sync settings
         */

        AccountManager accountManager =
                (AccountManager) getSystemService(
                        ACCOUNT_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        ContentResolver.requestSync(accounts[0], AUTHORITY, settingsBundle);
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

            int lastUpdateIndex = cursor.getColumnIndex(HomeTableColumns.LAST_UPDATE_TIME);
            lastUpdateTime = cursor.getString(lastUpdateIndex);

            for (LampStateListener listener :
                    mLampListeners) {
                listener.onChange(tallLampState, sofaLampState, windowLampState);
            }

            for (PresenceStateListener listener :
                    mPresenceListeners) {
                listener.onChange(oriState, oriLastStateTime, yifatState, yifatLastStateTime, lastUpdateTime);
            }

//            int homeImageIndex = cursor.getColumnIndex(HomeTableColumns.HOME_PIC);
//            byte[] homeImage64 = cursor.getBlob(homeImageIndex);
//            Log.d(LOG_TAG, "received imageBase64: "+homeImage64.length);
//            ImageView homeImageView = (ImageView)findViewById(R.id.homeImage);
//            byte[] decodedString = Base64.decode(homeImage64, Base64.DEFAULT);
//            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//            homeImageView.setImageBitmap(decodedByte);

            Log.d(LOG_TAG, "tallLampState is: "+tallLampStateStr+
                    " sofaLampState: "+sofaLampStateStr+
                    " windowLampState: "+windowLampStateStr);
            Log.d(LOG_TAG, "ori is "+oriState);
        }


    }

    private void updateBulbImage(ImageView bulb, Boolean bulbState) {
        bulb.setImageResource(bulbState ? R.drawable.on_bulb : R.drawable.off_bulb);
        bulb.setTag(bulbState ? "on" : "off");
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

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

//    public static class ImageFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        public ImageFragment() {
//        }
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static ImageFragment newInstance(int sectionNumber) {
//            ImageFragment fragment = new ImageFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.home_main, container, false);
////            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
////            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
//            return rootView;
//        }
//    }

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
                default:
                    return LightsFragment.newInstance(position+1);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
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
            }
            return null;
        }
    }

}

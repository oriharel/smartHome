package niyo.nfc.com.nfcori;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import java.io.UnsupportedEncodingException;

import static niyo.nfc.com.nfcori.HarelHome.AUTHORITY;

public class Main2Activity extends AppCompatActivity {

    public static final String LOG_TAG = Main2Activity.class.getSimpleName();
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;
    Account mAccount;
    public static final String s_url = "https://oriharel.herokuapp.com";

    public static final String ACCOUNT_TYPE = "harelHome";
    // The account name
    public static final String ACCOUNT = "harelAccount";

    public static final long SECONDS_PER_MINUTE = 60;
    public static final long SYNC_INTERVAL_IN_MINUTES = 1;
    public static final long SYNC_INTERVAL =
            SYNC_INTERVAL_IN_MINUTES *
                    SECONDS_PER_MINUTE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
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
        fab.setVisibility(View.INVISIBLE);

        Button offButton = (Button) findViewById(R.id.offButton);
        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnTheLights("off");
            }
        });

        Button onButton = (Button) findViewById(R.id.onButton);
        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnTheLights("on");
            }
        });

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
                            turnTheLights("on");
                        }
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        final Button tallLampOn = (Button) findViewById(R.id.tallOn);
        tallLampOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnSingleLight("tallLamp", "on", tallLampOn, "Tall Lamp");
            }
        });
        final Button tallLampOff = (Button) findViewById(R.id.tallOff);
        tallLampOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnSingleLight("tallLamp", "off", tallLampOff, "Tall Lamp");
            }
        });

        final Button sofaLampOn = (Button) findViewById(R.id.sofaOn);
        sofaLampOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnSingleLight("sofaLamp", "on", sofaLampOn, "Sofa Lamp");
            }
        });

        final Button sofaLampOff = (Button) findViewById(R.id.sofaOff);
        sofaLampOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnSingleLight("sofaLamp", "off", sofaLampOff, "Sofa Lamp");
            }
        });

        final Button windowLampOn = (Button) findViewById(R.id.windowOn);
        windowLampOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnSingleLight("windowLamp", "on", windowLampOn, "Window Lamp");
            }
        });

        final Button windowLampOff = (Button) findViewById(R.id.windowOff);
        windowLampOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                turnSingleLight("windowLamp", "off", windowLampOff, "Window Lamp");
            }
        });

        setupNotification();
        CreateSyncAccount();
        setUpSync();
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
            runSyncManually();
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

    private void setupNotification() {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.bulb)
                        .setTicker("My notification")
                        .setOngoing(true);
        mBuilder.setCustomContentView(getComplexNotificationViewMin());
        mBuilder.setCustomBigContentView(getComplexNotificationViewEx());
        Intent resultIntent = new Intent(this, Main2Activity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(Main2Activity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setPriority(Notification.PRIORITY_MAX);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private RemoteViews getComplexNotificationViewMin() {
        RemoteViews notificationView = new RemoteViews(
                getPackageName(),
                R.layout.notif_layout_min
        );
        notificationView.setImageViewResource(
                R.id.tallBulbMin,
                R.drawable.bulb);
        notificationView.setImageViewResource(
                R.id.sofaBulbMin,
                R.drawable.bulb);
        notificationView.setImageViewResource(
                R.id.windowBulbMin,
                R.drawable.bulb);
        return notificationView;
    }

    private RemoteViews getComplexNotificationViewEx() {
        RemoteViews notificationView = new RemoteViews(
                getPackageName(),
                R.layout.notif_layout_ex
        );
        notificationView.setImageViewResource(
                R.id.tallBulbEx,
                R.drawable.bulb);
        notificationView.setImageViewResource(
                R.id.sofaBulbEx,
                R.drawable.bulb);
        notificationView.setImageViewResource(
                R.id.windowBulbEx,
                R.drawable.bulb);

        Intent lightsIntent = new Intent(this, LightsBroadcastReceiver.class);
        PendingIntent toggleAllIntent = PendingIntent.getBroadcast(this, 0, lightsIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.toggleAll, toggleAllIntent);
        return notificationView;
    }

    private void turnSingleLight(final String id, final String state, final Button btn, final String socketName) {
        final ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                Log.d(LOG_TAG, "tallLamp is " + state + "?");

                Snackbar.make(btn, socketName + " turned " + state + " successfully", Snackbar.LENGTH_LONG)
                        .show();

            }

            @Override
            public void failure(Object data, String description) {
                Snackbar.make(btn, "Failed to turn " + state + " " + socketName, Snackbar.LENGTH_LONG)
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

    private void turnTheLights(final String state) {

        final ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                Log.d(LOG_TAG, "all sockets are " + state + "?");
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

}

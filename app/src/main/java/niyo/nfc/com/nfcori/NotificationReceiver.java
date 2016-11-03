package niyo.nfc.com.nfcori;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;

public class NotificationReceiver extends BroadcastReceiver {
    public static final String LOG_TAG = NotificationReceiver.class.getSimpleName();
    public NotificationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(LOG_TAG, "received notif intent");

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(HarelHome.HOME_STATE_URI, HarelHome.HOME_STATE_PROJECTION, null, null, null);
        assert cursor != null;

        if(cursor.moveToFirst() ){
            int colTallStateIndex = cursor.getColumnIndex(HomeTableColumns.TALL_LAMP_STATE);
            String tallLampStateStr = cursor.getString(colTallStateIndex);
            Boolean tallLampState = Boolean.valueOf(tallLampStateStr);

            int colSofaStateIndex = cursor.getColumnIndex(HomeTableColumns.SOFA_LAMP_STATE);
            String sofaLampStateStr = cursor.getString(colSofaStateIndex);
            Boolean sofaLampState = Boolean.valueOf(sofaLampStateStr);

            int colWindowStateIndex = cursor.getColumnIndex(HomeTableColumns.WINDOW_LAMP_STATE);
            String windowLampStateStr = cursor.getString(colWindowStateIndex);
            Boolean windowLampState = Boolean.valueOf(windowLampStateStr);

            int oriPresIndex = cursor.getColumnIndex(HomeTableColumns.ORI_PRESENCE);
            String oriState = cursor.getString(oriPresIndex);

            Log.d(LOG_TAG, "tallLampState is: "+tallLampStateStr+
                    " sofaLampState: "+sofaLampStateStr+
                    " windowLampState: "+windowLampStateStr);
            Log.d(LOG_TAG, "ori is "+oriState);
            setupNotification(context, tallLampState, sofaLampState, windowLampState);
        }
        else {
            Log.e(LOG_TAG, "Error no cursor!");
        }

        cursor.close();


    }

    private void setupNotification(Context context,
                                   Boolean tallLampState,
                                   Boolean sofaLampState,
                                   Boolean windowLampState) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.on_bulb)
                        .setTicker("My notification")
                        .setOngoing(true);
        mBuilder.setCustomContentView(getComplexNotificationViewMin(context, tallLampState, sofaLampState, windowLampState));
        mBuilder.setCustomBigContentView(getComplexNotificationViewEx(context, tallLampState, sofaLampState, windowLampState));
        Intent resultIntent = new Intent(context, Main2Activity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
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
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }

    private RemoteViews getComplexNotificationViewMin(Context context,
                                                      Boolean tallLampState,
                                                      Boolean sofaLampState,
                                                      Boolean windowLampState) {
        RemoteViews notificationView = new RemoteViews(
                context.getPackageName(),
                R.layout.notif_layout_min
        );
        int tallBulb = tallLampState ? R.drawable.on_bulb : R.drawable.off_bulb;
        int sofaBulb = sofaLampState ? R.drawable.on_bulb : R.drawable.off_bulb;
        int windowBulb = windowLampState ? R.drawable.on_bulb : R.drawable.off_bulb;
        notificationView.setImageViewResource(
                R.id.tallBulbMin,
                tallBulb);
        notificationView.setImageViewResource(
                R.id.sofaBulbMin,
                sofaBulb);
        notificationView.setImageViewResource(
                R.id.windowBulbMin,
                windowBulb);

        Intent tallIntent = new Intent(context, LightsBroadcastReceiver.class);
        tallIntent.putExtra(LightsBroadcastReceiver.BULB_NAME_EXTRA, LightsBroadcastReceiver.TALL_LAMP);
        PendingIntent tallPendingIntent = PendingIntent.getBroadcast(context, 0, tallIntent, 0);

        Intent sofaIntent = new Intent(context, LightsBroadcastReceiver.class);
        sofaIntent.putExtra(LightsBroadcastReceiver.BULB_NAME_EXTRA, LightsBroadcastReceiver.SOFA_LAMP);
        PendingIntent sofaPendingIntent = PendingIntent.getBroadcast(context, 1, sofaIntent, 0);

        Intent windowIntent = new Intent(context, LightsBroadcastReceiver.class);
        windowIntent.putExtra(LightsBroadcastReceiver.BULB_NAME_EXTRA, LightsBroadcastReceiver.WINDOW_LAMP);
        PendingIntent windowPendingIntent = PendingIntent.getBroadcast(context, 2, windowIntent, 0);

        notificationView.setOnClickPendingIntent(R.id.tallBulbMin, tallPendingIntent);
        notificationView.setOnClickPendingIntent(R.id.sofaBulbMin, sofaPendingIntent);
        notificationView.setOnClickPendingIntent(R.id.windowBulbMin, windowPendingIntent);

        Log.d(LOG_TAG, "setting up small notification with states: tallLampState: "+tallLampState+
                " sofaLampState: "+sofaLampState+
                " windowLampState: "+windowLampState);

        return notificationView;
    }

    private RemoteViews getComplexNotificationViewEx(Context context,
                                                     Boolean tallLampState,
                                                     Boolean sofaLampState,
                                                     Boolean windowLampState) {
        RemoteViews notificationView = new RemoteViews(
                context.getPackageName(),
                R.layout.notif_layout_ex
        );

        int tallBulb = tallLampState ? R.drawable.on_bulb : R.drawable.off_bulb;
        int sofaBulb = sofaLampState ? R.drawable.on_bulb : R.drawable.off_bulb;
        int windowBulb = windowLampState ? R.drawable.on_bulb : R.drawable.off_bulb;

        notificationView.setImageViewResource(
                R.id.tallBulbEx,
                tallBulb);
        notificationView.setImageViewResource(
                R.id.sofaBulbEx,
                sofaBulb);
        notificationView.setImageViewResource(
                R.id.windowBulbEx,
                windowBulb);

        Intent lightsOnIntent = new Intent(context, LightsBroadcastReceiver.class);
        lightsOnIntent.putExtra(LightsBroadcastReceiver.ALL_STATE_EXTRA, "on");
        PendingIntent allOnIntent = PendingIntent.getBroadcast(context, 8, lightsOnIntent, 0);

        Intent lightsOffIntent = new Intent(context, LightsBroadcastReceiver.class);
        lightsOffIntent.putExtra(LightsBroadcastReceiver.ALL_STATE_EXTRA, "off");
        PendingIntent allOffIntent = PendingIntent.getBroadcast(context, 7, lightsOffIntent, 0);

        Intent tallIntent = new Intent(context, LightsBroadcastReceiver.class);
        tallIntent.putExtra(LightsBroadcastReceiver.BULB_NAME_EXTRA, LightsBroadcastReceiver.TALL_LAMP);
        PendingIntent tallPendingIntent = PendingIntent.getBroadcast(context, 4, tallIntent, 0);

        Intent sofaIntent = new Intent(context, LightsBroadcastReceiver.class);
        sofaIntent.putExtra(LightsBroadcastReceiver.BULB_NAME_EXTRA, LightsBroadcastReceiver.SOFA_LAMP);
        PendingIntent sofaPendingIntent = PendingIntent.getBroadcast(context, 5, sofaIntent, 0);

        Intent windowIntent = new Intent(context, LightsBroadcastReceiver.class);
        windowIntent.putExtra(LightsBroadcastReceiver.BULB_NAME_EXTRA, LightsBroadcastReceiver.WINDOW_LAMP);
        PendingIntent windowPendingIntent = PendingIntent.getBroadcast(context, 6, windowIntent, 0);

        notificationView.setOnClickPendingIntent(R.id.allOnNotif, allOnIntent);
        notificationView.setOnClickPendingIntent(R.id.allOffNotif, allOffIntent);
        notificationView.setOnClickPendingIntent(R.id.tallBulbEx, tallPendingIntent);
        notificationView.setOnClickPendingIntent(R.id.sofaBulbEx, sofaPendingIntent);
        notificationView.setOnClickPendingIntent(R.id.windowBulbEx, windowPendingIntent);

        Log.d(LOG_TAG, "setting up big notification with states: tallLampState: "+tallLampState+
                " sofaLampState: "+sofaLampState+
                " windowLampState: "+windowLampState);

        return notificationView;
    }


}

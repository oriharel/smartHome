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
        cursor.moveToFirst();

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

        cursor.close();

        Log.d(LOG_TAG, "tallLampState is: "+tallLampStateStr+
                " sofaLampState: "+sofaLampStateStr+
                " windowLampState: "+windowLampStateStr);
        Log.d(LOG_TAG, "ori is "+oriState);
        setupNotification(context, tallLampState, sofaLampState, windowLampState);
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

        Intent lightsIntent = new Intent(context, LightsBroadcastReceiver.class);
        PendingIntent toggleAllIntent = PendingIntent.getBroadcast(context, 0, lightsIntent, 0);
        notificationView.setOnClickPendingIntent(R.id.toggleAll, toggleAllIntent);
        return notificationView;
    }
}

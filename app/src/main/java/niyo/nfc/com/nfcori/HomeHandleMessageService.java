package niyo.nfc.com.nfcori;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class HomeHandleMessageService extends FirebaseMessagingService {
    public static final String LOG_TAG = HomeHandleMessageService.class.getSimpleName();
    public HomeHandleMessageService() {

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(LOG_TAG, "From: " + remoteMessage.getFrom());
        Map<String, String> data = remoteMessage.getData();
        String title = null;
        String body = null;
        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
        }
        else {
            title = data.get("title");
            body = data.get("body");
        }
        String message = data.get("msg");
        Log.d(LOG_TAG, "onMessageReceived. refreshing state with "+message);
        Log.d(LOG_TAG, "onMessageReceived. title with "+title);
        Log.d(LOG_TAG, "onMessageReceived. body with "+body);

        if (message != null && (message.equals("getState") || message.equals("presence"))) {
            Intent serviceIntent = new Intent(this, HomeStateFetchService.class);
            serviceIntent.putExtra(HomeStateFetchService.EVENT_NAMT_EXTRA,
                    HomeStateFetchService.LAST_STATE_EVENT_NAME);
            serviceIntent.putExtra(HomeStateFetchService.TRIGGER_EXTRA, HomeStateFetchService.PUSH);
            serviceIntent.putExtra(HomeStateFetchService.PUSH_TITLE_EXTRA, title);
            serviceIntent.putExtra(HomeStateFetchService.PUSH_BODY_EXTRA, body);
            startService(serviceIntent);
        }


    }
}

package niyo.nfc.com.nfcori;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
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
        Map<String, String> data = remoteMessage.getData();
        String message = data.get("msg");
        Log.d(LOG_TAG, "onMessageReceived. refreshing state with "+message);

        if (message.equals("getState")) {
            Intent serviceIntent = new Intent(this, HomeStateFetchService.class);
            serviceIntent.putExtra(HomeStateFetchService.EVENT_NAMT_EXTRA,
                    HomeStateFetchService.LAST_STATE_EVENT_NAME);
            startService(serviceIntent);
        }


    }
}

package niyo.nfc.com.nfcori;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by oriharel on 03/03/2017.
 */

public class HomeFirebaseService extends FirebaseInstanceIdService {
    public static final String LOG_TAG = HomeFirebaseService.class.getSimpleName();
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(LOG_TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(refreshedToken);
    }

    public static void sendRegistrationToServer(String refreshedToken) {
        ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {
                Log.d(LOG_TAG, "registration token sent successfully. from server: "+data);
            }

            @Override
            public void failure(Object data, String description) {
                Log.e(LOG_TAG, "failed to send registration token "+description);
            }
        };

        GenericHttpRequestTask task = new GenericHttpRequestTask(caller);
        String registrationUrl = Utils.getHomeURL()+"/push/register";
        JSONObject params = new JSONObject();
        try {
            params.put("token", refreshedToken);
            params.put("email", "ori.harel@gmail.com");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        task.execute(registrationUrl, "true", params.toString());
    }
}

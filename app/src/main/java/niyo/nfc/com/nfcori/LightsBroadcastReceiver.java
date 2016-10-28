package niyo.nfc.com.nfcori;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static niyo.nfc.com.nfcori.Main2Activity.LOG_TAG;
import static niyo.nfc.com.nfcori.Main2Activity.s_url;

/**
 * Created by oriharel on 24/10/2016.
 */

public class LightsBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        final ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                Log.d(LOG_TAG, "all sockets success");
            }

            @Override
            public void failure(Object data, String description) {

            }
        };
        GenericHttpRequestTask task = new GenericHttpRequestTask(caller);
        String state = "off";
        String url = s_url+"/all/sockets/"+state;
        task.execute(url, "true");

    }
}

package niyo.nfc.com.nfcori;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import static niyo.nfc.com.nfcori.Main2Activity.s_url;

/**
 * Created by oriharel on 24/10/2016.
 */

public class LightsBroadcastReceiver extends BroadcastReceiver {

    public static final String LOG_TAG = LightsBroadcastReceiver.class.getSimpleName();
    public static final String BULB_NAME_EXTRA = "bulb_name_extra";
    public static final String TALL_LAMP = "tallLamp";
    public static final String SOFA_LAMP = "sofaLamp";
    public static final String WINDOW_LAMP = "windowLamp";
    public static final String ALL_STATE_EXTRA = "allExtra";

    @Override
    public void onReceive(final Context context, Intent intent) {

        final ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                Log.d(LOG_TAG, "all sockets success");

                Intent serviceIntent = new Intent(context, HomeStateFetchService.class);
                serviceIntent.putExtra(HomeStateFetchService.EVENT_NAMT_EXTRA,
                        HomeStateFetchService.STATE_EVENT_NAME);
                Log.d(LOG_TAG, "starting sync service...");
                context.startService(serviceIntent);
            }

            @Override
            public void failure(Object data, String description) {

            }
        };

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(HarelHome.HOME_STATE_URI, HarelHome.HOME_STATE_PROJECTION, null, null, null);
        assert cursor != null;
        if (cursor.moveToFirst()) {
            int colTallStateIndex = cursor.getColumnIndex(HomeTableColumns.TALL_LAMP_STATE);
            String tallLampStateStr = cursor.getString(colTallStateIndex);
            Boolean tallLampState = Boolean.valueOf(tallLampStateStr);

            int colSofaStateIndex = cursor.getColumnIndex(HomeTableColumns.SOFA_LAMP_STATE);
            String sofaLampStateStr = cursor.getString(colSofaStateIndex);
            Boolean sofaLampState = Boolean.valueOf(sofaLampStateStr);

            int colWindowStateIndex = cursor.getColumnIndex(HomeTableColumns.WINDOW_LAMP_STATE);
            String windowLampStateStr = cursor.getString(colWindowStateIndex);
            Boolean windowLampState = Boolean.valueOf(windowLampStateStr);

            String bulbName = intent.getStringExtra(BULB_NAME_EXTRA);

            String url;

            if (!TextUtils.isEmpty(bulbName)) {
                String state;
                switch (bulbName) {
                    case (TALL_LAMP):
                        state = calcStateToBe(tallLampState);
                        break;
                    case (SOFA_LAMP):
                        state = calcStateToBe(sofaLampState);
                        break;
                    case (WINDOW_LAMP):
                        state = calcStateToBe(windowLampState);
                        break;
                    default:
                        state = "";
                }

                url = s_url+"/sockets/"+bulbName+"/"+state;
            }
            else {
                String allState = intent.getStringExtra(ALL_STATE_EXTRA);
                url = s_url+"/all/sockets/"+allState;
            }

            GenericHttpRequestTask task = new GenericHttpRequestTask(caller);
            task.execute(url, "true");
        }

        cursor.close();
    }

    private String calcStateToBe(Boolean currentState) {
        if (currentState) {
            return "off";
        }
        else {
            return "on";
        }
    }


}

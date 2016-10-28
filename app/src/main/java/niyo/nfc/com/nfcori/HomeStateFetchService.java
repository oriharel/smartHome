package niyo.nfc.com.nfcori;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class HomeStateFetchService extends Service {

    public static final String LOG_TAG = HomeStateFetchService.class.getSimpleName();
    public static final String HOME_URL = "https://oriharel.herokuapp.com";
    public static final String EVENT_NAMT_EXTRA = "event_name";
    public static final String STATE_EVENT_NAME = "state";
    public static final String LAST_STATE_EVENT_NAME = "lastStateForClients";
    Socket mSocket;
    Timer mTimer;
    private Emitter.Listener stateListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            processEvent(STATE_EVENT_NAME, this, args);
        }
    };

    private Emitter.Listener lastStateListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            processEvent(LAST_STATE_EVENT_NAME, this, args);
        }
    };

    private static HashMap<String, String> sLampNameToMac;
    static {
        sLampNameToMac = new HashMap<>();
        sLampNameToMac.put("tallLamp", "accf23934866");
        sLampNameToMac.put("sofaLamp", "accf23931da4");
        sLampNameToMac.put("windowLamp", "accf23934938");
    }

    public HomeStateFetchService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {

            mSocket = IO.socket(HOME_URL);
            final String eventName = intent.getStringExtra(EVENT_NAMT_EXTRA);

            Emitter.Listener listener;
            if (eventName.equals(STATE_EVENT_NAME)) {
                listener = stateListener;
            }
            else {
                listener = lastStateListener;
            }

            final Emitter.Listener finalListener = listener;

            mSocket.on(eventName, listener);
            Log.d(LOG_TAG, "connecting socket on "+HOME_URL+" and waiting for "+eventName);
            mSocket.connect();

            mTimer = new Timer();
            Log.d(LOG_TAG, "starting a timer for the background fetch");
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.e(LOG_TAG, "time out expired for "+eventName+"...");
                    mSocket.disconnect();
                    Log.e(LOG_TAG, "unregistering finalListener from "+eventName+" because of timeout");
                    mSocket.off(eventName, finalListener);
                    stopSelf();
                }
            }, 60*1000*3);

        } catch (URISyntaxException e) {
            Log.e(LOG_TAG, "error creating socket", e);
        }

        return START_STICKY;
    }

    private void processEvent(String eventName, Emitter.Listener listener, Object... args) {
        Log.d(LOG_TAG, "received "+eventName+" message from socket");
        mTimer.cancel();
        JSONObject state = (JSONObject)args[0];

        try {
            JSONArray sockets = state.getJSONArray("sockets");
            Log.d(LOG_TAG, "sockets: "+sockets);
            JSONObject persons = state.getJSONObject("persons");
            Log.d(LOG_TAG, "persons: "+persons);
            JSONObject tempData = state.getJSONObject("tempData");
            Log.d(LOG_TAG, "tempData: "+tempData);
            String image = state.getString("image");

            insertStateToDb(sockets, persons, tempData, image);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "error processing state: "+state, e);
            return;
        }

        Log.d(LOG_TAG, "disconnecting socket now...");
        mSocket.disconnect();
        Log.d(LOG_TAG, "unregistering listener from "+eventName);
        mSocket.off(eventName, listener);

        Log.d(LOG_TAG, "stopping service now...");
        stopSelf();
    }

    private void insertStateToDb(JSONArray sockets, JSONObject persons, JSONObject tempData, String image) throws JSONException {

        Log.d(LOG_TAG, "insertStateToDb started");
        ContentValues values = new ContentValues();

        JSONObject tallLamp = getLampObject(sockets, sLampNameToMac.get("tallLamp"));
        JSONObject sofaLamp = getLampObject(sockets, sLampNameToMac.get("sofaLamp"));
        JSONObject windowLamp = getLampObject(sockets, sLampNameToMac.get("windowLamp"));

        Date current = new Date();
        values.put(HomeTableColumns.LAST_UPDATE_TIME, current.getTime());
        values.put(HomeTableColumns.TALL_LAMP_STATE, tallLamp.getString("state"));
        values.put(HomeTableColumns.SOFA_LAMP_STATE, sofaLamp.getString("state"));
        values.put(HomeTableColumns.WINDOW_LAMP_STATE, windowLamp.getString("state"));

        String[] oriInfo = persons.getString("Ori").split(": ");
        String[] yifatInfo = persons.getString("Yifat").split(": ");
        values.put(HomeTableColumns.ORI_PRESENCE, oriInfo[0]);
        values.put(HomeTableColumns.YIFAT_PRESENCE, yifatInfo[0]);
        values.put(HomeTableColumns.ORI_LAST_PRESENCE, oriInfo[1]);
        values.put(HomeTableColumns.YIFAT_LAST_PRESENCE, yifatInfo[1]);

        String temperature = tempData.getString("temp");
        values.put(HomeTableColumns.HOME_TEMP, temperature);

        byte[] imageBytes = image.getBytes();
        values.put(HomeTableColumns.HOME_PIC, imageBytes);

        //first delete all rows
        getContentResolver().delete(HarelHome.HOME_STATE_URI, null, null);

        Uri result = getContentResolver().insert(HarelHome.HOME_STATE_URI, values);

        Log.d(LOG_TAG, "added new state result was "+result);
    }

    private JSONObject getLampObject(JSONArray sockets, String macAddess) throws JSONException {
        JSONObject result = new JSONObject();

        for (int i = 0; i < sockets.length(); i++) {
            JSONObject lampInfo = sockets.getJSONObject(i);
            if (lampInfo.getString("macAddress").equals(macAddess))
                result = lampInfo;
        }

        return result;

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

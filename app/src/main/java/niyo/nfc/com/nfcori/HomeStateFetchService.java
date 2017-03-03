package niyo.nfc.com.nfcori;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Date;
import java.util.HashMap;

import niyo.nfc.com.nfcori.db.HomeTableColumns;

public class HomeStateFetchService extends Service {

    public static final String LOG_TAG = HomeStateFetchService.class.getSimpleName();
    public static final String EVENT_NAMT_EXTRA = "event_name";
    public static final String STATE_EVENT_NAME = "state";
    public static final String LAST_STATE_EVENT_NAME = "lastStateForClients";

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

        final Context context = this;
        final ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                Log.d(LOG_TAG, "state fetched successfully");
                String dataStr = (String)data;

                try {
                    processState(context, new JSONObject(dataStr));
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "error parsing state: "+data);
                }
                finally {
                    stopSelf();
                }
            }

            @Override
            public void failure(Object data, String description) {
                Log.e(LOG_TAG, "Error receiving state "+description);
                stopSelf();
            }
        };

        GenericHttpRequestTask task = new GenericHttpRequestTask(caller);

        String url = Utils.getHomeURL() + "/state";
        task.execute(url);
        return START_STICKY;
    }

    public static void processState(Context context, JSONObject state) {
        JSONArray sockets = null;
        JSONObject persons = null;
        JSONObject tempData = null;
        String image = null;
        String camImage = null;

        try {
            if (state.has("sockets")) {
                sockets = state.getJSONArray("sockets");
                Log.d(LOG_TAG, "sockets: "+sockets);
            }

            if (state.has("persons")) {
                persons = state.getJSONObject("persons");
                Log.d(LOG_TAG, "persons: "+persons);
            }

            if (state.has("tempData")) {
                tempData = state.getJSONObject("tempData");
                Log.d(LOG_TAG, "tempData: "+tempData);
            }

            if (state.has("image")) {
                image = state.getString("image");
            }

            if (state.has("camImage")) {
                camImage = state.getString("camImage");
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, "error processing state: "+state, e);
        }
        finally {
            insertStateToDb(context, sockets, persons, tempData, image, camImage);
        }

        Log.d(LOG_TAG, "stopping service now...");
        Intent notifIntent = new Intent("com.niyo.updateNotification");
        context.sendBroadcast(notifIntent);
    }

    public static void insertStateToDb(Context context, JSONArray sockets,
                                       JSONObject persons,
                                       JSONObject tempData,
                                       String image,
                                       String camImage) {

        Log.d(LOG_TAG, "insertStateToDb started");
        ContentValues values = new ContentValues();

        try {
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

            if (tempData != null) {
                String temperature = tempData.getString("temp");
                values.put(HomeTableColumns.HOME_TEMP, temperature);
            }


            if (image != null) {
                byte[] imageBytes = image.getBytes();
                values.put(HomeTableColumns.HOME_PIC, imageBytes);
            }

            if (camImage != null) {
                byte[] camImageBytes = camImage.getBytes();
                values.put(HomeTableColumns.HOME_CAM_PIC, camImageBytes);
            }


            //first delete all rows
            context.getContentResolver().delete(HarelHome.HOME_STATE_URI, null, null);

            Uri result = context.getContentResolver().insert(HarelHome.HOME_STATE_URI, values);


            Log.d(LOG_TAG, "added new state result was "+result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static JSONObject getLampObject(JSONArray sockets, String macAddess) throws JSONException {
        JSONObject result = new JSONObject();

        if (sockets != null) {
            for (int i = 0; i < sockets.length(); i++) {
                JSONObject lampInfo = sockets.getJSONObject(i);
                if (lampInfo.getString("macAddress").equals(macAddess))
                    result = lampInfo;
            }
        }


        return result;

    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

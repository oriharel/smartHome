package niyo.nfc.com.nfcori;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Base64;
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
    public static final String TRIGGER_EXTRA = "trigger_extra";
    public static final String PUSH = "push";
    public static final String PUSH_TITLE_EXTRA = "push_title";
    public static final String PUSH_BODY_EXTRA = "push_body";

    private static HashMap<String, String> sLampNameToMac;
    static {
        sLampNameToMac = new HashMap<>();
        sLampNameToMac.put("tallLamp", "accf23934866");
        sLampNameToMac.put("sofaLamp", "accf23931da4");
        sLampNameToMac.put("windowLamp", "accf23934938");
    }

    private static HashMap<String, String> sIdToSensor;
    static {
        sIdToSensor = new HashMap<>();
        sIdToSensor.put("gina", "158d000159c447");
        sIdToSensor.put("door", "158d00015a9488");
    }

    public HomeStateFetchService() {
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        final Context context = this;
        final ServiceCaller caller = new ServiceCaller() {
            @Override
            public void success(Object data) {

                Log.d(LOG_TAG, "state fetched successfully");
                String dataStr = (String)data;

                try {
                    processState(context, new JSONObject(dataStr), intent);
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

    public static void processState(Context context, JSONObject state, Intent intent) {
        JSONArray sockets = null;
        JSONObject persons = null;
        JSONObject tempData = null;
        JSONObject xiaomiData = null;
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

            if (state.has("xiaomiData")) {
                xiaomiData = state.getJSONObject("xiaomiData");
                Log.d(LOG_TAG, "xiaomiData: "+xiaomiData);
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
            insertStateToDb(context, sockets, persons, tempData, image, camImage, xiaomiData);
        }

        Log.d(LOG_TAG, "stopping service now...");
        Intent notifIntent = new Intent("com.niyo.updateNotification");
        context.sendBroadcast(notifIntent);

        if (intent != null && intent.getStringExtra(TRIGGER_EXTRA) != null && intent.getStringExtra(TRIGGER_EXTRA).equals(PUSH)) {
            Log.d(LOG_TAG, "home fetch service started from a push. showing notification");
            sendNotification(intent.getStringExtra(PUSH_TITLE_EXTRA),
                    intent.getStringExtra(PUSH_BODY_EXTRA),
                    context,
                    camImage);
        }
        else {
            Log.d(LOG_TAG, "service was not started from push");
        }
    }

    public static void insertStateToDb(Context context, JSONArray sockets,
                                       JSONObject persons,
                                       JSONObject tempData,
                                       String image,
                                       String camImage,
                                       JSONObject xiaomiData) {

        Log.d(LOG_TAG, "insertStateToDb started");
        ContentValues values = new ContentValues();

        try {
            JSONObject tallLamp = getLampObject(sockets, sLampNameToMac.get("tallLamp"));

            JSONObject sofaLamp = getLampObject(sockets, sLampNameToMac.get("sofaLamp"));
            JSONObject windowLamp = getLampObject(sockets, sLampNameToMac.get("windowLamp"));

            JSONObject doorObject = getXiaomiObject(xiaomiData, "door");
            JSONObject ginaObject = getXiaomiObject(xiaomiData, "gina");

            Log.d(LOG_TAG, "doorObject: "+doorObject);
            Log.d(LOG_TAG, "ginaObject: "+ginaObject);

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
            values.put(HomeTableColumns.DOOR_STATUS, doorObject.getString("value"));
            values.put(HomeTableColumns.DOOR_STATUS_TIME, doorObject.getLong("pubDate"));
            values.put(HomeTableColumns.GINA_STATUS, ginaObject.getString("value"));
            values.put(HomeTableColumns.GINA_STATUS_TIME, ginaObject.getLong("pubDate"));

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

    private static JSONObject getXiaomiObject(JSONObject xiaomiData, String deviceName) throws JSONException {
        String id = sIdToSensor.get(deviceName);
        return xiaomiData.getJSONObject(id);
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

    private static void sendNotification(String title, String body, Context context, String camImage) {
        Intent intent = new Intent(context, Main2Activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        byte[] camHomeImage64 = camImage.getBytes();
        byte[] decodedString = Base64.decode(camHomeImage64, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String userRingtoneStr = prefs.getString(context.getResources().
                getString(R.string.notifRingtone), null);
        if (userRingtoneStr != null) {
            defaultSoundUri = Uri.parse(userRingtoneStr);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setLargeIcon(decodedByte)/*Notification icon image*/
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigPictureStyle()
                        .bigPicture(decodedByte))/*Notification with Image*/
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(76 /* ID of notification */, notificationBuilder.build());
    }
}

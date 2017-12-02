package niyo.nfc.com.nfcori;

import android.net.Uri;

import niyo.nfc.com.nfcori.db.HomeTableColumns;

/**
 * Created by oriharel on 24/10/2016.
 */

public class HarelHome {

    public static String AUTHORITY = "com.niyo.home";
    public static String SCHEME = "content://";
    public static final String HOME_STATE = "/home_state";
    public static final Uri HOME_STATE_URI =  Uri.parse(SCHEME + AUTHORITY + HOME_STATE);
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.niyo.home";
    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.niyo.home";

    public static final String[] HOME_STATE_PROJECTION = new String[] {
            HomeTableColumns._ID,
            HomeTableColumns.LAST_UPDATE_TIME,
            HomeTableColumns.TALL_LAMP_STATE,
            HomeTableColumns.SOFA_LAMP_STATE,
            HomeTableColumns.WINDOW_LAMP_STATE,
            HomeTableColumns.ORI_PRESENCE,
            HomeTableColumns.YIFAT_PRESENCE,
            HomeTableColumns.ITCHUK_PRESENCE,
            HomeTableColumns.ORI_LAST_PRESENCE,
            HomeTableColumns.YIFAT_LAST_PRESENCE,
            HomeTableColumns.ITCHUK_LAST_PRESENCE,
            HomeTableColumns.HOME_TEMP,
            HomeTableColumns.HOME_PIC,
            HomeTableColumns.HOME_CAM_PIC,
            HomeTableColumns.DOOR_STATUS,
            HomeTableColumns.DOOR_STATUS_TIME,
            HomeTableColumns.GINA_STATUS,
            HomeTableColumns.GINA_STATUS_TIME
    };
}

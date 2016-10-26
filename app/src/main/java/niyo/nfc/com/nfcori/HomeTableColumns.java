package niyo.nfc.com.nfcori;

import android.provider.BaseColumns;

/**
 * Created by oriharel on 24/10/2016.
 */

class HomeTableColumns implements BaseColumns {

    static final String LAST_UPDATE_TIME = "last_update_time";
    static final String TALL_LAMP_STATE = "tall_state";
    static final String SOFA_LAMP_STATE = "sofa_state";
    static final String WINDOW_LAMP_STATE = "window_state";
    static final String ORI_PRESENCE = "ori_presence";
    static final String YIFAT_PRESENCE = "yifat_presence";
    static final String ORI_LAST_PRESENCE = "ori_last_presence";
    static final String YIFAT_LAST_PRESENCE = "yifat_last_presence";
    static final String HOME_TEMP = "home_temp";
    static final String HOME_PIC = "home_pic";
    static final String STATE_FETCH_IN_PROGRESS = "state_fetching";

    public static final int COLUMN_ID_PATH_INDEX = 1;
    public static final int COLUMN_LAST_UPDATE_TIME_INDEX = 2;
    public static final int COLUMN_TALL_LAMP_STATE_INDEX = 3;
    public static final int COLUMN_SOFA_LAMP_STATE_INDEX = 4;
    public static final int COLUMN_WINDOW_LAMP_STATE_INDEX = 5;
    public static final int COLUMN_ORI_PRESENCE_INDEX = 6;
    public static final int COLUMN_YIFAT_PRESENCE_INDEX = 7;
    public static final int COLUMN_ORI_LAST_PRESENCE_INDEX = 8;
    public static final int COLUMN_YIFAT_LAST_PRESENCE_INDEX = 9;
    public static final int COLUMN_HOME_TEMP_INDEX = 10;
    public static final int COLUMN_HOME_PIC_INDEX = 11;
}

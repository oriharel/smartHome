package niyo.nfc.com.nfcori.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by oriharel on 24/10/2016.
 */

public class HomeDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = HomeDbHelper.class.getSimpleName();
    public static final String HOME_TABLE = "home";

    private static final String TABLE_HOME_CREATE =
            "create table " + HOME_TABLE + " ("
                    + HomeTableColumns._ID + " integer primary key autoincrement, "
                    + HomeTableColumns.LAST_UPDATE_TIME + " DATE, "
                    + HomeTableColumns.TALL_LAMP_STATE + " TEXT, "
                    + HomeTableColumns.SOFA_LAMP_STATE + " TEXT, "
                    + HomeTableColumns.WINDOW_LAMP_STATE + " TEXT, "
                    + HomeTableColumns.ORI_PRESENCE + " TEXT, "
                    + HomeTableColumns.YIFAT_PRESENCE + " TEXT, "
                    + HomeTableColumns.ORI_LAST_PRESENCE + " DATE, "
                    + HomeTableColumns.YIFAT_LAST_PRESENCE + " DATE, "
                    + HomeTableColumns.HOME_TEMP + " FLOAT, "
                    + HomeTableColumns.STATE_FETCH_IN_PROGRESS + " BOOLEAN, "
                    + HomeTableColumns.HOME_PIC + " BLOB,"
                    + HomeTableColumns.DOOR_STATUS + " BOOLEAN,"
                    + HomeTableColumns.DOOR_STATUS_TIME + " BIGINT,"
                    + HomeTableColumns.GINA_STATUS + " BOOLEAN,"
                    + HomeTableColumns.GINA_STATUS_TIME + " BIGINT,"
                    + HomeTableColumns.ITCHUK_PRESENCE + " TEXT,"
                    + HomeTableColumns.ITCHUK_LAST_PRESENCE + " DATE,"
                    + HomeTableColumns.HOME_CAM_PIC + " BLOB);";

    public HomeDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                        int version) {
        super(context, name, factory, version);
        Log.d(LOG_TAG, "constructor started");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(LOG_TAG, "onCreate started "+TABLE_HOME_CREATE);
        sqLiteDatabase.execSQL(TABLE_HOME_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.d(LOG_TAG, "onUpdate...");
        sqLiteDatabase.execSQL("DROP TABLE "+HOME_TABLE);
        sqLiteDatabase.execSQL(TABLE_HOME_CREATE);
    }
}

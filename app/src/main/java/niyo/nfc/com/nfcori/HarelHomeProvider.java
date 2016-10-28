package niyo.nfc.com.nfcori;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;

public class HarelHomeProvider extends ContentProvider {
    public static final String LOG_TAG = HarelHomeProvider.class.getSimpleName();
    private HomeDbHelper _dbHelper;
    private static final String DATABASE_NAME = "harelHome.db";

    private static final int DATABASE_VERSION = 1;
    private static final int HOME_STATE = 1;

    private static final UriMatcher sUriMatcher;

    private static HashMap<String, String> sHomeStateProjectionMap;

    static {

        /*
         * Creates and initializes the URI matcher
         */
        // Create a new instance
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(HarelHome.AUTHORITY, HarelHome.HOME_STATE, HOME_STATE);

        sHomeStateProjectionMap = new HashMap<>();

        // Maps the string "_ID" to the column name "_ID"
        sHomeStateProjectionMap.put(HomeTableColumns._ID, HomeTableColumns._ID);

        // Maps "title" to "title"
        sHomeStateProjectionMap.put(HomeTableColumns.TALL_LAMP_STATE, HomeTableColumns.TALL_LAMP_STATE);

        // Maps "note" to "note"
        sHomeStateProjectionMap.put(HomeTableColumns.SOFA_LAMP_STATE, HomeTableColumns.SOFA_LAMP_STATE);
        sHomeStateProjectionMap.put(HomeTableColumns.WINDOW_LAMP_STATE, HomeTableColumns.WINDOW_LAMP_STATE);
        sHomeStateProjectionMap.put(HomeTableColumns.ORI_PRESENCE, HomeTableColumns.ORI_PRESENCE);
        sHomeStateProjectionMap.put(HomeTableColumns.YIFAT_PRESENCE, HomeTableColumns.YIFAT_PRESENCE);
        sHomeStateProjectionMap.put(HomeTableColumns.ORI_LAST_PRESENCE, HomeTableColumns.ORI_LAST_PRESENCE);
        sHomeStateProjectionMap.put(HomeTableColumns.YIFAT_LAST_PRESENCE, HomeTableColumns.YIFAT_LAST_PRESENCE);
        sHomeStateProjectionMap.put(HomeTableColumns.HOME_TEMP, HomeTableColumns.HOME_TEMP);
        sHomeStateProjectionMap.put(HomeTableColumns.HOME_PIC, HomeTableColumns.HOME_PIC);
        sHomeStateProjectionMap.put(HomeTableColumns.STATE_FETCH_IN_PROGRESS, HomeTableColumns.STATE_FETCH_IN_PROGRESS);

    }

    public HarelHomeProvider() {
    }

    private HomeDbHelper getDbHelper() {
        return _dbHelper;
    }

    private void setDbHelper(HomeDbHelper dbHelper) {
        _dbHelper = dbHelper;
    }

    private SQLiteDatabase getWritableDb()
    {
        return getDbHelper().getWritableDatabase();
    }

    private SQLiteDatabase getReadableDb() {
        return getDbHelper().getReadableDatabase();
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.d(LOG_TAG, "delete is called");
        // Opens the database object in "write" mode.
        SQLiteDatabase db = getWritableDb();
        String finalWhere;

        int count;

        // Does the delete based on the incoming URI pattern.
        switch (sUriMatcher.match(uri)) {

            // If the incoming pattern matches the general pattern for friends, does a delete
            // based on the incoming "where" columns and arguments.
            case HOME_STATE:
                count = db.delete(
                        HomeDbHelper.HOME_TABLE,  // The database table name
                        selection,                     // The incoming where clause column names
                        selectionArgs                  // The incoming where clause values
                );
                break;

            // If the incoming pattern is invalid, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */

        // Returns the number of rows deleted.
        return count;
    }

    @Override
    public String getType(Uri uri) {
        /**
         * Chooses the MIME type based on the incoming URI pattern
         */
        switch (sUriMatcher.match(uri)) {

            // If the pattern is for home state, returns the general content type.
            case HOME_STATE:
                return HarelHome.CONTENT_TYPE;

            // If the URI pattern doesn't match any permitted patterns, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {

        Log.d(LOG_TAG, "insert started "+uri);
        String table = HomeDbHelper.HOME_TABLE;
        getWritableDb().insert(table, null, values);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate started");
        Context context = getContext();

        setDbHelper(new HomeDbHelper(context, DATABASE_NAME, null, DATABASE_VERSION));
        return getWritableDb() == null ? false : true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query started with "+uri);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(HomeDbHelper.HOME_TABLE);

        switch (sUriMatcher.match(uri)) {

            case HOME_STATE:
                qb.setProjectionMap(sHomeStateProjectionMap);
                break;

            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Log.d(LOG_TAG, "going to query with selection "+selection);
        Log.d(LOG_TAG, "projection is "+AndroidUtil.getArrayAsString(projection));
        Log.d(LOG_TAG, "selectionArgs is "+AndroidUtil.getArrayAsString(selectionArgs));
        Log.d(LOG_TAG, "sort order is "+sortOrder);
        String orderBy = HomeTableColumns.LAST_UPDATE_TIME;

        Cursor cursor = qb.query(getReadableDb(), projection, selection, selectionArgs, null, null, orderBy);

        Log.d(LOG_TAG, "got " + cursor.getCount()
                + " results from uri " + uri);

        return cursor;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // Opens the database object in "write" mode.
        SQLiteDatabase db = getWritableDb();
        int count;

        // Does the update based on the incoming URI pattern
        switch (sUriMatcher.match(uri)) {

            // If the incoming URI matches the general notes pattern, does the update based on
            // the incoming data.
            case HOME_STATE:

                // Does the update and returns the number of rows updated.
                count = db.update(
                        HomeDbHelper.HOME_TABLE, // The database table name.
                        values,                   // A map of column names and new values to use.
                        selection,                    // The where clause column names.
                        selectionArgs                 // The where clause column values to select on.
                );
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        /*Gets a handle to the content resolver object for the current context, and notifies it
         * that the incoming URI changed. The object passes this along to the resolver framework,
         * and observers that have registered themselves for the provider are notified.
         */
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows updated.
        return count;
    }
}

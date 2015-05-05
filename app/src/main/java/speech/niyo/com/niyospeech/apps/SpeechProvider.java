package speech.niyo.com.niyospeech.apps;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;

import speech.niyo.com.niyospeech.AndroidUtil;
import speech.niyo.com.niyospeech.NiyoDbHelper;
import speech.niyo.com.niyospeech.NiyoSpeech;

public class SpeechProvider extends ContentProvider {
    public static final String LOG_TAG = SpeechProvider.class.getSimpleName();
    private NiyoDbHelper _dbHelper;
    private static final String DATABASE_NAME = "niyospeech.db";
    private static final int DATABASE_VERSION = 1;

    public static final int APPS = 1;
    public static final int APP_PKG = 2;

    private static final UriMatcher sUriMatcher;
    private static HashMap<String, String> sAppsProjectionMap;

    static {

        /*
         * Creates and initializes the URI matcher
         */
        // Create a new instance
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        sUriMatcher.addURI(NiyoSpeech.AUTHORITY, "apps", APPS);

        // Add a pattern that routes URIs terminated with "feeds" plus an integer
        // to a feed ID operation
        sUriMatcher.addURI(NiyoSpeech.AUTHORITY, "apps/#", APP_PKG);

        sAppsProjectionMap = new HashMap<String, String>();

        // Maps the string "_ID" to the column name "_ID"
        sAppsProjectionMap.put(AppsColumns._ID, AppsColumns._ID);

        // Maps "pkg" to "pkg"
        sAppsProjectionMap.put(AppsColumns.APP_PKG, AppsColumns.APP_PKG);

    }

    public SpeechProvider() {
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        // Opens the database object in "write" mode.
        SQLiteDatabase db = getWritableDb();
        String finalWhere;

        int count;

        // Does the delete based on the incoming URI pattern.
        switch (sUriMatcher.match(uri)) {

            // If the incoming pattern matches the general pattern for friends, does a delete
            // based on the incoming "where" columns and arguments.
            case APPS:
                count = db.delete(
                        NiyoDbHelper.APPS_TABLE,  // The database table name
                        where,                     // The incoming where clause column names
                        whereArgs                  // The incoming where clause values
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
        getContext().getContentResolver().notifyChange(uri, null);

        // Returns the number of rows deleted.
        return count;
    }

    @Override
    public String getType(Uri uri) {
        /**
         * Chooses the MIME type based on the incoming URI pattern
         */
        switch (sUriMatcher.match(uri)) {

            // If the pattern is for feeds, returns the general content type.
            case APPS:
                return NiyoSpeech.CONTENT_TYPE;

            // If the URI pattern doesn't match any permitted patterns, throws an exception.
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Log.d(LOG_TAG, "insert started "+uri);
        Log.d(LOG_TAG, "the app pkg is "+contentValues.getAsString(AppsColumns.APP_PKG));

        switch (sUriMatcher.match(uri)) {
            case APPS:
                getWritableDb().insert(NiyoDbHelper.APPS_TABLE, AppsColumns.APP_PKG, contentValues);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    @Override
    public boolean onCreate() {
        Log.d(LOG_TAG, "onCreate started");
        Context context = getContext();

        setDbHelper(new NiyoDbHelper(context, DATABASE_NAME, null, DATABASE_VERSION));
        return getWritableDb() == null ? false : true;
    }

    private SQLiteDatabase getWritableDb()
    {
        return getDbHelper().getWritableDatabase();
    }

    private NiyoDbHelper getDbHelper() {
        return _dbHelper;
    }

    private void setDbHelper(NiyoDbHelper dbHelper) {
        _dbHelper = dbHelper;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        Log.d(LOG_TAG, "query started with "+uri);
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(NiyoDbHelper.APPS_TABLE);

        switch (sUriMatcher.match(uri)) {

            case APPS:
                qb.setProjectionMap(sAppsProjectionMap);
                break;

            case APP_PKG:
                qb.setProjectionMap(sAppsProjectionMap);
                qb.appendWhere(
                        AppsColumns._ID +    // the name of the ID column
                                "=" +
                                // the position of the note ID itself in the incoming URI
                                uri.getPathSegments().get(AppsColumns.COLUMN_ID_INDEX));
                break;

            default:
                // If the URI doesn't match any of the known patterns, throw an exception.
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Log.d(LOG_TAG, "going to query with selection "+selection);
        Log.d(LOG_TAG, "projection is "+ AndroidUtil.getArrayAsString(projection));
        Log.d(LOG_TAG, "selectionArgs is "+AndroidUtil.getArrayAsString(selectionArgs));
        Log.d(LOG_TAG, "sort order is "+sortOrder);
        String orderBy = AppsColumns.APP_PKG;
        qb.setDistinct(true);
        Cursor cursor = qb.query(getReadableDb(), projection, selection, selectionArgs, null, null, orderBy);

        Log.d(LOG_TAG, "got " + cursor.getCount()
                + " results from uri " + uri);

        return cursor;
    }

    private SQLiteDatabase getReadableDb() {
        return getDbHelper().getReadableDatabase();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

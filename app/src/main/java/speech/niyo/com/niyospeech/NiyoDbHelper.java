package speech.niyo.com.niyospeech;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import speech.niyo.com.niyospeech.apps.AppsColumns;

/**
 * Created by oriharel on 7/15/14.
 */
public class NiyoDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = NiyoDbHelper.class.getSimpleName();
    public static final String APPS_TABLE = "apps";
    public static final String APPS_TABLE_CREATE = "create table " + APPS_TABLE + "("
            + AppsColumns._ID + "integer, "
            + AppsColumns.APP_PKG + " TEXT);";

    public NiyoDbHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.d(LOG_TAG, "onCreate started " + APPS_TABLE_CREATE);
        sqLiteDatabase.execSQL(APPS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        Log.d(LOG_TAG, "onUpgrade started");
    }
}

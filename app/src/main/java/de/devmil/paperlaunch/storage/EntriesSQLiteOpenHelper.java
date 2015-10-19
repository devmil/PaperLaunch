package de.devmil.paperlaunch.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EntriesSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = EntriesSQLiteOpenHelper.class.getName();

    public static final String TABLE_ENTRIES = "entries";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_ORDERINDEX = "orderindex";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LAUNCHINTENT = "launchintent";
    public static final String COLUMN_ICON = "icon";
    public static final String COLUMN_ISFOLDER = "isfolder";
    public static final String COLUMN_FOLDERID = "folderid";

    private static final String DATABASE_NAME = "entries.db";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE = "create table "
            + TABLE_ENTRIES
            + " ( "
            + COLUMN_ID             + " integer primary key autoincrement, "
            + COLUMN_ORDERINDEX     + " integer, "
            + COLUMN_LAUNCHINTENT   + " text, "
            + COLUMN_NAME           + " text, "
            + COLUMN_ICON           + " blob, "
            + COLUMN_ISFOLDER       + " integer, "
            + COLUMN_FOLDERID       + " integer "
            + " );";


    public EntriesSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database. Old version = " + oldVersion + " ==> new version = " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
        db.execSQL(DATABASE_CREATE);
    }
}

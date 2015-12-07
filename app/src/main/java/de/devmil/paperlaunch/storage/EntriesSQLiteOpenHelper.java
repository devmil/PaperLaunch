package de.devmil.paperlaunch.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class EntriesSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = EntriesSQLiteOpenHelper.class.getName();

    public static final String TABLE_ENTRIES = "entries";
    public static final String TABLE_LAUNCHES = "launches";
    public static final String TABLE_FOLDERS = "folders";

    public static final String COLUMN_ID = "_id";

    public static final String COLUMN_ENTRIES_LAUNCHID = "launchid";
    public static final String COLUMN_ENTRIES_FOLDERID = "folderid";
    public static final String COLUMN_ENTRIES_PARENTFOLDERID = "parentfolderid";
    public static final String COLUMN_ENTRIES_ORDERINDEX = "orderindex";

    public static final String COLUMN_LAUNCHES_NAME = "name";
    public static final String COLUMN_LAUNCHES_LAUNCHINTENT = "launchintent";
    public static final String COLUMN_LAUNCHES_ICON = "icon";

    public static final String COLUMN_FOLDERS_NAME = "name";
    public static final String COLUMN_FOLDERS_ICON = "icon";

    private static final String DATABASE_NAME = "entries.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_ENTRIES_CREATE = "create table "
            + TABLE_ENTRIES
            + " ( "
            + COLUMN_ID                     + " integer primary key autoincrement, "
            + COLUMN_ENTRIES_ORDERINDEX     + " integer, "
            + COLUMN_ENTRIES_LAUNCHID       + " integer, "
            + COLUMN_ENTRIES_FOLDERID       + " integer, "
            + COLUMN_ENTRIES_PARENTFOLDERID + " integer "
            + " ); ";
    private static final String TABLE_LAUNCHES_CREATE =  "create table "
            + TABLE_LAUNCHES
            + " ( "
            + COLUMN_ID                     + " integer primary key autoincrement, "
            + COLUMN_LAUNCHES_NAME          + " text, "
            + COLUMN_LAUNCHES_LAUNCHINTENT  + " text, "
            + COLUMN_LAUNCHES_ICON          + " blob "
            + " ); ";
    private static final String TABLE_FOLDERS_CREATE =  " create table "
            + TABLE_FOLDERS
            + " ( "
            + COLUMN_ID                     + " integer primary key autoincrement, "
            + COLUMN_FOLDERS_NAME           + " text, "
            + COLUMN_FOLDERS_ICON           + " blob "
            + " );";


    public EntriesSQLiteOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_ENTRIES_CREATE);
        db.execSQL(TABLE_LAUNCHES_CREATE);
        db.execSQL(TABLE_FOLDERS_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "Upgrading database. Old version = " + oldVersion + " ==> new version = " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAUNCHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDERS);
        db.execSQL(TABLE_ENTRIES_CREATE);
        db.execSQL(TABLE_LAUNCHES_CREATE);
        db.execSQL(TABLE_FOLDERS_CREATE);
    }

    public void clear(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAUNCHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDERS);
        db.execSQL(TABLE_ENTRIES_CREATE);
        db.execSQL(TABLE_LAUNCHES_CREATE);
        db.execSQL(TABLE_FOLDERS_CREATE);
    }
}

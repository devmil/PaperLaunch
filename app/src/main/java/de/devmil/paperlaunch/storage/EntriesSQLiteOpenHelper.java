/*
 * Copyright 2015 Devmil Solutions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.devmil.paperlaunch.storage;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

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
    public static final String COLUMN_FOLDERS_DEPTH = "depth";

    public static final String COLUMN_FOLDERS_NAME = "name";
    public static final String COLUMN_FOLDERS_ICON = "icon";

    private static final String DATABASE_NAME = "entries.db";
    private static final int DATABASE_VERSION = 2;

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
            + COLUMN_FOLDERS_ICON           + " blob, "
            + COLUMN_FOLDERS_DEPTH          + " integer "
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
        if(oldVersion < 1) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ENTRIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_LAUNCHES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FOLDERS);
            db.execSQL(TABLE_ENTRIES_CREATE);
            db.execSQL(TABLE_LAUNCHES_CREATE);
            db.execSQL(TABLE_FOLDERS_CREATE);
        } else if(oldVersion == 1) {
            db.execSQL("ALTER TABLE " + TABLE_FOLDERS + " ADD COLUMN " + COLUMN_FOLDERS_DEPTH + " integer");
            updateDepth(db);
        }
    }

    private void updateDepth(SQLiteDatabase db) {
        String sql = "SELECT f1._id f1ID, f2._id f2ID, f3._id f3ID, f4._id f4ID, f5._id f5ID, f6._id f6ID, f7._id f7ID, f8._id f8ID, f9._id f9ID, f10._id f10ID\n" +
                "FROM folders f1\n" +
                "INNER JOIN entries e1 ON (e1.folderid == f1._id)\n" +
                "LEFT OUTER JOIN folders f2 ON (f2._id == e1.parentfolderid)\n" +
                "LEFT OUTER JOIN entries e2 ON (e2.folderid == f2._id)\n" +
                "LEFT OUTER JOIN folders f3 ON (f3._id == e2.parentfolderid)\n" +
                "LEFT OUTER JOIN entries e3 ON (e3.folderid == f3._id)\n" +
                "LEFT OUTER JOIN folders f4 ON (f4._id == e3.parentfolderid)\n" +
                "LEFT OUTER JOIN entries e4 ON (e4.folderid == f4._id)\n" +
                "LEFT OUTER JOIN folders f5 ON (f5._id == e4.parentfolderid)\n" +
                "LEFT OUTER JOIN entries e5 ON (e5.folderid == f5._id)\n" +
                "LEFT OUTER JOIN folders f6 ON (f6._id == e5.parentfolderid)\n" +
                "LEFT OUTER JOIN entries e6 ON (e6.folderid == f6._id)\n" +
                "LEFT OUTER JOIN folders f7 ON (f7._id == e6.parentfolderid)\n" +
                "LEFT OUTER JOIN entries e7 ON (e7.folderid == f7._id)\n" +
                "LEFT OUTER JOIN folders f8 ON (f8._id == e7.parentfolderid)\n" +
                "LEFT OUTER JOIN entries e8 ON (e8.folderid == f8._id)\n" +
                "LEFT OUTER JOIN folders f9 ON (f9._id == e8.parentfolderid)\n" +
                "LEFT OUTER JOIN entries e9 ON (e9.folderid == f9._id)\n" +
                "LEFT OUTER JOIN folders f10 ON (f10._id == e9.parentfolderid)\n" +
                "LEFT OUTER JOIN entries e10 ON (e10.folderid == f10._id)";

        Cursor c = db.rawQuery(sql, null);

        HashMap<Integer, Integer> folderIdDepthMap = new HashMap<>();

        if(c.moveToFirst()) {
            do {
                int depth = getDepthFromCursor(c);
                folderIdDepthMap.put(c.getInt(0), depth);
            } while(c.moveToNext());
        }
        c.close();

        for(Integer id : folderIdDepthMap.keySet()) {
            String updateSql = "UPDATE " + TABLE_FOLDERS + " SET " + COLUMN_FOLDERS_DEPTH + " = " + Integer.toString(folderIdDepthMap.get(id)) + " WHERE " + COLUMN_ID + " = " + id;
            db.execSQL(updateSql);
        }
    }

    private int getDepthFromCursor(Cursor c) {
        for(int i=9; i>0; i--) {
            if(!c.isNull(i))
                return i;
        }
        return 0;
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

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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.devmil.paperlaunch.utils.BitmapUtils;
import de.devmil.paperlaunch.utils.IntentSerializer;

public class LaunchesAccess {
    private static final String[] launchesColumns = new String[]
            {
                    EntriesSQLiteOpenHelper.COLUMN_ID,
                    EntriesSQLiteOpenHelper.COLUMN_LAUNCHES_NAME,
                    EntriesSQLiteOpenHelper.COLUMN_LAUNCHES_LAUNCHINTENT,
                    EntriesSQLiteOpenHelper.COLUMN_LAUNCHES_ICON
            };
    private static final int INDEX_COLUMN_ID = 0;
    private static final int INDEX_COLUMN_NAME = 1;
    private static final int INDEX_COLUMN_LAUNCHINTENT = 2;
    private static final int INDEX_COLUMN_ICON = 3;

    private SQLiteDatabase mDatabase;
    private Context mContext;

    public LaunchesAccess(Context context, SQLiteDatabase database) {
        mContext = context;
        mDatabase = database;
    }

    public LaunchDTO queryLaunch(long launchId) {
        Cursor c = mDatabase.query(
                EntriesSQLiteOpenHelper.TABLE_LAUNCHES,
                launchesColumns,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + launchId,
                null,
                null,
                null,
                null
        );

        LaunchDTO result = null;
        if(c.moveToFirst()) {
            result = cursorToLaunch(c);
        }
        c.close();
        return result;
    }

    public LaunchDTO createNew() {
        ContentValues values = new ContentValues();
        values.put(EntriesSQLiteOpenHelper.COLUMN_LAUNCHES_NAME, (String)null);

        long id = mDatabase.insert(
                EntriesSQLiteOpenHelper.TABLE_LAUNCHES,
                null,
                values);

        return queryLaunch(id);
    }

    public void update(LaunchDTO launch) {
        ContentValues values = new ContentValues();

        launchToValues(launch, values);

        mDatabase.update(
                EntriesSQLiteOpenHelper.TABLE_LAUNCHES,
                values,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + launch.getId(),
                null
        );
    }

    public void delete(LaunchDTO launch) {
        delete(launch.getId());
    }

    public void delete(long launchId) {
        mDatabase.delete(
                EntriesSQLiteOpenHelper.TABLE_LAUNCHES,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + launchId,
                null
        );
    }

    private LaunchDTO cursorToLaunch(Cursor cursor) {
        return new LaunchDTO(
                cursor.getInt(INDEX_COLUMN_ID),
                cursor.getString(INDEX_COLUMN_NAME),
                IntentSerializer.deserialize(cursor.getString(INDEX_COLUMN_LAUNCHINTENT)),
                BitmapUtils.getIcon(mContext, cursor.getBlob(INDEX_COLUMN_ICON))
        );
    }

    private void launchToValues(LaunchDTO launch, ContentValues values) {
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_NAME, launch.getName());
        values.put(EntriesSQLiteOpenHelper.COLUMN_LAUNCHES_LAUNCHINTENT, IntentSerializer.serialize(launch.getLaunchIntent()));
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_ICON, BitmapUtils.getBytes(launch.getIcon()));
    }
}

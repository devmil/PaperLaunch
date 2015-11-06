package de.devmil.paperlaunch.storage;

import android.content.ContentValues;
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

    public LaunchesAccess(SQLiteDatabase database) {
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

        if(c.moveToFirst()) {
            return cursorToLaunch(c);
        }
        return null;
    }

    public LaunchDTO createNew() {
        long id = mDatabase.insert(
                EntriesSQLiteOpenHelper.TABLE_LAUNCHES,
                null,
                null);

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
        mDatabase.delete(
                EntriesSQLiteOpenHelper.TABLE_LAUNCHES,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + launch.getId(),
                null
        );
    }

    private LaunchDTO cursorToLaunch(Cursor cursor) {
        return new LaunchDTO(
                cursor.getInt(INDEX_COLUMN_ID),
                cursor.getString(INDEX_COLUMN_NAME),
                IntentSerializer.deserialize(cursor.getString(INDEX_COLUMN_LAUNCHINTENT)),
                BitmapUtils.getIcon(cursor.getBlob(INDEX_COLUMN_ICON))
        );
    }

    private void launchToValues(LaunchDTO launch, ContentValues values) {
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_NAME, launch.getName());
        values.put(EntriesSQLiteOpenHelper.COLUMN_LAUNCHES_LAUNCHINTENT, IntentSerializer.serialize(launch.getLaunchIntent()));
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_ICON, BitmapUtils.getBytes(launch.getIcon()));
    }
}

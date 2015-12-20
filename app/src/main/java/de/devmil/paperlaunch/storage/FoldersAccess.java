package de.devmil.paperlaunch.storage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import de.devmil.paperlaunch.utils.BitmapUtils;

public class FoldersAccess {
    private static final String[] foldersColumns = new String[]
            {
                    EntriesSQLiteOpenHelper.COLUMN_ID,
                    EntriesSQLiteOpenHelper.COLUMN_FOLDERS_NAME,
                    EntriesSQLiteOpenHelper.COLUMN_FOLDERS_ICON
            };
    private static final int INDEX_COLUMN_ID = 0;
    private static final int INDEX_COLUMN_NAME = 1;
    private static final int INDEX_COLUMN_ICON = 2;

    private SQLiteDatabase mDatabase;
    private Context mContext;

    public FoldersAccess(Context context, SQLiteDatabase database) {
        mContext = context;
        mDatabase = database;
    }

    public FolderDTO queryFolder(long folderId) {
        Cursor c = mDatabase.query(
                EntriesSQLiteOpenHelper.TABLE_FOLDERS,
                foldersColumns,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + folderId,
                null,
                null,
                null,
                null
        );

        FolderDTO result = null;
        if(c.moveToFirst()) {
            result = cursorToFolder(c);
        }
        c.close();
        return result;
    }

    public FolderDTO createNew() {
        ContentValues values = new ContentValues();
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_NAME, (String)null);

        long id = mDatabase.insert(
                EntriesSQLiteOpenHelper.TABLE_FOLDERS,
                null,
                values);

        return queryFolder(id);
    }

    public void update(FolderDTO folder) {
        ContentValues values = new ContentValues();

        folderToValues(folder, values);

        mDatabase.update(
                EntriesSQLiteOpenHelper.TABLE_FOLDERS,
                values,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + folder.getId(),
                null
        );
    }

    public void delete(FolderDTO folder) {
        delete(folder.getId());
    }

    public void delete(long folderId) {
        mDatabase.delete(
                EntriesSQLiteOpenHelper.TABLE_FOLDERS,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + folderId,
                null
        );
    }

    private FolderDTO cursorToFolder(Cursor cursor) {
        return new FolderDTO(
                cursor.getInt(INDEX_COLUMN_ID),
                cursor.getString(INDEX_COLUMN_NAME),
                BitmapUtils.getIcon(mContext, cursor.getBlob(INDEX_COLUMN_ICON))
        );
    }

    private void folderToValues(FolderDTO folder, ContentValues values) {
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_NAME, folder.getName());
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_ICON, BitmapUtils.getBytes(folder.getIcon()));
    }
}

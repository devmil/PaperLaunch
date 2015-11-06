package de.devmil.paperlaunch.storage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.model.LaunchEntry;

public class EntriesDataSource {

    private EntriesSQLiteOpenHelper mHelper;
    private SQLiteDatabase mDatabase;
    private EntriesAccess mEntriesAccess;

    public EntriesDataSource(Context context) {
        mHelper = new EntriesSQLiteOpenHelper(context);
    }

    public void open() throws SQLiteException {
        mDatabase = mHelper.getWritableDatabase();
        mEntriesAccess = new EntriesAccess(mDatabase);
    }

    public void close() {
        rollbackTransaction();
        mDatabase = null;
        mEntriesAccess = null;
        mHelper.close();
    }

    public void startTransaction() {
        mDatabase.beginTransaction();
    }

    public void commitTransaction() {
        if(mDatabase != null && mDatabase.inTransaction()) {
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        }
    }

    public void rollbackTransaction() {
        if(mDatabase != null && mDatabase.inTransaction()) {
            mDatabase.endTransaction();
        }
    }

    public LaunchEntry createLaunch(long parentFolderId) {
        //create Entry
        EntryDTO entry = mEntriesAccess.createNew();
        entry.setParentFolderId(parentFolderId);
        //create Launch
        LaunchDTO launch = mLaunchesAccess.createNew();
        //relate them
        entry.setLaunchId(launch.getId());

        mEntriesAccess.update(entry);

        return loadLaunch(launch.getId());
    }

    public FolderEntry createFolder(long parentFolderId) {
        //create Entry
        EntryDTO entry = mEntriesAccess.createNew();
        entry.setParentFolderId(parentFolderId);
        //create folder
        FolderDTO folder = mFoldersAccess.createNew();
        //relate them
        entry.setFolderId(folder.getId());

        mEntriesAccess.update(entry);

        return loadFolder(folder.getId());
    }

    public LaunchEntry loadLaunch(long launchId) {
        LaunchDTO launch = mLaunchesAccess.queryLaunch(launchId);

        return createLaunchFromDTO(launch);
    }

    public FolderEntry loadFolder(long folderId) {
        FolderDTO folder = mFoldersAccess.queryFolder(folderId);

        List<EntryDTO> subEntryDTOs = mEntriesAccess.queryAllEntries(folder.getId());

        return createFolderFromDTO(folder, subEntryDTOs);
    }

    public void updateLaunchData(LaunchEntry launchEntry) {
        LaunchDTO launch = getDTOFromLaunch(launchEntry);

        mLaunchesAccess.update(launch);
    }

    public void updateFolderData(FolderEntry folderEntry) {
        FolderDTO folder = getDTOFromFolder(folderEntry);

        mFoldersAccess.update(folder);
    }

    public void updateOrders(FolderEntry folder) {
        int orderIndex = 0;
        for(IEntry subEntry : folder.getEntries()) {
            if(subEntry.isFolder()) {
                updateOrderForFolder(subEntry.getId(), orderIndex);
            } else {
                updateOrderForLaunch(subEntry.getId(), orderIndex);
            }
            orderIndex++;
        }
    }

    public void updateOrderForFolder(long folderId, long orderIndex) {
        EntryDTO entry = mEntriesAccess.queryEntryForFolder(folderId);
        entry.setOrderIndex(orderIndex);

        mEntriesAccess.update(entry);
    }

    public void updateOrderForLaunch(long launchId, long orderIndex) {
        EntryDTO entry = mEntriesAccess.queryEntryForLaunch(launchId);
        entry.setOrderIndex(orderIndex);

        mEntriesAccess.update(entry);
    }

    public void deleteEntry(LaunchEntry entry) {
        long id = entry.getId();
        database.delete(
                EntriesSQLiteOpenHelper.TABLE_ENTRIES,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + id,
                null);
    }

    public void updateEntry(LaunchEntry entry) {

        ContentValues values = new ContentValues();
        entryToValues(values, entry);

        database.update(
                EntriesSQLiteOpenHelper.TABLE_ENTRIES,
                values,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + entry.getId(),
                null
        );
    }

    private LaunchEntry cursorToEntry(Cursor cursor) {
        Intent launchIntent = getIntentFromString(cursor.getString(COLUMN_LAUNCHINTENT_INDEX));
        LaunchEntry result =
                new LaunchEntry(
                        cursor.getLong(COLUMN_ID_INDEX),
                        cursor.getInt(COLUMN_ORDERINDEX_INDEX),
                        launchIntent,
                        cursor.getString(COLUMN_NAME_INDEX),
                        getIcon(cursor.getBlob(COLUMN_ICON_INDEX)),
                        cursor.getInt(COLUMN_ISFOLDER_INDEX) != 0,
                        cursor.getLong(COLUMN_FOLDERID_INDEX)
                        );
        return result;
    }

    private void entryToValues(ContentValues values, LaunchEntry entry) {
        values.put(EntriesSQLiteOpenHelper.COLUMN_ORDERINDEX, entry.getOrderIndex());
        values.put(EntriesSQLiteOpenHelper.COLUMN_LAUNCHINTENT, getStringFromIntent(entry.getLaunchIntent()));
        values.put(EntriesSQLiteOpenHelper.COLUMN_NAME, entry.getAppName());
        values.put(EntriesSQLiteOpenHelper.COLUMN_ICON, getBytes(entry.getAppIcon()));
        values.put(EntriesSQLiteOpenHelper.COLUMN_ISFOLDER, entry.isFolder() ? 1 : 0);
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERID, entry.getFolderId());
    }

    private Intent getIntentFromString(String string) {
        //TODO: serializableIntent
        return null;
    }

    private String getStringFromIntent(Intent intent) {
        //TODO: serializableIntent
        return null;
    }

    private Drawable getIcon(byte[] rawData) {
        return Drawable.createFromStream(new ByteArrayInputStream(rawData), null);
    }

    private byte[] getBytes(Drawable drawable) {
        Bitmap bmp = drawableToBitmap(drawable);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] byteArray = stream.toByteArray();

        bmp.recycle();
        return byteArray;
    }

    private static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        final int width = !drawable.getBounds().isEmpty() ? drawable
                .getBounds().width() : drawable.getIntrinsicWidth();

        final int height = !drawable.getBounds().isEmpty() ? drawable
                .getBounds().height() : drawable.getIntrinsicHeight();

        final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width,
                height <= 0 ? 1 : height, Bitmap.Config.ARGB_8888);

        Log.v("Bitmap width - Height :", width + " : " + height);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}

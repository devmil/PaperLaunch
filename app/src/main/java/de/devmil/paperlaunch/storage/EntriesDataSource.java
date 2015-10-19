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

import de.devmil.paperlaunch.model.LaunchEntry;

public class EntriesDataSource {

    private SQLiteDatabase database;
    private EntriesSQLiteOpenHelper dbHelper;

    private String[] allColumns = {
            EntriesSQLiteOpenHelper.COLUMN_ID,
            EntriesSQLiteOpenHelper.COLUMN_ORDERINDEX,
            EntriesSQLiteOpenHelper.COLUMN_NAME,
            EntriesSQLiteOpenHelper.COLUMN_LAUNCHINTENT,
            EntriesSQLiteOpenHelper.COLUMN_ICON,
            EntriesSQLiteOpenHelper.COLUMN_ISFOLDER,
            EntriesSQLiteOpenHelper.COLUMN_FOLDERID
    };

    private static final int COLUMN_ID_INDEX = 0;
    private static final int COLUMN_ORDERINDEX_INDEX = 1;
    private static final int COLUMN_LAUNCHINTENT_INDEX = 2;
    private static final int COLUMN_NAME_INDEX = 3;
    private static final int COLUMN_ICON_INDEX = 4;
    private static final int COLUMN_ISFOLDER_INDEX = 5;
    private static final int COLUMN_FOLDERID_INDEX = 6;

    public EntriesDataSource(Context context) {
        dbHelper = new EntriesSQLiteOpenHelper(context);
    }

    public void open() throws SQLiteException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public LaunchEntry createEntry(boolean isFolder) {
        ContentValues values = new ContentValues();

        values.put(EntriesSQLiteOpenHelper.COLUMN_ISFOLDER, isFolder ? 1 : 0);

        long id = database.insert(EntriesSQLiteOpenHelper.TABLE_ENTRIES, null, values);

        Cursor cursor = database.query(
                EntriesSQLiteOpenHelper.TABLE_ENTRIES,
                allColumns,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + id,
                null,
                null,
                null,
                null);
        cursor.moveToFirst();
        LaunchEntry result = cursorToEntry(cursor);
        cursor.close();
        return result;
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

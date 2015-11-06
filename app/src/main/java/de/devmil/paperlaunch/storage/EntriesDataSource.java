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

import de.devmil.paperlaunch.model.Folder;
import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.model.Launch;

public class EntriesDataSource {

    private EntriesSQLiteOpenHelper mHelper;
    private SQLiteDatabase mDatabase;
    private EntriesAccess mEntriesAccess;
    private FoldersAccess mFoldersAccess;
    private LaunchesAccess mLaunchesAccess;

    public EntriesDataSource(Context context) {
        mHelper = new EntriesSQLiteOpenHelper(context);
    }

    public void open() throws SQLiteException {
        mDatabase = mHelper.getWritableDatabase();
        mEntriesAccess = new EntriesAccess(mDatabase);
        mFoldersAccess = new FoldersAccess(mDatabase);
        mLaunchesAccess = new LaunchesAccess(mDatabase);
    }

    public void close() {
        rollbackTransaction();
        mDatabase = null;
        mEntriesAccess = null;
        mFoldersAccess = null;
        mLaunchesAccess = null;
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

    public Launch createLaunch(long parentFolderId) {
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

    public Folder createFolder(long parentFolderId) {
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

    public Launch loadLaunch(long launchId) {
        LaunchDTO launch = mLaunchesAccess.queryLaunch(launchId);

        return createLaunchFromDTO(launch);
    }

    private Launch createLaunchFromDTO(LaunchDTO dto) {
        return new Launch(dto);
    }

    public Folder loadFolder(long folderId) {
        FolderDTO folder = mFoldersAccess.queryFolder(folderId);

        List<EntryDTO> subEntryDTOs = mEntriesAccess.queryAllEntries(folder.getId());

        return createFolderFromDTO(folder, subEntryDTOs);
    }

    private Folder createFolderFromDTO(FolderDTO dto, List<EntryDTO> subEntryDTOs) {
        List<IEntry> subEntries = new ArrayList<>();
        for(EntryDTO entryDto : subEntryDTOs) {
            if(entryDto.getFolderId() > 0) {
                subEntries.add(loadFolder(entryDto.getFolderId()));
            } else if(entryDto.getLaunchId() > 0) {
                subEntries.add(loadLaunch(entryDto.getLaunchId()));
            }
        }

        return new Folder(dto, subEntries);
    }

    public void updateLaunchData(Launch launch) {
        LaunchDTO launchDto = launch.getDto();

        mLaunchesAccess.update(launchDto);
    }

    public void updateFolderData(Folder folder) {
        FolderDTO folderDto = folder.getDto();

        mFoldersAccess.update(folderDto);
    }

    public void updateOrders(Folder folder) {
        int orderIndex = 0;
        for(IEntry subEntry : folder.getSubEntries()) {
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
}

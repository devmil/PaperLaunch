package de.devmil.paperlaunch.storage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

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
        if (mDatabase != null && mDatabase.inTransaction()) {
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
        }
    }

    public void rollbackTransaction() {
        if (mDatabase != null && mDatabase.inTransaction()) {
            mDatabase.endTransaction();
        }
    }

    public void clear() {
        startTransaction();
        mHelper.clear(mDatabase);
        commitTransaction();
    }

    public Launch createLaunch(long parentFolderId) {
        return createLaunch(parentFolderId, -1);
    }

    public Launch createLaunch(long parentFolderId, int orderIndex) {
        //create Entry
        EntryDTO entry = mEntriesAccess.createNew(orderIndex);
        entry.setParentFolderId(parentFolderId);
        //create Launch
        LaunchDTO launch = mLaunchesAccess.createNew();
        //relate them
        entry.setLaunchId(launch.getId());

        mEntriesAccess.update(entry);

        return loadLaunch(launch.getId());
    }

    public Folder createFolder(long parentFolderId) {
        return createFolder(parentFolderId, -1);
    }

    public Folder createFolder(long parentFolderId, int orderIndex) {
        //create Entry
        EntryDTO entry = mEntriesAccess.createNew(orderIndex);
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
        EntryDTO entry = mEntriesAccess.queryEntryForLaunch(launchId);

        return createLaunchFromDTO(launch, entry);
    }

    public List<IEntry> loadRootContent() {
        List<EntryDTO> entryDTOs =  mEntriesAccess.queryAllEntries(-1);

        List<IEntry> entries = new ArrayList<>();
        for(EntryDTO entryDto : entryDTOs) {
            entries.add(loadEntry(entryDto));
        }

        return entries;
    }

    private Launch createLaunchFromDTO(LaunchDTO dto, EntryDTO entryDto) {
        return new Launch(dto, entryDto);
    }

    public Folder loadFolder(long folderId) {
        FolderDTO folder = mFoldersAccess.queryFolder(folderId);
        EntryDTO entry = mEntriesAccess.queryEntryForFolder(folderId);

        List<EntryDTO> subEntryDTOs = mEntriesAccess.queryAllEntries(folder.getId());

        return createFolderFromDTO(folder, entry, subEntryDTOs);
    }

    public void deleteEntry(long entryId) {
        EntryDTO entryDto = mEntriesAccess.queryEntry(entryId);
        if(entryDto != null) {
            if(entryDto.getFolderId() > 0) {
                mFoldersAccess.delete(entryDto.getFolderId());
            }
            else if(entryDto.getLaunchId() > 0) {
                mLaunchesAccess.delete(entryDto.getLaunchId());
            }
            mEntriesAccess.delete(entryDto);
        }
    }

    private Folder createFolderFromDTO(FolderDTO dto, EntryDTO entryDto, List<EntryDTO> subEntryDTOs) {
        List<IEntry> subEntries = new ArrayList<>();
        for(EntryDTO subEntryDto : subEntryDTOs) {
            subEntries.add(loadEntry(subEntryDto));
        }

        return new Folder(dto, entryDto, subEntries);
    }

    private IEntry loadEntry(EntryDTO entryDto) {
        if(entryDto.getFolderId() > 0) {
            return loadFolder(entryDto.getFolderId());
        } else if(entryDto.getLaunchId() > 0) {
            return loadLaunch(entryDto.getLaunchId());
        }
        return null;
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
        updateOrders(folder.getSubEntries());
    }

    public void updateOrders(List<IEntry> entries) {
        for(int i=0; i<entries.size(); i++) {
            updateOrder(entries.get(i), i);
        }
    }

    public void updateOrder(IEntry entry, int orderIndex) {
        EntryDTO entryDTO = mEntriesAccess.queryEntry(entry.getEntryId());
        entryDTO.setOrderIndex(orderIndex);

        mEntriesAccess.update(entryDTO);
    }
}

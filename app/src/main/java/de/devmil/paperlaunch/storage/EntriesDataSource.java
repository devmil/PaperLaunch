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

    private EntriesDataSource() {
    }

    private static final Object sInstanceLockObject = new Object();
    private static EntriesDataSource sInstance = null;
    public static EntriesDataSource getInstance() {
        synchronized (sInstanceLockObject) {
            if (sInstance == null) {
                sInstance = new EntriesDataSource();
            }
            return sInstance;
        }
    }

    public synchronized void accessData(Context context, ITransactionAction action) {
        boolean opened = mDatabase == null;
        if (opened) {
            open(context);
        }
        TransactionContext transactionContext = new TransactionContext();
        action.execute(transactionContext);
        if (opened) {
            close(transactionContext);
        }
    }

    private class TransactionContext implements ITransactionContext {
        @Override
        public void startTransaction() {
            mDatabase.beginTransaction();
        }

        @Override
        public void commitTransaction() {
            if (mDatabase != null && mDatabase.inTransaction()) {
                mDatabase.setTransactionSuccessful();
                mDatabase.endTransaction();
            }
        }

        @Override
        public void rollbackTransaction() {
            if (mDatabase != null && mDatabase.inTransaction()) {
                mDatabase.endTransaction();
            }
        }

        @Override
        public void clear() {
            startTransaction();
            mHelper.clear(mDatabase);
            commitTransaction();
        }

        @Override
        public Launch createLaunch(long parentFolderId) {
            return createLaunch(parentFolderId, -1);
        }

        @Override
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

        @Override
        public Folder createFolder(long parentFolderId) {
            return createFolder(parentFolderId, -1);
        }

        @Override
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

        @Override
        public Launch loadLaunch(long launchId) {
            LaunchDTO launch = mLaunchesAccess.queryLaunch(launchId);
            EntryDTO entry = mEntriesAccess.queryEntryForLaunch(launchId);

            return createLaunchFromDTO(launch, entry);
        }

        @Override
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

        @Override
        public Folder loadFolder(long folderId) {
            FolderDTO folder = mFoldersAccess.queryFolder(folderId);
            EntryDTO entry = mEntriesAccess.queryEntryForFolder(folderId);

            List<EntryDTO> subEntryDTOs = mEntriesAccess.queryAllEntries(folder.getId());

            return createFolderFromDTO(folder, entry, subEntryDTOs);
        }

        @Override
        public void deleteEntry(long entryId) {
            EntryDTO entryDto = mEntriesAccess.queryEntry(entryId);
            if(entryDto != null) {
                if(entryDto.getFolderId() > 0) {
                    deleteFolderContent(entryDto.getFolderId());
                    mFoldersAccess.delete(entryDto.getFolderId());
                }
                else if(entryDto.getLaunchId() > 0) {
                    mLaunchesAccess.delete(entryDto.getLaunchId());
                }
                mEntriesAccess.delete(entryDto);
            }
        }

        private void deleteFolderContent(long folderId) {
            Folder folder = loadFolder(folderId);
            for(IEntry entry : folder.getSubEntries()) {
                deleteEntry(entry.getEntryId());
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

        @Override
        public void updateLaunchData(Launch launch) {
            LaunchDTO launchDto = launch.getDto();

            mLaunchesAccess.update(launchDto);
        }

        @Override
        public void updateFolderData(Folder folder) {
            updateFolderData(folder.getDto());
        }

        @Override
        public void updateFolderData(FolderDTO folderDto) {
            mFoldersAccess.update(folderDto);
        }

        @Override
        public void updateOrders(Folder folder) {
            updateOrders(folder.getSubEntries());
        }

        @Override
        public void updateOrders(List<IEntry> entries) {
            for(int i=0; i<entries.size(); i++) {
                updateOrder(entries.get(i), i);
            }
        }

        @Override
        public void updateOrder(IEntry entry, int orderIndex) {
            EntryDTO entryDTO = mEntriesAccess.queryEntry(entry.getEntryId());
            entryDTO.setOrderIndex(orderIndex);

            mEntriesAccess.update(entryDTO);
        }
    }

    private void open(Context context) throws SQLiteException {
        mHelper = new EntriesSQLiteOpenHelper(context);
        mDatabase = mHelper.getWritableDatabase();
        mEntriesAccess = new EntriesAccess(mDatabase);
        mFoldersAccess = new FoldersAccess(context, mDatabase);
        mLaunchesAccess = new LaunchesAccess(context, mDatabase);
    }

    private void close(ITransactionContext context) {
        context.rollbackTransaction();
        mDatabase.close();
        mDatabase = null;
        mEntriesAccess = null;
        mFoldersAccess = null;
        mLaunchesAccess = null;
        mHelper.close();
    }
}

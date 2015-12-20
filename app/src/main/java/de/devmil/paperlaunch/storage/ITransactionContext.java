package de.devmil.paperlaunch.storage;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.model.Folder;
import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.model.Launch;

public interface ITransactionContext {
    void startTransaction();
    void commitTransaction();
    void rollbackTransaction();
    void clear();
    Launch createLaunch(long parentFolderId);
    Launch createLaunch(long parentFolderId, int orderIndex);
    Folder createFolder(long parentFolderId);
    Folder createFolder(long parentFolderId, int orderIndex);
    Launch loadLaunch(long launchId);
    List<IEntry> loadRootContent();
    Folder loadFolder(long folderId);
    void deleteEntry(long entryId);
    void updateLaunchData(Launch launch);
    void updateFolderData(Folder folder);
    void updateFolderData(FolderDTO folderDto);
    void updateOrders(Folder folder);
    void updateOrders(List<IEntry> entries);
    void updateOrder(IEntry entry, int orderIndex);
}

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
    Folder createFolder(long parentFolderId, int orderIndex, int parentFolderDepth);
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

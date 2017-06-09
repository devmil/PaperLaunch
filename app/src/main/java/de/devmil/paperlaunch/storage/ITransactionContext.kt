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
package de.devmil.paperlaunch.storage

import java.util.ArrayList

import de.devmil.paperlaunch.model.Folder
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.model.Launch

interface ITransactionContext {
    fun startTransaction()
    fun commitTransaction()
    fun rollbackTransaction()
    fun clear()
    fun createLaunch(parentFolderId: Long): Launch
    fun createLaunch(parentFolderId: Long, orderIndex: Int): Launch
    fun createFolder(parentFolderId: Long, orderIndex: Int, parentFolderDepth: Int): Folder
    fun loadLaunch(launchId: Long): Launch
    fun loadRootContent(): List<IEntry>
    fun loadFolder(folderId: Long): Folder
    fun deleteEntry(entryId: Long)
    fun updateLaunchData(launch: Launch)
    fun updateFolderData(folder: Folder)
    fun updateFolderData(folderDto: FolderDTO)
    fun updateOrders(folder: Folder)
    fun updateOrders(entries: List<IEntry>)
    fun updateOrder(entry: IEntry, orderIndex: Int)
}

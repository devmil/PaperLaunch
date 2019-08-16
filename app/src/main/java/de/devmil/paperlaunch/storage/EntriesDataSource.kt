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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import de.devmil.paperlaunch.model.*

import java.util.ArrayList

class EntriesDataSource private constructor() {
    private var helper: EntriesSQLiteOpenHelper? = null
    private var database: SQLiteDatabase? = null
    private var entriesAccess: EntriesAccess? = null
    private var foldersAccess: FoldersAccess? = null
    private var launchesAccess: LaunchesAccess? = null
    private var contextAccess: IContextAccess? = null;

    @Synchronized fun accessData(context: Context, action: ITransactionAction) {
        val opened = database == null
        if (opened) {
            open(context)
        }
        val transactionContext = TransactionContext(contextAccess!!)
        action.execute(transactionContext)
        if (opened) {
            close(transactionContext)
        }
    }

    private inner class TransactionContext(private val contextAccess: IContextAccess) : ITransactionContext {

        override fun startTransaction() {
            database!!.beginTransaction()
        }

        override fun commitTransaction() {
            if (database != null && database!!.inTransaction()) {
                database!!.setTransactionSuccessful()
                database!!.endTransaction()
            }
        }

        override fun rollbackTransaction() {
            if (database != null && database!!.inTransaction()) {
                database!!.endTransaction()
            }
        }

        override fun clear() {
            startTransaction()
            helper!!.clear(database!!)
            commitTransaction()
        }

        override fun createLaunch(parentFolderId: Long): Launch {
            return createLaunch(parentFolderId, -1)
        }

        override fun createLaunch(parentFolderId: Long, orderIndex: Int): Launch {
            //create Entry
            val entry = entriesAccess!!.createNew(orderIndex)
            entry!!.parentFolderId = parentFolderId
            //create Launch
            val launch = launchesAccess!!.createNew()
            //relate them
            entry.launchId = launch!!.id

            entriesAccess!!.update(entry)

            return loadLaunch(launch.id)
        }

        override fun createFolder(parentFolderId: Long, orderIndex: Int, parentFolderDepth: Int): Folder {
            //create Entry
            val entry = entriesAccess!!.createNew(orderIndex)
            entry!!.parentFolderId = parentFolderId
            //create folder
            val folder = foldersAccess!!.createNew()
            folder!!.depth = parentFolderDepth + 1
            foldersAccess!!.update(folder)
            //relate them
            entry.folderId = folder.id

            entriesAccess!!.update(entry)

            return loadFolder(folder.id)
        }

        override fun loadLaunch(launchId: Long): Launch {
            val launch = launchesAccess!!.queryLaunch(launchId)
            val entry = entriesAccess!!.queryEntryForLaunch(launchId)

            return createLaunchFromDTO(launch!!, entry!!)
        }

        override fun loadRootContent(): List<IEntry> {
            val entryDTOs = entriesAccess!!.queryAllEntries(-1)

            val entries = ArrayList<IEntry>()
            for (entryDto in entryDTOs) {
                entries.add(loadEntry(entryDto)!!)
            }

            return entries
        }

        private fun createLaunchFromDTO(dto: LaunchDTO, entryDto: EntryDTO): Launch {
            return Launch(contextAccess, dto, entryDto)
        }

        override fun loadFolder(folderId: Long): Folder {
            val folder = foldersAccess!!.queryFolder(folderId)
            val entry = entriesAccess!!.queryEntryForFolder(folderId)

            val subEntryDTOs = entriesAccess!!.queryAllEntries(folder!!.id)

            return createFolderFromDTO(folder, entry!!, subEntryDTOs)
        }

        override fun deleteEntry(entryId: Long) {
            val entryDto = entriesAccess!!.queryEntry(entryId)
            if (entryDto != null) {
                if (entryDto.folderId > 0) {
                    deleteFolderContent(entryDto.folderId)
                    foldersAccess!!.delete(entryDto.folderId)
                } else if (entryDto.launchId > 0) {
                    launchesAccess!!.delete(entryDto.launchId)
                }
                entriesAccess!!.delete(entryDto)
            }
        }

        private fun deleteFolderContent(folderId: Long) {
            val folder = loadFolder(folderId)
            for (entry in folder.subEntries.orEmpty()) {
                deleteEntry(entry.entryId)
            }
        }

        private fun createFolderFromDTO(dto: FolderDTO, entryDto: EntryDTO, subEntryDTOs: List<EntryDTO>): Folder {
            val subEntries = ArrayList<IEntry>()
            for (subEntryDto in subEntryDTOs) {
                subEntries.add(loadEntry(subEntryDto)!!)
            }

            return Folder(contextAccess, dto, entryDto, subEntries)
        }

        private fun loadEntry(entryDto: EntryDTO): IEntry? {
            if (entryDto.folderId > 0) {
                return loadFolder(entryDto.folderId)
            } else if (entryDto.launchId > 0) {
                return loadLaunch(entryDto.launchId)
            }
            return null
        }

        override fun updateLaunchData(launch: Launch) {
            val launchDto = launch.dto

            launchesAccess!!.update(launchDto)
        }

        override fun updateFolderData(folder: Folder) {
            updateFolderData(folder.dto)
        }

        override fun updateFolderData(folderDto: FolderDTO) {
            foldersAccess!!.update(folderDto)
        }

        override fun updateOrders(folder: Folder) {
            updateOrders(folder.subEntries.orEmpty())
        }

        override fun updateOrders(entries: List<IEntry>) {
            for (i in entries.indices) {
                updateOrder(entries[i], i)
            }
        }

        override fun updateOrder(entry: IEntry, orderIndex: Int) {
            val entryDTO = entriesAccess!!.queryEntry(entry.entryId)

            if (entryDTO != null) {
                entryDTO.orderIndex = orderIndex.toLong()
                entriesAccess!!.update(entryDTO)
            }
        }
    }

    @Throws(SQLiteException::class)
    private fun open(context: Context) {
        helper = EntriesSQLiteOpenHelper(context)
        database = helper!!.writableDatabase
        entriesAccess = EntriesAccess(database!!)
        foldersAccess = FoldersAccess(context, database!!)
        launchesAccess = LaunchesAccess(context, database!!)
        contextAccess = AndroidContextAccess(context)
    }

    private fun close(context: ITransactionContext) {
        context.rollbackTransaction()
        database!!.close()
        database = null
        entriesAccess = null
        foldersAccess = null
        launchesAccess = null
        helper!!.close()
    }

    companion object {

        private val sInstanceLockObject = Any()
        private var sInstance: EntriesDataSource? = null
        val instance: EntriesDataSource
            get() = synchronized(sInstanceLockObject) {
                if (sInstance == null) {
                    sInstance = EntriesDataSource()
                }
                return sInstance!!
            }
    }
}

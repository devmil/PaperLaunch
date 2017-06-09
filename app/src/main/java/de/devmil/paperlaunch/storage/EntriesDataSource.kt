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

import java.util.ArrayList

import de.devmil.paperlaunch.model.Folder
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.model.Launch

class EntriesDataSource private constructor() {
    private var mHelper: EntriesSQLiteOpenHelper? = null
    private var mDatabase: SQLiteDatabase? = null
    private var mEntriesAccess: EntriesAccess? = null
    private var mFoldersAccess: FoldersAccess? = null
    private var mLaunchesAccess: LaunchesAccess? = null

    @Synchronized fun accessData(context: Context, action: ITransactionAction) {
        val opened = mDatabase == null
        if (opened) {
            open(context)
        }
        val transactionContext = TransactionContext()
        action.execute(transactionContext)
        if (opened) {
            close(transactionContext)
        }
    }

    private inner class TransactionContext : ITransactionContext {
        override fun startTransaction() {
            mDatabase!!.beginTransaction()
        }

        override fun commitTransaction() {
            if (mDatabase != null && mDatabase!!.inTransaction()) {
                mDatabase!!.setTransactionSuccessful()
                mDatabase!!.endTransaction()
            }
        }

        override fun rollbackTransaction() {
            if (mDatabase != null && mDatabase!!.inTransaction()) {
                mDatabase!!.endTransaction()
            }
        }

        override fun clear() {
            startTransaction()
            mHelper!!.clear(mDatabase!!)
            commitTransaction()
        }

        override fun createLaunch(parentFolderId: Long): Launch {
            return createLaunch(parentFolderId, -1)
        }

        override fun createLaunch(parentFolderId: Long, orderIndex: Int): Launch {
            //create Entry
            val entry = mEntriesAccess!!.createNew(orderIndex)
            entry!!.parentFolderId = parentFolderId
            //create Launch
            val launch = mLaunchesAccess!!.createNew()
            //relate them
            entry.launchId = launch!!.id

            mEntriesAccess!!.update(entry)

            return loadLaunch(launch.id)
        }

        override fun createFolder(parentFolderId: Long, orderIndex: Int, parentFolderDepth: Int): Folder {
            //create Entry
            val entry = mEntriesAccess!!.createNew(orderIndex)
            entry!!.parentFolderId = parentFolderId
            //create folder
            val folder = mFoldersAccess!!.createNew()
            folder!!.depth = parentFolderDepth + 1
            mFoldersAccess!!.update(folder)
            //relate them
            entry.folderId = folder.id

            mEntriesAccess!!.update(entry)

            return loadFolder(folder.id)
        }

        override fun loadLaunch(launchId: Long): Launch {
            val launch = mLaunchesAccess!!.queryLaunch(launchId)
            val entry = mEntriesAccess!!.queryEntryForLaunch(launchId)

            return createLaunchFromDTO(launch!!, entry!!)
        }

        override fun loadRootContent(): List<IEntry> {
            val entryDTOs = mEntriesAccess!!.queryAllEntries(-1)

            val entries = ArrayList<IEntry>()
            for (entryDto in entryDTOs) {
                entries.add(loadEntry(entryDto)!!)
            }

            return entries
        }

        private fun createLaunchFromDTO(dto: LaunchDTO, entryDto: EntryDTO): Launch {
            return Launch(dto, entryDto)
        }

        override fun loadFolder(folderId: Long): Folder {
            val folder = mFoldersAccess!!.queryFolder(folderId)
            val entry = mEntriesAccess!!.queryEntryForFolder(folderId)

            val subEntryDTOs = mEntriesAccess!!.queryAllEntries(folder!!.id)

            return createFolderFromDTO(folder, entry!!, subEntryDTOs)
        }

        override fun deleteEntry(entryId: Long) {
            val entryDto = mEntriesAccess!!.queryEntry(entryId)
            if (entryDto != null) {
                if (entryDto.folderId > 0) {
                    deleteFolderContent(entryDto.folderId)
                    mFoldersAccess!!.delete(entryDto.folderId)
                } else if (entryDto.launchId > 0) {
                    mLaunchesAccess!!.delete(entryDto.launchId)
                }
                mEntriesAccess!!.delete(entryDto)
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

            return Folder(dto, entryDto, subEntries)
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

            mLaunchesAccess!!.update(launchDto)
        }

        override fun updateFolderData(folder: Folder) {
            updateFolderData(folder.dto)
        }

        override fun updateFolderData(folderDto: FolderDTO) {
            mFoldersAccess!!.update(folderDto)
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
            val entryDTO = mEntriesAccess!!.queryEntry(entry.entryId)

            if (entryDTO != null) {
                entryDTO.orderIndex = orderIndex.toLong()
                mEntriesAccess!!.update(entryDTO)
            }
        }
    }

    @Throws(SQLiteException::class)
    private fun open(context: Context) {
        mHelper = EntriesSQLiteOpenHelper(context)
        mDatabase = mHelper!!.writableDatabase
        mEntriesAccess = EntriesAccess(mDatabase!!)
        mFoldersAccess = FoldersAccess(context, mDatabase!!)
        mLaunchesAccess = LaunchesAccess(context, mDatabase!!)
    }

    private fun close(context: ITransactionContext) {
        context.rollbackTransaction()
        mDatabase!!.close()
        mDatabase = null
        mEntriesAccess = null
        mFoldersAccess = null
        mLaunchesAccess = null
        mHelper!!.close()
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

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

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import java.util.ArrayList

class EntriesAccess(private val database: SQLiteDatabase) {

    fun queryAllEntries(parentFolderId: Long): List<EntryDTO> {
        val selection = EntriesSQLiteOpenHelper.COLUMN_ENTRIES_PARENTFOLDERID + " = " + parentFolderId

        val c = database.query(
                EntriesSQLiteOpenHelper.TABLE_ENTRIES,
                entriesColumns,
                selection,
                null, null, null,
                EntriesSQLiteOpenHelper.COLUMN_ENTRIES_ORDERINDEX
        )

        val result = ArrayList<EntryDTO>()

        if (c.moveToFirst()) {
            do {
                val entry = cursorToEntry(c)
                result.add(entry)
            } while (c.moveToNext())
        }

        c.close()

        return result
    }

    fun queryEntry(entryId: Long): EntryDTO? {
        val c = database.query(
                EntriesSQLiteOpenHelper.TABLE_ENTRIES,
                entriesColumns,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + entryId, null, null, null, null
        )

        var result: EntryDTO? = null

        if (c.moveToFirst()) {
            result = cursorToEntry(c)
        }
        c.close()
        return result
    }

    fun queryEntryForFolder(folderId: Long): EntryDTO? {
        val c = database.query(
                EntriesSQLiteOpenHelper.TABLE_ENTRIES,
                entriesColumns,
                EntriesSQLiteOpenHelper.COLUMN_ENTRIES_FOLDERID + " = " + folderId, null, null, null, null
        )

        var result: EntryDTO? = null

        if (c.moveToFirst()) {
            result = cursorToEntry(c)
        }
        c.close()
        return result
    }

    fun queryEntryForLaunch(launchId: Long): EntryDTO? {
        val c = database.query(
                EntriesSQLiteOpenHelper.TABLE_ENTRIES,
                entriesColumns,
                EntriesSQLiteOpenHelper.COLUMN_ENTRIES_LAUNCHID + " = " + launchId, null, null, null, null
        )

        var result: EntryDTO? = null
        if (c.moveToFirst()) {
            result = cursorToEntry(c)
        }
        c.close()
        return result
    }

    fun createNew(orderIndex: Int): EntryDTO? {
        val values = ContentValues()
        values.put(EntriesSQLiteOpenHelper.COLUMN_ENTRIES_ORDERINDEX, orderIndex)
        val id = database.insert(
                EntriesSQLiteOpenHelper.TABLE_ENTRIES, null,
                values)

        return queryEntry(id)
    }

    fun update(entry: EntryDTO) {
        val values = ContentValues()

        entryToValues(entry, values)

        database.update(
                EntriesSQLiteOpenHelper.TABLE_ENTRIES,
                values,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + entry.id, null
        )
    }

    fun delete(entry: EntryDTO) {
        database.delete(
                EntriesSQLiteOpenHelper.TABLE_ENTRIES,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + entry.id, null
        )
    }

    private fun cursorToEntry(cursor: Cursor): EntryDTO {
        return EntryDTO(
                cursor.getInt(INDEX_COLUMN_ID).toLong(),
                cursor.getInt(INDEX_COLUMN_ORDERINDEX).toLong(),
                cursor.getInt(INDEX_COLUMN_LAUNCHID).toLong(),
                cursor.getInt(INDEX_COLUMN_FOLDERID).toLong(),
                cursor.getInt(INDEX_COLUMN_PARENTFOLDERID).toLong()
        )
    }

    private fun entryToValues(entry: EntryDTO, values: ContentValues) {
        values.put(EntriesSQLiteOpenHelper.COLUMN_ENTRIES_FOLDERID, entry.folderId)
        values.put(EntriesSQLiteOpenHelper.COLUMN_ENTRIES_LAUNCHID, entry.launchId)
        values.put(EntriesSQLiteOpenHelper.COLUMN_ENTRIES_ORDERINDEX, entry.orderIndex)
        values.put(EntriesSQLiteOpenHelper.COLUMN_ENTRIES_PARENTFOLDERID, entry.parentFolderId)
    }

    companion object {

        private val entriesColumns = arrayOf(EntriesSQLiteOpenHelper.COLUMN_ID, EntriesSQLiteOpenHelper.COLUMN_ENTRIES_FOLDERID, EntriesSQLiteOpenHelper.COLUMN_ENTRIES_LAUNCHID, EntriesSQLiteOpenHelper.COLUMN_ENTRIES_PARENTFOLDERID, EntriesSQLiteOpenHelper.COLUMN_ENTRIES_ORDERINDEX)

        private val INDEX_COLUMN_ID = 0
        private val INDEX_COLUMN_FOLDERID = 1
        private val INDEX_COLUMN_LAUNCHID = 2
        private val INDEX_COLUMN_PARENTFOLDERID = 3
        private val INDEX_COLUMN_ORDERINDEX = 4
    }
}

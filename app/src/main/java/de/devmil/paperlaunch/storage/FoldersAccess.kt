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
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import de.devmil.paperlaunch.utils.BitmapUtils

class FoldersAccess(private val context: Context, private val database: SQLiteDatabase) {

    fun queryFolder(folderId: Long): FolderDTO? {
        val c = database.query(
                EntriesSQLiteOpenHelper.TABLE_FOLDERS,
                foldersColumns,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + folderId,
                null, null, null, null
        )

        var result: FolderDTO? = null
        if (c.moveToFirst()) {
            result = cursorToFolder(c)
        }
        c.close()
        return result
    }

    fun createNew(): FolderDTO? {
        val values = ContentValues()
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_NAME, null as String?)

        val id = database.insert(
                EntriesSQLiteOpenHelper.TABLE_FOLDERS, null,
                values)

        return queryFolder(id)
    }

    fun update(folder: FolderDTO) {
        val values = ContentValues()

        folderToValues(folder, values)

        database.update(
                EntriesSQLiteOpenHelper.TABLE_FOLDERS,
                values,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + folder.id, null
        )
    }

    @Suppress("unused")
    fun delete(folder: FolderDTO) {
        delete(folder.id)
    }

    fun delete(folderId: Long) {
        database.delete(
                EntriesSQLiteOpenHelper.TABLE_FOLDERS,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + folderId, null
        )
    }

    private fun cursorToFolder(cursor: Cursor): FolderDTO {
        return FolderDTO(
                cursor.getInt(INDEX_COLUMN_ID).toLong(),
                cursor.getString(INDEX_COLUMN_NAME),
                BitmapUtils.getIcon(context, cursor.getBlob(INDEX_COLUMN_ICON)),
                cursor.getInt(INDEX_COLUMN_DEPTH)
        )
    }

    private fun folderToValues(folder: FolderDTO, values: ContentValues) {
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_NAME, folder.name)
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_ICON, BitmapUtils.getBytes(folder.icon))
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_DEPTH, folder.depth)
    }

    companion object {
        private val foldersColumns = arrayOf(EntriesSQLiteOpenHelper.COLUMN_ID, EntriesSQLiteOpenHelper.COLUMN_FOLDERS_NAME, EntriesSQLiteOpenHelper.COLUMN_FOLDERS_ICON, EntriesSQLiteOpenHelper.COLUMN_FOLDERS_DEPTH)
        private val INDEX_COLUMN_ID = 0
        private val INDEX_COLUMN_NAME = 1
        private val INDEX_COLUMN_ICON = 2
        private val INDEX_COLUMN_DEPTH = 3
    }
}

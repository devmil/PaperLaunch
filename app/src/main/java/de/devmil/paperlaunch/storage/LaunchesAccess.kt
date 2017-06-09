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
import de.devmil.paperlaunch.utils.IntentSerializer

class LaunchesAccess(private val mContext: Context, private val mDatabase: SQLiteDatabase) {

    fun queryLaunch(launchId: Long): LaunchDTO? {
        val c = mDatabase.query(
                EntriesSQLiteOpenHelper.TABLE_LAUNCHES,
                launchesColumns,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + launchId,
                null, null, null, null
        )

        var result: LaunchDTO? = null
        if (c.moveToFirst()) {
            result = cursorToLaunch(c)
        }
        c.close()
        return result
    }

    fun createNew(): LaunchDTO? {
        val values = ContentValues()
        values.put(EntriesSQLiteOpenHelper.COLUMN_LAUNCHES_NAME, null as String?)

        val id = mDatabase.insert(
                EntriesSQLiteOpenHelper.TABLE_LAUNCHES, null,
                values)

        return queryLaunch(id)
    }

    fun update(launch: LaunchDTO) {
        val values = ContentValues()

        launchToValues(launch, values)

        mDatabase.update(
                EntriesSQLiteOpenHelper.TABLE_LAUNCHES,
                values,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + launch.id, null
        )
    }

    fun delete(launch: LaunchDTO) {
        delete(launch.id)
    }

    fun delete(launchId: Long) {
        mDatabase.delete(
                EntriesSQLiteOpenHelper.TABLE_LAUNCHES,
                EntriesSQLiteOpenHelper.COLUMN_ID + " = " + launchId, null
        )
    }

    private fun cursorToLaunch(cursor: Cursor): LaunchDTO {
        return LaunchDTO(
                cursor.getInt(INDEX_COLUMN_ID).toLong(),
                cursor.getString(INDEX_COLUMN_NAME),
                IntentSerializer.deserialize(cursor.getString(INDEX_COLUMN_LAUNCHINTENT)),
                BitmapUtils.getIcon(mContext, cursor.getBlob(INDEX_COLUMN_ICON))
        )
    }

    private fun launchToValues(launch: LaunchDTO, values: ContentValues) {
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_NAME, launch.name)
        values.put(EntriesSQLiteOpenHelper.COLUMN_LAUNCHES_LAUNCHINTENT, IntentSerializer.serialize(launch.launchIntent))
        values.put(EntriesSQLiteOpenHelper.COLUMN_FOLDERS_ICON, BitmapUtils.getBytes(launch.icon))
    }

    companion object {
        private val launchesColumns = arrayOf(EntriesSQLiteOpenHelper.COLUMN_ID, EntriesSQLiteOpenHelper.COLUMN_LAUNCHES_NAME, EntriesSQLiteOpenHelper.COLUMN_LAUNCHES_LAUNCHINTENT, EntriesSQLiteOpenHelper.COLUMN_LAUNCHES_ICON)
        private val INDEX_COLUMN_ID = 0
        private val INDEX_COLUMN_NAME = 1
        private val INDEX_COLUMN_LAUNCHINTENT = 2
        private val INDEX_COLUMN_ICON = 3
    }
}

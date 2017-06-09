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
package de.devmil.paperlaunch.model

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable

import de.devmil.paperlaunch.storage.EntryDTO
import de.devmil.paperlaunch.storage.LaunchDTO
import de.devmil.paperlaunch.utils.AppMetadataUtils

class Launch(val dto: LaunchDTO, private val mEntryDto: EntryDTO) : IEntry {

    private var mDefaultAppName: String? = null
    private var mDefaultAppIcon: Drawable? = null

    override val id: Long
        get() = dto.id

    override val entryId: Long
        get() = mEntryDto.id

    override val orderIndex: Long
        get() = mEntryDto.orderIndex

    override fun getName(context: Context): String? {
        if (dto.name != null) {
            return dto.name
        }
        if (mDefaultAppName == null) {
            val launchIntent = launchIntent ?: return null

            mDefaultAppName = AppMetadataUtils.getAppName(context, launchIntent)
        }
        return mDefaultAppName
    }

    override fun getIcon(context: Context): Drawable? {
        if (dto.icon != null) {
            return dto.icon
        }
        if (mDefaultAppIcon == null) {
            val launchIntent = launchIntent
            if (launchIntent != null) {
                mDefaultAppIcon = AppMetadataUtils.getAppIcon(context, launchIntent)
            }
        }
        return mDefaultAppIcon
    }

    override fun getFolderSummaryIcon(context: Context): Drawable? {
        return getIcon(context)
    }

    override val isFolder: Boolean
        get() = false

    override fun useIconColor(): Boolean {
        return true
    }

    val launchIntent: Intent?
        get() = dto.launchIntent
}

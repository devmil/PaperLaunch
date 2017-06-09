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

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Drawable

import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.storage.EntryDTO
import de.devmil.paperlaunch.storage.FolderDTO

class Folder(val dto: FolderDTO, private val mEntryDto: EntryDTO, private var mSubEntries: List<IEntry>?) : IFolder {

    override val id: Long
        get() = dto.id

    override val entryId: Long
        get() = mEntryDto.id

    override val orderIndex: Long
        get() = mEntryDto.orderIndex

    override fun getName(context: Context): String? {
        return dto.name
    }

    override fun getFolderSummaryIcon(context: Context): Drawable {
        return context.resources.getDrawable(R.mipmap.ic_folder_grey600_48dp, context.theme)
    }

    override val isFolder: Boolean
        get() = true

    override fun getIcon(context: Context): Drawable? {
        var result = dto.icon
        if (result == null) {
            result = context.getDrawable(R.mipmap.folder_frame)
        }
        return result
    }

    override fun useIconColor(): Boolean {
        return true
    }

    override var subEntries: List<IEntry>?
        get() = mSubEntries
        set(value) { mSubEntries = value }
}

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

import android.graphics.drawable.Drawable
import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.storage.EntryDTO
import de.devmil.paperlaunch.storage.FolderDTO

class Folder(private val contextAccess: IContextAccess, val dto: FolderDTO, private val entryDto: EntryDTO, private var subEntriesList: List<IEntry>?) : IFolder {

    override val id: Long
        get() = dto.id

    override val entryId: Long
        get() = entryDto.id

    override val orderIndex: Long
        get() = entryDto.orderIndex

    override val name: String?
        get() = dto.name

    override val folderSummaryIcon: Drawable
        get() = contextAccess.getDrawable(R.mipmap.ic_folder_grey600_48dp, true)

    override val isFolder: Boolean
        get() = true

    override val icon: Drawable?
        get() {
            var result = dto.icon
            if (result == null) {
                result = contextAccess.getDrawable(R.mipmap.folder_frame, false)
            }
            return result
        }

    override val useIconColor: Boolean
        get() {
            return true
        }

    override var subEntries: List<IEntry>?
        get() = subEntriesList
        set(value) { subEntriesList = value }
}

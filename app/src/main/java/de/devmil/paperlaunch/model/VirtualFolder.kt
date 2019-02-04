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
import android.graphics.drawable.Drawable

class VirtualFolder(private val name: String, private val icon: Drawable, subEntries: List<IEntry>) : IFolder {
    override var subEntries: List<IEntry>? = null

    init {
        this.subEntries = subEntries
    }

    override val id: Long
        get() = -1

    override val entryId: Long
        get() = -1

    override val orderIndex: Long
        get() = -1

    override fun getName(): String? {
        return name
    }

    override fun getIcon(): Drawable? {
        return icon
    }

    override fun getFolderSummaryIcon(): Drawable? {
        return icon
    }

    override val isFolder: Boolean
        get() = true

    override fun useIconColor(): Boolean {
        return false
    }
}

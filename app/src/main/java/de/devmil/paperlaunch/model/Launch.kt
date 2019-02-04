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

import android.content.Intent
import android.graphics.drawable.Drawable
import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.storage.EntryDTO
import de.devmil.paperlaunch.storage.LaunchDTO

class Launch(private val contextAccess: IContextAccess, val dto: LaunchDTO, private val entryDto: EntryDTO) : IEntry {

    private var defaultAppName: String? = null
    private var defaultAppIcon: Drawable? = null

    override val id: Long
        get() = dto.id

    override val entryId: Long
        get() = entryDto.id

    override val orderIndex: Long
        get() = entryDto.orderIndex

    override val name: String?
        get() {
            if (dto.name != null) {
                return dto.name
            }
            if (defaultAppName == null) {
                defaultAppName = launchIntent?.let {
                    when (it.action) {
                        Intent.ACTION_MAIN -> {
                            contextAccess.getAppName(it)
                        }
                        Intent.ACTION_VIEW -> {
                            if (it.hasExtra(EXTRA_URL_NAME)) {
                                it.getStringExtra(EXTRA_URL_NAME)
                            } else {
                                it.data.toString()
                            }
                        }
                        else -> {
                            null
                        }
                    }
                }
            }
            return defaultAppName
        }

    override val icon: Drawable?
        get() {
            if (dto.icon != null) {
                return dto.icon
            }
            if (defaultAppIcon == null) {
                defaultAppIcon = launchIntent?.let {
                    when (it.action) {
                        Intent.ACTION_MAIN -> {
                            contextAccess.getAppIcon(it)
                        }
                        Intent.ACTION_VIEW -> {
                            contextAccess.getDrawable(R.mipmap.ic_web_black_48dp, false)
                        }
                        else -> {
                            null
                        }
                    }
                }
            }
            return defaultAppIcon
        }

    override val folderSummaryIcon: Drawable?
        get() {
            return icon
        }

    override val isFolder: Boolean
        get() = false

    override val useIconColor: Boolean
        get() {
            return true
        }

    val launchIntent: Intent?
        get() = dto.launchIntent

    companion object {
        val EXTRA_URL_NAME = "de.devmil.paperlaunch.EXTRA_URL_NAME"
    }
}

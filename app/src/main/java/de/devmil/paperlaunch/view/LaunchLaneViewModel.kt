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
package de.devmil.paperlaunch.view

import java.util.ArrayList

import de.devmil.paperlaunch.config.ILaunchLaneConfig
import de.devmil.paperlaunch.config.LauncherGravity

class LaunchLaneViewModel(entryModels: List<LaunchEntryViewModel>, private val mConfig: ILaunchLaneConfig) {
    val entries: List<LaunchEntryViewModel>
    var state = State.Init

    init {
        entries = ArrayList(entryModels)
    }

    val imageWidthDip: Float
        get() = mConfig.imageWidthDip

    @Suppress("unused")
    val highElevationDip: Float
        get() = mConfig.highElevationDip

    val isOnRightSide: Boolean
        get() = mConfig.isOnRightSide

    val entryMoveDiffMS: Int
        get() = mConfig.entryMoveDiffMS

    val frameDefaultColor: Int
        get() = mConfig.designConfig.frameDefaultColor

    val unknownAppImageId: Int
        get() = mConfig.designConfig.unknownAppImageId

    val selectedImageElevationDip: Float
        get() = mConfig.highElevationDip

    val selectingAnimationDurationMS: Int
        get() = mConfig.selectingAnimationDurationMS

    val laneIconTopMarginDip: Float
        get() = mConfig.laneIconTopMarginDip

    val laneIconMarginsDip: Float
        get() = mConfig.laneIconMarginsDip

    val laneTextTopMarginDip: Float
        get() = mConfig.laneTextTopMarginDip

    val itemNameTextSizeSP: Float
        get() = mConfig.itemNameTextSizeSP

    enum class State {
        Init,
        Focusing,
        Selecting,
        Selected
    }

    val launcherGravity: LauncherGravity?
        get() = mConfig.launcherGravity
}

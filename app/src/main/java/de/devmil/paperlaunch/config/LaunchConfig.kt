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
package de.devmil.paperlaunch.config

import android.graphics.Color

import java.util.ArrayList

import de.devmil.paperlaunch.model.IEntry

class LaunchConfig(userSettings: IUserSettings) : ILaunchEntryConfig, ILaunchLaneConfig {
    private var mEntries: List<IEntry>? = null

    private val BASE_IMAGE_WIDTH_DIP = 48f

    override val imageMarginDip = 1f
    override val entriesMarginDip = 3f
    override val lowElevationDip = 2f
    override val highElevationDip = 6f
    override val entryMoveAnimationDuration = 100
    override val entryMoveDiffMS = 50
    override val entryAlphaAnimationDuration = 100
    override val selectingAnimationDurationMS = 200
    override val itemNameTextSizeSP = 30f
    override val designConfig: IDesignConfig = DesignConfig()
    val neutralZoneWidthDip = 50f
    val launcherInitAnimationDurationMS = 150
    override val imageOffsetDip = 5f
    override val laneIconTopMarginDip = 5f
    override val laneIconMarginsDip = 3f
    override val laneTextTopMarginDip = 20f
    val launcherBackgroundColor = Color.BLACK
    val launcherBackgroundAlpha = 0.3f
    val launcherBackgroundAnimationDurationMS = 250
    val maxFolderDepth = 9
    val launcherHintAlpha = 0.28f

    //UserSettings
    @Suppress("JoinDeclarationAndAssignment")
    val launcherSensitivityDip: Int
    override val isOnRightSide: Boolean
    val showLauncherBackground: Boolean
    val isVibrateOnActivation: Boolean
    val launcherOffsetPosition: Int
    val launcherHeightPercent: Int
    val showLauncherHint : Boolean
    override val launcherGravity: LauncherGravity?
    override val imageWidthDip: Float
    override val showLogo: Boolean

    var entries: List<IEntry>
        get() = mEntries?.let { ArrayList(it) } ?: ArrayList()
        set(entries) {
            mEntries = ArrayList(entries)
        }

    init {
        launcherSensitivityDip = userSettings.sensitivityDip
        isOnRightSide = userSettings.isOnRightSide
        showLauncherBackground = userSettings.isShowBackground
        isVibrateOnActivation = userSettings.isVibrateOnActivation
        launcherGravity = userSettings.launcherGravity
        imageWidthDip = (BASE_IMAGE_WIDTH_DIP * userSettings.itemScalePercent) / 100f
        showLogo = userSettings.showLogo
        showLauncherHint = userSettings.isShowHint
        launcherOffsetPosition = userSettings.activationOffsetPosition
        launcherHeightPercent = userSettings.activationHeightPercent
    }
}

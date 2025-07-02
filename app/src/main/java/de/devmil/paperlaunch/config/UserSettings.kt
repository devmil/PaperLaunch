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

import android.content.Context

class UserSettings(context: Context) : IUserSettings {


    override var sensitivityDip: Int = 0
    override var isShowBackground: Boolean = false
    override var isVibrateOnActivation: Boolean = false
    override var isOnRightSide: Boolean = false
    override var launcherGravity: LauncherGravity = LauncherGravity.Center
    override var showLogo: Boolean = true
    override var itemScalePercent: Int = 100
    override var activationOffsetPosition: Int = 0
    override var activationHeightPercent: Int = 0
    override var isShowHint: Boolean = false


    init {
        load(context)
    }

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        sensitivityDip = prefs.getInt(KEY_SENSITIVITY_DIP, DEFAULT_SENSITIVITY_DIP)
        isShowBackground = prefs.getBoolean(KEY_SHOW_BACKGROUND, DEFAULT_SHOW_BACKGROUND)
        isVibrateOnActivation = prefs.getBoolean(KEY_VIBRATE_ON_ACTIVATION, DEFAULT_VIBRATE_ON_ACTIVATION)
        isOnRightSide = prefs.getBoolean(KEY_IS_ON_RIGHT_SIDE, DEFAULT_IS_ON_RIGHT_SIDE)
        launcherGravity = LauncherGravity.fromValue(prefs.getInt(KEY_LAUNCHER_GRAVITY, DEFAULT_LAUNCHER_GRAVITY.value))
        showLogo = prefs.getBoolean(KEY_SHOW_LOGO, DEFAULT_SHOW_LOGO)
        itemScalePercent = prefs.getInt(KEY_ITEM_SCALE_PERCENT, DEFAULT_ITEM_SCALE_PERCENT)
        activationOffsetPosition = prefs.getInt(KEY_ACTIVATION_OFFSET_POSITION, DEFAULT_ACTIVATION_OFFSET_POSITION)
        activationHeightPercent = prefs.getInt(KEY_ACTIVATION_HEIGHT_PERCENT, DEFAULT_ACTIVATION_HEIGHT_PERCENT)
        isShowHint = prefs.getBoolean(KEY_IS_SHOW_HINT, DEFAULT_SHOW_HINT)
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
                .putInt(KEY_SENSITIVITY_DIP, sensitivityDip)
                .putBoolean(KEY_SHOW_BACKGROUND, isShowBackground)
                .putBoolean(KEY_VIBRATE_ON_ACTIVATION, isVibrateOnActivation)
                .putBoolean(KEY_IS_ON_RIGHT_SIDE, isOnRightSide)
                .putInt(KEY_LAUNCHER_GRAVITY, launcherGravity.value)
                .putBoolean(KEY_SHOW_LOGO, showLogo)
                .putInt(KEY_ITEM_SCALE_PERCENT, itemScalePercent)
                .putBoolean(KEY_IS_SHOW_HINT, isShowHint)
                .putInt(KEY_ACTIVATION_OFFSET_POSITION, activationOffsetPosition)
                .putInt(KEY_ACTIVATION_HEIGHT_PERCENT, activationHeightPercent)
                .apply()
    }

    companion object {

        internal val SHARED_PREFS_NAME = "paperLaunch"

        internal val KEY_SENSITIVITY_DIP = "sensitivityDip"
        internal val KEY_SHOW_BACKGROUND = "showBackground"
        internal val KEY_VIBRATE_ON_ACTIVATION = "vibrateOnActivation"
        internal val KEY_IS_ON_RIGHT_SIDE = "isOnRightSide"
        internal val KEY_LAUNCHER_GRAVITY = "launcherGravity"
        internal val KEY_SHOW_LOGO = "showLogo"
        internal val KEY_ITEM_SCALE_PERCENT = "itemScalePercent"
        internal val KEY_ACTIVATION_OFFSET_POSITION = "activationOffsetPosition"
        internal val KEY_ACTIVATION_HEIGHT_PERCENT = "activationHeightPercent"
        internal val KEY_IS_SHOW_HINT = "isShowHint"


        private val DEFAULT_SENSITIVITY_DIP = 10
        private val DEFAULT_SHOW_BACKGROUND = false
        private val DEFAULT_VIBRATE_ON_ACTIVATION = false
        private val DEFAULT_IS_ON_RIGHT_SIDE = true
        private val DEFAULT_LAUNCHER_GRAVITY = LauncherGravity.Center
        private val DEFAULT_SHOW_LOGO = true
        private val DEFAULT_ITEM_SCALE_PERCENT = 100
        private val DEFAULT_SHOW_HINT = false
        private val DEFAULT_ACTIVATION_OFFSET_POSITION = 0
        private val DEFAULT_ACTIVATION_HEIGHT_PERCENT = 25
    }
}

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
    override var activationOffsetPositionDip: Int = 0
    override var activationOffsetHeightDip: Int = 0
    override var isShowBackground: Boolean = false
    override var isVibrateOnActivation: Boolean = false
    override var isOnRightSide: Boolean = false
    override var launcherGravity: LauncherGravity = LauncherGravity.Center

    init {
        load(context)
    }

    fun load(context: Context) {
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        sensitivityDip = prefs.getInt(KEY_SENSITIVITY_DIP, DEFAULT_SENSITIVITY_DIP)
        activationOffsetPositionDip = prefs.getInt(KEY_ACTIVATION_OFFSET_POSITION_DIP, DEFAULT_ACTIVATION_OFFSET_POSITION_DIP)
        activationOffsetHeightDip = prefs.getInt(KEY_ACTIVATION_OFFSET_HEIGHT_DIP, DEFAULT_ACTIVATION_OFFSET_HEIGHT_DIP)
        isShowBackground = prefs.getBoolean(KEY_SHOW_BACKGROUND, DEFAULT_SHOW_BACKGROUND)
        isVibrateOnActivation = prefs.getBoolean(KEY_VIBRATE_ON_ACTIVATION, DEFAULT_VIBRATE_ON_ACTIVATION)
        isOnRightSide = prefs.getBoolean(KEY_IS_ON_RIGHT_SIDE, DEFAULT_IS_ON_RIGHT_SIDE)
        launcherGravity = LauncherGravity.fromValue(prefs.getInt(KEY_LAUNCHER_GRAVITY, DEFAULT_LAUNCHER_GRAVITY.value))
    }

    fun save(context: Context) {
        val prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
                .putInt(KEY_SENSITIVITY_DIP, sensitivityDip)
                .putInt(KEY_ACTIVATION_OFFSET_POSITION_DIP, activationOffsetPositionDip)
                .putInt(KEY_ACTIVATION_OFFSET_HEIGHT_DIP, activationOffsetHeightDip)
                .putBoolean(KEY_SHOW_BACKGROUND, isShowBackground)
                .putBoolean(KEY_VIBRATE_ON_ACTIVATION, isVibrateOnActivation)
                .putBoolean(KEY_IS_ON_RIGHT_SIDE, isOnRightSide)
                .putInt(KEY_LAUNCHER_GRAVITY, launcherGravity.value)
                .apply()
    }

    companion object {

        private val SHARED_PREFS_NAME = "paperLaunch"

        private val KEY_SENSITIVITY_DIP = "sensitivityDip"
        private val KEY_ACTIVATION_OFFSET_POSITION_DIP = "activationOffsetPositionDip"
        private val KEY_ACTIVATION_OFFSET_HEIGHT_DIP = "activationOffsetHeightDip"
        private val KEY_SHOW_BACKGROUND = "showBackground"
        private val KEY_VIBRATE_ON_ACTIVATION = "vibrateOnActivation"
        private val KEY_IS_ON_RIGHT_SIDE = "isOnRightSide"
        private val KEY_LAUNCHER_GRAVITY = "launcherGravity"

        private val DEFAULT_SENSITIVITY_DIP = 10
        private val DEFAULT_ACTIVATION_OFFSET_POSITION_DIP = 0
        private val DEFAULT_ACTIVATION_OFFSET_HEIGHT_DIP = 0
        private val DEFAULT_SHOW_BACKGROUND = false
        private val DEFAULT_VIBRATE_ON_ACTIVATION = false
        private val DEFAULT_IS_ON_RIGHT_SIDE = true
        private val DEFAULT_LAUNCHER_GRAVITY = LauncherGravity.Center
    }
}

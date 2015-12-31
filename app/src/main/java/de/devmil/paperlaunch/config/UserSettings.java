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
package de.devmil.paperlaunch.config;

import android.content.Context;
import android.content.SharedPreferences;

import de.devmil.paperlaunch.config.IUserSettings;

public class UserSettings implements IUserSettings {

    private static final String SHARED_PREFS_NAME = "paperLaunch";

    private static final String KEY_SENSITIVITY_DIP = "sensitivityDip";
    private static final String KEY_ACTIVATION_OFFSET_POSITION_DIP = "activationOffsetPositionDip";
    private static final String KEY_ACTIVATION_OFFSET_HEIGHT_DIP = "activationOffsetHeightDip";
    private static final String KEY_SHOW_BACKGROUND = "showBackground";
    private static final String KEY_VIBRATE_ON_ACTIVATION = "vibrateOnActivation";
    private static final String KEY_IS_ON_RIGHT_SIDE = "isOnRightSide";
    private static final String KEY_LAUNCHER_GRAVITY = "launcherGravity";

    private static final int DEFAULT_SENSITIVITY_DIP = 15;
    private static final int DEFAULT_ACTIVATION_OFFSET_POSITION_DIP = 0;
    private static final int DEFAULT_ACTIVATION_OFFSET_HEIGHT_DIP = 0;
    private static final boolean DEFAULT_SHOW_BACKGROUND = false;
    private static final boolean DEFAULT_VIBRATE_ON_ACTIVATION = false;
    private static final boolean DEFAULT_IS_ON_RIGHT_SIDE = true;
    private static final LauncherGravity DEFAULT_LAUNCHER_GRAVITY = LauncherGravity.Center;


    private int mSensitivityDip;
    private int mActivationOffsetPositionDip;
    private int mActivationOffsetHeightDip;
    private boolean mShowBackground;
    private boolean mVibrateOnActivation;
    private boolean mIsOnRightSide;
    private LauncherGravity mLauncherGravity;

    public UserSettings(Context context) {
        load(context);
    }

    public void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        mSensitivityDip = prefs.getInt(KEY_SENSITIVITY_DIP, DEFAULT_SENSITIVITY_DIP);
        mActivationOffsetPositionDip = prefs.getInt(KEY_ACTIVATION_OFFSET_POSITION_DIP, DEFAULT_ACTIVATION_OFFSET_POSITION_DIP);
        mActivationOffsetHeightDip = prefs.getInt(KEY_ACTIVATION_OFFSET_HEIGHT_DIP, DEFAULT_ACTIVATION_OFFSET_HEIGHT_DIP);
        mShowBackground = prefs.getBoolean(KEY_SHOW_BACKGROUND, DEFAULT_SHOW_BACKGROUND);
        mVibrateOnActivation = prefs.getBoolean(KEY_VIBRATE_ON_ACTIVATION, DEFAULT_VIBRATE_ON_ACTIVATION);
        mIsOnRightSide = prefs.getBoolean(KEY_IS_ON_RIGHT_SIDE, DEFAULT_IS_ON_RIGHT_SIDE);
        mLauncherGravity = LauncherGravity.fromValue(prefs.getInt(KEY_LAUNCHER_GRAVITY, DEFAULT_LAUNCHER_GRAVITY.getValue()));
    }

    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_SENSITIVITY_DIP, mSensitivityDip)
                .putInt(KEY_ACTIVATION_OFFSET_POSITION_DIP, mActivationOffsetPositionDip)
                .putInt(KEY_ACTIVATION_OFFSET_HEIGHT_DIP, mActivationOffsetHeightDip)
                .putBoolean(KEY_SHOW_BACKGROUND, mShowBackground)
                .putBoolean(KEY_VIBRATE_ON_ACTIVATION, mVibrateOnActivation)
                .putBoolean(KEY_IS_ON_RIGHT_SIDE, mIsOnRightSide)
                .putInt(KEY_LAUNCHER_GRAVITY, mLauncherGravity.getValue())
                .apply();
    }

    public int getSensitivityDip() {
        return mSensitivityDip;
    }

    public void setSensitivityDip(int sensitivityDip) {
        mSensitivityDip = sensitivityDip;
    }

    public int getActivationOffsetPositionDip() {
        return mActivationOffsetPositionDip;
    }

    public void setActivationOffsetPositionDip(int activationOffsetPositionDip) {
        this.mActivationOffsetPositionDip = activationOffsetPositionDip;
    }

    public int getActivationOffsetHeightDip() {
        return mActivationOffsetHeightDip;
    }

    public void setActivationOffsetHeightDip(int activationOffsetHeightDip) {
        this.mActivationOffsetHeightDip = activationOffsetHeightDip;
    }

    public boolean isShowBackground() {
        return mShowBackground;
    }

    public void setShowBackground(boolean showBackground) {
        this.mShowBackground = showBackground;
    }

    public boolean isOnRightSide() {
        return mIsOnRightSide;
    }

    public void setIsOnRightSide(boolean isOnRightSide) {
        mIsOnRightSide = isOnRightSide;
    }

    public boolean isVibrateOnActivation() {
        return mVibrateOnActivation;
    }

    public void setVibrateOnActivation(boolean vibrateOnActivation) {
        mVibrateOnActivation = vibrateOnActivation;
    }

    public LauncherGravity getLauncherGravity() {
        return mLauncherGravity;
    }

    public void setLauncherGravity(LauncherGravity launcherGravity) {
        mLauncherGravity = launcherGravity;
    }
}

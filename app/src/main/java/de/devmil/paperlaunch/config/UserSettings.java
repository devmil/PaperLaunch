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

    private static final int DEFAULT_SENSITIVITY_DIP = 15;
    private static final int DEFAULT_ACTIVATION_OFFSET_POSITION_DIP = 0;
    private static final int DEFAULT_ACTIVATION_OFFSET_HEIGHT_DIP = 0;
    private static final boolean DEFAULT_SHOW_BACKGROUND = false;


    private int mSensitivityDip;
    private int mActivationOffsetPositionDip;
    private int mActivationOffsetHeightDip;
    private boolean mShowBackground;

    public UserSettings(Context context) {
        load(context);
    }

    public void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        mSensitivityDip = prefs.getInt(KEY_SENSITIVITY_DIP, DEFAULT_SENSITIVITY_DIP);
        mActivationOffsetPositionDip = prefs.getInt(KEY_ACTIVATION_OFFSET_POSITION_DIP, DEFAULT_ACTIVATION_OFFSET_POSITION_DIP);
        mActivationOffsetHeightDip = prefs.getInt(KEY_ACTIVATION_OFFSET_HEIGHT_DIP, DEFAULT_ACTIVATION_OFFSET_HEIGHT_DIP);
        mShowBackground = prefs.getBoolean(KEY_SHOW_BACKGROUND, DEFAULT_SHOW_BACKGROUND);
    }

    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_SENSITIVITY_DIP, mSensitivityDip)
                .putInt(KEY_ACTIVATION_OFFSET_POSITION_DIP, mActivationOffsetPositionDip)
                .putInt(KEY_ACTIVATION_OFFSET_HEIGHT_DIP, mActivationOffsetHeightDip)
                .putBoolean(KEY_SHOW_BACKGROUND, mShowBackground)
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
        //TODO: make this a setting
        return true;
    }
}

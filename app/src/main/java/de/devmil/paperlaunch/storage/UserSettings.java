package de.devmil.paperlaunch.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSettings {

    private static final String SHARED_PREFS_NAME = "paperLaunch";

    private static final String KEY_IS_ACTIVE = "isActive";
    private static final String KEY_SENSITIVITY_DIP = "sensitivityDip";

    private static final boolean DEFAULT_IS_ACTIVE = true;
    private static final int DEFAULT_SENSITIVITY_DIP = 15;

    private boolean mIsActive;
    private int mSensitivityDip;

    public UserSettings(Context context) {
        load(context);
    }

    public void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        mIsActive = prefs.getBoolean(KEY_IS_ACTIVE, DEFAULT_IS_ACTIVE);
        mSensitivityDip = prefs.getInt(KEY_SENSITIVITY_DIP, DEFAULT_SENSITIVITY_DIP);
    }

    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_ACTIVE, mIsActive)
                .putInt(KEY_SENSITIVITY_DIP, mSensitivityDip)
                .apply();
    }

    public boolean getIsActive() {
        return mIsActive;
    }

    public void setIsActive(boolean isActive) {
        mIsActive = isActive;
    }

    public int getSensitivityDip() {
        return mSensitivityDip;
    }

    public void setSensitivityDip(int sensitivityDip) {
        mSensitivityDip = sensitivityDip;
    }
}

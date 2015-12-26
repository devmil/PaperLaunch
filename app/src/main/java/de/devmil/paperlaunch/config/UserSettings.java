package de.devmil.paperlaunch.config;

import android.content.Context;
import android.content.SharedPreferences;

import de.devmil.paperlaunch.config.IUserSettings;

public class UserSettings implements IUserSettings {

    private static final String SHARED_PREFS_NAME = "paperLaunch";

    private static final String KEY_SENSITIVITY_DIP = "sensitivityDip";

    private static final int DEFAULT_SENSITIVITY_DIP = 15;

    private int mSensitivityDip;

    public UserSettings(Context context) {
        load(context);
    }

    public void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        mSensitivityDip = prefs.getInt(KEY_SENSITIVITY_DIP, DEFAULT_SENSITIVITY_DIP);
    }

    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putInt(KEY_SENSITIVITY_DIP, mSensitivityDip)
                .apply();
    }

    public int getSensitivityDip() {
        return mSensitivityDip;
    }

    public void setSensitivityDip(int sensitivityDip) {
        mSensitivityDip = sensitivityDip;
    }
}

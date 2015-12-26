package de.devmil.paperlaunch.service;

import android.content.Context;
import android.content.SharedPreferences;

public class ServiceState {
    private static final String SERVICE_PREFS_NAME = "launcherOverlayService";

    private static final String KEY_IS_ACTIVE = "isActive";

    private static final boolean DEFAULT_IS_ACTIVE = true;

    private boolean mIsActive;

    public ServiceState(Context context) {
        load(context);
    }

    public void load(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SERVICE_PREFS_NAME, Context.MODE_PRIVATE);
        mIsActive = prefs.getBoolean(KEY_IS_ACTIVE, DEFAULT_IS_ACTIVE);
    }

    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SERVICE_PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putBoolean(KEY_IS_ACTIVE, mIsActive)
                .apply();
    }

    public boolean getIsActive() {
        return mIsActive;
    }

    public void setIsActive(boolean isActive) {
        mIsActive = isActive;
    }
}

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

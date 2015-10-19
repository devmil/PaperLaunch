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
package de.devmil.paperlaunch.model;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public class LaunchEntry {
    private long mId;
    private int mOrderIndex;

    private Intent mLaunchIntent;
    private String mAppName;
    private Drawable mAppIcon;
    private boolean mIsFolder;
    private long mFolderId;

    public LaunchEntry(long id, int orderIndex, Intent launchIntent, String appName, Drawable appIcon, boolean isFolder, long folderId)
    {
        mId = id;
        mOrderIndex = orderIndex;
        mLaunchIntent = launchIntent;
        mAppName = appName;
        mAppIcon = appIcon;
        mIsFolder = isFolder;
        mFolderId = folderId;
    }

    public LaunchEntry(long id, int orderIndex)
    {
        this(id, orderIndex, null, "", null, false, -1);
    }

    public long getId() {
        return mId;
    }

    public int getOrderIndex() {
        return mOrderIndex;
    }

    public Intent getLaunchIntent()
    {
        return mLaunchIntent;
    }

    public String getAppName()
    {
        return mAppName;
    }

    public Drawable getAppIcon()
    {
        return mAppIcon;
    }

    public boolean isFolder() {
        return mIsFolder;
    }

    public long getFolderId() {
        return mFolderId;
    }

    public void setApp(Context context, IDesignConfig designConfig, ComponentName component) {
        PackageManager pm = context.getPackageManager();
        String appName = "";
        Drawable appIcon = null;
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(component.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            appInfo = null;
        }
        if(appInfo == null) {
            appIcon = context.getDrawable(designConfig.getUnknownAppImageId());
            appName = "?";
        } else {
            appIcon = pm.getApplicationIcon(appInfo);
            appName = appInfo.name;
        }

        Intent launchIntent = new Intent();
        launchIntent.setComponent(component);

        mLaunchIntent = launchIntent;
        mAppName = appName;
        mAppIcon = appIcon;
        mIsFolder = false;
    }
}

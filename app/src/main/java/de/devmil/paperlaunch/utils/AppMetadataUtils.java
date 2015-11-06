package de.devmil.paperlaunch.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public abstract class AppMetadataUtils {

    public static String getAppName(Context context, ComponentName componentName)
    {
        PackageManager pm = context.getPackageManager();

        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(componentName.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            appInfo = null;
        }
        if(appInfo == null) {
            return null;
        } else {
            return appInfo.name;
        }
    }

    public static Drawable getAppIcon(Context context, ComponentName componentName)
    {
        PackageManager pm = context.getPackageManager();

        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(componentName.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            appInfo = null;
        }
        if(appInfo == null) {
            return null;
        } else {
            return pm.getApplicationIcon(appInfo);
        }
    }
}

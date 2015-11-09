package de.devmil.paperlaunch.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

public abstract class AppMetadataUtils {

    public static String getAppName(Context context, ComponentName componentName)
    {
        PackageManager pm = context.getPackageManager();

        ApplicationInfo appInfo = null;
        ActivityInfo activityInfo = null;
        try {
            appInfo = pm.getApplicationInfo(componentName.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            appInfo = null;
        }
        try {
            activityInfo = pm.getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if(appInfo == null) {
            return null;
        } else {
            StringBuilder result = new StringBuilder();

            CharSequence appName = pm.getApplicationLabel(appInfo);
            if(appName != null) {
                result.append(appName);
            }

            if(activityInfo != null) {
                CharSequence activityName = activityInfo.loadLabel(pm);
                if(activityName != null && !activityName.equals(appName)) {
                    result.append(" - ").append(activityName);
                }
            }

            return result.toString();
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

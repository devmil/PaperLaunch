package de.devmil.paperlaunch.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;

import de.devmil.paperlaunch.R;

public abstract class AppMetadataUtils {

    public static String getAppName(Context context, Intent appIntent)
    {

        if(appIntent.hasExtra(Intent.EXTRA_SHORTCUT_NAME)) {
            return appIntent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        }

        if(appIntent.hasExtra(Intent.EXTRA_SHORTCUT_INTENT)) {
            appIntent = appIntent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        }
        ComponentName componentName = appIntent.getComponent();

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
            CharSequence appName = pm.getApplicationLabel(appInfo);
            CharSequence activityName = null;

            if(activityInfo != null) {
                activityName = activityInfo.loadLabel(pm);
            }

            if(activityName != null) {
                return activityName.toString();
            }

            if(appName != null) {
                appName.toString();
            }

            return null;
        }
    }

    public static Drawable getAppIcon(Context context, Intent launchIntent)
    {
        Drawable result = null;
        boolean isShortcut = false;

        if(launchIntent.hasExtra(Intent.EXTRA_SHORTCUT_INTENT)) {
            isShortcut = true;
        }


        if(launchIntent.hasExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)) {
            result = getShortcutIcon(context, launchIntent);
        }

        if(result != null) {
            return result;
        }

        if(launchIntent.hasExtra(Intent.EXTRA_SHORTCUT_INTENT)) {
            launchIntent = launchIntent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        }

        PackageManager pm = context.getPackageManager();

        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(launchIntent.getComponent().getPackageName(), 0);
        } catch (Exception e) {
            appInfo = null;
        }
        if(appInfo == null) {
            if(!isShortcut) {
                return context.getResources().getDrawable(R.mipmap.ic_missing_app_red, context.getTheme());
            }
            return context.getResources().getDrawable(R.mipmap.ic_link_black_48dp, context.getTheme());
        } else {
            return pm.getApplicationIcon(appInfo);
        }
    }

    private static Drawable getShortcutIcon(Context context, Intent shortcutIntent) {
        if(!shortcutIntent.hasExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)) {
            return null;
        }
        try {
            Intent.ShortcutIconResource iconRes = shortcutIntent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE);
            Resources appRes = context.getPackageManager().getResourcesForApplication(iconRes.packageName);
            int resId = appRes.getIdentifier(iconRes.resourceName, null, null);
            return appRes.getDrawable(resId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}

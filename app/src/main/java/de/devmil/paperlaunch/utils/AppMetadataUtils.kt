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
package de.devmil.paperlaunch.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import de.devmil.paperlaunch.R

object AppMetadataUtils {

    fun getAppName(context: Context, intent: Intent): String? {
        var appIntent = intent

        if (appIntent.hasExtra(Intent.EXTRA_SHORTCUT_NAME)) {
            return appIntent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME)
        }

        if (appIntent.hasExtra(Intent.EXTRA_SHORTCUT_INTENT)) {
            appIntent = appIntent.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
        }
        val componentName = appIntent.component

        val pm = context.packageManager

        var appInfo: ApplicationInfo?
        var activityInfo: ActivityInfo? = null
        try {
            appInfo = pm.getApplicationInfo(componentName.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            appInfo = null
        }

        try {
            activityInfo = pm.getActivityInfo(componentName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        if (appInfo == null) {
            return null
        } else {
            val appName = pm.getApplicationLabel(appInfo)
            var activityName: CharSequence? = null

            if (activityInfo != null) {
                activityName = activityInfo.loadLabel(pm)
            }

            if (activityName != null) {
                return activityName.toString()
            }

            if (appName != null) {
                return appName.toString()
            }

            return null
        }
    }

    fun getAppIcon(context: Context, intent: Intent): Drawable {
        var launchIntent = intent
        var result: Drawable? = null
        var isShortcut = false

        if (launchIntent.hasExtra(Intent.EXTRA_SHORTCUT_INTENT)) {
            isShortcut = true
        }


        if (launchIntent.hasExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)) {
            result = getShortcutIcon(context, launchIntent)
        }

        if (result != null) {
            return result
        }

        if (launchIntent.hasExtra(Intent.EXTRA_SHORTCUT_INTENT)) {
            launchIntent = launchIntent.getParcelableExtra<Intent>(Intent.EXTRA_SHORTCUT_INTENT)
        }

        val pm = context.packageManager

        var appInfo: ApplicationInfo?
        try {
            appInfo = pm.getApplicationInfo(launchIntent.component.packageName, 0)
        } catch (e: Exception) {
            appInfo = null
        }

        if (appInfo == null) {
            if (!isShortcut) {
                return context.resources.getDrawable(R.mipmap.ic_missing_app_red, context.theme)
            }
            return context.resources.getDrawable(R.mipmap.ic_link_black_48dp, context.theme)
        } else {
            return pm.getApplicationIcon(appInfo)
        }
    }

    private fun getShortcutIcon(context: Context, shortcutIntent: Intent): Drawable? {
        if (!shortcutIntent.hasExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)) {
            return null
        }
        try {
            val iconRes = shortcutIntent.getParcelableExtra<Intent.ShortcutIconResource>(Intent.EXTRA_SHORTCUT_ICON_RESOURCE)
            val appRes = context.packageManager.getResourcesForApplication(iconRes.packageName)
            val resId = appRes.getIdentifier(iconRes.resourceName, null, null)
            return ContextCompat.getDrawable(context, resId)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }
}

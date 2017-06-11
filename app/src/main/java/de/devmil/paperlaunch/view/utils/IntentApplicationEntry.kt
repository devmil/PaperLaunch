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
package de.devmil.paperlaunch.view.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

import java.util.ArrayList
import java.util.Arrays
import java.util.Comparator

class IntentApplicationEntry @Throws(NameNotFoundException::class)
constructor(private val context: Context, val packageName: String) : Comparable<IntentApplicationEntry> {

    enum class IntentType {
        Main,
        Shortcut,
        Launcher
    }

    private val appInfo: ApplicationInfo = context.packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    val name: CharSequence
    private var appIcon: Drawable? = null

    private val intentItems = ArrayList<IntentItem>()

    private var mainActivityIntentItems: MutableList<IntentItem>? = null
    private var shortcutIntentItems: MutableList<IntentItem>? = null

    init {

        name = context.packageManager.getApplicationLabel(appInfo)
    }

    fun addResolveInfo(info: ResolveInfo, intentType: IntentType) {
        var newItem = IntentItem(info)
        if (intentItems.contains(newItem))
            newItem = intentItems[intentItems.indexOf(newItem)]
        else
            intentItems.add(newItem)
        when (intentType) {
            IntentApplicationEntry.IntentType.Main -> newItem.isMainActivity = true
            IntentApplicationEntry.IntentType.Shortcut -> newItem.isShortcut = true
            IntentApplicationEntry.IntentType.Launcher -> newItem.setIsLauncher(true)
        }
        makeDirty()
    }

    fun getAppIcon(): Drawable? {
        if (appIcon == null)
            appIcon = context.packageManager.getApplicationIcon(appInfo)
        return appIcon
    }

    override fun compareTo(other: IntentApplicationEntry): Int {
        if (other === this)
            return 0
        return name.toString().compareTo(other.name.toString(), ignoreCase = true)
    }

    private fun makeDirty() {
        mainActivityIntentItems = null
        shortcutIntentItems = null
    }

    private fun ensureSubCollections() {
        if (mainActivityIntentItems != null)
            return
        mainActivityIntentItems = ArrayList<IntentItem>()
        shortcutIntentItems = ArrayList<IntentItem>()
        for (ii in intentItems) {
            if (ii.isMainActivity)
                mainActivityIntentItems!!.add(ii)
            if (ii.isShortcut)
                shortcutIntentItems!!.add(ii)
        }
    }

    fun sort() {
        ensureSubCollections()
        val intentItemComparator = Comparator<IntentItem> { object1, object2 -> object1.compareTo(object2) }

        mainActivityIntentItems!!.sortWith(intentItemComparator)
        shortcutIntentItems!!.sortWith(intentItemComparator)
    }

    fun getMainActivityIntentItems(): List<IntentItem> {
        ensureSubCollections()
        return mainActivityIntentItems.orEmpty().toList()
    }

    fun getShortcutIntentItems(): List<IntentItem> {
        ensureSubCollections()
        return shortcutIntentItems.orEmpty().toList()
    }

    override fun equals(other: Any?): Boolean {
        if (other == null)
            return false
        if (other !is IntentApplicationEntry)
            return false
        return compareTo((other as IntentApplicationEntry?)!!) == 0
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

    inner class IntentItem(resolveInfo: ResolveInfo) : Comparable<IntentItem> {
        val packageName: String = resolveInfo.activityInfo.packageName
        val activityName: String = resolveInfo.activityInfo.name
        private val name: CharSequence = resolveInfo.activityInfo.loadLabel(context.packageManager)
        var isShortcut = false
        var isMainActivity = false
        var isLauncherActivity = false
            internal set

        fun setIsLauncher(isLauncher: Boolean) {
            this.isLauncherActivity = isLauncher
            if (isLauncher)
                this.isMainActivity = true
        }

        override fun equals(other: Any?): Boolean {
            if (other == null)
                return false
            if (other is IntentItem)
                return false
            val castedOther = other as IntentItem?
            return castedOther!!.packageName == packageName && castedOther.activityName == activityName
        }

        override fun hashCode(): Int {
            var result = super.hashCode()

            result = result or packageName.hashCode()
            result = result or activityName.hashCode()
            result = result or name.hashCode()
            result = result or isShortcut.hashCode()
            result = result or isMainActivity.hashCode()
            result = result or isLauncherActivity.hashCode()

            return result
        }

        override fun compareTo(other: IntentItem): Int {
            if (other === this)
                return 0
            return name.toString().compareTo(other.name.toString())
        }

        val displayName: String
            get() = name.toString() + if (isLauncherActivity) " (Launcher)" else ""
    }
}

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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.content.pm.ResolveInfo
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import de.devmil.paperlaunch.R
import java.lang.ref.WeakReference
import java.util.*

class IntentSelector : Activity() {

    private var llWait: LinearLayout? = null
    private var lvActivities: ExpandableListView? = null
    private var chkShowAllActivities: CheckBox? = null
    private var lvShortcuts: ExpandableListView? = null
    private var txtShortcuts: TextView? = null

    private var mToolbar: Toolbar? = null

    class SearchTask
    constructor(intentSelector : IntentSelector) : AsyncTask<Unit, Int, Unit>() {

        private val entries = mutableListOf<IntentApplicationEntry>()

        var isAnotherSearchRunning: Boolean = false
        var isObsolete: Boolean = false

        override fun doInBackground(vararg params: Unit?) {
            //this approach can kill the PackageManager if there are too many apps installed
            //				List<ResolveInfo> shortcutResolved = getPackageManager().queryIntentActivities(shortcutIntent, PackageManager.GET_ACTIVITIES | PackageManager.GET_INTENT_FILTERS);
            //				List<ResolveInfo> mainResolved = getPackageManager().queryIntentActivities(mainIntent, PackageManager.GET_ACTIVITIES | PackageManager.GET_INTENT_FILTERS);
            //				List<ResolveInfo> launcherResolved = getPackageManager().queryIntentActivities(launcherIntent, PackageManager.GET_ACTIVITIES | PackageManager.GET_INTENT_FILTERS);

            val pm = intentSelector.get()!!.packageManager

            val shortcutResolved = ArrayList<ResolveInfo>()
            val mainResolved = ArrayList<ResolveInfo>()
            val launcherResolved = ArrayList<ResolveInfo>()

            val appInfos = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            val showAll = intentSelector.get()!!.chkShowAllActivities!!.isChecked

            for (appInfo in appInfos) {
                if(isCancelled || isObsolete) {
                    return
                }
                val shortcutIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)
                shortcutIntent.`package` = appInfo.packageName

                val appShortcutResolved = pm.queryIntentActivities(shortcutIntent, PackageManager.GET_META_DATA)
                shortcutResolved.addAll(appShortcutResolved)

                var addMainActivities = true

                if (showAll) {
                    try {
                        val pi = pm.getPackageInfo(appInfo.packageName, PackageManager.GET_ACTIVITIES or PackageManager.GET_INTENT_FILTERS)

                        for (ai in pi.activities) {
                            val ri = ResolveInfo()
                            ri.activityInfo = ai

                            mainResolved.add(ri)
                        }

                        addMainActivities = false
                    } catch (e: Exception) {
                    }

                }

                if (addMainActivities) {
                    val mainIntent = Intent(Intent.ACTION_MAIN)
                    mainIntent.`package` = appInfo.packageName

                    val appMainResolved = pm.queryIntentActivities(mainIntent, PackageManager.GET_META_DATA)
                    mainResolved.addAll(appMainResolved)
                }

                val launcherIntent = Intent(Intent.ACTION_MAIN)
                launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                launcherIntent.`package` = appInfo.packageName

                val appLauncherResolved = pm.queryIntentActivities(launcherIntent, PackageManager.GET_META_DATA)
                launcherResolved.addAll(appLauncherResolved)
            }

            for (ri in shortcutResolved) {
                if(isCancelled || isObsolete) {
                    return
                }
                addResolveInfo(ri, IntentApplicationEntry.IntentType.Shortcut, false, entries)
            }
            for (ri in mainResolved) {
                if(isCancelled || isObsolete) {
                    return
                }
                addResolveInfo(ri, IntentApplicationEntry.IntentType.Main, showAll, entries)
            }
            for (ri in launcherResolved) {
                if(isCancelled || isObsolete) {
                    return
                }
                addResolveInfo(ri, IntentApplicationEntry.IntentType.Launcher, false, entries)
            }
            //sort
            val comparator = Comparator<IntentApplicationEntry> { object1, object2 -> object1.compareTo(object2) }
            entries.sortWith(comparator)
            entries.forEach { if(!isCancelled && !isObsolete) it.sort() }
        }

        override fun onPreExecute() {
            super.onPreExecute()
            val localIntentSelector = intentSelector.get()
            localIntentSelector?.runOnUiThread { localIntentSelector.llWait!!.visibility = View.VISIBLE }
        }

        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            val localIntentSelector = intentSelector.get()
            localIntentSelector?.runOnUiThread {
                if(!isCancelled && !isObsolete) {
                    localIntentSelector.adapterActivities = IntentSelectorAdapter(localIntentSelector, entries, IntentApplicationEntry.IntentType.Main)
                    localIntentSelector.adapterShortcuts = IntentSelectorAdapter(localIntentSelector, entries, IntentApplicationEntry.IntentType.Shortcut)

                    localIntentSelector.lvActivities!!.setAdapter(localIntentSelector.adapterActivities)
                    localIntentSelector.lvShortcuts!!.setAdapter(localIntentSelector.adapterShortcuts)
                }
                if(!isAnotherSearchRunning) {
                    localIntentSelector.llWait!!.visibility = View.GONE
                }
            }
        }

        override fun onCancelled() {
            super.onCancelled()
            val localIntentSelector = intentSelector.get()
            localIntentSelector?.runOnUiThread {
                if(!isAnotherSearchRunning) {
                    localIntentSelector.llWait!!.visibility = View.GONE
                }
            }
        }

        private fun addResolveInfo(ri: ResolveInfo, intentType: IntentApplicationEntry.IntentType, addAll: Boolean, entries : MutableList<IntentApplicationEntry>) {
            val localIntentSelector = intentSelector.get()
            if(localIntentSelector == null) {
                return
            }
            try {
                if (!addAll && !ri.activityInfo.exported)
                    return
                var newEntry = IntentApplicationEntry(localIntentSelector, ri.activityInfo.packageName)
                if (!entries.contains(newEntry)) {
                    entries.add(newEntry)
                } else {
                    newEntry = entries[entries.indexOf(newEntry)]
                }
                newEntry.addResolveInfo(ri, intentType)
            } catch (e: NameNotFoundException) {
                Log.e(TAG, "Error while adding a package", e)
            }

        }

        private var intentSelector : WeakReference<IntentSelector> = WeakReference<IntentSelector>(intentSelector)
    }

    private var mSearchTask : SearchTask? = null

    internal var adapterActivities: IntentSelectorAdapter? = null
    internal var adapterShortcuts: IntentSelectorAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(mSearchTask != null) {
            mSearchTask!!.cancel(true)
            mSearchTask = null
        }

        var shortcutText = ""
        if (intent.hasExtra(EXTRA_SHORTCUT_TEXT))
            shortcutText = intent.getStringExtra(EXTRA_SHORTCUT_TEXT)

        var shortcutLabel = "Shortcuts"
        if (intent.hasExtra(EXTRA_STRING_SHORTCUTS))
            shortcutLabel = intent.getStringExtra(EXTRA_STRING_SHORTCUTS)
        var activitiesLabel = "Shortcuts"
        if (intent.hasExtra(EXTRA_STRING_ACTIVITIES))
            activitiesLabel = intent.getStringExtra(EXTRA_STRING_ACTIVITIES)

        setContentView(R.layout.common__intentselectorview)

        llWait = findViewById(R.id.common__intentSelector_llWait) as LinearLayout
        //		progressWait = (ProgressBar)findViewById(R.id.intentSelector_progressWait);
        lvActivities = findViewById(R.id.common__intentSelector_lvActivities) as ExpandableListView
        chkShowAllActivities = findViewById(R.id.common__intentSelector_chkShowAllActivities) as CheckBox
        lvShortcuts = findViewById(R.id.common__intentSelector_lvShortcuts) as ExpandableListView
        txtShortcuts = findViewById(R.id.common__intentSelector_txtShortcuts) as TextView
        mToolbar = findViewById(R.id.common__intentSelector_toolbar) as Toolbar

        setActionBar(mToolbar)

        txtShortcuts!!.text = shortcutText

        lvActivities!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val resultIntent = Intent(Intent.ACTION_MAIN)
            val entry = adapterActivities!!.getChild(groupPosition, childPosition) as IntentApplicationEntry.IntentItem
            resultIntent.setClassName(entry.packageName, entry.activityName)
            setResultIntent(resultIntent)
            true
        }
        chkShowAllActivities!!.setOnCheckedChangeListener { _, _ -> startSearch() }
        lvShortcuts!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val shortcutIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)
            val entry = adapterShortcuts!!.getChild(groupPosition, childPosition) as IntentApplicationEntry.IntentItem
            shortcutIntent.setClassName(entry.packageName, entry.activityName)
            startActivityForResult(shortcutIntent, CREATE_SHORTCUT_REQUEST)
            false
        }

        val tabs = this.findViewById(android.R.id.tabhost) as TabHost
        tabs.setup()
        val tspecActivities = tabs.newTabSpec(activitiesLabel)
        tspecActivities.setIndicator(activitiesLabel)
        tspecActivities.setContent(R.id.common__intentSelector_tabActivities)
        tabs.addTab(tspecActivities)
        val tspecShortcuts = tabs.newTabSpec(shortcutLabel)
        tspecShortcuts.setIndicator(shortcutLabel)
        if (shortcutText == "") {
            tspecShortcuts.setContent(R.id.common__intentSelector_tabShortcuts)
            txtShortcuts!!.visibility = View.GONE
        } else {
            tspecShortcuts.setContent(R.id.common__intentSelector_tabTextShortcuts)
            lvShortcuts!!.visibility = View.GONE
        }
        tabs.addTab(tspecShortcuts)

        llWait!!.visibility = View.VISIBLE

        startSearch()
    }

    private fun startSearch() {
        if (mSearchTask != null) {
            mSearchTask!!.isAnotherSearchRunning = true;
            mSearchTask!!.isObsolete = true;
            mSearchTask!!.cancel(true)
            mSearchTask = null
        }

        mSearchTask = SearchTask(this)
        mSearchTask!!.execute()
    }

    private fun setResultIntent(intent: Intent) {
        setResult(Activity.RESULT_OK, intent)
        this@IntentSelector.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == CREATE_SHORTCUT_REQUEST && resultCode == Activity.RESULT_OK) {
            setResultIntent(data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    internal class IntentSelectorAdapter(private val context: Context, entriesList: List<IntentApplicationEntry>, private val intentType: IntentApplicationEntry.IntentType) : BaseExpandableListAdapter() {
        private val entries: MutableList<IntentApplicationEntry>

        init {
            this.entries = entriesList
                    .filter { getSubList(it).isNotEmpty() }
                    .toMutableList()
        }

        fun getSubList(entry: IntentApplicationEntry): List<IntentApplicationEntry.IntentItem> {
            return IntentSelector.getSubList(entry, intentType)
        }

        override fun getChild(groupPosition: Int, childPosition: Int): Any {
            return getSubList(entries[groupPosition])[childPosition]
        }

        override fun getChildId(groupPosition: Int, childPosition: Int): Long {
            return (groupPosition * 1000 + childPosition).toLong()
        }

        override fun getChildView(groupPosition: Int, childPosition: Int,
                                  isLastChild: Boolean, convertView: View?, parent: ViewGroup): View {
            var effectiveConvertView = convertView
            if (effectiveConvertView == null) {
                effectiveConvertView = LayoutInflater.from(context).inflate(R.layout.common__intentselectoritem, parent, false)
            }
            val txt = effectiveConvertView!!.findViewById(R.id.common__intentselectoritem_text) as TextView
            val txtActivityName = effectiveConvertView.findViewById(R.id.common__intentselectoritem_activityName) as TextView

            txt.text = getSubList(entries[groupPosition])[childPosition].displayName
            txtActivityName.text = getSubList(entries[groupPosition])[childPosition].activityName
            return effectiveConvertView
        }

        override fun getChildrenCount(groupPosition: Int): Int {
            return getSubList(entries[groupPosition]).size
        }

        override fun getGroup(groupPosition: Int): Any {
            return entries[groupPosition]
        }

        override fun getGroupCount(): Int {
            return entries.size
        }

        override fun getGroupId(groupPosition: Int): Long {
            return (groupPosition * 1000).toLong()
        }

        override fun getGroupView(groupPosition: Int, isExpanded: Boolean,
                                  convertView: View?, parent: ViewGroup): View {
            var effectiveConvertView = convertView
            if (effectiveConvertView == null) {
                effectiveConvertView = LayoutInflater.from(context).inflate(R.layout.common__intentselectorgroup, parent, false)
            }
            val img = effectiveConvertView!!.findViewById(R.id.common__intentselectorgroup_img) as ImageView
            val txt = effectiveConvertView.findViewById(R.id.common__intentselectorgroup_text) as TextView

            txt.text = entries[groupPosition].name
            val appIcon = entries[groupPosition].getAppIcon()
            if (appIcon != null) {
                img.setImageDrawable(entries[groupPosition].getAppIcon())
                img.visibility = View.VISIBLE
            } else {
                img.visibility = View.INVISIBLE
            }
            return effectiveConvertView
        }

        override fun hasStableIds(): Boolean {
            return false
        }

        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean {
            return true
        }
    }

    companion object {
        private val TAG = IntentSelector::class.java.simpleName
        private val CREATE_SHORTCUT_REQUEST = 1

        var EXTRA_SHORTCUT_TEXT = "de.devmil.common.extras.SHORTCUT_TEXT"
        var EXTRA_STRING_SHORTCUTS = "de.devmil.common.extras.STRING_SHORTCUTS"
        var EXTRA_STRING_ACTIVITIES = "de.devmil.common.extras.STRING_ACTIVITIES"

        private fun getSubList(entry: IntentApplicationEntry, intentType: IntentApplicationEntry.IntentType): List<IntentApplicationEntry.IntentItem> {
            when (intentType) {
                IntentApplicationEntry.IntentType.Main, IntentApplicationEntry.IntentType.Launcher -> return entry.getMainActivityIntentItems()
                IntentApplicationEntry.IntentType.Shortcut -> return entry.getShortcutIntentItems()
            }
        }
    }
}

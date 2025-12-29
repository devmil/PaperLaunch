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
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MenuItem
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

class IntentSelector : AppCompatActivity(), SearchView.OnQueryTextListener {

    private var llWait: LinearLayout? = null
    private var lvActivities: ExpandableListView? = null
    private var chkShowAllActivities: CheckBox? = null
    private var lvShortcuts: ExpandableListView? = null
    private var txtShortcuts: TextView? = null

    private var toolbar: Toolbar? = null

    class SearchTask
    constructor(intentSelector : IntentSelector) : AsyncTask<Unit, Int, Unit>() {

        private val entries = mutableListOf<IntentApplicationEntry>()

        var isAnotherSearchRunning: Boolean = false
        var isObsolete: Boolean = false

        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Unit?) {
            intentSelectorRef.get()?.let {
                val pm = it.packageManager

                val shortcutResolved = ArrayList<ResolveInfo>()
                val mainResolved = ArrayList<ResolveInfo>()
                val launcherResolved = ArrayList<ResolveInfo>()

                val showAll = it.chkShowAllActivities!!.isChecked

                // Instead of getInstalledApplications, we query intents directly for better performance

                // 1. Launcher Activities (Always needed)
                val launcherIntent = Intent(Intent.ACTION_MAIN)
                launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER)
                try {
                    launcherResolved.addAll(pm.queryIntentActivities(launcherIntent, 0))
                } catch (e: Exception) {
                    Log.e(TAG, "Error querying launcher activities", e)
                }

                if (isCancelled || isObsolete) return

                // 2. Shortcuts (Always needed)
                val shortcutIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)
                try {
                    shortcutResolved.addAll(pm.queryIntentActivities(shortcutIntent, 0))
                } catch (e: Exception) {
                    Log.e(TAG, "Error querying shortcut activities", e)
                }

                if (isCancelled || isObsolete) return

                // 3. Main Activities (Only if showAll is true)
                // Note: Querying ACTION_MAIN without category can be huge, but if user requests it...
                // We can optimize by iterating launcherResolved/shortcutResolved to find packages?
                // Or just query only if checked.
                if (showAll) {
                    val mainIntent = Intent(Intent.ACTION_MAIN)
                    try {
                        mainResolved.addAll(pm.queryIntentActivities(mainIntent, 0))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error querying main activities", e)
                    }
                }

                // Process Launcher Activities
                for (ri in launcherResolved) {
                    if (isCancelled || isObsolete) return
                    addResolveInfo(ri, IntentApplicationEntry.IntentType.Launcher, false, entries)
                }

                // Process Shortcut Activities
                for (ri in shortcutResolved) {
                     if (isCancelled || isObsolete) return
                     addResolveInfo(ri, IntentApplicationEntry.IntentType.Shortcut, false, entries)
                }

                // Process Main Activities
                if (showAll) {
                    for (ri in mainResolved) {
                        if (isCancelled || isObsolete) return
                        addResolveInfo(ri, IntentApplicationEntry.IntentType.Main, true, entries)
                    }
                }

                //sort
                val comparator = Comparator<IntentApplicationEntry> { object1, object2 -> object1.compareTo(object2) }
                entries.sortWith(comparator)
                entries.forEach { if(!isCancelled && !isObsolete) it.sort() }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onPreExecute() {
            super.onPreExecute()
            val localIntentSelector = intentSelectorRef.get()
            localIntentSelector?.runOnUiThread { localIntentSelector.llWait!!.visibility = View.VISIBLE }
        }

        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: Unit?) {
            super.onPostExecute(result)
            val localIntentSelector = intentSelectorRef.get()
            localIntentSelector?.runOnUiThread {
                if(!isCancelled && !isObsolete) {
                    localIntentSelector.adapterActivities = IntentSelectorAdapter(localIntentSelector, entries, IntentApplicationEntry.IntentType.Main)
                    localIntentSelector.adapterShortcuts = IntentSelectorAdapter(localIntentSelector, entries, IntentApplicationEntry.IntentType.Shortcut)

                    localIntentSelector.lvActivities!!.setAdapter(localIntentSelector.adapterActivities)
                    localIntentSelector.lvShortcuts!!.setAdapter(localIntentSelector.adapterShortcuts)

                    localIntentSelector.adapterActivities?.filter(localIntentSelector.mCurrentQuery)
                    localIntentSelector.adapterShortcuts?.filter(localIntentSelector.mCurrentQuery)
                }
                if(!isAnotherSearchRunning) {
                    localIntentSelector.llWait!!.visibility = View.GONE
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCancelled() {
            super.onCancelled()
            val localIntentSelector = intentSelectorRef.get()
            localIntentSelector?.runOnUiThread {
                if(!isAnotherSearchRunning) {
                    localIntentSelector.llWait!!.visibility = View.GONE
                }
            }
        }

        private fun addResolveInfo(ri: ResolveInfo, intentType: IntentApplicationEntry.IntentType, addAll: Boolean, entries : MutableList<IntentApplicationEntry>) {
            intentSelectorRef.get()?.let {
                try {
                    if (!addAll && !ri.activityInfo.exported)
                        return
                    var newEntry = IntentApplicationEntry(it, ri.activityInfo.packageName)
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
        }

        private var intentSelectorRef: WeakReference<IntentSelector> = WeakReference(intentSelector)
    }
    private var mSearchTask : SearchTask? = null
    private var adapterActivities: IntentSelectorAdapter? = null
    private var adapterShortcuts: IntentSelectorAdapter? = null

    private var fabDone: android.support.design.widget.FloatingActionButton? = null
    private var allowMultiSelect: Boolean = false
    private val selectedIntents = ArrayList<Intent>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        allowMultiSelect = intent.getBooleanExtra(EXTRA_ALLOW_MULTI_SELECT, false)

        if(mSearchTask != null) {
            mSearchTask!!.cancel(true)
            mSearchTask = null
        }

        var shortcutText = ""
        if (intent.hasExtra(EXTRA_SHORTCUT_TEXT))
            shortcutText = intent.getStringExtra(EXTRA_SHORTCUT_TEXT)!!

        var shortcutLabel = "Shortcuts"
        if (intent.hasExtra(EXTRA_STRING_SHORTCUTS))
            shortcutLabel = intent.getStringExtra(EXTRA_STRING_SHORTCUTS)!!
        var activitiesLabel = "Shortcuts"
        if (intent.hasExtra(EXTRA_STRING_ACTIVITIES))
            activitiesLabel = intent.getStringExtra(EXTRA_STRING_ACTIVITIES)!!

        setContentView(R.layout.common__intentselectorview)

        llWait = findViewById(R.id.common__intentSelector_llWait)
        //		progressWait = (ProgressBar)findViewById(R.id.intentSelector_progressWait);
        lvActivities = findViewById(R.id.common__intentSelector_lvActivities)
        chkShowAllActivities = findViewById(R.id.common__intentSelector_chkShowAllActivities)
        lvShortcuts = findViewById(R.id.common__intentSelector_lvShortcuts)
        txtShortcuts = findViewById(R.id.common__intentSelector_txtShortcuts)
        toolbar = findViewById(R.id.common__intentSelector_toolbar)

        setSupportActionBar(toolbar)

        txtShortcuts!!.text = shortcutText

        lvActivities!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val entry = adapterActivities!!.getChild(groupPosition, childPosition) as IntentApplicationEntry.IntentItem
            val resultIntent = Intent(Intent.ACTION_MAIN)
            resultIntent.setClassName(entry.packageName, entry.activityName)

            if (allowMultiSelect) {
                toggleSelection(resultIntent)
                adapterActivities!!.notifyDataSetChanged()
                true
            } else {
                setResultIntent(resultIntent)
                true
            }
        }
        chkShowAllActivities!!.setOnCheckedChangeListener { _, _ -> startSearch() }
        lvShortcuts!!.setOnChildClickListener { _, _, groupPosition, childPosition, _ ->
            val entry = adapterShortcuts!!.getChild(groupPosition, childPosition) as IntentApplicationEntry.IntentItem
            val shortcutIntent = Intent(Intent.ACTION_CREATE_SHORTCUT)
            shortcutIntent.setClassName(entry.packageName, entry.activityName)
            startActivityForResult(shortcutIntent, CREATE_SHORTCUT_REQUEST)
            false
        }

        fabDone = findViewById(R.id.common__intentSelector_fab)
        if (allowMultiSelect) {
            fabDone!!.show()
            fabDone!!.setOnClickListener {
                val resultIntent = Intent()
                resultIntent.putParcelableArrayListExtra(EXTRA_RESULT_INTENTS, selectedIntents)
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            }
            // Keep tabs but maybe hide shortcuts for multiselect if not supported?
            // Assuming shortcuts are not supported in multi-select for now or handled same way?
            // Existing logic launches helper activity for shortcuts. This breaks multi-select flow unless handled.
            // For now, let's just make Multi-Select work for Apps which is the performance killer.
        } else {
            fabDone!!.hide()
        }

        val tabs = this.findViewById<TabHost>(android.R.id.tabhost)
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_intent_selector, menu)
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(this)
        return true
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    private var mCurrentQuery: String? = null

    override fun onQueryTextChange(newText: String?): Boolean {
        mCurrentQuery = newText
        adapterActivities?.filter(newText)
        adapterShortcuts?.filter(newText)
        return true
    }

    private fun startSearch() {
        if (mSearchTask != null) {
            mSearchTask!!.isAnotherSearchRunning = true
            mSearchTask!!.isObsolete = true
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(data == null) {
            return
        }
        if (requestCode == CREATE_SHORTCUT_REQUEST && resultCode == Activity.RESULT_OK) {
            setResultIntent(data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun toggleSelection(intent: Intent) {
        // Simple check based on component name
        val component = intent.component ?: return
        val existing = selectedIntents.find { it.component == component }
        if (existing != null) {
            selectedIntents.remove(existing)
        } else {
            selectedIntents.add(intent)
        }
        updateTitleInternal()
    }

    private fun updateTitleInternal() {
        if (!allowMultiSelect) return
        if (selectedIntents.size > 0) {
            title = getString(R.string.activity_intentselector_title_count, selectedIntents.size)
        } else {
            title = getString(R.string.activity_intentselector_label)
        }
    }

    private fun isSelected(intent: Intent): Boolean {
        val component = intent.component ?: return false
        return selectedIntents.any { it.component == component }
    }

    internal class IntentSelectorAdapter(private val context: Context, entriesList: List<IntentApplicationEntry>, private val intentType: IntentApplicationEntry.IntentType) : BaseExpandableListAdapter() {
        private var entries: MutableList<IntentApplicationEntry>
        private val originalEntries: List<IntentApplicationEntry>

        init {
            this.originalEntries = entriesList
                    .filter { getSubList(it).isNotEmpty() }
            this.entries = this.originalEntries.toMutableList()
        }

        fun filter(query: String?) {
            entries.clear()
            if (query.isNullOrEmpty()) {
                entries.addAll(originalEntries)
            } else {
                val q = query.lowercase(Locale.getDefault())
                for (entry in originalEntries) {
                    var matches = false
                    if (entry.nameLowercase.contains(q)) {
                        matches = true
                    } else {
                        val subList = getSubList(entry)
                        if (subList.any { it.displayNameLowercase.contains(q) }) {
                            matches = true
                        }
                    }

                    if (matches) {
                        entries.add(entry)
                    }
                }
            }
            notifyDataSetChanged()
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
            val txt = effectiveConvertView!!.findViewById<TextView>(R.id.common__intentselectoritem_text)
            val txtActivityName = effectiveConvertView.findViewById<TextView>(R.id.common__intentselectoritem_activityName)
            val checkbox = effectiveConvertView.findViewById<CheckBox>(R.id.common__intentselectoritem_checkbox)

            val item = getSubList(entries[groupPosition])[childPosition]
            txt.text = item.displayName
            txtActivityName.text = item.activityName

            if ((context as IntentSelector).allowMultiSelect && intentType == IntentApplicationEntry.IntentType.Main) {
                checkbox.visibility = View.VISIBLE
                val testIntent = Intent(Intent.ACTION_MAIN)
                testIntent.setClassName(item.packageName, item.activityName)
                checkbox.isChecked = context.isSelected(testIntent)
            } else {
                checkbox.visibility = View.GONE
            }

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
            val img = effectiveConvertView!!.findViewById<ImageView>(R.id.common__intentselectorgroup_img)
            val txt = effectiveConvertView.findViewById<TextView>(R.id.common__intentselectorgroup_text)

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
        var EXTRA_ALLOW_MULTI_SELECT = "de.devmil.common.extras.ALLOW_MULTI_SELECT"
        var EXTRA_RESULT_INTENTS = "de.devmil.common.extras.RESULT_INTENTS"

        private fun getSubList(entry: IntentApplicationEntry, intentType: IntentApplicationEntry.IntentType): List<IntentApplicationEntry.IntentItem> {
            when (intentType) {
                IntentApplicationEntry.IntentType.Main, IntentApplicationEntry.IntentType.Launcher -> return entry.getMainActivityIntentItems()
                IntentApplicationEntry.IntentType.Shortcut -> return entry.getShortcutIntentItems()
            }
        }
    }
}

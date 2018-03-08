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
package de.devmil.paperlaunch.view.fragments


import android.app.Activity
import android.app.AlertDialog
import android.app.Fragment
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.cocosw.bottomsheet.BottomSheet
import com.makeramen.dragsortadapter.DragSortAdapter
import de.devmil.paperlaunch.EditFolderActivity
import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.config.LaunchConfig
import de.devmil.paperlaunch.config.UserSettings
import de.devmil.paperlaunch.model.Folder
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.service.LauncherOverlayService
import de.devmil.paperlaunch.storage.EntriesDataSource
import de.devmil.paperlaunch.storage.FolderDTO
import de.devmil.paperlaunch.storage.ITransactionAction
import de.devmil.paperlaunch.storage.ITransactionContext
import de.devmil.paperlaunch.utils.FolderImageHelper
import de.devmil.paperlaunch.view.utils.IntentSelector
import de.devmil.paperlaunch.view.utils.UrlSelector
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Use the [EditFolderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditFolderFragment : Fragment() {

    private var adapter: EntriesAdapter? = null
    private var recyclerView: RecyclerView? = null
    private var emptyListView: View? = null
    private var addButton: FloatingActionButton? = null
    private var editNameLayout: LinearLayout? = null
    private var folderNameEditText: EditText? = null
    private var folderId: Long = -1
    private var folder: Folder? = null
    internal var config: LaunchConfig? = null

    internal var folderNameChangedCallback: ((String) -> Unit)? = null

    fun setListener(listener: (String) -> Unit) {
        folderNameChangedCallback = listener
        if (folderNameChangedCallback != null && folder != null) {
            folderNameChangedCallback!!(folder!!.dto.name!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        config = LaunchConfig(UserSettings(activity))
        if (arguments != null) {
            folderId = arguments.getLong(ARG_PARAM_FOLDERID, -1)
        }
    }

    enum class CreateSheetActionIds(val id: Int) {
        AddApp(20001),
        AddFolder(2002),
        AddURL(2003);

        companion object {
            fun fromId(id: Int) : CreateSheetActionIds? {
                return values().firstOrNull { it.id == id }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val result = inflater.inflate(R.layout.fragment_edit_folder, container, false)

        recyclerView = result.findViewById(R.id.fragment_edit_folder_entrieslist)
        emptyListView = result.findViewById(R.id.fragment_edit_folder_emptyentries)
        addButton = result.findViewById(R.id.fragment_edit_folder_fab)
        editNameLayout = result.findViewById(R.id.fragment_edit_folder_editname_layout)
        folderNameEditText = result.findViewById(R.id.fragment_edit_folder_editname_text)

        editNameLayout!!.visibility = if (folderId >= 0) View.VISIBLE else View.GONE

        recyclerView!!.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        recyclerView!!.itemAnimator = EntriesItemAnimator()

        loadData()

        folderNameEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (folder == null) {
                    return
                }
                folder!!.dto.name = folderNameEditText!!.text.toString()
                EntriesDataSource.instance.accessData(activity, object: ITransactionAction {
                    override fun execute(transactionContext: ITransactionContext) {
                        transactionContext.updateFolderData(folder!!)
                    }
                })
                if (folderNameChangedCallback != null) {
                    folderNameChangedCallback!!(folder!!.dto.name!!)
                }
                notifyDataChanged()
            }
        })

        addButton!!.setOnClickListener {
            BottomSheet.Builder(activity)
                    .title(R.string.folder_settings_add_title)
                    .grid()
                    .sheet(CreateSheetActionIds.AddApp.id, R.mipmap.ic_link_black_48dp, R.string.folder_settings_add_app)
                    .sheet(CreateSheetActionIds.AddURL.id, R.mipmap.ic_web_black_48dp, R.string.folder_settings_add_url)
                    .sheet(CreateSheetActionIds.AddFolder.id, R.mipmap.ic_folder_black_48dp, R.string.folder_settings_add_folder)
                    .icon(R.mipmap.ic_add_black_24dp)
                    .listener { dialog, which ->
                        when (CreateSheetActionIds.fromId(which)) {
                            CreateSheetActionIds.AddApp -> {
                                initiateCreateLaunch()
                                dialog.dismiss()
                            }
                            CreateSheetActionIds.AddFolder -> {
                                initiateCreateFolder()
                                dialog.dismiss()
                            }
                            CreateSheetActionIds.AddURL -> {
                                initiateCreateUrl()
                                dialog.dismiss()
                            }
                        }
                    }.show()
        }
        return result
    }

    private fun invalidate() {
        loadData()
    }

    private fun loadData() {
        adapter = EntriesAdapter(recyclerView!!, loadEntries())
        recyclerView!!.adapter = adapter
        adapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                checkEntryViewStates()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                checkEntryViewStates()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                checkEntryViewStates()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                checkEntryViewStates()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                checkEntryViewStates()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                checkEntryViewStates()
            }
        })
        if (folder != null) {
            folderNameEditText!!.setText(folder!!.dto.name)
            if (folderNameChangedCallback != null) {
                folderNameChangedCallback!!(folder!!.dto.name!!)
            }
        }
        checkEntryViewStates()
    }

    private fun checkEntryViewStates() {
        val isEmpty = adapter == null || adapter!!.itemCount <= 0

        emptyListView!!.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView!!.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun loadEntries(): MutableList<IEntry> {

        class Local {
            var result: List<IEntry>? = null
        }

        val local = Local()
        EntriesDataSource.instance.accessData(activity, object: ITransactionAction {
            override fun execute(transactionContext: ITransactionContext) {
                if (folderId == -1L) {
                    local.result = transactionContext.loadRootContent()
                } else {
                    folder = transactionContext.loadFolder(folderId)
                    local.result = folder!!.subEntries
                }
            }
        })

        return local.result.orEmpty().toMutableList()
    }

    private fun initiateCreateFolder() {
        if (folder != null && folder!!.dto.depth >= config!!.maxFolderDepth) {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.folder_settings_add_folder_maxdepth_alert_title)
                    .setMessage(R.string.folder_settings_add_folder_maxdepth_alert_message)
                    .setPositiveButton(R.string.folder_settings_add_folder_maxdepth_alert_OK, null)
                    .show()
            return
        }
        val folderId = addFolder(resources.getString(R.string.fragment_edit_folder_new_folder_name))

        val editFolderIntent = EditFolderActivity.createLaunchIntent(activity, folderId)
        startActivityForResult(editFolderIntent, REQUEST_EDIT_FOLDER)
    }

    private fun initiateCreateLaunch() {
        val intent = Intent()
        intent.setClass(activity, IntentSelector::class.java)
        intent.putExtra(IntentSelector.EXTRA_STRING_ACTIVITIES, resources.getString(R.string.folder_settings_add_app_activities))
        intent.putExtra(IntentSelector.EXTRA_STRING_SHORTCUTS, resources.getString(R.string.folder_settings_add_app_shortcuts))

        startActivityForResult(intent, REQUEST_ADD_APP)
    }

    private fun initiateCreateUrl() {
        val intent = Intent()
        intent.setClass(activity, UrlSelector::class.java)

        startActivityForResult(intent, REQUEST_ADD_URL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_ADD_APP,
            REQUEST_ADD_URL -> {
                if (resultCode != Activity.RESULT_OK) {
                    return
                }
                if(data != null) {
                    addLaunch(data)
                }
            }
            REQUEST_EDIT_FOLDER -> {
                invalidate()
            }
        }
    }

    private fun addLaunch(launchIntent: Intent) {
        EntriesDataSource.instance.accessData(activity, object: ITransactionAction {
            override fun execute(transactionContext: ITransactionContext) {
                val l = transactionContext.createLaunch(folderId)
                l.dto.launchIntent = launchIntent
                transactionContext.updateLaunchData(l)

                adapter!!.addEntry(l)

                if (folder != null) {
                    updateFolderImage(folder!!.dto, adapter!!.entries)
                }
            }
        })
        notifyDataChanged()
    }

    private fun updateFolderImage(folderDto: FolderDTO, entries: List<IEntry>) {
        val imgWidth = config!!.imageWidthDip
        val bmp = FolderImageHelper.createImageFromEntries(activity, entries, imgWidth)
        val newIcon = BitmapDrawable(resources, bmp)
        folderDto.icon = newIcon

        EntriesDataSource.instance.accessData(activity, object: ITransactionAction {
            override fun execute(transactionContext: ITransactionContext) {
                transactionContext.updateFolderData(folderDto)
            }
        })
        notifyDataChanged()
    }

    private fun addFolder(initialName: String): Long {
        class Local {
            var folder: Folder? = null
        }

        val local = Local()
        EntriesDataSource.instance.accessData(activity, object: ITransactionAction {
            override fun execute(transactionContext: ITransactionContext) {
                local.folder = transactionContext.createFolder(folderId, -1, if (folder == null) 0 else folder!!.dto.depth)
                local.folder!!.dto.name = initialName
                transactionContext.updateFolderData(local.folder!!)
            }
        })

        adapter!!.addEntry(local.folder!!)

        notifyDataChanged()

        return local.folder!!.id
    }

    private var mScheduledNotifyDataChanged: ScheduledFuture<*>? = null

    private fun notifyDataChanged() {
        if (mScheduledNotifyDataChanged != null) {
            mScheduledNotifyDataChanged!!.cancel(false)
        }
        mScheduledNotifyDataChanged = sNotifyDataChangedWorker.schedule({ LauncherOverlayService.notifyDataChanged(activity) }, 1, TimeUnit.SECONDS)
    }

    private inner class EntriesItemAnimator : DefaultItemAnimator() {
        override fun getAddDuration(): Long {
            return 60
        }

        override fun getChangeDuration(): Long {
            return 120
        }

        override fun getMoveDuration(): Long {
            return 120
        }

        override fun getRemoveDuration(): Long {
            return 60
        }
    }

    private inner class EntriesAdapter(recyclerView: RecyclerView, private val mEntries: MutableList<IEntry>) : DragSortAdapter<EntriesAdapter.ViewHolder>(recyclerView) {

        val entries: List<IEntry>
            get() = mEntries

        fun addEntry(entry: IEntry) {
            mEntries.add(entry)
            saveOrder()
            notifyDataSetChanged()
        }

        override fun getPositionForId(id: Long): Int {
            return mEntries.indices.firstOrNull { mEntries[it].entryId == id }  ?: -1
        }

        private fun getEntryById(entryId: Long): IEntry? {
            return mEntries.firstOrNull { it.entryId == entryId }
        }

        override fun getItemId(position: Int): Long {
            return mEntries[position].entryId
        }

        override fun move(fromPosition: Int, toPosition: Int): Boolean {
            if (fromPosition < 0 || toPosition >= mEntries.size) {
                return false
            }
            mEntries.add(toPosition, mEntries.removeAt(fromPosition))
            saveOrder()
            if (folder != null) {
                updateFolderImage(folder!!.dto, mEntries)
            }
            notifyDataChanged()
            return true
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(activity).inflate(R.layout.fragment_edit_folder_entries, parent, false)
            return ViewHolder(this, view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val entry = mEntries[position]

            holder.container.visibility = if (draggingId == entry.entryId) View.INVISIBLE else View.VISIBLE
            holder.detailsImg.visibility = if (entry.isFolder) View.VISIBLE else View.GONE
            holder.imageView.setImageDrawable(entry.getIcon(holder.imageView.context))
            holder.textView.text = entry.getName(holder.textView.context)
        }

        override fun getItemCount(): Int {
            return mEntries.size
        }

        private fun saveOrder() {
            EntriesDataSource.instance.accessData(activity, object: ITransactionAction {
                override fun execute(transactionContext: ITransactionContext) {
                    transactionContext.updateOrders(mEntries)
                }
            })
        }

        internal inner class ViewHolder(dragSortAdapter: DragSortAdapter<*>, itemView: View) : DragSortAdapter.ViewHolder(dragSortAdapter, itemView), View.OnLongClickListener, View.OnClickListener {
            val container: LinearLayout = itemView.findViewById(R.id.fragment_edit_folder_entries_container)
            val imageView: ImageView = itemView.findViewById(R.id.fragment_edit_folder_entries_image)
            val textView: TextView = itemView.findViewById(R.id.fragment_edit_folder_entries_text)
            private val handle: ImageView = itemView.findViewById(R.id.fragment_edit_folder_entries_handle)
            private val deleteImg: ImageView = itemView.findViewById(R.id.fragment_edit_folder_entries_delete)
            val detailsImg: ImageView = itemView.findViewById(R.id.fragment_edit_folder_entries_details)

            init {

                handle.setOnLongClickListener(this)

                container.setOnClickListener(this)

                deleteImg.setOnClickListener(this)
            }

            override fun onLongClick(view: View): Boolean {
                startDrag()
                return true
            }

            override fun onClick(v: View) {
                val entry = getEntryById(itemId)
                if (v === container) {
                    if (entry!!.isFolder) {
                        val editFolderIntent = EditFolderActivity.createLaunchIntent(activity, entry.id)
                        startActivityForResult(editFolderIntent, REQUEST_EDIT_FOLDER)
                    }
                } else if (v === deleteImg) {
                    EntriesDataSource.instance.accessData(activity, object: ITransactionAction {
                        override fun execute(transactionContext: ITransactionContext) {
                            entry?.let { e ->
                                transactionContext.deleteEntry(e.entryId)

                                val pos = getPositionForId(itemId)
                                mEntries.removeAt(pos)
                                folder?.let { f ->
                                    updateFolderImage(f.dto, mEntries)
                                }
                                notifyItemRemoved(pos)
                            }
                        }
                    })
                    notifyDataChanged()
                }
            }
        }
    }

    companion object {
        private val ARG_PARAM_FOLDERID = "paramFolderId"
        private val REQUEST_ADD_APP = 1000
        private val REQUEST_ADD_URL = 1001
        private val REQUEST_EDIT_FOLDER = 1010

        private val sNotifyDataChangedWorker = Executors.newSingleThreadScheduledExecutor()

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.

         * @param folderId FolderId of the folder to edit.
         * *
         * @return A new instance of fragment EditFolderFragment.
         */
        fun newInstance(folderId: Long): EditFolderFragment {
            val fragment = EditFolderFragment()
            val args = Bundle()
            args.putLong(ARG_PARAM_FOLDERID, folderId)
            fragment.arguments = args
            return fragment
        }
    }
}

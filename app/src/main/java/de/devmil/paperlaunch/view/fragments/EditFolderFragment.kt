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
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass.
 * Use the [EditFolderFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditFolderFragment : Fragment() {

    private var mAdapter: EntriesAdapter? = null
    private var mRecyclerView: RecyclerView? = null
    private var mEmptyListView: View? = null
    private var mAddButton: FloatingActionButton? = null
    private var mEditNameLayout: LinearLayout? = null
    private var mFolderNameEditText: EditText? = null
    private var mFolderId: Long = -1
    private var mFolder: Folder? = null
    internal var mConfig: LaunchConfig? = null

    internal var mFolderNameChangedCallback: ((String) -> Unit)? = null

    interface IEditFolderFragmentListener {
        fun onFolderNameChanged(newName: String)
    }

    fun setListener(listener: (String) -> Unit) {
        mFolderNameChangedCallback = listener
        if (mFolderNameChangedCallback != null && mFolder != null) {
            mFolderNameChangedCallback!!(mFolder!!.dto.name!!)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mConfig = LaunchConfig(UserSettings(activity))
        if (arguments != null) {
            mFolderId = arguments.getLong(ARG_PARAM_FOLDERID, -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val result = inflater.inflate(R.layout.fragment_edit_folder, container, false)

        mRecyclerView = result.findViewById(R.id.fragment_edit_folder_entrieslist) as RecyclerView
        mEmptyListView = result.findViewById(R.id.fragment_edit_folder_emptyentries)
        mAddButton = result.findViewById(R.id.fragment_edit_folder_fab) as FloatingActionButton
        mEditNameLayout = result.findViewById(R.id.fragment_edit_folder_editname_layout) as LinearLayout
        mFolderNameEditText = result.findViewById(R.id.fragment_edit_folder_editname_text) as EditText

        mEditNameLayout!!.visibility = if (mFolderId >= 0) View.VISIBLE else View.GONE

        mRecyclerView!!.layoutManager = LinearLayoutManager(activity, LinearLayout.VERTICAL, false)
        mRecyclerView!!.itemAnimator = EntriesItemAnimator()

        loadData()

        mFolderNameEditText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable) {
                if (mFolder == null) {
                    return
                }
                mFolder!!.dto.name = mFolderNameEditText!!.text.toString()
                EntriesDataSource.instance.accessData(activity, object: ITransactionAction {
                    override fun execute(transactionContext: ITransactionContext) {
                        transactionContext.updateFolderData(mFolder!!)
                    }
                })
                if (mFolderNameChangedCallback != null) {
                    mFolderNameChangedCallback!!(mFolder!!.dto.name!!)
                }
                notifyDataChanged()
            }
        })

        mAddButton!!.setOnClickListener {
            BottomSheet.Builder(activity)
                    .title(R.string.folder_settings_add_title)
                    .grid()
                    .sheet(2001, R.mipmap.ic_link_black_48dp, R.string.folder_settings_add_app)
                    .sheet(2002, R.mipmap.ic_folder_black_48dp, R.string.folder_settings_add_folder)
                    .icon(R.mipmap.ic_add_black_24dp)
                    .listener { dialog, which ->
                        when (which) {
                            2001 -> {
                                initiateCreateLaunch()
                                dialog.dismiss()
                            }
                            2002 -> {
                                initiateCreateFolder()
                                dialog.dismiss()
                            }
                        }
                    }.show()
        }
        return result
    }

    fun invalidate() {
        loadData()
    }

    private fun loadData() {
        mAdapter = EntriesAdapter(mRecyclerView!!, loadEntries())
        mRecyclerView!!.setAdapter(mAdapter)
        mAdapter!!.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
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
        if (mFolder != null) {
            mFolderNameEditText!!.setText(mFolder!!.dto.name)
            if (mFolderNameChangedCallback != null) {
                mFolderNameChangedCallback!!(mFolder!!.dto.name!!)
            }
        }
        checkEntryViewStates()
    }

    private fun checkEntryViewStates() {
        val isEmpty = mAdapter == null || mAdapter!!.itemCount <= 0

        mEmptyListView!!.visibility = if (isEmpty) View.VISIBLE else View.GONE
        mRecyclerView!!.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun loadEntries(): MutableList<IEntry> {

        class Local {
            var result: List<IEntry>? = null
        }

        val local = Local()
        EntriesDataSource.instance.accessData(activity, object: ITransactionAction {
            override fun execute(transactionContext: ITransactionContext) {
                if (mFolderId == -1L) {
                    local.result = transactionContext.loadRootContent()
                } else {
                    mFolder = transactionContext.loadFolder(mFolderId)
                    local.result = mFolder!!.subEntries
                }
            }
        })

        return local.result.orEmpty().toMutableList()
    }

    private fun initiateCreateFolder() {
        if (mFolder != null && mFolder!!.dto.depth >= mConfig!!.maxFolderDepth) {
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (REQUEST_ADD_APP == requestCode) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }
            if(data != null) {
                addLaunch(data!!)
            }
        } else if (REQUEST_EDIT_FOLDER == requestCode) {
            invalidate()
        }
    }

    private fun addLaunch(launchIntent: Intent) {
        EntriesDataSource.instance.accessData(activity, object: ITransactionAction {
            override fun execute(transactionContext: ITransactionContext) {
                val l = transactionContext.createLaunch(mFolderId)
                l.dto.launchIntent = launchIntent
                transactionContext.updateLaunchData(l)

                mAdapter!!.addEntry(l)

                if (mFolder != null) {
                    updateFolderImage(mFolder!!.dto, mAdapter!!.entries)
                }
            }
        })
        notifyDataChanged()
    }

    private fun updateFolderImage(folder: Folder) {
        updateFolderImage(folder.dto, folder.subEntries.orEmpty())
    }

    private fun updateFolderImage(folderDto: FolderDTO, entries: List<IEntry>) {
        val imgWidth = mConfig!!.imageWidthDip
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
                local.folder = transactionContext.createFolder(mFolderId, -1, if (mFolder == null) 0 else mFolder!!.dto.depth)
                local.folder!!.dto.name = initialName
                transactionContext.updateFolderData(local.folder!!)
            }
        })

        mAdapter!!.addEntry(local.folder!!)

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

        override fun getPositionForId(entryId: Long): Int {
            for (i in mEntries.indices) {
                if (mEntries[i].entryId == entryId) {
                    return i
                }
            }
            return -1
        }

        private fun getEntryById(entryId: Long): IEntry? {
            for (entry in mEntries) {
                if (entry.entryId == entryId) {
                    return entry
                }
            }
            return null
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
            if (mFolder != null) {
                updateFolderImage(mFolder!!.dto, mEntries)
            }
            notifyDataChanged()
            return true
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(activity).inflate(R.layout.fragment_edit_folder_entries, parent, false)
            val vh = ViewHolder(this, view)
            return vh
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
            val container: LinearLayout = itemView.findViewById(R.id.fragment_edit_folder_entries_container) as LinearLayout
            val imageView: ImageView = itemView.findViewById(R.id.fragment_edit_folder_entries_image) as ImageView
            val textView: TextView = itemView.findViewById(R.id.fragment_edit_folder_entries_text) as TextView
            private val handle: ImageView = itemView.findViewById(R.id.fragment_edit_folder_entries_handle) as ImageView
            private val deleteImg: ImageView = itemView.findViewById(R.id.fragment_edit_folder_entries_delete) as ImageView
            val detailsImg: ImageView = itemView.findViewById(R.id.fragment_edit_folder_entries_details) as ImageView

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
                            transactionContext.deleteEntry(entry!!.entryId)

                            val pos = getPositionForId(itemId)
                            mEntries.removeAt(pos)
                            if (mFolder != null) {
                                updateFolderImage(mFolder!!.dto, mEntries)
                            }
                            notifyItemRemoved(pos)
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

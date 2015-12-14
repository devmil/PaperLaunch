package de.devmil.paperlaunch.view.fragments;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.makeramen.dragsortadapter.DragSortAdapter;

import java.util.List;

import de.devmil.paperlaunch.EditFolderActivity;
import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.model.Folder;
import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.model.Launch;
import de.devmil.paperlaunch.storage.EntriesDataSource;
import de.devmil.paperlaunch.view.utils.IntentSelector;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link EditFolderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditFolderFragment extends Fragment {
    private static final String ARG_PARAM_FOLDERID = "paramFolderId";
    private static final int REQUEST_ADD_APP = 1000;
    private static final int REQUEST_EDIT_FOLDER = 1010;

    private EntriesAdapter mAdapter;
    private EntriesDataSource mDataSource;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mAddButton;
    private LinearLayout mEditNameLayout;
    private EditText mFolderNameEditText;
    private long mFolderId = -1;
    private Folder mFolder = null;

    public EditFolderFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param folderId FolderId of the folder to edit.
     * @return A new instance of fragment EditFolderFragment.
     */
    public static EditFolderFragment newInstance(long folderId) {
        EditFolderFragment fragment = new EditFolderFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PARAM_FOLDERID, folderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mFolderId = getArguments().getLong(ARG_PARAM_FOLDERID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_edit_folder, container, false);

        mDataSource = new EntriesDataSource(getContext());

        mRecyclerView = (RecyclerView)result.findViewById(R.id.fragment_edit_folder_entrieslist);
        mAddButton = (FloatingActionButton)result.findViewById(R.id.fragment_edit_folder_fab);
        mEditNameLayout = (LinearLayout)result.findViewById(R.id.fragment_edit_folder_editname_layout);
        mFolderNameEditText = (EditText)result.findViewById(R.id.fragment_edit_folder_editname_text);

        mEditNameLayout.setVisibility(mFolderId >= 0 ? View.VISIBLE : View.GONE);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayout.VERTICAL, false));
        mRecyclerView.setItemAnimator(new EntriesItemAnimator());

        loadData();

        mFolderNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mFolder == null) {
                    return;
                }
                mFolder.getDto().setName(mFolderNameEditText.getText().toString());
                mDataSource.open();
                mDataSource.updateFolderData(mFolder);
                mDataSource.close();
            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new BottomSheet.Builder(getActivity())
                        .title(R.string.folder_settings_add_title)
                        .grid()
                        .sheet(2001, R.mipmap.ic_link_black_48dp, R.string.folder_settings_add_app)
                        .sheet(2002, R.mipmap.ic_folder_black_48dp, R.string.folder_settings_add_folder)
                        .listener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 2001:
                                        initiateCreateLaunch();
                                        dialog.dismiss();
                                        break;
                                    case 2002:
                                        initiateCreateFolder();
                                        dialog.dismiss();
                                        break;
                                }
                            }
                        }).show();

            }
        });
        return result;
    }

    public void invalidate() {
        loadData();
    }

    private void loadData() {
        mRecyclerView.setAdapter(mAdapter = new EntriesAdapter(mRecyclerView, loadEntries(), mDataSource));
        if(mFolder != null) {
            mFolderNameEditText.setText(mFolder.getDto().getName());
        }
    }

    private List<IEntry> loadEntries() {
        mDataSource.open();

        List<IEntry> result = null;
        if(mFolderId == -1) {
            result = mDataSource.loadRootContent();
        } else {
            mFolder = mDataSource.loadFolder(mFolderId);
            result = mFolder.getSubEntries();
        }

        mDataSource.close();

        return result;
    }

    private void initiateCreateFolder() {
        long folderId = addFolder("New Folder");

        Intent editFolderIntent = EditFolderActivity.createLaunchIntent(getContext(), folderId);
        startActivityForResult(editFolderIntent, REQUEST_EDIT_FOLDER);
    }

    private void initiateCreateLaunch() {
        Intent intent = new Intent();
        intent.setClass(getContext(), IntentSelector.class);
        intent.putExtra(IntentSelector.EXTRA_STRING_ACTIVITIES, getResources().getString(R.string.folder_settings_add_app_activities));
        intent.putExtra(IntentSelector.EXTRA_STRING_SHORTCUTS, getResources().getString(R.string.folder_settings_add_app_shortcuts));

        startActivityForResult(intent, REQUEST_ADD_APP);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(REQUEST_ADD_APP == requestCode) {
            if(resultCode != Activity.RESULT_OK) {
                return;
            }
            addLaunch(data);
        }
        else if(REQUEST_EDIT_FOLDER == requestCode) {
            invalidate();
        }
    }

    private void addLaunch(Intent launchIntent) {
        mDataSource.open();

        Launch l = mDataSource.createLaunch(mFolderId);
        l.getDto().setLaunchIntent(launchIntent);
        mDataSource.updateLaunchData(l);
        mDataSource.close();

        mAdapter.addEntry(l);
    }

    private long addFolder(String initialName) {
        mDataSource.open();
        Folder folder = mDataSource.createFolder(mFolderId);
        folder.getDto().setName(initialName);
        mDataSource.updateFolderData(folder);
        mDataSource.close();

        mAdapter.addEntry(folder);

        return folder.getId();
    }

    private class EntriesItemAnimator extends DefaultItemAnimator
    {
        @Override
        public long getAddDuration() {
            return 60;
        }

        @Override
        public long getChangeDuration() {
            return 120;
        }

        @Override
        public long getMoveDuration() {
            return 120;
        }

        @Override
        public long getRemoveDuration() {
            return 60;
        }
    }

    private class EntriesAdapter extends DragSortAdapter<EntriesAdapter.ViewHolder>
    {
        private List<IEntry> mEntries;
        private EntriesDataSource mDataSource;

        public EntriesAdapter(RecyclerView recyclerView, List<IEntry> entries, EntriesDataSource dataSource) {
            super(recyclerView);
            mEntries = entries;
            mDataSource = dataSource;
        }

        public void addEntry(IEntry entry) {
            mEntries.add(entry);
            saveOrder();
            notifyDataSetChanged();
        }

        @Override
        public int getPositionForId(long entryId) {
            for(int i=0; i<mEntries.size(); i++) {
                if(mEntries.get(i).getEntryId() == entryId) {
                    return i;
                }
            }
            return -1;
        }

        private IEntry getEntryById(long entryId) {
            for(IEntry entry : mEntries) {
                if(entry.getEntryId() == entryId) {
                    return entry;
                }
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return mEntries.get(position).getEntryId();
        }

        @Override
        public boolean move(int fromPosition, int toPosition) {
            if(fromPosition < 0
                    || toPosition >= mEntries.size()) {
                return false;
            }
            mEntries.add(toPosition, mEntries.remove(fromPosition));
            saveOrder();
            return true;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater(null).inflate(R.layout.fragment_edit_folder_entries, parent, false);
            ViewHolder vh = new ViewHolder(this, view);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            IEntry entry = mEntries.get(position);

            holder.container.setVisibility(getDraggingId() == entry.getId() ? View.INVISIBLE : View.VISIBLE);
            holder.imageView.setImageDrawable(entry.getIcon(holder.imageView.getContext()));
            holder.textView.setText(entry.getName(holder.textView.getContext()));

            int editVisibility = entry.isFolder() ? View.VISIBLE : View.GONE;
            if(holder.editImg.getVisibility() != editVisibility) {
                holder.editImg.setVisibility(editVisibility);
            }
        }

        @Override
        public int getItemCount() {
            return mEntries.size();
        }

        private void saveOrder() {
            mDataSource.open();

            mDataSource.updateOrders(mEntries);

            mDataSource.close();
        }

        class ViewHolder extends DragSortAdapter.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
            public LinearLayout container;
            public ImageView imageView;
            public TextView textView;
            private ImageView handle;
            private ImageView editImg;
            private ImageView deleteImg;

            public ViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
                super(dragSortAdapter, itemView);
                container = (LinearLayout)itemView.findViewById(R.id.fragment_edit_folder_entries_container);
                imageView = (ImageView)itemView.findViewById(R.id.fragment_edit_folder_entries_image);
                textView = (TextView)itemView.findViewById(R.id.fragment_edit_folder_entries_text);
                handle = (ImageView)itemView.findViewById(R.id.fragment_edit_folder_entries_handle);
                editImg = (ImageView)itemView.findViewById(R.id.fragment_edit_folder_entries_edit);
                deleteImg = (ImageView)itemView.findViewById(R.id.fragment_edit_folder_entries_delete);

                //TODO: check if a long click or a tap event fits better
                handle.setOnLongClickListener(this);

                //TODO: attach tap event for opening the details activity / fragment (tablets?)
                container.setOnClickListener(this);

                editImg.setOnClickListener(this);

                deleteImg.setOnClickListener(this);
            }

            @Override
            public boolean onLongClick(View view) {
                startDrag();
                return true;
            }

            @Override
            public void onClick(View v) {
                IEntry entry = getEntryById(getItemId());
                if(v == container) {
                }
                else if(v == editImg) {
                    if(entry.isFolder()) {
                        Intent editFolderIntent = EditFolderActivity.createLaunchIntent(getContext(), entry.getId());
                        startActivityForResult(editFolderIntent, REQUEST_EDIT_FOLDER);
                    }
                }
                else if(v == deleteImg) {
                    mDataSource.open();
                    mDataSource.deleteEntry(entry.getEntryId());
                    mDataSource.close();
                    int pos = getPositionForId(getItemId());
                    mEntries.remove(pos);
                    notifyItemRemoved(pos);
                }
            }
        }
    }
}

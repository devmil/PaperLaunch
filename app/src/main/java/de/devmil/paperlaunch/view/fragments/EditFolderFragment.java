package de.devmil.paperlaunch.view.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.devmil.paperlaunch.EditFolderActivity;
import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.model.Folder;
import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.model.Launch;
import de.devmil.paperlaunch.model.LaunchConfig;
import de.devmil.paperlaunch.service.LauncherOverlayService;
import de.devmil.paperlaunch.storage.EntriesDataSource;
import de.devmil.paperlaunch.storage.FolderDTO;
import de.devmil.paperlaunch.storage.ITransactionAction;
import de.devmil.paperlaunch.storage.ITransactionContext;
import de.devmil.paperlaunch.storage.UserSettings;
import de.devmil.paperlaunch.utils.FolderImageHelper;
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
    private RecyclerView mRecyclerView;
    private FloatingActionButton mAddButton;
    private LinearLayout mEditNameLayout;
    private EditText mFolderNameEditText;
    private long mFolderId = -1;
    private Folder mFolder = null;
    LaunchConfig mConfig;

    IEditFolderFragmentListener mListener = null;

    public interface IEditFolderFragmentListener {
        void onFolderNameChanged(String newName);
    }

    private static final ScheduledExecutorService sNotifyDataChangedWorker = Executors.newSingleThreadScheduledExecutor();

    public EditFolderFragment() {
    }

    public void setListener(IEditFolderFragmentListener listener) {
        mListener = listener;
        if(mListener != null && mFolder != null) {
            mListener.onFolderNameChanged(mFolder.getDto().getName());
        }
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
        mConfig = new LaunchConfig(new UserSettings(getActivity()));
        if (getArguments() != null) {
            mFolderId = getArguments().getLong(ARG_PARAM_FOLDERID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.fragment_edit_folder, container, false);

        mRecyclerView = (RecyclerView)result.findViewById(R.id.fragment_edit_folder_entrieslist);
        mAddButton = (FloatingActionButton)result.findViewById(R.id.fragment_edit_folder_fab);
        mEditNameLayout = (LinearLayout)result.findViewById(R.id.fragment_edit_folder_editname_layout);
        mFolderNameEditText = (EditText)result.findViewById(R.id.fragment_edit_folder_editname_text);

        mEditNameLayout.setVisibility(mFolderId >= 0 ? View.VISIBLE : View.GONE);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayout.VERTICAL, false));
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
                EntriesDataSource.getInstance().accessData(getActivity(), new ITransactionAction() {
                    @Override
                    public void execute(ITransactionContext transactionContext) {
                        transactionContext.updateFolderData(mFolder);
                    }
                });
                if(mListener != null) {
                    mListener.onFolderNameChanged(mFolder.getDto().getName());
                }
                notifyDataChanged();
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
        mRecyclerView.setAdapter(mAdapter = new EntriesAdapter(mRecyclerView, loadEntries()));
        if(mFolder != null) {
            mFolderNameEditText.setText(mFolder.getDto().getName());
            if(mListener != null) {
                mListener.onFolderNameChanged(mFolder.getDto().getName());
            }
        }
    }

    private List<IEntry> loadEntries() {

        class Local {
            List<IEntry> result;
        }

        final Local local = new Local();
        EntriesDataSource.getInstance().accessData(getActivity(), new ITransactionAction() {
            @Override
            public void execute(ITransactionContext transactionContext) {
                if (mFolderId == -1) {
                    local.result = transactionContext.loadRootContent();
                } else {
                    mFolder = transactionContext.loadFolder(mFolderId);
                    local.result = mFolder.getSubEntries();
                }
            }
        });

        return local.result;
    }

    private void initiateCreateFolder() {
        long folderId = addFolder(getResources().getString(R.string.fragment_edit_folder_new_folder_name));

        Intent editFolderIntent = EditFolderActivity.createLaunchIntent(getActivity(), folderId);
        startActivityForResult(editFolderIntent, REQUEST_EDIT_FOLDER);
    }

    private void initiateCreateLaunch() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), IntentSelector.class);
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

    private void addLaunch(final Intent launchIntent) {
        EntriesDataSource.getInstance().accessData(getActivity(), new ITransactionAction() {
            @Override
            public void execute(ITransactionContext transactionContext) {
                Launch l = transactionContext.createLaunch(mFolderId);
                l.getDto().setLaunchIntent(launchIntent);
                transactionContext.updateLaunchData(l);

                mAdapter.addEntry(l);

                if (mFolder != null) {
                    updateFolderImage(mFolder.getDto(), mAdapter.getEntries());
                }
            }
        });
        notifyDataChanged();
    }

    private void updateFolderImage(Folder folder) {
        updateFolderImage(folder.getDto(), folder.getSubEntries());
    }

    private void updateFolderImage(final FolderDTO folderDto, List<IEntry> entries) {
        float imgWidth = mConfig.getImageWidthDip();
        Bitmap bmp = FolderImageHelper.createImageFromEntries(getActivity(), entries, imgWidth);
        Drawable newIcon = new BitmapDrawable(getResources(), bmp);
        folderDto.setIcon(newIcon);

        EntriesDataSource.getInstance().accessData(getActivity(), new ITransactionAction() {
            @Override
            public void execute(ITransactionContext transactionContext) {
                transactionContext.updateFolderData(folderDto);
            }
        });
        notifyDataChanged();
    }

    private long addFolder(final String initialName) {
        class Local {
            Folder folder = null;
        }
        final Local local = new Local();
        EntriesDataSource.getInstance().accessData(getActivity(), new ITransactionAction() {
            @Override
            public void execute(ITransactionContext transactionContext) {
                local.folder = transactionContext.createFolder(mFolderId);
                local.folder.getDto().setName(initialName);
                transactionContext.updateFolderData(local.folder);
            }
        });

        mAdapter.addEntry(local.folder);

        notifyDataChanged();

        return local.folder.getId();
    }

    private ScheduledFuture<?> mScheduledNotifyDataChanged;

    private void notifyDataChanged() {
        if(mScheduledNotifyDataChanged != null) {
            mScheduledNotifyDataChanged.cancel(false);
        }
        mScheduledNotifyDataChanged = sNotifyDataChangedWorker.schedule(new Runnable() {
            @Override
            public void run() {
                LauncherOverlayService.notifyDataChanged(getActivity());
            }
        }, 1, TimeUnit.SECONDS);
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

        public EntriesAdapter(RecyclerView recyclerView, List<IEntry> entries) {
            super(recyclerView);
            mEntries = entries;
        }

        public List<IEntry> getEntries() {
            return mEntries;
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
            if (mFolder != null) {
                updateFolderImage(mFolder.getDto(), mEntries);
            }
            notifyDataChanged();
            return true;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_edit_folder_entries, parent, false);
            ViewHolder vh = new ViewHolder(this, view);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            IEntry entry = mEntries.get(position);

            holder.container.setVisibility(getDraggingId() == entry.getEntryId() ? View.INVISIBLE : View.VISIBLE);
            holder.imageView.setImageDrawable(entry.getIcon(holder.imageView.getContext()));
            holder.textView.setText(entry.getName(holder.textView.getContext()));
        }

        @Override
        public int getItemCount() {
            return mEntries.size();
        }

        private void saveOrder() {
            EntriesDataSource.getInstance().accessData(getActivity(), new ITransactionAction() {
                @Override
                public void execute(ITransactionContext transactionContext) {
                    transactionContext.updateOrders(mEntries);
                }
            });
        }

        class ViewHolder extends DragSortAdapter.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
            public LinearLayout container;
            public ImageView imageView;
            public TextView textView;
            private ImageView handle;
            private ImageView deleteImg;

            public ViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
                super(dragSortAdapter, itemView);
                container = (LinearLayout)itemView.findViewById(R.id.fragment_edit_folder_entries_container);
                imageView = (ImageView)itemView.findViewById(R.id.fragment_edit_folder_entries_image);
                textView = (TextView)itemView.findViewById(R.id.fragment_edit_folder_entries_text);
                handle = (ImageView)itemView.findViewById(R.id.fragment_edit_folder_entries_handle);
                deleteImg = (ImageView)itemView.findViewById(R.id.fragment_edit_folder_entries_delete);

                //TODO: check if a long click or a tap event fits better
                handle.setOnLongClickListener(this);

                container.setOnClickListener(this);

                deleteImg.setOnClickListener(this);
            }

            @Override
            public boolean onLongClick(View view) {
                startDrag();
                return true;
            }

            @Override
            public void onClick(View v) {
                final IEntry entry = getEntryById(getItemId());
                if(v == container) {
                    if(entry.isFolder()) {
                        Intent editFolderIntent = EditFolderActivity.createLaunchIntent(getActivity(), entry.getId());
                        startActivityForResult(editFolderIntent, REQUEST_EDIT_FOLDER);
                    }
                }
                else if(v == deleteImg) {
                    EntriesDataSource.getInstance().accessData(getActivity(), new ITransactionAction() {
                        @Override
                        public void execute(ITransactionContext transactionContext) {
                            transactionContext.deleteEntry(entry.getEntryId());

                            int pos = getPositionForId(getItemId());
                            mEntries.remove(pos);
                            if (mFolder != null) {
                                updateFolderImage(mFolder.getDto(), mEntries);
                            }
                            notifyItemRemoved(pos);
                        }
                    });
                    notifyDataChanged();
                }
            }
        }
    }
}

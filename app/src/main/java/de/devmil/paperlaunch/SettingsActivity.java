package de.devmil.paperlaunch;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cocosw.bottomsheet.BottomSheet;
import com.makeramen.dragsortadapter.DragSortAdapter;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.model.Folder;
import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.model.Launch;
import de.devmil.paperlaunch.storage.EntriesDataSource;
import de.devmil.paperlaunch.view.utils.IntentSelector;

public class SettingsActivity extends Activity {

    private static final int CODE_ADD_APP_RESULT = 1000;

    private EntriesAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Button mButtonTest;
    private FloatingActionButton mAddButton;
    private int mParentFolderId = -1;
    private EntriesDataSource mDataSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mDataSource = new EntriesDataSource(this);

        mRecyclerView = (RecyclerView)findViewById(R.id.activity_settings_entrieslist);
        mButtonTest = (Button)findViewById(R.id.activity_settings_buttontest);
        mAddButton = (FloatingActionButton)findViewById(R.id.activity_settings_fab);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayout.VERTICAL, false));
        mRecyclerView.setItemAnimator(new EntriesItemAnimator());

        mRecyclerView.setAdapter(mAdapter = new EntriesAdapter(mRecyclerView, loadEntries(), mDataSource));

        mButtonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = new Intent(SettingsActivity.this, TestDrive.class);
                startActivity(launchIntent);
            }
        });

        //TODO: attach a sub menu for choosing between adding a Launch or a Folder
        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new BottomSheet.Builder(SettingsActivity.this)
                        .title(R.string.folder_settings_add_title)
                        .grid()
                        .sheet(2001, R.mipmap.ic_link_black_48dp, R.string.folder_settings_add_app)
                        .sheet(2002, R.mipmap.ic_folder_black_48dp, R.string.folder_settings_add_folder)
                        .listener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch(which) {
                                    case 2001:
                                        Intent intent = new Intent();
                                        intent.setClass(SettingsActivity.this, IntentSelector.class);
                                        intent.putExtra(IntentSelector.EXTRA_STRING_ACTIVITIES, getResources().getString(R.string.folder_settings_add_app_activities));
                                        intent.putExtra(IntentSelector.EXTRA_STRING_SHORTCUTS, getResources().getString(R.string.folder_settings_add_app_shortcuts));

                                        startActivityForResult(intent, CODE_ADD_APP_RESULT);
                                        break;
                                    case 2002:
                                        break;
                                }
                            }
                        }).show();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != RESULT_OK) {
            return;
        }
        if(CODE_ADD_APP_RESULT == requestCode) {
            addLaunch(data);
        }
    }

    private void addLaunch(Intent launchIntent) {
        mDataSource.open();

        Launch l = mDataSource.createLaunch(mParentFolderId);
        l.getDto().setLaunchIntent(launchIntent);
        mDataSource.updateLaunchData(l);
        mDataSource.close();

        mAdapter.addEntry(l);
    }

    private List<IEntry> loadEntries() {
        mDataSource.open();

        //resetData();

        List<IEntry> result = mDataSource.loadRootContent();

        mDataSource.close();

        return result;
    }

    private void resetData() {
        mDataSource.clear();

        createLaunch("com.agilebits.onepassword", "com.agilebits.onepassword.activity.LoginActivity", 1);
        createLaunch("org.kman.AquaMail", "org.kman.AquaMail.ui.AccountListActivity", 2);
        createLaunch("com.microsoft.office.onenote", "com.microsoft.office.onenote.ui.ONMSplashActivity", 3);
        createLaunch("com.spotify.music", "com.spotify.music.MainActivity", 5);

        List<ComponentName> folderLaunchComponents = new ArrayList<>();
        folderLaunchComponents.add(new ComponentName("mobi.koni.appstofiretv", "mobi.koni.appstofiretv.MainActivity"));
        folderLaunchComponents.add(new ComponentName("org.dmfs.tasks", "org.dmfs.tasks.TaskListActivity"));

        createFolder("Test Folder", folderLaunchComponents, 4);
    }

    private Launch createLaunch(String packageName, String className, int orderIndex) {
        return createLaunch(-1, packageName, className, orderIndex);
    }


    private Launch createLaunch(long parentFolderId, String packageName, String className, int orderIndex) {
        Launch l = mDataSource.createLaunch(parentFolderId, orderIndex);

        Intent launchIntent = new Intent();
        launchIntent.setComponent(new ComponentName(packageName, className));

        l.getDto().setLaunchIntent(launchIntent);

        mDataSource.updateLaunchData(l);

        return l;
    }

    private Folder createFolder(String folderName, List<ComponentName> launchComponentNames, int orderIndex)
    {
        Folder f = mDataSource.createFolder(-1, orderIndex);
        f.getDto().setName(folderName);

        mDataSource.updateFolderData(f);

        int launchOrderIndex = 1;

        for(ComponentName cn : launchComponentNames) {
            createLaunch(f.getId(), cn.getPackageName(), cn.getClassName(), launchOrderIndex);
            launchOrderIndex++;
        }

        return mDataSource.loadFolder(f.getId());
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
        public int getPositionForId(long id) {
            for(int i=0; i<mEntries.size(); i++) {
                if(mEntries.get(i).getId() == id) {
                    return i;
                }
            }
            return -1;
        }

        private IEntry getEntryById(long id) {
            for(IEntry entry : mEntries) {
                if(entry.getId() == id) {
                    return entry;
                }
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return mEntries.get(position).getId();
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
            View view = getLayoutInflater().inflate(R.layout.activity_settings_entries, parent, false);
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
                container = (LinearLayout)itemView.findViewById(R.id.activity_settings_entries_container);
                imageView = (ImageView)itemView.findViewById(R.id.activity_settings_entries_image);
                textView = (TextView)itemView.findViewById(R.id.activity_settings_entries_text);
                handle = (ImageView)itemView.findViewById(R.id.activity_settings_entries_handle);
                editImg = (ImageView)itemView.findViewById(R.id.activity_settings_entries_edit);
                deleteImg = (ImageView)itemView.findViewById(R.id.activity_settings_entries_delete);

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
                    Intent editIntent = entry.getEditIntent(v.getContext());
                    if (editIntent != null) {
                        startActivity(editIntent);
                    }
                }
                else if(v == editImg) {
                    //TODO: launch edit folder
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

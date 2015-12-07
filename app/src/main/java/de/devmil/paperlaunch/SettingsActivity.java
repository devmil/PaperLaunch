package de.devmil.paperlaunch;

import android.app.Activity;
import android.content.ComponentName;
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

import com.makeramen.dragsortadapter.DragSortAdapter;

import java.util.ArrayList;
import java.util.List;

import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.model.Launch;
import de.devmil.paperlaunch.model.LaunchConfig;
import de.devmil.paperlaunch.storage.EntriesDataSource;
import de.devmil.paperlaunch.storage.LaunchDTO;
import de.devmil.paperlaunch.view.utils.IntentSelector;

public class SettingsActivity extends Activity {

    private static final int CODE_ADD_APP_RESULT = 1000;

    private EntriesAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Button mButtonTest;
    private FloatingActionButton mAddButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mRecyclerView = (RecyclerView)findViewById(R.id.activity_settings_entrieslist);
        mButtonTest = (Button)findViewById(R.id.activity_settings_buttontest);
        mAddButton = (FloatingActionButton)findViewById(R.id.activity_settings_fab);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayout.VERTICAL, false));
        mRecyclerView.setItemAnimator(new EntriesItemAnimator());

        mRecyclerView.setAdapter(mAdapter = new EntriesAdapter(mRecyclerView, loadEntries()));

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
                Intent intent = new Intent();
                intent.setClass(v.getContext(), IntentSelector.class);
                intent.putExtra(IntentSelector.EXTRA_STRING_ACTIVITIES, "--Activities--");
                intent.putExtra(IntentSelector.EXTRA_STRING_SHORTCUTS, "--Shortcuts--");

                startActivityForResult(intent, CODE_ADD_APP_RESULT);
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

        long id = mAdapter.getNextId();
        LaunchDTO launchDTO = new LaunchDTO(id, null, launchIntent, null);
        Launch l = new Launch(launchDTO);

        mAdapter.addEntry(l);
    }

    private List<IEntry> loadEntries() {
        EntriesDataSource ds = new EntriesDataSource(this);

        ds.open();

        //resetData(ds);

        List<IEntry> result = ds.loadRootContent();

        ds.close();

        return result;
    }

    private void resetData(EntriesDataSource ds) {
        ds.clear();

        createLaunch(ds, "com.agilebits.onepassword", "com.agilebits.onepassword.activity.LoginActivity");
        createLaunch(ds, "org.kman.AquaMail", "org.kman.AquaMail.ui.AccountListActivity");
        createLaunch(ds, "com.microsoft.office.onenote", "com.microsoft.office.onenote.ui.ONMSplashActivity");
        createLaunch(ds, "com.spotify.music", "com.spotify.music.MainActivity");
    }

    private Launch createLaunch(EntriesDataSource ds, String packageName, String className) {
        Launch l = ds.createLaunch(-1);

        Intent launchIntent = new Intent();
        launchIntent.setComponent(new ComponentName(packageName, className));

        l.getDto().setLaunchIntent(launchIntent);

        ds.updateLaunchData(l);

        return l;
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

        public long getNextId() {
            long max = 0;
            for(IEntry e : mEntries) {
                if(max < e.getId()) {
                    max = e.getId();
                }
            }
            return max + 1;
        }

        public void addEntry(IEntry entry) {
            mEntries.add(entry);
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
        }

        @Override
        public int getItemCount() {
            return mEntries.size();
        }

        class ViewHolder extends DragSortAdapter.ViewHolder implements View.OnLongClickListener, View.OnClickListener {
            public LinearLayout container;
            public ImageView imageView;
            public TextView textView;
            private ImageView handle;

            public ViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
                super(dragSortAdapter, itemView);
                container = (LinearLayout)itemView.findViewById(R.id.activity_settings_entries_container);
                imageView = (ImageView)itemView.findViewById(R.id.activity_settings_entries_image);
                textView = (TextView)itemView.findViewById(R.id.activity_settings_entries_text);
                handle = (ImageView)itemView.findViewById(R.id.activity_settings_entries_handle);

                //TODO: check if a long click or a tap event fits better
                handle.setOnLongClickListener(this);

                //TODO: attach tap event for opening the details activity / fragment (tablets?)
                container.setOnClickListener(this);
            }

            @Override
            public boolean onLongClick(View view) {
                startDrag();
                return true;
            }

            @Override
            public void onClick(View v) {
                IEntry entry = getEntryById(getItemId());
                Intent editIntent = entry.getEditIntent(v.getContext());
                if(editIntent != null) {
                    startActivity(editIntent);
                }
            }
        }
    }
}

package de.devmil.paperlaunch;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

public class SettingsActivity extends Activity {

    private EntriesAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private Button mButtonTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mRecyclerView = (RecyclerView)findViewById(R.id.activity_settings_entrieslist);
        mButtonTest = (Button)findViewById(R.id.activity_settings_buttontest);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayout.VERTICAL, false));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        LaunchConfig cfg = new LaunchConfig();

        List<IEntry> entries = new ArrayList<>();
        entries.add(Launch.create(this, cfg.getDesignConfig(), "com.agilebits.onepassword"));
        entries.add(Launch.create(this, cfg.getDesignConfig(), "org.kman.AquaMail"));
        entries.add(Launch.create(this, cfg.getDesignConfig(), "com.microsoft.office.onenote"));
        entries.add(Launch.create(this, cfg.getDesignConfig(), "com.spotify.music"));

        mRecyclerView.setAdapter(new EntriesAdapter(mRecyclerView, entries));

        mButtonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launchIntent = new Intent(SettingsActivity.this, TestDrive.class);
                startActivity(launchIntent);
            }
        });
    }

    private class EntriesAdapter extends DragSortAdapter<EntriesAdapter.ViewHolder>
    {
        private List<IEntry> mEntries;

        public EntriesAdapter(RecyclerView recyclerView, List<IEntry> entries) {
            super(recyclerView);
            mEntries = entries;
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
            holder.imageView.setImageDrawable(entry.getIcon(holder.imageView.getContext()));
            holder.textView.setText(entry.getName(holder.textView.getContext()));

            //FIXME: holder.container.setVisibility(getDraggingId() == itemId ? View.INVISIBLE : View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return mEntries.size();
        }

        class ViewHolder extends DragSortAdapter.ViewHolder implements View.OnLongClickListener
        {
            public ImageView imageView;
            public TextView textView;

            public ViewHolder(DragSortAdapter<?> dragSortAdapter, View itemView) {
                super(dragSortAdapter, itemView);
                imageView = (ImageView)itemView.findViewById(R.id.activity_settings_entries_image);
                textView = (TextView)itemView.findViewById(R.id.activity_settings_entries_text);

                imageView.setOnLongClickListener(this);
            }

            @Override
            public boolean onLongClick(View view) {
                startDrag();
                return true;
            }
        }
    }
}

package de.devmil.paperlaunch.view.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toolbar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import de.devmil.paperlaunch.R;

public class IntentSelector extends Activity {
	private static final String TAG = IntentSelector.class.getSimpleName();
	private static final int CREATE_SHORTCUT_REQUEST = 1;
	
	public static String EXTRA_SHORTCUT_TEXT = "de.devmil.common.extras.SHORTCUT_TEXT";
	public static String EXTRA_STRING_SHORTCUTS = "de.devmil.common.extras.STRING_SHORTCUTS";
	public static String EXTRA_STRING_ACTIVITIES = "de.devmil.common.extras.STRING_ACTIVITIES";
	
	private LinearLayout llWait;
	private ExpandableListView lvActivities;
    private CheckBox chkShowAllActivities;
	private ExpandableListView lvShortcuts;
	private TextView txtShortcuts;

    private Toolbar mToolbar;
	
	private Thread searchThread = null;
    private Object searchThreadLockObject = new Object();
	
	private List<IntentApplicationEntry> entries = new ArrayList<IntentApplicationEntry>();
	IntentSelectorAdapter adapterActivities;
	IntentSelectorAdapter adapterShortcuts;
	
	private void addResolveInfo(ResolveInfo ri, IntentApplicationEntry.IntentType intentType, boolean addAll) {
		IntentApplicationEntry newEntry;
		try {
			if(!addAll && !ri.activityInfo.exported)
				return;
			newEntry = new IntentApplicationEntry(IntentSelector.this, ri.activityInfo.packageName);
			if(!entries.contains(newEntry)) {
				entries.add(newEntry);
			} else {
				newEntry = entries.get(entries.indexOf(newEntry));
			}
			newEntry.addResolveInfo(ri, intentType);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Error while adding a package", e);
		}
	}
	
	protected static List<IntentApplicationEntry.IntentItem> getSubList(IntentApplicationEntry entry, IntentApplicationEntry.IntentType intentType) {
		switch(intentType) {
		case Main:
		case Launcher:
			return entry.getMainActivityIntentItems();
		case Shortcut:
			return entry.getShortcutIntentItems();
		}
		return null;
	}
	
	@SuppressWarnings("deprecation")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        synchronized (searchThreadLockObject)
        {
            if (searchThread != null) {
                searchThread.stop();
                searchThread = null;
            }
        }
		
		String shortcutText = "";
		if(getIntent().hasExtra(EXTRA_SHORTCUT_TEXT))
			shortcutText = getIntent().getStringExtra(EXTRA_SHORTCUT_TEXT);
		
		String shortcutLabel = "Shortcuts";
		if(getIntent().hasExtra(EXTRA_STRING_SHORTCUTS))
			shortcutLabel = getIntent().getStringExtra(EXTRA_STRING_SHORTCUTS);
		String activitiesLabel = "Shortcuts";
		if(getIntent().hasExtra(EXTRA_STRING_ACTIVITIES))
			activitiesLabel = getIntent().getStringExtra(EXTRA_STRING_ACTIVITIES);
		
		setContentView(R.layout.common__intentselectorview);
		
		llWait = (LinearLayout)findViewById(R.id.common__intentSelector_llWait);
//		progressWait = (ProgressBar)findViewById(R.id.intentSelector_progressWait);
		lvActivities = (ExpandableListView)findViewById(R.id.common__intentSelector_lvActivities);
        chkShowAllActivities = (CheckBox)findViewById(R.id.common__intentSelector_chkShowAllActivities);
		lvShortcuts = (ExpandableListView)findViewById(R.id.common__intentSelector_lvShortcuts);
		txtShortcuts = (TextView)findViewById(R.id.common__intentSelector_txtShortcuts);
        mToolbar = (Toolbar)findViewById(R.id.common__intentSelector_toolbar);

        setActionBar(mToolbar);
		
		txtShortcuts.setText(shortcutText);
		
		lvActivities.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Intent resultIntent = new Intent(Intent.ACTION_MAIN);
				IntentApplicationEntry.IntentItem entry = (IntentApplicationEntry.IntentItem)adapterActivities.getChild(groupPosition, childPosition);
				resultIntent.setClassName(entry.getPackageName(), entry.getActivityName());
				setResultIntent(resultIntent);
				return true;
			}
		});
        chkShowAllActivities.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                startSearch();
            }
        });
		lvShortcuts.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				Intent shortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
				IntentApplicationEntry.IntentItem entry = (IntentApplicationEntry.IntentItem)adapterShortcuts.getChild(groupPosition, childPosition);
				shortcutIntent.setClassName(entry.getPackageName(), entry.getActivityName());
				startActivityForResult(shortcutIntent, CREATE_SHORTCUT_REQUEST);
				return false;
			}
		});
		
		TabHost tabs = (TabHost) this.findViewById(android.R.id.tabhost);
		tabs.setup();
		TabSpec tspecActivities = tabs.newTabSpec(activitiesLabel);
		tspecActivities.setIndicator(activitiesLabel);
		tspecActivities.setContent(R.id.common__intentSelector_tabActivities);
		tabs.addTab(tspecActivities);
		TabSpec tspecShortcuts = tabs.newTabSpec(shortcutLabel);
		tspecShortcuts.setIndicator(shortcutLabel);
		if(shortcutText.equals("")) {
			tspecShortcuts.setContent(R.id.common__intentSelector_tabShortcuts);
			txtShortcuts.setVisibility(View.GONE);
		}
		else {
			tspecShortcuts.setContent(R.id.common__intentSelector_tabTextShortcuts);
			lvShortcuts.setVisibility(View.GONE);
		}
		tabs.addTab(tspecShortcuts);
		
		llWait.setVisibility(View.VISIBLE);

        startSearch();
	}

    private boolean stopThread;

    private void startSearch()
    {
        Thread searchThreadCopy = null;
        synchronized (searchThreadLockObject)
        {
            if (searchThread != null) {
                searchThreadCopy = searchThread;
                stopThread = true;
            }
        }

        if(searchThreadCopy != null)
        {
            try
            {
                searchThreadCopy.join();
            }
            catch(Exception e)
            {}
        }
        stopThread = false;
        entries.clear();
        searchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                IntentSelector.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        llWait.setVisibility(View.VISIBLE);
                    }
                });
                //this approach can kill the PackageManager if there are too many apps installed
                //				List<ResolveInfo> shortcutResolved = getPackageManager().queryIntentActivities(shortcutIntent, PackageManager.GET_ACTIVITIES | PackageManager.GET_INTENT_FILTERS);
                //				List<ResolveInfo> mainResolved = getPackageManager().queryIntentActivities(mainIntent, PackageManager.GET_ACTIVITIES | PackageManager.GET_INTENT_FILTERS);
                //				List<ResolveInfo> launcherResolved = getPackageManager().queryIntentActivities(launcherIntent, PackageManager.GET_ACTIVITIES | PackageManager.GET_INTENT_FILTERS);

                PackageManager pm = getPackageManager();

                List<ResolveInfo> shortcutResolved = new ArrayList<ResolveInfo>();
                List<ResolveInfo> mainResolved = new ArrayList<ResolveInfo>();
                List<ResolveInfo> launcherResolved = new ArrayList<ResolveInfo>();

                List<ApplicationInfo> appInfos = pm.getInstalledApplications(PackageManager.GET_META_DATA);

                boolean showAll = chkShowAllActivities.isChecked();

                for(ApplicationInfo appInfo : appInfos)
                {
                    synchronized (searchThreadLockObject)
                    {
                        if(stopThread)
                            return;
                    }
                    Intent shortcutIntent = new Intent(Intent.ACTION_CREATE_SHORTCUT);
                    shortcutIntent.setPackage(appInfo.packageName);

                    List<ResolveInfo> appShortcutResolved = pm.queryIntentActivities(shortcutIntent, PackageManager.GET_ACTIVITIES | PackageManager.GET_INTENT_FILTERS);
                    shortcutResolved.addAll(appShortcutResolved);

                    boolean addMainActivities = true;

                    if(showAll)
                    {
                        try
                        {
                            PackageInfo pi = pm.getPackageInfo(appInfo.packageName, PackageManager.GET_ACTIVITIES | PackageManager.GET_INTENT_FILTERS);

                            for(ActivityInfo ai : pi.activities)
                            {
                                ResolveInfo ri = new ResolveInfo();
                                ri.activityInfo = ai;

                                mainResolved.add(ri);
                            }

                            addMainActivities = false;
                        }
                        catch(Exception e)
                        {}
                    }

                    if(addMainActivities)
                    {
                        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
                        mainIntent.setPackage(appInfo.packageName);

                        List<ResolveInfo> appMainResolved = pm.queryIntentActivities(mainIntent, PackageManager.GET_ACTIVITIES | PackageManager.GET_INTENT_FILTERS);
                        mainResolved.addAll(appMainResolved);
                    }

                    Intent launcherIntent = new Intent(Intent.ACTION_MAIN);
                    launcherIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    launcherIntent.setPackage(appInfo.packageName);

                    List<ResolveInfo> appLauncherResolved = pm.queryIntentActivities(launcherIntent, PackageManager.GET_ACTIVITIES | PackageManager.GET_INTENT_FILTERS);
                    launcherResolved.addAll(appLauncherResolved);
                }

                for(ResolveInfo ri : shortcutResolved) {
                    addResolveInfo(ri, IntentApplicationEntry.IntentType.Shortcut, false);
                }
                for(ResolveInfo ri : mainResolved) {
                    addResolveInfo(ri, IntentApplicationEntry.IntentType.Main, showAll);
                }
                for(ResolveInfo ri : launcherResolved) {
                    addResolveInfo(ri, IntentApplicationEntry.IntentType.Launcher, false);
                }
                //sort
                IntentApplicationEntry[] entriesArray = entries.toArray(new IntentApplicationEntry[0]);
                Arrays.sort(entriesArray, new Comparator<IntentApplicationEntry>() {
					@Override
					public int compare(IntentApplicationEntry object1,
									   IntentApplicationEntry object2) {

						return object1.compareTo(object2);
					}
				});
                entries = new ArrayList<IntentApplicationEntry>(Arrays.asList(entriesArray));
                for(IntentApplicationEntry entry : entries)
                    entry.sort();
                //set
                IntentSelector.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapterActivities = new IntentSelectorAdapter(IntentSelector.this, entries, IntentApplicationEntry.IntentType.Main);
                        adapterShortcuts = new IntentSelectorAdapter(IntentSelector.this, entries, IntentApplicationEntry.IntentType.Shortcut);

                        lvActivities.setAdapter(adapterActivities);
                        lvShortcuts.setAdapter(adapterShortcuts);
                        llWait.setVisibility(View.GONE);
                    }
                });

                synchronized (searchThreadLockObject)
                {
                    searchThread = null;
                }
            }
        });
        searchThread.start();
    }
	
	private void setResultIntent(Intent intent) {
		setResult(Activity.RESULT_OK, intent);
		IntentSelector.this.finish();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == CREATE_SHORTCUT_REQUEST && resultCode == Activity.RESULT_OK) {
			setResultIntent(data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	class IntentSelectorAdapter extends BaseExpandableListAdapter {
		private List<IntentApplicationEntry> entries;
		private IntentApplicationEntry.IntentType intentType;
		private Context context;
		
		public IntentSelectorAdapter(Context context, List<IntentApplicationEntry> entries, IntentApplicationEntry.IntentType intentType) {
			this.context = context;
			this.intentType = intentType;
			this.entries = new ArrayList<IntentApplicationEntry>();
			for(IntentApplicationEntry entry : entries) {
				if(getSubList(entry).size() > 0)
					this.entries.add(entry);
			}
		}
		
		public List<IntentApplicationEntry.IntentItem> getSubList(IntentApplicationEntry entry) {
			return IntentSelector.getSubList(entry, intentType);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return getSubList(entries.get(groupPosition)).get(childPosition);
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return groupPosition * 1000 + childPosition;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.common__intentselectoritem, null);
			}
			TextView txt = (TextView)convertView.findViewById(R.id.common__intentselectoritem_text);
			TextView txtActivityName = (TextView)convertView.findViewById(R.id.common__intentselectoritem_activityName);
			
			txt.setText(getSubList(entries.get(groupPosition)).get(childPosition).getName());
			txtActivityName.setText(getSubList(entries.get(groupPosition)).get(childPosition).getActivityName());
			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return getSubList(entries.get(groupPosition)).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return entries.get(groupPosition);
		}

		@Override
		public int getGroupCount() {
			return entries.size();
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition * 1000;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			if(convertView == null) {
				convertView = LayoutInflater.from(context).inflate(R.layout.common__intentselectorgroup, null);
			}
			ImageView img = (ImageView)convertView.findViewById(R.id.common__intentselectorgroup_img);
			TextView txt = (TextView)convertView.findViewById(R.id.common__intentselectorgroup_text);
			
			txt.setText(entries.get(groupPosition).getName());
			Drawable appIcon = entries.get(groupPosition).getAppIcon();
			if(appIcon != null) {
				img.setImageDrawable(entries.get(groupPosition).getAppIcon());
				img.setVisibility(View.VISIBLE);
			} else {
				img.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
	}
}

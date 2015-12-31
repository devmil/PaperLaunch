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
package de.devmil.paperlaunch.view.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class IntentApplicationEntry implements Comparable<IntentApplicationEntry> {
	
	public enum IntentType {
		Main,
		Shortcut,
		Launcher
	}

	private Context context;
	
	private String packageName;
	
	private ApplicationInfo appInfo;
	private CharSequence appName;
	private Drawable appIcon = null;
	
	private List<IntentItem> intentItems = new ArrayList<IntentItem>();
	
	private List<IntentItem> mainActivityIntentItems;
	private List<IntentItem> shortcutIntentItems;
	
	public IntentApplicationEntry(Context context, String packageName) throws NameNotFoundException {
		this.context = context;
		this.packageName = packageName;
		
		appInfo = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_ACTIVITIES | PackageManager.GET_INTENT_FILTERS);
		appName = context.getPackageManager().getApplicationLabel(appInfo);
	}
	
	public void addResolveInfo(ResolveInfo info, IntentType intentType) {
		IntentItem newItem = new IntentItem(info);
		if(intentItems.contains(newItem))
			newItem = intentItems.get(intentItems.indexOf(newItem));
		else
			intentItems.add(newItem);
		switch(intentType) {
		case Main:
			newItem.setIsMainActivity(true);
			break;
		case Shortcut:
			newItem.setIsShortcut(true);
			break;
		case Launcher:
			newItem.setIsLauncher(true);
			break;
		}
		makeDirty();
	}
	
	public boolean hasShortcuts() {
		ensureSubCollections();
		return shortcutIntentItems.size() > 0;
	}

	public boolean hasMainActivities() {
		ensureSubCollections();
		return mainActivityIntentItems.size() > 0;
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public CharSequence getName() {
		return appName;
	}
	
	public Drawable getAppIcon() {
		if(appIcon == null)
			appIcon = context.getPackageManager().getApplicationIcon(appInfo);
		return appIcon;
	}

	@Override
	public int compareTo(IntentApplicationEntry another) {
		if(another == null)
			return 0;
		if(another == this)
			return 0;
		return appName.toString().compareToIgnoreCase(another.getName().toString());
	}
	
	public void extractItemsFromResolveInfo(ResolveInfo ri) {
		if(!ri.activityInfo.packageName.equals(packageName))
			return;
		if(!intentItems.contains(ri))
			intentItems.add(new IntentItem(ri));
		makeDirty();
	}
	
	private void makeDirty() {
		mainActivityIntentItems = null;
		shortcutIntentItems = null;
	}
	
	private void ensureSubCollections() {
		if(mainActivityIntentItems != null)
			return;
		mainActivityIntentItems = new ArrayList<IntentItem>();
		shortcutIntentItems = new ArrayList<IntentItem>();
		for(IntentItem ii : intentItems) {
			if(ii.isMainActivity())
				mainActivityIntentItems.add(ii);
			if(ii.isShortcut())
				shortcutIntentItems.add(ii);
		}
	}
	
	public void sort() {
		ensureSubCollections();
		Comparator<IntentItem> intentItemComparator = new Comparator<IntentItem>() {

			@Override
			public int compare(IntentItem object1, IntentItem object2) {
				return object1.compareTo(object2);
			}
		};

		IntentItem[] mainArray = mainActivityIntentItems.toArray(new IntentItem[0]);
		Arrays.sort(mainArray, intentItemComparator);
		mainActivityIntentItems = new ArrayList<IntentItem>(Arrays.asList(mainArray));
		
		IntentItem[] shortcutArray = shortcutIntentItems.toArray(new IntentItem[0]);
		Arrays.sort(shortcutArray, intentItemComparator);
		shortcutIntentItems = new ArrayList<IntentItem>(Arrays.asList(shortcutArray));
}
	
	public List<IntentItem> getMainActivityIntentItems() {
		ensureSubCollections();
		return mainActivityIntentItems;
	}

	public List<IntentItem> getShortcutIntentItems() {
		ensureSubCollections();
		return shortcutIntentItems;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null)
			return false;
		if(!IntentApplicationEntry.class.isAssignableFrom(o.getClass()))
			return false;
		return compareTo((IntentApplicationEntry)o) == 0;
	}
	
	@Override
	public int hashCode() {
		return appName.hashCode();
	}

	public class IntentItem implements Comparable<IntentItem> {
		private String packageName;
		private String activityName;
		private CharSequence name;
		boolean isShortcut = false;
		boolean isMain = false;
		boolean isLauncher = false;
		
		public IntentItem(ResolveInfo resolveInfo) {
			this.packageName = resolveInfo.activityInfo.packageName;
			this.activityName = resolveInfo.activityInfo.name;
			this.name = resolveInfo.activityInfo.loadLabel(context.getPackageManager());
		}
		
		public boolean isShortcut() {
			return isShortcut;
		}
		
		public void setIsShortcut(boolean isShortcut) {
			this.isShortcut = isShortcut;
		}
		
		public boolean isMainActivity() {
			return isMain;			
		}
		
		public void setIsMainActivity(boolean isMain) {
			this.isMain = isMain;
		}
		
		public boolean isLauncherActivity() {
			return isLauncher;
		}
		
		public void setIsLauncher(boolean isLauncher) {
			this.isLauncher = isLauncher;
			if(isLauncher)
				this.isMain = true;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o == null)
				return false;
			if(!IntentItem.class.isAssignableFrom(o.getClass()))
				return false;
			IntentItem other = (IntentItem)o;
			return other.packageName.equals(packageName) 
					&& other.activityName.equals(activityName);
		}

		@Override
		public int compareTo(IntentItem another) {
			if(another == null)
				return 0;
			if(another == this)
				return 0;
			return name.toString().compareTo(another.name.toString());
		}

		public String getName() {
			return name.toString() + (isLauncher ? " (Launcher)" : "");
		}
		
		public String getActivityName() {
			return activityName;
		}
		
		public String getPackageName() {
			return packageName;
		}
	}
}

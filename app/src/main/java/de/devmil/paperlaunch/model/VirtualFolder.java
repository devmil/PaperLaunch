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
package de.devmil.paperlaunch.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.List;

public class VirtualFolder implements IFolder {

    private String mName;
    private Drawable mIcon;
    private List<IEntry> mSubEntries;

    public VirtualFolder(String name, Drawable icon, List<IEntry> subEntries) {
        mName = name;
        mIcon = icon;
        mSubEntries = subEntries;
    }

    @Override
    public List<IEntry> getSubEntries() {
        return mSubEntries;
    }

    public void setSubEntries(List<IEntry> entries) {
        mSubEntries = entries;
    }

    @Override
    public long getId() {
        return -1;
    }

    @Override
    public long getEntryId() {
        return -1;
    }

    @Override
    public long getOrderIndex() {
        return -1;
    }

    @Override
    public String getName(Context context) {
        return mName;
    }

    @Override
    public Drawable getIcon(Context context) {
        return mIcon;
    }

    @Override
    public Drawable getFolderSummaryIcon(Context context) {
        return mIcon;
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public boolean useIconColor() {
        return false;
    }
}

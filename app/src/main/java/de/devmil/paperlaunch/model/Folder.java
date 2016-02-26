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
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.util.List;

import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.storage.EntryDTO;
import de.devmil.paperlaunch.storage.FolderDTO;

public class Folder implements IFolder {
    private FolderDTO mDto;
    private EntryDTO mEntryDto;
    private List<IEntry> mSubEntries;

    public Folder(FolderDTO folderDTO, EntryDTO entryDto, List<IEntry> subEntries) {
        mDto = folderDTO;
        mEntryDto = entryDto;
        mSubEntries = subEntries;
    }

    @Override
    public long getId() {
        return mDto.getId();
    }

    @Override
    public long getEntryId() {
        return mEntryDto.getId();
    }

    @Override
    public long getOrderIndex() {
        return mEntryDto.getOrderIndex();
    }

    @Override
    public String getName(Context context) {
        return mDto.getName();
    }

    @Override
    public Drawable getFolderSummaryIcon(Context context) {
        return context.getResources().getDrawable(R.mipmap.ic_folder_grey600_48dp, context.getTheme());
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public Drawable getIcon(Context context) {
        Drawable result = mDto.getIcon();
        if(result == null) {
            result = context.getDrawable(R.mipmap.folder_frame);
        }
        return result;
    }

    @Override
    public boolean useIconColor() {
        return true;
    }

    public List<IEntry> getSubEntries() {
        return mSubEntries;
    }

    public void setSubEntries(List<IEntry> entries) {
        mSubEntries = entries;
    }

    public FolderDTO getDto() {
        return mDto;
    }
}

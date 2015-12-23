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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import de.devmil.paperlaunch.storage.EntryDTO;
import de.devmil.paperlaunch.storage.LaunchDTO;
import de.devmil.paperlaunch.utils.AppMetadataUtils;

public class Launch implements IEntry {
    private LaunchDTO mDto;
    private EntryDTO mEntryDto;

    private String mDefaultAppName = null;
    private Drawable mDefaultAppIcon = null;

    public Launch(LaunchDTO launchDTO, EntryDTO entryDTO)
    {
        mDto = launchDTO;
        mEntryDto = entryDTO;
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
        if(mDto.getName() != null) {
            return mDto.getName();
        }
        if(mDefaultAppName == null) {
            Intent launchIntent = getLaunchIntent();
            if(launchIntent == null) {
                return null;
            }

            mDefaultAppName = AppMetadataUtils.getAppName(context, launchIntent);
        }
        return mDefaultAppName;
    }

    @Override
    public Drawable getIcon(Context context)
    {
        if(mDto.getIcon() != null) {
            return mDto.getIcon();
        }
        if(mDefaultAppIcon == null) {
            Intent launchIntent = getLaunchIntent();
            if(launchIntent != null) {
                mDefaultAppIcon = AppMetadataUtils.getAppIcon(context, launchIntent);
            }
        }
        return mDefaultAppIcon;
    }

    @Override
    public Drawable getFolderSummaryIcon(Context context) {
        return getIcon(context);
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public boolean useIconColor() {
        return false;
    }

    public Intent getLaunchIntent()
    {
        return mDto.getLaunchIntent();
    }

    public LaunchDTO getDto() {
        return mDto;
    }
}

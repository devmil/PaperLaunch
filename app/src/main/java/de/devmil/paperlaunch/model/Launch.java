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

import de.devmil.paperlaunch.storage.LaunchDTO;
import de.devmil.paperlaunch.utils.AppMetadataUtils;
import de.devmil.paperlaunch.view.activities.EditLaunchActivity;

public class Launch implements IEntry {
    private LaunchDTO mDto;

    private String mDefaultAppName = null;
    private Drawable mDefaultAppIcon = null;

    public Launch(LaunchDTO launchDTO)
    {
        mDto = launchDTO;
    }

    @Override
    public long getId() {
        return mDto.getId();
    }

    @Override
    public String getName(Context context) {
        if(mDto.getName() != null) {
            return mDto.getName();
        }
        if(mDefaultAppName == null) {
            if(getLaunchIntent() == null) {
                return null;
            }
            ComponentName componentName = getLaunchIntent().getComponent();
            mDefaultAppName = AppMetadataUtils.getAppName(context, componentName);
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
            mDefaultAppIcon = getAppIcon(context);
        }
        return mDefaultAppIcon;
    }

    public Drawable getAppIcon(Context context) {
        if(getLaunchIntent() == null) {
            return null;
        }
        ComponentName componentName = getLaunchIntent().getComponent();
        return AppMetadataUtils.getAppIcon(context, componentName);
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    @Override
    public Intent getEditIntent(Context context) {
        return EditLaunchActivity.createLaunchIntent(context, getDto());
    }

    public Intent getLaunchIntent()
    {
        return mDto.getLaunchIntent();
    }

    public LaunchDTO getDto() {
        return mDto;
    }

    public static Launch create(Context context, IDesignConfig designConfig, String packageName, String className, long id) {
        Intent launchIntent = new Intent();
        launchIntent.setComponent(new ComponentName(packageName, className));

        LaunchDTO dto = new LaunchDTO(id, null, launchIntent, null);
        return new Launch(dto);
    }
}

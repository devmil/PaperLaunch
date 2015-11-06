package de.devmil.paperlaunch.storage;

import android.content.Intent;
import android.graphics.drawable.Drawable;

public class LaunchDTO {

    private long mId;
    private String mName;
    private Intent mLaunchIntent;
    private Drawable mIcon;

    public LaunchDTO(long id, String name, Intent launchIntent, Drawable icon)
    {
        mId = id;
        mName = name;
        mLaunchIntent = launchIntent;
        mIcon = icon;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Intent getLaunchIntent() {
        return mLaunchIntent;
    }

    public void setLaunchIntent(Intent launchIntent) {
        this.mLaunchIntent = launchIntent;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }
}

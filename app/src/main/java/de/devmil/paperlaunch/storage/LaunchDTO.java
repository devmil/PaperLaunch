package de.devmil.paperlaunch.storage;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import de.devmil.paperlaunch.utils.Base64;
import de.devmil.paperlaunch.utils.BitmapUtils;
import de.devmil.paperlaunch.utils.IntentSerializer;

public class LaunchDTO {

    private static final String TAG = LaunchDTO.class.getSimpleName();

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

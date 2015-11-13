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

    private static final String KEY_ID              = "ID";
    private static final String KEY_NAME            = "NAME";
    private static final String KEY_LAUNCHINTENT    = "LAUNCHINTENT";
    private static final String KEY_ICON            = "ICON";

    public String serialize() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put(KEY_ID, getId());
            jsonObj.put(KEY_NAME, getName());
            jsonObj.put(KEY_LAUNCHINTENT, IntentSerializer.serialize(getLaunchIntent()));
            Drawable icon = getIcon();
            if(icon != null) {
                jsonObj.put(KEY_ICON, Base64.encodeBytes(BitmapUtils.getBytes(icon)));
            } else {
                jsonObj.put(KEY_ICON, "");
            }
        } catch (JSONException e) {
            return null;
        }
        return jsonObj.toString();
    }

    public static LaunchDTO deserialize(String serializedLaunch) {
        JSONObject jsonObj = null;
        try {
            jsonObj = new JSONObject(serializedLaunch);
            long id = jsonObj.getLong(KEY_ID);
            String name = jsonObj.optString(KEY_NAME, null);
            Intent launchIntent = IntentSerializer.deserialize(jsonObj.getString(KEY_LAUNCHINTENT));
            String iconString = jsonObj.optString(KEY_ICON, null);
            Drawable icon = null;
            if(!"".equals(iconString)) {
                icon = BitmapUtils.getIcon(Base64.decode(jsonObj.getString(KEY_ICON)));
            }
            return new LaunchDTO(id, name, launchIntent, icon);
        } catch (JSONException | IOException e) {
            Log.w(TAG, "Problem deserializing Launch", e);
        }
        return null;
    }
}

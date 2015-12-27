package de.devmil.paperlaunch.storage;

import android.graphics.drawable.Drawable;

public class FolderDTO {

    private long mId;
    private int mDepth;
    private String mName;
    private Drawable mIcon;

    public FolderDTO(long id, String name, Drawable icon, int depth)
    {
        mId = id;
        mDepth = depth;
        mName = name;
        mIcon = icon;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public int getDepth() {
        return mDepth;
    }

    public void setDepth(int depth) {
        mDepth = depth;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public Drawable getIcon() {
        return mIcon;
    }

    public void setIcon(Drawable icon) {
        this.mIcon = icon;
    }
}

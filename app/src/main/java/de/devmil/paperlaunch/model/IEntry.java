package de.devmil.paperlaunch.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

public interface IEntry {

    long getId();
    long getEntryId();
    long getOrderIndex();
    String getName(Context context);
    Drawable getIcon(Context context);
    Drawable getFolderSummaryIcon(Context context);
    boolean isFolder();
    boolean useIconColor();
}

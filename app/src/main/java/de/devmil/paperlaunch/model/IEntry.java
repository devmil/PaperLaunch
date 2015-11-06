package de.devmil.paperlaunch.model;

import android.content.Context;
import android.graphics.drawable.Drawable;

public interface IEntry {

    long getId();
    String getName(Context context);
    Drawable getIcon(Context context);
    boolean isFolder();
}

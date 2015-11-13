package de.devmil.paperlaunch.model;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import java.util.List;

import de.devmil.paperlaunch.storage.FolderDTO;

public class Folder implements IEntry {
    private FolderDTO mDto;
    private List<IEntry> mSubEntries;

    public Folder(FolderDTO folderDTO, List<IEntry> subEntries) {
        mDto = folderDTO;
        mSubEntries = subEntries;
    }

    @Override
    public long getId() {
        return mDto.getId();
    }

    @Override
    public String getName(Context context) {
        return mDto.getName();
    }

    @Override
    public boolean isFolder() {
        return true;
    }

    @Override
    public Drawable getIcon(Context context) {
        return mDto.getIcon();
    }

    @Override
    public Intent getEditIntent(Context context) {
        return null;
    }

    public List<IEntry> getSubEntries() {
        return mSubEntries;
    }

    public FolderDTO getDto() {
        return mDto;
    }
}

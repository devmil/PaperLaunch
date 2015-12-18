package de.devmil.paperlaunch.model;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.util.List;

import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.storage.EntryDTO;
import de.devmil.paperlaunch.storage.FolderDTO;

public class Folder implements IEntry {
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
        return mDto.getIcon();
    }

    public List<IEntry> getSubEntries() {
        return mSubEntries;
    }

    public FolderDTO getDto() {
        return mDto;
    }
}

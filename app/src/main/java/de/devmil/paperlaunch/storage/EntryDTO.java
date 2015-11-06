package de.devmil.paperlaunch.storage;

public class EntryDTO {
    private long mId;
    private long mOrderIndex;
    private long mLaunchId;
    private long mFolderId;
    private long mParentFolderId;

    public EntryDTO() {
    }

    public EntryDTO(long id, long orderIndex, long launchId, long folderId, long parentFolderId) {
        this.mId = id;
        this.mOrderIndex = orderIndex;
        this.mLaunchId = launchId;
        this.mFolderId = folderId;
        this.mParentFolderId = parentFolderId;
    }

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public long getOrderIndex() {
        return mOrderIndex;
    }

    public void setOrderIndex(long orderIndex) {
        this.mOrderIndex = orderIndex;
    }

    public long getLaunchId() {
        return mLaunchId;
    }

    public void setLaunchId(long launchId) {
        this.mLaunchId = launchId;
    }

    public long getFolderId() {
        return mFolderId;
    }

    public void setFolderId(long folderId) {
        this.mFolderId = folderId;
    }

    public long getParentFolderId() {
        return mParentFolderId;
    }

    public void setParentFolderId(long parentFolderId) {
        this.mParentFolderId = parentFolderId;
    }
}

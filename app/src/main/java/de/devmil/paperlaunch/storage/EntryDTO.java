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

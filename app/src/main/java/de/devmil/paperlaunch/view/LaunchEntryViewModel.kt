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
package de.devmil.paperlaunch.view

import android.content.Context
import android.graphics.drawable.Drawable

import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.config.ILaunchEntryConfig

class LaunchEntryViewModel(private val mContext: Context, val entry: IEntry, private val mConfig: ILaunchEntryConfig) {
    var state = State.Inactive

    @Suppress("unused")
    val appName: String
        get() = entry.name.orEmpty()

    val appIcon: Drawable?
        get() = entry.icon

    val imageWidthDip: Float
        get() = mConfig.imageWidthDip

    val imageMarginDip: Float
        get() = mConfig.imageMarginDip

    val entriesMarginDip: Float
        get() = mConfig.entriesMarginDip

    val imageElevationDip: Float
        get() = mConfig.lowElevationDip

    val frameDefaultColor: Int
        get() = mConfig.designConfig.frameDefaultColor

    val imageOffsetDip: Float
        get() = mConfig.imageOffsetDip

    enum class State constructor(private val mId: Int, private val mAnimationRefId: Int, private val mAnimationStateId: Int) {
        Inactive(0, -1, -1),
        Activating(1, 2, -1),
        Active(2, -1, 1),
        Focusing(3, 4, -1),
        Focused(4, -1, 3),
        Selected(5, -1, -1);

        fun isAnimationStateFor(other: State): Boolean {
            return mAnimationRefId == other.mId
        }

        fun hasAnimationState(): Boolean {
            return mAnimationStateId > -1
        }

        val animationState: State
            get() = getStateForId(mAnimationStateId)

        companion object {

            fun getStateForId(id: Int): State {
                if (Inactive.mId == id)
                    return Inactive
                if (Active.mId == id)
                    return Active
                if (Activating.mId == id)
                    return Activating
                if (Focusing.mId == id)
                    return Focusing
                if (Focused.mId == id)
                    return Focused
                if (Selected.mId == id)
                    return Selected
                return Inactive
            }
        }
    }

    val moveDuration: Int
        get() = mConfig.entryMoveAnimationDuration

    val alphaDuration: Int
        get() = mConfig.entryAlphaAnimationDuration

    val isOnRightSide: Boolean
        get() = mConfig.isOnRightSide

    companion object {

        fun createFrom(context: Context, entry: IEntry, config: ILaunchEntryConfig): LaunchEntryViewModel {
            return LaunchEntryViewModel(context, entry, config)
        }
    }
}

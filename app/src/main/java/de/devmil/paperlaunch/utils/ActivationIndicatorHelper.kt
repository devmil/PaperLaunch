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
package de.devmil.paperlaunch.utils

import android.graphics.Rect

object ActivationIndicatorHelper {

    fun calculateActivationIndicatorSize(
            sensitivity: Int,
            offsetPosition: Int,
            offsetSize: Int,
            isOnRightSide: Boolean,
            availableRect: Rect): Rect {

        var top = availableRect.top
        var left = availableRect.left
        var right = availableRect.right
        val bottom: Int

        if (isOnRightSide) {
            left = right - sensitivity
        } else {
            right = left + sensitivity
        }

        val height = (availableRect.height() * (offsetSize/100.0f)).toInt()

        // top += (center Y of the window) - (half the view height) + ( (position range) * position percentage)
        top += ((availableRect.height()/2) - (height / 2) + ( (availableRect.height()-height) * offsetPosition/100.0f) ).toInt()

        bottom = top + height

        val result = Rect(left, top, right, bottom)

        return result
    }
}

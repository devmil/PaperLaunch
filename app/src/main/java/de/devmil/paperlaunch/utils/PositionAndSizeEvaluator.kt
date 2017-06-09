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
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import java.io.InvalidClassException

/**
 * This class assumes that there is no initial margin and that the target view resides in a LinearLayout (or a derivative of it)
 * and that the endValue is the same as margins == 0
 */
class PositionAndSizeEvaluator @Throws(InvalidClassException::class)
constructor(private val mTargetView: View) : RectEvaluator() {

    init {
        mTargetView.layoutParams as? LinearLayout.LayoutParams ?: throw InvalidClassException("Only Views in a derivative of LinearLayout are supported!")

    }

    override fun evaluate(fraction: Float, startValue: Rect, endValue: Rect): Rect {
        val result = super.evaluate(fraction, startValue, endValue)

        val p = mTargetView.layoutParams as LinearLayout.LayoutParams

        val leftMargin = result.left - endValue.left
        val topMargin = result.top - endValue.top
        val rightMargin = endValue.right - result.right
        val bottomMargin = endValue.bottom - result.bottom
        p.setMargins(leftMargin, topMargin, rightMargin, bottomMargin)

        mTargetView.layoutParams = p

        return result
    }
}

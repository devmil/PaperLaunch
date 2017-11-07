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
package de.devmil.paperlaunch.view.widgets

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView

class VerticalTextView : TextView {
    private var topDown: Boolean = false

    constructor(context: Context) : super(context) {
        construct()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        construct()
    }

    private fun construct() {
        val gravity = gravity
        topDown = if (Gravity.isVertical(gravity) && gravity and Gravity.VERTICAL_GRAVITY_MASK == Gravity.BOTTOM) {
            setGravity(gravity and Gravity.HORIZONTAL_GRAVITY_MASK or Gravity.TOP)
            false
        } else
            true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        return super.setFrame(l, t, l + (b - t), t + (r - l))
    }

    override fun draw(canvas: Canvas) {
        if (topDown) {
            canvas.translate(height.toFloat(), 0f)
            canvas.rotate(90f)
        } else {
            canvas.translate(0f, width.toFloat())
            canvas.rotate(-90f)
        }
        @Suppress("DEPRECATION")
        canvas.clipRect(0f, 0f, width.toFloat(), height.toFloat(), android.graphics.Region.Op.REPLACE)
        super.draw(canvas)
    }
}
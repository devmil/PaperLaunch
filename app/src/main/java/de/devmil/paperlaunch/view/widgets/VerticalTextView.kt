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
import android.support.v7.widget.AppCompatTextView
import android.text.TextPaint
import android.util.AttributeSet


class VerticalTextView(context: Context, attrs: AttributeSet? = null) :
    AppCompatTextView(context, attrs) {
    private var vTopToDown = true
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(heightMeasureSpec, widthMeasureSpec)
        setMeasuredDimension(measuredHeight, measuredWidth)
    }

    override fun onDraw(canvas: Canvas) {
        val textPaint: TextPaint = paint
        textPaint.color = currentTextColor
        //Typeface typeface = ResourcesCompat.getFont(getContext(), R.font.ti_ti_lli_um_web_regular);
        //textPaint.setTypeface(typeface);
        textPaint.drawableState = drawableState
        canvas.save()
        if (vTopToDown) {
            canvas.translate(0f, height.toFloat())
            canvas.rotate(-90f)
        } else {
            canvas.translate(width.toFloat(), 0f)
            canvas.rotate(90f)
        }
        canvas.translate(compoundPaddingLeft.toFloat(), extendedPaddingTop.toFloat())
        layout.draw(canvas)
        canvas.restore()
    }

    init {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        if (isInEditMode) return
        vTopToDown = false //typedArray.getBoolean(R.styleable.VerticalTextView_vt_align_top_to_btm, true)
    }
}
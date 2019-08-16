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

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import de.devmil.paperlaunch.R
import de.devmil.paperlaunch.model.IEntry
import de.devmil.paperlaunch.view.utils.ViewUtils

object FolderImageHelper {

    fun createImageFromEntries(context: Context, entries: List<IEntry>, imgSizeDip: Float): Bitmap {

        val imgLayout = FrameLayout(context)

        val cols = 3
        val rows = 2

        val sizePx = ViewUtils.getPxFromDip(context, imgSizeDip).toInt()

        val offsetTopPx = ViewUtils.getPxFromDip(context, imgSizeDip / 4).toInt()
        val offsetBottomPx = ViewUtils.getPxFromDip(context, imgSizeDip / 4.2f).toInt()
        val offsetLeftPx = ViewUtils.getPxFromDip(context, imgSizeDip / 8).toInt()
        val offsetRightPx = ViewUtils.getPxFromDip(context, imgSizeDip / 8).toInt()

        val contentRect = Rect(offsetLeftPx, offsetTopPx, sizePx - offsetRightPx, sizePx - offsetBottomPx)

        val cellRect = Rect(0, 0, contentRect.width() / cols, contentRect.height() / rows)

        imgLayout.setBackgroundResource(R.mipmap.folder_frame)

        var idx = 0

        for (r in 0 until rows) {
            if (entries.size <= idx) {
                break
            }
            for (c in 0 until cols) {
                if (entries.size <= idx) {
                    break
                }

                val entry = entries[idx]

                val img = entry.folderSummaryIcon
                val entryImageView = ImageView(context)
                entryImageView.setImageDrawable(img)
                entryImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE

                entryImageView.maxHeight = cellRect.height()
                entryImageView.minimumHeight = cellRect.height()
                entryImageView.maxWidth = cellRect.width()
                entryImageView.minimumWidth = cellRect.width()

                val params = FrameLayout.LayoutParams(cellRect.width(), cellRect.height())
                params.setMargins(
                        contentRect.left + c * cellRect.width(),
                        contentRect.top + r * cellRect.height(),
                        0,
                        0)

                imgLayout.addView(entryImageView, params)

                idx++
            }
        }

        imgLayout.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED)
        imgLayout.layout(0, 0, sizePx, sizePx)

        val result = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(result)
        imgLayout.draw(canvas)

        return result
    }
}

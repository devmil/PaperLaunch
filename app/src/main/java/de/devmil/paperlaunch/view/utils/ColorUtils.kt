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
package de.devmil.paperlaunch.view.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.support.v7.graphics.Palette

import de.devmil.paperlaunch.utils.BitmapUtils

object ColorUtils {

    fun getBackgroundColorFromImage(bitmap: Bitmap, defaultColor: Int): Int {
        val p = Palette.from(bitmap).generate()

        return getColorFromPalette(p, defaultColor)
    }

    fun getBackgroundColorFromImage(drawable: Drawable, defaultColor: Int): Int {
        val bmpResult = BitmapUtils.drawableToBitmap(drawable)
        if (bmpResult == null) {
            return defaultColor
        }
        val p = Palette.from(bmpResult.bitmap).generate()

        if (bmpResult.isNew) {
            bmpResult.bitmap.recycle()
        }

        return getColorFromPalette(p, defaultColor)
    }

    private fun getColorFromPalette(palette: Palette, defaultColor: Int): Int {
        var result = palette.getLightMutedColor(Color.BLACK)
        if (result == Color.BLACK) {
            result = palette.getLightVibrantColor(Color.BLACK)
        }
        if (result == Color.BLACK) {
            result = palette.getMutedColor(Color.BLACK)
        }
        if (result == Color.BLACK) {
            result = palette.getVibrantColor(Color.BLACK)
        }
        if (result == Color.BLACK) {
            result = defaultColor
        }
        return result
    }

}

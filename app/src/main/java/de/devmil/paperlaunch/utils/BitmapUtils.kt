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
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import java.io.ByteArrayOutputStream

object BitmapUtils {

    fun getIcon(context: Context, rawData: ByteArray?): Drawable? {
        if (rawData == null) {
            return null
        }
        val bmp = BitmapFactory.decodeByteArray(rawData, 0, rawData.size)
        return BitmapDrawable(context.resources, bmp)
    }

    fun getBytes(drawable: Drawable?): ByteArray? {
        if (drawable == null) {
            return null
        }

        val bmpResult = drawableToBitmap(drawable)

        val stream = ByteArrayOutputStream()
        bmpResult!!.bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

        val byteArray = stream.toByteArray()

        if (bmpResult.isNew) {
            bmpResult.bitmap.recycle()
        }
        return byteArray
    }

    class BitmapResult(val bitmap: Bitmap, val isNew: Boolean)

    fun drawableToBitmap(drawable: Drawable?): BitmapResult? {
        if (drawable == null) {
            return null
        }
        if (drawable is BitmapDrawable) {
            return BitmapResult(drawable.bitmap, false)
        }

        val width = if (!drawable.bounds.isEmpty)
            drawable
                    .bounds.width()
        else
            drawable.intrinsicWidth

        val height = if (!drawable.bounds.isEmpty)
            drawable
                    .bounds.height()
        else
            drawable.intrinsicHeight

        val bitmap = Bitmap.createBitmap(if (width <= 0) 1 else width,
                if (height <= 0) 1 else height, Bitmap.Config.ARGB_8888)

        Log.v("Bitmap width - Height :", width.toString() + " : " + height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return BitmapResult(bitmap, true)
    }
}

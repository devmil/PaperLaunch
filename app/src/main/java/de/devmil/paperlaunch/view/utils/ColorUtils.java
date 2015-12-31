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
package de.devmil.paperlaunch.view.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.graphics.Palette;

import de.devmil.paperlaunch.utils.BitmapUtils;

public class ColorUtils {

    public static int getBackgroundColorFromImage(Bitmap bitmap, int defaultColor) {
        Palette p = Palette.from(bitmap).generate();

        return getColorFromPalette(p, defaultColor);
    }

    public static int getBackgroundColorFromImage(Drawable drawable, int defaultColor) {
        BitmapUtils.BitmapResult bmpResult = BitmapUtils.drawableToBitmap(drawable);
        if(bmpResult == null || bmpResult.getBitmap() == null) {
            return defaultColor;
        }
        Palette p = Palette.from(bmpResult.getBitmap()).generate();

        if(bmpResult.isNew()) {
            bmpResult.getBitmap().recycle();
        }

        return getColorFromPalette(p, defaultColor);
    }

    private static int getColorFromPalette(Palette palette, int defaultColor) {
        int result = palette.getLightMutedColor(Color.BLACK);
        if(result == Color.BLACK) {
            result = palette.getLightVibrantColor(Color.BLACK);
        }
        if(result == Color.BLACK) {
            result = palette.getMutedColor(Color.BLACK);
        }
        if(result == Color.BLACK) {
            result = palette.getVibrantColor(Color.BLACK);
        }
        if(result == Color.BLACK) {
            result = defaultColor;
        }
        return result;
    }

}

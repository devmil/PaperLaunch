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
package de.devmil.paperlaunch.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public abstract class BitmapUtils {

    public static Drawable getIcon(Context context, byte[] rawData) {
        if(rawData == null) {
            return null;
        }
        Bitmap bmp = BitmapFactory.decodeByteArray(rawData, 0, rawData.length);
        return new BitmapDrawable(context.getResources(), bmp);
    }

    public static byte[] getBytes(Drawable drawable) {
        if(drawable == null) {
            return null;
        }

        BitmapResult bmpResult = drawableToBitmap(drawable);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmpResult.getBitmap().compress(Bitmap.CompressFormat.PNG, 100, stream);

        byte[] byteArray = stream.toByteArray();

        if(bmpResult.isNew()) {
            bmpResult.getBitmap().recycle();
        }
        return byteArray;
    }

    public static class BitmapResult {
        private Bitmap mBitmap;
        private boolean mIsNew;

        public BitmapResult(Bitmap bitmap, boolean isNew) {
            mBitmap = bitmap;
            mIsNew = isNew;
        }

        public Bitmap getBitmap() {
            return mBitmap;
        }

        public boolean isNew() {
            return mIsNew;
        }

    }

    public static BitmapResult drawableToBitmap(Drawable drawable) {
        if(drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            return new BitmapResult(((BitmapDrawable) drawable).getBitmap(), false);
        }

        final int width = !drawable.getBounds().isEmpty() ? drawable
                .getBounds().width() : drawable.getIntrinsicWidth();

        final int height = !drawable.getBounds().isEmpty() ? drawable
                .getBounds().height() : drawable.getIntrinsicHeight();

        final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width,
                height <= 0 ? 1 : height, Bitmap.Config.ARGB_8888);

        Log.v("Bitmap width - Height :", width + " : " + height);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return new BitmapResult(bitmap, true);
    }
}

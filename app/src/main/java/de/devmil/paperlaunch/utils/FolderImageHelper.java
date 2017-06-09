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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

import de.devmil.paperlaunch.R;
import de.devmil.paperlaunch.model.IEntry;
import de.devmil.paperlaunch.view.utils.ViewUtils;

public class FolderImageHelper {

    public static Bitmap createImageFromEntries(Context context, List<IEntry> entries, float imgSizeDip) {

        FrameLayout imgLayout = new FrameLayout(context);

        int cols = 3;
        int rows = 2;

        int sizePx = (int) ViewUtils.INSTANCE.getPxFromDip(context, imgSizeDip);

        int offsetTopPx = (int) ViewUtils.INSTANCE.getPxFromDip(context, imgSizeDip / 4);
        int offsetBottomPx = (int) ViewUtils.INSTANCE.getPxFromDip(context, imgSizeDip / 4.2f);
        int offsetLeftPx = (int) ViewUtils.INSTANCE.getPxFromDip(context, imgSizeDip / 8);
        int offsetRightPx = (int) ViewUtils.INSTANCE.getPxFromDip(context, imgSizeDip / 8);

        Rect contentRect = new Rect(offsetLeftPx, offsetTopPx, sizePx - offsetRightPx, sizePx - offsetBottomPx);

        Rect cellRect = new Rect(0, 0, contentRect.width() / cols, contentRect.height() / rows);

        imgLayout.setBackgroundResource(R.mipmap.folder_frame);

        int idx = 0;

        for(int r=0; r<rows; r++) {
            if(entries.size() <= idx) {
                break;
            }
            for(int c=0; c<cols; c++) {
                if(entries.size() <= idx) {
                    break;
                }

                IEntry entry = entries.get(idx);

                Drawable img = entry.getFolderSummaryIcon(context);
                ImageView entryImageView = new ImageView(context);
                entryImageView.setImageDrawable(img);
                entryImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                entryImageView.setMaxHeight(cellRect.height());
                entryImageView.setMinimumHeight(cellRect.height());
                entryImageView.setMaxWidth(cellRect.width());
                entryImageView.setMinimumWidth(cellRect.width());

                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(cellRect.width(), cellRect.height());
                params.setMargins(
                        contentRect.left + c * cellRect.width(),
                        contentRect.top + r * cellRect.height(),
                        0,
                        0);

                imgLayout.addView(entryImageView, params);

                idx++;
            }
        }

        imgLayout.measure(
                View.MeasureSpec.UNSPECIFIED,
                View.MeasureSpec.UNSPECIFIED);
        imgLayout.layout(0, 0, sizePx, sizePx);

        Bitmap result = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(result);
        imgLayout.draw(canvas);

        return result;
    }
}

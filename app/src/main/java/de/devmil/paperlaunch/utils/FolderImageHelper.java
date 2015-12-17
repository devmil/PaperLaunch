package de.devmil.paperlaunch.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

import de.devmil.paperlaunch.model.IEntry;

public class FolderImageHelper {

    public static Bitmap createImageFromEntries(Context context, List<IEntry> entries, float imgSizeDip) {

        FrameLayout imgLayout = new FrameLayout(context);

        int sizePx = (int)ViewUtils.getPxFromDip(context, imgSizeDip);
        int entryImgSizePx = sizePx / 2;

        int marginOffset = entryImgSizePx / 3;
        int currentMargin = 0;

        for(IEntry entry : entries) {

            if(currentMargin > sizePx) {
                continue;
            }

            Drawable img = entry.getIcon(context);
            ImageView entryImageView = new ImageView(context);
            entryImageView.setImageDrawable(img);
            entryImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            entryImageView.setMaxHeight(entryImgSizePx);
            entryImageView.setMaxWidth(entryImgSizePx);

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(entryImgSizePx, entryImgSizePx);
            params.setMargins(currentMargin, currentMargin, 0, 0);

            imgLayout.addView(entryImageView, params);

            currentMargin += marginOffset;
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

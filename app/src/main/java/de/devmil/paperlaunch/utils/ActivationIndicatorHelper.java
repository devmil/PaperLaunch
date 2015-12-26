package de.devmil.paperlaunch.utils;

import android.graphics.Rect;

public abstract class ActivationIndicatorHelper {
    private ActivationIndicatorHelper() {
    }

    public static Rect calculateActivationIndicatorSize(
            int sensitivity,
            int offsetPosition,
            int offsetSize,
            boolean isOnRightSide,
            Rect availableRect) {

        int top = availableRect.top;
        int left = availableRect.left;
        int right = availableRect.right;
        int bottom = availableRect.bottom;

        if(isOnRightSide) {
            left = right - sensitivity;
        } else {
            right = left + sensitivity;
        }

        int height = availableRect.height() - offsetSize;
        top = top + offsetPosition + (offsetSize / 2);
        bottom = top + height;

        Rect result = new Rect(left, top, right, bottom);

        if(!result.intersect(availableRect)) {
            return availableRect;
        }

        return result;
    }
}

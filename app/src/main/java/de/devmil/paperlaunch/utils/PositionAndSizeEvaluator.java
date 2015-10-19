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

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.InvalidClassException;

/**
 * Created by michaellamers on 25.05.15.
 *
 * This class assumes that there is no initial margin and that the target view resides in a LinearLayout (or a derivative of it)
 * and that the endValue is the same as margins == 0
 */
public class PositionAndSizeEvaluator extends RectEvaluator {

    private View mTargetView;

    public PositionAndSizeEvaluator(View targetView) throws InvalidClassException {
        mTargetView = targetView;
        ViewGroup.LayoutParams params = mTargetView.getLayoutParams();

        if(!(params instanceof LinearLayout.LayoutParams)) {
            throw new InvalidClassException("Only Views in a derivative of LinearLayout are supported!");
        }
    }

    @Override
    public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
        Rect result = super.evaluate(fraction, startValue, endValue);

        LinearLayout.LayoutParams p = (LinearLayout.LayoutParams)mTargetView.getLayoutParams();

        int leftMargin = result.left - endValue.left;
        int topMargin = result.top - endValue.top;
        int rightMargin = endValue.right - result.right;
        int bottomMargin = endValue.bottom - result.bottom;
        p.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

        mTargetView.setLayoutParams(p);

        return result;
    }
}

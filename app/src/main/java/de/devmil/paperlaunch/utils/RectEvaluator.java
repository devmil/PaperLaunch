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

import android.animation.IntEvaluator;
import android.animation.TypeEvaluator;
import android.graphics.Rect;

/**
 * Created by michaellamers on 25.05.15.
 */
public class RectEvaluator implements TypeEvaluator<Rect> {
    @Override
    public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
        IntEvaluator intEvaluator = new IntEvaluator();

        int left = intEvaluator.evaluate(fraction, startValue.left, endValue.left);
        int top = intEvaluator.evaluate(fraction, startValue.top, endValue.top);
        int right = intEvaluator.evaluate(fraction, startValue.right, endValue.right);
        int bottom = intEvaluator.evaluate(fraction, startValue.bottom, endValue.bottom);

        return new Rect(left, top, right, bottom);
    }
}

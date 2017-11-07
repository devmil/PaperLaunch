/*
 * Copyright (C) 2015 Vincent Mi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.makeramen.dragsortadapter

import android.graphics.Point
import android.util.Log
import android.view.View

open class DragSortShadowBuilder(view: View, touchPoint: Point) : View.DragShadowBuilder(view) {

    private val touchPoint = Point()

    init {
        this.touchPoint.set(touchPoint.x, touchPoint.y)
    }

    override fun onProvideShadowMetrics(shadowSize: Point, shadowTouchPoint: Point) {
        val view = view
        if (view != null) {
            shadowSize.set(view.width, view.height)
            shadowTouchPoint.set(touchPoint.x, touchPoint.y)
        } else {
            Log.e(TAG, "Asked for drag thumb metrics but no view")
        }
    }

    companion object {

        val TAG = DragSortShadowBuilder::class.java.simpleName!!
    }
}

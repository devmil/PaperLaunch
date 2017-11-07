package com.makeramen.dragsortadapter

import android.graphics.Point
import android.graphics.PointF

internal class DragInfo(private val itemId: Long, shadowSize: Point, shadowTouchPoint: Point, private val dragPoint: PointF) {
    private val shadowSize: Point = Point(shadowSize)
    private val shadowTouchPoint: Point = Point(shadowTouchPoint)

    fun itemId(): Long {
        return itemId
    }

    fun shouldScrollLeft(): Boolean {
        return dragPoint.x < shadowTouchPoint.x
    }

    fun shouldScrollRight(parentWidth: Int): Boolean {
        return dragPoint.x > parentWidth - (shadowSize.x - shadowTouchPoint.x)
    }

    fun shouldScrollUp(): Boolean {
        return dragPoint.y < shadowTouchPoint.y
    }

    fun shouldScrollDown(parentHeight: Int): Boolean {
        return dragPoint.y > parentHeight - (shadowSize.y - shadowTouchPoint.y)
    }

    fun setDragPoint(x: Float, y: Float) {
        dragPoint.set(x, y)
    }
}

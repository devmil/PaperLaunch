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

import android.content.res.Resources
import android.graphics.Point
import android.graphics.PointF
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.view.MotionEvent
import android.view.View

abstract class DragSortAdapter<VH : DragSortAdapter.ViewHolder>(recyclerView: RecyclerView) : RecyclerView.Adapter<VH>() {

    private val scrollAmount = (2 * Resources.getSystem().displayMetrics.density).toInt()

    private val dragManager: DragManager
    private var scrollState = RecyclerView.SCROLL_STATE_IDLE
    private val lastTouchPoint = PointF() // used to create ShadowBuilder

    /**
     * You probably want to use this to set the currently dragging item to blank while it's being
     * dragged
     *
     * @return the id of the item currently being dragged or `RecyclerView.NO_ID ` if not being
     * dragged
     */
    val draggingId: Long
        get() = dragManager.draggingId

    init {
        @Suppress("LeakingThis")
        setHasStableIds(true)

        @Suppress("LeakingThis")
        dragManager = DragManager(recyclerView, this)
        recyclerView.setOnDragListener(dragManager)

        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                lastTouchPoint.set(e.x, e.y)
                return false
            }

            override fun onRequestDisallowInterceptTouchEvent(b: Boolean) {}

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
        })

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
                recyclerView!!.post { handleScroll(recyclerView) }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView?, newState: Int) {
                scrollState = newState
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> handleScroll(recyclerView)
                    RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
                    }
                }
            }
        })
    }

    /**
     * This should be reasonably performant as it gets called a lot on the UI thread.
     *
     * @return position of the item with the given id
     */
    abstract fun getPositionForId(id: Long): Int

    /**
     * This is called during the dragging event, the actual positions of the views and data need to
     * change in the adapter for the drag animations to look correct.
     *
     * @return true if the position can be moved from fromPosition to toPosition
     */
    abstract fun move(fromPosition: Int, toPosition: Int): Boolean

    /**
     * Called after a drop event, override to save changes after drop event.
     */
    fun onDrop() {}

    fun getLastTouchPoint(): PointF {
        return PointF(lastTouchPoint.x, lastTouchPoint.y)
    }

    private fun handleScroll(recyclerView: RecyclerView?) {
        if (scrollState != RecyclerView.SCROLL_STATE_IDLE) {
            return
        }
        val lastDragInfo = dragManager.lastDragInfo
        if (lastDragInfo != null) {
            handleDragScroll(recyclerView, lastDragInfo)
        }
    }

    internal fun handleDragScroll(rv: RecyclerView?, dragInfo: DragInfo) {
        if (rv!!.layoutManager.canScrollHorizontally()) {
            if (rv.canScrollHorizontally(-1) && dragInfo.shouldScrollLeft()) {
                rv.scrollBy(-scrollAmount, 0)
                dragManager.clearNextMove()
            } else if (rv.canScrollHorizontally(1) && dragInfo.shouldScrollRight(rv.width)) {
                rv.scrollBy(scrollAmount, 0)
                dragManager.clearNextMove()
            }
        } else if (rv.layoutManager.canScrollVertically()) {
            if (rv.canScrollVertically(-1) && dragInfo.shouldScrollUp()) {
                rv.scrollBy(0, -scrollAmount)
                dragManager.clearNextMove()
            } else if (rv.canScrollVertically(1) && dragInfo.shouldScrollDown(rv.height)) {
                rv.scrollBy(0, scrollAmount)
                dragManager.clearNextMove()
            }
        }
    }

    abstract class ViewHolder(private val adapter: DragSortAdapter<*>, itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun startDrag() {
            val touchPoint = adapter.getLastTouchPoint()
            val x = (touchPoint.x - itemView.x).toInt()
            val y = (touchPoint.y - itemView.y).toInt()

            startDrag(getShadowBuilder(itemView, Point(x, y)))
        }

        private fun getShadowBuilder(itemView: View, touchPoint: Point): View.DragShadowBuilder {
            return DragSortShadowBuilder(itemView, touchPoint)
        }

        fun startDrag(dragShadowBuilder: View.DragShadowBuilder) {
            val shadowSize = Point()
            val shadowTouchPoint = Point()
            dragShadowBuilder.onProvideShadowMetrics(shadowSize, shadowTouchPoint)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                itemView.startDragAndDrop(null, dragShadowBuilder,
                        DragInfo(itemId, shadowSize, shadowTouchPoint, adapter.getLastTouchPoint()), 0)
            } else {
                @Suppress("DEPRECATION")
                itemView.startDrag(null, dragShadowBuilder,
                        DragInfo(itemId, shadowSize, shadowTouchPoint, adapter.getLastTouchPoint()), 0)
            }

            adapter.notifyItemChanged(adapterPosition)
        }
    }
}

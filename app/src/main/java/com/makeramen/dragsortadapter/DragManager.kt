package com.makeramen.dragsortadapter

import android.graphics.PointF
import android.support.v7.widget.RecyclerView
import android.view.DragEvent
import android.view.View
import java.lang.ref.WeakReference

import java.lang.Float.MIN_VALUE

internal class DragManager(recyclerView: RecyclerView, private val adapter: DragSortAdapter<*>) : View.OnDragListener {

    private val recyclerViewRef: WeakReference<RecyclerView> = WeakReference(recyclerView)
    var draggingId = RecyclerView.NO_ID
        private set
    private val nextMoveTouchPoint = PointF(MIN_VALUE, MIN_VALUE)
    var lastDragInfo: DragInfo? = null
        private set

    override fun onDrag(v: View, event: DragEvent): Boolean {
        if (v !== recyclerViewRef.get() || event.localState !is DragInfo) {
            return false
        }
        val dragInfo = event.localState as DragInfo
        val itemId = dragInfo.itemId()

        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                draggingId = itemId
                adapter.notifyItemChanged(v.findViewHolderForItemId(itemId).adapterPosition)
            }

            DragEvent.ACTION_DRAG_LOCATION -> {
                val x = event.x
                val y = event.y

                val fromPosition = adapter.getPositionForId(itemId)
                var toPosition = -1

                val child = v.findChildViewUnder(event.x, event.y)
                if (child != null) {
                    toPosition = v.getChildViewHolder(child).adapterPosition
                }

                if (toPosition >= 0 && fromPosition != toPosition) {
                    val animator = v.itemAnimator

                    val scheduleNextMove = nextMoveTouchPoint.equals(MIN_VALUE, MIN_VALUE)
                    nextMoveTouchPoint.set(x, y)

                    if (scheduleNextMove)
                        animator.isRunning(RecyclerView.ItemAnimator.ItemAnimatorFinishedListener {
                            if (nextMoveTouchPoint.equals(MIN_VALUE, MIN_VALUE)) {
                                return@ItemAnimatorFinishedListener
                            }

                            val fromPos = adapter.getPositionForId(itemId)

                            val localChild = v
                                    .findChildViewUnder(nextMoveTouchPoint.x, nextMoveTouchPoint.y)
                            if (localChild != null) {
                                val toPos = v.getChildViewHolder(localChild).adapterPosition
                                if (adapter.move(fromPos, toPos)) {

                                    v.post { adapter.notifyItemMoved(fromPos, toPos) }
                                }
                            }

                            // reset so we know to schedule listener again next time
                            clearNextMove()
                        })
                }

                lastDragInfo = dragInfo
                lastDragInfo!!.setDragPoint(x, y)
                adapter.handleDragScroll(v, dragInfo)
            }

            DragEvent.ACTION_DRAG_ENDED -> {
                draggingId = RecyclerView.NO_ID
                lastDragInfo = null

                handleDragEnd(v, itemId)
            }

            DragEvent.ACTION_DROP -> adapter.onDrop()

            DragEvent.ACTION_DRAG_ENTERED -> {
            }
            DragEvent.ACTION_DRAG_EXITED -> {
            }
        }// probably not used?
        // TODO edge scrolling
        return true
    }

    private fun handleDragEnd(recyclerView: RecyclerView, itemId: Long) {
        // queue up the show animation until after all move animations are finished
        recyclerView.itemAnimator.isRunning {
            val position = adapter.getPositionForId(itemId)

            val vh = recyclerView.findViewHolderForItemId(itemId)
            // if positions don't match, there's still an outstanding move animation
            // so we try to reschedule the notifyItemChanged until after that
            if (vh != null && vh.adapterPosition != position) {
                recyclerView.post { handleDragEnd(recyclerView, itemId) }
            } else {
                adapter.notifyItemChanged(adapter.getPositionForId(itemId))
            }
        }
    }

    fun clearNextMove() {
        nextMoveTouchPoint.set(MIN_VALUE, MIN_VALUE)
    }
}

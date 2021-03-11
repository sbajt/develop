package com.scorealarm.meeting.rooms.list

import android.graphics.Rect
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt

class MeetingRoomItemDecoration : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        when (parent.getChildAdapterPosition(view)) {
            0 -> outRect.top = parent.getOffset()
            parent.adapter?.itemCount?.minus(1) -> outRect.bottom = parent.getOffset()
        }
    }

    private fun ViewGroup.getOffset(): Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        16f,
        this.resources.displayMetrics
    ).roundToInt()

}
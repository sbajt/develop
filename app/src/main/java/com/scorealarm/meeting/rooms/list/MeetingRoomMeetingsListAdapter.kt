package com.scorealarm.meeting.rooms.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.list.viewholders.MeetingViewHolder
import com.scorealarm.meeting.rooms.list.viewholders.MeetingsCountViewHolder
import com.scorealarm.meeting.rooms.list.viewholders.OngoingMeetingViewHolder
import com.scorealarm.meeting.rooms.models.Meeting

class MeetingRoomMeetingsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewType {
        MEETING, ONGOING_MEETING, MEETINGS_COUNT_BY_DAY
    }

    private val items = mutableListOf<Meeting>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ViewType.MEETINGS_COUNT_BY_DAY.ordinal -> MeetingsCountViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ongoing_meeting, parent, false)
            )
            ViewType.ONGOING_MEETING.ordinal -> OngoingMeetingViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ongoing_meeting, parent, false)
            )
            else -> MeetingViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ongoing_meeting, parent, false)
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MeetingViewHolder).bind(items[position])
    }

    override fun getItemViewType(position: Int): Int = items[position].type.ordinal

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    fun update(input: List<Meeting>, onEmptyList: () -> Unit = {}) {
        items.clear()
        items.addAll(input)
        notifyDataSetChanged()
        if (items.isEmpty())
            onEmptyList.invoke()
    }

}
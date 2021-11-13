package com.scorealarm.meeting.rooms.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.list.viewholders.MeetingViewHolder
import com.scorealarm.meeting.rooms.list.viewholders.MeetingsCountViewHolder
import com.scorealarm.meeting.rooms.list.viewholders.OngoingMeetingViewHolder
import com.scorealarm.meeting.rooms.models.MeetingItemViewModel

class MeetingRoomMeetingsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    enum class ViewType {
        MEETING, ONGOING_MEETING, MEETINGS_COUNT
    }

    private val items = mutableListOf<MeetingItemViewModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            ViewType.MEETINGS_COUNT.ordinal -> MeetingsCountViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_lable, parent, false)
            )
            ViewType.ONGOING_MEETING.ordinal -> OngoingMeetingViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_ongoing_meeting, parent, false)
            )
            else -> MeetingViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_meeting, parent, false)
            )
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (items[position].type) {
            ViewType.MEETING -> (holder as MeetingViewHolder).bind(items[position].meeting)
            ViewType.ONGOING_MEETING -> (holder as OngoingMeetingViewHolder).bind(items[position].meeting)
            ViewType.MEETINGS_COUNT -> (holder as MeetingsCountViewHolder).bind(items.size)
        }
    }

    override fun getItemViewType(position: Int): Int = items[position].type.ordinal

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    fun update(input: List<MeetingItemViewModel>) {
        items.clear()
        items.addAll(input)
        notifyDataSetChanged()
    }

}
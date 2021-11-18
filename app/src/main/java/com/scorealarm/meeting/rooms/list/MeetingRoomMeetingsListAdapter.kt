package com.scorealarm.meeting.rooms.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.list.viewholders.MeetingViewHolder
import com.scorealarm.meeting.rooms.models.MeetingItemViewModel

class MeetingRoomMeetingsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<MeetingItemViewModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MeetingViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_meeting, parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        (holder as MeetingViewHolder).bind(items[position])

    override fun getItemViewType(position: Int): Int = 0

    override fun getItemCount(): Int = items.size

    override fun getItemId(position: Int): Long = items[position].hashCode().toLong()

    fun update(input: List<MeetingItemViewModel>) {
        items.clear()
        items.addAll(input)
        notifyDataSetChanged()
    }

}
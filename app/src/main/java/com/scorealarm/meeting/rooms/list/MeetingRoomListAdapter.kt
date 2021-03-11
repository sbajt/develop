package com.scorealarm.meeting.rooms.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.models.MeetingRoom

class MeetingRoomListAdapter(private val actionListener: ListItemActionListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<MeetingRoom>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MeetingRoomViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false), actionListener
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MeetingRoomViewHolder).bind(items[position])
    }

    override fun getItemViewType(position: Int): Int = 0

    override fun getItemCount(): Int =
        items.size

    override fun getItemId(position: Int): Long =
        items[position].name.hashCode().toLong()

    fun update(input: List<MeetingRoom>) {
        items.clear()
        items.addAll(input)
        notifyDataSetChanged()
    }

}
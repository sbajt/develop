package com.scorealarm.meeting.rooms.list.viewholders

import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.list.ListItemActionListener
import com.scorealarm.meeting.rooms.models.MeetingRoom
import kotlinx.android.extensions.LayoutContainer

class MeetingRoomViewHolder(
    override val containerView: View,
    private val actionListener: ListItemActionListener<MeetingRoom>
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    private val textView: TextView = containerView.findViewById(android.R.id.text1)

    fun bind(meetingRoom: MeetingRoom) {
        textView.text = meetingRoom.name
        containerView.run {
            isClickable = true
            background = ContextCompat.getDrawable(context, R.drawable.bg_meeting_room)
            setOnClickListener {
                actionListener.onClick(meetingRoom)
                isClickable = false
            }
        }
    }

}
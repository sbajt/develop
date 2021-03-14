package com.scorealarm.meeting.rooms.list

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.models.MeetingRoom
import kotlinx.android.extensions.LayoutContainer

class MeetingRoomViewHolder(
    override val containerView: View,
    private val actionListener: MeetingRoomListItemActionListener
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    private val textView: TextView = containerView.findViewById(android.R.id.text1)

    fun bind(meetingRoom: MeetingRoom) {
        textView.text = meetingRoom.name
        containerView.run {
            isClickable = true
            setOnClickListener {
                actionListener.click(meetingRoom)
                containerView.isClickable = false
            }
        }
    }

}
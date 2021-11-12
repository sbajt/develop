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
    containerView: View,
    private val actionListener: ListItemActionListener<MeetingRoom>
) : RecyclerView.ViewHolder(containerView){

    private val textView: TextView = containerView.findViewById(android.R.id.text1)

    fun bind(meetingRoom: MeetingRoom) {
        textView.text = meetingRoom.name
        itemView.run {
            isClickable = true
            background = ContextCompat.getDrawable(context, android.R.color.white)
            setOnClickListener {
                actionListener.onClick(meetingRoom)
                isClickable = false
            }
        }
    }

}
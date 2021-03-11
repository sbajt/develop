package com.scorealarm.meeting.rooms.list

import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.models.MeetingRoom
import kotlinx.android.extensions.LayoutContainer

class MeetingRoomViewHolder(
    override val containerView: View,
    private val actionListener: ListItemActionListener
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    private val textView: TextView = containerView.findViewById(android.R.id.text1)

    fun bind(meetingRoom: MeetingRoom) {
        containerView.run {
            isClickable = true
            val typedValue = TypedValue()
            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                typedValue,
                true
            )
            setBackgroundResource(typedValue.resourceId)
        }
        textView.text = meetingRoom.name
        containerView.setOnClickListener {
            containerView.isClickable = false
            actionListener.onClick(meetingRoom.id)
        }
    }
}
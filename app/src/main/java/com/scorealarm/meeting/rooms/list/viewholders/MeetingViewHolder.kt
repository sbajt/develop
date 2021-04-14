package com.scorealarm.meeting.rooms.list.viewholders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.models.Meeting
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_meeting_list.*

class MeetingViewHolder(
    override val containerView: View,
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {


    fun bind(meeting: Meeting) {
        meeting.run {
            timeView?.text = "${startDateTime.toString("HH:mm")} - ${endDateTime.toString("HH:mm")}"
            meetingNameView?.text = title
            meetingOrganizerView?.text = organizer
            attendeeCountView?.text = invitesNumber.toString()
        }
    }

}
package com.scorealarm.meeting.rooms.list.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.models.Meeting
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_meeting_list.*
import org.joda.time.Period

class MeetingViewHolder(
    override val containerView: View,
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {


    fun bind(meeting: Meeting) {
        meeting.run {
            timeView?.text = if (Period(
                    meeting.startDateTime,
                    meeting.endDateTime
                ).days == 1
            ) "Meeting lasts all day"
            else "${meeting.startDateTime.toString("HH:mm")} - ${meeting.endDateTime.toString("HH:mm")}"
            meetingNameView?.text = title
            meetingOrganizerView?.text = organizer
            attendeeCountView?.text = invitesNumber.toString()
        }
    }

}
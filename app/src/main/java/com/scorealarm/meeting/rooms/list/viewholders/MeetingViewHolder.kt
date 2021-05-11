package com.scorealarm.meeting.rooms.list.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.MeetingStateType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.utils.Utils.state
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_meeting_list.*

class MeetingViewHolder(
    override val containerView: View,
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(meeting: Meeting) {
        meeting.run {
            if (meeting.state() != MeetingStateType.EXCLUDED) {
                if (meeting.state() == MeetingStateType.ALL_DAY)
                    timeView?.text = itemView.context.getText(R.string.meeting_state_all_day)
                else
                    timeView?.text = meeting.startDateTime.toString(
                        itemView.context.getText(R.string.meeting_time_format).toString()
                    ) +
                            " - ${
                                meeting.endDateTime.toString(
                                    itemView.context.getText(R.string.meeting_time_format)
                                        .toString()
                                )
                            }"
                meetingNameView?.text = title
                meetingOrganizerView?.text = organizer
                attendeeCountView?.text = invitesNumber.toString()
            } else {
                timeView?.text = ""
                meetingNameView?.text = ""
                meetingOrganizerView?.text = ""
                attendeeCountView?.text = ""
            }
        }
    }
}
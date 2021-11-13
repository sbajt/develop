package com.scorealarm.meeting.rooms.list.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.MeetingStateType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.utils.Utils.state
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_ongoing_meeting.*

class OngoingMeetingViewHolder(
    override val containerView: View,
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(meeting: Meeting?) {
        meeting?.run {
            if (meeting.state() == MeetingStateType.ALL_DAY)
                timeView?.text = itemView.context.getText(R.string.meeting_state_all_day)
            else
                timeView?.text =
                    itemView.context.getString(
                        R.string.label_meeting_time,
                        meeting.startDateTime.toString(itemView.context.getString(R.string.meeting_time_format)),
                        meeting.endDateTime.toString(itemView.context.getString(R.string.format_date_time_meeting))
                    )
            meetingNameView?.text = title
            meetingOrganizerView?.text = organizer
            invitesCountView?.text = invitesNumber.toString()
        }
    }
}
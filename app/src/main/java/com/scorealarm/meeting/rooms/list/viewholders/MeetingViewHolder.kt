package com.scorealarm.meeting.rooms.list.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.models.MeetingItemViewModel
import com.scorealarm.meeting.rooms.models.types.MeetingStateType
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.item_meeting.*


class MeetingViewHolder(
    override val containerView: View,
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    fun bind(meetingItemViewModel: MeetingItemViewModel?) {
        meetingItemViewModel?.run {
            if ( this.type == MeetingStateType.EXCLUDED) {
                timeView?.text = ""
                meetingNameView?.text = ""
                meetingOrganizerView?.text = ""
                invitesCountView?.text = ""
            } else {
                if (meetingItemViewModel.type == MeetingStateType.ALL_DAY)
                    timeView?.text = itemView.context.getText(R.string.meeting_state_all_day)
                else
                    timeView?.text = meetingItemViewModel.meeting?.startDateTime?.toString(itemView.context.getText(R.string.meeting_time_format).toString()) +
                            " - ${meetingItemViewModel.meeting?.endDateTime?.toString(itemView.context.getText(R.string.meeting_time_format).toString())
                    }"
                meetingNameView?.text = meeting?.title
                meetingOrganizerView?.text = meeting?.organizer
                invitesCountView?.text = meeting?.invitesNumber?.toString()
            }
        }
    }
}
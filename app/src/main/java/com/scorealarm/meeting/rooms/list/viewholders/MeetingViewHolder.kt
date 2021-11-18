package com.scorealarm.meeting.rooms.list.viewholders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.models.MeetingItemViewModel
import com.scorealarm.meeting.rooms.models.types.MeetingStateType
import com.scorealarm.meeting.rooms.utils.Utils.getTimeString
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
                timeView?.text = meeting.getTimeString(itemView.context)
                meetingNameView?.text = meeting?.title
                meetingOrganizerView?.text = meeting?.organizer
                invitesCountView?.text = meeting?.invitesNumber?.toString()
            }
        }
    }
}
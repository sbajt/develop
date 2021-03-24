package com.scorealarm.meeting.rooms.list

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.models.Meeting
import kotlinx.android.extensions.LayoutContainer

class MeetingViewHolder(
    override val containerView: View,
) : RecyclerView.ViewHolder(containerView),
    LayoutContainer {

    private val titleView: TextView? = containerView.findViewById(R.id.titleView)
    private val subtitleView: TextView? = containerView.findViewById(R.id.subtitleView)
    private val attendeeCountView: TextView? = containerView.findViewById(R.id.attendeeCountView)

    fun bind(meeting: Meeting) {
        meeting.run {
            titleView?.text = title
            subtitleView?.text =
                "${startDateTime.toString("HH:mm")} - ${endDateTime.toString("HH:mm")}"
            attendeeCountView?.text = invitesNumber.toString()
        }
    }

}
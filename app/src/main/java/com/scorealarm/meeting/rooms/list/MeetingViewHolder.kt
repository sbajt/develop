package com.scorealarm.meeting.rooms.list

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.models.Meeting
import kotlinx.android.extensions.LayoutContainer

class MeetingViewHolder(
    override val containerView: View,
    private val actionListener: MeetingListItemActionListener
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
        containerView.run {
            DrawableCompat.wrap(background)
            DrawableCompat.setTint(
                background,
                Color.parseColor("#33111111")
            )
            isClickable = true
            setOnClickListener {
                actionListener.click(meeting.id)
                isClickable = false
            }
        }
    }

}
package com.scorealarm.meeting.rooms.list.viewholders

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.scorealarm.meeting.rooms.R

class MeetingsCountViewHolder(containerView: View) : RecyclerView.ViewHolder(containerView) {

    private val textView: TextView = containerView.findViewById(R.id.textView)

    fun bind(meetingsCount: Int) {
        when (meetingsCount) {
            0 -> textView.run {
                setText(R.string.label_meetings_empty_list)
            }
            1 -> textView.run {
                textView.setText(R.string.label_meetings_no_next)
            }
            else -> textView.run {
                setText(R.string.label_meetings_next)
            }
        }
    }
}
package com.scorealarm.meeting.rooms.utils

import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.net.Network
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.scorealarm.meeting.rooms.MeetingStateType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.models.Meeting
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period


object Utils {

    private val TAG = Utils::class.java.canonicalName
    private val internetConnectivityStateSubject = BehaviorSubject.createDefault(false)

    fun destroy() {
        internetConnectivityStateSubject.onComplete()
    }

    fun Meeting?.state(): MeetingStateType {
        return when {
            this == null -> MeetingStateType.EXCLUDED
            Period(startDateTime, endDateTime).days == 1 -> MeetingStateType.ALL_DAY
            Interval(startDateTime, endDateTime).containsNow() -> MeetingStateType.ONGOING
            startDateTime.withTimeAtStartOfDay().millis ==
                    DateTime.now().withTimeAtStartOfDay().millis ->
                MeetingStateType.INCLUDED
            else -> MeetingStateType.EXCLUDED
        }
    }

    fun Meeting?.stateText(context: Context?, meeting: Meeting) = when (this.state()) {
        MeetingStateType.ALL_DAY -> context?.getString(R.string.meeting_state_all_day)
        MeetingStateType.ONGOING -> "${meeting.startDateTime.toString("HH:mm")} - ${
            meeting.endDateTime.toString("HH:mm")
        }"
        MeetingStateType.INCLUDED,
        MeetingStateType.EXCLUDED -> ""
    }

    fun List<Meeting>?.filterUpcoming(): List<Meeting> {
        val todayMeetingList = this?.filter { it.state() != MeetingStateType.EXCLUDED }
        return when {
            todayMeetingList.isNullOrEmpty() -> emptyList()
            todayMeetingList.any { it.startDateTime.isAfterNow } -> todayMeetingList.filter { it.startDateTime.isAfterNow }
            else -> emptyList()
        }
    }

    fun List<Meeting>?.filterToday(): List<Meeting> {
        return this?.filter { it.state() != MeetingStateType.EXCLUDED } ?: emptyList()
    }

    fun TextView.styleAndSetText(meetingList: List<Meeting>?) {
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        when {
            meetingList.isNullOrEmpty() -> {
                layoutParams.topMargin =
                    resources.getDimensionPixelSize(R.dimen.meeting_room_description_space_big)
                typeface = Typeface.create("poppins", Typeface.BOLD)
                setText(R.string.label_meetings_empty_list)
            }
            meetingList.any { it.state() == MeetingStateType.ALL_DAY || it.state() == MeetingStateType.ONGOING } ||
                    meetingList.filterUpcoming().isEmpty() -> {
                layoutParams.topMargin =
                    resources.getDimensionPixelSize(R.dimen.meeting_room_description_space_big)
                typeface = Typeface.create("poppins", Typeface.BOLD)
                setText(R.string.label_meetings_no_next)
            }
            else -> {
                layoutParams.topMargin =
                    resources.getDimensionPixelSize(R.dimen.meeting_room_description_space_small)
                typeface = Typeface.create("poppins", Typeface.NORMAL)
                setText(R.string.label_meetings_next)
            }
        }
        this.layoutParams = layoutParams
    }

    fun TextView.setText(meeting: Meeting?, text: String?) =
        if (meeting == null || text.isNullOrBlank()) this.text = ""
        else this.text = text

    fun observeInternetConnectivity(context: Context): Observable<Boolean> {
        observeInternetConnectivityState(context)
        return internetConnectivityStateSubject
    }

    private fun observeInternetConnectivityState(context: Context?) {
        val cm = (context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager)
        if (cm == null)
            internetConnectivityStateSubject.onNext(false)

        cm?.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                internetConnectivityStateSubject.onNext(true)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                internetConnectivityStateSubject.onNext(false)
            }

        })
    }

}
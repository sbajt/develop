package com.scorealarm.meeting.rooms.utils

import com.scorealarm.meeting.rooms.MeetingStateType
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period


object Utils {

    private val TAG = Utils::class.java.canonicalName

    fun MeetingRoom.updateMeetings(meetingList: List<Meeting>) =
        this.copy(meetingList = meetingList)

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

    fun List<Meeting>?.filterUpcoming(): List<Meeting> {
        val todayMeetingList = this?.filter { it.state() != MeetingStateType.EXCLUDED }
        return when {
            todayMeetingList.isNullOrEmpty() -> emptyList()
            todayMeetingList.any { it.startDateTime.isAfterNow } -> todayMeetingList.filter { it.startDateTime.isAfterNow }
            else -> emptyList()
        }
    }

}
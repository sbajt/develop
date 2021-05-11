package com.scorealarm.meeting.rooms.utils

import com.scorealarm.meeting.rooms.MeetingStateType
import com.scorealarm.meeting.rooms.models.Meeting
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period

object Utils {

    fun Meeting?.state(): MeetingStateType {
        val meetingInterval = Interval(
            this?.startDateTime,
            this?.endDateTime
        )
        return when {
            this == null -> MeetingStateType.EXCLUDED
            this.startDateTime.toDate() == DateTime.now().toDate() -> MeetingStateType.INCLUDED
            Period(this.startDateTime, this.endDateTime).days == 1 -> MeetingStateType.ALL_DAY
            meetingInterval.containsNow() -> MeetingStateType.ONGOING
            else -> MeetingStateType.EXCLUDED
        }
    }

    fun List<Meeting>?.filterToday(): List<Meeting> {
        val todayMeetingList = this?.filter { it.state() != MeetingStateType.EXCLUDED }
        return when {
            todayMeetingList.isNullOrEmpty() -> emptyList()
            todayMeetingList.any { it.state() == MeetingStateType.ALL_DAY } -> listOf(
                todayMeetingList.first { it.state() == MeetingStateType.ALL_DAY })
            else -> todayMeetingList
        }
    }

    fun Meeting?.isToday(): Boolean =
        this.state() != MeetingStateType.EXCLUDED
}
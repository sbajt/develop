package com.scorealarm.meeting.rooms.utils

import com.scorealarm.meeting.rooms.MeetingStateType
import com.scorealarm.meeting.rooms.models.Meeting
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period

object Utils {

    fun Meeting?.state(): MeetingStateType {
        return when {
            this == null -> MeetingStateType.EXCLUDED
            Interval(
                DateTime.now().withTimeAtStartOfDay(),
                DateTime.now().withTimeAtStartOfDay().plusDays(1)
            )
                .contains(this.startDateTime) ||
                    this.startDateTime == DateTime.now()
                .withTimeAtStartOfDay() -> MeetingStateType.INCLUDED
            Interval(
                this.startDateTime,
                this.endDateTime
            ).containsNow() -> MeetingStateType.ONGOING
            else -> MeetingStateType.EXCLUDED
        }
    }

    fun Meeting?.isTodayAllDay() =
        Period(this?.startDateTime, this?.endDateTime).days == 1

    fun List<Meeting>?.filterToday(): List<Meeting> {
        val todayMeetingList = this?.filter { it.state() != MeetingStateType.EXCLUDED }
        return if (todayMeetingList.isNullOrEmpty())
            emptyList()
        else {
            if (todayMeetingList.any { it.isTodayAllDay() })
                listOf(todayMeetingList.single { it.isTodayAllDay() })
            else
                todayMeetingList
        }
    }

}
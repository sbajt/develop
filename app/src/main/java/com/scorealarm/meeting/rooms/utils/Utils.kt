package com.scorealarm.meeting.rooms.utils

import com.scorealarm.meeting.rooms.MeetingStateType
import com.scorealarm.meeting.rooms.list.MeetingRoomMeetingsListAdapter
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingItemViewModel
import com.scorealarm.meeting.rooms.models.MeetingRoom
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period


object Utils {

    private val TAG = Utils::class.java.canonicalName

    fun MeetingRoom.updateMeetingsListForRoom(meetingList: List<Meeting>) =
        this.copy(meetingList = meetingList)

    fun Meeting?.state(): MeetingStateType =
        when {
            this == null -> MeetingStateType.EXCLUDED
            Period(startDateTime, endDateTime).days == 1 -> MeetingStateType.ALL_DAY
            Interval(startDateTime, endDateTime).containsNow() -> MeetingStateType.ONGOING
            startDateTime.withTimeAtStartOfDay() == DateTime.now()
                .withTimeAtStartOfDay() -> MeetingStateType.UPCOMING
            startDateTime.withTimeAtStartOfDay().millis ==
                    DateTime.now().withTimeAtStartOfDay().millis ->
                MeetingStateType.INCLUDED
            else -> MeetingStateType.EXCLUDED
        }

    fun MeetingItemViewModel?.state(): MeetingStateType =
        this?.meeting?.state() ?: MeetingStateType.EXCLUDED

    fun List<Meeting>?.mapToMeetingItemViewModelList(): List<MeetingItemViewModel> {
        val list = this.filterUpcoming()
        when {
            list.any { it.state() == MeetingStateType.ONGOING } -> list + createMeetingsCountItemViewModel(list.size)
            list.any { it.state() != MeetingStateType.ONGOING } -> listOf(createMeetingsCountItemViewModel(list.size)) + list
            else -> listOf(createMeetingsCountItemViewModel(0))
        }
        return list
    }

    private fun List<Meeting>?.filterUpcoming(): List<MeetingItemViewModel> {
        val upcomingMeetingList = this?.filter { it.state() == MeetingStateType.UPCOMING }
        return upcomingMeetingList?.map {
            MeetingItemViewModel(
                type = MeetingRoomMeetingsListAdapter.ViewType.MEETING,
                meeting = it,
                meetingCount = upcomingMeetingList.size
            )
        } ?: emptyList()
    }

    private fun createMeetingsCountItemViewModel(count: Int): MeetingItemViewModel =
        MeetingItemViewModel(
            type = MeetingRoomMeetingsListAdapter.ViewType.MEETINGS_COUNT,
            meetingCount = count
        )
}

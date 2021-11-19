package com.scorealarm.meeting.rooms.utils

import android.content.Context
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.fragments.models.MeetingListViewModel
import com.scorealarm.meeting.rooms.fragments.models.MeetingRoomDescriptionViewModel
import com.scorealarm.meeting.rooms.fragments.models.MeetingRoomListViewModel
import com.scorealarm.meeting.rooms.fragments.models.OngoingMeetingViewModel
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingItemViewModel
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.models.types.MeetingStateType
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period


object Utils {

    private val TAG = Utils::class.java.canonicalName

    fun createMeetingRoomDescriptionViewModel(meetingRoom: MeetingRoom): MeetingRoomDescriptionViewModel =
        MeetingRoomDescriptionViewModel(meetingRoom)

    fun createOngoingMeetingViewModel(
        context: Context?,
        meeting: Meeting?
    ): OngoingMeetingViewModel =
        OngoingMeetingViewModel(
            meeting = meeting,
            meetingTimeText = if (meeting.state() == MeetingStateType.ALL_DAY)
                context?.getString(R.string.meeting_state_all_day)
            else
                context?.getString(
                    R.string.label_meeting_time,
                    meeting?.startDateTime?.toString(context.getString(R.string.meeting_time_format)),
                    meeting?.endDateTime?.toString(context.getString(R.string.format_date_time_meeting))
                )
        )

    fun createMeetingRoomListViewModel(meetingRooms: List<MeetingRoom>?): MeetingRoomListViewModel =
        MeetingRoomListViewModel(
            meetingRoomList = meetingRooms ?: emptyList()
        )

    fun createMeetingListItemViewModel(
        context: Context?,
        meetingList: List<Meeting>?
    ): MeetingListViewModel {
        val meetingList =
            meetingList?.filter { it.state() == MeetingStateType.UPCOMING }
        return MeetingListViewModel(
            meetingList = meetingList,
            labelData = meetingList.getLabelData(context, meetingList?.getOngoingMeeting() != null),
        )
    }

    fun mapToMeetingItemViewModel(meetings: List<Meeting>?): List<MeetingItemViewModel> =
        meetings?.filter { it.startDateTime.isAfterNow }
            ?.map {
                MeetingItemViewModel(
                    type = it.state(),
                    meeting = it
                )
            } ?: emptyList()


    fun List<Meeting>?.getOngoingMeeting(): Meeting? =
        this?.find { it.state() == MeetingStateType.ONGOING || it.state() == MeetingStateType.ALL_DAY }

    fun List<Meeting>?.updateMeetingRoom(meetingRoom: MeetingRoom): MeetingRoom =
        meetingRoom.copy(meetingList = this)

    fun Meeting?.getTimeString(context: Context?) = this?.run {
        if (this.state() == MeetingStateType.ALL_DAY)
            context?.getString(R.string.meeting_state_all_day)
        else
            context?.getString(
                R.string.label_meeting_time,
                this.startDateTime.toString(context.getString(R.string.meeting_time_format)),
                this.endDateTime.toString(context.getString(R.string.meeting_time_format))
            )
    }


    private fun List<Meeting>?.getLabelData(
        context: Context?,
        hasOngoing: Boolean?
    ): Pair<String?, Int?> =
        when {
            this.isNullOrEmpty() -> Pair(
                context?.getString(R.string.label_meetings_no_today),
                R.style.textAppearanceMeetingCountBold
            )
            this.size == 1 && hasOngoing == true -> Pair(
                context?.getString(R.string.label_meetings_no_next),
                R.style.textAppearanceMeetingCountBold
            )
            this.size == 1 && hasOngoing == false || this.size > 1 -> Pair(
                context?.getString(R.string.label_meetings_next),
                R.style.textAppearanceMeetingCountNormal
            )
            else -> Pair(null, null)
        }

    private fun Meeting?.state(): MeetingStateType =
        when {
            this == null
                    || !Interval(
                DateTime.now()
                    .withTimeAtStartOfDay(),
                DateTime.now()
                    .withTimeAtStartOfDay()
                    .plusDays(1)
            ).contains(this.startDateTime)
            -> MeetingStateType.EXCLUDED
            Period(startDateTime, endDateTime).days == 1
            -> MeetingStateType.ALL_DAY
            Interval(startDateTime, endDateTime).containsNow()
            -> MeetingStateType.ONGOING
            startDateTime.withTimeAtStartOfDay().millis ==
                    DateTime.now()
                        .withTimeAtStartOfDay().millis
                    && startDateTime.isAfterNow
            -> MeetingStateType.UPCOMING
            else -> MeetingStateType.EXCLUDED
        }


}




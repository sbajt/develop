package com.scorealarm.meeting.rooms.utils

import android.content.Context
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.fragments.models.MeetingRoomDescriptionViewModel
import com.scorealarm.meeting.rooms.fragments.models.MeetingRoomListViewModel
import com.scorealarm.meeting.rooms.fragments.models.MeetingRoomMeetingListViewModel
import com.scorealarm.meeting.rooms.fragments.models.OngoingMeetingViewModel
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingItemViewModel
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.models.types.LabelType
import com.scorealarm.meeting.rooms.models.types.MeetingStateType
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period


object Utils {

    private val TAG = Utils::class.java.canonicalName

    fun createMeetingRoomListViewModel(meetingRooms: List<MeetingRoom>?): MeetingRoomListViewModel =
        MeetingRoomListViewModel(
            meetingRoomList = meetingRooms ?: emptyList()
        )

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

    fun createMeetingsItemViewModelList(meetingList: List<Meeting>?): MeetingRoomMeetingListViewModel {
        val meetingList =
            meetingList?.filter { it.state() != MeetingStateType.ONGOING || it.state() != MeetingStateType.ALL_DAY }
        return MeetingRoomMeetingListViewModel(
            meetingList = meetingList,
            labelType = meetingList.labelType()
        )
    }

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

    fun LabelType?.labelTypeToLabel(context: Context?) =
        when (this) {
            LabelType.NONE_UPCOMING -> context?.getString(R.string.label_meetings_no_today)
            LabelType.NONE_TODAY -> context?.getString(R.string.label_meetings_no_next)
            LabelType.HAS_UPCOMING -> context?.getString(R.string.label_meetings_next)
            else -> context?.getString(R.string.label_meetings_no_today)
        }

    fun mapToMeetingItemViewModel(meetings: List<Meeting>?): List<MeetingItemViewModel> =
        meetings?.filter { it.startDateTime.isAfterNow }
            ?.map {
                MeetingItemViewModel(
                    type = it.state(),
                    meeting = it
                )
            } ?: emptyList()

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

    private fun List<Meeting>?.labelType() =
        if (this.isNullOrEmpty())
            LabelType.NONE_TODAY
        else if (this.any { it.state() == MeetingStateType.UPCOMING })
            LabelType.HAS_UPCOMING
        else if (this.size == 1)
            LabelType.NONE_UPCOMING
        else
            LabelType.NONE_TODAY

}




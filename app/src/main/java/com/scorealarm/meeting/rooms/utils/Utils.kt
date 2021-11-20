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

    fun Meeting?.state(): MeetingStateType =
        this?.run {
            if (!startDateTime.isToday())
                return MeetingStateType.EXCLUDED
            return when {
                Period(startDateTime, endDateTime).days < 1
                        && Interval(
                    startDateTime,
                    endDateTime
                ).containsNow() -> MeetingStateType.ONGOING
                Period(startDateTime, endDateTime).days == 1 -> MeetingStateType.ALL_DAY
                startDateTime.isAfterNow -> MeetingStateType.UPCOMING
                else -> MeetingStateType.EXCLUDED
            }
        } ?: MeetingStateType.EXCLUDED

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
                    meeting?.endDateTime?.toString(context.getString(R.string.meeting_time_format))
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
        return MeetingListViewModel(
            meetingList = meetingList?.filter { it.state() == MeetingStateType.UPCOMING },
            labelData = meetingList?.getLabelData(context)
        )
    }


    fun mapToMeetingItemViewModel(meetings: List<Meeting>?): List<MeetingItemViewModel> =
        meetings?.filter { it.state() == MeetingStateType.UPCOMING }
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
        context: Context?
    ): Triple<String, Int, Float> {
        val filteredMeetingList = this?.filter { it.state() != MeetingStateType.EXCLUDED }
        return when {
            filteredMeetingList.isNullOrEmpty() -> Triple(
                context?.getString(R.string.label_meetings_none_today) ?: "",
                R.style.TextAppearanceMeetingCountBold,
                context?.resources?.getDimension(R.dimen.space_small) ?: 0f
            )
            !filteredMeetingList.any { it.state() == MeetingStateType.UPCOMING } ->
                Triple(
                    context?.getString(R.string.label_meetings_no_next) ?: "",
                    R.style.TextAppearanceMeetingCountBold,
                    context?.resources?.getDimension(R.dimen.space_big) ?: 0f
                )
            filteredMeetingList.any { it.state() == MeetingStateType.ALL_DAY || it.state() == MeetingStateType.ONGOING } -> {
                Triple(
                    context?.getString(R.string.label_meetings_next) ?: "",
                    R.style.TextAppearanceMeetingCountNormal,
                    context?.resources?.getDimension(R.dimen.space_big) ?: 0f
                )
            }
            !filteredMeetingList.any { it.state() == MeetingStateType.ALL_DAY || it.state() == MeetingStateType.ONGOING } -> {
                Triple(
                    context?.getString(R.string.label_meetings_next) ?: "",
                    R.style.TextAppearanceMeetingCountNormal,
                    context?.resources?.getDimension(R.dimen.space_small) ?: 0f
                )
            }
            else -> Triple("", 0, 0f)
        }
    }

    private fun DateTime?.isToday(): Boolean {
        val now = DateTime.now()
        return Interval(
            now.withTimeAtStartOfDay(),
            now.withTimeAtStartOfDay().plusDays(1)
        ).contains(this)
    }
}




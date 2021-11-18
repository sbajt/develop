package com.scorealarm.meeting.rooms.utils

import android.content.Context
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingItemViewModel
import com.scorealarm.meeting.rooms.models.OngoingMeetingViewModel
import com.scorealarm.meeting.rooms.models.types.MeetingStateType
import org.joda.time.DateTime
import org.joda.time.Interval
import org.joda.time.Period


object Utils {

    private val TAG = Utils::class.java.canonicalName

    fun Meeting?.state(): MeetingStateType =
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

    fun List<Meeting>.createUpcomingMeetingsItemViewModelList(): List<MeetingItemViewModel> =
        this.filter { it.state() != MeetingStateType.ONGOING || it.state() != MeetingStateType.ALL_DAY }
            .map {
                MeetingItemViewModel(
                    it.state(),
                    it
                )
            }

    fun List<Meeting>.getOngoingMeeting(): Meeting? =
        this.find { it.state() == MeetingStateType.ONGOING || it.state() == MeetingStateType.ALL_DAY }

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
}



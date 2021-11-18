package com.scorealarm.meeting.rooms.fragments.models

import com.scorealarm.meeting.rooms.models.Meeting

data class OngoingMeetingViewModel(
    val meeting: Meeting?,
    val meetingTimeText: String?
)

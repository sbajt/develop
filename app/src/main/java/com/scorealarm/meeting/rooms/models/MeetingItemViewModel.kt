package com.scorealarm.meeting.rooms.models

import com.scorealarm.meeting.rooms.models.types.MeetingStateType

data class MeetingItemViewModel(
    val type: MeetingStateType,
    val meeting: Meeting? = null,
)
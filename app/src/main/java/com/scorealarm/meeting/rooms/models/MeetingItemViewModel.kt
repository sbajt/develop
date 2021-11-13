package com.scorealarm.meeting.rooms.models

import com.scorealarm.meeting.rooms.list.MeetingRoomMeetingsListAdapter

data class MeetingItemViewModel(
    val type: MeetingRoomMeetingsListAdapter.ViewType,
    val meeting: Meeting? = null,
    val meetingCount: Int
)
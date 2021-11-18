package com.scorealarm.meeting.rooms.fragments.models

import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.types.LabelType

data class MeetingRoomMeetingListViewModel(
    val meetingList: List<Meeting>?,
    val labelType: LabelType
)

package com.scorealarm.meeting.rooms.fragments.models

import com.scorealarm.meeting.rooms.models.Meeting

data class MeetingListViewModel(
    val meetingList: List<Meeting>?,
    val labelData: Pair<String?, Int?>
)

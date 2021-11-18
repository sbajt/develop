package com.scorealarm.meeting.rooms.rest.models

import com.google.gson.annotations.SerializedName
import com.scorealarm.meeting.rooms.models.Meeting

data class MeetingListRestModelWrapper(
    @SerializedName("events")
    val meetingList: List<Meeting>
)

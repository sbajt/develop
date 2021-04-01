package com.scorealarm.meeting.rooms.models

import com.google.gson.annotations.SerializedName

data class MeetingRoom(
    val id: String,
    val name: String,
    @SerializedName("events")
    val meetingList: List<Meeting>?
)

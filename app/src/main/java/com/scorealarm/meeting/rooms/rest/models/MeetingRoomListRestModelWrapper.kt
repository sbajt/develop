package com.scorealarm.meeting.rooms.rest.models

import com.google.gson.annotations.SerializedName
import com.scorealarm.meeting.rooms.models.MeetingRoom

data class MeetingRoomListRestModelWrapper(
    @SerializedName("rooms")
    val meetingRoomList: List<MeetingRoom>
)

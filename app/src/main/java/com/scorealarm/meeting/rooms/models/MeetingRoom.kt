package com.scorealarm.meeting.rooms.models

import org.joda.time.DateTime

data class MeetingRoom(
    val id: String,
    val name: String,
    val meetings: List<Meeting>
)

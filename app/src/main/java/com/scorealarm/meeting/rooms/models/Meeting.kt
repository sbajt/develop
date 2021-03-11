package com.scorealarm.meeting.rooms.models

import org.joda.time.DateTime

data class Meeting(
    val title: String?,
    val organizer: String?,
    val guestNumber: Int?,
    val startDateTime: DateTime,
    val endDateTime: DateTime
)

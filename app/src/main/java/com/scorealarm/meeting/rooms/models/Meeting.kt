package com.scorealarm.meeting.rooms.models

import org.joda.time.DateTime

data class Meeting(
    val id: String,
    val title: String?,
    val organizer: String?,
    val invitesNumber: Int?,
    val startDateTime: DateTime,
    val endDateTime: DateTime
)

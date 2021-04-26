package com.scorealarm.meeting.rooms.models

import com.google.gson.annotations.SerializedName
import org.joda.time.DateTime

data class Meeting(
    @SerializedName("summary")
    val title: String?,

    val description: String?,

    val organizer: String?,

    @SerializedName("attendees")
    val invitesNumber: Int?,

    @SerializedName("start")
    val startDateTime: DateTime,

    @SerializedName("end")
    val endDateTime: DateTime
)

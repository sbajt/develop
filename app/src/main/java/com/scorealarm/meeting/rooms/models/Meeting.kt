package com.scorealarm.meeting.rooms.models

import com.google.gson.annotations.SerializedName
import com.scorealarm.meeting.rooms.list.MeetingRoomMeetingsListAdapter
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
    val endDateTime: DateTime,

    ) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Meeting

        if (title != other.title) return false
        if (description != other.description) return false
        if (organizer != other.organizer) return false
        if (invitesNumber != other.invitesNumber) return false
        if (startDateTime != other.startDateTime) return false
        if (endDateTime != other.endDateTime) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title?.hashCode() ?: 0
        result = 31 * result + (description?.hashCode() ?: 0)
        result = 31 * result + (organizer?.hashCode() ?: 0)
        result = 31 * result + (invitesNumber ?: 0)
        result = 31 * result + startDateTime.hashCode()
        result = 31 * result + endDateTime.hashCode()
        return result
    }
}

package com.scorealarm.meeting.rooms.models

data class MeetingRoom(
    val id: String,
    val name: String,
    val meetingList: MutableList<Meeting> = mutableListOf()
)

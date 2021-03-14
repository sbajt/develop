package com.scorealarm.meeting.rooms.list

import com.scorealarm.meeting.rooms.models.MeetingRoom

interface MeetingRoomListItemActionListener {

    fun click(meetingRoom: MeetingRoom)

}
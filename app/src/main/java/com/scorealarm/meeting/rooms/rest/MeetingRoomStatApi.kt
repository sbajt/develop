package com.scorealarm.meeting.rooms.rest

import com.scorealarm.meeting.rooms.models.MeetingRoom
import io.reactivex.Observable
import retrofit2.http.GET


interface MeetingRoomStatApi {

    @GET("meetingrooms/statuses")
    fun getMeetingRooms(): Observable<List<MeetingRoom>>

}
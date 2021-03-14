package com.scorealarm.meeting.rooms.rest

import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query


interface MeetingRoomStatApi {

    @GET("meetingRooms")
    fun getMeetingRooms(): Observable<List<MeetingRoom>>

    @GET("meetingRoom/meetings")
    fun getMeetings(@Query("id") roomId: String): Observable<List<Meeting>>

}
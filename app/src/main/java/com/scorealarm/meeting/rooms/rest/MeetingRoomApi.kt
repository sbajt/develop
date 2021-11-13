package com.scorealarm.meeting.rooms.rest

import com.scorealarm.meeting.rooms.models.MeetingRoomList
import com.scorealarm.meeting.rooms.models.MeetingsList
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query


interface MeetingRoomApi {

    @GET("/")
    fun getMeetingRooms(): Observable<MeetingRoomList>

    @GET("/room")
    fun getMeetings(@Query("id") roomId: String): Observable<MeetingsList>

}
package com.scorealarm.meeting.rooms.rest

import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.models.MeetingRoomListWrapper
import com.scorealarm.meeting.rooms.models.MeetingsListWrapper
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query


interface MeetingRoomApi {

    @GET("/")
    fun getMeetingRooms(): Observable<MeetingRoomListWrapper>

    @GET("/room")
    fun getMeetings(@Query("id") roomId: String): Observable<MeetingsListWrapper>

}
package com.scorealarm.meeting.rooms.rest

import com.scorealarm.meeting.rooms.rest.models.MeetingListRestModelWrapper
import com.scorealarm.meeting.rooms.rest.models.MeetingRoomListRestModelWrapper
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query


interface MeetingRoomApi {

    @GET("/")
    fun getMeetingRoomList(): Observable<MeetingRoomListRestModelWrapper>

    @GET("/room")
    fun getMeetingsByRoom(@Query("id") roomId: String): Observable<MeetingListRestModelWrapper>

}
package com.scorealarm.meeting.rooms.rest

import io.reactivex.Observable
import retrofit2.http.GET

interface MockApi {

    @GET
    fun getMockData(): Observable<String>
}
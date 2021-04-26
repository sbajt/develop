package com.scorealarm.meeting.rooms.rest

import com.google.gson.GsonBuilder
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.joda.time.DateTime
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RestService {

    val gson = GsonBuilder()
        .registerTypeAdapter(DateTime::class.java, DateTimeAdapter())
        .create()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request()
                .newBuilder()
                .addHeader("authentication", Config.HEADER_AUTH)
                .build()
            it.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(Config.BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
        .create(MeetingRoomApi::class.java)

    fun fetchMeetingRoomList(): Observable<List<MeetingRoom>> =
        api.getMeetingRooms().flatMap { Observable.just(it.rooms) }.subscribeOn(Schedulers.io())

    fun fetchMeetingList(meetingRoomId: String): Observable<List<Meeting>> =
        api.getMeetings(meetingRoomId).flatMap { Observable.just(it.events) }
            .subscribeOn(Schedulers.io())

    private fun mockMeetingList(): Observable<List<Meeting>> =
        Observable.just(
            listOf(
                Meeting(
                    title = "Test meeting 1",
                    organizer = "Tester",
                    invitesNumber = 4,
                    startDateTime = DateTime.parse("2021-4-25T11:00:00.000+01:00"),
                    endDateTime = DateTime.parse("2021-4-25T13:00:00.000+01:00")
                ),
                Meeting(
                    title = "Test meeting 2",
                    organizer = "Tester",
                    invitesNumber = 1,
                    startDateTime = DateTime.parse("2021-4-25T13:00:00.000+01:00"),
                    endDateTime = DateTime.parse("2021-4-25T14:00:00.000+01:00")
                ),
                Meeting(
                    title = "Test meeting 3",
                    organizer = "Tester",
                    invitesNumber = 12,
                    startDateTime = DateTime.parse("2021-4-25T15:00:00.000+01:00"),
                    endDateTime = DateTime.parse("2021-4-25T16:30:00.000+01:00")
                ),
                Meeting(
                    title = "Test meeting 4",
                    organizer = "Tester",
                    invitesNumber = 3,
                    startDateTime = DateTime.parse("2021-4-25T16:30:00.000+01:00"),
                    endDateTime = DateTime.parse("2021-4-25T17:00:00.000+01:00")
                )
            )
        )


}

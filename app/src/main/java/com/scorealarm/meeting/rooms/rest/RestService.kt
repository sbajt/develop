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
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

//    private val api = Retrofit.Builder()
//        .baseUrl(Config.BASE_URL)
//        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//        .addConverterFactory(GsonConverterFactory.create(gson))
//        .client(okHttpClient)
//        .build()
//        .create(MeetingRoomApi::class.java)

    fun fetchMeetingRoomList(): Observable<List<MeetingRoom>> =
//        api.getMeetingRooms().subscribeOn(Schedulers.io())
        Observable.just(mockMeetingRoomList()).subscribeOn(Schedulers.io())

    fun fetchMeetingList(meetingRoomId: String): Observable<List<Meeting>> =
//        api.getMeetings(meetingRoomId).subscribeOn(Schedulers.io())
        Observable.just(mockMeetingList()).subscribeOn(Schedulers.io())

    private fun mockMeetingRoomList() =
        listOf(
            MeetingRoom(
                id = "0",
                name = "Meeting room 1",
            ),
            MeetingRoom(
                id = "1",
                name = "Meeting room 2",
            ),
            MeetingRoom(
                id = "2",
                name = "Meeting room 3",
            )
        )

    private fun mockMeetingList() =
        listOf(
            Meeting(
                id = "0",
                title = "Meeting 1",
                organizer = "Lula",
                invitesNumber = 12,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(9),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(10)
            ),
            Meeting(
                id = "1",
                title = "Meeting 2",
                organizer = "Vatroslav",
                invitesNumber = 3,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(12),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(13)
            ),
            Meeting(
                id = "2",
                title = "Meeting 3",
                organizer = "Marin",
                invitesNumber = 5,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(14),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(15),
            ),
            Meeting(
                id = "3",
                title = "Meeting 4",
                organizer = "Lula",
                invitesNumber = 1,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(15),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(16)
            ),
            Meeting(
                id = "4",
                title = "Meeting 5",
                organizer = "Lolić",
                invitesNumber = 3,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(16),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(17).plusMinutes(30),
            )
        )

}
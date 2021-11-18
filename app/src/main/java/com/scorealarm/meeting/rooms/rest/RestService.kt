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
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val api = Retrofit.Builder()
        .baseUrl(Config.BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build()
        .create(MeetingRoomApi::class.java)

    fun getMeetingRoomList(): Observable<List<MeetingRoom>> =
        api.getMeetingRoomList().flatMap { Observable.fromArray(it.meetingRoomList) }.subscribeOn(Schedulers.io())

    fun getMeetingListByRoom(meetingRoomId: String): Observable<List<Meeting>> =
        api.getMeetingsByRoom(meetingRoomId).flatMap { Observable.fromArray(it.meetingList) }
            .subscribeOn(Schedulers.io())


}

package com.scorealarm.meeting.rooms.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.gson.GsonBuilder
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.list.ListItemActionListener
import com.scorealarm.meeting.rooms.list.MeetingRoomItemDecoration
import com.scorealarm.meeting.rooms.list.MeetingRoomListAdapter
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.MeetingRoomStatApi
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_room_list.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MeetingRoomListFragment : Fragment(R.layout.fragment_meeting_room_list),
    ListItemActionListener {

    private val listAdapter = MeetingRoomListAdapter(this)
    private val restService by lazy { initRestService() }
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = listAdapter
        recyclerView.addItemDecoration(MeetingRoomItemDecoration())
        mockData()
    }

    override fun onClick(itemId: String) {
        Log.d("MeetingRoomListFragment", "Room with id:$itemId...")
//        fetchData()
        mockData()
    }

    private fun initRestService(): MeetingRoomStatApi {
        val gson = GsonBuilder()
            .create()

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(Config.BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()
            .create(MeetingRoomStatApi::class.java)
    }

    private fun fetchData() {
        compositeDisposable.add(
            restService.getMeetingRooms()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    listAdapter.update(it)
                }, { Log.e(TAG, it.toString()) })
        )
    }

    private fun mockData() {
        compositeDisposable.addAll(
            Observable.just(
                listOf(
                    MeetingRoom(
                        id = "0",
                        name = "Meeting room 1",
                        meetings = emptyList()
                    ), MeetingRoom(
                        id = "1",
                        name = "Meeting room 2",
                        meetings = emptyList()
                    ), MeetingRoom(
                        id = "2",
                        name = "Meeting room 3",
                        meetings = emptyList()
                    )
                )
            )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    listAdapter.update(it)
                }, { Log.e(TAG, it.toString()) })
        )
    }

    companion object {

        val TAG = MeetingRoomListFragment::class.java.canonicalName

    }


}
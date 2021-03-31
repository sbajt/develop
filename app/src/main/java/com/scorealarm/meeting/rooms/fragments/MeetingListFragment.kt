package com.scorealarm.meeting.rooms.fragments

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.MeetingListAdapter
import com.scorealarm.meeting.rooms.models.MeetingRoom
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_list.*
import java.util.concurrent.TimeUnit

class MeetingListFragment(
    private val meetingRoom: MeetingRoom
) : Fragment(R.layout.fragment_meeting_list) {

    private val listAdapter = MeetingListAdapter()
    private val compositeDisposable = CompositeDisposable()
    private val wifiManager: WifiManager by lazy {
        context?.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = listAdapter
    }

    override fun onStart() {
        super.onStart()
        initMeetings(meetingRoom)
        observeMeetingList()
        updateMeetingList(
            meetingRoom = meetingRoom,
            period = 5,
            periodUnit = TimeUnit.MINUTES
        )
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun initMeetings(meetingRoom: MeetingRoom) {
        compositeDisposable.add(
            Observable.defer {
                if (wifiManager.isWifiEnabled)
                    (activity as MainActivity).fetchMeetingsByMeetingRoom(meetingRoom.id)
                else
                    Observable.empty()
            }.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listAdapter::update) { Log.e(TAG, it.toString()) }
        )
    }

    private fun observeMeetingList() {
        compositeDisposable.add(
            (activity as MainActivity).meetingRoomSubject
                .subscribeOn(Schedulers.newThread())
                .flatMap { Observable.just(it.meetingList) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listAdapter::update) { Log.e(TAG, it.toString()) }
        )
    }

    private fun updateMeetingList(
        meetingRoom: MeetingRoom,
        period: Long,
        periodUnit: TimeUnit
    ) {
        compositeDisposable.add(
            Observable.interval(period, periodUnit, Schedulers.newThread())
                .takeWhile { wifiManager.isWifiEnabled }
                .flatMap { (activity as MainActivity).fetchMeetingsByMeetingRoom(meetingRoom.id) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ meetings ->
                    (activity as MainActivity).run {
                        updateMeetingRoomWithMeetings(meetingRoom, meetings)
                        saveMeetingRoomIntoPreference(meetingRoom)
                        meetingRoomSubject.onNext(meetingRoom)
                    }
                }, { Log.e(TAG, it.toString()) })
        )
    }

    companion object {

        private val TAG = MeetingListFragment::class.java.canonicalName

        fun getInstance(meetingRoom: MeetingRoom) = MeetingListFragment(meetingRoom)
    }
}

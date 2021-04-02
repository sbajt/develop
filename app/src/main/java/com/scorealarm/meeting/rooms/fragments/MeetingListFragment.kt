package com.scorealarm.meeting.rooms.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.MeetingListAdapter
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_list.*
import java.util.concurrent.TimeUnit

class MeetingListFragment : Fragment(R.layout.fragment_meeting_list) {

    private val listAdapter = MeetingListAdapter()
    private val compositeDisposable = CompositeDisposable()

    private var isUpdating = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = listAdapter
    }

    override fun onStart() {
        super.onStart()
        initMeetingList()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
        isUpdating = false
    }

    private fun initMeetingList() {
        compositeDisposable.add(
            (activity as MainActivity).meetingRoomSubject
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ meetingRoom ->
                    if (meetingRoom.meetingList.isNullOrEmpty())
                        (activity as MainActivity).showEmptyMeetingListFragment("No meetings today")
                    else
                        listAdapter.update(meetingRoom.meetingList)
                    if (!isUpdating)
                        updateMeetingListByInterval(meetingRoom, 1, TimeUnit.MINUTES)
                }) { Log.e(TAG, it.toString()) }
        )
    }

    private fun updateMeetingListByInterval(
        meetingRoom: MeetingRoom,
        period: Long,
        periodUnit: TimeUnit
    ) {
        isUpdating = true
        compositeDisposable.add(
            Observable.interval(period, periodUnit, Schedulers.newThread())
                .takeWhile { (activity as MainActivity).wifiManager.isWifiEnabled }
                .flatMap { (activity as MainActivity).fetchMeetingsByMeetingRoom(meetingRoom.id) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    (activity as MainActivity).saveMeetingRoomIntoPreference(
                        updateMeetingRoomSubject(meetingRoom, it)
                    )
                }) { Log.e(TAG, it.toString()) }
        )
    }

    private fun updateMeetingRoomSubject(
        meetingRoom: MeetingRoom,
        meetings: List<Meeting>
    ): MeetingRoom {
        val room = meetingRoom.copy(
            id = meetingRoom.id,
            name = meetingRoom.name,
            meetingList = meetings
        )
        (activity as MainActivity).meetingRoomSubject.onNext(room)
        return room
    }

    companion object {

        private val TAG = MeetingListFragment::class.java.canonicalName

        fun getInstance() = MeetingListFragment()
    }
}

package com.scorealarm.meeting.rooms.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.MeetingListAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_list.*
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class MeetingListFragment : Fragment(R.layout.fragment_meeting_list) {

    private val listAdapter = MeetingListAdapter()
    private val compositeDisposable = CompositeDisposable()

    private var hasMeetingListUpdateStarted = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = listAdapter
    }

    override fun onStart() {
        super.onStart()
        observeMeetingList()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun observeMeetingList() {
        compositeDisposable.add(
            (activity as MainActivity).meetingRoomSubject
                .subscribeOn(Schedulers.newThread())
                .map { meetingRoom ->
                    if (!hasMeetingListUpdateStarted) {
                        updateMeetingList(
                            meetingRoomId = meetingRoom.id,
                            period = 5,
                            periodUnit = TimeUnit.MINUTES
                        )
                        hasMeetingListUpdateStarted = true
                    }
                    meetingRoom.meetingList.filter { meeting ->
                        meeting.startDateTime.isAfter(
                            DateTime.now().withTimeAtStartOfDay()
                        )
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    listAdapter.update(it)
                })
                { Log.e(TAG, it.toString()) }
        )
    }

    private fun updateMeetingList(
        meetingRoomId: String,
        period: Long,
        periodUnit: TimeUnit
    ) {
        compositeDisposable.add(
            Observable.interval(period, periodUnit, Schedulers.newThread())
                .flatMap { MainActivity.getMeetingRoomData(meetingRoomId) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ data ->
                    Log.d(TAG, data)
//                    (activity as MainActivity).meetingRoomSubject.onNext(meetingRoom)
                }, { Log.e(TAG, it.toString()) })
        )
    }

    companion object {

        private val TAG = MeetingListFragment::class.java.canonicalName

        fun getInstance() = MeetingListFragment()
    }
}

package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.EmptyListSourceType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.MeetingListAdapter
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
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

    override fun onStart() {
        super.onStart()
        recyclerView?.adapter = listAdapter
        observeMeetingRoomSubject()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun observeMeetingRoomSubject() {
        compositeDisposable.add(
            (activity as MainActivity).meetingRoomSubject
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val meetingListToday = it.meetingList?.filter { meeting ->
                        meeting.endDateTime.dayOfMonth() == DateTime.now().dayOfMonth()
                    }
                    if (meetingListToday.isNullOrEmpty()) {
                        (activity as MainActivity).navigateToEmptyFragment(EmptyListSourceType.MEETING_LIST)
                    } else {
                        listAdapter.update(meetingListToday)
                    }
                }, { Log.d(TAG, it.toString()) })
        )
    }

    companion object {

        private val TAG = MeetingListFragment::class.java.canonicalName

        fun getInstance() = MeetingListFragment()
    }
}

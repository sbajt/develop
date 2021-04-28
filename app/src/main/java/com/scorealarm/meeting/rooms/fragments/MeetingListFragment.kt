package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.ListDisplayType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.MeetingListAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_list.*
import org.joda.time.DateTime
import org.joda.time.Interval

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
                    if (it.meetingList.isNullOrEmpty()) {
                        (activity as MainActivity).run {
                            showEmptyFragment(ListDisplayType.MEETING_LIST)
                        }
                    } else {
                        val todayInterval = Interval(
                            DateTime.now().withTimeAtStartOfDay(),
                            DateTime.now().withTimeAtStartOfDay().plusDays(1)
                        )
                        val todayMeetingList = it.meetingList.filter { meeting ->
                            val meetingInterval = Interval(
                                meeting.startDateTime,
                                meeting.endDateTime
                            )
                            todayInterval.contains(meetingInterval) && todayInterval.contains(
                                meetingInterval
                            )
                        }
                        if (todayMeetingList.isNullOrEmpty())
                            (activity as MainActivity).showEmptyFragment(ListDisplayType.MEETING_LIST)
                        else
                            listAdapter.update(todayMeetingList)
                    }
                })
                { Log.d(TAG, it.toString()) }
        )
    }

    companion object {

        private val TAG = MeetingListFragment::class.java.canonicalName

        fun getInstance() = MeetingListFragment()
    }
}

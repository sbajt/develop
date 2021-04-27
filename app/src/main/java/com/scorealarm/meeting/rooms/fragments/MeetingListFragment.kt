package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.ListDisplayType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.MeetingListAdapter
import com.scorealarm.meeting.rooms.models.Meeting
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_list.*
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Interval
import org.joda.time.Period

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
                    val today = Interval(DateTime.now().withTimeAtStartOfDay(), Days.ONE)
                    val meetingListToday = mutableListOf<Meeting>()
                    if (it?.meetingList?.any {
                            Period(
                                it.startDateTime,
                                it.endDateTime
                            ).days == 1
                        } != null)
                        meetingListToday.add(it.meetingList.first {
                            Period(
                                it.startDateTime,
                                it.endDateTime
                            ).days == 1
                        })
                    else
                        meetingListToday.addAll(it.meetingList?.filter { today.contains(it.startDateTime) }
                            ?: emptyList())
                    if (meetingListToday.isNullOrEmpty()) {
                        (activity as MainActivity).navigateToEmptyFragment(ListDisplayType.MEETING_LIST)
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

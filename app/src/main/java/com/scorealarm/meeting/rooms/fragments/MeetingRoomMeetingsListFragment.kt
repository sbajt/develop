package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.MeetingRoomMeetingsListAdapter
import com.scorealarm.meeting.rooms.utils.Utils.createUpcomingMeetingsItemViewModelList
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list.*

class MeetingRoomMeetingsListFragment :
    Fragment(R.layout.fragment_list) {

    private val listAdapter = MeetingRoomMeetingsListAdapter()
    private val compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        recyclerView?.adapter = listAdapter
        setHasOptionsMenu(false)
        fetchMeetings()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun fetchMeetings() {
        compositeDisposable.add(
            (activity as MainActivity).meetingRoomSubject
                .map { it.meetingList?.createUpcomingMeetingsItemViewModelList() }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ it?.run { listAdapter.update(this) } }) { Log.d(TAG, it.toString()) }
        )
    }

    companion object {

        private val TAG = MeetingRoomMeetingsListFragment::class.java.canonicalName

    }
}

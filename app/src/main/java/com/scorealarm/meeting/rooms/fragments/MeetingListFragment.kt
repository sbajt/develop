package com.scorealarm.meeting.rooms.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.MeetingListAdapter
import com.scorealarm.meeting.rooms.list.MeetingListItemActionListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_list.*
import org.joda.time.DateTime

class MeetingListFragment : Fragment(R.layout.fragment_meeting_list),
    MeetingListItemActionListener {

    private val listAdapter = MeetingListAdapter(this)
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = listAdapter
    }

    override fun onStart() {
        super.onStart()
        observeMeetingRoom()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun observeMeetingRoom() {
        compositeDisposable.add(
            (activity as MainActivity).meetingRoomSubject
                .subscribeOn(Schedulers.newThread())
                .map { meetingRoom ->
                    meetingRoom.meetingList.filter { meeting ->
                        meeting.startDateTime.isAfter(
                            DateTime.now().withTimeAtStartOfDay()
                        )
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listAdapter::update) { Log.e(TAG, it.toString()) }
        )
    }

    override fun click(meetingId: String) {
        Log.d(TAG, "Meeting with id $meetingId clicked..,")
    }


    companion object {

        private val TAG = MeetingListFragment::class.java.canonicalName
    }
}

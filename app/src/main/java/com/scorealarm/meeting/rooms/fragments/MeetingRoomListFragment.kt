package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.ListItemActionListener
import com.scorealarm.meeting.rooms.list.MeetingRoomListAdapter
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_meeting_room_list.*

class MeetingRoomListFragment : Fragment(R.layout.fragment_meeting_room_list),
    ListItemActionListener<MeetingRoom> {

    private val listAdapter = MeetingRoomListAdapter(this)
    private val compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        recyclerView?.adapter = listAdapter
        compositeDisposable.add(
            RestService.fetchMeetingRoomList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listAdapter::update) { Log.d(TAG, it.toString()) }
        )
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    override fun onClick(data: MeetingRoom) {
        compositeDisposable.add(
            RestService.fetchMeetingList(data.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    (activity as MainActivity).run {
                        val newMeetingRoom = MeetingRoom(
                            id = data.id,
                            name = data.name,
                            meetingList = it
                        )
                        saveMeetingRoomIntoPreference(newMeetingRoom)
                        meetingRoomSubject.onNext(newMeetingRoom)
                        navigateToMeetingRoomDetails(newMeetingRoom)
                    }
                }, { Log.e(TAG, it.toString()) })
        )
    }

    companion object {

        private val TAG = MeetingRoomListFragment::class.java.canonicalName

        fun getInstance() = MeetingRoomListFragment()

    }


}
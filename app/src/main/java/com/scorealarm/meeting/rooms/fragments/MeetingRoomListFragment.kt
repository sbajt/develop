package com.scorealarm.meeting.rooms.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.MeetingRoomItemDecoration
import com.scorealarm.meeting.rooms.list.MeetingRoomListAdapter
import com.scorealarm.meeting.rooms.list.MeetingRoomListItemActionListener
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_room_list.*

class MeetingRoomListFragment : Fragment(R.layout.fragment_meeting_room_list),
    MeetingRoomListItemActionListener {

    private val listAdapter = MeetingRoomListAdapter(this)
    private val compositeDisposable = CompositeDisposable()

    private val meetingRoomList = mutableListOf<MeetingRoom>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = listAdapter
        recyclerView.addItemDecoration(MeetingRoomItemDecoration())
        mockMeetingRoomList()
    }

    override fun click(meetingRoom: MeetingRoom) {
        activity?.run {
            getPreferences(Context.MODE_PRIVATE)?.edit()
                ?.putString(MainActivity.meetingRoomKey, RestService.gson.toJson(meetingRoom))
                ?.commit()
            (this as? MainActivity)?.meetingRoomSubject?.onNext(meetingRoom)
            supportFragmentManager.commit {
                remove(this@MeetingRoomListFragment)
                replace<MeetingRoomDescriptionFragment>(R.id.meetingRoomDescriptionContainer)
                replace<MeetingListFragment>(R.id.meetingListContainer)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun mockMeetingRoomList() {
        compositeDisposable.addAll(
            Observable.defer {
                meetingRoomList.clear()
                meetingRoomList.add(
                    MeetingRoom(
                        id = "0",
                        name = "Meeting room 1",
                    )
                )
                meetingRoomList.add(
                    MeetingRoom(
                        id = "1",
                        name = "Meeting room 2",
                    )
                )
                meetingRoomList.add(
                    MeetingRoom(
                        id = "2",
                        name = "Meeting room 3",
                    )
                )
                Observable.just(meetingRoomList)
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listAdapter::update) { Log.e(TAG, it.toString()) }
        )
    }


    companion object {

        private val TAG = MeetingRoomListFragment::class.java.canonicalName

    }


}
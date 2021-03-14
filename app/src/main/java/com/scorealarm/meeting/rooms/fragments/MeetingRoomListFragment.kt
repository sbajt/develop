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
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_room_list.*
import kotlinx.coroutines.runBlocking
import okhttp3.internal.wait
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

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
        setMockMeetingListOnMeetingRoom(meetingRoom)
        activity?.getPreferences(Context.MODE_PRIVATE)?.edit()
            ?.putString(MainActivity.meetingRoomKey, RestService.gson.toJson(meetingRoom))
            ?.commit()
        (activity as? MainActivity)?.meetingRoomSubject
            ?.onNext(meetingRoom)
        activity?.supportFragmentManager?.commit {
            remove(this@MeetingRoomListFragment)
            replace<MeetingRoomDescriptionFragment>(R.id.meetingRoomDescriptionContainer)
            replace<MeetingListFragment>(R.id.meetingListContainer)
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

    private fun setMockMeetingListOnMeetingRoom(meetingRoom: MeetingRoom) {
        meetingRoom.meetingList.clear()
        meetingRoom.meetingList.add(
            Meeting(
                id = "0",
                title = "Meeting 1",
                organizer = "Lula",
                invitesNumber = 12,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(9),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(10)
            ))
        meetingRoom.meetingList.add(
            Meeting(
                id = "1",
                title = "Meeting 2",
                organizer = "Vatroslav",
                invitesNumber = 3,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(12),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(13)
            ))
        meetingRoom.meetingList.add(
            Meeting(
                id = "2",
                title = "Meeting 3",
                organizer = "Marin",
                invitesNumber = 5,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(14),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(14).plusMinutes(30),
            ))
        meetingRoom.meetingList.add(
            Meeting(
                id = "3",
                title = "Meeting 4",
                organizer = "Luls",
                invitesNumber = 1,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(18),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(19)
            ))
        meetingRoom.meetingList.add(
            Meeting(
                id = "4",
                title = "Meeting 5",
                organizer = "Lolić",
                invitesNumber = 3,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(20),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(20).plusMinutes(30),
            ))

    }


    companion object {

        private val TAG = MeetingRoomListFragment::class.java.canonicalName

    }


}
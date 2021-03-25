package com.scorealarm.meeting.rooms.fragments

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.MeetingRoomItemDecoration
import com.scorealarm.meeting.rooms.list.MeetingRoomListAdapter
import com.scorealarm.meeting.rooms.list.MeetingRoomListItemActionListener
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_meeting_room_list.*
import org.joda.time.DateTime

class MeetingRoomListFragment : Fragment(R.layout.fragment_meeting_room_list),
    MeetingRoomListItemActionListener {

    private val listAdapter = MeetingRoomListAdapter(this)
    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.adapter = listAdapter
        recyclerView.addItemDecoration(MeetingRoomItemDecoration())
    }

    override fun onStart() {
        super.onStart()
        listAdapter.update(mockMeetingRoomList())
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    override fun click(meetingRoom: MeetingRoom) {
        setMeetingListOnMeetingRoom(meetingRoom)
        (activity as? MainActivity)?.run {
            getPreferences(Context.MODE_PRIVATE)?.edit()
                ?.putString(MainActivity.meetingRoomKey, RestService.gson.toJson(meetingRoom))
                ?.apply()
            meetingRoomSubject.onNext(meetingRoom)
            supportFragmentManager.commit {
                remove(this@MeetingRoomListFragment)
                replace(R.id.meetingRoomDescriptionContainer, MeetingRoomDescriptionFragment.getInstance())
                replace(R.id.meetingListContainer, MeetingListFragment.getInstance())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun mockMeetingRoomList() =
        listOf(
            MeetingRoom(
                id = "0",
                name = "Meeting room 1",
            ),
            MeetingRoom(
                id = "1",
                name = "Meeting room 2",
            ),
            MeetingRoom(
                id = "2",
                name = "Meeting room 3",
            )
        )

    private fun setMeetingListOnMeetingRoom(meetingRoom: MeetingRoom?) {
        meetingRoom?.meetingList?.clear()
        meetingRoom?.meetingList?.add(
            Meeting(
                id = "0",
                title = "Meeting 1",
                organizer = "Lula",
                invitesNumber = 12,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(9),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(10)
            )
        )
        meetingRoom?.meetingList?.add(
            Meeting(
                id = "1",
                title = "Meeting 2",
                organizer = "Vatroslav",
                invitesNumber = 3,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(12),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(13)
            )
        )
        meetingRoom?.meetingList?.add(
            Meeting(
                id = "2",
                title = "Meeting 3",
                organizer = "Marin",
                invitesNumber = 5,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(14),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(15),
            )
        )
        meetingRoom?.meetingList?.add(
            Meeting(
                id = "3",
                title = "Meeting 4",
                organizer = "Lula",
                invitesNumber = 1,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(15),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(16)
            )
        )
        meetingRoom?.meetingList?.add(
            Meeting(
                id = "4",
                title = "Meeting 5",
                organizer = "Lolić",
                invitesNumber = 3,
                startDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(16),
                endDateTime = DateTime.now().withTimeAtStartOfDay().plusHours(17).plusMinutes(30),
            )
        )

    }


    companion object {

        private val TAG = MeetingRoomListFragment::class.java.canonicalName

        fun getInstance() = MeetingListFragment()
    }


}
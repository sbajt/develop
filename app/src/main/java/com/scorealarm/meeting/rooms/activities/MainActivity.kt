package com.scorealarm.meeting.rooms.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.startup.AppInitializer
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.fragments.MeetingListFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomDescriptionFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomListFragment
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import net.danlew.android.joda.JodaTimeInitializer
import org.joda.time.DateTime

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    val meetingRoomSubject = ReplaySubject.create<MeetingRoom>(1)

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppInitializer.getInstance(this).initializeComponent(JodaTimeInitializer::class.java)

        val meetingRoom = RestService.gson.fromJson(
            getPreferences(Context.MODE_PRIVATE).getString(meetingRoomKey, ""),
            MeetingRoom::class.java
        )
        if (meetingRoom == null) {
            supportFragmentManager.commit {
                replace<MeetingRoomListFragment>(R.id.containerLayout)
            }
            supportActionBar?.title = "Meeting room chooser"
        } else {
            meetingRoomSubject.onNext(meetingRoom)
        }
    }

    override fun onStart() {
        super.onStart()
        observeMeetingRoom()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    fun updateMeetingListInMeetingRoom(meetingRoom: MeetingRoom?) {
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
        getPreferences(Context.MODE_PRIVATE).edit()
            .putString(meetingRoomKey, RestService.gson.toJson(meetingRoom))
            .commit()
    }

    private fun onMeetingRoomChoose(meetingRoom: MeetingRoom) {
        updateMeetingListInMeetingRoom(meetingRoom)
        supportFragmentManager.run {
            commit {
                remove(MeetingRoomListFragment())
                replace<MeetingRoomDescriptionFragment>(R.id.meetingRoomDescriptionContainer)
                replace<MeetingListFragment>(R.id.meetingListContainer)
            }
        }
        supportActionBar?.title = meetingRoom.name
    }

    private fun observeMeetingRoom() {
        compositeDisposable.add(
            meetingRoomSubject.subscribeOn(Schedulers.newThread())
                .firstElement()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    onMeetingRoomChoose(it)
                    compositeDisposable.dispose()
                }, { Log.e(TAG, it.toString()) })
        )
    }

    companion object {

        const val meetingRoomKey = "meetingRoom"

        private val TAG = MainActivity::class.java.canonicalName
    }


}
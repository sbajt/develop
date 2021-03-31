package com.scorealarm.meeting.rooms.activities

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.startup.AppInitializer
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.fragments.MeetingListFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomDescriptionFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomListFragment
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import net.danlew.android.joda.JodaTimeInitializer

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    val meetingRoomSubject = PublishSubject.create<MeetingRoom>()

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
                add(
                    R.id.containerLayout,
                    MeetingRoomListFragment.getInstance(),
                    MeetingRoomListFragment::class.java.canonicalName
                )
            }
            supportActionBar?.title = "Meeting room chooser"
        } else {
            meetingRoomSubject.onNext(meetingRoom)
            navigateToMeetingRoom(meetingRoom)
            supportActionBar?.title = meetingRoom.name
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    fun saveMeetingRoomIntoPreference(meetingRoom: MeetingRoom) {
        getPreferences(Context.MODE_PRIVATE)?.edit()
            ?.putString(meetingRoomKey, RestService.gson.toJson(meetingRoom))
            ?.apply()
    }

    fun navigateToMeetingRoom(meetingRoom: MeetingRoom) {
        supportFragmentManager.run {
            val meetingRoomListFragment =
                findFragmentByTag(MeetingRoomListFragment::class.java.canonicalName)
            meetingRoomListFragment?.run { beginTransaction().remove(this).commit() }
            commit {
                add(
                    R.id.meetingRoomDescriptionContainer,
                    MeetingRoomDescriptionFragment.getInstance(meetingRoom)
                )
                add(R.id.meetingListContainer, MeetingListFragment.getInstance(meetingRoom))
            }
        }
        supportFragmentManager.commit {
//            remove(MeetingRoomListFragment.getInstance())

        }
    }

    fun fetchMeetingRoomList(): Observable<List<MeetingRoom>> =
        RestService.fetchMeetingRoomList()


    fun fetchMeetingsByMeetingRoom(meetingRoomId: String): Observable<List<Meeting>> =
        RestService.fetchMeetingList(meetingRoomId)


    fun updateMeetingRoomWithMeetings(meetingRoom: MeetingRoom, meetings: List<Meeting>) {
        meetingRoom.meetingList.clear()
        meetingRoom.meetingList.addAll(meetings)
    }

    companion object {

        const val meetingRoomKey = "meetingRoom"

        private val TAG = MainActivity::class.java.canonicalName

    }

}
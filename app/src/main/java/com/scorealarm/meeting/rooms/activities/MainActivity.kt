package com.scorealarm.meeting.rooms.activities

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.startup.AppInitializer
import com.scorealarm.meeting.rooms.EmptyListSourceType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.fragments.EmptyListFragment
import com.scorealarm.meeting.rooms.fragments.MeetingListFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomDetailsFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomListFragment
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.ReplaySubject
import kotlinx.android.synthetic.main.activity_main.*
import net.danlew.android.joda.JodaTimeInitializer

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    val meetingRoomSubject = ReplaySubject.createWithSize<MeetingRoom>(1)

    val wifiManager: WifiManager by lazy { getSystemService(Context.WIFI_SERVICE) as WifiManager }

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppInitializer.getInstance(this).initializeComponent(JodaTimeInitializer::class.java)

    }

    override fun onStart() {
        super.onStart()

        val meetingRoom = RestService.gson.fromJson(
            getPreferences(Context.MODE_PRIVATE).getString(meetingRoomKey, ""),
            MeetingRoom::class.java
        )
        if (meetingRoom == null) {
            navigateToMeetingRoomList()
        } else {
            meetingRoomSubject.onNext(meetingRoom)
            navigateToMeetingRoomDetails(meetingRoom)
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

    fun navigateToMeetingRoomDetails(meetingRoom: MeetingRoom) {
        supportActionBar?.title = meetingRoom.name
        supportFragmentManager.commit {
            remove(MeetingRoomListFragment.getInstance())
            replace(R.id.meetingRoomDetailsContainer, MeetingRoomDetailsFragment.getInstance())
            replace(R.id.meetingRoomMeetingListContainer, MeetingListFragment.getInstance())
        }
    }

    fun navigateToEmptyFragment(type: EmptyListSourceType) {
        var text = ""
        var layoutRes = 0
        when (type) {
            EmptyListSourceType.MEETING_ROOM_LIST -> {
                text = "No meeting rooms"
                layoutRes = R.id.containerLayout
            }
            EmptyListSourceType.MEETING_LIST -> {
                text = "No meetings today"
                layoutRes = R.id.meetingRoomMeetingListContainer
            }
        }
        supportFragmentManager.commit {
            replace(layoutRes, EmptyListFragment.getInstance(text))
        }
    }

    private fun navigateToMeetingRoomList() {
        supportFragmentManager.commit {
            replace(R.id.containerLayout, MeetingRoomListFragment.getInstance())
        }
        supportActionBar?.title = "Meeting room chooser"
    }

    companion object {

        const val meetingRoomKey = "meetingRoom"

        private val TAG = MainActivity::class.java.canonicalName

    }

}
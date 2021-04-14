package com.scorealarm.meeting.rooms.activities

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.startup.AppInitializer
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.EmptyListSourceType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.fragments.EmptyListFragment
import com.scorealarm.meeting.rooms.fragments.MeetingListFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomDetailsFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomListFragment
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import net.danlew.android.joda.JodaTimeInitializer
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    val meetingRoomSubject = ReplaySubject.createWithSize<MeetingRoom>(1)

    val wifiManager: WifiManager by lazy { getSystemService(Context.WIFI_SERVICE) as WifiManager }

    private val meetingRoomListFragmentTag = "MeetingRoomListFragment"
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
            updateListByInterval(meetingRoom, Config.DATA_REFRESH_RATE_SECONDS, TimeUnit.SECONDS)
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
//        meetingRoomListContainer.visibility = View.GONE
        val meetingRoomListFragment =
            supportFragmentManager.findFragmentByTag(meetingRoomListFragmentTag)
        if (meetingRoomListFragment != null) {
            supportFragmentManager.beginTransaction().remove(meetingRoomListFragment).commit()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.meetingRoomDetailsContainer, MeetingRoomDetailsFragment.getInstance())
            .replace(R.id.meetingRoomMeetingListContainer, MeetingListFragment.getInstance())
            .commit()
        supportActionBar?.title = meetingRoom.name
    }

    fun navigateToEmptyFragment(type: EmptyListSourceType) {
        var text = ""
        var layoutRes = 0
        when (type) {
            EmptyListSourceType.MEETING_ROOM_LIST -> {
                text = "No meeting rooms"
                layoutRes = R.id.meetingRoomListContainer
            }
            EmptyListSourceType.MEETING_LIST -> {
                text = "No meetings today"
                layoutRes = R.id.meetingRoomMeetingListContainer
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(layoutRes, EmptyListFragment.getInstance(text))
            .commit()
    }

    private fun updateListByInterval(
        meetingRoom: MeetingRoom,
        period: Long,
        periodUnit: TimeUnit
    ) {
        compositeDisposable.add(
            Observable.interval(period, periodUnit, Schedulers.newThread())
                .filter { wifiManager.isWifiEnabled }
                .flatMap { RestService.fetchMeetingList(meetingRoom.id) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    updateMeetingRoomWithMeetings(meetingRoom, it)
                }) { Log.e(TAG, it.toString()) }
        )
    }

    private fun updateMeetingRoomWithMeetings(meetingRoom: MeetingRoom, meetings: List<Meeting>) {
        this.run {
            saveMeetingRoomIntoPreference(
                meetingRoom.copy(
                    id = meetingRoom.id,
                    name = meetingRoom.name,
                    meetingList = meetings
                )
            )
            meetingRoomSubject.onNext(
                meetingRoom.copy(
                    id = meetingRoom.id,
                    name = meetingRoom.name,
                    meetingList = meetings
                )
            )
        }
    }

    private fun navigateToMeetingRoomList() {
        supportFragmentManager.beginTransaction()
            .add(
                R.id.meetingRoomListContainer,
                MeetingRoomListFragment.getInstance(),
                meetingRoomListFragmentTag
            )
            .commit()
        supportActionBar?.title = "Meeting room chooser"
    }

    companion object {

        const val meetingRoomKey = "meetingRoom"

        private val TAG = MainActivity::class.java.canonicalName

    }

}
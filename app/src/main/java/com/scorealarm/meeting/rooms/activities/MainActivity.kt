package com.scorealarm.meeting.rooms.activities

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.startup.AppInitializer
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.ListDisplayType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.fragments.EmptyListFragment
import com.scorealarm.meeting.rooms.fragments.MeetingListFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomDescriptionFragment
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
            fetchMeetingsForMeetingRoom(meetingRoom)
            updateListByInterval(meetingRoom, Config.DATA_REFRESH_RATE_IN_SECONDS, TimeUnit.SECONDS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    fun navigateToEmptyFragment(type: ListDisplayType) {
        var text = ""
        var layoutRes = 0
        when (type) {
            ListDisplayType.MEETING_ROOM_LIST -> {
                text = "No meeting rooms"
                layoutRes = R.id.meetingRoomListContainer
            }
            ListDisplayType.MEETING_LIST -> {
                text = "No meetings today"
                layoutRes = R.id.meetingRoomMeetingListContainer
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(layoutRes, EmptyListFragment.getInstance(text))
            .commit()
    }

    fun fetchMeetingsForMeetingRoom(meetingRoom: MeetingRoom) {
        compositeDisposable.add(
            RestService.fetchMeetingList(meetingRoom.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val newMeetingRoomObject = updateMeetingRoomWithMeetings(meetingRoom, it)
                    saveMeetingRoomIntoPreference(newMeetingRoomObject)
                    meetingRoomSubject.onNext(newMeetingRoomObject)
                    navigateToMeetingRoomDetails(newMeetingRoomObject)
                }, { Log.e(TAG, it.toString()) })
        )
    }

    private fun saveMeetingRoomIntoPreference(meetingRoom: MeetingRoom) {
        getPreferences(Context.MODE_PRIVATE)?.edit()
            ?.putString(meetingRoomKey, RestService.gson.toJson(meetingRoom))
            ?.apply()
    }

    private fun navigateToMeetingRoomDetails(meetingRoom: MeetingRoom) {
        val meetingRoomListFragment =
            supportFragmentManager.findFragmentByTag(meetingRoomListFragmentTag)
        if (meetingRoomListFragment != null) {
            supportFragmentManager.beginTransaction().remove(meetingRoomListFragment).commit()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.meetingRoomDetailsContainer, MeetingRoomDescriptionFragment.getInstance())
            .replace(R.id.meetingRoomMeetingListContainer, MeetingListFragment.getInstance())
            .commit()
        supportActionBar?.title = meetingRoom.name
    }

    private fun updateMeetingRoomWithMeetings(
        meetingRoom: MeetingRoom,
        meetings: List<Meeting>
    ): MeetingRoom =
        this.run {
            MeetingRoom(
                id = meetingRoom.id,
                name = meetingRoom.name,
                meetingList = meetings
            )
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
                    val newMeetingRoomObject = updateMeetingRoomWithMeetings(meetingRoom, it)
                    saveMeetingRoomIntoPreference(newMeetingRoomObject)
                    meetingRoomSubject.onNext(newMeetingRoomObject)
                }) { Log.e(TAG, it.toString()) }
        )
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
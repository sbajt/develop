package com.scorealarm.meeting.rooms.activities

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
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
import org.joda.time.DateTime
import org.joda.time.Duration
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    var meetingRoomSubject = ReplaySubject.createWithSize<MeetingRoom>(1)

    private val wifiManager: WifiManager by lazy { getSystemService(Context.WIFI_SERVICE) as WifiManager }

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
            showMeetingRoomListFragment()
        } else {
            refreshMeetingRoomSubjectOnDayChange(meetingRoom)
            updateMeetingListInMeetingRoomByPeriod(
                meetingRoom,
                Config.DATA_REFRESH_RATE_IN_SECONDS,
                TimeUnit.SECONDS
            )
            if (meetingRoom.meetingList.isNullOrEmpty())
                showEmptyFragment(ListDisplayType.MEETING_LIST)
            else
                showMeetingListFragment()

            meetingRoomSubject.onNext(meetingRoom)
            showMeetingRoomDescriptionFragment(meetingRoom)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.removePersistedData -> {
                removePersistedMeetingRoom()
                meetingRoomSubject = ReplaySubject.createWithSize(1)
                showMeetingRoomListFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun onSelectMeetingRoom(meetingRoom: MeetingRoom) {
        compositeDisposable.add(
            RestService.fetchMeetingList(meetingRoom.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val newMeetingRoomObject = updateMeetingRoomWithMeetings(meetingRoom, it)
                    meetingRoomSubject.onNext(newMeetingRoomObject)
                    saveMeetingRoomIntoPreference(newMeetingRoomObject)
                    showMeetingRoomDescriptionFragment(meetingRoom)
                    if (it.isNullOrEmpty()) {
                        showEmptyFragment(ListDisplayType.MEETING_LIST)
                    } else {
                        showMeetingListFragment()
                    }
                }, { Log.e(TAG, it.toString()) })
        )
    }

    fun showEmptyFragment(type: ListDisplayType) {
        var text = ""
        var layoutRes = 0
        when (type) {
            ListDisplayType.MEETING_ROOM_LIST -> {
                text = getString(R.string.empty_meeting_room_list)
                layoutRes = R.id.meetingRoomListContainer
            }
            ListDisplayType.MEETING_LIST -> {
                text = getString(R.string.empty_meeting_list)
                layoutRes = R.id.meetingRoomMeetingListContainer
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(layoutRes, EmptyListFragment.getInstance(text))
            .commit()
    }

    private fun showMeetingRoomDescriptionFragment(meetingRoom: MeetingRoom) {
        val meetingRoomListFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.meeting_room_list_fragment_tag))
        if (meetingRoomListFragment != null) {
            supportFragmentManager.beginTransaction().remove(meetingRoomListFragment).commit()
        }
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.meetingRoomDescriptionContainer,
                MeetingRoomDescriptionFragment.getInstance()
            )
            .commit()
        supportActionBar?.title = meetingRoom.name
    }

    private fun showMeetingListFragment() {
        val meetingListFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.meeting_list_fragment_tag))
        if (meetingListFragment != null)
            supportFragmentManager.beginTransaction()
                .remove(meetingListFragment)
                .commit()
        supportFragmentManager.beginTransaction()
            .replace(R.id.meetingRoomMeetingListContainer, MeetingListFragment.getInstance())
            .commit()
    }

    private fun showMeetingRoomListFragment() {
        val meetingsListFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.meeting_room_list_fragment_tag))
        if (meetingsListFragment != null) {
            supportFragmentManager.beginTransaction().remove(meetingsListFragment).commit()
        }
        val meetingDescriptionFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.meeting_description_fragment_tag))
        if (meetingDescriptionFragment != null)
            supportFragmentManager.beginTransaction()
                .remove(meetingDescriptionFragment)
                .commit()
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.meetingRoomListContainer,
                MeetingRoomListFragment.getInstance(),
                getString(R.string.meeting_room_list_fragment_tag)
            )
            .commit()
        supportActionBar?.title = "Meeting room chooser"
    }

    private fun saveMeetingRoomIntoPreference(meetingRoom: MeetingRoom) {
        getPreferences(Context.MODE_PRIVATE)?.edit()
            ?.putString(meetingRoomKey, RestService.gson.toJson(meetingRoom))
            ?.apply()
    }

    private fun removePersistedMeetingRoom() {
        getPreferences(Context.MODE_PRIVATE).edit()
            .remove(meetingRoomKey)
            .apply()

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

    private fun refreshMeetingRoomSubjectOnDayChange(meetingRoom: MeetingRoom) {
        compositeDisposable.add(Observable.timer(
            Duration(
                DateTime.now(),
                DateTime.now().plusDays(1).withTimeAtStartOfDay()
            ).standardSeconds, TimeUnit.SECONDS
        )
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                meetingRoomSubject.onNext(meetingRoom)
            }) { Log.d(TAG, it.toString()) })
    }

    private fun updateMeetingListInMeetingRoomByPeriod(
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
                    if (it.isNullOrEmpty()) {
                        showEmptyFragment(ListDisplayType.MEETING_LIST)
                        showMeetingRoomDescriptionFragment(meetingRoom)
                    } else {
                        val newMeetingRoomObject = updateMeetingRoomWithMeetings(meetingRoom, it)
                        saveMeetingRoomIntoPreference(newMeetingRoomObject)
                        meetingRoomSubject.onNext(newMeetingRoomObject)
                    }
                }) { Log.e(TAG, it.toString()) }
        )
    }

    companion object {

        const val meetingRoomKey = "meetingRoom"

        private val TAG = MainActivity::class.java.canonicalName

    }

}
package com.scorealarm.meeting.rooms.activities

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
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

    val meetingRoomSubject = ReplaySubject.createWithSize<MeetingRoom>(1)

    private val wifiManager: WifiManager by lazy { getSystemService(Context.WIFI_SERVICE) as WifiManager }

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
            showMeetingRoomListFragment()
        } else {
            refreshMeetingRoomSubjectOnDayChange(meetingRoom)
            updateMeetingListInMeetingRoomByPeriod(
                meetingRoom,
                Config.DATA_REFRESH_RATE_IN_SECONDS,
                TimeUnit.SECONDS
            )
            if (meetingRoom.meetingList.isNullOrEmpty()) {
                showEmptyFragment(ListDisplayType.MEETING_LIST)
                showMeetingRoomDescriptionFragment(meetingRoom)
            } else
                onSelectMeetingRoom(meetingRoom)

            invalidateOptionsMenu()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.removePersistedData)?.isVisible =
            getPreferences(Context.MODE_PRIVATE).contains(meetingRoomKey)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.removePersistedData -> {
                removePersistedMeetingRoom()
                showMeetingRoomListFragment()
                invalidateOptionsMenu()
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
                    if (it.isNullOrEmpty()) {
                        showEmptyFragment(ListDisplayType.MEETING_LIST)
                        showMeetingRoomDescriptionFragment(meetingRoom)
                    } else {
                        val newMeetingRoomObject = updateMeetingRoomWithMeetings(meetingRoom, it)
                        saveMeetingRoomIntoPreference(newMeetingRoomObject)
                        meetingRoomSubject.onNext(newMeetingRoomObject)
                        showMeetingRoomDetails(newMeetingRoomObject)
                    }
                    invalidateOptionsMenu()
                }, { Log.e(TAG, it.toString()) })
        )
    }

    fun showEmptyFragment(type: ListDisplayType) {
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

    private fun showMeetingRoomDescriptionFragment(meetingRoom: MeetingRoom) {
        val meetingRoomListFragment =
            supportFragmentManager.findFragmentByTag(meetingRoomListFragmentTag)
        if (meetingRoomListFragment != null) {
            supportFragmentManager.beginTransaction().remove(meetingRoomListFragment).commit()
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.meetingRoomDetailsContainer, MeetingRoomDescriptionFragment.getInstance())
            .commit()
        supportActionBar?.title = meetingRoom.name
    }

    private fun showMeetingRoomDetails(meetingRoom: MeetingRoom) {
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

    private fun showMeetingRoomListFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.meetingRoomListContainer,
                MeetingRoomListFragment.getInstance(),
                meetingRoomListFragmentTag
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
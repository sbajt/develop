package com.scorealarm.meeting.rooms.activities

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.startup.AppInitializer
import com.google.android.material.snackbar.Snackbar
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.fragments.MeetingRoomDescriptionFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomMeetingsListFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomsListFragment
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.ReplaySubject
import kotlinx.android.synthetic.main.activity_main.*
import net.danlew.android.joda.JodaTimeInitializer
import org.joda.time.DateTime
import org.joda.time.Duration
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val wifiManager: WifiManager by lazy { getSystemService(Context.WIFI_SERVICE) as WifiManager }
    private val connectivityManager: ConnectivityManager by lazy { getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    private val compositeDisposable = CompositeDisposable()

    val meetingRoomSubject = ReplaySubject.createWithSize<MeetingRoom?>(1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppInitializer.getInstance(this).initializeComponent(JodaTimeInitializer::class.java)
    }

    override fun onStart() {
        super.onStart()

        observeInternetState()

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
                Config.MEETING_LIST_REFRESH_RATE_IN_SECONDS,
                TimeUnit.SECONDS
            )
            compositeDisposable.add(RestService.fetchMeetingListByRoom(meetingRoom.id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    meetingRoomSubject.onNext(meetingRoom.copy(meetingList = it))
                    showMeetingRoomDescriptionFragment()
                    showOngoingMeetingFragment()
                    showMeetingRoomMeetingsListFragment()
                }) { Log.e(TAG, it.toString()) })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    fun onSelectMeetingRoom(meetingRoom: MeetingRoom) =
        compositeDisposable.add(
            RestService.fetchMeetingListByRoom(meetingRoom.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    removeMeetingRoomListFragment()
                    showOngoingMeetingFragment()
                    showMeetingRoomMeetingsListFragment()
                    meetingRoomSubject.onNext(meetingRoom.copy(meetingList = it))
                }, { Log.e(TAG, it.toString()) })
        )

    fun onClearViewClick() {
        removeMeetingRoomDescriptionsFragment()
        removeOngoingMeetingFragment()
        removeMeetingRoomMeetingsListFragment()
        meetingRoomSubject.cleanupBuffer()
        showMeetingRoomListFragment()
    }

    private fun persistMeetingRoom(meetingRoom: MeetingRoom) =
        getPreferences(Context.MODE_PRIVATE)?.edit()
            ?.putString(meetingRoomKey, RestService.gson.toJson(meetingRoom))
            ?.apply()

    private fun observeInternetState() {
        val snackbar = Snackbar.make(
            containerView,
            R.string.label_internet_off,
            Snackbar.LENGTH_INDEFINITE
        )
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                snackbar.dismiss()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                snackbar.show()
            }

        })
    }

    private fun showMeetingRoomListFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.containerView,
                MeetingRoomsListFragment(),
                getString(R.string.meeting_room_list_fragment_tag)
            )
            .commit()
    }

    private fun removeMeetingRoomListFragment() {
        val meetingRoomListFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.meeting_room_list_fragment_tag))
        if (meetingRoomListFragment != null)
            supportFragmentManager.beginTransaction()
                .detach(meetingRoomListFragment)
                .commit()
    }

    private fun showMeetingRoomDescriptionFragment() {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.meetingRoomDescriptionContainer,
                    MeetingRoomsListFragment(),
                    getString(R.string.meeting_room_description_fragment_tag)
                )
                .commit()
    }

    private fun removeMeetingRoomDescriptionsFragment() {
        val meetingRoomDescriptionFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.meeting_room_description_fragment_tag))
        if (meetingRoomDescriptionFragment != null)
            supportFragmentManager.beginTransaction()
                .detach(meetingRoomDescriptionFragment)
                .commit()
    }

    private fun showOngoingMeetingFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.ongoingMeetingContainer,
                MeetingRoomDescriptionFragment(),
                getString(R.string.ongoing_meeting_fragment_tag)
            )
            .commit()
    }

    private fun removeOngoingMeetingFragment() {
        val ongoingMeetingFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.ongoing_meeting_fragment_tag))
        if (ongoingMeetingFragment != null)
            supportFragmentManager.beginTransaction()
                .detach(ongoingMeetingFragment)
                .commit()
    }

    private fun showMeetingRoomMeetingsListFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.meetingRoomMeetingsListContainer,
                MeetingRoomMeetingsListFragment(),
                getString(R.string.meeting_room_list_fragment_tag)
            )
            .commit()
    }

    private fun removeMeetingRoomMeetingsListFragment() {
        val meetingRoomMeetingsListFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.meeting_room_meetings_list_fragment_tag))
        if (meetingRoomMeetingsListFragment != null)
            supportFragmentManager.beginTransaction()
                .detach(meetingRoomMeetingsListFragment)
                .commit()
    }

    private fun refreshMeetingRoomSubjectOnDayChange(meetingRoom: MeetingRoom) =
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


    private fun updateMeetingListInMeetingRoomByPeriod(
        meetingRoom: MeetingRoom,
        period: Long,
        periodUnit: TimeUnit
    ) =
        compositeDisposable.add(
            Observable.interval(period, periodUnit, Schedulers.newThread())
                .filter { wifiManager.isWifiEnabled }
                .flatMap { RestService.fetchMeetingListByRoom(meetingRoom.id) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val newMeetingRoomObject = meetingRoom.copy(meetingList = it)
                    if (it.isNotEmpty())
                        persistMeetingRoom(newMeetingRoomObject)
                    meetingRoomSubject.onNext(newMeetingRoomObject)
                }) { Log.e(TAG, it.toString()) }
        )

    companion object {

        const val meetingRoomKey = "meetingRoom"

        private val TAG = MainActivity::class.java.canonicalName

    }

}
package com.scorealarm.meeting.rooms.activities

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.startup.AppInitializer
import com.google.android.material.snackbar.Snackbar
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.fragments.MeetingListFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomDescriptionFragment
import com.scorealarm.meeting.rooms.fragments.MeetingRoomListFragment
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import com.scorealarm.meeting.rooms.utils.Utils.updateMeetings
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

    var meetingRoomSubject = ReplaySubject.createWithSize<MeetingRoom?>(1)

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
            if (!meetingRoom.meetingList.isNullOrEmpty()) {
                showMeetingListFragment()
            }

            meetingRoomSubject.onNext(meetingRoom)
            showMeetingRoomDescriptionFragment()
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

    fun onSelectMeetingRoom(meetingRoom: MeetingRoom) =
        compositeDisposable.add(
            RestService.fetchMeetingList(meetingRoom.id)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val newMeetingRoomObject = meetingRoom.updateMeetings(it)
                    meetingRoomSubject.onNext(newMeetingRoomObject)
                    saveMeetingRoomIntoPreference(newMeetingRoomObject)
                    showMeetingRoomDescriptionFragment()
                    showMeetingListFragment()
                }, { Log.e(TAG, it.toString()) })
        )

    fun saveMeetingRoomIntoPreference(meetingRoom: MeetingRoom) =
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

    private fun showMeetingRoomDescriptionFragment() {
        removeMeetingRoomListFragment()
        supportFragmentManager.commit {
            replace<MeetingRoomDescriptionFragment>(
                R.id.meetingRoomDescriptionContainer,
                getString(R.string.meeting_description_fragment_tag)
            )
        }
    }

    private fun showMeetingListFragment() {
        removeMeetingRoomListFragment()
        supportFragmentManager.commit {
            replace<MeetingListFragment>(
                R.id.meetingListContainer,
                getString(R.string.meeting_list_fragment_tag)
            )
        }
    }

    private fun showMeetingRoomListFragment() {
        val meetingListFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.meeting_list_fragment_tag))
        val meetingDescriptionFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.meeting_description_fragment_tag))
        supportFragmentManager.commit {
            if (meetingDescriptionFragment != null)
                detach(meetingDescriptionFragment)
            if (meetingListFragment != null)
                detach(meetingListFragment)

            replace<MeetingRoomListFragment>(
                R.id.containerView,
                getString(R.string.meeting_room_list_fragment_tag)
            )
        }
    }

    private fun removeMeetingRoomListFragment() {
        val meetingRoomListFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.meeting_room_list_fragment_tag))
        if (meetingRoomListFragment != null)
            supportFragmentManager.commit { detach(meetingRoomListFragment) }
    }

    private fun removePersistedMeetingRoom() =
        getPreferences(Context.MODE_PRIVATE).edit()
            .remove(meetingRoomKey)
            .apply()


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
                .flatMap { RestService.fetchMeetingList(meetingRoom.id) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val newMeetingRoomObject = meetingRoom.updateMeetings(it)
                    if (it.isNotEmpty())
                        saveMeetingRoomIntoPreference(newMeetingRoomObject)
                    meetingRoomSubject.onNext(newMeetingRoomObject)
                    showMeetingRoomDescriptionFragment()
                }) { Log.e(TAG, it.toString()) }
        )

    companion object {

        const val meetingRoomKey = "meetingRoom"

        private val TAG = MainActivity::class.java.canonicalName

    }

}
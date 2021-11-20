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
import com.scorealarm.meeting.rooms.fragments.MeetingRoomListFragment
import com.scorealarm.meeting.rooms.fragments.MeetingsListFragment
import com.scorealarm.meeting.rooms.fragments.OngoingMeetingFragment
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.models.types.MeetingStateType
import com.scorealarm.meeting.rooms.rest.RestService
import com.scorealarm.meeting.rooms.utils.Utils.state
import com.scorealarm.meeting.rooms.utils.Utils.updateMeetingRoom
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

    private var isUpdatingOnDayChange = false
    private var isUpdatingWithTimer = false

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
            removeMeetingRoomDescriptionFragment()
            removeOngoingMeetingFragment()
            removeMeetingRoomListFragment()
            meetingRoomSubject.cleanupBuffer()
            showMeetingRoomListFragment()
        } else {
            showMeetingRoomDetailsFragments(meetingRoom.meetingList ?: emptyList())
            updateMeetingRoomSubjectOnDayChange(meetingRoom)
            updateMeetingRoomSubjectWithTimer(
                meetingRoom,
                Config.MEETING_LIST_REFRESH_RATE_IN_SECONDS,
                TimeUnit.SECONDS
            )
            meetingRoomSubject.onNext(meetingRoom)
        }
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
        isUpdatingOnDayChange = false
        isUpdatingWithTimer = false
    }

    fun onSelectMeetingRoom(meetingRoom: MeetingRoom) {
        if (wifiManager.isWifiEnabled)
            compositeDisposable.add(
                RestService.getMeetingListByRoom(meetingRoom.id)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        updateMeetingRoomSubjectOnDayChange(meetingRoom)
                        updateMeetingRoomSubjectWithTimer(
                            meetingRoom,
                            Config.MEETING_LIST_REFRESH_RATE_IN_SECONDS,
                            TimeUnit.SECONDS
                        )
                        val newMeetingRoomObject = it.updateMeetingRoom(meetingRoom)
                        if (it.isNotEmpty())
                            persistMeetingRoom(newMeetingRoomObject)
                        removeMeetingRoomListFragment()
                        showMeetingRoomDetailsFragments(it)
                        meetingRoomSubject.onNext(meetingRoom.copy(meetingList = it))
                    }, { Log.e(TAG, it.toString()) })
            )
    }

    fun onClearViewClick() {
        removeMeetingRoomDescriptionFragment()
        removeOngoingMeetingFragment()
        removeMeetingsListFragment()
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
                MeetingRoomListFragment(),
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
                MeetingRoomDescriptionFragment(),
                getString(R.string.meeting_room_description_fragment_tag)
            )
            .commit()
    }

    private fun removeMeetingRoomDescriptionFragment() {
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
                OngoingMeetingFragment(),
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

    private fun showMeetingListFragment() {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.listFragmentContainer,
                MeetingsListFragment(),
                getString(R.string.meeting_room_list_fragment_tag)
            )
            .commit()
    }

    private fun removeMeetingsListFragment() {
        val meetingRoomMeetingsListFragment =
            supportFragmentManager.findFragmentByTag(getString(R.string.meeting_room_meetings_list_fragment_tag))
        if (meetingRoomMeetingsListFragment != null)
            supportFragmentManager.beginTransaction()
                .detach(meetingRoomMeetingsListFragment)
                .commit()
    }

    private fun updateMeetingRoomSubjectOnDayChange(meetingRoom: MeetingRoom) {
        isUpdatingOnDayChange = true
        compositeDisposable.add(Observable.timer(
            Duration(
                DateTime.now(),
                DateTime.now().withTimeAtStartOfDay().plusDays(1)
            ).standardSeconds, TimeUnit.SECONDS

        )
            .filter { wifiManager.isWifiEnabled && !isUpdatingOnDayChange }
            .flatMap { RestService.getMeetingListByRoom(meetingRoom.id) }
            .subscribeOn(Schedulers.newThread())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ updateMeetingList(it, meetingRoom) }) { Log.d(TAG, it.toString()) })
    }

    private fun updateMeetingRoomSubjectWithTimer(
        meetingRoom: MeetingRoom,
        period: Long,
        periodUnit: TimeUnit
    ) {
        isUpdatingWithTimer = true
        compositeDisposable.add(
            Observable.interval(period, periodUnit, Schedulers.newThread())
                .filter { wifiManager.isWifiEnabled }
                .flatMap { RestService.getMeetingListByRoom(meetingRoom.id) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ updateMeetingList(it, meetingRoom) }) { Log.e(TAG, it.toString()) }
        )
    }

    private fun updateMeetingList(
        meetings: List<Meeting>,
        meetingRoom: MeetingRoom
    ) {
        val newMeetingRoomObject = meetings.updateMeetingRoom(meetingRoom)
        if (meetings.isNotEmpty())
            persistMeetingRoom(newMeetingRoomObject)
        showMeetingRoomDetailsFragments(meetings)
        meetingRoomSubject.onNext(newMeetingRoomObject)
    }

    private fun showMeetingRoomDetailsFragments(meetingList: List<Meeting>) {
        if (!MeetingRoomDescriptionFragment.isAlive)
            showMeetingRoomDescriptionFragment()
        if (!OngoingMeetingFragment.isAlive && meetingList.any { it.state() == MeetingStateType.ONGOING || it.state() == MeetingStateType.ALL_DAY })
            showOngoingMeetingFragment()
        if (!MeetingsListFragment.isAlive)
            showMeetingListFragment()
    }


    companion object {

        const val meetingRoomKey = "meetingRoom"

        private val TAG = MainActivity::class.java.canonicalName

    }

}
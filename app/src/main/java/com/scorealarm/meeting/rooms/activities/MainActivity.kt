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
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import net.danlew.android.joda.JodaTimeInitializer

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    val meetingRoomSubject = BehaviorSubject.create<MeetingRoom?>()

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AppInitializer.getInstance(this).initializeComponent(JodaTimeInitializer::class.java)

        val meetingRoom = RestService.gson.fromJson(
            getPreferences(Context.MODE_PRIVATE).getString(meetingRoomKey, ""),
            MeetingRoom::class.java
        )
        navigate(meetingRoom) {
            if (it) observeMeetingRoom()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun observeMeetingRoom() {
        compositeDisposable.add(
            meetingRoomSubject.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::navigate) { Log.e(TAG, it.toString()) })
    }

    private fun navigate(meetingRoom: MeetingRoom?, meetingRoomChooser: (Boolean) -> Unit = {}) {
        if (meetingRoom == null) {
            supportFragmentManager.commit {
                replace<MeetingRoomListFragment>(R.id.containerLayout)
            }
            meetingRoomChooser.invoke(true)
        } else {
            if (meetingRoomSubject.value == null) {
                compositeDisposable.dispose()
                meetingRoomSubject.onNext(meetingRoom)
            }

            supportFragmentManager.run {
                commit {
                    remove(MeetingRoomListFragment())
                    replace<MeetingRoomDescriptionFragment>(R.id.meetingRoomDescriptionContainer)
                    replace<MeetingListFragment>(R.id.meetingListContainer)
                }
            }
            meetingRoomChooser.invoke(false)
        }
    }

    companion object {

        const val meetingRoomKey = "meetingRoom"

        private val TAG = MainActivity::class.java.canonicalName
    }


}
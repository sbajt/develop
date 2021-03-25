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

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    val meetingRoomSubject = ReplaySubject.create<MeetingRoom>(1)

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
                replace<MeetingRoomListFragment>(R.id.containerLayout)
            }
            supportActionBar?.title = "Meeting room chooser"
        } else {
            meetingRoomSubject.onNext(meetingRoom)
            supportFragmentManager.commit {
                remove(MeetingRoomListFragment.getInstance())
                replace<MeetingRoomDescriptionFragment>(R.id.meetingRoomDescriptionContainer)
                replace<MeetingListFragment>(R.id.meetingListContainer)
            }
            supportActionBar?.title = meetingRoom.name
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    companion object {

        const val meetingRoomKey = "meetingRoom"

        private val TAG = MainActivity::class.java.canonicalName

        fun getMeetingRoomData(meetingRoomId: String): Observable<String> =
            RestService.getMockData()
    }


}
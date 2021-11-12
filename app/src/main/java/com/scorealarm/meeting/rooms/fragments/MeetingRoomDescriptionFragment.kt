package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.models.MeetingRoom
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_meeting_room_description.*
import org.joda.time.DateTime


class MeetingRoomDescriptionFragment : Fragment(R.layout.fragment_meeting_room_description) {

    private val compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        observeMeetingRoomSubject()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun runClock() {
        timeView?.text = DateTime.now().toString("HH:mm")
        compositeDisposable.add(
            Observable.interval(
                Config.CLOCK_PERIOD,
                Config.CLOCK_TIME_UNIT,
                Schedulers.newThread()
            )
                .map { DateTime.now() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    timeView?.text = it.toString("HH:mm")
                }) { Log.e(TAG, it.toString()) }
        )
    }

    private fun observeMeetingRoomSubject() {
        compositeDisposable.add(
            (activity as MainActivity).meetingRoomSubject
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::bindViews) { Log.e(TAG, it.toString()) }
        )
    }

    private fun bindViews(meetingRoom: MeetingRoom?) {
        runClock()
        clearView?.setOnClickListener { (activity as MainActivity).doOnClearViewClick() }
        roomNameView?.text = meetingRoom?.name
    }

    companion object {

        private val TAG = MeetingRoomDescriptionFragment::class.java.canonicalName

    }


}
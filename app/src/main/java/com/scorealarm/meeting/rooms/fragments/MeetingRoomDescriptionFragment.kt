package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.fragments.models.MeetingRoomDescriptionViewModel
import com.scorealarm.meeting.rooms.utils.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_room_description.*
import org.joda.time.DateTime


class MeetingRoomDescriptionFragment : Fragment(R.layout.fragment_meeting_room_description) {

    private val compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        isAlive = true
        observeMeetingRoomSubject()
    }

    override fun onStop() {
        super.onStop()
        isAlive = false
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
                .map { Utils.createMeetingRoomDescriptionViewModel(it) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::bind) { Log.e(TAG, it.toString()) }
        )
    }

    private fun bind(meetingRoomDescriptionViewModel: MeetingRoomDescriptionViewModel?) {
        runClock()
        clearView?.setOnClickListener { (activity as MainActivity).onClearViewClick() }
        roomNameView?.text = meetingRoomDescriptionViewModel?.meeting?.name
    }

    companion object {

        private val TAG = MeetingRoomDescriptionFragment::class.java.canonicalName

        var isAlive = false

    }


}
package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.models.Meeting
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_room_description.*
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class MeetingRoomDescriptionFragment :
    Fragment(R.layout.fragment_meeting_room_description) {

    private val compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        initViews()
        observeMeetingRoomSubject()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun initViews() {
        timeView?.text = DateTime.now().toString("HH:mm")
        runClock()
    }

    private fun runClock() {
        compositeDisposable.add(
            Observable.interval(
                Config.ANIMATE_CLOCK_PERIOD,
                Config.ANIMATE_CLOCK_TIME_UNIT,
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
                .subscribe({ setupDescriptionViews(it.meetingList) })
                { Log.e(TAG, it.toString()) }
        )
    }

    private fun setupDescriptionViews(meetings: List<Meeting>?) {
        if (meetings?.filter {
                it.startDateTime.dayOfMonth() == DateTime.now()
                    .dayOfMonth() && it.endDateTime.dayOfMonth() == DateTime.now().dayOfMonth()
            }
                .isNullOrEmpty()) {
            currentMeetingNameView?.run {
                text = ""
                textSize = 24f
            }
            currentMeetingTimeView?.text = ""
            currentMeetingOrganizerView?.text = ""
        } else {
            compositeDisposable.add(Observable.interval(1, TimeUnit.SECONDS, Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    val currentMeeting =
                        meetings?.find { it.startDateTime.isBeforeNow && it.endDateTime.isAfterNow }
                    if (currentMeeting == null) {
                        currentMeetingNameView?.run {
                            text = "No meeting in progress"
                            textSize = 16f
                        }
                        currentMeetingTimeView?.text = ""
                        currentMeetingOrganizerView?.text = ""
                        invitesCountView?.text = ""
                    } else {
                        currentMeetingTimeView?.text =
                            currentMeeting.startDateTime.toString("HH:mm") + " - " + currentMeeting.endDateTime.toString(
                                "HH:mm"
                            )
                        currentMeetingNameView?.text = "${currentMeeting.title}"
                        currentMeetingOrganizerView?.text = "${currentMeeting.organizer}"
                        invitesCountView?.text = "Invites: ${currentMeeting.invitesNumber}"
                    }
                }) { Log.d(TAG, it.toString()) }
            )
        }
    }


    companion object {

        private val TAG = MeetingRoomDescriptionFragment::class.java.canonicalName

        fun getInstance() = MeetingRoomDescriptionFragment()

    }


}
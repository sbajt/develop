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

    private var currentMeeting: Meeting? = null

    override fun onStart() {
        super.onStart()
        initViews()
        runClock()
        observeMeetingRoomSubject()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun initViews() {
        timeView?.text = DateTime.now().toString("HH:mm")
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
        val currentMeeting = meetings?.find {
            it.startDateTime.withTimeAtStartOfDay() == DateTime.now().withTimeAtStartOfDay()
                    && it.endDateTime.withTimeAtStartOfDay() == DateTime.now()
                .withTimeAtStartOfDay()
        }
        bindViews(currentMeeting)
        if (currentMeeting != null) {
            periodicallyRefreshDescriptionViews(meetings)
        }
    }

    private fun periodicallyRefreshDescriptionViews(meetings: List<Meeting>?) {
        compositeDisposable.add(Observable.interval(
            1,
            TimeUnit.SECONDS,
            Schedulers.newThread()
        )
            .map { meetings?.find { it.startDateTime.isBeforeNow && it.endDateTime.isAfterNow } }
            .filter { it != currentMeeting }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                bindViews(it)
                currentMeeting = it
            }) { Log.d(TAG, it.toString()) }
        )
    }

    private fun bindViews(meeting: Meeting?) {
        currentMeetingNameView.text = if (meeting == null) "" else meeting.title
        currentMeetingNameView.textSize = if (meeting == null) 16f else 24f
        currentMeetingTimeView.text = if (meeting == null) "" else
            "${meeting.startDateTime.toString("HH:mm")} - ${meeting.endDateTime.toString("HH:mm")}"
        currentMeetingOrganizerView.text = if (meeting == null) "" else "${meeting.organizer}"
        invitesCountView.text = if (meeting == null) "" else "Invites: ${meeting.invitesNumber}"
    }


    companion object {

        private val TAG = MeetingRoomDescriptionFragment::class.java.canonicalName

        fun getInstance() = MeetingRoomDescriptionFragment()

    }


}
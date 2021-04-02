package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.models.MeetingRoom
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
        initClock()
        observeMeetingRoom()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun initClock() {
        compositeDisposable.add(
            Observable.just(DateTime.now())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    setTimeText(it)
                    runClock()
                }, { Log.e(TAG, it.toString()) })
        )

    }

    private fun setTimeText(dateTime: DateTime) {
        timeView?.text = dateTime.toString("HH:mm")
    }

    private fun runClock() {
        compositeDisposable.add(
            Observable.interval(1, TimeUnit.MINUTES, Schedulers.newThread())
                .map { DateTime.now() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::setTimeText) { Log.e(TAG, it.toString()) }
        )
    }

    private fun observeMeetingRoom() {
        compositeDisposable.add(
            (activity as MainActivity).meetingRoomSubject
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::setupDescriptionViews) { Log.e(TAG, it.toString()) }
        )
    }

    private fun setupDescriptionViews(meetingRoom: MeetingRoom) {
        if (meetingRoom.meetingList?.isNullOrEmpty() == true) {
            meetingDescription1View?.text = "No meetings scheduled today"
            meetingDescription2View?.text = ""
        } else {
            val currentMeeting =
                meetingRoom.meetingList.find { it.startDateTime.isBeforeNow && it.endDateTime.isAfterNow }
            if (currentMeeting == null) {
                meetingDescription1View?.text = "No meeting in progress"
                meetingDescription2View?.text = ""
            } else {
                meetingDescription1View?.text = "${currentMeeting.title}"
                meetingDescription2View?.text =
                    currentMeeting.startDateTime.toString("HH:mm") + " - " + currentMeeting.endDateTime.toString(
                        "HH:mm"
                    ) + System.lineSeparator() +
                            "Organizer: ${currentMeeting.organizer}" + System.lineSeparator() +
                            "Attendee count: ${currentMeeting.invitesNumber}"
            }
        }
    }

    companion object {

        private val TAG = MeetingRoomDescriptionFragment::class.java.canonicalName

        fun getInstance() = MeetingRoomDescriptionFragment()

    }


}
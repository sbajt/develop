package com.scorealarm.meeting.rooms.fragments

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_room_description.*
import org.joda.time.DateTime
import java.util.concurrent.TimeUnit

class MeetingRoomDescriptionFragment : Fragment(R.layout.fragment_meeting_room_description) {

    private val compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        compositeDisposable.add(
            Observable.just(
                RestService.gson.fromJson(
                    activity?.getPreferences(Context.MODE_PRIVATE)
                        ?.getString(MainActivity.meetingRoomKey, ""), MeetingRoom::class.java
                )
            )
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    meetingRoomNameView?.text = it?.name
                    setDescriptionViews(it)
                    timeView?.text = DateTime.now().toString("HH:mm:ss")
                    runClock(it)
                 }, { Log.e(TAG, it.toString()) })
        )
//        setupButtonViews()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun runClock(meetingRoom: MeetingRoom) {
        compositeDisposable.add(
            Observable.interval(1, TimeUnit.SECONDS, Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    timeView?.text = DateTime.now().toString("HH:mm:ss")
                    setDescriptionViews(meetingRoom)
//                    if (it > 0 && it % 15L == 0L) {
//                        (activity as? MainActivity)?.updateMeetingListInMeetingRoom(meetingRoom)
//                    }
                }) { Log.e(TAG, it.toString()) }
        )
    }

    private fun setDescriptionViews(meetingRoom: MeetingRoom) {
        val currentMeeting =
            meetingRoom.meetingList.find { it.startDateTime.isBeforeNow && it.endDateTime.isAfterNow } // FIX
        if (currentMeeting == null) {
            meetingDescription1View?.text = "No meeting in progress."
            meetingDescription2View?.text = ""
        } else {
            meetingDescription1View?.text = "${currentMeeting.title}\n" +
                    currentMeeting.startDateTime.toString("HH:mm") +
                    "- ${currentMeeting.endDateTime.toString("HH:mm")}"
            meetingDescription2View?.text =
                "Organizer: ${currentMeeting.organizer}\n" +
                        "Attendee count: ${currentMeeting.invitesNumber}"
        }
    }

    private fun setupButtonViews() {
        extendButtonView?.run {
            isClickable = true
            setOnClickListener {
                Log.d(TAG, "Extend meeting clicked")
                it.isClickable = false
            }
        }
        endNowButtonView?.run {
            isClickable = true
            setOnClickListener {
                Log.d(TAG, "End now meeting clicked")
                it.isClickable = false
            }
        }
    }


    companion object {

        private val TAG = MeetingRoomDescriptionFragment::class.java.canonicalName

    }


}
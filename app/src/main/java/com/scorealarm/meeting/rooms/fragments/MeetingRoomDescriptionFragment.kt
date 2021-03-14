package com.scorealarm.meeting.rooms.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
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
    private val meetingRoom = RestService.gson.fromJson(
        activity?.getPreferences(Context.MODE_PRIVATE)
            ?.getString(MainActivity.meetingRoomKey, "{}"),
        MeetingRoom::class.java
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeMeetingRoom()
        setupButtonViews()
        timeView?.text = DateTime.now().toString("HH:mm")
//        runClock()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    private fun observeMeetingRoom() {
        compositeDisposable.add(
            (activity as MainActivity).meetingRoomSubject
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    meetingRoomNameView?.text = it?.name ?: ""
                    setDescriptionViews(it)
                }) { Log.e(TAG, it.toString()) }
        )
    }

    private fun runClock() {
        compositeDisposable.add(
            Observable.interval(1, TimeUnit.SECONDS, Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    timeView?.text = DateTime.now().toString("HH:mm:ss")
                    setDescriptionViews(meetingRoom)
//                    if(it % 15 == 0L)

                }) { Log.e(TAG, it.toString()) }
        )
    }

    private fun setDescriptionViews(meetingRoom: MeetingRoom?) {
        if (meetingRoom == null) {
            meetingDescription1View?.text = "No meeting in progress."
            meetingDescription2View?.text = ""
        } else {
            val currentMeeting =
                meetingRoom.meetingList.first { it.startDateTime.isBeforeNow && it.endDateTime.isAfterNow }
            meetingDescription1View?.text = "${currentMeeting.title}\n" +
                    currentMeeting.startDateTime.toString("HH:mm") +
                    "- ${currentMeeting.endDateTime.toString("HH:mm")}"
            meetingDescription2View?.text =
                "Organizer: ${currentMeeting.organizer} Attendee count: ${currentMeeting.invitesNumber}"
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
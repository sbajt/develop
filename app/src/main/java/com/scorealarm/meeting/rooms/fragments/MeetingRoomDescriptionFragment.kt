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
import org.joda.time.DateTimeFieldType
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
        setTimeView()
        compositeDisposable.add(
            Observable.just(Any())
                .delay {
                    Observable.timer(
                        DateTime.now()[DateTimeFieldType.secondOfMinute()].toLong(),
                        TimeUnit.SECONDS
                    )
                }
                .flatMap { Observable.just(it) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    setTimeView()
                    compositeDisposable.add(
                        Observable.interval(1, TimeUnit.MINUTES, Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .doOnNext {
                                if (it % 5 == 0L)
                                    compositeDisposable.add(
                                        MainActivity.getData()
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe({ data ->
                                                Log.d(TAG, data)
//                                                (activity as MainActivity).run {
//                                                    updateMeetingListInMeetingRoom(meetingRoom)
//                                                    meetingRoomSubject.onNext(meetingRoom)
//                                                }
                                            }, { Log.e(TAG, it.toString()) })
                                    )
                            }
                            .subscribe({
                                setTimeView()
                                setDescriptionViews(meetingRoom)
                            }) { Log.e(TAG, it.toString()) }
                    )
                }, { Log.e(TAG, it.toString()) })
        )

    }

    private fun setTimeView() {
        timeView?.text = DateTime.now().toString("HH:mm")
    }

    private fun setDescriptionViews(meetingRoom: MeetingRoom) {
        val currentMeeting =
            meetingRoom.meetingList.find { it.startDateTime.isBeforeNow && it.endDateTime.isAfterNow }
        if (currentMeeting == null) {
            meetingDescription1View?.text = "No meeting in progress."
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

    private fun setupButtonViews() {
        extendButtonView?.run {
            isEnabled = true
            setOnClickListener {
                Log.d(TAG, "Extend meeting clicked")
                it.isEnabled = false
            }
        }
        endNowButtonView?.run {
            isEnabled = true
            setOnClickListener {
                Log.d(TAG, "End now meeting clicked")
                it.isEnabled = false
            }
        }
    }


    companion object {

        private val TAG = MeetingRoomDescriptionFragment::class.java.canonicalName

    }


}
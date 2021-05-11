package com.scorealarm.meeting.rooms.fragments

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.MeetingStateType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.utils.Utils.isToday
import com.scorealarm.meeting.rooms.utils.Utils.state
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        timeView?.text = DateTime.now().toString("HH:mm")
        runClock()
        observeMeetingRoomSubject()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.remove_persisted_data, menu)
    }

    private fun runClock() {
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
                .subscribe({ meetingRoom ->
                    bindViews(meetingRoom.meetingList?.filter { it.isToday() })
                    startPeriodicallyUpdatingViews()
                }) { Log.e(TAG, it.toString()) }
        )
    }

    /**
     * Refresh current meeting if any.
     * Updates description views each second.
     **/
    private fun startPeriodicallyUpdatingViews() {
        compositeDisposable.add(Observable.interval(
            1,
            TimeUnit.SECONDS,
            Schedulers.newThread()
        )
            .flatMap { (activity as MainActivity).meetingRoomSubject }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ meetingRoom ->
                meetingRoom.meetingList?.run {
                    bindViews(this.filter { it.isToday() })
                }
            }) { Log.d(TAG, it.toString()) }
        )
    }

    private fun bindViews(todayMeetingList: List<Meeting>?) {
        val ongoingMeeting =
            todayMeetingList?.firstOrNull { it.state() == MeetingStateType.ALL_DAY || it.state() == MeetingStateType.ONGOING }
        currentMeetingTimeView?.run {
            text =
                when {
                    todayMeetingList.isNullOrEmpty() -> ""
                    todayMeetingList.firstOrNull { it.state() == MeetingStateType.ALL_DAY } != null -> "Meeting lasts all day"
                    else -> {
                        "${ongoingMeeting?.startDateTime?.toString("HH:mm")} - ${
                            ongoingMeeting?.endDateTime?.toString("HH:mm")
                        }"
                    }
                }
        }
        currentMeetingNameView?.run {
            text = when {
                todayMeetingList.isNullOrEmpty() -> ""
                ongoingMeeting != null -> ongoingMeeting.title
                else -> ""
            }
        }
        currentMeetingDescriptionView?.run {
            text = when {
                todayMeetingList.isNullOrEmpty() -> ""
                ongoingMeeting != null -> ongoingMeeting.description
                else -> ""
            }
        }
         currentMeetingOrganizerView?.run {
             text = when {
                 todayMeetingList.isNullOrEmpty() -> ""
                 ongoingMeeting != null -> ongoingMeeting.organizer
                 else -> ""
             }
         }
         invitesCountView?.run {
             text = when {
                 todayMeetingList.isNullOrEmpty() -> ""
                 ongoingMeeting != null -> "${ongoingMeeting.invitesNumber}"
                 else -> ""
             }
         }
    }


    companion object {

        private val TAG = MeetingRoomDescriptionFragment::class.java.canonicalName

        fun getInstance() = MeetingRoomDescriptionFragment()

    }


}
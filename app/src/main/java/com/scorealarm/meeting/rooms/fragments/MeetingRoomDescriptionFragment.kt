package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.MeetingStateType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.models.Meeting
import com.scorealarm.meeting.rooms.utils.Utils.isTodayAllDay
import com.scorealarm.meeting.rooms.utils.Utils.state
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_room_description.*
import org.joda.time.DateTime
import org.joda.time.Period
import java.util.concurrent.TimeUnit

class MeetingRoomDescriptionFragment :
    Fragment(R.layout.fragment_meeting_room_description) {

    private val compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        setHasOptionsMenu(true)
        initViews()
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
        val currentMeeting = meetings?.firstOrNull {
            it.startDateTime.isBeforeNow && it.endDateTime.isAfterNow
                    || Period(it.startDateTime, it.endDateTime).minutes == 0
        }
        bindViews(currentMeeting)
        if (currentMeeting != null) {
            periodicallyRefreshViews(meetings)
        }
    }

    private fun periodicallyRefreshViews(meetings: List<Meeting>?) {
        compositeDisposable.add(Observable.interval(
            1,
            TimeUnit.SECONDS,
            Schedulers.newThread()
        )
            .map {
                meetings?.filter {
                    it.state() != MeetingStateType.EXCLUDED
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                bindViews(it?.singleOrNull { it.isTodayAllDay() || it.state() == MeetingStateType.INCLUDED })
            }) { Log.d(TAG, it.toString()) }
        )
    }

    private fun bindViews(meeting: Meeting?) {
        currentMeetingTimeView.text =
            if (meeting.state() != MeetingStateType.EXCLUDED)
                if (meeting.isTodayAllDay())
                    "Meeting lasts all day"
                else
                    "${meeting?.startDateTime?.toString("HH:mm")} - ${
                        meeting?.endDateTime?.toString(
                            "HH:mm"
                        )
                    }"
            else
                ""
        currentMeetingNameView?.run {
            visibility = if (meeting?.title?.isNotBlank() == true) View.VISIBLE else View.GONE
            text = meeting?.title ?: ""
            textSize = if (meeting == null) 16f else 24f
        }
        currentMeetingDescriptionView?.run {
            visibility = if (meeting?.description?.isNotBlank() == true) View.VISIBLE else View.GONE
            text = if (meeting?.description.isNullOrBlank()) "" else meeting?.description
        }
        currentMeetingOrganizerView?.run {
            visibility =
                if (meeting != null && !meeting.organizer.isNullOrBlank()) View.VISIBLE else View.GONE
            text = if (meeting == null) "" else "${meeting.organizer}"
        }
        invitesCountView?.run {
            visibility = if (meeting?.invitesNumber != null) View.VISIBLE else View.GONE
            text = if (meeting == null) "" else "Invites: ${meeting.invitesNumber}"
        }
    }


    companion object {

        private val TAG = MeetingRoomDescriptionFragment::class.java.canonicalName

        fun getInstance() = MeetingRoomDescriptionFragment()

    }


}
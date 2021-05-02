package com.scorealarm.meeting.rooms.fragments

import android.os.Bundle
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
import com.scorealarm.meeting.rooms.utils.Utils.filterToday
import com.scorealarm.meeting.rooms.utils.Utils.isTodayAllDay
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
                .subscribe({ meetingRoom ->
                    val ongoingMeeting =
                        meetingRoom.meetingList?.firstOrNull { it.state() == MeetingStateType.ONGOING }
                    bindViews(ongoingMeeting, meetingRoom.meetingList.filterToday().count() > 1)
                    startPeriodicallyUpdatingViews()
                }) { Log.e(TAG, it.toString()) }
        )
    }

    /**
     * Refresh current meeting if any.
     * Upeates description views each second.
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
                val ongoingMeeting =
                    meetingRoom.meetingList?.firstOrNull { it.state() == MeetingStateType.ONGOING }
                bindViews(ongoingMeeting, meetingRoom.meetingList.filterToday().count() > 1)
            }) { Log.d(TAG, it.toString()) }
        )
    }


    private fun bindViews(ongoingMeeting: Meeting?, isMoreThenOneMeetingToday: Boolean) {
        currentMeetingTimeView?.run {
            visibility =
                when {
                    ongoingMeeting?.startDateTime == null ||
                            ongoingMeeting.state() == MeetingStateType.EXCLUDED -> View.GONE
                    else -> View.VISIBLE
                }
            text =
                when {
                    ongoingMeeting.isTodayAllDay() -> "Meeting lasts all day"
                    else -> "${ongoingMeeting?.startDateTime?.toString("HH:mm")} - ${
                        ongoingMeeting?.endDateTime?.toString("HH:mm")
                    }"
                }
        }
        currentMeetingNameView?.run {
            text = when {
                isMoreThenOneMeetingToday -> "No ongoing meeting"
                ongoingMeeting?.state() == MeetingStateType.ONGOING -> ongoingMeeting.title
                else -> ""
            }
            textSize = if (isMoreThenOneMeetingToday) 24f else 16f
        }
        currentMeetingDescriptionView?.run {
            visibility =
                when {
                    ongoingMeeting?.description.isNullOrBlank()
                            || ongoingMeeting?.state() == MeetingStateType.EXCLUDED -> View.GONE
                    else -> View.VISIBLE
                }
            text =
                if (ongoingMeeting?.description.isNullOrBlank()) "" else ongoingMeeting?.description
        }
        currentMeetingOrganizerView?.run {
            visibility =
                if (ongoingMeeting != null && !ongoingMeeting.organizer.isNullOrBlank()) View.VISIBLE else View.GONE
            text = if (ongoingMeeting == null) "" else "${ongoingMeeting.organizer}"
        }
        invitesCountView?.run {
            visibility =
                if (ongoingMeeting?.invitesNumber != null && ongoingMeeting.invitesNumber > 0) View.VISIBLE else View.GONE
            text = if (ongoingMeeting == null) "" else "Invites: ${ongoingMeeting.invitesNumber}"
        }
    }


    companion object {

        private val TAG = MeetingRoomDescriptionFragment::class.java.canonicalName

        fun getInstance() = MeetingRoomDescriptionFragment()

    }


}
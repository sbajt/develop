package com.scorealarm.meeting.rooms.fragments

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.Config
import com.scorealarm.meeting.rooms.MeetingStateType
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.rest.RestService
import com.scorealarm.meeting.rooms.utils.Utils.filterToday
import com.scorealarm.meeting.rooms.utils.Utils.filterUpcoming
import com.scorealarm.meeting.rooms.utils.Utils.setText
import com.scorealarm.meeting.rooms.utils.Utils.state
import com.scorealarm.meeting.rooms.utils.Utils.stateText
import com.scorealarm.meeting.rooms.utils.Utils.styleAndSetText
import com.scorealarm.meeting.rooms.utils.Utils.updateMeetings
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_meeting_room_description.*
import org.joda.time.DateTime


class MeetingRoomDescriptionFragment :
    Fragment(R.layout.fragment_meeting_room_description) {

    private val compositeDisposable = CompositeDisposable()

    private lateinit var menu: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onStart() {
        super.onStart()
        runClock()
        observeMeetingRoomSubject()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    override fun onDetach() {
        super.onDetach()
        menu.removeItem(R.id.removePersistedData)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.remove_persisted_data, menu)
        this.menu = menu
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
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::bindViews) { Log.e(TAG, it.toString()) }
        )
    }

    private fun bindViews(meetingRoom: MeetingRoom?) {
        val todayMeetingList = meetingRoom?.meetingList?.filterToday()
        if (todayMeetingList == null || todayMeetingList.isEmpty()) {
            ongoingMeetingDescriptionContainer?.visibility = View.GONE
        } else {
            val ongoingMeeting =
                todayMeetingList.find { it.state() == MeetingStateType.ALL_DAY || it.state() == MeetingStateType.ONGOING }
            if (ongoingMeeting == null) {
                ongoingMeetingDescriptionContainer?.visibility = View.GONE
            } else {
                ongoingMeetingDescriptionContainer?.visibility = View.VISIBLE
                ongoingMeetingNameView?.text = ongoingMeeting.title
                ongoingMeetingDescriptionView?.setText(
                    ongoingMeeting,
                    Html.fromHtml(
                        ongoingMeeting.description,
                        Html.FROM_HTML_MODE_COMPACT
                    ).toString()
                )
                ongoingMeetingOrganizerView?.setText(ongoingMeeting, ongoingMeeting.organizer)
                invitesCountView?.setText(ongoingMeeting, ongoingMeeting.invitesNumber?.toString())
                ongoingMeetingTimeView?.text = ongoingMeeting.stateText(context, ongoingMeeting)
            }
        }
        meetingListStatusLabelView?.styleAndSetText(todayMeetingList.filterUpcoming())
        roomNameView?.text = meetingRoom?.name
    }

    companion object {

        private val TAG = MeetingRoomDescriptionFragment::class.java.canonicalName

    }


}
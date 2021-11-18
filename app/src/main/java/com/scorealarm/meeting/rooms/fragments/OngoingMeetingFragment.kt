package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.models.OngoingMeetingViewModel
import com.scorealarm.meeting.rooms.utils.Utils
import com.scorealarm.meeting.rooms.utils.Utils.getOngoingMeeting
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_ongoing_meeting.*


class OngoingMeetingFragment : Fragment(R.layout.fragment_meeting_room_description) {

    private val compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        observeMeetingRoomSubject()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    private fun observeMeetingRoomSubject() {
        compositeDisposable.add(
            (activity as MainActivity).meetingRoomSubject
                .map {
                    Utils.createOngoingMeetingViewModel(
                        activity,
                        it.meetingList?.getOngoingMeeting()
                    )
                }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::bindViews) { Log.e(TAG, it.toString()) }
        )
    }

    private fun bindViews(ongoingMeetingViewModel: OngoingMeetingViewModel?) {
        if (ongoingMeetingViewModel == null) {
            timeView?.text = ""
            meetingNameView?.text = ""
            meetingOrganizerView?.text = ""
            invitesCountView?.text = ""
        } else {
            ongoingMeetingViewModel.meeting?.run {
                timeView?.text = ongoingMeetingViewModel.meetingTimeText
                meetingNameView?.text = title
                meetingOrganizerView?.text = organizer
                invitesCountView?.text = invitesNumber.toString()
            }
        }
    }

    companion object {

        private val TAG = OngoingMeetingFragment::class.java.canonicalName

    }


}
package com.scorealarm.meeting.rooms.fragments

import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.fragments.models.MeetingListViewModel
import com.scorealarm.meeting.rooms.list.MeetingRoomMeetingsListAdapter
import com.scorealarm.meeting.rooms.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list.*

class MeetingsListFragment : Fragment(R.layout.fragment_list) {

    private val listAdapter = MeetingRoomMeetingsListAdapter()
    private val compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        isAlive = true
        recyclerView?.adapter = listAdapter
        setHasOptionsMenu(false)
        compositeDisposable.add(
            (activity as MainActivity).meetingRoomSubject
                .map { Utils.createMeetingListItemViewModel(activity, it.meetingList) }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::bind) { Log.d(TAG, it.toString()) }
        )
    }

    override fun onStop() {
        super.onStop()
        isAlive = false
        compositeDisposable.dispose()
    }


    private fun bind(meetingRoomMeetingListViewModel: MeetingListViewModel) {
        meetingRoomMeetingListViewModel.run {
            listAdapter.update(Utils.mapToMeetingItemViewModel(this.meetingList))
            if (labelData.first != null)
                textView?.run {
                    visibility = View.VISIBLE
                    text = labelData.first
                    if (labelData.second != null)
                        setTextAppearance(labelData.second ?: 0)
                }
        }
    }

    companion object {

        private val TAG = MeetingsListFragment::class.java.canonicalName

        var isAlive = false

    }
}

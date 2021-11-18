package com.scorealarm.meeting.rooms.fragments

import android.graphics.Color
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import com.scorealarm.meeting.rooms.activities.MainActivity
import com.scorealarm.meeting.rooms.list.ListItemActionListener
import com.scorealarm.meeting.rooms.list.MeetingRoomsListAdapter
import com.scorealarm.meeting.rooms.models.MeetingRoom
import com.scorealarm.meeting.rooms.fragments.models.MeetingRoomListViewModel
import com.scorealarm.meeting.rooms.rest.RestService
import com.scorealarm.meeting.rooms.utils.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_list.*


class MeetingRoomListFragment : Fragment(R.layout.fragment_list),
    ListItemActionListener<MeetingRoom> {

    private val listAdapter = MeetingRoomsListAdapter(this)
    private val compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        recyclerView?.adapter = listAdapter
        textView?.visibility = View.GONE
        compositeDisposable.add(
            RestService.getMeetingRoomList()
                .map { Utils.createMeetingRoomListViewModel(it) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(::bind) { Log.d(TAG, it.toString()) }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
    }

    override fun onStop() {
        super.onStop()
        compositeDisposable.dispose()
    }

    override fun onClick(data: MeetingRoom) {
        (activity as MainActivity).onSelectMeetingRoom(data)
    }

    private fun bind(meetingRoomViewModel: MeetingRoomListViewModel) {
        meetingRoomViewModel.run {
            recyclerView?.setBackgroundColor(Color.BLACK)
            listAdapter.update(this.meetingRoomList)
        }
    }

    companion object {

        private val TAG = MeetingRoomListFragment::class.java.canonicalName

    }


}
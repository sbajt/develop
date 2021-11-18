package com.scorealarm.meeting.rooms.fragments

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
import com.scorealarm.meeting.rooms.rest.RestService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_list.*


class MeetingRoomsListFragment : Fragment(R.layout.fragment_list),
    ListItemActionListener<MeetingRoom> {

    private val listAdapter = MeetingRoomsListAdapter(this)
    private val compositeDisposable = CompositeDisposable()

    override fun onStart() {
        super.onStart()
        textView?.visibility = View.GONE
        recyclerView?.adapter = listAdapter
        compositeDisposable.add(
            RestService.fetchMeetingRoomList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(listAdapter::update) { Log.d(TAG, it.toString()) }
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

    companion object {

        private val TAG = MeetingRoomsListFragment::class.java.canonicalName

    }


}
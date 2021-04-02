package com.scorealarm.meeting.rooms.fragments

import androidx.fragment.app.Fragment
import com.scorealarm.meeting.rooms.R
import kotlinx.android.synthetic.main.fragment_empty_list.*

class EmptyMeetingsFragment(val text: String)
    : Fragment(R.layout.fragment_empty_list){

    override fun onStart() {
        super.onStart()
        textView?.text = text
    }

        companion object {

            private val TAG = EmptyMeetingsFragment::class.java.canonicalName

            fun getInstance(text: String) = EmptyMeetingsFragment(text)
        }
}
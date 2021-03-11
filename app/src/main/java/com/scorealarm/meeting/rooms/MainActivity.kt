package com.scorealarm.meeting.rooms

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.startup.AppInitializer
import com.scorealarm.meeting.rooms.fragments.MeetingRoomListFragment
import net.danlew.android.joda.JodaTimeInitializer


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppInitializer.getInstance(this).initializeComponent(JodaTimeInitializer::class.java)
        supportFragmentManager.commit {
            replace<MeetingRoomListFragment>(R.id.containerView)
            setReorderingAllowed(true)
            addToBackStack(MeetingRoomListFragment.TAG)
        }
    }

}
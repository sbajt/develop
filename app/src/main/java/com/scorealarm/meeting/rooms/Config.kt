package com.scorealarm.meeting.rooms

import java.util.concurrent.TimeUnit

object Config {

    const val BASE_URL = "https://meeting-rooms.superology.dev"
    const val HEADER_AUTH = "drSsLeYSzdWVgwqKFk6mFt66X3ZWETQW"

    const val MEETING_LIST_REFRESH_RATE_IN_SECONDS = 5 * 60L
    const val CLOCK_PERIOD = 1L
    val CLOCK_TIME_UNIT = TimeUnit.MINUTES

}
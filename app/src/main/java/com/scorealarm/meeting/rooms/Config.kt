package com.scorealarm.meeting.rooms

import java.util.concurrent.TimeUnit

object Config {

    val BASE_URL = "https://meeting-rooms.superology.dev"
    val HEADER_AUTH = "drSsLeYSzdWVgwqKFk6mFt66X3ZWETQW"
    val DATA_REFRESH_RATE_IN_SECONDS = 5 * 60L
    val CLOCK_TIME_UNIT = TimeUnit.MINUTES
    val CLOCK_PERIOD = 1L

}
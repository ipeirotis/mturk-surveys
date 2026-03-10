package com.ipeirotis.util;

import java.util.Calendar;

public class CalendarUtils {

    private CalendarUtils() {}

    /**
     * Truncate a Calendar to the start of the day (midnight),
     * zeroing out hours, minutes, seconds, and milliseconds.
     */
    public static void truncateToDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }
}

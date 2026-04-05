package com.ipeirotis.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;

/**
 * Shared date validation for task endpoints that accept from/to date ranges.
 */
public class DateValidation {

    // Earliest reasonable date (before the survey started in 2015)
    private static final int EARLIEST_YEAR = 2000;
    // How far into the future 'to' dates are allowed
    private static final int MAX_FUTURE_YEARS = 2;

    /**
     * Validate a date string can be parsed with the given pattern.
     *
     * @throws IllegalArgumentException if the date is null, blank, or unparseable
     */
    public static void requireValidDate(String date, String paramName, String pattern) {
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException(paramName + " is required");
        }
        DateFormat df = SafeDateFormat.forPattern(pattern);
        df.setLenient(false);
        ParsePosition pos = new ParsePosition(0);
        df.parse(date, pos);
        if (pos.getIndex() != date.length() || pos.getErrorIndex() >= 0) {
            throw new IllegalArgumentException(
                    paramName + " must be in " + pattern + " format, got: " + date);
        }
    }

    /**
     * Validate a from/to date range: both parseable, from <= to, dates within
     * reasonable bounds (not before 2000, not more than 2 years in the future).
     *
     * @throws IllegalArgumentException on any validation failure
     */
    public static void requireValidRange(String from, String to, String pattern) {
        requireValidDate(from, "from", pattern);
        requireValidDate(to, "to", pattern);

        try {
            DateFormat df = SafeDateFormat.forPattern(pattern);
            df.setLenient(false);
            Calendar start = Calendar.getInstance();
            start.setTime(df.parse(from));
            Calendar end = Calendar.getInstance();
            end.setTime(df.parse(to));

            if (start.after(end)) {
                throw new IllegalArgumentException(
                        "from (" + from + ") must not be after to (" + to + ")");
            }

            if (start.get(Calendar.YEAR) < EARLIEST_YEAR) {
                throw new IllegalArgumentException(
                        "from date is before " + EARLIEST_YEAR + ": " + from);
            }

            Calendar maxFuture = Calendar.getInstance();
            maxFuture.add(Calendar.YEAR, MAX_FUTURE_YEARS);
            if (end.after(maxFuture)) {
                throw new IllegalArgumentException(
                        "to date is too far in the future: " + to);
            }
        } catch (ParseException e) {
            // Already validated above, should not happen
            throw new IllegalArgumentException("Invalid date format", e);
        }
    }
}

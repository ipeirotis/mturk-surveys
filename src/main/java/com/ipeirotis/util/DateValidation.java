package com.ipeirotis.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

/**
 * Shared date validation for task endpoints that accept from/to date ranges.
 */
public class DateValidation {

    private static final int MAX_RANGE_DAYS = 4100; // ~11 years, covers full dataset 2015-present

    /**
     * Validate a date string can be parsed with the given pattern.
     *
     * @throws IllegalArgumentException if the date is null, blank, or unparseable
     */
    public static void requireValidDate(String date, String paramName, String pattern) {
        if (date == null || date.isBlank()) {
            throw new IllegalArgumentException(paramName + " is required");
        }
        try {
            SafeDateFormat.forPattern(pattern).parse(date);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    paramName + " must be in " + pattern + " format, got: " + date);
        }
    }

    /**
     * Validate a from/to date range: both parseable, from <= to, range not too large.
     *
     * @throws IllegalArgumentException on any validation failure
     */
    public static void requireValidRange(String from, String to, String pattern) {
        requireValidDate(from, "from", pattern);
        requireValidDate(to, "to", pattern);

        try {
            DateFormat df = SafeDateFormat.forPattern(pattern);
            Calendar start = Calendar.getInstance();
            start.setTime(df.parse(from));
            Calendar end = Calendar.getInstance();
            end.setTime(df.parse(to));

            if (start.after(end)) {
                throw new IllegalArgumentException(
                        "from (" + from + ") must not be after to (" + to + ")");
            }

            long days = TimeUnit.MILLISECONDS.toDays(
                    end.getTimeInMillis() - start.getTimeInMillis()) + 1;
            if (days > MAX_RANGE_DAYS) {
                throw new IllegalArgumentException(
                        "Date range too large: " + days + " days (max " + MAX_RANGE_DAYS + ")");
            }
        } catch (ParseException e) {
            // Already validated above, should not happen
            throw new IllegalArgumentException("Invalid date format", e);
        }
    }
}

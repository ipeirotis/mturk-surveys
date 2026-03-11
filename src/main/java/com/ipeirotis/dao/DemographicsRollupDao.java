package com.ipeirotis.dao;

import com.googlecode.objectify.Key;
import com.ipeirotis.entity.DemographicsRollup;
import com.ipeirotis.ofy.OfyBaseDao;
import com.ipeirotis.util.SafeDateFormat;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Service
public class DemographicsRollupDao extends OfyBaseDao<DemographicsRollup> {

    protected DemographicsRollupDao() {
        super(DemographicsRollup.class);
    }

    /**
     * Query rollups by granularity and date range.
     * Dates are expected in MM/dd/yyyy format and converted to yyyy-MM-dd for querying.
     *
     * Uses key-based range queries since rollup IDs follow the format "granularity:yyyy-MM-dd"
     * (e.g., "weekly:2015-04-06"). This avoids requiring a composite index on (granularity, date).
     *
     * The from/to dates are aligned to period boundaries so that the first and last
     * periods are not accidentally excluded (e.g. from=03/26/2015 includes the
     * monthly:2015-03-01 rollup, not just monthly:2015-03-26+).
     */
    public List<DemographicsRollup> listByGranularityAndDateRange(String granularity, String from, String to) {
        try {
            DateFormat displayDf = SafeDateFormat.forPattern("MM/dd/yyyy");
            DateFormat sortableDf = SafeDateFormat.forPattern("yyyy-MM-dd");
            LocalDate fromDate = LocalDate.parse(sortableDf.format(displayDf.parse(from)));
            LocalDate toDate = LocalDate.parse(sortableDf.format(displayDf.parse(to)));

            // Align dates to period boundaries
            String alignedFrom = alignToPeriodStart(fromDate, granularity);
            String alignedTo = alignToPeriodEnd(toDate, granularity);

            Key<DemographicsRollup> fromKey = Key.create(DemographicsRollup.class, granularity + ":" + alignedFrom);
            Key<DemographicsRollup> toKey = Key.create(DemographicsRollup.class, granularity + ":" + alignedTo);

            return ofy().load().type(DemographicsRollup.class)
                    .filterKey(">=", fromKey)
                    .filterKey("<", toKey)
                    .list();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + from + " or " + to, e);
        }
    }

    /**
     * Align a date to the start of its period (1st of month for monthly, Monday for weekly).
     */
    private String alignToPeriodStart(LocalDate date, String granularity) {
        if ("monthly".equals(granularity)) {
            return date.withDayOfMonth(1).toString();
        } else if ("weekly".equals(granularity)) {
            return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString();
        }
        return date.toString();
    }

    /**
     * Align a date to the start of the NEXT period (exclusive upper bound).
     */
    private String alignToPeriodEnd(LocalDate date, String granularity) {
        if ("monthly".equals(granularity)) {
            return date.plusMonths(1).withDayOfMonth(1).toString();
        } else if ("weekly".equals(granularity)) {
            return date.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).toString();
        }
        return date.toString();
    }
}

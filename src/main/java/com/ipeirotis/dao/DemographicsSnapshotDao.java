package com.ipeirotis.dao;

import com.ipeirotis.entity.DemographicsSnapshot;
import com.ipeirotis.ofy.OfyBaseDao;
import com.ipeirotis.util.SafeDateFormat;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Service
public class DemographicsSnapshotDao extends OfyBaseDao<DemographicsSnapshot> {

    protected DemographicsSnapshotDao() {
        super(DemographicsSnapshot.class);
    }

    /**
     * Query snapshots by date range. Converts from/to from MM/dd/yyyy to yyyy-MM-dd
     * for correct lexicographic comparison in Datastore.
     * Uses chunked fetching (year-by-year) for large ranges to avoid Datastore timeouts.
     */
    public List<DemographicsSnapshot> listByDateRange(String from, String to) {
        try {
            DateFormat displayDf = SafeDateFormat.forPattern("MM/dd/yyyy");
            DateFormat sortableDf = SafeDateFormat.forPattern("yyyy-MM-dd");
            String sortableFrom = sortableDf.format(displayDf.parse(from));
            String sortableTo = sortableDf.format(displayDf.parse(to));

            java.time.LocalDate startDate = java.time.LocalDate.parse(sortableFrom);
            java.time.LocalDate endDate = java.time.LocalDate.parse(sortableTo);
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);

            // For ranges <= 2 years, fetch in one query
            if (totalDays <= 730) {
                return ofy().load().type(DemographicsSnapshot.class)
                        .filter("date >=", sortableFrom)
                        .filter("date <", sortableTo)
                        .list();
            }

            // For large ranges, fetch year-by-year to avoid Datastore RPC timeouts
            List<DemographicsSnapshot> all = new java.util.ArrayList<>();
            java.time.LocalDate chunkStart = startDate;
            while (chunkStart.isBefore(endDate)) {
                java.time.LocalDate chunkEnd = chunkStart.plusYears(1);
                if (chunkEnd.isAfter(endDate)) {
                    chunkEnd = endDate;
                }
                List<DemographicsSnapshot> chunk = ofy().load().type(DemographicsSnapshot.class)
                        .filter("date >=", chunkStart.toString())
                        .filter("date <", chunkEnd.toString())
                        .list();
                all.addAll(chunk);
                chunkStart = chunkEnd;
            }
            return all;
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + from + " or " + to, e);
        }
    }
}

package com.ipeirotis.dao;

import com.googlecode.objectify.Key;
import com.ipeirotis.entity.DemographicsRollup;
import com.ipeirotis.ofy.OfyBaseDao;
import com.ipeirotis.util.SafeDateFormat;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.ParseException;
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
     */
    public List<DemographicsRollup> listByGranularityAndDateRange(String granularity, String from, String to) {
        try {
            DateFormat displayDf = SafeDateFormat.forPattern("MM/dd/yyyy");
            DateFormat sortableDf = SafeDateFormat.forPattern("yyyy-MM-dd");
            String sortableFrom = sortableDf.format(displayDf.parse(from));
            String sortableTo = sortableDf.format(displayDf.parse(to));

            Key<DemographicsRollup> fromKey = Key.create(DemographicsRollup.class, granularity + ":" + sortableFrom);
            Key<DemographicsRollup> toKey = Key.create(DemographicsRollup.class, granularity + ":" + sortableTo);

            return ofy().load().type(DemographicsRollup.class)
                    .filterKey(">=", fromKey)
                    .filterKey("<", toKey)
                    .list();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + from + " or " + to, e);
        }
    }
}

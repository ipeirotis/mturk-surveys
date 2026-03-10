package com.ipeirotis.dao;

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
     */
    public List<DemographicsRollup> listByGranularityAndDateRange(String granularity, String from, String to) {
        try {
            DateFormat displayDf = SafeDateFormat.forPattern("MM/dd/yyyy");
            DateFormat sortableDf = SafeDateFormat.forPattern("yyyy-MM-dd");
            String sortableFrom = sortableDf.format(displayDf.parse(from));
            String sortableTo = sortableDf.format(displayDf.parse(to));
            return ofy().load().type(DemographicsRollup.class)
                    .filter("granularity", granularity)
                    .filter("date >=", sortableFrom)
                    .filter("date <", sortableTo)
                    .list();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + from + " or " + to, e);
        }
    }
}

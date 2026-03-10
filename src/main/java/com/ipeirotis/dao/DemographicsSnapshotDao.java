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
     */
    public List<DemographicsSnapshot> listByDateRange(String from, String to) {
        try {
            DateFormat displayDf = SafeDateFormat.forPattern("MM/dd/yyyy");
            DateFormat sortableDf = SafeDateFormat.forPattern("yyyy-MM-dd");
            String sortableFrom = sortableDf.format(displayDf.parse(from));
            String sortableTo = sortableDf.format(displayDf.parse(to));
            return ofy().load().type(DemographicsSnapshot.class)
                    .filter("date >=", sortableFrom)
                    .filter("date <", sortableTo)
                    .list();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid date format: " + from + " or " + to, e);
        }
    }
}

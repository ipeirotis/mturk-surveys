package com.ipeirotis.dao;

import com.ipeirotis.entity.DemographicsSnapshot;
import com.ipeirotis.ofy.OfyBaseDao;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.googlecode.objectify.ObjectifyService.ofy;

@Service
public class DemographicsSnapshotDao extends OfyBaseDao<DemographicsSnapshot> {

    protected DemographicsSnapshotDao() {
        super(DemographicsSnapshot.class);
    }

    public List<DemographicsSnapshot> listByDateRange(String from, String to) {
        return ofy().load().type(DemographicsSnapshot.class)
                .filter("date >=", from)
                .filter("date <", to)
                .list();
    }
}

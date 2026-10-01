package com.ipeirotis.entity;

import java.util.Date;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Per-date lease that serializes BigQuery exports. The export checks which
 * (worker, HIT) pairs are already in demographics.responses and then inserts the
 * rest; without the lease, two overlapping exports for the same date could both
 * insert the same rows. Not cached: the lease is read inside a transaction.
 */
@Entity
public class BigQueryExportLock {

    @Id
    private String date;
    private String owner;
    private Date expiresAt;

    public BigQueryExportLock() {
    }

    public BigQueryExportLock(String date, String owner, Date expiresAt) {
        this.date = date;
        this.owner = owner;
        this.expiresAt = expiresAt;
    }

    public String getDate() {
        return date;
    }

    public String getOwner() {
        return owner;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public boolean isHeldAt(Date now) {
        return expiresAt != null && expiresAt.after(now);
    }
}

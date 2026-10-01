package com.ipeirotis.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class BigQueryExportLockTest {

    @Test
    void isHeldAt_beforeExpiry_isHeld() {
        BigQueryExportLock lock = new BigQueryExportLock("2016-11-15", "a", new Date(2_000));
        assertTrue(lock.isHeldAt(new Date(1_000)));
    }

    @Test
    void isHeldAt_atOrAfterExpiry_isFree() {
        BigQueryExportLock lock = new BigQueryExportLock("2016-11-15", "a", new Date(2_000));
        assertFalse(lock.isHeldAt(new Date(2_000)));
        assertFalse(lock.isHeldAt(new Date(3_000)));
    }

    @Test
    void isHeldAt_noExpiry_isFree() {
        assertFalse(new BigQueryExportLock().isHeldAt(new Date()));
    }
}

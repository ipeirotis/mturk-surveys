package com.ipeirotis.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class BigQueryExportServiceTest {

    @Test
    void sha256Hex_knownInput_returnsExpectedHash() throws Exception {
        // SHA-256 of "hello" is well-known
        String result = invokeSha256Hex("hello");
        assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824", result);
    }

    @Test
    void sha256Hex_emptyString_returnsHash() throws Exception {
        // SHA-256 of "" is well-known
        String result = invokeSha256Hex("");
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", result);
    }

    @Test
    void sha256Hex_sameInput_returnsSameHash() throws Exception {
        String hash1 = invokeSha256Hex("worker123");
        String hash2 = invokeSha256Hex("worker123");
        assertEquals(hash1, hash2);
    }

    @Test
    void sha256Hex_differentInputs_returnsDifferentHashes() throws Exception {
        String hash1 = invokeSha256Hex("worker_a");
        String hash2 = invokeSha256Hex("worker_b");
        assertNotEquals(hash1, hash2);
    }

    @Test
    void sha256Hex_returnsLowercaseHex() throws Exception {
        String result = invokeSha256Hex("test");
        assertTrue(result.matches("[0-9a-f]{64}"));
    }

    @Test
    void sha256Hex_returns64CharacterString() throws Exception {
        String result = invokeSha256Hex("any input string");
        assertEquals(64, result.length());
    }

    @Test
    void sha256Hex_unicodeInput_works() throws Exception {
        String result = invokeSha256Hex("hello world");
        assertNotNull(result);
        assertEquals(64, result.length());
    }

    @Test
    void pairKey_hashesWorkerIdLikeTheTable() throws Exception {
        com.ipeirotis.entity.UserAnswer ua = new com.ipeirotis.entity.UserAnswer();
        ua.setWorkerId("hello");
        ua.setHitId("HIT1");
        assertEquals(invokeSha256Hex("hello") + "|HIT1", BigQueryExportService.pairKey(ua));
    }

    @Test
    void pairKey_nullFields_useEmptyStrings() {
        assertEquals("|", BigQueryExportService.pairKey(new com.ipeirotis.entity.UserAnswer()));
    }

    @Test
    void lockDates_coversTheLookupWindow() {
        assertEquals(java.util.List.of("2016-02-28", "2016-02-29", "2016-03-01"),
                BigQueryExportService.lockDates("2016-02-29"));
    }

    @Test
    void lockDates_adjacentDatesShareALease() {
        java.util.List<String> a = BigQueryExportService.lockDates("2016-11-15");
        java.util.List<String> b = BigQueryExportService.lockDates("2016-11-16");
        assertTrue(a.stream().anyMatch(b::contains));
    }

    private String invokeSha256Hex(String input) throws Exception {
        Method method = BigQueryExportService.class.getDeclaredMethod("sha256Hex", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, input);
    }
}

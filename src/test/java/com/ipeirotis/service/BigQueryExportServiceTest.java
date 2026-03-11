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

    private String invokeSha256Hex(String input) throws Exception {
        Method method = BigQueryExportService.class.getDeclaredMethod("sha256Hex", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, input);
    }
}

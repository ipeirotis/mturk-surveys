package com.ipeirotis.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MD5Test {

    @Test
    void crypt_knownInput_returnsExpectedHash() {
        // MD5 of "hello" is well-known
        assertEquals("5d41402abc4b2a76b9719d911017c592", MD5.crypt("hello"));
    }

    @Test
    void crypt_emptyString_returnsHash() {
        // MD5 of "" is well-known
        assertEquals("d41d8cd98f00b204e9800998ecf8427e", MD5.crypt(""));
    }

    @Test
    void crypt_sameInput_returnsConsistentHash() {
        String hash1 = MD5.crypt("worker123");
        String hash2 = MD5.crypt("worker123");
        assertEquals(hash1, hash2);
    }

    @Test
    void crypt_differentInputs_returnDifferentHashes() {
        assertNotEquals(MD5.crypt("alice"), MD5.crypt("bob"));
    }

    @Test
    void crypt_returns32CharacterHex() {
        String result = MD5.crypt("test");
        assertEquals(32, result.length());
        assertTrue(result.matches("[0-9a-f]{32}"));
    }
}

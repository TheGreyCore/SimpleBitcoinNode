package org.students.simplebitcoinnode.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.students.simplebitcoinnode.exceptions.encoding.SerializationException;
import org.students.simplebitcoinnode.util.Encoding;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public abstract class AsymmetricCryptographyServiceSHA256Tests extends AsymmetricCryptographyServiceTests {
    @Test
    @DisplayName("Ensure that SHA-256 digest produces correct result for non-Transaction types")
    public void testDigest_EnsureMatchingSHA256Digest_SerializableString() {
        final String[] msgs = {"Hello world!", "hello world!", "Yoooooo!", "Hash me pls"};
        final String[] hashes = {
            "c0535e4be2b79ffd93291305436bf889314e4a3faec05ecffcbb7df31ad9e51a",
            "7509e5bda0c762d2bac7f90d758b5b2263fa01ccbc542ab5e3df163be08e6ca9",
            "d347889a452c32b2a3c347784562ab9841b780a648e2f2f1d90f232d4ceda59b",
            "119b8980fa09350d10828b07ab92e48adc9f34ceb350d01e6dc75b64d0c1a48e"
        };

        try {
            for (int i = 0; i < msgs.length; i++) {
                assertEquals(hashes[i], Encoding.toHexString(asymmetricCryptographyService.digestObject(msgs[i])));
            }
        } catch (SerializationException e) {
            fail();
        }
    }
}

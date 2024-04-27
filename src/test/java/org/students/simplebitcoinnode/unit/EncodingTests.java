package org.students.simplebitcoinnode.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.util.Encoding;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class EncodingTests {
    /* Hexadecimal encoding and decoding */
    @Test
    @DisplayName("Ensure that hexadecimal encoding works as intended")
    void testHexadecimalEncoding() {
        final String[] msgs = {
            "Hello test!",
            "Terekest seal üõölä",
            "日本語を話します"
        };

        final String[] expectedHexStrings = {
            "48656c6c6f207465737421",
            "546572656b657374207365616c20c3bcc3b5c3b66cc3a4",
            "e697a5e69cace8aa9ee38292e8a9b1e38197e381bee38199"
        };

        for (int i = 0; i < msgs.length; i++)
            assertEquals(expectedHexStrings[i], Encoding.toHexString(msgs[i].getBytes()));
    }

    @Test
    @DisplayName("Ensure that hexadecimal decoding works as intended")
    void testHexadecimalDecoding_ExpectNoException() {
        final String[] expectedMsgs = {
            "Hello test!",
            "Terekest seal üõölä",
            "日本語を話します"
        };

        final String[] hexStrings = {
            "48656c6c6f207465737421",
            "546572656b657374207365616c20c3bcc3b5c3b66cc3a4",
            "e697a5e69cace8aa9ee38292e8a9b1e38197e381bee38199"
        };

        for (int i = 0; i < hexStrings.length; i++) {
            try {
                assertEquals(expectedMsgs[i], new String(Encoding.hexStringToBytes(hexStrings[i])));
            }
            catch (Exception e) {
                fail();
            }
        }
    }

    @Test
    @DisplayName("Ensure that InvalidEncodedStringException is thrown when decoding hex string with invalid length or characters")
    void testHexadecimalDecoding_ExpectInvalidEncodedStringException() {
        assertThrows(InvalidEncodedStringException.class, () -> Encoding.hexStringToBytes("48656c6c6f20746573742"));
        assertThrows(InvalidEncodedStringException.class, () -> Encoding.hexStringToBytes("notahexstring"));
    }

    @Test
    @DisplayName("Ensure that hexadecimal encoding and decoding don't lose data with leading null bytes")
    void testHexadecimalEncodingAndDecodingLeadingNullBytes() throws InvalidEncodedStringException {
        byte[] src = {0x00, 0x00, 0x00, 0x00};
        String hex = Encoding.toHexString(src);
        assertEquals(0, Arrays.compare(src, Encoding.hexStringToBytes(hex)));
    }

    /* Base58 encoding and decoding */
    @Test
    @DisplayName("Ensure that base58 encoding works as intended")
    void testBase58Encoding() {
        final String[] msgs = {
            "Hello test!",
            "Terekest seal üõölä",
            "日本語を話します"
        };

        final String[] expectedBase58 = {
            "JxF12TrwXeZfikG",
            "2k6xTNJHGaijeBPGKDQpuyATch9ArJCB",
            "N2G5p7NXne8qqavcQaQmYKyFrgg67dBuA"
        };

        for (int i = 0; i < msgs.length; i++)
            assertEquals(expectedBase58[i], Encoding.base58Encode(msgs[i].getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("Ensure that base58 decoding works as intended")
    void testBase58Decoding_ExpectNoExceptions() {
        final String[] expectedMsgs = {
            "Hello test!",
            "Terekest seal üõölä",
            "日本語を話します"
        };

        final String[] base58 = {
            "JxF12TrwXeZfikG",
            "2k6xTNJHGaijeBPGKDQpuyATch9ArJCB",
            "N2G5p7NXne8qqavcQaQmYKyFrgg67dBuA"
        };

        for (int i = 0; i < expectedMsgs.length; i++) {
            try {
                assertEquals(expectedMsgs[i], new String(Encoding.base58Decode(base58[i]), StandardCharsets.UTF_8));
            }
            catch (Exception e) {
                fail();
            }
        }
    }

    @Test
    @DisplayName("Ensure that base58 encoding and decoding doesn't lose data with leading null bytes")
    void testBase58EncodingAndDecodingLeadingNullBytes() throws InvalidEncodedStringException {
        final byte[] src = {0x00, 0x00, 0x00, 0x00};
        String encoded = Encoding.base58Encode(src);
        assertEquals(0, Arrays.compare(src, Encoding.base58Decode(encoded)));
    }
}

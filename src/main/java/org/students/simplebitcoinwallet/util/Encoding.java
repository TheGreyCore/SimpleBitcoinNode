package org.students.simplebitcoinwallet.util;

import org.students.simplebitcoinwallet.exceptions.encoding.InvalidEncodedStringException;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Encoding utility class
 */
public class Encoding {
    private static final String base58Charset = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    /**
     * Encodes the public key with current default public key encoding
     * @param pubKey represents the public key as byte array to encode
     * @return string value containing the encoded public key
     */
    public static String defaultPubKeyEncoding(byte[] pubKey) {
        return toHexString(pubKey);
    }

    /**
     * Decodes the provided public key with current default public key decoding algorithm
     * @param encodedPublicKey represents the encoded public key as string
     * @return byte array representing the public key
     * @throws InvalidEncodedStringException
     */
    public static byte[] defaultPubKeyDecoding(String encodedPublicKey) throws InvalidEncodedStringException {
        return hexStringToBytes(encodedPublicKey);
    }

    /**
     * Converts the byte array into hexadecimal string
     * @param bytes represents the array of bytes to use for encoding
     * @return a hexadecimal string representing the byte array
     */
    public static String toHexString(byte[] bytes) {
        final char[] hexSymbols = "0123456789abcdef".toCharArray();

        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            int val = b & 0xff;
            stringBuilder.append(hexSymbols[val >> 4]);
            stringBuilder.append(hexSymbols[val & 0x0f]);
        }

        return stringBuilder.toString();
    }

    /**
     * Converts given hexadecimal string to an array of bytes if possible.
     * @param hexString specifies the hexadecimal string to decode. The string is assumed to only contain lowercase numerical or lowercase `abcdef` characters.
     *                  Other-wise the method call will fail with InvalidHexStringException.
     * @return array of decoded bytes
     * @throws InvalidEncodedStringException
     */
    public static byte[] hexStringToBytes(String hexString) throws InvalidEncodedStringException {
        validateHexStringOrException(hexString);
        byte[] bytes = new byte[hexString.length() / 2];

        for (int i = 0; i < hexString.length(); i += 2) {
            int b = hexSymbolToInt(hexString.charAt(i)) * 16 + hexSymbolToInt(hexString.charAt(i+1));
            bytes[i / 2] = (byte)(b & 0xff);
        }

        return bytes;
    }

    /**
     * Encodes a byte array as base58 string
     * @param bytes represents the byte array to encode
     * @return string containing base58 representation of the data
     */
    public static String base58Encode(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();

        byte[] copy = Arrays.copyOf(bytes, bytes.length);
        for (int i = 0; i < copy.length;) {
            int carry = 0;
            for (int j = 0; j < copy.length; j++) {
                int digit = (copy[j] & 0xff) + (carry << 8);
                copy[j] = (byte) (digit / 58);
                carry = digit % 58;
            }

            if (copy[i] == 0)
                i++;
            stringBuilder.append(base58Charset.charAt(carry));
        }

        return stringBuilder.reverse().toString();
    }

    /**
     * Decodes base58 encoded string into a byte array
     * @param string specifies base58 encoded string to decode
     * @return byte array containing decoded data
     * @throws InvalidEncodedStringException
     */
    public static byte[] base58Decode(String string) throws InvalidEncodedStringException {
        List<Byte> byteList = new ArrayList<>();

        // represent base58 integer as byte array and count leading zeroes
        byte[] base58AsBytes = new byte[string.length()];
        for (int i = 0; i < string.length(); i++) {
            base58AsBytes[i] = (byte) binarySearchBase58AlphabetIndex(string.charAt(i));
        }

        // ugly baseness conversion algorithm all over again
        for (int i = 0; i < base58AsBytes.length; i++) {
            int carry = base58AsBytes[i];
            for (int j = 0; j < byteList.size(); j++) {
                // evil bit manipulations (⊙_⊙)
                carry += (byteList.get(j) & 0xff) * 58;
                byteList.set(j, (byte)(carry & 0xff));
                carry >>= 8;
            }

            // decompose carry bits into bytes
            while (carry > 0) {
                byteList.add((byte)(carry & 0xff));
                carry >>= 8;
            }
        }

        // deal with leading zeroes
        for (int i = 0; i < base58AsBytes.length && base58AsBytes[i] == (byte)0; i++) {
            byteList.add((byte)0);
        }

        // convert list to byte array
        byte[] res = new byte[byteList.size()];
        for (int i = 0; i < byteList.size(); i++)
            res[i] = byteList.get(byteList.size() - 1 - i);
        return res;
    }

    public static void main(String[] args) throws InvalidEncodedStringException {
        String msg = "Hello world!";
        String encoded = base58Encode(msg.getBytes(StandardCharsets.UTF_8));
        System.out.println(encoded);
        System.out.println(new String(base58Decode(encoded)));
    }

    private static int binarySearchBase58AlphabetIndex(char c) throws InvalidEncodedStringException {
        // find character's index using binary search
        int left = 0; // inclusive
        int right = base58Charset.length(); // exclusive
        int mid = (left + right) / 2;

        while (base58Charset.charAt(mid) != c) {
            if (right - left <= 1)
                throw new InvalidEncodedStringException("Base58 decoding failed: character '" + c + "' is not in Base58 alphabet");

            if (base58Charset.charAt(mid) > c)
                right = mid;
            else if (base58Charset.charAt(mid) < c)
                left = mid;
            mid = (left + right) / 2;
        }

        return mid;
    }

    private static void validateHexStringOrException(String hexString) throws InvalidEncodedStringException {
        // length must be multiple of 2 requirement
        if (hexString.length() % 2 != 0)
            throw new InvalidEncodedStringException("HexString length must be multiple of 2");

        // allowed symbol requirements
        for (char c : hexString.toCharArray()) {
            if (c < '0' || (c > '9' && c < 'a') || c > 'z')
                throw new InvalidEncodedStringException("Invalid symbol '" + c + "' in hexString ");
        }
    }

    private static int hexSymbolToInt(char hexSymbol) {
        return switch (hexSymbol) {
            case '1' -> 1;
            case '2' -> 2;
            case '3' -> 3;
            case '4' -> 4;
            case '5' -> 5;
            case '6' -> 6;
            case '7' -> 7;
            case '8' -> 8;
            case '9' -> 9;
            case 'a' -> 10;
            case 'b' -> 11;
            case 'c' -> 12;
            case 'd' -> 13;
            case 'e' -> 14;
            case 'f' -> 15;
            default -> 0;
        };
    }
}

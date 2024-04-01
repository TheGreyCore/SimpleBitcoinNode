package org.students.simplebitcoinwallet.exceptions.encoding;

/**
 * Base exception class to use exceptions that indicate issues with data encoding
 */
public class EncodingException extends Exception {
    public EncodingException(String msg) {
        super(msg);
    }
}

package org.students.simplebitcoinnode.exceptions.encoding;

/**
 * Base exception class to use exceptions that indicate issues with data encoding
 */
public class InvalidEncodedStringException extends Exception {
    public InvalidEncodedStringException(String msg) {
        super(msg);
    }
}

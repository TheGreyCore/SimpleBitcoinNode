package org.students.simplebitcoinwallet.exceptions.encoding;

/**
 * Exception type, which indicates errors with provided hexadecimal string
 */
public class InvalidHexStringException extends EncodingException {
    public InvalidHexStringException(String msg) {
        super(msg);
    }
}

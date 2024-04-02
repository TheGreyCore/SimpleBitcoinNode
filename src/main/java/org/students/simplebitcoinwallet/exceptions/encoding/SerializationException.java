package org.students.simplebitcoinwallet.exceptions.encoding;

/**
 * Type of exception which is thrown, when object serialization fails
 */
public class SerializationException extends InvalidEncodedStringException {
    public SerializationException(String msg) {
        super(msg);
    }
}

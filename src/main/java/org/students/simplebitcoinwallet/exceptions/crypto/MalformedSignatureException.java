package org.students.simplebitcoinwallet.exceptions.crypto;

/**
 * Type of cryptographic exception that gets thrown if provided signature encoding is invalid
 */
public class MalformedSignatureException extends GeneralCryptographyException {
    public MalformedSignatureException(String msg) {
        super(msg);
    }
}

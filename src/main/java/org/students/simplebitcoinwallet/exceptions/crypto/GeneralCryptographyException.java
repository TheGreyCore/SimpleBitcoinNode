package org.students.simplebitcoinwallet.exceptions.crypto;

/**
 * Base class for cryptography exceptions.
 */
public class GeneralCryptographyException extends Exception {
    public GeneralCryptographyException(String msg) {
        super(msg);
    }
}

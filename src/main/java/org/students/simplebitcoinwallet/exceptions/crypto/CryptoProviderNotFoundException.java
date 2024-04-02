package org.students.simplebitcoinwallet.exceptions.crypto;


/**
 * This exception should be thrown when the underlying cryptographic provider does not support given algorithm or cryptographic parameters.
 */
public class CryptoProviderNotFoundException extends RuntimeException {
    public CryptoProviderNotFoundException(String msg) {
        super(msg);
    }
}

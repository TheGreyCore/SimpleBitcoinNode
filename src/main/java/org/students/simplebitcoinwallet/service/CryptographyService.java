package org.students.simplebitcoinwallet.service;

import java.io.Serializable;

public interface CryptographyService {
    /**
     * Calculates SHA-256 hash from given serializable object
     * @param serializable specifies the object whose hash to calculate
     * @return array of bytes with a length of exactly 32 bytes, which represent the digested SHA-256 value. In case the hash couldn't be calculated an array of zeroes is returned.
     */
    byte[] sha256Digest(Serializable serializable);

    /**
     * Verifies if the digital signature matches signer's public key and the message that was signed.
     * @param messageObject specifies the Serializable object that composes the message
     * @param signature specifies the signature itself as a byte array
     * @param pubKey specifies the signer's public key
     * @return a boolean value with either true, if signature verification was successful, or false otherwise
     */
    boolean verifyDigitalSignature(Serializable messageObject, byte[] signature, byte[] pubKey);
}

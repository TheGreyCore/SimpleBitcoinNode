package org.students.simplebitcoinnode.exceptions.crypto;

/**
 * Type of exception which gets thrown if hash calculation fails.<br>
 * Usually it indicates a severe problem with the implementation
 */
public class HashAlgorithmException extends RuntimeException {
    public HashAlgorithmException(String msg) {
        super(msg);
    }
}

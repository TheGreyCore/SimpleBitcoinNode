package org.students.simplebitcoinwallet.unit.transaction;

import java.security.MessageDigest;

/**
 * Calculate SHA-256 hashes from Transaction objects
 */
public class SHA256TransactionHasher extends TransactionHasher {

    /**
     * Perform SHA-256 hashing on specified Transaction object contained in TransactionContainer
     * @param transaction specifies the Transaction object to use for hashing
     */
    @Override
    public void hashTransaction(TransactionContainer transaction) throws Exception {
        // using MessageDigest class
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        transaction.setHash(digest.digest(byteSerializeTransaction(transaction)));
    }
}

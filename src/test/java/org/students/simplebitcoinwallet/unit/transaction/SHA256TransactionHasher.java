package org.students.simplebitcoinwallet.unit.transaction;

import org.students.simplebitcoinwallet.entity.Transaction;

import java.security.MessageDigest;
import java.util.List;

/**
 * Calculate SHA-256 hashes from Transaction objects
 */
public class SHA256TransactionHasher extends TransactionHasher {

    /**
     * Perform SHA-256 hashing on specified Transaction object
     * @param transaction specifies the Transaction object to use for hashing
     * @return byte array containing the hash of the serialized Transaction object
     */
    @Override
    public void hashTransaction(TransactionContainer transaction) throws Exception {
        // using MessageDigest class
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        transaction.setHash(digest.digest(byteSerializeTransaction(transaction)));
    }

    /**
     * Calculates and outputs SHA-256 hashes from all Transaction objects provided by TestTransactionBuilder class
     * @param args
     */
    public static void main(String[] args) throws Exception {
        TransactionHasher hasher = new SHA256TransactionHasher();

        List<TransactionContainer> containerList = TestTransactionBuilder.buildAllTransactions();
        for (TransactionContainer container : containerList) {
            hasher.hashTransaction(container);
            System.out.println(container);
        }
    }
}

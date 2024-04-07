package org.students.simplebitcoinwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.students.simplebitcoinwallet.entity.Transaction;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    /**
     * Find all transactions where given wallet's public key has received some tokens from other wallets excluding return transfers
     * @param publicKey specifies the wallet's public key to use for querying
     * @return list of transactions that match given criteria
     */
    @Query(value = "SELECT l.* FROM LEDGER l JOIN TRANSACTION_OUTPUT t ON l.id=t.OUTPUT_ID WHERE t.RECEIVER_PUBLIC_KEY='?1' AND l.SENDER_PUBLIC_KEY != t.RECEIVER_PUBLIC_KEY", nativeQuery = true)
    List<Transaction> findReceivedTransactionsExcludeReturns(String publicKey);

    /**
     * Find all transactions where given wallet's public key has received some tokens including return transfers
     * @param publicKey specfies the wallet's public key to use for querying
     * @return list of transactions matching the criteria
     */
    @Query(value = "SELECT l.* FROM LEDGER l JOIN TRANSACTION_OUTPUT t ON l.id=t.OUTPUT_ID WHERE t.RECEIVER_PUBLIC_KEY='?1'", nativeQuery = true)
    List<Transaction> findAllReceivedTransactionsByPublicKeyAddress(String publicKey);

    /**
     * Find all transactions where given wallet's keys have been used to transfer tokens to some other wallets
     * @param publicKey specifies the wallet's public key to use for querying
     * @return list of transactions matching the criteria
     */
    @Query(value = "SELECT l.* FROM LEDGER l WHERE l.SENDER_PUBLIC_KEY='?1'", nativeQuery = true)
    List<Transaction> findSentTransactionsByPublicKeyAddress(String publicKey);

    /**
     * Find all transactions where given wallet's keys have been used, either as a receiver's address or sender's address
     * @param publicKey specifies the wallet's public key to use for querying
     * @return list of transactions matching the criteria
     */
    @Query(value = "SELECT DISTINCT l.* FROM LEDGER l JOIN TRANSACTION_OUTPUT t on l.ID=t.OUTPUT_ID WHERE t.RECEIVER_PUBLIC_KEY='?1' OR l.SENDER_PUBLIC_KEY='?1'", nativeQuery = true)
    List<Transaction> findAllTransactionsByPublicKeyAddress(String publicKey);
}

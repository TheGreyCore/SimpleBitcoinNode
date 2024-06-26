package org.students.simplebitcoinnode.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.students.simplebitcoinnode.entity.Transaction;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    /**
     * Find all transactions where given wallet's public key has received some tokens from other wallets excluding return transfers
     * @param publicKey specifies the wallet's public key to use for querying
     * @return list of transactions that match given criteria
     */
    @Query(value = "SELECT l.* FROM LEDGER l JOIN TRANSACTION_OUTPUT t ON l.id=t.OUTPUT_ID WHERE t.RECEIVER_PUBLIC_KEY=?1 AND l.SENDER_PUBLIC_KEY != t.RECEIVER_PUBLIC_KEY", nativeQuery = true)
    List<Transaction> findReceivedTransactionsExcludeReturns(String publicKey);

    /**
     * Find all transactions where given wallet's public key has received some tokens including return transfers
     * @param publicKey specifies the wallet's public key to use for querying
     * @return list of transactions matching the criteria
     */
    @Query(value = "SELECT l.* FROM LEDGER l JOIN TRANSACTION_OUTPUT t ON l.id=t.OUTPUT_ID WHERE t.RECEIVER_PUBLIC_KEY=?1", nativeQuery = true)
    List<Transaction> findAllReceivedTransactionsByPublicKeyAddress(String publicKey);

    /**
     * Find all transactions where given wallet's keys have been used to transfer tokens to some other wallets
     * @param publicKey specifies the wallet's public key to use for querying
     * @return list of transactions matching the criteria
     */
    @Query(value = "SELECT l.* FROM LEDGER l WHERE l.SENDER_PUBLIC_KEY=?1", nativeQuery = true)
    List<Transaction> findSentTransactionsByPublicKeyAddress(String publicKey);

    /**
     * Find all transactions where given wallet's keys have been used, either as a receiver's address or sender's address
     * @param publicKey specifies the wallet's public key to use for querying
     * @return list of transactions matching the criteria
     */
    @Query(value = "SELECT DISTINCT l.* FROM LEDGER l JOIN TRANSACTION_OUTPUT t on l.ID=t.OUTPUT_ID WHERE t.RECEIVER_PUBLIC_KEY=?1 OR l.SENDER_PUBLIC_KEY=?1", nativeQuery = true)
    List<Transaction> findAllTransactionsByPublicKeyAddress(String publicKey);

    /**
     * Queries the amount of unverified transactions, i.e. transactions which haven't been mined into a block
     * @return long value representing the total amount of unverified transactions
     */
    @Query(value = """
        SELECT COUNT(*) FROM LEDGER l
        WHERE l.ID NOT IN (
            SELECT i.TRANSACTION_ID FROM INTERMEDIATE_MERKLE_TREE_NODES i
            WHERE i.TRANSACTION_ID != NULL
        )
    """, nativeQuery = true)
    Long findAmountOfUnverifiedTransactions();

    /**
     * Queries N unverified transactions (i.e. not belonging to any merkle tree) ordered by timestamp
     * @param limit specifies the maximum amount of transactions to query for
     * @return list containing at max N unverified transactions
     */
    @Query(value = """
        SELECT * FROM LEDGER l
        WHERE l.ID NOT IN (
            SELECT TRANSACTION_ID FROM INTERMEDIATE_MERKLE_TREE_NODES
            WHERE TRANSACTION_ID IS NOT NULL
        )
        ORDER BY TIMESTAMP
        LIMIT ?1
    """, nativeQuery = true)
    List<Transaction> findUnverifiedTransactionsLimitByN(int limit);
}

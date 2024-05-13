package org.students.simplebitcoinnode.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.students.simplebitcoinnode.entity.TransactionOutput;

import java.util.List;
import java.util.Optional;

/**
 * Data repository for querying transaction outputs
 */
@Repository
public interface TransactionOutputRepository extends JpaRepository<TransactionOutput, Long> {
    /**
     * Finds the number of unspent transaction outputs with given signature.
     * @param signature specifies the signature to use for searching
     * @return an integer value of how many unspent transaction outputs were found
     */
    @Query(value = "SELECT COUNT(*) FROM TRANSACTION_OUTPUT t WHERE t.signature=?1 AND t.OUTPUT_ID=NULL", nativeQuery = true)
    int findUtxoCountBySignature(String signature);

    /**
     * Finds the transaction output by digital signature
     * @param signature specifies the hexadecimal encoded digital signature to use for searching
     * @return an instance of Optional<> containing TransactionOutput, if query returned an object, null otherwise.
     */
    Optional<TransactionOutput> findTransactionOutputBySignature(String signature);

    /**
     * Finds all unspent transaction outputs (UXTOs) that belong to given wallet
     * @param receiverPublicKey specifies the receiver's public key to use for querying
     * @return a list of all unspent transaction outputs belonging to given wallet
     */
    @Query(value = "SELECT * FROM TRANSACTION_OUTPUT t WHERE t.receiverPublicKey=?1 AND t.OUTPUT_ID=NULL", nativeQuery = true)
    List<TransactionOutput> findUTXOsByReceiverPublicKey(String receiverPublicKey);
}

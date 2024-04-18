package org.students.simplebitcoinnode.service;

import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.repository.TransactionRepository;

import java.util.List;
import java.util.logging.Logger;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final Logger logger = Logger.getLogger(TransactionService.class.getName());

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieves a list of transactions associated with a given public key and transaction type.
     *
     * @param pubKey The public key associated with the transactions. This is a unique identifier for a user.
     * @param type The type of transactions to retrieve. This can be "sent", "received", or "all".
     *
     * @return A list of transactions that match the given public key and transaction type.
     *         Returns null if the transaction type does not match any of the predefined types.
     *
     * @throws IllegalArgumentException if the provided public key is null or empty.
     * @throws IllegalArgumentException if the provided type is null or empty.
     */
    public List<Transaction> getTransactions(String pubKey, String type) {
        return switch (type) {
            case "sent" -> transactionRepository.findSentTransactionsByPublicKeyAddress(pubKey);
            case "received" -> transactionRepository.findReceivedTransactionsExcludeReturns(pubKey);
            default -> transactionRepository.findAllTransactionsByPublicKeyAddress(pubKey);
        };
    }

    /**
     * This method is used to save new transactions.
     *
     * @param transaction This is the transaction to be created.
     * @return int This returns the HTTP status code of the transaction creation operation.
     *             It returns 201 if the transaction was successfully created, and 400 if an exception occurred.
     */
    
    public int newTransactions(Transaction transaction) {
        try {
            // TransactionRepository.save() call fails if the entity is annotated with custom constraints that have dependencies
            // Is there a way to avoid validation in save() calls because the validation would be done in the endpoint controller anyway?
            transactionRepository.save(transaction);
        } catch (Exception e){
            logger.warning(e.getMessage());
            return 400;
        }
        return 200;
    }
}

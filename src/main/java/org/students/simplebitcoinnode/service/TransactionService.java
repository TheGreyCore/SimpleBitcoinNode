package org.students.simplebitcoinnode.service;

import org.springframework.stereotype.Service;
import org.students.simplebitcoinnode.datatransferobjects.GetTransactionDTO;
import org.students.simplebitcoinnode.datatransferobjects.NewTransactionDTO;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.repository.TransactionRepository;
import org.students.simplebitcoinnode.util.DTOMapperWrapper;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final DTOMapperWrapper dtoMapperWrapper;

    public TransactionService(TransactionRepository transactionRepository, DTOMapperWrapper dtoMapperWrapper) {
        this.transactionRepository = transactionRepository;
        this.dtoMapperWrapper = dtoMapperWrapper;
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
    public List<GetTransactionDTO> getTransactions(String pubKey, String type) {
        List<Transaction> transactions;
        return switch (type) {
            case "sent":
                transactions = transactionRepository.findSentTransactionsByPublicKeyAddress(pubKey);
                yield dtoMapperWrapper.mapAll(transactions, GetTransactionDTO.class);
            case "received":
                transactions = transactionRepository.findAllReceivedTransactionsByPublicKeyAddress(pubKey);
                yield dtoMapperWrapper.mapAll(transactions, GetTransactionDTO.class);
            case "all":
                transactions = transactionRepository.findAllTransactionsByPublicKeyAddress(pubKey);
                yield dtoMapperWrapper.mapAll(transactions, GetTransactionDTO.class);
            default: yield null;
        };
    }


    /**
     * This method is used to save new transactions.
     *
     * @param newTransactionDTO This is the transaction to be created.
     * @return int This returns the HTTP status code of the transaction creation operation.
     *             It returns 201 if the transaction was successfully created, and 400 if an exception occurred.
     */
    
    public GetTransactionDTO newTransactions(NewTransactionDTO newTransactionDTO) {
        Transaction Transaction = transactionRepository.save(dtoMapperWrapper.unmap(newTransactionDTO, org.students.simplebitcoinnode.entity.Transaction.class));
        return dtoMapperWrapper.map(Transaction, GetTransactionDTO.class);
    }
}

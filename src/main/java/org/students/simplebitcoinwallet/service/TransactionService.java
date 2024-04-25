package org.students.simplebitcoinwallet.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.students.simplebitcoinwallet.dataTransferObjects.GetTransactionDTO;
import org.students.simplebitcoinwallet.dataTransferObjects.NewTransactionDTO;
import org.students.simplebitcoinwallet.dataTransferObjects.TransactionOutputDTO;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final DTOMapperService dtoMapperService;

    public TransactionService(TransactionRepository transactionRepository, DTOMapperService dtoMapperService) {
        this.transactionRepository = transactionRepository;
        this.dtoMapperService = dtoMapperService;
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
                yield dtoMapperService.mapAll(transactions, GetTransactionDTO.class);
            case "received":
                transactions = transactionRepository.findAllReceivedTransactionsByPublicKeyAddress(pubKey);
                yield dtoMapperService.mapAll(transactions, GetTransactionDTO.class);
            case "all":
                transactions = transactionRepository.findAllTransactionsByPublicKeyAddress(pubKey);
                yield dtoMapperService.mapAll(transactions, GetTransactionDTO.class);
            default: yield null;
        };
    }


    /**
     * This method is used to save new transactions.
     *
     * @param transaction This is the transaction to be created.
     * @return int This returns the HTTP status code of the transaction creation operation.
     *             It returns 201 if the transaction was successfully created, and 400 if an exception occurred.
     */
    
    public int newTransactions(NewTransactionDTO newTransactionDTO) {
        try {
            transactionRepository.save(dtoMapperService.unmap(newTransactionDTO, Transaction.class));
        } catch (Exception e){
            return 400;
        }
        return 201;
    }
}

package org.students.simplebitcoinwallet.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.repository.TransactionRepository;

import java.util.List;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public List<Transaction> getTransactions(String pubKey, String type) {
        return switch (type) {
            case "sent" -> transactionRepository.findSentTransactionsByPublicKey(pubKey);
            case "received" -> transactionRepository.findReceivedTransactionsByPublicKey(pubKey);
            case "all" -> transactionRepository.findAllTransactionsByPublicKey(pubKey);
            default -> null;
        };
    }
}

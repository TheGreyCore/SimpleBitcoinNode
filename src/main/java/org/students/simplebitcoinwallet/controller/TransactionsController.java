package org.students.simplebitcoinwallet.controller;

import org.springframework.web.bind.annotation.*;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.service.TransactionService;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/blockchain")
public class TransactionsController {
    private final TransactionService transactionService;

    public TransactionsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * A controller method mapped to the "/transactions" endpoint.
     * It retrieves a list of transactions associated with a given public key and transaction type.
     *
     * @param pubKey The public key associated with the transactions. This is a unique identifier for a user.
     * @param type The type of transactions to retrieve. This can be "sent", "received", or "all".
     *
     * @return A list of transactions that match the given public key and transaction type.
     *
     * @throws IllegalArgumentException if the provided public key is null or empty.
     * @throws IllegalArgumentException if the provided type is null or empty.
     */
    @GetMapping("/transactions")
    public List<Transaction> getTransactions(@RequestParam String pubKey, @RequestParam String type) {
        return transactionService.getTransactions(pubKey, type);
    }

    /**
     * This method is used to create new transactions.
     *
     * @param transaction This is the transaction to be created.
     * @return int This returns the result of the transaction creation.
     */
    @PostMapping("/send")
    public int newTransactions(@RequestParam Transaction transaction){
        return transactionService.newTransactions(transaction);
    }

}

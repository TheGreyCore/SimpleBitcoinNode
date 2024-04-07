package org.students.simplebitcoinwallet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.service.TransactionService;

import java.util.List;

@RestController
@RequestMapping("/blockchain")
public class TransactionsController {
    private final TransactionService transactionService;

    public TransactionsController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/transactions")
    public List<Transaction> getTransactions(@RequestParam String pubKey, @RequestParam String type) {
        return transactionService.getTransactions(pubKey, type);
    }
}

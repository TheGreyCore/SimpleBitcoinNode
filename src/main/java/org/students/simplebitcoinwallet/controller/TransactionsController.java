package org.students.simplebitcoinwallet.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.students.simplebitcoinwallet.service.TransactionService;

@RestController
public class TransactionsController {
    private TransactionService transactionService;
    @GetMapping("/blockchain/transactions")
    public String getTransactions(@RequestParam String pubkey, @RequestParam String type) {
        return transactionService.getTransactions(pubkey, type);
    }
}

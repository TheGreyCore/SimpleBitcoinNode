package org.students.simplebitcoinwallet.service;

import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    public String getTransactions(String pubkey, String type) {
        return "ok";
    }
}

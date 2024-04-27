package org.students.simplebitcoinnode.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.students.simplebitcoinnode.dto.TransactionDTO;
import org.students.simplebitcoinnode.representation.ValidationErrorResponse;
import org.students.simplebitcoinnode.service.TransactionService;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@RestController
@RequestMapping("/blockchain")
public class TransactionsController {
    private final TransactionService transactionService;
    Logger logger = Logger.getLogger(TransactionsController.class.getName());

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
    public ResponseEntity<?> getTransactions(@RequestParam String pubKey, @RequestParam String type) {
        return ResponseEntity.ok().body(transactionService.getTransactions(pubKey, type));
    }

    /**
     * This method is used to create new transactions.
     *
     * @param newTransactionDTO This is the transaction to be created.
     * @return returns the result of the transaction creation.
     */
    @PostMapping("/send")
    public ResponseEntity<?> newTransactions(@Valid @RequestBody TransactionDTO newTransactionDTO){
        return ResponseEntity.ok().body(transactionService.newTransactions(newTransactionDTO));
    }

    /**
     * Handles MethodArgumentNotValidException exceptions.
     *
     * @param e The MethodArgumentNotValidException that was thrown.
     * @return ResponseEntity containing the BadRequestErrorResponse with validation errors.
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        logger.warning("MethodArgumentNotValidException thrown at TransactionsController: " + e.getMessage());

        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String objectName = error.getObjectName();
            String errorMessage = error.getDefaultMessage();
            errors.put(objectName, errorMessage);
        });
        return ResponseEntity.badRequest().body(new ValidationErrorResponse(errors));
    }
}

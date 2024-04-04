package org.students.simplebitcoinwallet.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;
import org.students.simplebitcoinwallet.entity.validation.annotations.DoubleSpendingConstraint;
import org.students.simplebitcoinwallet.repository.TransactionOutputRepository;

import java.util.logging.Logger;

/**
 * Checks if following Transaction constraints are satisfied: <br>
 *  a) Transaction inputs exist and thus are valid<br>
 *  b) Transaction inputs haven't been spent before<br>
 *  c) There are maximum of 2 transaction outputs made by given transaction
 *  d) Recipient addresses in transaction outputs must be unique
 */
public class DoubleSpendingConstraintValidator implements ConstraintValidator<DoubleSpendingConstraint, Transaction> {
    @Autowired
    private TransactionOutputRepository transactionOutputRepository;
    private final Logger logger = Logger.getLogger(DoubleSpendingConstraintValidator.class.getName());

    @Override
    public void initialize(DoubleSpendingConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Transaction transaction, ConstraintValidatorContext context) {
        // for each input check if it is a valid
        for (TransactionOutput utxoCandidate : transaction.getInputs()) {
            if (transactionOutputRepository.findUtxoCountBySignature(utxoCandidate.getSignature()) != 1) {
                logger.info("Transaction output with signature '" + utxoCandidate.getSignature() + "' is not a valid UTXO, validation failed!");
                return false;
            }
        }

        // check if there are more than two transaction outputs
        if (transaction.getOutputs().size() > 2 || transaction.getOutputs().isEmpty())
            return false;

        // check that each transaction output is unique
        return transaction.getOutputs().size() <= 1 || !transaction.getOutputs().get(0).getReceiverPublicKey().equals(transaction.getOutputs().get(1).getReceiverPublicKey());
    }
}

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
 * Verifies if: <br>
 *  a) transaction inputs exist and thus are valid<br>
 *  b) transaction inputs haven't been spent before<br>
 */
public class DoubleSpendingConstraintValidator implements ConstraintValidator<DoubleSpendingConstraint, Transaction> {
    @Autowired
    private TransactionOutputRepository transactionOutputRepository;
    private Logger logger = Logger.getLogger(DoubleSpendingConstraintValidator.class.getName());

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

        return true;
    }
}

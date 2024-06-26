package org.students.simplebitcoinnode.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.students.simplebitcoinnode.dto.TransactionDTO;
import org.students.simplebitcoinnode.dto.TransactionOutputDTO;
import org.students.simplebitcoinnode.entity.validation.annotations.DoubleSpendingConstraint;
import org.students.simplebitcoinnode.repository.TransactionOutputRepository;

import java.util.logging.Logger;

/**
 * Validates if Transaction inputs have not been spent before.
 */
public class DoubleSpendingConstraintValidator implements ConstraintValidator<DoubleSpendingConstraint, TransactionDTO> {
    private final TransactionOutputRepository transactionOutputRepository;
    private final Logger logger = Logger.getLogger(DoubleSpendingConstraintValidator.class.getName());

    @Autowired
    public DoubleSpendingConstraintValidator(TransactionOutputRepository transactionOutputRepository) {
        this.transactionOutputRepository = transactionOutputRepository;
    }

    @Override
    public void initialize(DoubleSpendingConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(TransactionDTO transaction, ConstraintValidatorContext context) {
        // null value checks
        if (transaction == null)
            return false;

        // for each input check if it is a valid
        for (TransactionOutputDTO utxoCandidate : transaction.getInputs()) {
            if (transactionOutputRepository.findUtxoCountBySignature(utxoCandidate.getSignature()) < 1) {
                logger.info("Transaction output with signature '" + utxoCandidate.getSignature() + "' is not a valid UTXO, validation failed!");
                return false;
            }
        }

        return true;
    }
}

package org.students.simplebitcoinnode.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.students.simplebitcoinnode.entity.TransactionOutput;
import org.students.simplebitcoinnode.entity.validation.annotations.PositiveTransactionOutputConstraint;

import java.math.BigDecimal;
import java.util.List;

/**
 * Validator which checks that all amounts specified in transaction outputs are positive decimals
 */
public class PositiveTransactionOutputConstraintValidator
        implements ConstraintValidator<PositiveTransactionOutputConstraint, List<TransactionOutput>> {
    @Override
    public void initialize(PositiveTransactionOutputConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<TransactionOutput> outputs, ConstraintValidatorContext context) {
        for (TransactionOutput output : outputs) {
            if (output.getAmount().compareTo(BigDecimal.ZERO) <= 0)
                return false;
        }
        return true;
    }
}

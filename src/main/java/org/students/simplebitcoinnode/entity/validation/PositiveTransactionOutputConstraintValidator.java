package org.students.simplebitcoinnode.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.students.simplebitcoinnode.dto.TransactionOutputDTO;
import org.students.simplebitcoinnode.entity.validation.annotations.PositiveTransactionOutputConstraint;

import java.math.BigDecimal;
import java.util.List;

/**
 * Validator which checks that all amounts specified in transaction outputs are positive decimals
 */
public class PositiveTransactionOutputConstraintValidator
        implements ConstraintValidator<PositiveTransactionOutputConstraint, List<TransactionOutputDTO>> {
    @Override
    public void initialize(PositiveTransactionOutputConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<TransactionOutputDTO> outputs, ConstraintValidatorContext context) {
        for (TransactionOutputDTO output : outputs) {
            if (output.getAmount().compareTo(BigDecimal.ZERO) <= 0)
                return false;
        }
        return true;
    }
}

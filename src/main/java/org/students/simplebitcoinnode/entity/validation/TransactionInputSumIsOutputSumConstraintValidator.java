package org.students.simplebitcoinnode.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.students.simplebitcoinnode.dto.TransactionDTO;
import org.students.simplebitcoinnode.dto.TransactionOutputDTO;
import org.students.simplebitcoinnode.entity.validation.annotations.TransactionInputSumIsOutputSumConstraint;

import java.math.BigDecimal;

public class TransactionInputSumIsOutputSumConstraintValidator
        implements ConstraintValidator<TransactionInputSumIsOutputSumConstraint, TransactionDTO> {
    @Override
    public void initialize(TransactionInputSumIsOutputSumConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(TransactionDTO transaction, ConstraintValidatorContext context) {
        if (transaction == null)
            return false;

        // calculate the sum of transaction inputs
        BigDecimal inputSum = BigDecimal.ZERO;
        for (TransactionOutputDTO input : transaction.getInputs())
            inputSum = inputSum.add(input.getAmount());

        // calculate the sum of transaction outputs
        BigDecimal outputSum = BigDecimal.ZERO;
        for (TransactionOutputDTO output : transaction.getOutputs())
            outputSum = outputSum.add(output.getAmount());

        return inputSum.compareTo(outputSum) == 0;
    }
}

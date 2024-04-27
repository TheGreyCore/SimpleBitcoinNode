package org.students.simplebitcoinnode.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.entity.TransactionOutput;
import org.students.simplebitcoinnode.entity.validation.annotations.TransactionInputSumIsOutputSumConstraint;

import java.math.BigDecimal;

public class TransactionInputSumIsOutputSumConstraintValidator
        implements ConstraintValidator<TransactionInputSumIsOutputSumConstraint, Transaction> {
    @Override
    public void initialize(TransactionInputSumIsOutputSumConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Transaction transaction, ConstraintValidatorContext context) {
        if (transaction == null)
            return false;

        // calculate the sum of transaction inputs
        BigDecimal inputSum = BigDecimal.ZERO;
        for (TransactionOutput input : transaction.getInputs())
            inputSum = inputSum.add(input.getAmount());

        // calculate the sum of transaction outputs
        BigDecimal outputSum = BigDecimal.ZERO;
        for (TransactionOutput output : transaction.getOutputs())
            outputSum = outputSum.add(output.getAmount());

        if (inputSum.compareTo(outputSum) == 0)
            return true;
        else return false;
    }
}

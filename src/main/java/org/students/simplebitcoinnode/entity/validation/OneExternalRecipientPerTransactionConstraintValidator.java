package org.students.simplebitcoinnode.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.students.simplebitcoinnode.dto.TransactionDTO;
import org.students.simplebitcoinnode.dto.TransactionOutputDTO;
import org.students.simplebitcoinnode.entity.validation.annotations.OneExternalRecipientPerTransactionConstraint;

public class OneExternalRecipientPerTransactionConstraintValidator
        implements ConstraintValidator<OneExternalRecipientPerTransactionConstraint, TransactionDTO> {
    @Override
    public void initialize(OneExternalRecipientPerTransactionConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(TransactionDTO transaction, ConstraintValidatorContext context) {
        if (transaction == null)
            return false;

        int externalRecipientCount = 0;
        for (TransactionOutputDTO output : transaction.getOutputs()) {
            if (!output.getReceiverPublicKey().equals(transaction.getSenderPublicKey()))
                externalRecipientCount++;
        }

        return externalRecipientCount == 1;
    }
}

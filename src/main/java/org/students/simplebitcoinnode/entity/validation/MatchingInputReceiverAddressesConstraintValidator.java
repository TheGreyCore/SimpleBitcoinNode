package org.students.simplebitcoinnode.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.students.simplebitcoinnode.dto.TransactionDTO;
import org.students.simplebitcoinnode.dto.TransactionOutputDTO;
import org.students.simplebitcoinnode.entity.TransactionOutput;
import org.students.simplebitcoinnode.entity.validation.annotations.MatchingInputReceiverAddressesConstraint;
import org.students.simplebitcoinnode.repository.TransactionOutputRepository;

import java.util.Optional;

public class MatchingInputReceiverAddressesConstraintValidator
    implements ConstraintValidator<MatchingInputReceiverAddressesConstraint, TransactionDTO> {

    private final TransactionOutputRepository transactionOutputRepository;

    public MatchingInputReceiverAddressesConstraintValidator(TransactionOutputRepository transactionOutputRepository) {
        this.transactionOutputRepository = transactionOutputRepository;
    }

    @Override
    public void initialize(MatchingInputReceiverAddressesConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(TransactionDTO transaction, ConstraintValidatorContext context) {
        if (transaction == null || transaction.getInputs() == null || transaction.getInputs().isEmpty())
            return false;

        for (TransactionOutputDTO input : transaction.getInputs()) {
            String signature = input.getSignature();
            Optional<TransactionOutput> transactionOutput = transactionOutputRepository.findTransactionOutputBySignature(signature);
            if (transactionOutput.isEmpty() || !transactionOutput.get().getReceiverPublicKey().equals(input.getReceiverPublicKey()))
                return false;
        }

        return true;
    }
}

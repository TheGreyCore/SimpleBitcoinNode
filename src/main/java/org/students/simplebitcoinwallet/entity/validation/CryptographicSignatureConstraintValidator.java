package org.students.simplebitcoinwallet.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.validation.annotations.CryptographicSignatureConstraint;
import org.students.simplebitcoinwallet.entity.validation.annotations.TransactionHashConstraint;


public class CryptographicSignatureConstraintValidator implements ConstraintValidator<CryptographicSignatureConstraint, Transaction> {
    @Override
    public void initialize(CryptographicSignatureConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Transaction transaction, ConstraintValidatorContext constraintValidatorContext) {
        return false;
    }
}

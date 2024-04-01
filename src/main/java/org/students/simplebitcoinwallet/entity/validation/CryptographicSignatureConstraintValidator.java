package org.students.simplebitcoinwallet.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;
import org.students.simplebitcoinwallet.entity.validation.annotations.CryptographicSignatureConstraint;
import org.students.simplebitcoinwallet.exceptions.encoding.InvalidHexStringException;
import org.students.simplebitcoinwallet.service.CryptographyService;
import org.students.simplebitcoinwallet.util.Encoding;

import java.util.logging.Logger;


public class CryptographicSignatureConstraintValidator implements ConstraintValidator<CryptographicSignatureConstraint, Transaction> {
    @Autowired
    private CryptographyService cryptographyService;
    private Logger logger = Logger.getLogger(CryptographicSignatureConstraintValidator.class.getName());

    @Override
    public void initialize(CryptographicSignatureConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Transaction transaction, ConstraintValidatorContext constraintValidatorContext) {
        // for each transaction output verify its signature
        try {
            for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                if (!cryptographyService.verifyDigitalSignature(transaction, Encoding.hexStringToBytes(transactionOutput.getSignature()), Encoding.hexStringToBytes(transaction.getSenderPublicKey())))
                    return false;
            }
            return true;
        }
        catch (InvalidHexStringException e) {
            logger.severe("Could not parse digital signature or sender's public key");
            return false;
        }
    }
}

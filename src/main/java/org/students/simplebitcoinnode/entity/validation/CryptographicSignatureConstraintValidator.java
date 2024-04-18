package org.students.simplebitcoinnode.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.entity.TransactionOutput;
import org.students.simplebitcoinnode.entity.validation.annotations.CryptographicSignatureConstraint;
import org.students.simplebitcoinnode.exceptions.crypto.MalformedKeyException;
import org.students.simplebitcoinnode.exceptions.crypto.MalformedSignatureException;
import org.students.simplebitcoinnode.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.util.Encoding;

import java.util.logging.Logger;


/**
 * Validator class that checks that each transaction output has been properly signed with sender's public key.
 * The message whose signature is checked is constructed from the transaction hash and receiver's public key as illustrated below: <br>
 *   [HASH] + [RECEIVER_PUBKEY]<br>
 */
public class CryptographicSignatureConstraintValidator implements ConstraintValidator<CryptographicSignatureConstraint, Transaction> {
    private final AsymmetricCryptographyService asymmetricCryptographyService;
    private final Logger logger = Logger.getLogger(CryptographicSignatureConstraintValidator.class.getName());

    @Autowired
    public CryptographicSignatureConstraintValidator(AsymmetricCryptographyService asymmetricCryptographyService) {
        this.asymmetricCryptographyService = asymmetricCryptographyService;
    }

    @Override
    public void initialize(CryptographicSignatureConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Transaction transaction, ConstraintValidatorContext constraintValidatorContext) {
        // null value checks
        if (transaction == null)
            return false;

        // for each transaction output verify its signature
        try {
            for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                // null signatures get invalidated
                if (transactionOutput.getSignature() == null)
                    return false;

                // concatenate the transaction hash and output's receiver public key to get signature message
                byte[] sigMessage = new byte[120];
                System.arraycopy(asymmetricCryptographyService.digestObject(transaction), 0, sigMessage, 0, 32);
                System.arraycopy(Encoding.defaultPubKeyDecoding(transactionOutput.getReceiverPublicKey()), 0, sigMessage, 32, 88);
                if (!asymmetricCryptographyService.verifyDigitalSignature(sigMessage, Encoding.hexStringToBytes(transactionOutput.getSignature()), Encoding.defaultPubKeyDecoding(transaction.getSenderPublicKey())))
                    return false;
            }
            return true;
        }
        catch (MalformedKeyException e) {
            logger.warning("Sender's public key '" + transaction.getSenderPublicKey() + "' is malformed");
            return false;
        }
        catch (MalformedSignatureException e) {
            logger.warning("Transaction output signature is malformed");
            return false;
        }
        catch (InvalidEncodedStringException e) {
            logger.severe("Could not parse digital signature or sender's public key");
            return false;
        }
    }
}

package org.students.simplebitcoinwallet.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.bouncycastle.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;
import org.students.simplebitcoinwallet.entity.validation.annotations.CryptographicSignatureConstraint;
import org.students.simplebitcoinwallet.exceptions.crypto.MalformedKeyException;
import org.students.simplebitcoinwallet.exceptions.crypto.MalformedSignatureException;
import org.students.simplebitcoinwallet.exceptions.encoding.InvalidEncodedStringException;
import org.students.simplebitcoinwallet.service.AsymmetricCryptographyService;
import org.students.simplebitcoinwallet.util.Encoding;

import java.util.logging.Logger;


/**
 * Validator class that checks that each transaction output has been properly signed with sender's public key.
 * The message whose signature is checked is constructed from the transaction hash and receiver's public key as illustrated below: <br>
 *   [HASH] + [RECEIVER_PUBKEY]<br>
 */
public class CryptographicSignatureConstraintValidator implements ConstraintValidator<CryptographicSignatureConstraint, Transaction> {
    @Autowired
    private AsymmetricCryptographyService asymmetricCryptographyService;
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
                // concatenate the transaction hash and output's receiver public key to get signature message
                byte[] sigMessage = Arrays.concatenate(asymmetricCryptographyService.digestObject(transaction), Encoding.hexStringToBytes(transactionOutput.getReceiverPublicKey()));
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

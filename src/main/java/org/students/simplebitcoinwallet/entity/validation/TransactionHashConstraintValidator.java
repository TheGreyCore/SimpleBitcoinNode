package org.students.simplebitcoinwallet.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.validation.annotations.TransactionHashConstraint;
import org.students.simplebitcoinwallet.exceptions.encoding.SerializationException;
import org.students.simplebitcoinwallet.service.AsymmetricCryptographyService;
import org.students.simplebitcoinwallet.util.Encoding;

import java.util.logging.Logger;

/**
 * Validation class that checks if TransactionHashConstraint is satisfied or in other words it checks whether the provided transaction hash matches the actual calculated transaction hash.
 * The SHA-256 hash calculation itself is not performed on the entire serialized transaction class, because that would result in a bad circular dependency between the hash and the transaction output signature.
 * With that considered only selected fields from Transaction and TransactionOutput classes shall be serialized into a byte array which eventually gets hashed with SHA-256.<br>
 * The specific structure of the serialized hashable byte array is following:<br>
 * <ul>
 *     <li>amount of unspent transaction outputs to use as inputs (encoded as <code>int</code>)</li>
 *     <li>array of inputs</li>
 *     <ul>
 *         <li>signature (encoded as UTF-8 string)</li>
 *         <li>amount of tokens available for spending at given UTXO (encoded as <code>BigDecimal</code>)</li>
 *         <li>receiver's public key (encoded as base58 UTF-8 string)</li>
 *     </ul>
 *     <li>amount of unspent transaction outputs made by this transaction (encoded as <code>int</code>)</li>
 *     <li>array of outputs</li>
 *     <ul>
 *         <li>amount of tokens made available at this output (encoded as <code>BigDecimal</code>)</li>
 *         <li>receiver's public key (encoded as base58 UTF-8 string)</li>
 *     </ul>
 *     <li>sender's public key (encoded as base58 UTF-8 string)</li>
 *     <li>UTC timestamp, when the transaction was made (encoded as <code>LocalDateTime</code>)</li>
 * </ul>
 */
public class TransactionHashConstraintValidator implements ConstraintValidator<TransactionHashConstraint, Transaction> {
    private final Logger logger = Logger.getLogger(TransactionHashConstraintValidator.class.getName());

    @Autowired
    private AsymmetricCryptographyService asymmetricCryptographyService;

    @Override
    public void initialize(TransactionHashConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Transaction transaction, ConstraintValidatorContext constraintValidatorContext) {
        // null value checks
        if (transaction == null)
            return false;

        try {
            // return false if transaction hash is null
            if (transaction.getTransactionHash() == null)
                return false;

            return transaction.getTransactionHash().equals(Encoding.toHexString(asymmetricCryptographyService.digestObject(transaction)));
        }
        catch (SerializationException e) {
            logger.warning("Could not serialize transaction object: " + e.getMessage());
            return false;
        }
    }
}

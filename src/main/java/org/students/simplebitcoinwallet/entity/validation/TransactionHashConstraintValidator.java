package org.students.simplebitcoinwallet.entity.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;
import org.students.simplebitcoinwallet.entity.validation.annotations.TransactionHashConstraint;
import org.students.simplebitcoinwallet.util.Encoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private Logger logger = Logger.getLogger(TransactionHashConstraintValidator.class.getName());

    private byte[] serializeToByteArray(Transaction transaction) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream))
        {
            out.writeObject(transaction.getInputs().size());
            for (TransactionOutput output : transaction.getInputs()) {
                out.writeObject(output.getSignature());
                out.writeObject(output.getAmount());
                out.writeObject(output.getReceiverPublicKey());
            }
            out.writeObject(transaction.getOutputs().size());
            for (TransactionOutput output : transaction.getOutputs()) {
                out.writeObject(output.getAmount());
                out.writeObject(output.getReceiverPublicKey());
            }
            out.writeObject(transaction.getSenderPublicKey());
            out.writeObject(transaction.getTimestamp());
            out.flush();
            return byteArrayOutputStream.toByteArray();
        }
        catch (IOException e) {
            logger.severe("Failed to serialize transaction, as a result the hash verification will fail: " + e.getMessage());
        }

        return new byte[]{};
    }

    private String calculateSHA256Hash(Transaction transaction) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] digestBytes = digest.digest(serializeToByteArray(transaction));
            return Encoding.toHexString(digestBytes);
        }
        catch (NoSuchAlgorithmException e) {
            logger.severe("Current MessageDigest implementation does not support 'SHA-256' hashing: " + e.getMessage());
            return "0".repeat(64);
        }
    }

    @Override
    public void initialize(TransactionHashConstraint constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(Transaction transaction, ConstraintValidatorContext constraintValidatorContext) {
        return transaction.getTransactionHash().equals(calculateSHA256Hash(transaction));
    }
}

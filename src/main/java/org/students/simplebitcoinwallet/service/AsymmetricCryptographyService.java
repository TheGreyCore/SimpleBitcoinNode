package org.students.simplebitcoinwallet.service;

import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;
import org.students.simplebitcoinwallet.exceptions.crypto.MalformedKeyException;
import org.students.simplebitcoinwallet.exceptions.crypto.MalformedSignatureException;
import org.students.simplebitcoinwallet.exceptions.encoding.SerializationException;

import java.io.*;
import java.security.KeyPair;
import java.util.logging.Logger;

/**
 * Provides a high level interface for performing cryptographic operations such as keypair generation, signature verification and digital signing.
 */
public abstract class AsymmetricCryptographyService {
    private final Logger logger;
    protected AsymmetricCryptographyService(String className) {
        logger = Logger.getLogger(className);
    }

    /**
     * Digests given messageObject and returns its calculated hash. The specific hashing algorithm is implementation dependent
     * @param messageObject specifies the Serializable object to use as a message for hashing
     * @return byte array containing the calculated hash
     * @throws SerializationException
     */
    public abstract byte[] digestObject(Serializable messageObject) throws SerializationException;

    /**
     * Verifies if the digital signature matches signer's public key and the message that was signed.
     * @param messageObject specifies the Serializable object that composes the message
     * @param signature specifies the signature itself as a byte array
     * @param pubKey specifies the signer's public key
     * @return a boolean value with either true, if signature verification was successful, or false otherwise
     * @throws SerializationException
     * @throws MalformedKeyException
     * @throws MalformedSignatureException
     */
    public abstract boolean verifyDigitalSignature(Serializable messageObject, byte[] signature, byte[] pubKey) throws SerializationException, MalformedKeyException, MalformedSignatureException;

    /**
     * Generates a new public/private keypair
     * @return a KeyPair object representing public/private keys
     */
    public abstract KeyPair generateNewKeypair();

    /**
     * Signs given byte array composed of serialized messageObject with provided private key
     * @param messageObject specifies the message object to sign
     * @param privateKey specifies an array of bytes encoded in implementation specific manner
     * @return array of bytes containing the digital signature
     * @throws SerializationException
     * @throws MalformedKeyException
     */
    public abstract byte[] signMessage(Serializable messageObject, byte[] privateKey) throws SerializationException, MalformedKeyException;

    /**
     * Performs serialization on a transaction object.<br>
     * The specific structure of the serialized object is following:<br>
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
     * @param transaction specifies the Transaction object to byte serialize
     * @return a byte array with serialized class content
     * @throws SerializationException
     */
    private byte[] byteSerializeTransaction(Transaction transaction) throws SerializationException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream))
        {
            out.writeObject(transaction.getInputs().size());
            for (TransactionOutput transactionOutput : transaction.getInputs()) {
                out.writeObject(transactionOutput.getSignature());
                out.writeObject(transactionOutput.getAmount());
                out.writeObject(transactionOutput.getReceiverPublicKey());
            }
            out.writeObject(transaction.getOutputs().size());
            for (TransactionOutput transactionOutput : transaction.getOutputs()) {
                out.writeObject(transactionOutput.getAmount());
                out.writeObject(transactionOutput.getReceiverPublicKey());
            }
            out.writeObject(transaction.getSenderPublicKey());
            out.writeObject(transaction.getTimestamp());
            out.flush();
            return byteArrayOutputStream.toByteArray();
        }
        catch (IOException e) {
            throw new SerializationException("Failed to byte-serialize transaction object: " + e.getMessage());
        }
    }

    /**
     * Serializes given serializable object into byte array in memory.
     * @param serializable specifies the Serializable object to use
     * @return byte array containing the serialized object
     * @throws SerializationException
     */
    private byte[] byteSerializeObject(Serializable serializable) throws SerializationException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream))
        {
            out.writeObject(serializable);
            out.flush();
            return byteArrayOutputStream.toByteArray();
        }
        catch (IOException e) {
            throw new SerializationException("Failed to byte-serialize an object: " + e.getMessage());
        }
    }

    /**
     * Serialize given object according to type-specific cryptographic serialization rules
     * @param serializable specifies the object to serialize
     * @return serialized object's byte array
     * @throws SerializationException
     */
    protected byte[] byteSerialize(Serializable serializable) throws SerializationException {
        if (serializable instanceof Transaction) {
            return byteSerializeTransaction((Transaction) serializable);
        }
        return byteSerializeObject(serializable);
    }

    protected final Logger getLogger() {
        return logger;
    }
}
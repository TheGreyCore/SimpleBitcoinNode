package org.students.simplebitcoinwallet.service.impl;

import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;
import org.students.simplebitcoinwallet.service.CryptographyService;
import org.students.simplebitcoinwallet.util.Encoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.*;
import java.security.spec.*;
import java.util.logging.Logger;

public class ECDSACryptographyServiceImpl implements CryptographyService {
    private Logger logger = Logger.getLogger(ECDSACryptographyServiceImpl.class.getName());

    /**
     * Calculates SHA-256 hash from given serializable object
     * @param serializable specifies the object whose hash to calculate
     * @return array of bytes with a length of exactly 32 bytes, which represent the digested SHA-256 value. In case the hash couldn't be calculated an array of zeroes is returned.
     */
    @Override
    public byte[] sha256Digest(Serializable serializable) {
        // try to create new MessageDigest object
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // if generic T is of Transaction type then serialize it accordingly
            if (serializable.getClass().isInstance(Transaction.class)) {
                // digest as Transaction (gets special treatment)
                return digest.digest(serializeTransaction((Transaction)serializable));
            }
            // digest as regular Serializable
            return digest.digest(serializeObject(serializable));
        }
        catch (NoSuchAlgorithmException e) {
            logger.severe("Could not create a MessageDigest object with SHA-256 algorithm: " + e.getMessage());
        }
        catch (IOException e) {
            logger.severe("Could not serialize object into byte array: " + e.getMessage());
        }
        return new byte[0];
    }

    /**
     * Verifies if the digital signature matches signer's public key and the message that was signed.
     * @param messageObject specifies the Serializable object that composes the message
     * @param signature specifies the signature itself as a byte array
     * @param pubKey specifies the signer's public key
     * @return a boolean value with either true, if signature verification was successful, or false otherwise
     */
    @Override
    public boolean verifyDigitalSignature(Serializable messageObject, byte[] signature, byte[] pubKey) {
        try {
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            KeyFactory kf = KeyFactory.getInstance("EC");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubKey);
            PublicKey publicKey = kf.generatePublic(publicKeySpec);
            ecdsaVerify.initVerify(publicKey);

            // check if the object is Transaction
            if (messageObject.getClass().isInstance(Transaction.class)) {
                ecdsaVerify.update(serializeTransaction((Transaction) messageObject));
            }
            else {
                ecdsaVerify.update(serializeObject(messageObject));
            }
            return ecdsaVerify.verify(signature);
        }
        catch (NoSuchAlgorithmException e) {
            logger.severe("Could not create new Signature instance with SHA256withECDSA: " + e.getMessage());
        }
        catch (IOException e) {
            logger.severe("Object serialization failed: ");
        }
        catch (GeneralSecurityException e) {
            logger.severe("Signature check failed: : " + e.getMessage());
        }
        return false;
    }

    /**
     * Performs serialization in transaction object.<br>
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
     * @throws IOException
     */
    private byte[] serializeTransaction(Transaction transaction) throws IOException {
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
    }

    /**
     * Simply serializes given object into byte array in memory.
     * @param serializable specifies the Serializable object to use
     * @return byte array containing the serialized object
     * @throws IOException
     */
    private byte[] serializeObject(Serializable serializable) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream))
        {
            out.writeObject(serializable);
            out.flush();
            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * Quick smoke test
     */
    public static void main(String[] args) throws Exception {
        Security.insertProviderAt(new org.bouncycastle.jce.provider.BouncyCastleProvider(), 1);
        final String msg = "Hello world!";
        ECDSACryptographyServiceImpl service = new ECDSACryptographyServiceImpl();

        // generate new keypair to use
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(ecSpec, new SecureRandom());
        KeyPair keypair = generator.generateKeyPair();
        PublicKey pubKey = keypair.getPublic();
        PrivateKey privKey = keypair.getPrivate();

        // sign the message
        Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
        ecdsaSign.initSign(privKey);
        ecdsaSign.update(service.serializeObject(msg));
        byte[] signature = ecdsaSign.sign();
        System.out.println("Message: " + msg);
        System.out.println("Signature: " + Encoding.toHexString(signature));
        System.out.println("Public key: " + Encoding.toHexString(pubKey.getEncoded()));
        System.out.println("Public key length: " + pubKey.getEncoded().length);

        // check if signature verification works
        System.out.println("Signature status: " + (service.verifyDigitalSignature(msg, signature, pubKey.getEncoded())));
    }
}

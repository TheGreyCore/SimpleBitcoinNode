package org.students.simplebitcoinwallet.service;

import org.springframework.stereotype.Service;
import org.students.simplebitcoinwallet.exceptions.crypto.MalformedKeyException;
import org.students.simplebitcoinwallet.exceptions.crypto.MalformedSignatureException;
import org.students.simplebitcoinwallet.exceptions.encoding.SerializationException;

import java.io.*;
import java.security.KeyPair;
import java.util.logging.Logger;

/**
 * Provides a high level interface for performing cryptographic operations such as keypair generation, signature verification and digital signing.
 */
@Service
public abstract class AsymmetricCryptographyService {
    private final Logger logger;

    protected AsymmetricCryptographyService(String className) {
        logger = Logger.getLogger(className);
    }

    /**
     * Digests given messageObject and returns its calculated hash. The specific hashing algorithm is implementation dependent
     * @param messageObject specifies the object to use as a message for hashing
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
     * Serialize given object into byte array
     * @param serializable specifies the object to serialize
     * @return serialized object's byte array
     * @throws SerializationException
     */
    protected byte[] byteSerialize(Serializable serializable) throws SerializationException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(byteArrayOutputStream))
        {
            if (serializable instanceof Externalizable)
                ((Externalizable)serializable).writeExternal(out);
            else out.writeObject(serializable);
            out.flush();
            return byteArrayOutputStream.toByteArray();
        }
        catch (IOException e) {
            throw new SerializationException("Failed to byte-serialize an object: " + e.getMessage());
        }
    }

    protected final Logger getLogger() {
        return logger;
    }
}
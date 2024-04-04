package org.students.simplebitcoinwallet.service.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;
import org.students.simplebitcoinwallet.exceptions.crypto.CryptoProviderNotFoundException;
import org.students.simplebitcoinwallet.exceptions.crypto.MalformedKeyException;
import org.students.simplebitcoinwallet.exceptions.crypto.MalformedSignatureException;
import org.students.simplebitcoinwallet.exceptions.encoding.SerializationException;
import org.students.simplebitcoinwallet.util.Encoding;

import java.io.Serializable;
import java.security.*;
import java.security.spec.*;

/**
 * High level cryptographic service for performing ECDSA with SHA-256
 */
@Service
@Qualifier("asymmetricCryptographyService")
public class ECDSAWithSHA256CryptographicService extends AsymmetricCryptographyServiceSHA256 {
    private ECGenParameterSpec ecSpec;
    private KeyPairGenerator generator;

    public ECDSAWithSHA256CryptographicService() {
        super(ECDSAWithSHA256CryptographicService.class.getName());
        try {
            ecSpec = new ECGenParameterSpec("secp256k1");
            generator = KeyPairGenerator.getInstance("EC");
        }
        catch (NoSuchAlgorithmException e) {
            throw new CryptoProviderNotFoundException("Current crypto provider does not support elliptic-curve cryptography: " + e.getMessage());
        }
    }

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
    @Override
    public boolean verifyDigitalSignature(Serializable messageObject, byte[] signature, byte[] pubKey) throws SerializationException, MalformedKeyException, MalformedSignatureException {
        try {
            Signature ecdsaVerify = Signature.getInstance("SHA256withECDSA");
            KeyFactory kf = KeyFactory.getInstance("EC");
            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubKey);
            PublicKey publicKey = kf.generatePublic(publicKeySpec);
            ecdsaVerify.initVerify(publicKey);
            ecdsaVerify.update(digestObject(messageObject));
            return ecdsaVerify.verify(signature);
        }
        catch (NoSuchAlgorithmException e) {
            getLogger().severe("Signature verification with algorithm 'SHA256withECDSA' is not supported with current crypto provider");
            throw new CryptoProviderNotFoundException("Current crypto provider does not support requested algorithms: " + e.getMessage());
        }
        catch (InvalidKeySpecException e) {
            getLogger().severe("Malformed public key '" + Encoding.toHexString(pubKey));
            throw new MalformedKeyException("Provided public key is not encoded with X509 standard: " + e.getMessage());
        }
        catch (InvalidKeyException e) {
            throw new MalformedKeyException(e.getMessage());
        }
        catch (SignatureException e) {
            throw new MalformedSignatureException("Provided signature is malformed: " + e.getMessage());
        }
    }

    /**
     * Generates a new public/private keypair
     * @return a KeyPair object representing public/private keys
     */
    @Override
    public KeyPair generateNewKeypair() {
        try {
            generator.initialize(ecSpec, new SecureRandom());
            return generator.generateKeyPair();
        }
        catch (InvalidAlgorithmParameterException e) {
            getLogger().severe("Current crypto provider does not support elliptic-curve key generation: " + e.getMessage());
            return null;
        }
    }

    /**
     * Signs given byte array composed of serialized messageObject with provided private key
     * @param messageObject specifies the message object to sign
     * @param privateKey specifies an array of bytes encoded in implementation specific manner
     * @return array of bytes containing the digital signature
     * @throws SerializationException
     * @throws MalformedKeyException
     */
    @Override
    public byte[] signMessage(Serializable messageObject, byte[] privateKey) throws SerializationException, MalformedKeyException {
        try {
            Signature ecdsaSign = Signature.getInstance("SHA256withECDSA");
            KeyFactory kf = KeyFactory.getInstance("EC");
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey);
            PrivateKey privKey = kf.generatePrivate(privateKeySpec);
            ecdsaSign.initSign(privKey);
            ecdsaSign.update(byteSerialize(messageObject));
            return ecdsaSign.sign();
        }
        catch (NoSuchAlgorithmException e) {
            getLogger().severe("Current crypto provider does not support creating digital signatures with 'SHA256withECDSA' algorithm");
            throw new CryptoProviderNotFoundException(e.getMessage());
        }
        catch (InvalidKeySpecException | InvalidKeyException e) {
            getLogger().warning("Private key used for attempted signing was malformed");
            throw new MalformedKeyException(e.getMessage());
        }
        catch (SignatureException e) {
            // temporary fix for cases that should never happen
            throw new RuntimeException(e.getMessage());
        }
    }
}

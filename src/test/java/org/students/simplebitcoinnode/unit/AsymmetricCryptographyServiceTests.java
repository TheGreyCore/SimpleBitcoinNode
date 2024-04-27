package org.students.simplebitcoinnode.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.students.simplebitcoinnode.dto.TransactionDTO;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.unit.transaction.TestTransactionBuilder;
import org.students.simplebitcoinnode.util.Encoding;

import java.security.KeyPair;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract unit testing class for AsymmetricCryptographyService
 */
public abstract class AsymmetricCryptographyServiceTests {
    protected static AsymmetricCryptographyService asymmetricCryptographyService;

    @Test
    @DisplayName("Ensure that key pairs can be generated and they can be used for digitally signing messages")
    public void testKeyGeneration_CanBeUsedForSigning_DoesNotThrow() {
        final String messageToSign = "Hello world!";
        assertDoesNotThrow(() -> {
            KeyPair keyPair = asymmetricCryptographyService.generateNewKeypair();
            assertTrue(asymmetricCryptographyService.signMessage(messageToSign, keyPair.getPrivate().getEncoded()).length > 0); // ensures that the signature is not empty
        });
    }

    @Test
    @DisplayName("Ensure that cryptographically signed transactions can be verified based on the public key")
    public void testTransactionSignatureVerification_ValidSignature_DoesNotThrow() {
        // key pair acts as our "wallet"
        KeyPair keyPair = asymmetricCryptographyService.generateNewKeypair();
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(Encoding.defaultPubKeyEncoding(keyPair.getPublic().getEncoded()), "1".repeat(64));

        // the transaction is hashed
        try {
            transaction.setTransactionHash(Encoding.toHexString(asymmetricCryptographyService.digestObject(transaction)));
            byte[] signature = asymmetricCryptographyService.signMessage(transaction, keyPair.getPrivate().getEncoded());
            assertTrue(asymmetricCryptographyService.verifyDigitalSignature(transaction, signature, keyPair.getPublic().getEncoded()));
        }
        catch (Exception e) {
            fail();
        }
    }
}

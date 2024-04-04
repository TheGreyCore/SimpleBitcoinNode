package org.students.simplebitcoinwallet.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;
import org.students.simplebitcoinwallet.service.AsymmetricCryptographyService;
import org.students.simplebitcoinwallet.unit.transaction.TestTransactionBuilder;
import org.students.simplebitcoinwallet.util.Encoding;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.Arrays;

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
        Transaction transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(Encoding.defaultPubKeyEncoding(keyPair.getPublic().getEncoded()), "0".repeat(177));

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

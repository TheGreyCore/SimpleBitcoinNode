package org.students.simplebitcoinnode.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.students.simplebitcoinnode.dto.TransactionDTO;
import org.students.simplebitcoinnode.dto.TransactionOutputDTO;
import org.students.simplebitcoinnode.entity.validation.CryptographicSignatureConstraintValidator;
import org.students.simplebitcoinnode.entity.validation.DoubleSpendingConstraintValidator;
import org.students.simplebitcoinnode.entity.validation.TransactionHashConstraintValidator;
import org.students.simplebitcoinnode.repository.TransactionOutputRepository;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.unit.transaction.TestTransactionBuilder;
import org.students.simplebitcoinnode.util.Encoding;

import java.io.Serializable;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;


/**
 * Unit tests to check if transaction validations work as expected
 */
@ExtendWith(MockitoExtension.class)
public class TransactionValidationTests {
    @Mock
    private TransactionOutputRepository transactionOutputRepository;
    @Mock
    private AsymmetricCryptographyService asymmetricCryptographyService;

    private final String senderPublicKey;
    private final String recipientPublicKey;

    public TransactionValidationTests() {
        byte[] senderPublicKey = new byte[88];
        Arrays.fill(senderPublicKey, (byte)1);
        byte[] recipientPublicKey = new byte[88];
        Arrays.fill(recipientPublicKey, (byte)2);

        this.senderPublicKey = Encoding.defaultPubKeyEncoding(senderPublicKey);
        this.recipientPublicKey = Encoding.defaultPubKeyEncoding(recipientPublicKey);
    }

    @Test
    @DisplayName("Ensure that correct transaction does not give validation errors")
    public void testValidTransaction_ExpectNoValidationErrors() throws Exception {
        final String fakeSignatureStr = "0".repeat(144);
        final String fakeHashStr = "0".repeat(64);

        // create a test transaction object
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);
        transaction.setTransactionHash(fakeHashStr);

        // for each transaction output, set fake signature
        for (TransactionOutputDTO output : transaction.getOutputs())
            output.setSignature(fakeSignatureStr);

        // mock digestObject(), presumably used in TransactionHashConstraint
        given(asymmetricCryptographyService.digestObject(transaction))
            .willReturn(Encoding.hexStringToBytes(fakeHashStr));
        // mock verifySignature(), presumably used in CryptographicSignatureConstraint
        given(asymmetricCryptographyService.verifyDigitalSignature(any(Serializable.class), any(), any()))
                .willReturn(true);
        // mock findUtxoCountBySignature(), presumably used in DoubleSpendingConstraint
        given(transactionOutputRepository.findUtxoCountBySignature(anyString()))
                .willReturn(0);

        // construct validators
        DoubleSpendingConstraintValidator doubleSpendingConstraintValidator = new DoubleSpendingConstraintValidator(transactionOutputRepository);
        TransactionHashConstraintValidator transactionHashConstraintValidator = new TransactionHashConstraintValidator(asymmetricCryptographyService);
        CryptographicSignatureConstraintValidator cryptographicSignatureConstraintValidator = new CryptographicSignatureConstraintValidator(asymmetricCryptographyService);

        assertTrue(doubleSpendingConstraintValidator.isValid(transaction, null));
        assertTrue(transactionHashConstraintValidator.isValid(transaction, null));
        assertTrue(cryptographicSignatureConstraintValidator.isValid(transaction, null));
    }

    /* CryptographicSignatureConstraint violations */
    @Test
    @DisplayName("Ensure that validation error is given when signature is null")
    public void testInvalidTransaction_NullSignature_ExpectCryptographicSignatureValidationError() throws Exception {
        // create a test transaction object
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);
        transaction.setTransactionHash("0".repeat(64));

        given(asymmetricCryptographyService.digestObject(transaction))
            .willReturn(Encoding.hexStringToBytes("0".repeat(64)));

        CryptographicSignatureConstraintValidator cryptographicSignatureConstraintValidator = new CryptographicSignatureConstraintValidator(asymmetricCryptographyService);
        assertFalse(cryptographicSignatureConstraintValidator.isValid(transaction, null));
    }

    @Test
    @DisplayName("Ensure that validation error is given when signature is invalid")
    public void testInvalidTransaction_InvalidSignature_ExpectCryptographicSignatureValidationError() throws Exception {
        final String fakeHashStr = "0".repeat(64);

        // create a test transaction object
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);
        transaction.setTransactionHash(fakeHashStr);

        // set some random signatures to outputs
        for (int i = 0; i < transaction.getOutputs().size(); i++) {
            transaction.getOutputs().get(i).setSignature(("" + i).repeat(144));
        }

        // mock digestObject(), presumably used in TransactionHashConstraint and CryptographicSignatureConstraint
        given(asymmetricCryptographyService.digestObject(transaction))
            .willReturn(Encoding.hexStringToBytes(fakeHashStr));
        given(asymmetricCryptographyService.verifyDigitalSignature(any(Serializable.class), any(), any()))
            .willReturn(false);

        CryptographicSignatureConstraintValidator cryptographicSignatureConstraintValidator = new CryptographicSignatureConstraintValidator(asymmetricCryptographyService);
        assertFalse(cryptographicSignatureConstraintValidator.isValid(transaction, null));
    }

    @Test
    @DisplayName("Ensure that exceptions are handled when malformed signature format is given")
    public void testInvalidTransaction_MalformedSignature_ExpectNoExceptionsAndCryptographicSignatureValidationError() throws Exception {
        final String fakeHashStr = "0".repeat(64);
        // create a test transaction object
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);
        transaction.setTransactionHash(fakeHashStr);

        given(asymmetricCryptographyService.digestObject(transaction))
            .willReturn(Encoding.hexStringToBytes(fakeHashStr));

        // set malformed signatures to transaction outputs
        for (TransactionOutputDTO output : transaction.getOutputs()) {
            output.setSignature("Hello, I am an invalid signature as you can see.");
        }

        CryptographicSignatureConstraintValidator cryptographicSignatureConstraintValidator = new CryptographicSignatureConstraintValidator(asymmetricCryptographyService);
        try {
            assertFalse(cryptographicSignatureConstraintValidator.isValid(transaction, null));
        }
        catch (Exception e) {
            fail();
        }
    }

    /* DoubleSpendingConstraint violations */
    @Test
    @DisplayName("Ensure that reusing already used Transaction output as new Transaction's input gives DoubleSpendingConstraint error")
    public void testInvalidTransaction_DoubleSpending_ExpectDoubleSpendingConstraintError() {
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);
        transaction.setTransactionHash("0".repeat(64));

        // set random signatures
        for (TransactionOutputDTO output : transaction.getOutputs()) {
            output.setSignature("0".repeat(144));
        }

        // mock findUtxoCountBySignature(), presumably used in DoubleSpendingConstraint
        given(transactionOutputRepository.findUtxoCountBySignature(any()))
            .willReturn(1);

        DoubleSpendingConstraintValidator doubleSpendingConstraintValidator = new DoubleSpendingConstraintValidator(transactionOutputRepository);
        assertFalse(doubleSpendingConstraintValidator.isValid(transaction, null));
    }

    /* TransactionHashConstraint violations */
    @Test
    @DisplayName("Ensure that TransactionHashConstraint error is given if hash mismatch happens")
    public void testInvalidTransaction_MismatchingHashes_ExpectTransactionHashConstraintError() throws Exception {
        final String fakeHashStr = "0".repeat(64);
        final String calculatedHashStr = "1".repeat(64);

        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);
        transaction.setTransactionHash(fakeHashStr);

        // set signatures
        for (TransactionOutputDTO output : transaction.getOutputs()) {
            output.setSignature("0".repeat(144));
        }

        // mock digestObject(), presumably used in TransactionHashConstraint
        given(asymmetricCryptographyService.digestObject(transaction))
            .willReturn(Encoding.hexStringToBytes(calculatedHashStr));

        TransactionHashConstraintValidator transactionHashConstraintValidator = new TransactionHashConstraintValidator(asymmetricCryptographyService);
        assertFalse(transactionHashConstraintValidator.isValid(transaction, null));
    }

    @Test
    @DisplayName("Ensure that TransactionHashConstraint error is given and no exceptions thrown when specified transaction hash is null")
    public void testInvalidTransaction_NullHash_ExpectTransactionHashConstraintErrorDoesNotThrow() {
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);

        // set random signatures
        for (TransactionOutputDTO output : transaction.getOutputs()) {
            output.setSignature("0".repeat(144));
        }

        TransactionHashConstraintValidator transactionHashConstraintValidator = new TransactionHashConstraintValidator(asymmetricCryptographyService);
        try {
            assertFalse(transactionHashConstraintValidator.isValid(transaction, null));
        }
        catch (Exception e) {
            fail();
        }
    }
}

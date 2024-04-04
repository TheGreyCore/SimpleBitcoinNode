package org.students.simplebitcoinwallet.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.students.simplebitcoinwallet.entity.Transaction;
import org.students.simplebitcoinwallet.entity.TransactionOutput;
import org.students.simplebitcoinwallet.entity.validation.CryptographicSignatureConstraintValidator;
import org.students.simplebitcoinwallet.entity.validation.DoubleSpendingConstraintValidator;
import org.students.simplebitcoinwallet.entity.validation.TransactionHashConstraintValidator;
import org.students.simplebitcoinwallet.repository.TransactionOutputRepository;
import org.students.simplebitcoinwallet.service.AsymmetricCryptographyService;
import org.students.simplebitcoinwallet.unit.transaction.TestTransactionBuilder;
import org.students.simplebitcoinwallet.util.Encoding;

import java.io.Serializable;

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

    @Test
    @DisplayName("Ensure that correct transaction does not give validation errors")
    public void testValidTransaction_ExpectNoValidationErrors() throws Exception {
        // create a test transaction object
        Transaction transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys("1".repeat(176), "0".repeat(176));
        transaction.setTransactionHash("0".repeat(32));

        final String fakeSignatureStr = "0".repeat(64);

        // for each transaction output, set fake signature
        for (TransactionOutput output : transaction.getOutputs())
            output.setSignature(fakeSignatureStr);

        // mock digestObject(), presumably used in TransactionHashConstraint
        given(asymmetricCryptographyService.digestObject(transaction))
            .willReturn(Encoding.hexStringToBytes("0".repeat(32)));
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

        assertTrue(doubleSpendingConstraintValidator.isValid(transaction, null) &&
                    transactionHashConstraintValidator.isValid(transaction, null) &&
                    cryptographicSignatureConstraintValidator.isValid(transaction, null));
    }

    /* CryptographicSignatureConstraint violations */
    @Test
    @DisplayName("Ensure that validation error is given when signature is null")
    public void testInvalidTransaction_NullSignature_ExpectCryptographicSignatureValidationError()  {
        // create a test transaction object
        Transaction transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys("1".repeat(176), "0".repeat(176));
        transaction.setTransactionHash("0".repeat(32));

        CryptographicSignatureConstraintValidator cryptographicSignatureConstraintValidator = new CryptographicSignatureConstraintValidator(asymmetricCryptographyService);
        assertFalse(cryptographicSignatureConstraintValidator.isValid(transaction, null));
    }

    @Test
    @DisplayName("Ensure that validation error is given when signature is invalid")
    public void testInvalidTransaction_InvalidSignature_ExpectCryptographicSignatureValidationError() throws Exception {
        // create a test transaction object
        Transaction transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys("1".repeat(176), "0".repeat(176));
        transaction.setTransactionHash("0".repeat(32));

        // set some random signatures to outputs
        for (int i = 0; i < transaction.getOutputs().size(); i++) {
            transaction.getOutputs().get(i).setSignature(("" + i).repeat(64));
        }

        // mock digestObject(), presumably used in TransactionHashConstraint and CryptographicSignatureConstraint
        given(asymmetricCryptographyService.digestObject(transaction))
            .willReturn(Encoding.hexStringToBytes("0".repeat(32)));
        // mock verifySignature(), presumably used in CryptographicSignatureConstraint
        given(asymmetricCryptographyService.verifyDigitalSignature(any(Serializable.class), any(), any()))
            .willReturn(false);

        CryptographicSignatureConstraintValidator cryptographicSignatureConstraintValidator = new CryptographicSignatureConstraintValidator(asymmetricCryptographyService);
        assertFalse(cryptographicSignatureConstraintValidator.isValid(transaction, null));
    }

    @Test
    @DisplayName("Ensure that exceptions are handled when malformed signature format is given")
    public void testInvalidTransaction_MalformedSignature_ExpectNoExceptionsAndCryptographicSignatureValidationError() {
        // create a test transaction object
        Transaction transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys("1".repeat(176), "0".repeat(176));
        transaction.setTransactionHash("0".repeat(32));

        // set malformed signatures to transaction outputs
        for (TransactionOutput output : transaction.getOutputs()) {
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
        Transaction transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys("1".repeat(176), "0".repeat(176));
        transaction.setTransactionHash("0".repeat(32));

        // set random signatures
        for (TransactionOutput output : transaction.getOutputs()) {
            output.setSignature("0".repeat(64));
        }

        // mock findUtxoCountBySignature(), presumably used in DoubleSpendingConstraint
        given(transactionOutputRepository.findUtxoCountBySignature(any()))
            .willReturn(1);

        DoubleSpendingConstraintValidator doubleSpendingConstraintValidator = new DoubleSpendingConstraintValidator(transactionOutputRepository);
        assertFalse(doubleSpendingConstraintValidator.isValid(transaction, null));
    }

    /* TransactionHashConstraint violations */
    @Test
    @DisplayName("Ensure that TransactionHashConstraint error is given if calculated mismatch happens")
    public void testInvalidTransaction_MismatchingHashes_ExpectTransactionHashConstraintError() throws Exception {
        Transaction transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys("1".repeat(176), "0".repeat(176));
        transaction.setTransactionHash("0".repeat(32));

        // set signatures
        for (TransactionOutput output : transaction.getOutputs()) {
            output.setSignature("0".repeat(64));
        }

        // mock digestObject(), presumably used in TransactionHashConstraint
        given(asymmetricCryptographyService.digestObject(transaction))
                .willReturn(Encoding.hexStringToBytes("1".repeat(32)));

        TransactionHashConstraintValidator transactionHashConstraintValidator = new TransactionHashConstraintValidator(asymmetricCryptographyService);
        assertFalse(transactionHashConstraintValidator.isValid(transaction, null));
    }

    @Test
    @DisplayName("Ensure that TransactionHashConstraint error is given and no exceptions thrown when specified transaction hash is null")
    public void testInvalidTransaction_NullHash_ExpectTransactionHashConstraintErrorDoesNotThrow() {
        Transaction transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys("1".repeat(176), "0".repeat(176));

        // set random signatures
        for (TransactionOutput output : transaction.getOutputs()) {
            output.setSignature("0".repeat(64));
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

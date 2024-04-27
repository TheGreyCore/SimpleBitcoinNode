package org.students.simplebitcoinnode.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.students.simplebitcoinnode.dto.TransactionDTO;
import org.students.simplebitcoinnode.dto.TransactionOutputDTO;
import org.students.simplebitcoinnode.entity.TransactionOutput;
import org.students.simplebitcoinnode.entity.validation.*;
import org.students.simplebitcoinnode.repository.TransactionOutputRepository;
import org.students.simplebitcoinnode.service.AsymmetricCryptographyService;
import org.students.simplebitcoinnode.unit.transaction.TestTransactionBuilder;
import org.students.simplebitcoinnode.util.DTOMapperWrapper;
import org.students.simplebitcoinnode.util.Encoding;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

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

    private final DTOMapperWrapper dtoMapperWrapper = new DTOMapperWrapper();

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

        for (TransactionOutputDTO input : transaction.getInputs()) {
            given(transactionOutputRepository.findTransactionOutputBySignature(input.getSignature()))
                    .willReturn(Optional.of(dtoMapperWrapper.map(input, TransactionOutput.class)));
        }

        // construct validators
        DoubleSpendingConstraintValidator doubleSpendingConstraintValidator = new DoubleSpendingConstraintValidator(transactionOutputRepository);
        TransactionHashConstraintValidator transactionHashConstraintValidator = new TransactionHashConstraintValidator(asymmetricCryptographyService);
        CryptographicSignatureConstraintValidator cryptographicSignatureConstraintValidator = new CryptographicSignatureConstraintValidator(asymmetricCryptographyService);
        MatchingInputReceiverAddressesConstraintValidator matchingInputReceiverAddressesConstraintValidator = new MatchingInputReceiverAddressesConstraintValidator(transactionOutputRepository);
        OneExternalRecipientPerTransactionConstraintValidator oneExternalRecipientPerTransactionConstraintValidator = new OneExternalRecipientPerTransactionConstraintValidator();
        PositiveTransactionOutputConstraintValidator positiveTransactionOutputConstraintValidator = new PositiveTransactionOutputConstraintValidator();
        TransactionInputSumIsOutputSumConstraintValidator transactionInputSumIsOutputSumConstraintValidator = new TransactionInputSumIsOutputSumConstraintValidator();

        assertTrue(doubleSpendingConstraintValidator.isValid(transaction, null));
        assertTrue(transactionHashConstraintValidator.isValid(transaction, null));
        assertTrue(cryptographicSignatureConstraintValidator.isValid(transaction, null));
        assertTrue(matchingInputReceiverAddressesConstraintValidator.isValid(transaction, null));
        assertTrue(oneExternalRecipientPerTransactionConstraintValidator.isValid(transaction, null));
        assertTrue(positiveTransactionOutputConstraintValidator.isValid(transaction.getOutputs(), null));
        assertTrue(transactionInputSumIsOutputSumConstraintValidator.isValid(transaction, null));
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

    @Test
    @DisplayName("Ensure that validation error is given when sender address doesn't match all receiver addresses specified in transaction inputs")
    public void testInvalidTransaction_InvalidTransactionInputReceiverAddress_ExpectValidationError() {
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);
        // set invalid input address
        transaction.getInputs().getFirst().setReceiverPublicKey(recipientPublicKey);

        given(transactionOutputRepository.findTransactionOutputBySignature(transaction.getInputs().getFirst().getSignature()))
                .willReturn(Optional.of(dtoMapperWrapper.map(transaction.getInputs().getFirst(), TransactionOutput.class)));

        MatchingInputReceiverAddressesConstraintValidator matchingInputReceiverAddressesConstraintValidator =
                new MatchingInputReceiverAddressesConstraintValidator(transactionOutputRepository);

        assertFalse(matchingInputReceiverAddressesConstraintValidator.isValid(transaction, null));
    }

    @Test
    @DisplayName("Ensure that validation error is given when one of transaction inputs are not found in the database")
    public void testInvalidTransaction_TransactionInputsAreNotFound_ExceptValidationError() {
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);

        // mock the repository in way that empty Optional object is returned
        given(transactionOutputRepository.findTransactionOutputBySignature(anyString()))
            .willReturn(Optional.empty());

        MatchingInputReceiverAddressesConstraintValidator matchingInputReceiverAddressesConstraintValidator =
                new MatchingInputReceiverAddressesConstraintValidator(transactionOutputRepository);

        assertFalse(matchingInputReceiverAddressesConstraintValidator.isValid(transaction, null));
    }

    @Test
    @DisplayName("Ensure that validation error is given when multiple external recipients addresses are specified in transaction outputs")
    public void testInvalidTransaction_TransactionOutputsSpecifyMultipleExternal_Recipients() {
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);

        // change all transaction output destination addresses to external ones
        for (TransactionOutputDTO output : transaction.getOutputs()) {
            output.setReceiverPublicKey(recipientPublicKey);
        }

        OneExternalRecipientPerTransactionConstraintValidator oneExternalRecipientPerTransactionConstraintValidator =
                new OneExternalRecipientPerTransactionConstraintValidator();
        assertFalse(oneExternalRecipientPerTransactionConstraintValidator.isValid(transaction, null));

        // change all transaction output destination addresses to sender's address
        for (TransactionOutputDTO output : transaction.getOutputs()) {
            output.setReceiverPublicKey(senderPublicKey);
        }

        assertFalse(oneExternalRecipientPerTransactionConstraintValidator.isValid(transaction, null));
    }

    @Test
    @DisplayName("Ensure that validation error is given when at least one of transaction outputs specify a negative amount to transfer")
    public void testInvalidTransaction_TransactionOutputsSpecifyNegativeAmount_ExpectValidationError() {
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);
        BigDecimal inputSum = BigDecimal.ZERO;
        for (TransactionOutputDTO output : transaction.getOutputs())
            inputSum = inputSum.add(output.getAmount());

        transaction.getOutputs().getFirst().setAmount(new BigDecimal(-1000));
        transaction.getOutputs().get(1).setAmount(inputSum.subtract(new BigDecimal(-1000)));

        PositiveTransactionOutputConstraintValidator positiveTransactionOutputConstraintValidator =
                new PositiveTransactionOutputConstraintValidator();
        assertFalse(positiveTransactionOutputConstraintValidator.isValid(transaction.getOutputs(), null));
    }

    @Test
    @DisplayName("Ensure that validation error is given when transaction input sum is smaller than transaction output sum")
    public void testInvalidTransaction_TransactionInputSumIsSmallerThanTransactionOutputSum_ExpectValidationError() {
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);
        transaction.getInputs().getFirst().setAmount(new BigDecimal("0.0001"));

        TransactionInputSumIsOutputSumConstraintValidator transactionInputSumIsOutputSumConstraintValidator =
                new TransactionInputSumIsOutputSumConstraintValidator();

        assertFalse(transactionInputSumIsOutputSumConstraintValidator.isValid(transaction, null));
    }

    @Test
    @DisplayName("Ensure that validation error is given when transaction input sum is larger than transaction input sum")
    public void testInvalidTransaction_TransactionInputSumIsLargerThanTransactionInputSum_ExpectValidationError() {
        TransactionDTO transaction = TestTransactionBuilder.aliceSendsToBobCustomKeys(senderPublicKey, recipientPublicKey);
        transaction.getInputs().getFirst().setAmount(new BigDecimal(100000));

        TransactionInputSumIsOutputSumConstraintValidator transactionInputSumIsOutputSumConstraintValidator =
                new TransactionInputSumIsOutputSumConstraintValidator();

        assertFalse(transactionInputSumIsOutputSumConstraintValidator.isValid(transaction, null));
    }
}

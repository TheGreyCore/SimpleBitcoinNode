package org.students.simplebitcoinnode.unit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.students.simplebitcoinnode.dto.TransactionDTO;
import org.students.simplebitcoinnode.dto.TransactionOutputDTO;
import org.students.simplebitcoinnode.entity.Transaction;
import org.students.simplebitcoinnode.entity.TransactionOutput;
import org.students.simplebitcoinnode.util.DTOMapperWrapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DTOMapperWrapperTests {
    private final DTOMapperWrapper dtoMapperWrapper = new DTOMapperWrapper();

    @Test
    @DisplayName("Test mapping all Transaction objects to destination DTO objects")
    public void testMapAll() {
        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setSignature("signature");
        transactionOutput.setAmount(new BigDecimal(100));
        transactionOutput.setReceiverPublicKey("receiverPublicKey");

        List<TransactionOutputDTO> result = dtoMapperWrapper.mapAll(List.of(transactionOutput), TransactionOutputDTO.class);

        assertEquals(1, result.size());
        assertEquals("signature", result.getFirst().getSignature());
        assertEquals(new BigDecimal(100), result.getFirst().getAmount());
        assertEquals("receiverPublicKey", result.getFirst().getReceiverPublicKey());
    }

    @Test
    @DisplayName("Test mapping a Transaction object to a destination DTO object")
    public void testMap() {
        Transaction transaction = new Transaction();
        transaction.setTransactionHash("transactionHash");
        transaction.setSenderPublicKey("senderPublicKey");
        transaction.setTimestamp(LocalDateTime.now());

        TransactionDTO result = dtoMapperWrapper.map(transaction, TransactionDTO.class);

        assertEquals("transactionHash", result.getTransactionHash());
        assertEquals("senderPublicKey", result.getSenderPublicKey());
        // Use isEqualIgnoringNanos() because ModelMapper may not keep nanoseconds when mapping LocalDateTime
        assertEquals(transaction.getTimestamp().withNano(0), result.getTimestamp().withNano(0));
    }

    @Test
    @DisplayName("Test unmapping a DTO object to a Transaction object")
    public void testUnmap() {
        TransactionDTO getTransactionDTO = new TransactionDTO();
        getTransactionDTO.setTransactionHash("transactionHash");
        getTransactionDTO.setSenderPublicKey("senderPublicKey");
        getTransactionDTO.setTimestamp(LocalDateTime.now());

        Transaction result = dtoMapperWrapper.unmap(getTransactionDTO, Transaction.class);

        assertEquals("transactionHash", result.getTransactionHash());
        assertEquals("senderPublicKey", result.getSenderPublicKey());
        assertEquals(getTransactionDTO.getTimestamp().withNano(0), result.getTimestamp().withNano(0));
    }

    @Test
    @DisplayName("Test mapping a Transaction object to a destination DTO object")
    public void testMapNewTransactionDTO() {
        Transaction transaction = new Transaction();
        transaction.setTransactionHash("transactionHash");
        transaction.setSenderPublicKey("senderPublicKey");
        transaction.setTimestamp(LocalDateTime.now());

        TransactionOutput transactionOutput = new TransactionOutput();
        transactionOutput.setSignature("signature");
        transactionOutput.setAmount(new BigDecimal(100));
        transactionOutput.setReceiverPublicKey("receiverPublicKey");

        transaction.setInputs(List.of(transactionOutput));
        transaction.setOutputs(List.of(transactionOutput));

        TransactionDTO result = dtoMapperWrapper.map(transaction, TransactionDTO.class);

        assertEquals("transactionHash", result.getTransactionHash());
        assertEquals("senderPublicKey", result.getSenderPublicKey());
        // Use isEqualIgnoringNanos() because ModelMapper may not keep nanoseconds when mapping LocalDateTime
        assertEquals(transaction.getTimestamp().withNano(0), result.getTimestamp().withNano(0));
        assertEquals(1, result.getInputs().size());
        assertEquals(1, result.getOutputs().size());
    }

    @Test
    @DisplayName("Test unmapping a DTO object to a newTransaction object")
    public void testUnmapNewTransactionDTO() {
        TransactionDTO newTransactionDTO = new TransactionDTO();
        newTransactionDTO.setTransactionHash("transactionHash");
        newTransactionDTO.setSenderPublicKey("senderPublicKey");
        newTransactionDTO.setTimestamp(LocalDateTime.now());

        TransactionOutputDTO transactionOutputDTO = new TransactionOutputDTO();
        transactionOutputDTO.setSignature("signature");
        transactionOutputDTO.setAmount(new BigDecimal(100));
        transactionOutputDTO.setReceiverPublicKey("receiverPublicKey");

        newTransactionDTO.setInputs(List.of(transactionOutputDTO));
        newTransactionDTO.setOutputs(List.of(transactionOutputDTO));

        Transaction result = dtoMapperWrapper.unmap(newTransactionDTO, Transaction.class);

        assertEquals("transactionHash", result.getTransactionHash());
        assertEquals("senderPublicKey", result.getSenderPublicKey());
        // Use isEqualIgnoringNanos() because ModelMapper may not keep nanoseconds when mapping LocalDateTime
        assertEquals(newTransactionDTO.getTimestamp().withNano(0), result.getTimestamp().withNano(0));
        assertEquals(1, result.getInputs().size());
        assertEquals(1, result.getOutputs().size());
    }
}


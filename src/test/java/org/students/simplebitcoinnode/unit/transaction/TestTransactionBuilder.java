package org.students.simplebitcoinnode.unit.transaction;

import org.students.simplebitcoinnode.dto.TransactionDTO;
import org.students.simplebitcoinnode.dto.TransactionOutputDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class TestTransactionBuilder {

    /**
     * Builds a new transaction where Alice sends some of her tokens to Bob.<br>
     * Constraint validity depends on provided keys
     * @param senderPublicKey specifies the sender public key to use (encoded as string)
     * @param receiverPublicKey specifies the receiver's public key to use (encoded as string)
     * @return constructed Transaction object
     */
    public static TransactionDTO aliceSendsToBobCustomKeys(String senderPublicKey, String receiverPublicKey) {
        // Bob tries to send some of his tokens to Alice
        return TransactionDTO.builder()
            .inputs(Arrays.asList(
                // Bob received 1.5 BTC from Alice
                TransactionOutputDTO.builder()
                    .signature("0".repeat(144)) // some random input signature
                    .amount(new BigDecimal("1.5"))
                    .receiverPublicKey(senderPublicKey)
                    .build(),
                // Bob also received 2.4 BTC from Charlie
                TransactionOutputDTO.builder()
                    .signature("1".repeat(144)) // -"-
                    .amount(new BigDecimal("2.4"))
                    .receiverPublicKey(senderPublicKey)
                    .build()
                ))
                .outputs(Arrays.asList(
                    // Bob sends 3 BTC to Alice
                    TransactionOutputDTO.builder()
                        .amount(new BigDecimal("3.0"))
                        .receiverPublicKey(receiverPublicKey)
                        .signature("0".repeat(144))
                        .build(),
                    // 0.9 BTC is transferred back to Bob
                    TransactionOutputDTO.builder()
                        .amount(new BigDecimal("0.9"))
                        .receiverPublicKey(senderPublicKey)
                        .signature("1".repeat(144))
                        .build()
                ))
                .senderPublicKey(senderPublicKey)
                .timestamp(LocalDateTime.of(2006, 1, 1, 0, 0, 0))
                .build();
    }

    /**
     * Builds all transactions into a list
     * @return a list containing TransactionContainer instances
     */
    public static List<TransactionContainer> buildAllTransactions() {
        return List.of(
            new TransactionContainer("aliceSendsToBobZeroSenderKeyOneReceiverKey", aliceSendsToBobCustomKeys("1".repeat(64), "2".repeat(176)), null)
        );
    }
}

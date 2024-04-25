package org.students.simplebitcoinwallet.dataTransferObjects;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class TransactionOutputDTO {
    private String signature;
    private BigDecimal amount;
    private String receiverPublicKey;
}
